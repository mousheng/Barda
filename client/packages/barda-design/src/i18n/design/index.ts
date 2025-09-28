import { Translator } from "barda-core";
import * as localeData from "./locales";

export const { trans } = new Translator<typeof localeData.en>(localeData, REACT_APP_LANGUAGES);
