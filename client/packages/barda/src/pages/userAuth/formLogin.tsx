import { FormInput, PasswordInput } from "barda-design";
import {
  AuthBottomView,
  ConfirmButton,
  FormWrapperMobile,
  LoginCardTitle,
  StyledRouteLink,
} from "pages/userAuth/authComponents";
import React, { useContext, useState } from "react";
import styled from "styled-components";
import UserApi from "api/userApi";
import OrgApi from "api/orgApi";
import { useRedirectUrl } from "util/hooks";
import { checkEmailValid, checkPhoneValid } from "util/stringUtils";
import { UserConnectionSource } from "@barda/constants/userConstants";
import { trans } from "i18n";
import { AuthContext, useAuthSubmit } from "pages/userAuth/authUtils";
import { ThirdPartyAuth } from "pages/userAuth/thirdParty/thirdPartyAuth";
import { AUTH_REGISTER_URL } from "constants/routesURL";
import { useLocation } from "react-router-dom";
import { LockOutlined } from '@ant-design/icons';
import encryptUtils from "@barda/util/encryptUtils";
import { doValidResponse } from "api/apiUtils";
import { Org } from "constants/orgConstants";
import { validateResponse } from "api/apiUtils";
import { messageInstance } from "barda-design";
import { default as Card } from "antd/es/card";
import { default as Typography } from "antd/es/typography";
import Flex from "antd/es/flex";
import OrgLogo from "pages/common/orgLogo";

const AccountLoginWrapper = styled(FormWrapperMobile)`
  display: flex;
  flex-direction: column;
  margin-bottom: 106px;
`;

const OrgCard = styled(Card)`
  cursor: pointer;
  transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12); }
`;

const StyledOrgLogo = styled(OrgLogo)`
  width: 40px;
  height: 40px;
  border-radius: 6px;

  img {
    border-radius: 6px;
  }
`;

const OrgPickerWrapper = styled.div`
  width: 100%;
  max-width: 408px;
`;

export default function FormLogin() {
  const [account, setAccount] = useState("");
  const [password, setPassword] = useState("");
  const [orgs, setOrgs] = useState<Org[] | null>(null);
  const [selecting, setSelecting] = useState<string | null>(null);
  const redirectUrl = useRedirectUrl();
  const { systemConfig, inviteInfo, orgId } = useContext(AuthContext);
  const invitationId = inviteInfo?.invitationId;
  const authId = systemConfig?.form.id;
  const location = useLocation();
  const registerUrl = orgId ? `/org/${orgId}/auth/register` : AUTH_REGISTER_URL;
  const EncryptUtils = new encryptUtils(systemConfig.form.publicKey);

  const handleSelectOrg = (o: Org) => {
    setSelecting(o.id);
    OrgApi.switchOrg(o.id)
      .then((resp) => {
        if (validateResponse(resp)) {
          window.location.replace(redirectUrl || "/");
        }
      })
      .catch((e) => {
        messageInstance.error(e.message);
        setSelecting(null);
      });
  };

  const { onSubmit, loading } = useAuthSubmit(
    async () => {
      return UserApi.formLogin({
        register: false,
        loginId: await EncryptUtils.encrypt(account),
        password: await EncryptUtils.encrypt(password),
        invitationId: invitationId,
        source: UserConnectionSource.email,
        authId,
        orgId,
      })
    },
    false,
    redirectUrl,
    orgId ? undefined : async (resp) => {
      if (doValidResponse(resp)) {
        try {
          const userResp = await UserApi.getUser();
          const orgsAndRoles = userResp.data?.data?.orgAndRoles;
          if (orgsAndRoles && orgsAndRoles.length > 1) {
            setOrgs(orgsAndRoles.map((oar) => oar.org));
            return false;
          }
        } catch {}
      }
      return true;
    }
  );

  if (orgs) {
    return (
      <>
        <LoginCardTitle>{trans("userAuth.selectOrgTitle")}</LoginCardTitle>
        <OrgPickerWrapper>
          <Typography.Text style={{ marginBottom: 16, display: "block" }}>
            {trans("userAuth.selectOrgDesc")}
          </Typography.Text>
          <Flex vertical gap="12px">
            {orgs.map((org) => (
              <OrgCard
                key={org.id}
                hoverable
                loading={selecting === org.id}
                onClick={() => handleSelectOrg(org)}
              >
                <Flex align="center" gap="12px">
                  <StyledOrgLogo source={org.logoUrl} orgName={org.name} side={40} />
                  <Flex vertical>
                    <Typography.Text strong>{org.name}</Typography.Text>
                    {org.isPrimaryOrganization && (
                      <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                        {trans("userAuth.primaryOrg")}
                      </Typography.Text>
                    )}
                  </Flex>
                </Flex>
              </OrgCard>
            ))}
          </Flex>
        </OrgPickerWrapper>
      </>
    );
  }

  return (
    <>
      <LoginCardTitle>{trans("userAuth.login")}</LoginCardTitle>
      <AccountLoginWrapper>
        <FormInput
          className="form-input"
          label={trans("userAuth.email")}
          onChange={(value: string, valid: boolean) => setAccount(valid ? value : "")}
          placeholder={trans("userAuth.inputEmail")}
          checkRule={{
            check: (value) => checkPhoneValid(value) || checkEmailValid(value),
            errorMsg: trans("userAuth.inputValidEmail"),
          }}
        />
        <PasswordInput
          className="form-input"
          onChange={(value) => setPassword(value)}
          valueCheck={() => [true, ""]}
        />
        <ConfirmButton
          loading={loading}
          disabled={!account || !password}
          onClick={onSubmit}
          icon={systemConfig.form.enableRSA ? <LockOutlined /> : undefined}
        >
          {trans("userAuth.login")}
        </ConfirmButton>
      </AccountLoginWrapper>
      <AuthBottomView>
        <ThirdPartyAuth invitationId={invitationId} authGoal="login" />
        {systemConfig.form.enableRegister && (
          <StyledRouteLink to={{ pathname: registerUrl, state: location.state }}>
            {trans("userAuth.register")}
          </StyledRouteLink>
        )}
      </AuthBottomView>
    </>
  );
}
