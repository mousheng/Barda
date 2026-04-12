import React, { ReactNode, useEffect, useState } from "react";
import { CustomModal } from "components/CustomModal";
import { trans } from "i18n";
import { Tabs, message, Upload, Button, Table, Popconfirm, Space, Typography, Tag, Form, Input } from "antd";
import { UploadOutlined, DeleteOutlined, CloudDownloadOutlined, PlusOutlined } from "@ant-design/icons";
import styled from "styled-components";
import { useDispatch, useSelector } from "react-redux";
import { getUser } from "redux/selectors/usersSelectors";
import { currentOrgAdmin } from "util/permissionUtils";
import LibraryApi from "api/libraryApi";
import { recommendJSLibrarySelector } from "redux/selectors/jsLibrarySelector";
import { fetchJSLibraryRecommendsAction } from "redux/reduxActions/jsLibraryActions";
import { JSLibraryInfo, JSLibraryLabel } from "components/JSLibraryTree";
import { DocLink } from "components/ExternalLink";
import { Input as CustomInput } from "components/Input";
import { TacoButton } from "components/button";
import { Spin } from "antd";
import {
  CalendarDeleteIcon,
  DocBoldIcon,
  DownloadBoldIcon,
  DownloadedIcon,
  ErrorIcon,
} from "icons";
import { ActiveTextColor, GreyTextColor } from "constants/style";
import { LoadingOutlined } from "@ant-design/icons";
import { JSLibraryMeta } from "api/jsLibraryApi";
import log from "loglevel";
import { TacoMarkDown } from "components/markdown";
import { messageInstance } from "barda-design";

const { Text } = Typography;

const ModalLabel = styled.div`
  display: flex;
  justify-content: space-between;
  flex-grow: 1;
`;

const InputWrapper = styled.div`
  display: flex;
  gap: 8px;
`;

const JSLibraryRecommends = styled.div`
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 13px 56px;
  margin-top: 10px;
  overflow: hidden;
`;

const JSLibraryCardWrapper = styled.div`
  max-height: 106px;
  border-bottom: 1px solid #f0f0f0;
  width: 280px;
  margin-bottom: -1px;
`;

const StyledDocIcon = styled(DocBoldIcon)`
  margin-right: 16px;
  cursor: pointer;
  color: ${GreyTextColor};

  :hover {
    & > g > g {
      stroke: ${ActiveTextColor};
    }
  }
`;

const StyledDownloadIcon = styled(DownloadBoldIcon)`
  cursor: pointer;

  :hover {
    & > g > g {
      stroke: ${ActiveTextColor};
    }
  }
`;

const ErrorWrapper = styled.div`
  border-radius: 8px;
  background: #fff3f1;
  padding: 10px 16px;
  white-space: pre-wrap;
  margin-top: 16px;

  .error-title {
    font-size: 14px;
    font-weight: 500;
    display: flex;
    justify-content: space-between;

    .close-button {
      width: 8px;
      height: 8px;
      margin-right: -8px;
      cursor: pointer;
      color: #8b8fa3;
      display: none;

      :hover {
        color: #000000;
      }
    }
  }

  :hover {
    .close-button {
      display: block;
    }
  }

  .error-description a {
    :hover {
      color: #315efb;
    }
  }

  .markdown-body {
    background-color: unset;
    font-size: 13px;
  }
`;

const HelpText = styled(TacoMarkDown)`
  font-size: 13px;
  color: ${GreyTextColor};
  margin: 8px 0;
  line-height: 13px;
`;

const LibraryContainer = styled.div`
  .ant-table {
    margin-top: 16px;
  }

  .upload-section {
    margin-bottom: 24px;
    padding: 16px;
    background: #f5f5f5;
    border-radius: 4px;
  }
`;

interface LibraryFile {
  id: string;
  libraryId?: string;
  originalFilename?: string;
  filename: string;
  displayName?: string;
  version?: string;
  fileSize: number;
  type: string;
  description?: string;
  createdAt: number;
}

type URLErrorType = { title: string; description: ReactNode } | undefined;

interface UnifiedLibraryModalProps {
  trigger: ReactNode;
  runInHost: boolean;
  onCheck: (url?: string) => boolean;
  onLoad: (url: string) => Promise<any>;
  onSuccess: (url: string) => void;
  onDelete?: (url: string) => void;
}

const handleDownload = (props: {
  url?: string;
  runInHost: boolean;
  onCheck: (url: string) => boolean;
  onLoad: (url: string) => Promise<any>;
  onSuccess: (url: string) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: URLErrorType) => void;
}) => {
  const trimUrl = props?.url?.trim() ?? "";
  props.setLoading(true);
  return props
    .onLoad(trimUrl)
    .then(() => {
      props.onSuccess(trimUrl);
      messageInstance.success(trans("preLoad.jsLibraryInstallSuccess"));
    })
    .catch((e) => {
      if (props.runInHost) {
        props.setError({
          title: trans("preLoad.jsLibraryInstallFailed"),
          description: trans("preLoad.jsLibraryInstallFailedHost", { message: e.message }),
        });
      } else {
        props.setError({
          title: trans("preLoad.jsLibraryInstallFailed"),
          description: (
            <TacoMarkDown>
              {trans("preLoad.jsLibraryInstallFailedCloud", { message: e.message })}
            </TacoMarkDown>
          ),
        });
      }
      log.warn(e);
      props.setLoading(false);
    });
};

const Error = (props: {
  title: string;
  description: ReactNode;
  setError: (error: URLErrorType) => void;
}) => (
  <ErrorWrapper>
    <div className={"error-title"}>
      <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
        <ErrorIcon />
        {props.title}
      </div>
      <CalendarDeleteIcon className={"close-button"} onClick={() => props.setError(undefined)} />
    </div>
    <div className={"error-description"}>{props.description}</div>
  </ErrorWrapper>
);

const JSLibraryCard = (props: {
  meta: JSLibraryMeta;
  runInHost: boolean;
  onCheck: (url?: string) => boolean;
  onLoad: (url: string) => Promise<any>;
  onSuccess: (url: string) => void;
  setError: (error: URLErrorType) => void;
}) => {
  const { meta } = props;
  const [loading, setLoading] = useState(false);

  const loadingChanged = props.onCheck(meta?.downloadUrl);
  useEffect(() => {
    loading && setLoading(false);
  }, [loadingChanged]);

  return (
    <JSLibraryCardWrapper>
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <JSLibraryLabel name={meta.name} version={meta.latestVersion} />
        <div style={{ display: "flex", alignItems: "center" }}>
          {meta.homepage && <StyledDocIcon onClick={() => window.open(meta.homepage, "_blank")} />}
          {loading ? (
            <Spin indicator={<LoadingOutlined style={{ fontSize: 15 }} spin />} />
          ) : !props.onCheck(meta?.downloadUrl) ? (
            <DownloadedIcon />
          ) : (
            <StyledDownloadIcon
              onClick={() =>
                handleDownload({
                  ...props,
                  url: meta?.downloadUrl ?? "",
                  setLoading,
                })
              }
            />
          )}
        </div>
      </div>
      <JSLibraryInfo description={meta.description} />
    </JSLibraryCardWrapper>
  );
};

export function UnifiedLibraryModal(props: UnifiedLibraryModalProps) {
  const [visible, setVisible] = useState(false);
  const dispatch = useDispatch();
  const user = useSelector(getUser);
  const isAdmin = currentOrgAdmin(user);
  const recommends = useSelector(recommendJSLibrarySelector);

  // 在线库状态
  const [onlineLoading, setOnlineLoading] = useState(false);
  const [url, setURL] = useState("");
  const [urlError, setURLError] = useState<string | undefined>(undefined);
  const [installError, setInstallError] = useState<URLErrorType>(undefined);

  // 共享库和组织库状态
  const [isPrimaryOrgAdmin, setIsPrimaryOrgAdmin] = useState(false);
  const [sharedLibraries, setSharedLibraries] = useState<LibraryFile[]>([]);
  const [orgLibraries, setOrgLibraries] = useState<LibraryFile[]>([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadModalVisible, setUploadModalVisible] = useState(false);
  const [uploadType, setUploadType] = useState<"shared" | "org">("shared");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [form] = Form.useForm();
  const [addingUrls, setAddingUrls] = useState<Set<string>>(new Set());

  useEffect(() => {
    dispatch(fetchJSLibraryRecommendsAction());
  }, [dispatch]);

  useEffect(() => {
    if (visible) {
      checkPrimaryOrgAdmin();
      loadLibraries();
    }
  }, [visible]);

  const checkPrimaryOrgAdmin = async () => {
    try {
      const result = await LibraryApi.isPrimaryOrgAdmin();
      setIsPrimaryOrgAdmin(result.data.data);
    } catch (error) {
      console.error("检查主要组织管理员失败:", error);
    }
  };

  const loadLibraries = async () => {
    setLoading(true);
    try {
      const [sharedRes, orgRes] = await Promise.all([
        LibraryApi.listSharedLibraries(),
        LibraryApi.listOrgLibraries(),
      ]);
      setSharedLibraries(sharedRes.data.data || []);
      setOrgLibraries(orgRes.data.data || []);
    } catch (error) {
      message.error(trans("library.loadFailed"));
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async (file: File, type: "shared" | "org") => {
    const validExtensions = [".js", ".css"];
    const fileExtension = file.name.substring(file.name.lastIndexOf(".")).toLowerCase();
    if (!validExtensions.includes(fileExtension)) {
      message.error(trans("library.invalidFileType"));
      return false;
    }

    if (file.size > 10 * 1024 * 1024) {
      message.error(trans("library.fileTooLarge"));
      return false;
    }

    // 自动提取文件名作为显示名称（去掉扩展名）
    const displayName = file.name.substring(0, file.name.lastIndexOf("."));

    setSelectedFile(file);
    setUploadType(type);

    // 自动填充显示名称
    form.setFieldsValue({
      displayName: displayName,
    });

    setUploadModalVisible(true);
    return false;
  };

  const handleUploadConfirm = async () => {
    if (!selectedFile) return;

    try {
      const values = await form.validateFields();
      setUploading(true);

      const base64Content = await fileToBase64(selectedFile);

      if (uploadType === "shared") {
        await LibraryApi.uploadSharedLibrary({
          filename: selectedFile.name,
          content: base64Content,
          displayName: values.displayName,
          version: values.version,
          description: values.description,
        });
      } else {
        await LibraryApi.uploadOrgLibrary({
          filename: selectedFile.name,
          content: base64Content,
          displayName: values.displayName,
          version: values.version,
          description: values.description,
        });
      }

      message.success(trans("library.uploadSuccess"));
      loadLibraries();
      setUploadModalVisible(false);
      form.resetFields();
      setSelectedFile(null);
    } catch (error: any) {
      if (error.errorFields) {
        return;
      }
      message.error(error.message || trans("library.uploadFailed"));
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (filename: string, type: "shared" | "org") => {
    const url =
      type === "shared"
        ? `/api/libraries/shared/${filename}`
        : `/api/libraries/org/${filename}`;
    try {
      if (type === "shared") {
        await LibraryApi.deleteSharedLibrary(filename);
      } else {
        await LibraryApi.deleteOrgLibrary(filename);
      }
      message.success(trans("library.deleteSuccess"));
      loadLibraries();
      props.onDelete?.(url);
    } catch (error: any) {
      message.error(error.message || trans("library.deleteFailed"));
    }
  };

  const fileToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => {
        const base64 = (reader.result as string).split(",")[1];
        resolve(base64);
      };
      reader.onerror = (error) => reject(error);
    });
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + " KB";
    return (bytes / (1024 * 1024)).toFixed(2) + " MB";
  };

  // 添加库到预加载列表
  const handleAddLibrary = async (record: LibraryFile, type: "shared" | "org") => {
    const url =
      type === "shared"
        ? `/api/libraries/shared/${record.filename}`
        : `/api/libraries/org/${record.filename}`;

    // 检查是否已经添加或正在添加中
    if (!props.onCheck(url) || addingUrls.has(url)) {
      messageInstance.warning(trans("preLoad.jsLibraryExist"));
      return;
    }

    setAddingUrls((prev) => new Set(prev).add(url));
    try {
      // 加载并验证库
      await props.onLoad(url);
      // 添加到预加载列表
      props.onSuccess(url);
      messageInstance.success(trans("preLoad.jsLibraryInstallSuccess"));
      // 关闭模态框
      setVisible(false);
    } catch (error: any) {
      messageInstance.error(trans("preLoad.jsLibraryInstallFailed") + ": " + error.message);
    } finally {
      setAddingUrls((prev) => {
        const next = new Set(prev);
        next.delete(url);
        return next;
      });
    }
  };

  const columns = (type: "shared" | "org") => [
    {
      title: trans("library.displayName"),
      dataIndex: "displayName",
      key: "displayName",

    },
    {
      title: trans("library.version"),
      dataIndex: "version",
      key: "version",
      width: 120,
      render: (text: string) => (text ? <Tag color="blue">{text}</Tag> : "-"),
    },
    {
      title: trans("library.fileType"),
      dataIndex: "originalFilename",
      key: "fileType",
      width: 80,
      render: (originalFilename: string, record: LibraryFile) => {
        const filename = originalFilename || record.filename;
        if (filename.endsWith(".js")) return <Tag color="blue">JS</Tag>;
        if (filename.endsWith(".css")) return <Tag color="green">CSS</Tag>;
        return "-";
      },
    },
    {
      title: trans("library.fileSize"),
      dataIndex: "fileSize",
      key: "fileSize",
      width: 120,
      render: (size: number) => formatFileSize(size),
    },
    // {
    //   title: trans("library.uploadTime"),
    //   dataIndex: "createdAt",
    //   key: "createdAt",
    //   width: 180,
    //   render: (time: number) => new Date(time).toLocaleString(),
    // },
    {
      title: trans("library.actions"),
      key: "actions",
      width: 180,
      render: (_: any, record: LibraryFile) => {
        const url =
          type === "shared"
            ? `/api/libraries/shared/${record.filename}`
            : `/api/libraries/org/${record.filename}`;
        const isAdded = !props.onCheck(url);
        const isAdding = addingUrls.has(url);

        return (
          <Space>
            <Button
              type="link"
              icon={<PlusOutlined />}
              disabled={isAdded || isAdding}
              loading={isAdding}
              onClick={() => handleAddLibrary(record, type)}
            >
              {isAdded ? trans("library.added") : trans("library.addToPreload")}
            </Button>
            {(type === "shared" ? isPrimaryOrgAdmin : isAdmin) && (
              <Popconfirm
                title={trans("library.deleteConfirm")}
                onConfirm={() => handleDelete(record.filename, type)}
                okText={trans("delete")}
                cancelText={trans("cancel")}
              >
                <Button type="link" danger icon={<DeleteOutlined />}>
                  {trans("delete")}
                </Button>
              </Popconfirm>
            )}
          </Space>
        );
      },
    },
  ];

  const renderUploadSection = (type: "shared" | "org") => {
    const canUpload = type === "shared" ? isPrimaryOrgAdmin : isAdmin;
    if (!canUpload) return null;

    return (
      <div className="upload-section">
        <Space direction="vertical" style={{ width: "100%" }}>
          <Text strong>
            {type === "shared" ? trans("library.uploadShared") : trans("library.uploadOrg")}
          </Text>
          <Upload
            beforeUpload={(file) => handleUpload(file, type)}
            showUploadList={false}
            accept=".js,.css"
          >
            <Button icon={<UploadOutlined />} loading={uploading}>
              {trans("library.selectFile")}
            </Button>
          </Upload>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {trans("library.uploadHint")}
          </Text>
        </Space>
      </div>
    );
  };

  // 在线库标签页内容
  const renderOnlineLibraryTab = () => (
    <div>
      <div style={{ lineHeight: "10px" }}>URL</div>
      <HelpText>{trans("preLoad.urlTooltip")}</HelpText>
      <InputWrapper>
        <CustomInput
          status={urlError || installError ? "error" : undefined}
          placeholder={"https://cdn.example.com/example.min.js"}
          value={url}
          onChange={(e) => {
            setURL(e.target.value);
            const trimUrl = e.target.value.trim();
            if (trimUrl) {
              if (!/^https?.+/.test(trimUrl)) {
                setURLError(trans("preLoad.jsLibraryURLError"));
                return;
              }
              if (!props.onCheck(trimUrl)) {
                setURLError(trans("preLoad.jsLibraryExist"));
                return;
              }
            }
            setURLError(undefined);
          }}
        />
        <TacoButton
          buttonType={"primary"}
          loading={onlineLoading}
          disabled={!!urlError}
          onClick={() => {
            setInstallError(undefined);
            return handleDownload({
              ...props,
              url,
              setLoading: setOnlineLoading,
              onSuccess: (url) => {
                props.onSuccess(url);
                setVisible(false);
                setOnlineLoading(false);
              },
              setError: setInstallError,
            });
          }}
          style={{ minWidth: "80px" }}
        >
          {trans("preLoad.add")}
        </TacoButton>
      </InputWrapper>

      {urlError && <div style={{ color: "#ff4d4f", fontSize: "12px" }}>{urlError}</div>}

      {typeof installError === "object" && (
        <Error
          title={installError.title}
          description={installError.description}
          setError={setInstallError}
        />
      )}

      {recommends.length > 0 && (
        <>
          <div style={{ marginTop: "24px" }}>{trans("preLoad.recommended")}</div>
          <JSLibraryRecommends>
            {recommends.map((r, idx) => (
              <JSLibraryCard key={idx} meta={r} {...props} setError={setInstallError} />
            ))}
          </JSLibraryRecommends>
        </>
      )}
    </div>
  );

  const tabItems = [
    {
      key: "online",
      label: trans("library.onlineLibraries"),
      children: renderOnlineLibraryTab(),
    },
    {
      key: "shared",
      label: trans("library.sharedLibraries"),
      children: (
        <LibraryContainer>
          {renderUploadSection("shared")}
          <Table
            columns={columns("shared")}
            dataSource={sharedLibraries}
            rowKey="id"
            loading={loading}
            pagination={false}
          />
        </LibraryContainer>
      ),
    },
    {
      key: "org",
      label: trans("library.orgLibraries"),
      children: (
        <LibraryContainer>
          {renderUploadSection("org")}
          <Table
            columns={columns("org")}
            dataSource={orgLibraries}
            rowKey="id"
            loading={loading}
            pagination={false}
          />
        </LibraryContainer>
      ),
    },
  ];

  return (
    <div onClick={(e: React.MouseEvent) => e.stopPropagation()}>
      <div onClick={() => setVisible(true)}>{props.trigger}</div>
      <CustomModal
        draggable
        open={visible}
        title={
          <ModalLabel>
            {trans("library.title")}
            {trans("docUrls.thirdLib") && (
              <DocLink href={trans("docUrls.thirdLib")}>{trans("docUrls.thirdLibUrlText")}</DocLink>
            )}
          </ModalLabel>
        }
        onCancel={() => setVisible(false)}
        afterClose={() => {
          setURLError(undefined);
          setInstallError(undefined);
          setURL("");
        }}
        destroyOnHidden={true}
        showOkButton={false}
        showCancelButton={false}
        width="800px"
      >
        <Tabs defaultActiveKey="online" items={tabItems} />
      </CustomModal>

      {/* 上传元数据模态框 */}
      <CustomModal
        open={uploadModalVisible}
        zIndex={1099}
        title={trans("library.uploadMetadata")}
        onOk={handleUploadConfirm}
        onCancel={() => {
          setUploadModalVisible(false);
          form.resetFields();
          setSelectedFile(null);
        }}
        okButtonProps={{ loading: uploading }}
        okText={trans("library.upload")}
        cancelText={trans("cancel")}
      >
        <Form form={form} layout="vertical">
          <Form.Item label={trans("library.filename")}>
            <Input value={selectedFile?.name} disabled />
          </Form.Item>
          <Form.Item
            name="displayName"
            label={trans("library.displayName")}
            rules={[{ required: true, message: trans("library.displayNameRequired") }]}
          >
            <Input placeholder={trans("library.displayNamePlaceholder")} />
          </Form.Item>
          <Form.Item
            name="version"
            label={trans("library.version")}
            rules={[{ required: true, message: trans("library.versionRequired") }]}
          >
            <Input placeholder={trans("library.versionPlaceholder")} />
          </Form.Item>
          <Form.Item name="description" label={trans("library.libraryDescription")}>
            <Input.TextArea rows={3} placeholder={trans("library.descriptionPlaceholder")} />
          </Form.Item>
        </Form>
      </CustomModal>
    </div>
  );
}
