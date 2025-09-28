import { MaterialUploadTypeEnum } from "@barda/api/materialApi";
import { BrandingConfig } from "@barda/constants/configConstants";
import { trans } from "@barda/i18n";
import { updateBrandingConfig } from "@barda/redux/reduxActions/configActions";
import { getBrandingConfig } from "@barda/redux/selectors/configSelectors";
import { Flex, Checkbox } from "antd";
import { CustomModal, messageInstance, TacoButton } from "barda-design";
import { ColorSelect } from "components/colorSelect";
import { isValidColor, toHex } from "components/colorSelect/colorUtils";
import { TacoInput } from "components/tacoInput";
import { debounce, get as lodashGet } from "lodash";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { setCommonSettings } from "redux/reduxActions/commonSettingsActions";
import { getUser } from "redux/selectors/usersSelectors";
import styled from "styled-components";
import { useShallowEqualSelector } from "util/hooks";
import { Level1SettingPageContentWithList, Level1SettingPageTitleWithBtn } from "../styled";
import { BrandingPreview } from "./BrandingPreview";
import { UploadIconComponent } from "./uploadIconComponent";
import { Prompt } from "react-router";
import history from "util/history";
import { Location } from "history";

const ColorSelectWapper = styled.div`
    width: 26px;
    margin: 0 10px 0 0;
`
const BrandingSetting = styled.div`
    display: flex;
    flex-direction: column;
    width: 40%;
    gap: 32px;
    padding: 0 30px 0 10px;
    `
const Text = styled.div`
    margin-bottom: 5px;
    margin-top: 5px;
    color: #333333;
`
const SubText = styled.div`
    margin-top: 10px;
    color: #a8a8a8;
    `
const BrandingHorizontalSpace = styled.div`
    width: 100%;
    display: flex;
    align-items: center;
`

const ColorInput = styled(TacoInput)`
    width: 100%;
`
const SaveButton = styled(TacoButton)`
    padding: 4px 8px;
    min-width: 84px;
    height: 32px;
    width: 64px;
    margin-top: 20px;
`;

let locationInfo: Location | Location<unknown> | null = null;

export function BrandingConfigComp() {
    const dispatch = useDispatch();
    const currentUser = useSelector(getUser);
    const commonSettings = useShallowEqualSelector(getBrandingConfig);

    const [settings, setSettings] = useState(commonSettings);
    const [canLeave, setCanLeave] = useState(false);
    const [headerColor, setHeaderColor] = useState(lodashGet(commonSettings, "headerColor", ""));
    const [brandName, setBrandName] = useState(lodashGet(commonSettings, "brandName", ""));
    const [logo, setLogo] = useState(lodashGet(commonSettings, "logo", ""));
    const [logoContainsName, setLogoContainsName] = useState(lodashGet(commonSettings, "logoContainsName", false));
    const [favicon, setFavicon] = useState(lodashGet(commonSettings, "favicon", ""));
    const isNotChange = JSON.stringify(settings) === JSON.stringify(commonSettings);
    useEffect(() => {
        if (canLeave) {
            history.push((locationInfo as Location)?.pathname);
        }
    }, [canLeave]);

    useEffect(() => {
        setSettings({
            ...headerColor ? { headerColor } : {},
            ...brandName ? { brandName } : {},
            ...logo ? { logo } : {},
            ...favicon ? { favicon } : {},
            ...logoContainsName && logo ? { logoContainsName } : {},
        })
    }, [headerColor, brandName, logo, favicon, logoContainsName])


    const colorInputBlur = () => {
        if (!headerColor || !isValidColor(headerColor)) {
            setHeaderColor(headerColor);
        } else {
            setHeaderColor(toHex(headerColor));
        }
    };

    const handleSave = (key: keyof typeof settings, onSuccess?: () => void) => {
        return (value?: any) => {
            dispatch(
                setCommonSettings({
                    orgId: currentUser.currentOrgId,
                    data: {
                        key,
                        value: value ?? settings![key],
                    },
                    onSuccess: () => {
                        onSuccess?.();
                        messageInstance.success(trans("advanced.saveSuccess"));
                        dispatch(updateBrandingConfig(settings as BrandingConfig));
                    },
                })
            );
        };
    };

    return (
        <Level1SettingPageContentWithList>
            <Prompt
                message={(location) => {
                    locationInfo = location;

                    if (!canLeave && isNotChange) {
                        setCanLeave(true);
                    }
                    if (canLeave) {
                        return true;
                    }
                    CustomModal.confirm({
                        title: trans("theme.leaveTipTitle"),
                        content: trans("theme.leaveTipContent"),
                        okText: trans("theme.leaveTipOkText"),
                        onConfirm: () => {
                            setCanLeave(true);
                        },
                    });
                    return false;
                }}
                when={!isNotChange}
            />
            <Level1SettingPageTitleWithBtn>
                {trans("settings.branding")}
            </Level1SettingPageTitleWithBtn>
            <Flex gap="middle" vertical={false}>
                <BrandingSetting>
                    <div >
                        <Text>{trans("branding.logoTitle")}</Text>
                        <UploadIconComponent
                            imgUrl={logo}
                            onURLChange={setLogo}
                            allowTypes={["jpg", "png", "svg", "jpeg"]}
                            uploadType={MaterialUploadTypeEnum.LOGO}
                            backgroundColor={headerColor ? headerColor : "#2c2c2c"}
                        ></UploadIconComponent>
                        <SubText>{trans("branding.logoHelp")}</SubText>
                        <Checkbox
                            disabled={!logo}
                            checked={logoContainsName}
                            onChange={(e) => {
                                setLogoContainsName(e.target.checked);
                            }}
                        >
                            {trans("branding.logoContainsName")}
                        </Checkbox>
                    </div>
                    <div>
                        <Text>{trans("branding.headColorTitle")}</Text>
                        <BrandingHorizontalSpace>
                            <ColorSelectWapper>
                                <ColorSelect
                                    changeColor={debounce(setHeaderColor, 500, {
                                        leading: true,
                                        trailing: true,
                                    })}
                                    color={headerColor ?? "#2c2c2c"}
                                />
                            </ColorSelectWapper>
                            <ColorInput
                                value={headerColor ?? "#2c2c2c"}
                                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setHeaderColor(e.target.value)}
                                onBlur={colorInputBlur}
                                onKeyUp={(e: React.KeyboardEvent) => e.nativeEvent.key === "Enter" && colorInputBlur()}
                            />
                        </BrandingHorizontalSpace>
                    </div>
                    <div>
                        <Text>{trans("branding.brandNameTitle")}</Text>
                        <BrandingHorizontalSpace>
                            <ColorInput
                                value={brandName ?? "Barda"}
                                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setBrandName(e.target.value)}
                            />
                        </BrandingHorizontalSpace>
                    </div>
                    <div>
                        <Text>{trans("branding.faviconTitle")}</Text>
                        <UploadIconComponent
                            imgUrl={favicon}
                            onURLChange={setFavicon}
                            allowTypes={["jpg", "png", "svg", "jpeg", "ico"]}
                            uploadType={MaterialUploadTypeEnum.FAVICON}
                        ></UploadIconComponent>
                        <SubText>{trans("branding.faviconHelp")}</SubText>
                    </div>

                </BrandingSetting>

                <BrandingPreview {...settings} />
            </Flex>
            <SaveButton
                buttonType={isNotChange ? "blue" : "primary"}
                disabled={isNotChange}
                onClick={() => handleSave("branding" as never)(settings)}
            >
                {trans("advanced.saveBtn")}
            </SaveButton>
        </Level1SettingPageContentWithList>
    )
}