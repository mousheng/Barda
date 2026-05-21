import { useRef, useState } from "react";
import { trans } from "i18n";
import { default as Input } from "antd/es/input";
import { default as Button } from "antd/es/button";
import { UploadOutlined, DeleteOutlined } from "@ant-design/icons";
import Space from "antd/es/space";
import Flex from "antd/es/flex";
import styled from "styled-components";

const Preview = styled.div`
  width: 48px;
  height: 48px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  background: #fafafa;

  img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
  }
`;

const HiddenInput = styled.input`
  display: none;
`;

export function ImageUrlInput(props: {
  value: string;
  onChange: (value: string) => void;
}) {
  const { value, onChange } = props;
  const fileRef = useRef<HTMLInputElement>(null);
  const [url, setUrl] = useState(value);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result as string;
      setUrl(result);
      onChange(result);
    };
    reader.readAsDataURL(file);
    e.target.value = "";
  };

  const handleUrlChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newUrl = e.target.value;
    setUrl(newUrl);
    onChange(newUrl);
  };

  const handleClear = () => {
    setUrl("");
    onChange("");
  };

  return (
    <Flex vertical gap="8px">
      <Space.Compact style={{ width: "100%" }}>
        <Input
          value={url}
          onChange={handleUrlChange}
          placeholder={trans("idSource.imageUrlPlaceholder")}
          style={{ flex: 1 }}
        />
        <Button
          icon={<UploadOutlined />}
          onClick={() => fileRef.current?.click()}
        >
          {trans("idSource.uploadImage")}
        </Button>
        {url && (
          <Button
            danger
            icon={<DeleteOutlined />}
            onClick={handleClear}
          />
        )}
      </Space.Compact>
      <HiddenInput
        ref={fileRef}
        type="file"
        accept="image/*"
        onChange={handleFileChange}
      />
      {url && (
        <Preview>
          <img src={url} alt="" />
        </Preview>
      )}
    </Flex>
  );
}
