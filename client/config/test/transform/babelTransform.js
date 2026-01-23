import babelJest from "babel-jest";

export default babelJest.createTransformer({
  presets: [
    [
      "babel-preset-react-app",
      {
        runtime: "automatic",
      },
    ],
  ],
  plugins: ["babel-plugin-transform-import-meta"],
  babelrc: false,
  configFile: false,
});
