import { trans } from "i18n/comps";
import React from "react";
import TuiImageEditor from "tui-image-editor";

export const localeData = {
  "Crop": trans("imageEditor.Crop"),
  "Flip": trans("imageEditor.Flip"),
  "Rotate": trans("imageEditor.Rotate"),
  "Draw": trans("imageEditor.Draw"),
  "Shape": trans("imageEditor.Shape"),
  "Icon": trans("imageEditor.Icon"),
  "Text": trans("imageEditor.Text"),
  "Mask": trans("imageEditor.Mask"),
  "Filter": trans("imageEditor.Filter"),
  "Resize": trans("imageEditor.Resize"),
  "ZoomIn": trans("imageEditor.ZoomIn"),
  "ZoomOut": trans("imageEditor.ZoomOut"),
  "History": trans("imageEditor.History"),
  "Undo": trans("imageEditor.Undo"),
  "Redo": trans("imageEditor.Redo"),
  "Reset": trans("imageEditor.Reset"),
  "Delete": trans("imageEditor.Delete"),
  "DeleteAll": trans("imageEditor.DeleteAll"),
  "Apply": trans("imageEditor.Apply"),
  "Cancel": trans("imageEditor.Cancel"),
  "Custom": trans("imageEditor.Custom"),
  "Square": trans("imageEditor.Square"),
  "Flip X": trans("imageEditor.Flip X"),
  "Flip Y": trans("imageEditor.Flip Y"),
  "Range": trans("imageEditor.Range"),
  "Free": trans("imageEditor.Free"),
  "Color": trans("imageEditor.Color"),
  "Straight": trans("imageEditor.Straight"),
  "Rectangle": trans("imageEditor.Rectangle"),
  "Circle": trans("imageEditor.Circle"),
  "Triangle": trans("imageEditor.Triangle"),
  "Fill": trans("imageEditor.Fill"),
  "Stroke": trans("imageEditor.Stroke"),
  "Arrow": trans("imageEditor.Arrow"),
  "Arrow-2": trans("imageEditor.Arrow-2"),
  "Arrow-3": trans("imageEditor.Arrow-3"),
  "Star-1": trans("imageEditor.Star-1"),
  "Star-2": trans("imageEditor.Star-2"),
  "Polygon": trans("imageEditor.Polygon"),
  "Location": trans("imageEditor.Location"),
  "Heart": trans("imageEditor.Heart"),
  "Bubble": trans("imageEditor.Bubble"),
  "Custom icon": trans("imageEditor.Custom icon"),
  "Bold": trans("imageEditor.Bold"),
  "Italic": trans("imageEditor.Italic"),
  "Underline": trans("imageEditor.Underline"),
  "Left": trans("imageEditor.Left"),
  "Center": trans("imageEditor.Center"),
  "Right": trans("imageEditor.Right"),
  "Load Mask Image": trans("imageEditor.Load Mask Image"),
  "Grayscale": trans("imageEditor.Grayscale"),
  "Invert": trans("imageEditor.Invert"),
  "Sepia": trans("imageEditor.Sepia"),
  "Sepia2": trans("imageEditor.Sepia2"),
  "Blur": trans("imageEditor.Blur"),
  "Sharpen": trans("imageEditor.Sharpen"),
  "Emboss": trans("imageEditor.Emboss"),
  "Remove White": trans("imageEditor.Remove White"),
  "Distance": trans("imageEditor.Distance"),
  "Brightness": trans("imageEditor.Brightness"),
  "Noise": trans("imageEditor.Noise"),
  "Pixelate": trans("imageEditor.Pixelate"),
  "Color Filter": trans("imageEditor.Color Filter"),
  "Threshold": trans("imageEditor.Threshold"),
  "Tint": trans("imageEditor.Tint"),
  "Multiply": trans("imageEditor.Multiply"),
  "Blend": trans("imageEditor.Blend")
}

export class ImageEditor extends React.Component<any> {
  rootEl = React.createRef<any>();

  imageEditorInst: TuiImageEditor | undefined;
  props: any;
  constructor(props: any) {
    super(props);
    this.props = props;
  }
  componentDidMount() {
    if (this.rootEl.current !== null) {
      this.imageEditorInst = new TuiImageEditor(this.rootEl.current, {
        ...(this.props as any),
      });
    }
  }

  componentWillUnmount() {
    if (this.imageEditorInst !== undefined) {
      this.imageEditorInst.destroy();

      this.imageEditorInst = undefined;
    }
  }

  shouldComponentUpdate(nextProps: any) {
    if (
      JSON.stringify([this.props.includeUI.menu, this.props.includeUI.loadImage]) !==
      JSON.stringify([nextProps.includeUI.menu, nextProps.includeUI.loadImage])
    ) {
      this.imageEditorInst = new TuiImageEditor(this.rootEl.current as any, {
        ...nextProps,
      });
    }
    return false;
  }

  getInstance() {
    return this.imageEditorInst;
  }

  getRootElement() {
    return this.rootEl.current;
  }

  render() {
    return <div ref={this.rootEl} />;
  }
}
