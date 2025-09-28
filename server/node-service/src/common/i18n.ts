import { Translator as CoreTranslator } from "barda-core";

export class I18n<Messages extends object> extends CoreTranslator<Messages> {
  constructor(fileData: object, locales?: string[]) {
    super(fileData, "", locales);
  }
}
