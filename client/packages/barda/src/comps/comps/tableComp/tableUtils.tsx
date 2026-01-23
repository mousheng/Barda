import { safeJSONStringify } from "util/objectUtils";
import {
  ColumnType,
  FilterValue,
  SorterResult,
  TableCurrentDataSource,
  TablePaginationConfig,
} from "antd/es/table/interface";
import { SortOrder } from "antd/lib/table/interface";
import { changeChildAction, CompAction, NodeToValue } from "barda-core";
import { EditableIcon } from "barda-design";
import { __COLUMN_DISPLAY_VALUE_FN } from "comps/comps/tableComp/column/columnTypeCompBuilder";
import { RawColumnType, Render } from "comps/comps/tableComp/column/tableColumnComp";
import { TableFilter, tableFilterOperatorMap } from "comps/comps/tableComp/tableToolbarComp";
import { SortValue, TableOnEventView } from "comps/comps/tableComp/tableTypes";
import _ from "lodash";
import { tryToNumber } from "util/convertUtils";
import { JSONObject, JSONValue } from "util/jsonTypes";
import { StatusType } from "./column/columnTypeComps/columnStatusComp";
import { ColumnListComp, tableDataRowExample } from "./column/tableColumnListComp";

export const COLUMN_CHILDREN_KEY = "children";
export const OB_ROW_ORI_INDEX = "__ob_origin_index";
export const OB_ROW_RECORD = "__ob_origin_record";

export const COL_MIN_WIDTH = 55;
export const COL_MAX_WIDTH = 500;

/**
 * Add __originIndex__, mainly for the logic of the default key
 */
export type RecordType = JSONObject & { [OB_ROW_ORI_INDEX]: string };

export function filterData(
  data: Array<RecordType>,
  searchValue: string,
  filter: TableFilter,
  showFilter: boolean,
  columnInfo?: {
    dataIndexes: Array<string>;
    hides: Array<{ value: boolean }>;
    tempHides: Array<boolean>;
    allowSearchWhenHidden?: Array<{ value: boolean }>;
    columnSetting: boolean;
  }
) {
  let resultData = data;
  
  let visibleColumns: string[] | null = null;
  let searchableColumns: string[] | null = null;
  
  if (columnInfo) {
    const { dataIndexes, hides, tempHides, allowSearchWhenHidden, columnSetting } = columnInfo;
    const visibleSet = new Set<string>();
    const searchableSet = new Set<string>();
    
    dataIndexes.forEach((dataIndex, idx) => {
      const isHidden = columnHide({
        hide: hides[idx]?.value || false,
        tempHide: tempHides[idx] || false,
        enableColumnSetting: columnSetting,
      });
      
      if (!isHidden) {
        visibleSet.add(dataIndex);
        searchableSet.add(dataIndex);
      } else {
        // 如果列隐藏了，但允许搜索，则添加到可搜索列集合中
        if (allowSearchWhenHidden && allowSearchWhenHidden[idx]?.value) {
          searchableSet.add(dataIndex);
        }
      }
    });
    
    visibleColumns = Array.from(visibleSet);
    searchableColumns = Array.from(searchableSet);
  }
  
  if (searchValue) {
    resultData = resultData.filter((row) => {
      let searchLower = searchValue?.toLowerCase();
      if (!searchLower) {
        return true;
      } else {
        // 使用可搜索列（包括隐藏但允许搜索的列）进行搜索
        const searchableValues = searchableColumns 
          ? searchableColumns.map(col => row[col]).filter(v => v !== undefined)
          : visibleColumns
          ? visibleColumns.map(col => row[col]).filter(v => v !== undefined)
          : Object.values(row);
        return searchableValues.find((v) => v?.toString().toLowerCase().includes(searchLower));
      }
    });
  }
  
  if (showFilter && filter.filters.length > 0) {
    const visibleFilters = visibleColumns 
      ? filter.filters.filter(f => visibleColumns!.includes(f.columnKey))
      : filter.filters;
    
    if (visibleFilters.length === 0) {
      return resultData;
    }
    
    resultData = resultData.filter((row) => {
      for (let f of visibleFilters) {
        const columnValue = row[f.columnKey];
        const result = tableFilterOperatorMap[f.operator].filter(f.filterValue, columnValue);
        if (filter.stackType === "or" && result) {
          // one condition is met
          return true;
        } else if (filter.stackType === "and" && !result) {
          // one condition is not met
          return false;
        }
      }
      if (visibleFilters.length === 0) {
        return true;
      } else if (filter.stackType === "and") {
        return true;
      } else if (filter.stackType === "or") {
        return false;
      }
      return true;
    });
  }
  return resultData;
}

export function sortData(
  data: Array<JSONObject>,
  columns: Record<string, { sortable: boolean }>, // key: dataIndex
  sorter: Array<SortValue>
): Array<RecordType> {
  let resultData: Array<RecordType> = data.map((row, index) => ({
    ...row,
    [OB_ROW_ORI_INDEX]: index + "",
  }));
  if (sorter.length > 0) {
    const [sortColumns, sortMethods] = _(sorter)
      .filter((s) => {
        return !!s.column && columns[s.column]?.sortable;
      })
      .map((s) => [s.column, s.desc ? "desc" : "asc"] as const)
      .unzip()
      .value() as [string[], ("desc" | "asc")[]];
    resultData = _.orderBy(
      resultData,
      sortColumns.map((colName) => {
        return (obj) => {
          const val = obj[colName];
          if (typeof val === "string") {
            return val.toLowerCase();
          } else {
            return val;
          }
        };
      }),
      sortMethods
    );
  }
  return resultData;
}

export function columnHide({
  hide,
  tempHide,
  enableColumnSetting,
}: {
  hide: boolean;
  tempHide: boolean;
  enableColumnSetting: boolean;
}) {
  if (enableColumnSetting) {
    return tempHide || hide;
  } else {
    return hide;
  }
}

export function buildOriginIndex(index: string, childIndex: string) {
  return index + "-" + childIndex;
}

export function tranToTableRecord(dataObj: JSONObject, index: string | number): RecordType {
  const indexString = index + "";
  if (Array.isArray(dataObj[COLUMN_CHILDREN_KEY])) {
    return {
      ...dataObj,
      [OB_ROW_ORI_INDEX]: indexString,
      children: dataObj[COLUMN_CHILDREN_KEY].map((child: any, i: number) =>
        tranToTableRecord(child, buildOriginIndex(indexString, i + ""))
      ),
    };
  }
  return {
    ...dataObj,
    [OB_ROW_ORI_INDEX]: indexString,
  };
}

/**
 * 获取当前页的原始显示数据
 * @param data 过滤后的数据
 * @param pageSize 每页大小
 * @param pageNo 当前页码
 * @param columns 列配置
 * @returns 当前页的显示数据
 */
export function getCurrentPageOriDisplayData(
  data: Array<RecordType>,
  pageSize: number,
  pageNo: number,
  columns: Array<{ dataIndex: string; render: NodeToValue<ReturnType<Render["node"]>> }>
) {
  const startIndex = (pageNo - 1) * pageSize;
  const endIndex = startIndex + pageSize;
  const currentPageData = data.slice(startIndex, endIndex);
  return currentPageData.map((row, idx) => {
    const displayData: RecordType = { [OB_ROW_ORI_INDEX]: row[OB_ROW_ORI_INDEX] };
    columns.forEach((col) => {
      // if (!row.hasOwnProperty(col.dataIndex)) return;
      const node = col.render.wrap({
        currentCell: row[col.dataIndex],
        currentRow: _.omit(row, OB_ROW_ORI_INDEX),
        currentIndex: idx,
        currentOriginalIndex: row[OB_ROW_ORI_INDEX],
      }) as any;
      if (Array.isArray(row[COLUMN_CHILDREN_KEY])) {
        displayData[COLUMN_CHILDREN_KEY] = getCurrentPageOriDisplayData(
          row[COLUMN_CHILDREN_KEY] as Array<RecordType>,
          pageSize,
          pageNo,
          columns
        );
      }
      const colValue = node.comp[__COLUMN_DISPLAY_VALUE_FN](node.comp);
      if (colValue !== null) {
        displayData[col.dataIndex] = colValue;
      }
    });
    return displayData;
  });
}

export function transformDispalyData(
  oriDisplayData: JSONObject[],
  dataIndexTitleDict: _.Dictionary<string>
): JSONObject[] {
  return oriDisplayData.map((row) => {
    const transData = _(row)
      .omit(OB_ROW_ORI_INDEX)
      .mapKeys((value, key) => dataIndexTitleDict[key] || key)
      .value();
    if (Array.isArray(row[COLUMN_CHILDREN_KEY])) {
      return {
        ...transData,
        [COLUMN_CHILDREN_KEY]: transformDispalyData(
          row[COLUMN_CHILDREN_KEY] as JSONObject[],
          dataIndexTitleDict
        ),
      };
    }
    return transData;
  });
}

export type ColumnsAggrData = Record<string, Record<string, JSONValue> & { compType: string }>;

function extractTags(input: unknown): string[] {
  if (Array.isArray(input)) {
    return input.flatMap((item) => extractTags(item));
  }

  if (typeof input === "object" && input !== null) {
    if ("text" in input) {
      return extractTags((input as any).text);
    }
    return [];
  }

  if (["string", "number", "boolean"].includes(typeof input)) {
    return [String(input)];
  }

  return [];
}


export function getColumnsAggr(
  oriDisplayData: JSONObject[],
  dataIndexWithParamsDict: NodeToValue<
    ReturnType<InstanceType<typeof ColumnListComp>["withParamsNode"]>
  >,
  changeSet?: Record<string, Record<string, JSONValue>>
): ColumnsAggrData {
  return _.mapValues(dataIndexWithParamsDict, (withParams, dataIndex) => {
    const { compType } = withParams.wrap() as any;
    const res: Record<string, JSONValue> & { compType: string } = { compType };

    switch (compType) {
      case "tag":
      case "tags": {
        const originalTags = _(oriDisplayData)
          .map((row) => extractTags(row[dataIndex]))
          .flatten()
          .filter((t) => Boolean(t))
          .value();

        const changeSetTags = changeSet
          ? _(changeSet)
              .flatMap((rowChanges) => extractTags(rowChanges[dataIndex]))
              .filter((t) => Boolean(t))
              .value()
          : [];

        res.uniqueTags = _.uniq([...originalTags, ...changeSetTags]);
        break;
      }

      case "badgeStatus": {
        res.uniqueStatus = _(oriDisplayData)
          .map((row) => {
            const value = row[dataIndex] as string;
            const spaceIndex = value.indexOf(" ");
            return spaceIndex !== -1
              ? { status: value.slice(0, spaceIndex), text: value.slice(spaceIndex + 1) }
              : { status: value, text: "" };
          })
          .uniqBy("text")
          .value();
        break;
      }
    }

    return res;
  });
}

function renderTitle(props: { title: string; editable: boolean }) {
  const { title, editable } = props;
  return (
    <div>
      {title}
      {editable && <EditableIcon style={{ verticalAlign: "baseline", marginLeft: "4px" }} />}
    </div>
  );
}

export type CustomColumnType<RecordType> = ColumnType<RecordType> & {
  onWidthResize?: (width: number) => void;
  titleText: string;
  columnType?: string;
};

/**
 * convert column in raw format into antd format
 */
export function columnsToAntdFormat(
  columns: Array<RawColumnType>,
  sort: SortValue[],
  enableColumnSetting: boolean,
  size: string,
  dynamicColumn: boolean,
  dynamicColumnConfig: Array<string>,
  columnsAggrData: ColumnsAggrData
): Array<CustomColumnType<RecordType>> {
  const sortMap: Map<string | undefined, SortOrder> = new Map(
    sort.map((s) => [s.column, s.desc ? "descend" : "ascend"])
  );
  const sortedColumns = _.sortBy(columns, (c) => {
    if (c.fixed === "left") {
      return -1;
    } else if (c.fixed === "right") {
      return Number.MAX_SAFE_INTEGER;
    } else if (dynamicColumnConfig.length > 0) {
      // sort by dynamic config array
      const index = dynamicColumnConfig.indexOf(c.isCustom ? c.title : c.dataIndex);
      if (index >= 0) {
        return index;
      }
    }
    return 0;
  });
  return sortedColumns.flatMap((column) => {
    if (
      columnHide({
        hide: column.hide,
        tempHide: column.tempHide,
        enableColumnSetting: enableColumnSetting,
      })
    ) {
      return [];
    }
    if (
      dynamicColumn &&
      dynamicColumnConfig.length > 0 &&
      !dynamicColumnConfig.includes(column.isCustom ? column.title : column.dataIndex)
    ) {
      return [];
    }
    const tags = ((columnsAggrData[column.dataIndex] ?? {}).uniqueTags ?? []) as string[];
    const status = ((columnsAggrData[column.dataIndex] ?? {}).uniqueStatus ?? []) as {
      text: string;
      status: StatusType;
    }[];
    const title = renderTitle({ title: column.title, editable: column.editable });
    return {
      title: title,
      titleText: column.title,
      dataIndex: column.dataIndex,
      align: column.align,
      width: column.autoWidth === "auto" ? 0 : column.width,
      fixed: column.fixed === "close" ? false : column.fixed,
      onWidthResize: column.onWidthResize,
      columnType: (column as any).columnType,
      render: (value: any, record: RecordType, index: number) => {
        return column
          .render(
            {
              currentCell: value,
              currentRow: _.omit(record, OB_ROW_ORI_INDEX),
              currentIndex: index,
              currentOriginalIndex: tryToNumber(record[OB_ROW_ORI_INDEX]),
            },
            String(record[OB_ROW_ORI_INDEX])
          )
          .getView()
          .view({
            editable: column.editable,
            size,
            candidateTags: tags,
            candidateStatus: status,
            columnType: (column as any).columnType,
          });
      },
      ...(column.sortable
        ? {
          sorter: true,
          sortOrder: sortMap.get(column.dataIndex),
        }
        : {}),
    };
  });
}

function getSortValue(sortResult: SorterResult<RecordType>) {
  return sortResult.column?.dataIndex
    ? {
      column: sortResult.column.dataIndex.toString(),
      desc: sortResult.order === "descend",
    }
    : null;
}

export function onTableChange(
  pagination: TablePaginationConfig,
  filters: Record<string, FilterValue | null>,
  sorter: SorterResult<RecordType> | SorterResult<RecordType>[],
  extra: TableCurrentDataSource<RecordType>,
  dispatch: (action: CompAction<JSONValue>) => void,
  onEvent: TableOnEventView
) {
  if (extra.action === "sort") {
    let sortValues: SortValue[] = [];
    if (Array.isArray(sorter)) {
      // multi-column sort
      sorter.forEach((s) => {
        const v = getSortValue(s);
        v && sortValues.push(v);
      });
    } else {
      const v = getSortValue(sorter);
      v && sortValues.push(v);
    }
    dispatch(changeChildAction("sort", sortValues, true));
    onEvent("sortChange");
  }
}

export function calcColumnWidth(columnKey: string, data: Array<Record<string, unknown>>): number {
  const computeWidth = (val: unknown): number => {
    const s = val == null ? "" : safeJSONStringify(val);
    const perChar = /[^\x00-\x7F]/.test(s) ? 20 : 10;
    return s.length * perChar;
  };

  const titleWidth = computeWidth(columnKey);

  let maxCellWidth = 0;
  for (let i = 0; i < data.length; i++) {
    const cellValue = data[i]?.[columnKey];
    const width = cellValue == null ? COL_MIN_WIDTH : computeWidth(cellValue);
    if (width > maxCellWidth) {
      maxCellWidth = width;
      if (maxCellWidth >= COL_MAX_WIDTH) break;
    }
  }

  const raw = Math.max(titleWidth, maxCellWidth) + 10;
  return Math.max(Math.min(COL_MAX_WIDTH, raw), COL_MIN_WIDTH);
}

export function genSelectionParams(
  filterData: RecordType[],
  selection: string
): Record<string, unknown> | undefined {
  const idx = filterData.findIndex((row) => row[OB_ROW_ORI_INDEX] === selection);
  if (idx < 0) {
    return undefined;
  }
  const currentRow = filterData[idx];
  return {
    currentRow: _.omit(currentRow, OB_ROW_ORI_INDEX),
    currentIndex: idx,
    currentOriginalIndex: tryToNumber(currentRow[OB_ROW_ORI_INDEX]),
  };
}

export function supportChildrenTree(data: Array<JSONObject>) {
  const rowSample = tableDataRowExample(data) as any;
  return rowSample && Array.isArray(rowSample[COLUMN_CHILDREN_KEY]);
}
