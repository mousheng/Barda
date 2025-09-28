import { RecordConstructorToView } from "barda-core";
import {
  BoolControl,
  eventHandlerControl,
  hiddenPropertyView,
  NameConfig,
  NameConfigHidden,
  Section,
  sectionNames,
  stringExposingStateControl,
  StringStateControl,
  UICompBuilder,
  withDefault,
  withExposingConfigs,
} from "barda-sdk";
import { trans } from "i18n/comps";
import { keys, pickBy } from "lodash";
import { useRef } from "react";
import { useResizeDetector } from "react-resize-detector";
import { ImageEditor, localeData } from "./imageEditorClass";
import { Container, customTheme, EmbeddedButton, saveEvent } from "./imageEditorConstants";

const childrenMap = {
  src: withDefault(StringStateControl, trans("imageEditor.defaultSrc")),
  name: withDefault(StringStateControl, "Example"),
  crop: withDefault(BoolControl, true),
  flip: withDefault(BoolControl, true),
  rotate: withDefault(BoolControl, true),
  draw: withDefault(BoolControl, true),
  shape: withDefault(BoolControl, true),
  icon: withDefault(BoolControl, true),
  text: withDefault(BoolControl, true),
  mask: withDefault(BoolControl, true),
  filter: withDefault(BoolControl, true),
  dataURI: stringExposingStateControl("dataURI"),
  data: stringExposingStateControl("data"),
  onEvent: eventHandlerControl([saveEvent] as const),
  buttonText: withDefault(StringStateControl, trans("imageEditor.save")),
};

const ContainerImageEditor = (props: RecordConstructorToView<typeof childrenMap>) => {
  const editorRef = useRef<any>(null);

  let filteredMenu = keys(pickBy({
    crop: props.crop, flip: props.flip, rotate: props.rotate, draw: props.draw, shape: props.shape, icon: props.icon, text: props.text, mask: props.mask, filter: props.filter
  }))

  const saveImage = () => {
    let imageEditorInst = editorRef.current.imageEditorInst;
    let dataURL = imageEditorInst.toDataURL();
    props.dataURI.onChange(dataURL);
    props.data.onChange(dataURL.split(",")[1]);
  };

  const { width, height, ref } = useResizeDetector({
    refreshMode: 'throttle',
    refreshRate: 50,
  });

  return (
    <Container ref={ref}>
      <EmbeddedButton
        type="primary"
        onClick={() => {
          saveImage();
          props.onEvent("save");
        }}
      >
        {props.buttonText.value}
      </EmbeddedButton>
      <div style={{ width: "100%", height: "100%" }}>
        <ImageEditor
          ref={editorRef}
          includeUI={{
            loadImage: {
              path: props.src.value,
              name: props.name.value,
            },
            menu: filteredMenu,
            theme: customTheme,
            uiSize: {
              width,
              height,
            },
            menuBarPosition: "bottom",
            locale: localeData,
          }}
          cssMaxWidth={document.documentElement.clientWidth}
          cssMaxHeight={document.documentElement.clientHeight}
          selectionStyle={{
            cornerSize: 50,
            rotatingPointOffset: 100,
          }}
          usageStatistics={false}
        />
      </div>
    </Container>
  );
};
let ImageEditorBasicComp = (function () {
  return new UICompBuilder(childrenMap, (props) => {
    return <ContainerImageEditor {...props} />;
  })
    .setPropertyViewFn((children) => {
      return (
        <>
          <Section name={sectionNames.basic}>
            {children.src.propertyView({
              label: trans("imageEditor.src"),
              placeholder: "http://xxx.jpg",
            })}
            {children.name.propertyView({
              label: trans("imageEditor.name"),
            })}
            {children.buttonText.propertyView({
              label: trans("imageEditor.buttonText"),
            })}
          </Section>
          <Section name={sectionNames.interaction}>{children.onEvent.getPropertyView()}</Section>
          <Section name={sectionNames.advanced}>
            {children.crop.propertyView({
              label: trans("imageEditor.Crop"),
            })}
            {children.flip.propertyView({
              label: trans("imageEditor.Flip"),
            })}
            {children.rotate.propertyView({
              label: trans("imageEditor.Rotate"),
            })}
            {children.draw.propertyView({
              label: trans("imageEditor.Draw"),
            })}
            {children.shape.propertyView({
              label: trans("imageEditor.Shape"),
            })}
            {children.icon.propertyView({
              label: trans("imageEditor.Icon"),
            })}
            {children.text.propertyView({
              label: trans("imageEditor.Text"),
            })}
            {children.mask.propertyView({
              label: trans("imageEditor.Mask"),
            })}
            {children.filter.propertyView({
              label: trans("imageEditor.Filter"),
            })}
          </Section>
          <Section name={sectionNames.layout}>{hiddenPropertyView(children)}</Section>
        </>
      );
    })
    .build();
})();

ImageEditorBasicComp = class extends ImageEditorBasicComp {
  override autoHeight(): boolean {
    return false;
  }
};

export const ImageEditorComp = withExposingConfigs(ImageEditorBasicComp, [
  new NameConfig("src", trans("imageEditor.srcDesc")),
  new NameConfig("name", trans("imageEditor.nameDesc")),
  new NameConfig("dataURI", trans("imageEditor.dataURIDesc")),
  new NameConfig("data", trans("imageEditor.dataDesc")),
  new NameConfig("buttonText", trans("imageEditor.buttonTextDesc")),
  NameConfigHidden,
]);
