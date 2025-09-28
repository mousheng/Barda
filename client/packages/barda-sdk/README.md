# barda-sdk

## 用法(Usage)

yarn:

```bash
yarn add barda-sdk
```

npm:

```bash
npm install barda-sdk 
```

## 将Barda的应用程序/模块集成到现有的应用程序页面中(Integrate Barda' app/module into existing app page)

1. 在Barda中发布您的应用程序/模块（Publish your app/module in Barda）.
2. 将应用程序/模块的访问权限设置为公用（Set the app/module's access privilege as public）.
3. 在现有应用程序中添加代码，如下所示（Add code in your existing app as below）.


### 导入样式(Import style)

```ts
import "barda-sdk/dist/style.css";
```

### React程序(For react app):

```ts
import { BardaAppView } from "barda-sdk";

<BardaAppView appId="{YOUR_APPLICATION_ID}" />;
```

#### 参数(BardaViewProps)

| Name                   | Type                        | Description                                                                             | Default value              |
| ---------------------- | --------------------------- | --------------------------------------------------------------------------------------- | -------------------------- |
| appId                  | string                      | app的程序ID,必要!(The app's id in Barda,Required! )                                                   | --                         |
| baseUrl                | string                      | Barda 基础地址 (Barda' api base url)                                                                | https://api.barda.dev |
| onModuleEventTriggered | (eventName: string) => void | 当模块的自定义事件被触发时触发。仅当应用程序是模块时才有效 (Triggered when module's custom event is triggered. Works only when the app is a module.) | --                         |
| onModuleOutputChange   | (output: any) => void       | (当模块的输出发生变化时触发。仅当应用程序是一个模块时才有效。) Triggered when module's outputs change. Works only when the app is a module.            | --                         |

#### 调用模块方法（Invoke module methods）

```tsx
import { useRef } from "ref";
import { BardaAppView } from "barda-sdk";

function MyExistingAppPage() {
  const appRef = useRef();
  return (
    <div>
      <BardaAppView appId={YOUR_APPLICATION_ID} ref={appRef} />;
      <button onClick={() => appRef.current?.invokeMethod("some-method-name")}>
        Invoke method
      </button>
    </div>
  );
}
```

### For vanilla js:

```js
import { bootstrapAppAt } from "barda-sdk";

const node = document.querySelector("#my-app");

async function bootstrap() {
  const instance = await bootstrapAppAt(YOUR_APPLICATION_ID, node);

  // set module inputs
  instance.setModuleInputs({ input1: "xxx", input2: "xxx" });

  // invoke module methods
  instance.setModuleInputs({ input1: "xxx", input2: "xxx" });

  // listen module event trigger
  instance.on("moduleEventTriggered", (eventName) => {
    console.info("event triggered:", eventName);
  });

  // listen module output change
  instance.on("moduleOutputChange", (data) => {
    console.info("output data:", data);
  });
}
```
