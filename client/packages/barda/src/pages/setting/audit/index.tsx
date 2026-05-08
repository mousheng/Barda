import React, { useEffect, useState, useCallback, useRef } from "react";
import { DatePicker, Select, Button, Space, Card } from "antd";
import { Table } from "components/Table";
import AuditLogApi, { AuditLog, AuditLogSearchRequest, AuditLogPageResponse, EventTypeItem } from "api/auditLogApi";
import ApplicationApi from "api/applicationApi";
import OrgApi from "api/orgApi";
import { validateResponse } from "api/apiUtils";
import { messageInstance } from "components/GlobalInstances";
import { trans } from "i18n";
import { Level1SettingPageContentWithList, Level1SettingPageTitleWithBtn } from "../styled";
import { timestampToHumanReadable } from "util/dateTimeUtils";
import { useSelector } from "react-redux";
import { getUser } from "redux/selectors/usersSelectors";
import { Dayjs } from "dayjs";
import styled from "styled-components";
import { VirtualJsonTree } from "components/resultPanel/VirtualJsonTree";

const { RangePicker } = DatePicker;
const { Option } = Select;

interface UserOption {
  id: string;
  name: string;
}

interface AppOption {
  applicationId: string;
  name: string;
}

const TableDetailContainer = styled.div`
  display: flex;
  gap: 16px;
  align-items: flex-start;
`;

const TableWrapper = styled.div`
  flex: 1;
  min-width: 0;

  .ant-table-cell {
    white-space: nowrap !important;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .ant-table-tbody > tr > td {
    white-space: nowrap !important;
  }
`;

const DetailPanel = styled(Card)`
  width: 40%;
  max-width: 450px;
  flex-shrink: 0;
  height: 440px;
  overflow: hidden;

  .ant-card-body {
    padding: 16px;
    max-height: 100%;
    overflow-y: auto;
  }
`;

const JsonViewWrapper = styled.div`
  height: 100%;
  border-radius: 4px;
`;

export default function AuditSetting() {
  const user = useSelector(getUser);
  const [loading, setLoading] = useState(false);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [eventTypes, setEventTypes] = useState<EventTypeItem[]>([]);
  const [selectedRow, setSelectedRow] = useState<AuditLog | null>(null);

  // 用户和应用列表
  const [users, setUsers] = useState<UserOption[]>([]);
  const [applications, setApplications] = useState<AppOption[]>([]);

  // 查询条件
  const [dateRange, setDateRange] = useState<[Dayjs | null, Dayjs | null] | null>(null);
  const [selectedEventType, setSelectedEventType] = useState<string | undefined>(undefined);
  const [selectedUserId, setSelectedUserId] = useState<string | undefined>(undefined);
  const [selectedAppId, setSelectedAppId] = useState<string | undefined>(undefined);

  // 加载事件类型列表
  useEffect(() => {
    AuditLogApi.getEventTypes()
      .then((resp) => {
        if (validateResponse(resp)) {
          setEventTypes(resp.data.data || []);
        }
      })
      .catch((e) => {
        messageInstance.error(e.message);
      });
  }, []);

  // 加载用户列表
  useEffect(() => {
    const orgId = user?.currentOrgId;
    if (orgId) {
      OrgApi.fetchOrgUsers(orgId)
        .then((resp) => {
          if (validateResponse(resp)) {
            const userList = resp.data.data.members.map((member) => ({
              id: member.userId,
              name: member.name,
            }));
            setUsers(userList);
          }
        })
        .catch((e) => {
          messageInstance.error(e.message);
        });
    }
  }, [user]);

  // 加载应用列表
  useEffect(() => {
    ApplicationApi.fetchAllApplications({})
      .then((resp) => {
        if (validateResponse(resp)) {
          const appList = resp.data.data.map((app: any) => ({
            applicationId: app.applicationId,
            name: app.name,
          }));
          setApplications(appList);
        }
      })
      .catch((e) => {
        messageInstance.error(e.message);
      });
  }, []);

  // 加载审计日志
  const loadAuditLogs = useCallback(() => {
    setLoading(true);
    const request: AuditLogSearchRequest = {
      pageNum: pageNum,
      pageSize: 7, // 固定为7行
    };

    if (dateRange && dateRange[0] && dateRange[1]) {
      request.startTime = dateRange[0].format("YYYY-MM-DD HH:mm:ss");
      request.endTime = dateRange[1].format("YYYY-MM-DD HH:mm:ss");
    }

    if (selectedEventType) {
      request.eventType = selectedEventType;
    }

    if (selectedUserId) {
      request.userId = selectedUserId;
    }

    if (selectedAppId) {
      request.appId = selectedAppId;
    }

    AuditLogApi.search(request)
      .then((resp) => {
        if (validateResponse(resp)) {
          const responseData = resp.data as unknown as AuditLogPageResponse;
          if (responseData && Array.isArray(responseData.data)) {
            setAuditLogs(responseData.data);
            setTotal(responseData.total || 0);
          } else {
            setAuditLogs([]);
            setTotal(0);
          }
        }
      })
      .catch((e) => {
        messageInstance.error(e.message);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [pageNum, dateRange, selectedEventType, selectedUserId, selectedAppId]);

  // 当查询条件变化时，重置到第一页
  useEffect(() => {
    setPageNum(1);
  }, [dateRange, selectedEventType, selectedUserId, selectedAppId]);

  // 当 pageNum 或查询条件变化时加载数据
  useEffect(() => {
    loadAuditLogs();
  }, [loadAuditLogs]);

  // 重置
  const handleReset = () => {
    setDateRange(null);
    setSelectedEventType(undefined);
    setSelectedUserId(undefined);
    setSelectedAppId(undefined);
    setPageNum(1);
    setSelectedRow(null);
  };

  // 分页变化
  const handlePageChange = (page: number) => {
    setPageNum(page);
    setSelectedRow(null); // 切换页面时清空选中
  };

  // 获取事件类型描述
  const getEventTypeDesc = (eventType: string) => {
    const item = eventTypes.find((e) => e.event === eventType);
    return item ? item.desc : eventType;
  };

  // 表格列定义 - 移除了详情列
  const columns = [
    {
      title: trans("auditLog.time"),
      dataIndex: "createTime",
      key: "createTime",
      width: 130,
      render: (time: number) => timestampToHumanReadable(time),
    },
    {
      title: trans("auditLog.user"),
      dataIndex: "user",
      key: "user",
      width: 150,
      render: (user: any) => user?.name || "-",
    },
    {
      title: trans("auditLog.eventType"),
      dataIndex: "eventType",
      key: "eventType",
      render: (eventType: string) => getEventTypeDesc(eventType),
    },
  ];

  return (
    <Level1SettingPageContentWithList>
      <Level1SettingPageTitleWithBtn>{trans("settings.audit")}</Level1SettingPageTitleWithBtn>

      {/* 查询条件 */}
      <Space wrap style={{ marginBottom: 16, marginLeft: 12 }}>
        <RangePicker
          value={dateRange}
          onChange={(dates) => setDateRange(dates as [Dayjs | null, Dayjs | null] | null)}
          placeholder={[trans("auditLog.startTime"), trans("auditLog.endTime")]}
          style={{ width: 400 }}
        />
        <Select
          placeholder={trans("auditLog.eventType")}
          value={selectedEventType}
          onChange={setSelectedEventType}
          allowClear
          showSearch={{
            filterOption: (input, option) => {
              const label = (option?.children as unknown as string) || "";
              if (typeof label === "string") {
                return label.toLowerCase().includes(input.toLowerCase());
              }
              return false;
            },
          }}
          style={{ width: 200 }}
        >
          {eventTypes.map((item) => (
            <Option key={item.event} value={item.event}>
              {item.desc}
            </Option>
          ))}
        </Select>
        <Select
          placeholder={trans("auditLog.userId")}
          value={selectedUserId}
          onChange={setSelectedUserId}
          allowClear
          showSearch={{
            filterOption: (input, option) => {
              const label = option?.label;
              if (typeof label === "string") {
                return label.toLowerCase().includes(input.toLowerCase());
              }
              return false;
            },
          }}
          style={{ width: 200 }}
          options={users.map((user) => ({
            label: user.name,
            value: user.id,
          }))}
        />
        <Select
          placeholder={trans("auditLog.appId")}
          value={selectedAppId}
          onChange={setSelectedAppId}
          allowClear
          showSearch={{
            filterOption: (input, option) => {
              const label = option?.label;
              if (typeof label === "string") {
                return label.toLowerCase().includes(input.toLowerCase());
              }
              return false;
            },
          }}
          style={{ width: 200 }}
          options={applications.map((app) => ({
            label: app.name,
            value: app.applicationId,
          }))}
        />
        <Button onClick={handleReset}>{trans("auditLog.reset")}</Button>
      </Space>

      {/* 表格和详情面板 */}
      <TableDetailContainer>
        <TableWrapper>
          <Table
            loading={loading}
            dataSource={auditLogs}
            columns={columns}
            rowKey="id"
            scroll={{ x: 530 }}
            onRow={(record) => ({
              onClick: () => setSelectedRow(record as AuditLog),
              style: {
                cursor: "pointer",
                backgroundColor: selectedRow?.id === (record as AuditLog).id ? "#f5f5f6" : undefined,
              },
            })}
            pagination={{
              placement: ["bottomStart"],
              current: pageNum,
              pageSize: 7,
              total: total,
              showSizeChanger: false,
              showTotal: (total) => trans("auditLog.total", { total }),
              onChange: handlePageChange,
            }}
          />
        </TableWrapper>

        {selectedRow && (
          <DetailPanel title={trans("auditLog.detailData")} >
            <JsonViewWrapper>
              <VirtualJsonTree src={selectedRow} collapsed={2} enableClipboard={true} />
            </JsonViewWrapper>
          </DetailPanel>
        )}
      </TableDetailContainer>
    </Level1SettingPageContentWithList>
  );
}
