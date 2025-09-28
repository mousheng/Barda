export {};

declare global {
  interface Window {
    printPerf: () => void;
    __BARDA_DEV__?: {};
    dayjs: {};
  }
}
