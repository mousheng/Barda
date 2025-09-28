import React, { Dispatch, SetStateAction, useState } from 'react';
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import { message, Upload as AntUpload } from 'antd';
import type { GetProp, UploadProps } from 'antd';
import MaterialApi, { MaterialUploadTypeEnum } from '@barda/api/materialApi';
import { trans } from '@barda/i18n';
import styled from 'styled-components';

type FileType = Parameters<GetProp<UploadProps, 'beforeUpload'>>[0];

const getBase64 = (img: FileType, callback: (url: string) => void) => {
    const reader = new FileReader();
    reader.addEventListener('load', () => callback(reader.result as string));
    reader.readAsDataURL(img);
};
interface uploadIconComponentProps {
    imgUrl: string,
    onURLChange: Dispatch<SetStateAction<string>>;
    maxSize?: number;
    allowTypes: string[];
    uploadType: MaterialUploadTypeEnum,
    backgroundColor?: string,
}
const Upload = styled(AntUpload)`
    .ant-upload{
        justify-content: space-around!important;
    }
`

const Img = styled.img<{ backgroundColor?: string }>`
    height: 100px;
    background-color: ${props => props.backgroundColor};
    object-fit: contain;
    max-width: 400px;
`


export const UploadIconComponent: React.FC<uploadIconComponentProps> = ({ imgUrl, onURLChange, maxSize, allowTypes, uploadType, backgroundColor }) => {
    const [loading, setLoading] = useState(false);
    const [imageUrl, setImageUrl] = useState(imgUrl ? `/api/materials/${imgUrl}?type=preview` : "");

    const handleChange: UploadProps['onChange'] = (info) => {
        if (info.file.status === 'uploading') {
            setLoading(true);
            return;
        }
        if (info.file.status === 'done') {
            getBase64(info.file.originFileObj as FileType, (url) => {
                setLoading(false);
                setImageUrl(url);
            });
        }
    };

    const uploadButton = (
        <button style={{ border: 0, background: 'none' }} type="button">
            {loading ? <LoadingOutlined /> : <PlusOutlined />}
            <div style={{ marginTop: 8 }}>{trans("branding.upload")}</div>
        </button>
    );

    const beforeUpload = (file: FileType) => {
        const isJpgOrPng = allowTypes.includes(file.name.split('.').pop()?.toLowerCase() as string);
        if (!isJpgOrPng) {
            message.error(trans("branding.typeError", { fileTypes: allowTypes.join("/") }));
        }
        const isLt2M = file.size / 1024 / 1024 < (maxSize ?? 2);
        if (!isLt2M) {
            message.error(trans("branding.sizeError", { size: maxSize }));
        }
        if (isJpgOrPng && isLt2M) {
            getBase64(file, (base64) => {
                MaterialApi.upload(file.name.toLowerCase(), uploadType, base64.replace(/^data:[\/\-+\w]+;base64,/, ''))
                    .then((response) => {
                        if (response.status == 200) {
                            let uri = `/api/materials/${response.data.data.id}?type=preview`
                            setImageUrl(uri);
                            onURLChange(response.data.data.id)
                        } else {
                            message.error(response.statusText);
                        }
                    });
            });
        }
        return false;
    };

    return (
        <Upload
            name="avatar"
            listType="picture-card"
            showUploadList={false}
            action="/api/materials"
            beforeUpload={beforeUpload}
            onChange={handleChange}
        >
            {imageUrl ? <Img
                src={imageUrl}
                alt="avatar"
                onClick={(e) => {
                    onURLChange("");
                    setImageUrl("");
                    e.stopPropagation();
                }}
                backgroundColor={backgroundColor}
            /> : uploadButton}
        </Upload>
    );
};