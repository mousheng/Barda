import { Logo } from "@barda/assets/images";
import { getBrandingConfig } from "@barda/redux/selectors/configSelectors";
import { MouseEventHandler, useEffect, useState } from "react";
import { useSelector } from "react-redux";
import styled from "styled-components";
import { PlatformTitle } from "./header";

const StyledLinkWarpper = styled.a<{ $overflow?: boolean }>`
  display: flex;
  align-items: center;
  margin-right: 12px;
  ${props => props.$overflow ? "width: 32px;overflow: hidden;" : ""}
  svg {
    width: 24px;
    border-radius: 3px;
  }
  img{
    border-radius: 3px;
  }
`;

const Img = styled.img<{ height: string }>`
    height: 28px;
    width: ${props => props.width};
`

type StyledLinkProps = {
  onClick: MouseEventHandler<HTMLAnchorElement> | undefined,
  overflow?: boolean,
  showTitle?: boolean,
  type?: "index" | "editAndPreview"
}

export const StyledLinkComp: React.FC<StyledLinkProps> = (props) => {
  const brandingConfig = useSelector(getBrandingConfig);

  return <StyledLinkWarpper onClick={props.onClick} $overflow={props.overflow} >
    {brandingConfig?.logo ? <Img src={`/api/materials/${brandingConfig.logo}?type=preview`} height={props.type == "editAndPreview" ? "28px" : "auto"} /> : <Logo />}
    {props.overflow ? "" : < PlatformTitle > {!brandingConfig?.logoContainsName ? (brandingConfig?.brandName ?? "Barda") : ""}</PlatformTitle>
    }
  </StyledLinkWarpper >
}