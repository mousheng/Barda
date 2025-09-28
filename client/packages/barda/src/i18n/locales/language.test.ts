import { en } from './en';
import { zh } from './zh';
import { get, set } from "lodash";
import { en as en_d } from "../../../../barda-design/src/i18n/design/locales/en"
import { zh as zh_d } from "../../../../barda-design/src/i18n/design/locales/zh"
import { en as en_comps } from "../../../../barda-comps/src/i18n/comps/locales/en"
import { zh as zh_comps } from "../../../../barda-comps/src/i18n/comps/locales/zh"

function CompareLanguageFile(obj1: any, obj2: any, path = '', result = {}, depth = 2) {
    if (depth === 0) return result
    for (let key in obj1) {
        let currentPath = path ? `${path}.${key}` : key;
        if (typeof obj1[key] === 'object') {
            CompareLanguageFile(obj1[key], get(obj2, key), currentPath, result, depth - 1);
        }
        else if (get(obj2, key, null) === null) {
            set(result, currentPath, get(obj1, key));
        }
    }
    return result;
}

test("测试中文语言文件是否缺失键值", () => {
    const result = CompareLanguageFile(en, zh)
    expect(result).toEqual({});
})

test("测试 中文语言文件中多余的键值", () => {
    const result = CompareLanguageFile(zh, en)
    expect(result).toEqual({});
})

test("测试Barda-design模块 中文语言文件是否缺失键值", () => {
    const result = CompareLanguageFile(en_d, zh_d)
    expect(result).toEqual({});
})

test("测试Barda-design模块 中文语言文件中多余的键值", () => {
    const result = CompareLanguageFile(zh_d, en_d)
    expect(result).toEqual({});
})

test("测试Barda-Comps模块 中文语言文件是否缺失键值", () => {
    const result = CompareLanguageFile(en_comps, zh_comps)
    expect(result).toEqual({});
})

test("测试Barda-Comps模块 中文语言文件中多余的键值", () => {
    const result = CompareLanguageFile(zh_comps, en_comps)
    expect(result).toEqual({});
})