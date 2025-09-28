/// <reference types="@welldone-software/why-did-you-render" />
// ^ https://github.com/welldone-software/why-did-you-render/issues/161
// 在index.ts中使用 import "./wdyr" 引入

import React from 'react';
import whyDidYouRender from '@welldone-software/why-did-you-render';

if (process.env.NODE_ENV === "development") {
  whyDidYouRender(React, {
    trackAllPureComponents: false,
    include: [
      /^NewGridLayout$/,
    ],
    // trackExtraHooks: [
    //   [useSelector, "useSelector"]
    // ],
    logOnDifferentValues: true,
    trackHooks: true,
  });
}
