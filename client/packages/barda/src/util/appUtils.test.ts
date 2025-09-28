import { clearGlobalSettings } from "@barda/index.sdk";
import { handlinghooks, keyValueListToSearchStr, recordToSearchStr } from "./appUtils";
import { AppSummaryInfo } from "@barda/redux/reduxActions/applicationActions";
import { KeyValue } from "@barda/types/common";

const dsl1: AppSummaryInfo = {
    dsl: {
        hooks: [
            { name: "moment", compType: "momentLib" },
            { name: "dayjs", compType: "dayJsLib" },
        ],

    },
    id: "",
    appType: 1,
}

const dsl2: AppSummaryInfo = {
    dsl: {
        hooks: [
            { name: "moment", compType: "momentLib" },
        ],

    },
    id: "",
    appType: 1,
}

const dsl3: AppSummaryInfo = {
    dsl: {
        hooks: [
            { name: "dayjs", compType: "dayJsLib" },
        ]
    },
    id: "",
    appType: 1,
}

describe("测试AppSummaryInfo函数", () => {
    test("测试删除moment", () => {
        expect(handlinghooks(dsl1)).toEqual(dsl3);
    });

    test("测试修改moment为dayjs", () => {
        expect(handlinghooks(dsl2)).toEqual(dsl3);
    });

});

describe("测试keyValueListToSearchStr函数", () => {
    test("测试多个属性转换为URL", () => {
        const kvs: KeyValue[] = [{ key: "testKey", value: "testValue" }, { key: "testKey2", value: "testValue2" }];
        const expectedSearchStr = "testKey=testValue&testKey2=testValue2";
        expect(keyValueListToSearchStr(kvs)).toBe(expectedSearchStr);
    });

    test("测试空数组转URL", () => {
        const kvs: KeyValue[] = [];
        const expectedSearchStr = "";
        expect(keyValueListToSearchStr(kvs)).toBe(expectedSearchStr);
    });

    test("测试畸形数据转URL", () => {
        const kvs: KeyValue[] = [
            { key: "testKey1", value: "testValue1" },
            { key: "testKey2", value: "testValue2" },
            { key: "", value: "testValue3" },
        ];
        const expectedSearchStr = "testKey1=testValue1&testKey2=testValue2";
        expect(keyValueListToSearchStr(kvs)).toBe(expectedSearchStr);
    });

})

describe('测试recordToSearchStr 函数', () => {
    it('能够将参数对象正确转换为搜索字符串', () => {
        const params = {
            key1: 'value1',
            key2: 'value2',
            key3: 'value3'
        };
        const result = recordToSearchStr(params);
        expect(result).toEqual('key1=value1&key2=value2&key3=value3');
    });

    it('当参数对象为空时，返回空字符串', () => {
        const params = {};
        const result = recordToSearchStr(params);
        expect(result).toEqual('');
    });

    it('能够处理特殊字符', () => {
        const params = {
            key1: 'value with spa+ces',
            key2: 'value&with=special@characters'
        };
        const result = recordToSearchStr(params);
        expect(result).toEqual('key1=value%20with%20spa%2Bces&key2=value%26with%3Dspecial%40characters');
    });
});