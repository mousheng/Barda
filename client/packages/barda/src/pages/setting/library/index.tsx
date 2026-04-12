import React, { useEffect, useState } from "react";
import { message, Upload, Button, Table, Popconfirm, Tabs, Space, Typography, Tag, Modal, Form, Input } from "antd";
import { UploadOutlined, DeleteOutlined, CloudDownloadOutlined } from "@ant-design/icons";
import { trans } from "i18n";
import { useSelector } from "react-redux";
import { getUser } from "redux/selectors/usersSelectors";
import { currentOrgAdmin } from "util/permissionUtils";
import LibraryApi from "api/libraryApi";
import styled from "styled-components";

const { Text } = Typography;

const LibraryContainer = styled.div`
  .ant-table {
    margin-top: 16px;
  }

  .library-header {
    margin-bottom: 24px;
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

export function LibraryManagement() {
  const user = useSelector(getUser);
  const isAdmin = currentOrgAdmin(user);
  const [isPrimaryOrgAdmin, setIsPrimaryOrgAdmin] = useState(false);
  const [sharedLibraries, setSharedLibraries] = useState<LibraryFile[]>([]);
  const [orgLibraries, setOrgLibraries] = useState<LibraryFile[]>([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadModalVisible, setUploadModalVisible] = useState(false);
  const [uploadType, setUploadType] = useState<"shared" | "org">("shared");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    checkPrimaryOrgAdmin();
    loadLibraries();
  }, []);

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
    // 验证文件类型
    const validExtensions = [".js", ".css"];
    const fileExtension = file.name.substring(file.name.lastIndexOf(".")).toLowerCase();
    if (!validExtensions.includes(fileExtension)) {
      message.error(trans("library.invalidFileType"));
      return false;
    }

    // 验证文件大小（10MB）
    if (file.size > 10 * 1024 * 1024) {
      message.error(trans("library.fileTooLarge"));
      return false;
    }

    // 打开模态框收集元数据
    setSelectedFile(file);
    setUploadType(type);
    setUploadModalVisible(true);

    return false; // 阻止默认上传行为
  };

  const handleUploadConfirm = async () => {
    if (!selectedFile) return;

    try {
      const values = await form.validateFields();
      setUploading(true);

      // 读取文件内容并转换为 Base64
      const base64Content = await fileToBase64(selectedFile);

      if (uploadType === "shared") {
        await LibraryApi.uploadSharedLibrary({
          filename: selectedFile.name,
          content: base64Content,
          displayName: values.displayName,
          version: values.version,
          description: values.description,
        });
        message.success(trans("library.uploadSuccess"));
      } else {
        await LibraryApi.uploadOrgLibrary({
          filename: selectedFile.name,
          content: base64Content,
          displayName: values.displayName,
          version: values.version,
          description: values.description,
        });
        message.success(trans("library.uploadSuccess"));
      }

      loadLibraries();
      setUploadModalVisible(false);
      form.resetFields();
      setSelectedFile(null);
    } catch (error: any) {
      if (error.errorFields) {
        // 表单验证错误，不关闭模态框
        return;
      }
      message.error(error.message || trans("library.uploadFailed"));
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (filename: string, type: "shared" | "org") => {
    try {
      if (type === "shared") {
        await LibraryApi.deleteSharedLibrary(filename);
      } else {
        await LibraryApi.deleteOrgLibrary(filename);
      }
      message.success(trans("library.deleteSuccess"));
      loadLibraries();
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

  const columns = (type: "shared" | "org") => [
    {
      title: trans("library.displayName"),
      dataIndex: "displayName",
      key: "displayName",
      render: (text: string, record: LibraryFile) => (
        <Space direction="vertical" size={0}>
          <Text strong>{text || record.originalFilename || record.filename}</Text>
          {record.libraryId && (
            <Text type="secondary" style={{ fontSize: 12 }}>
              ID: {record.libraryId}
            </Text>
          )}
          {record.originalFilename && (
            <Text type="secondary" style={{ fontSize: 12 }}>
              {record.originalFilename}
            </Text>
          )}
        </Space>
      ),
    },
    {
      title: trans("library.version"),
      dataIndex: "version",
      key: "version",
      width: 120,
      render: (text: string) => text ? <Tag color="blue">{text}</Tag> : "-",
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
    {
      title: trans("library.uploadTime"),
      dataIndex: "createdAt",
      key: "createdAt",
      width: 180,
      render: (time: number) => new Date(time).toLocaleString(),
    },
    {
      title: trans("library.actions"),
      key: "actions",
      width: 180,
      render: (_: any, record: LibraryFile) => (
        <Space>
          <Button
            type="link"
            icon={<CloudDownloadOutlined />}
            onClick={() => {
              // 使用存储文件名（filename）进行下载
              const url = type === "shared"
                ? `/api/libraries/shared/${record.filename}`
                : `/api/libraries/org/${record.filename}`;
              window.open(url, "_blank");
            }}
          >
            {trans("library.download")}
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
      ),
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

  const tabItems = [
    {
      key: "shared",
      label: trans("library.sharedLibraries"),
      children: (
        <>
          {renderUploadSection("shared")}
          <Table
            columns={columns("shared")}
            dataSource={sharedLibraries}
            rowKey="id"
            loading={loading}
            pagination={false}
          />
        </>
      ),
    },
    {
      key: "org",
      label: trans("library.orgLibraries"),
      children: (
        <>
          {renderUploadSection("org")}
          <Table
            columns={columns("org")}
            dataSource={orgLibraries}
            rowKey="id"
            loading={loading}
            pagination={false}
          />
        </>
      ),
    },
  ];

  return (
    <LibraryContainer>
      <Tabs defaultActiveKey="shared" items={tabItems} />

      <Modal
        title={trans("library.uploadMetadata")}
        open={uploadModalVisible}
        onOk={handleUploadConfirm}
        onCancel={() => {
          setUploadModalVisible(false);
          form.resetFields();
          setSelectedFile(null);
        }}
        confirmLoading={uploading}
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
          <Form.Item
            name="description"
            label={trans("library.libraryDescription")}
          >
            <Input.TextArea
              rows={3}
              placeholder={trans("library.descriptionPlaceholder")}
            />
          </Form.Item>
        </Form>
      </Modal>
    </LibraryContainer>
  );
}

export default LibraryManagement;
