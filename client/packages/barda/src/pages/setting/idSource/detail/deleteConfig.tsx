import { default as Button } from "antd/es/button";
import { default as Tooltip } from "antd/es/tooltip";
import { DeleteWrapper } from "pages/setting/idSource/styledComponents";
import { trans } from "i18n";
import { useState } from "react";
import { validateResponse } from "api/apiUtils";
import IdSourceApi from "api/idSourceApi";
import { CustomModal } from "barda-design";
import history from "util/history";
import { IDSOURCE_SETTING } from "constants/routesURL";
import { messageInstance } from "barda-design";
import Flex from "antd/es/flex";
import Alert from "antd/es/alert";
import { AuthType } from "@barda/pages/setting/idSource/idSourceConstants";

export const DeleteConfig = (props: {
  id: string,
  allowDisable?: boolean,
  isLastEnabledConfig?: boolean,
  authType?: AuthType,
}) => {
  const [disableLoading, setDisableLoading] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const allowDelete = !props.allowDisable && props.authType === AuthType.Generic;

  const handleDisable = () => {
    const action = trans("idSource.disable");
    CustomModal.confirm({
      title: trans("idSource.disableTip"),
      content: trans("idSource.disableContentWithAction", {action}) || trans("idSource.disableContent"),
      onConfirm: () => {
        setDisableLoading(true);
        IdSourceApi.deleteConfig(props.id)
        .then((resp) => {
          if (validateResponse(resp)) {
              messageInstance.success(trans("idSource.disableSuccess"), 0.8, () =>
                history.push(IDSOURCE_SETTING)
              );
            }
          })
          .catch((e) => messageInstance.error(e.message))
          .finally(() => setDisableLoading(false));
      },
    });
  };

  const handleDelete = () => {
    CustomModal.confirm({
      title: trans("idSource.deleteTip"),
      content: trans("idSource.deleteContent"),
      onConfirm: () => {
        setDeleteLoading(true);
        IdSourceApi.deleteConfig(props.id)
        .then((resp) => {
          if (validateResponse(resp)) {
              messageInstance.success(trans("idSource.deleteSuccess"), 0.8, () =>
                history.push(IDSOURCE_SETTING)
              );
            }
          })
          .catch((e) => messageInstance.error(e.message))
          .finally(() => setDeleteLoading(false));
      },
    });
  };

  return (
    <DeleteWrapper>
      <h4>{trans("idSource.dangerLabel")}</h4>
      <Alert
        className="danger-tip"
        description={trans("idSource.dangerTip")}
        type="warning"
        showIcon
      />
      {props.isLastEnabledConfig && (
        <Alert
          className="danger-tip"
          description={trans("idSource.lastEnabledConfig")}
          type="warning"
          showIcon
        />
      )}
      {allowDelete && (
        <Alert
          className="danger-tip"
          description={trans("idSource.deleteImpactTip")}
          type="error"
          showIcon
        />
      )}
      <Flex gap={8}>
        {props.allowDisable && (
          <Tooltip title={props.isLastEnabledConfig ? trans("idSource.lastEnabledConfig") : undefined}>
            <Button danger disabled={props.isLastEnabledConfig} loading={disableLoading} onClick={() => handleDisable()}>
              {trans("idSource.disable")}
            </Button>
          </Tooltip>
        )}
        {allowDelete && (
          <Button danger loading={deleteLoading} onClick={() => handleDelete()}>
            {trans("idSource.delete")}
          </Button>
        )}
      </Flex>
    </DeleteWrapper>
  );
};
