import { safeJSONStringify } from "@barda/index.sdk";
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
    columnSetting: boolean;
  }
) {
  let resultData = data;
  
  let visibleColumns: string[] | null = null;
  
  if (columnInfo) {
    const { dataIndexes, hides, tempHides, columnSetting } = columnInfo;
    const visibleSet = new Set<string>();
    
    dataIndexes.forEach((dataIndex, idx) => {
      const isHidden = columnHide({
        hide: hides[idx]?.value || false,
        tempHide: tempHides[idx] || false,
        enableColumnSetting: columnSetting,
      });
      
      if (!isHidden) {
        visibleSet.add(dataIndex);
      }
    });
    
    visibleColumns = Array.from(visibleSet);
  }
  
  if (searchValue) {
    resultData = resultData.filter((row) => {
      let searchLower = searchValue?.toLowerCase();
      if (!searchLower) {
        return true;
      } else {
        const searchableValues = visibleColumns 
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

export function getColumnsAggr(
  oriDisplayData: JSONObject[],
  dataIndexWithParamsDict: NodeToValue<
    ReturnType<InstanceType<typeof ColumnListComp>["withParamsNode"]>
  >,
  changeSet?: Record<string, Record<string, JSONValue>>
): ColumnsAggrData {
  return _.mapValues(dataIndexWithParamsDict, (withParams, dataIndex) => {
    const compType = (withParams.wrap() as any).compType;
    const res: Record<string, JSONValue> & { compType: string } = { compType };
    if (compType === "tag" || compType === "tags") {
      // 收集原始数据中的标签
      const originalTags = _(oriDisplayData)
        .map((row) => row[dataIndex]!)
        .filter((tag) => !!tag)
        .flatMap((tag) => {
          if (_.isArray(tag)) {
            return tag.map((item: any) => {
              // 如果是对象数组，提取text字段
              if (typeof item === 'object' && item !== null && 'text' in item) {
                return String(item.text);
              }
              return String(item);
            });
          }
          return [String(tag)];
        })
        .value();
      
      // 收集changeSet中的临时标签值
      const changeSetTags: string[] = [];
      if (changeSet) {
        _.forEach(changeSet, (rowChanges) => {
          const changedValue = rowChanges[dataIndex];
          if (changedValue !== undefined) {
            if (_.isArray(changedValue)) {
              changedValue.forEach((item: any) => {
                if (typeof item === 'object' && item !== null && 'text' in item) {
                  changeSetTags.push(String((item as {text: any}).text));
                } else {
                  changeSetTags.push(String(item));
                }
              });
            } else if (changedValue) {
              changeSetTags.push(String(changedValue));
            }
          }
        });
      }
      
      // 合并并去重
      res.uniqueTags = _.uniq([...originalTags, ...changeSetTags]);
    } else if (compType === "badgeStatus") {
      res.uniqueStatus = _(oriDisplayData)
        .map((row) => {
          const value = row[dataIndex] as any;
          if (value.split(" ")[1]) {
            return {
              status: value.slice(0, value.indexOf(" ")),
              text: value.slice(value.indexOf(" ") + 1),
            };
          } else {
            return {
              status: value,
              text: "",
            };
          }
        })
        .uniqBy("text")
        .value();
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
          .view({ editable: column.editable, size, candidateTags: tags, candidateStatus: status });
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
