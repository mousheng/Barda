import styled, { css } from "styled-components";
import { Section, sectionNames } from "barda-design";
import { clickEvent, eventHandlerControl } from "../controls/eventHandlerControl";
import { StringStateControl } from "../controls/codeStateControl";
import { UICompBuilder, withDefault } from "../generators";
import { NameConfig, NameConfigHidden, withExposingConfigs } from "../generators/withExposing";
import { RecordConstructorToView } from "barda-core";
import { useEffect } from "react";
import _ from "lodash";
import { useResizeDetector } from "react-resize-detector";
import { styleControl } from "comps/controls/styleControl";
import { ImageStyle, ImageStyleType } from "comps/controls/styleControlConstants";
import { hiddenPropertyView } from "comps/utils/propertyUtils";
import { trans } from "i18n";
import { AutoHeightControl } from "comps/controls/autoHeightControl";
import { BoolControl } from "comps/controls/boolControl";
import { Image as AntImage } from "antd";
import { DEFAULT_IMG_URL } from "util/stringUtils";
import { dropdownControl } from "@barda/index.sdk";

const Container = styled.div<{ $style: ImageStyleType | undefined, $objectFit: string }>`
  height: 100%;
  width: 100%;
  
  .ant-image {
    display: flex;
  }
  
  .ant-image,
  img {
    width: 100%;
    height: 100%;
  }

  img {
    object-fit: ${props => props.$objectFit ?? "contain"};
    pointer-events: auto;
  }

  ${(props) => props.$style && getStyle(props.$style)}
`;

const getStyle = ($style: ImageStyleType) => {
  return css`
    img {
      ${$style.border !== "#00000000" ? `border: 1px solid ${$style.border}` : ""};
      border-radius: ${$style.radius};
    }

    .ant-image-mask {
      border-radius: ${$style.radius};
    }
  `;
};

const EventOptions = [clickEvent] as const;

const objectFitOptions = [
  { label: trans("image.contain"), value: "contain" },
  { label: trans("image.cover"), value: "cover" },
  { label: trans("image.fill"), value: "fill" },
  { label: trans("image.none"), value: "none" },
  { label: trans("image.scaleDown"), value: "scale-down" },
];

const ContainerImg = (props: RecordConstructorToView<typeof childrenMap>) => {

  const { width: imgWidth, height: imgHeight, ref } = useResizeDetector({
    refreshMode: 'throttle',
    refreshRate: 50,
  });

  useEffect(() => {
    const newImage = new Image(0, 0);
    newImage.src = props.src.value;
    newImage.onerror = function (e) {
      newImage.src = DEFAULT_IMG_URL;
    };
  }, [props.src.value]);

  return (
    <Container
      ref={ref}
      $objectFit={props.objectFit}
      $style={{
        ...props.style,
        ...(props.autoHeight ? { width: imgWidth, height: imgHeight } : {})
      }}>
      <AntImage
        src={props.src.value}
        referrerPolicy="same-origin"
        draggable={false}
        preview={props.supportPreview}
        fallback={DEFAULT_IMG_URL}
        onClick={() => props.onEvent("click")}
      />
    </Container>
  );
};

const childrenMap = {
  src: withDefault(StringStateControl, "https://temp.im/350x400"),
  onEvent: eventHandlerControl(EventOptions),
  style: styleControl(ImageStyle),
  autoHeight: withDefault(AutoHeightControl, "fixed"),
  supportPreview: BoolControl,
  objectFit: dropdownControl(objectFitOptions, "contain")
};

let ImageBasicComp = new UICompBuilder(childrenMap, (props) => {
  return <ContainerImg {...props} />;
})
  .setPropertyViewFn((children) => {
    return (
      <>
        <Section name={sectionNames.basic}>
          {children.src.propertyView({
            label: trans("image.src"),
          })}
          {children.supportPreview.propertyView({
            label: trans("image.supportPreview"),
            tooltip: trans("image.supportPreviewTip"),
          })}
        </Section>

        <Section name={sectionNames.interaction}>{children.onEvent.getPropertyView()}</Section>

        <Section name={sectionNames.layout}>
          {children.autoHeight.getPropertyView()}
          {hiddenPropertyView(children)}
          {children.objectFit.propertyView({
            label: trans("image.objectFit"),
            tooltip: trans("image.objectFitTip"),
          })}
        </Section>

        <Section name={sectionNames.style}>{children.style.getPropertyView()}</Section>
      </>
    );
  })
  .build();

ImageBasicComp = class extends ImageBasicComp {
  override autoHeight(): boolean {
    return this.children.autoHeight.getView();
  }
};

export const ImageComp = withExposingConfigs(ImageBasicComp, [
  new NameConfig("src", trans("image.srcDesc")),
  NameConfigHidden,
]);
