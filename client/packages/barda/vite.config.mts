import dotenv from "dotenv";
import { defineConfig, ServerOptions, UserConfig } from "vite";
import react from "@vitejs/plugin-react";
import viteTsconfigPaths from "vite-tsconfig-paths";
import svgrPlugin from "vite-plugin-svgr";
import checker from "vite-plugin-checker";
import { visualizer } from "rollup-plugin-visualizer";
import path from "path";
import chalk from "chalk";
import { createHtmlPlugin } from "vite-plugin-html";
import { ensureLastSlash } from "barda-dev-utils/util";
import { buildVars } from "barda-dev-utils/buildVars";
import { globalDepPlugin } from "barda-dev-utils/globalDepPlguin";
import { copyFileSync, mkdirSync, readdirSync, statSync } from "fs";
import { join } from "path";

dotenv.config();
!process.env.BARDA_API_SERVICE_URL && console.log("默认连接http://localhost:3000,您可以使用以下启动命令指定地址：BARDA_API_SERVICE_URL=http://自定义地址:端口 yarn start");
const apiProxyTarget = process.env.BARDA_API_SERVICE_URL ?? "http://localhost:3000";
const nodeServiceApiProxyTarget = process.env.NODE_SERVICE_API_PROXY_TARGET;
const nodeEnv = process.env.NODE_ENV ?? "development";
const edition = process.env.REACT_APP_EDITION;
const isEEGlobal = edition === "enterprise-global";
const isEE = edition === "enterprise" || isEEGlobal;
const isDev = nodeEnv === "development";
const isVisualizerEnabled = !!process.env.ENABLE_VISUALIZER;
const browserCheckFileName = `browser-check-${process.env.REACT_APP_COMMIT_ID}.js`;
const base = ensureLastSlash(process.env.PUBLIC_URL);
// 检查是否在容器内
const inContainer = process.env["IN_CONTAINER"] || false;

if (!apiProxyTarget && isDev) {
  console.log();
  console.log(chalk.red`API_PROXY_TARGET is required.\n`);
  console.log(chalk.cyan`Start with command: API_PROXY_TARGET=\{backend-api-addr\} yarn start`);
  console.log();
  process.exit(1);
}

const proxyConfig: ServerOptions["proxy"] = {
  "/api": {
    target: apiProxyTarget,
    changeOrigin: false,
  },
};

if (nodeServiceApiProxyTarget) {
  proxyConfig["/node-service"] = {
    target: nodeServiceApiProxyTarget,
  };
}

const define = {};
buildVars.forEach(({ name, defaultValue }) => {
  define[name] = JSON.stringify(process.env[name] || defaultValue);
});

// 复制static文件夹的插件
const copyStaticPlugin = () => {
  return {
    name: 'copy-static',
    closeBundle() {
      const sourceDir = path.resolve(__dirname, 'static');
      const targetDir = path.resolve(__dirname, 'build/static');
      
      const copyDir = (src: string, dest: string) => {
        try {
          if (!statSync(src).isDirectory()) return;
          
          if (!statSync(dest, { throwIfNoEntry: false })?.isDirectory()) {
            mkdirSync(dest, { recursive: true });
          }
          
          const files = readdirSync(src);
          files.forEach(file => {
            const srcPath = join(src, file);
            const destPath = join(dest, file);
            
            if (statSync(srcPath).isDirectory()) {
              copyDir(srcPath, destPath);
            } else {
              copyFileSync(srcPath, destPath);
            }
          });
          console.log(`✅ 成功复制static文件夹到 ${targetDir}`);
        } catch (error) {
          console.warn(`❌ 复制static文件夹时出错: ${error.message}`);
        }
      };
      
      copyDir(sourceDir, targetDir);
    }
  };
};

// https://vitejs.dev/config/
export const viteConfig: UserConfig = {
  define,
  assetsInclude: ["**/*.md"],
  resolve: {
    extensions: [".mjs", ".js", ".ts", ".jsx", ".tsx", ".json"],
    alias: {
      "@barda": path.resolve(
        __dirname,
        isEE ? `../barda/src/${isEEGlobal ? "ee-global" : "ee"}` : "../barda/src"
      ),
    },
  },
  base,
  build: {
    manifest: true,
    target: "es2015",
    cssTarget: "chrome63",
    outDir: "build",
    assetsDir: "static",
    emptyOutDir: false,
    rollupOptions: {
      output: {
        chunkFileNames: "[name]-[hash].js",
      },
    },
    commonjsOptions: {
      defaultIsModuleExports: (id) => {
        if (id.indexOf("antd/lib") !== -1) {
          return false;
        }
        return "auto";
      },
    },
  },
  css: {
    preprocessorOptions: {
      less: {
        modifyVars: {
          "@primary-color": "#3377FF",
          "@link-color": "#3377FF",
          "@border-color-base": "#D7D9E0",
          "@border-radius-base": "4px",
        },
        javascriptEnabled: true,
      },
    },
  },
  server: {
    open: !inContainer,
    cors: true,
    port: 8000,
    host: "0.0.0.0",
    proxy: proxyConfig,
  },
  plugins: [
    checker({
      typescript: true,
      eslint: {
        lintCommand: 'eslint --quiet "./src/**/*.{ts,tsx}"',
        dev: {
          logLevel: ["error"],
        },
      },
    }),
    react({
      babel: {
        parserOpts: {
          plugins: ["decorators-legacy"],
        },
        plugins:
          isDev ? [['babel-plugin-styled-components', { displayName: true, fileName: true }]] : []
      },
    }),
    viteTsconfigPaths({
      projects: ["../barda/tsconfig.json", "../barda-design/tsconfig.json"],
    }),
    svgrPlugin({
      svgrOptions: {
        exportType: "named",
        prettier: false,
        svgo: false,
        titleProp: true,
        ref: true,
      },
    }),
    globalDepPlugin(),
    createHtmlPlugin({
      minify: true,
      inject: {
        data: {
          browserCheckScript: isDev ? "" : `<script src="${base}${browserCheckFileName}"></script>`,
        },
      },
    }),
    isVisualizerEnabled && visualizer({
      emitFile: false,
      filename: "visualizer.html",
    }),
    copyStaticPlugin(),
  ].filter(Boolean) as any,
};

const browserCheckConfig: UserConfig = {
  ...viteConfig,
  define: {
    ...viteConfig.define,
    "process.env.NODE_ENV": JSON.stringify("production"),
  },
  build: {
    ...viteConfig.build,
    manifest: false,
    copyPublicDir: false,
    emptyOutDir: true,
    lib: {
      formats: ["iife"],
      name: "BrowserCheck",
      entry: "./src/browser-check.ts",
      fileName: () => {
        return browserCheckFileName;
      },
    },
  },
};

const buildTargets = {
  main: viteConfig,
  browserCheck: browserCheckConfig,
};

const buildTarget = buildTargets[process.env.BUILD_TARGET || "main"];

export default defineConfig(buildTarget || viteConfig);
