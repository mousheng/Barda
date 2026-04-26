import { useCallback, useRef, useState, useEffect, memo } from "react";
import styled from "styled-components";

// ─── Types ────────────────────────────────────────────────────────────

type NodeType = "object" | "array" | "string" | "number" | "boolean" | "bigint" | "null" | "undefined";

interface FlatNode {
  key: string;
  name: string;
  depth: number;
  raw: any;
  type: NodeType;
  expandable: boolean;
  childCount: number;
  expanded: boolean;
  isChunk?: boolean;
  chunkStart?: number;
  chunkEnd?: number;
  /** 子树展开节点数（不含自身），用于 O(1) collapse */
  subSize: number;
  /** 字符串截断缓存 */
  preview?: string;
}

interface Props {
  src: any;
  rowHeight?: number;
}

// ─── CSS ──────────────────────────────────────────────────────────────

const Tree = styled.div`
  overflow-y: auto; overflow-x: overlay; height: 100%;
  font-family: "RobotoMono", monospace;
  font-size: 13px; line-height: 20px; user-select: text;

  .vr {
    display: flex; align-items: center; height: 20px; white-space: nowrap;
    transition: background 0.08s;
    &:hover { background: rgba(255,255,255,0.04); cursor: pointer; }
  }
  .va {
    display: inline-flex; align-items: center; justify-content: center;
    width: 18px; height: 18px; flex-shrink: 0;
    color: #6b7280; font-size: 10px; transition: transform .12s, color .12s;
    &::before { content: "\\25B6"; }
    &:hover { color: #374151; }
  }
  .va.o { transform: rotate(90deg); }
  .v0 { display: inline-flex; width: 18px; flex-shrink: 0; }
  .vk { color: #4965f2; margin-right: 4px; flex-shrink: 0; overflow: hidden; text-overflow: ellipsis; max-width: 40%; }
  .vc { color: #888; margin-right: 6px; }
  .vs { color: #ce9178; }    /* string */
  .vn { color: #b5cea8; }    /* number */
  .vb { color: #569cd6; }    /* boolean */
  .vx { color: #808080; font-style: italic; }  /* null / undefined */
  .vm { color: #9ca3af; margin-left: 6px; font-size: 12px; }
  .vp { color: #9ca3af; margin-left: 4px; }
  .vbk { color: #d4d4d4; }   /* bracket */
  .copy-btn {
    visibility: hidden; display: inline-flex; align-items: center;
    padding: 0 4px; cursor: pointer;
    color: #888; font-size: 11px; flex-shrink: 0; user-select: none;
  }
  .vr:hover .copy-btn { visibility: visible; }
  .copy-btn:hover { color: #ccc; }
`;

// ─── Helpers ──────────────────────────────────────────────────────────

const CHUNK_SIZE = 100;

function t(val: any): NodeType {
  if (val === null) return "null";
  if (val === undefined) return "undefined";
  const tp = typeof val;
  if (tp === "string") return "string";
  if (tp === "number") return "number";
  if (tp === "bigint") return "bigint";
  if (tp === "boolean") return "boolean";
  if (Array.isArray(val)) return "array";
  if (tp === "object") return "object";
  return "undefined";
}

function cc(val: any, type: NodeType): number {
  if (type === "array") return (val as any[]).length;
  if (type === "object") return Object.keys(val as object).length;
  return 0;
}

function trunc(s: string): string {
  return s.length > 2000 ? s.slice(0, 2000) + "…" : s;
}

function previewValue(val: any, type: NodeType, start?: number, end?: number): string {
  if (type === "array") {
    const arr = val as any[];
    const slice = start !== undefined ? arr.slice(start, end) : arr;
    return "[" + slice.slice(0, 3).map(v => JSON.stringify(v)).join(", ") +
      (slice.length > 3 ? ", …]" : "]");
  }
  if (type === "object") {
    const keys = Object.keys(val);
    const sliceKeys = start !== undefined ? keys.slice(start, end) : keys;
    return "{ " + sliceKeys.slice(0, 3)
      .map(k => k + ": " + JSON.stringify(val[k]))
      .join(", ") +
      (sliceKeys.length > 3 ? ", … }" : " }");
  }
  return "";
}

function copyValue(e: React.MouseEvent, node: FlatNode) {
  e.stopPropagation();
  let text: string;
  switch (node.type) {
    case "null": text = "null"; break;
    case "undefined": text = "undefined"; break;
    case "string": text = node.raw; break;
    case "bigint": text = String(node.raw); break;
    case "number":
    case "boolean": text = String(node.raw); break;
    default: text = JSON.stringify(node.raw, null, 2);
  }
  navigator.clipboard.writeText(text).catch(() => {});
}

// ─── subSize 向上冒泡（动态冒泡，更新所有祖先节点） ────────────────────

function bubble(flat: FlatNode[], idx: number, delta: number) {
  let base = flat[idx].depth;
  for (let i = idx - 1; i >= 0; i--) {
    if (flat[i].depth < base) {
      flat[i].subSize += delta;
      base = flat[i].depth;
    }
  }
}

// ─── Build root ───────────────────────────────────────────────────────

function buildRoot(src: any): FlatNode[] {
  const type = t(src);
  return [{
    key: "$", name: "", depth: 0, raw: src, type,
    expandable: type === "object" || type === "array",
    childCount: cc(src, type), expanded: false, subSize: 1,
  }];
}

// ─── Build children ───────────────────────────────────────────────────

function buildChildren(node: FlatNode): FlatNode[] {
  const { raw, depth, key, type } = node;
  const nd = depth + 1;

  if (type === "array") {
    const arr = raw as any[];
    if (arr.length > CHUNK_SIZE) {
      const cs: FlatNode[] = [];
      for (let i = 0; i < arr.length; i += CHUNK_SIZE) {
        const end = Math.min(i + CHUNK_SIZE, arr.length);
        cs.push({
          key: key + "|c|" + i,
          name: i + "–" + (end - 1),
          depth: nd, raw: arr, type: "array",
          expandable: true, childCount: end - i,
          expanded: false, isChunk: true,
          chunkStart: i, chunkEnd: end, subSize: 1,
        });
      }
      return cs;
    }
    const items: FlatNode[] = [];
    for (let i = 0; i < arr.length; i++) {
      const v = arr[i]; const vt = t(v);
      const exp = (vt === "object" || vt === "array") && v !== null;
      items.push({
        key: key + "[" + i + "]", name: String(i), depth: nd, raw: v, type: vt,
        expandable: exp, childCount: exp ? cc(v, vt) : 0, expanded: false, subSize: 1,
        preview: vt === "string" ? trunc(v) : undefined,
      });
    }
    return items;
  }

  if (type === "object") {
    const keys = Object.keys(raw as Record<string, any>);
    if (keys.length > CHUNK_SIZE) {
      const cs: FlatNode[] = [];
      for (let i = 0; i < keys.length; i += CHUNK_SIZE) {
        const end = Math.min(i + CHUNK_SIZE, keys.length);
        cs.push({
          key: key + "|c|" + i,
          name: i + "–" + (end - 1),
          depth: nd, raw,
          type: "object",
          expandable: true, childCount: end - i,
          expanded: false, isChunk: true,
          chunkStart: i, chunkEnd: end, subSize: 1,
        });
      }
      return cs;
    }
    const items: FlatNode[] = [];
    for (const [k, v] of Object.entries(raw as Record<string, any>)) {
      const vt = t(v);
      const exp = (vt === "object" || vt === "array") && v !== null;
      items.push({
        key: key + "|" + k, name: k, depth: nd, raw: v, type: vt,
        expandable: exp, childCount: exp ? cc(v, vt) : 0, expanded: false, subSize: 1,
        preview: vt === "string" ? trunc(v) : undefined,
      });
    }
    return items;
  }
  return [];
}

// ─── Expand / Collapse ────────────────────────────────────────────────

function expandNode(flat: FlatNode[], idx: number) {
  const node = flat[idx];
  if (!node.expandable || node.expanded) return;
  const children = buildChildren(node);
  if (!children.length) return;
  flat.splice(idx + 1, 0, ...children);
  node.subSize = children.length;
  node.expanded = true;
  bubble(flat, idx, children.length);
}

function collapseNode(flat: FlatNode[], idx: number) {
  const node = flat[idx];
  if (!node.expanded) return;
  const size = node.subSize ?? 0;
  if (size > 0) {
    flat.splice(idx + 1, size);
    bubble(flat, idx, -size);
  }
  node.subSize = 0;
  node.expanded = false;
}

function expandChunk(flat: FlatNode[], idx: number) {
  const node = flat[idx];
  if (!node.isChunk || node.expanded) return;
  const items: FlatNode[] = [];
  const nd = node.depth + 1;

  if (node.type === "object") {
    const keys = Object.keys(node.raw as Record<string, any>);
    for (let i = node.chunkStart!; i < node.chunkEnd! && i < keys.length; i++) {
      const k = keys[i];
      const v = node.raw[k];
      const vt = t(v);
      const exp = (vt === "object" || vt === "array") && v !== null;
      items.push({
        key: node.key + "|" + k, name: k, depth: nd, raw: v, type: vt,
        expandable: exp, childCount: exp ? cc(v, vt) : 0, expanded: false, subSize: 1,
        preview: vt === "string" ? trunc(v) : undefined,
      });
    }
  } else {
    const arr = node.raw as any[];
    for (let i = node.chunkStart!; i < node.chunkEnd! && i < arr.length; i++) {
      const v = arr[i]; const vt = t(v);
      const exp = (vt === "object" || vt === "array") && v !== null;
      items.push({
        key: node.key + "[" + i + "]", name: String(i), depth: nd, raw: v, type: vt,
        expandable: exp, childCount: exp ? cc(v, vt) : 0, expanded: false, subSize: 1,
        preview: vt === "string" ? trunc(v) : undefined,
      });
    }
  }

  if (!items.length) return;
  flat.splice(idx + 1, 0, ...items);
  const cnt = items.length;
  node.subSize = cnt;
  node.expanded = true;
  bubble(flat, idx, cnt);
}

// ─── Memoized leaf value ──────────────────────────────────────────────

const LeafVal = memo(function LeafVal({ node }: { node: FlatNode }) {
  switch (node.type) {
    case "string":
      return <span className="vs">{JSON.stringify(node.preview ?? trunc(node.raw))}</span>;
    case "number": return <span className="vn">{String(node.raw)}</span>;
    case "bigint": return <span className="vn">{String(node.raw)}n</span>;
    case "boolean": return <span className="vb">{String(node.raw)}</span>;
    case "null": return <span className="vx">null</span>;
    case "undefined": return <span className="vx">undefined</span>;
    default: return <span>{String(node.raw)}</span>;
  }
});

// ─── Memoized row body ────────────────────────────────────────────────

const RowBody = memo(function RowBody({ node, expanded }: { node: FlatNode; expanded: boolean }) {
  return (
    <>
      {node.expandable ? (
        <span className={"va" + (expanded ? " o" : "")} />
      ) : (
        <span className="v0" />
      )}
      {node.name !== "" && (
        <><span className="vk">{node.isChunk ? `[${node.name}]` : node.name}</span><span className="vc">:</span></>
      )}
      {node.expandable ? (
        <>
          {!node.isChunk && <span className="vbk">{node.type === "array" ? "[" : "{"}</span>}
          <span className="vm">
            {node.childCount === 0
              ? "empty"
              : node.isChunk
              ? (node.type === "array" ? "Array(" + node.childCount + ")" : "Object(" + node.childCount + ")")
              : node.type === "array" ? node.childCount + " items" : node.childCount + " keys"}
          </span>
          <span className="copy-btn" onClick={e => copyValue(e, node)} title="Copy value">⧉</span>
          {expanded ? (
            <span className="vbk" style={{ marginLeft: 4 }}>{node.type === "array" ? "]" : "}"}</span>
          ) : (
            <span className="vp">{previewValue(node.raw, node.type, node.chunkStart, node.chunkEnd)}</span>
          )}
        </>
      ) : (
        <>
          <LeafVal node={node} />
          <span className="copy-btn" onClick={e => copyValue(e, node)} title="Copy value">⧉</span>
        </>
      )}
    </>
  );
});

// ─── Main Component ───────────────────────────────────────────────────

export function VirtualJsonTree({ src, rowHeight = 20 }: Props) {
  const ref = useRef<HTMLDivElement>(null);
  const [vh, setVh] = useState(300);
  const [, force] = useState(0);

  const stRef = useRef(0);
  const st = stRef.current;

  const flatRef = useRef<FlatNode[] | null>(null);
  const srcRef = useRef<any>(null);

  // rebuild when src changes
  if (src !== srcRef.current) {
    srcRef.current = src;
    const f = buildRoot(src);
    flatRef.current = f;
    if (f.length && f[0].expandable) expandNode(f, 0);
    stRef.current = 0;
  }
  const flat = flatRef.current!;

  // visible window — for loop, zero GC
  const over = 5;
  const totalH = flat.length * rowHeight;
  const maxST = Math.max(0, totalH - vh);
  const clampedST = Math.min(st, maxST);
  const si = Math.max(0, Math.floor(clampedST / rowHeight) - over);
  const ei = Math.min(flat.length, Math.ceil((clampedST + vh) / rowHeight) + over);

  // toggle
  const toggleNode = useCallback((idx: number) => {
    const f = flatRef.current;
    if (!f) return;
    const node = f[idx];
    if (!node || !node.expandable) return;
    if (node.isChunk) {
      if (node.expanded) collapseNode(f, idx);
      else expandChunk(f, idx);
    } else if (node.expanded) collapseNode(f, idx);
    else expandNode(f, idx);
    force((x) => x + 1);
  }, []);

  // scroll: RAF throttle, keep in ref
  const tickRef = useRef(false);
  const onScroll = useCallback((e: React.UIEvent<HTMLDivElement>) => {
    const t = e.currentTarget;
    if (tickRef.current) return;
    tickRef.current = true;
    requestAnimationFrame(() => {
      stRef.current = t.scrollTop;
      tickRef.current = false;
      force((x) => x + 1);
    });
  }, []);

  // ResizeObserver
  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    let rafId = 0;
    const ro = new ResizeObserver(([e]) => {
      cancelAnimationFrame(rafId);
      rafId = requestAnimationFrame(() => setVh(e.contentRect.height));
    });
    ro.observe(el);
    return () => { ro.disconnect(); cancelAnimationFrame(rafId); };
  }, []);

  // render visible rows with for loop (zero intermediate array)
  const rows: React.ReactNode[] = [];
  for (let i = si; i < ei; i++) {
    const node = flat[i];
    rows.push(
      <div
        key={node.key}
        data-i={i}
        className="vr"
        onClick={() => toggleNode(i)}
        style={{
          position: "absolute",
          top: 0, left: 0, right: 0,
          transform: "translateY(" + (i * rowHeight) + "px)",
          height: rowHeight,
          paddingLeft: node.depth * 16 + 4,
        }}
      >
        <RowBody node={node} expanded={node.expanded} />
      </div>
    );
  }

  return (
    <Tree ref={ref} onScroll={onScroll}>
      <div style={{ height: totalH, position: "relative" }}>
        {rows}
      </div>
    </Tree>
  );
}
