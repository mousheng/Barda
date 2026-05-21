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

const AccountLoginWrapper = styled(FormWrapperMobile)`
  display: flex;
  flex-direction: column;
  margin-bottom: 106px;
`;

export default function FormLogin() {
  const [account, setAccount] = useState("");
  const [password, setPassword] = useState("");
  const redirectUrl = useRedirectUrl();
  const { systemConfig, inviteInfo, orgId } = useContext(AuthContext);
  const invitationId = inviteInfo?.invitationId;
  const authId = systemConfig?.form.id;
  const location = useLocation();
  const registerUrl = orgId ? `/org/${orgId}/auth/register` : AUTH_REGISTER_URL;
  const EncryptUtils = new encryptUtils(systemConfig.form.publicKey);

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
    }
    ,
    false,
    redirectUrl
  );

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
