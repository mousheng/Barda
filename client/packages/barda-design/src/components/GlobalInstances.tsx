import { App } from 'antd';
import type { MessageInstance } from 'antd/es/message/interface';
import type { ModalStaticFunctions } from 'antd/es/modal/confirm';
import type { NotificationInstance } from 'antd/es/notification/interface';

let messageInstance: MessageInstance;
let notificationInstance: NotificationInstance;
let modalInstance: Omit<ModalStaticFunctions, 'warn'>;

const GlobalInstances = () => {
  const staticFunction = App.useApp();
  messageInstance = staticFunction.message;
  modalInstance = staticFunction.modal;
  notificationInstance = staticFunction.notification;
  return null;
};

export { GlobalInstances, messageInstance, notificationInstance, modalInstance };