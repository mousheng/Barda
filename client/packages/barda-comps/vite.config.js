import config from "barda-cli/config/vite.config";
import viteTsconfigPaths from "vite-tsconfig-paths";
export default {
  ...config,
  plugins: [...config.plugins, viteTsconfigPaths()],
  server: {
    // open: !(process.env["IN_CONTAINER"] || false),
    open: !(process.env["IN_CONTAINER"] || false),
    port: 9000,
    host: "0.0.0.0",
  },
};
