import styled from "styled-components";
import React from "react";
import { BrandingConfig } from "@barda/constants/configConstants";
import { PlatformTitle } from "@barda/pages/common/header";
import { Skeleton } from "antd";
import { Logo } from "@barda/assets/images";

const MockBrowser = styled.div`
        height: 550px;
        -webkit-box-flex: 1;
        flex-grow: 1;
        border-radius: 7px;
        display: flex;
        flex-direction: column;
        box-shadow: rgb(215, 217, 224) 0px 0px 0px 1px;
    `

const MockBrowserTitle = styled.div`
        border-radius: 7px 7px 0px 0px;
        background-color: rgb(223, 225, 229);
        height: 46px;
        display: flex;
        -webkit-box-align: center;
        align-items: center;
        padding-left: 12px;
        margin-bottom: 4px;
    `
const MockBrowserButton = styled.div<{ color: string }>`
        border-radius: 50%;
        height: 12px;
        width: 12px;
        margin-right: 8px;
        background-color: ${props => props.color};
    `
const MockBrowserTag = styled.div`
        margin-left: 8px;
        width: 213px;
        height: 35px;
        border-radius: 10px;
        background-color: rgb(255, 255, 255);
        margin-top: 7px;
        position: relative;
        &::before {
            content: "";
            border-radius: 0px 0px 10px;
            box-shadow: rgb(255, 255, 255) 14px 0.25em 0px -4px;
            position: absolute;
            left: -100%;
            bottom: -1px;
            width: 100%;
            height: 35px;
        }
    `
const MockBrowserTagContext = styled.div`
        font-size: 13px;
        display: flex;
        -webkit-box-align: center;
        align-items: center;
        height: 100%;
        margin-left: 10px;
        &::after {
            content: " ";
            border-radius: 0px 0px 0px 10px;
            box-shadow: rgb(255, 255, 255) -14px 0.25em 0px -4px;
            position: absolute;
            right: -100%;
            bottom: -1px;
            width: 100%;
            height: 35px;
        }
        svg {
            width: 24px;
            margin-right: 5px;
        }
    `
const MockBrowserPageTitle = styled.div<{ color?: string }>`
        display: flex;
        width: 100%;
        height: 48px;
        background-color: ${props => props.color ?? "rgb(44, 44, 44)"};
        padding: 8px 24px;
        -webkit-box-pack: justify;
        justify-content: flex-start;
        align-items: center;
        svg {
            width: 24px;
            border-radius: 3px;
        }
        img{
            border-radius: 3px;
        }
    `

const Favorite = styled.img`
    margin-right: 8px;
    width: 16px;
    height: 16px;
`

const ProductText = styled.span`
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
    line-height: 15px;
`

const LogoWapper = styled.img`
    margin-right: 8px;
    max-height: 28px;
`

export const BrandingPreview: React.FC<BrandingConfig> = (props) => {
    return <MockBrowser>
        <MockBrowserTitle>
            <MockBrowserButton color={"#fe5f5b"}></MockBrowserButton>
            <MockBrowserButton color={"#ffbe2e"}></MockBrowserButton>
            <MockBrowserButton color={"#2aca44"}></MockBrowserButton>
            <MockBrowserTag>
                <MockBrowserTagContext >
                    {props.favicon ? <Favorite src={`/api/materials/${props.favicon}?type=preview`}></Favorite> : <Logo />}
                    <ProductText>{props.brandName ?? "Barda"}</ProductText>
                </MockBrowserTagContext>
            </MockBrowserTag>
        </MockBrowserTitle>
        <MockBrowserPageTitle color={props.headerColor ?? "#2c2c2c"}>
            {props.logo ? <LogoWapper src={`/api/materials/${props.logo}?type=preview`}></LogoWapper> : <Logo />}
            {props.logoContainsName ? "" : <PlatformTitle>{props.brandName ?? "Barda"}</PlatformTitle>}
        </MockBrowserPageTitle>
        <Skeleton avatar paragraph={{ rows: 8 }} style={{ padding: "30px" }} />
    </MockBrowser>
}