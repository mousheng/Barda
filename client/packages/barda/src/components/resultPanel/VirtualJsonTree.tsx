import { useCallback, useRef, useState, useEffect, useLayoutEffect, memo } from "react";
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
  /** 折叠预览 / 字符串截断缓存（build 时预计算，避免 render 中重复计算） */
  preview?: string;
}

interface Props {
  src: any;
  rowHeight?: number;
  /** true=全折叠 false=全展开 number=展开到指定深度（0=仅根） */
  collapsed?: boolean | number;
  enableClipboard?: boolean;
  collapseStringsAfterLength?: number;
}

// ─── CSS ──────────────────────────────────────────────────────────────

const Tree = styled.div`
  overflow-y: auto; overflow-x: hidden; height: 100%;
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
  .vs.trunc { cursor: pointer; }
  .vs.trunc:hover { text-decoration: underline; }
  .vn { color: #b5cea8; }    /* number */
  .vb { color: #569cd6; }    /* boolean */
  .vx { color: #808080; font-style: italic; }  /* null / undefined */
  .vm { color: #9ca3af; margin-left: 6px; font-size: 12px; }
  .vp { color: #9ca3af; margin-left: 4px; }
  .vbk { color: #d4d4d4; }   /* bracket */
  .copy-btn {
    visibility: hidden; display: inline-flex; align-items: center;
    padding: 0 4px; cursor: pointer;
    color: #888; font-size: 11px; line-height: 1; flex-shrink: 0; user-select: none;
  }
  .vr:hover .copy-btn { visibility: visible; }
  .copy-btn:hover { color: #ccc; }
  .copy-btn.copied { color: #4ade80; visibility: visible; }
`;

// ─── Helpers ──────────────────────────────────────────────────────────

const CHUNK_SIZE = 100;
const keysCache = new WeakMap<object, string[]>();

function getKeys(obj: object): string[] {
  let keys = keysCache.get(obj);
  if (!keys) {
    keys = Object.keys(obj);
    keysCache.set(obj, keys);
  }
  return keys;
}

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
  if (type === "object") return getKeys(val as object).length;
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
    const keys = getKeys(val);
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
  const expandable = type === "object" || type === "array";
  return [{
    key: "$", name: "", depth: 0, raw: src, type,
    expandable,
    childCount: cc(src, type), expanded: false, subSize: 1,
    preview: expandable ? previewValue(src, type) : undefined,
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
        preview: exp ? previewValue(v, vt) : vt === "string" ? trunc(v) : undefined,
      });
    }
    return items;
  }

  if (type === "object") {
    const keys = getKeys(raw as Record<string, any>);
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
    for (const k of getKeys(raw as Record<string, any>)) {
      const v = (raw as Record<string, any>)[k];
      const vt = t(v);
      const exp = (vt === "object" || vt === "array") && v !== null;
      items.push({
        key: key + "|" + k, name: k, depth: nd, raw: v, type: vt,
        expandable: exp, childCount: exp ? cc(v, vt) : 0, expanded: false, subSize: 1,
        preview: exp ? previewValue(v, vt) : vt === "string" ? trunc(v) : undefined,
      });
    }
    return items;
  }
  return [];
}

// ─── Expand / Collapse ────────────────────────────────────────────────

/** 自动展开的节点数上限，超出后由用户手动点击展开 */
const MAX_AUTO_EXPAND = 3000;

function canExpand(flat: FlatNode[], node: FlatNode): boolean {
  return flat.length + (node.childCount || 0) <= MAX_AUTO_EXPAND;
}

/** 按 depth 展开 flat 列表中的可展开节点，到达上限后停止以保持按需遍历 */
function expandToDepth(flat: FlatNode[], maxDepth: number) {
  let i = 0;
  while (i < flat.length && flat.length < MAX_AUTO_EXPAND) {
    const node = flat[i];
    if (node.expandable && !node.expanded && node.depth < maxDepth && canExpand(flat, node)) {
      if (node.isChunk) expandChunk(flat, i);
      else expandNode(flat, i);
    }
    i++;
  }
}

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
    const keys = getKeys(node.raw as Record<string, any>);
    for (let i = node.chunkStart!; i < node.chunkEnd! && i < keys.length; i++) {
      const k = keys[i];
      const v = node.raw[k];
      const vt = t(v);
      const exp = (vt === "object" || vt === "array") && v !== null;
      items.push({
        key: node.key + "|" + k, name: k, depth: nd, raw: v, type: vt,
        expandable: exp, childCount: exp ? cc(v, vt) : 0, expanded: false, subSize: 1,
        preview: exp ? previewValue(v, vt) : vt === "string" ? trunc(v) : undefined,
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
        preview: exp ? previewValue(v, vt) : vt === "string" ? trunc(v) : undefined,
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

const LeafVal = memo(function LeafVal({ node, isExpanded, onToggleString, collapseLength }: {
  node: FlatNode;
  isExpanded: boolean;
  onToggleString: (e: React.MouseEvent) => void;
  collapseLength: number;
}) {
  switch (node.type) {
    case "string": {
      const raw = node.raw as string;
      if (collapseLength > 0 && raw.length > collapseLength) {
        const display = isExpanded ? raw : raw.slice(0, collapseLength);
        return (
          <span className={"vs trunc"} onClick={onToggleString} title={isExpanded ? "Collapse" : "Expand"}>
            {JSON.stringify(display)}{!isExpanded && "…"}
          </span>
        );
      }
      return <span className="vs">{JSON.stringify(node.preview ?? trunc(node.raw))}</span>;
    }
    case "number": return <span className="vn">{String(node.raw)}</span>;
    case "bigint": return <span className="vn">{String(node.raw)}n</span>;
    case "boolean": return <span className="vb">{String(node.raw)}</span>;
    case "null": return <span className="vx">null</span>;
    case "undefined": return <span className="vx">undefined</span>;
    default: return <span>{String(node.raw)}</span>;
  }
});

// ─── Memoized row body ────────────────────────────────────────────────

const RowBody = memo(function RowBody({ node, expanded, isCopied, onCopy, enableClipboard, isStringExpanded, nodeKey, onToggleStringKey, collapseLength }: {
  node: FlatNode;
  expanded: boolean;
  isCopied: boolean;
  onCopy: (e: React.MouseEvent, node: FlatNode) => void;
  enableClipboard: boolean;
  isStringExpanded: boolean;
  nodeKey: string;
  onToggleStringKey: (e: React.MouseEvent, key: string) => void;
  collapseLength: number;
}) {
  const handleToggleString = useCallback((e: React.MouseEvent) => {
    onToggleStringKey(e, nodeKey);
  }, [onToggleStringKey, nodeKey]);

  const handleCopy = useCallback((e: React.MouseEvent) => {
    onCopy(e, node);
  }, [onCopy, node]);

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
          {enableClipboard && (
            <span className={"copy-btn" + (isCopied ? " copied" : "")} onClick={handleCopy} title="Copy value">{isCopied ? "✓" : "⧉"}</span>
          )}
          {expanded ? (
            <span className="vbk" style={{ marginLeft: 4 }}>{node.type === "array" ? "]" : "}"}</span>
          ) : (
            <span className="vp">{node.preview}</span>
          )}
        </>
      ) : (
        <>
          <LeafVal node={node} isExpanded={isStringExpanded} onToggleString={handleToggleString} collapseLength={collapseLength} />
          {enableClipboard && (
            <span className={"copy-btn" + (isCopied ? " copied" : "")} onClick={handleCopy} title="Copy value">{isCopied ? "✓" : "⧉"}</span>
          )}
        </>
      )}
    </>
  );
});

// ─── Main Component ───────────────────────────────────────────────────

export function VirtualJsonTree({ src, rowHeight = 20, collapsed = false, enableClipboard = true, collapseStringsAfterLength = 0 }: Props) {
  const maxExpandDepth = collapsed === true ? 0 : collapsed === false ? Infinity : collapsed as number;
  const ref = useRef<HTMLDivElement>(null);
  const [vh, setVh] = useState(300);
  const [contentWidth, setContentWidth] = useState(0);
  const [, force] = useState(0);
  const [copiedKey, setCopiedKey] = useState<string | null>(null);
  const copyTimeoutRef = useRef<ReturnType<typeof setTimeout>>();
  const [expandedStrings, setExpandedStrings] = useState<Set<string>>(new Set());

  const stRef = useRef(0);
  const st = stRef.current;

  const handleCopy = useCallback((e: React.MouseEvent, node: FlatNode) => {
    copyValue(e, node);
    setCopiedKey(node.key);
    clearTimeout(copyTimeoutRef.current);
    copyTimeoutRef.current = setTimeout(() => setCopiedKey(null), 2000);
  }, []);

  const handleToggleString = useCallback((e: React.MouseEvent, key: string) => {
    e.stopPropagation();
    setExpandedStrings(prev => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  }, []);

  const flatRef = useRef<FlatNode[] | null>(null);
  const srcRef = useRef<any>(null);

  // rebuild when src changes
  if (src !== srcRef.current) {
    srcRef.current = src;
    const f = buildRoot(src);
    flatRef.current = f;
    expandToDepth(f, maxExpandDepth);
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

  // let Tree expand beyond parent so outer container shows h-scroll
  useLayoutEffect(() => {
    if (ref.current) setContentWidth(ref.current.scrollWidth);
  }, [flat.length]);

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
        <RowBody
          node={node}
          expanded={node.expanded}
          isCopied={copiedKey === node.key}
          onCopy={handleCopy}
          enableClipboard={enableClipboard}
          isStringExpanded={expandedStrings.has(node.key)}
          nodeKey={node.key}
          onToggleStringKey={handleToggleString}
          collapseLength={collapseStringsAfterLength}
        />
      </div>
    );
  }

  return (
    <Tree ref={ref} onScroll={onScroll} style={{ minWidth: contentWidth || undefined }}>
      <div style={{ height: totalH, position: "relative" }}>
        {rows}
      </div>
    </Tree>
  );
}
