import {
  BindCardWrapper,
  CardConfirmButton,
  StyledPasswordInput,
} from "pages/setting/profile/profileComponets";
import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import UserApi from "api/userApi";
import { validateResponse } from "api/apiUtils";
import { fetchUserAction } from "redux/reduxActions/userActions";
import { trans } from "i18n";
import { checkPassWithMsg } from "pages/userAuth/authUtils";
import { messageInstance } from "barda-design";
import { LockOutlined } from '@ant-design/icons';
import { selectSystemConfig } from "@barda/redux/selectors/configSelectors";
import encryptUtils from "@barda/util/encryptUtils";

function PasswordCard(props: { hasPass: boolean }) {
  const [oldPass, setOldPass] = useState("");
  const [pass, setPass] = useState("");
  const dispatch = useDispatch();
  const sysConfig = useSelector(selectSystemConfig);
  const EncryptUtils = new encryptUtils(sysConfig?.form?.publicKey);

  const onSubmit = async () => {
    const responsePromise = props.hasPass
      ? UserApi.updatePassword({ oldPassword: await EncryptUtils.encrypt(oldPass), newPassword: await EncryptUtils.encrypt(pass) })
      : UserApi.setPassword({ password: pass });
    const successMsg = props.hasPass
      ? trans("profile.passwordModifiedSuccess")
      : trans("profile.passwordSetSuccess");
    responsePromise
      .then((resp) => {
        if (validateResponse(resp)) {
          messageInstance.success(successMsg);
          dispatch(fetchUserAction());
        }
      })
      .catch((e) => {
        messageInstance.error(e.message);
      });
  };

  return (
    <BindCardWrapper>
      {props.hasPass ? (
        <>
          <StyledPasswordInput
            passInputConf={{
              label: trans("profile.oldPassword"),
              placeholder: trans("profile.inputCurrentPassword"),
            }}
            onChange={(value: string, valid: boolean) => {
              setOldPass(valid ? value : "");
            }}
          />
          <StyledPasswordInput
            doubleCheck
            valueCheck={checkPassWithMsg}
            onChange={(value: string, valid: boolean) => setPass(valid ? value : "")}
            passInputConf={{
              label: trans("profile.newPassword"),
              placeholder: trans("profile.inputNewPassword"),
            }}
            confirmPassConf={{
              label: trans("profile.confirmNewPassword"),
              placeholder: trans("profile.inputNewPasswordAgain"),
            }}
          />
        </>
      ) : (
        <StyledPasswordInput
          doubleCheck
          valueCheck={checkPassWithMsg}
          onChange={(value: string, valid: boolean) => setPass(valid ? value : "")}
        />
      )}
      <CardConfirmButton
        buttonType="primary"
        disabled={(props.hasPass && !oldPass) || !pass}
        onClick={onSubmit}
        icon={sysConfig?.form.enableRSA ? <LockOutlined /> : undefined}
      >
        {trans("profile.submit")}
      </CardConfirmButton>
    </BindCardWrapper>
  );
}

export default PasswordCard;
