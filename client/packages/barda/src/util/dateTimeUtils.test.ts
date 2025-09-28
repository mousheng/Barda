import dayjs from "dayjs";
import { formatTimestamp, timestampToHumanReadable, fromStringSafeGetDayjs } from "./dateTimeUtils";
import "dayjs/locale/zh-cn";
dayjs.locale("zh-cn");

describe("测试formatTimestamp函数", () => {
    test("测试大整数", () => {
        const largeTimestamp = 2147483647000;
        const expectedFormattedDate = "2038-01-19 11:14";

        expect(formatTimestamp(largeTimestamp)).toBe(expectedFormattedDate);
    });

    test("测试十秒之后的日期", () => {
        const futureTimestamp = Date.now() + 10000;
        const expectedFormattedDate = dayjs(futureTimestamp).format("YYYY-MM-DD HH:mm");

        expect(formatTimestamp(futureTimestamp)).toBe(expectedFormattedDate);
    });

    test("测试十秒之前的日期", () => {
        const pastTimestamp = Date.now() - 10000;
        const expectedFormattedDate = dayjs(pastTimestamp).format("YYYY-MM-DD HH:mm");
        expect(formatTimestamp(pastTimestamp)).toBe(expectedFormattedDate);
    });

    test("测试输入为0", () => {
        const zeroTimestamp = 0;
        const expectedFormattedDate = "1970-01-01 08:00";

        expect(formatTimestamp(zeroTimestamp)).toBe(expectedFormattedDate);
    });

    test("正确格式化负数", () => {
        const negativeTimestamp = -10000;
        const expectedFormattedDate = dayjs(negativeTimestamp).format("YYYY-MM-DD HH:mm");

        expect(formatTimestamp(negativeTimestamp)).toBe(expectedFormattedDate);
    });
});

describe("测试timestampToHumanReadable函数", () => {
    test("测试错误输入", () => {
        expect(timestampToHumanReadable(undefined)).toBe("");
    });

    test("几秒内", () => {
        const recentTimestamp = Date.now() + 44000;
        const expectedResult = "几秒内";
        expect(timestampToHumanReadable(recentTimestamp)).toContain(expectedResult);
    });

    test("分钟内", () => {
        const recentTimestamp = Date.now() + 44 * 60 * 1000;
        const expectedResult = "44 分钟内";
        expect(timestampToHumanReadable(recentTimestamp)).toContain(expectedResult);
    });

    test("一小时内", () => {
        const recentTimestamp = Date.now() + 89 * 60 * 1000;
        const expectedResult = "1 小时内";
        expect(timestampToHumanReadable(recentTimestamp)).toContain(expectedResult);
    });

});

describe("测试安全获取dayjs函数", () => {
    test("should return null when input is an empty string", () => {
        const result = fromStringSafeGetDayjs("");
        expect(result.isValid()).toBeFalsy();
        expect(result.toString()).toBe("Invalid Date")
      });

      test("测试是否正确返回日期字符串", () => {
        const result = fromStringSafeGetDayjs("2022-01-01", "YYYY/MM/DD");
        expect(result.isValid()).toBeTruthy();
        expect(result.format("YYYY-MM-DD")).toBe("2022-01-01");
      });
    
})