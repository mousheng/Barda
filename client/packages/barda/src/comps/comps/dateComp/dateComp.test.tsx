import dayjs from 'dayjs';
import { validate } from './dateComp';
import { trans } from "i18n";

const DATE_TIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';

describe('validate', () => {
    it('测试 成功', () => {
        const props = {
            value: { start: '2023-08-10 12:00:00' },
            required: true,
        };
        const result = validate(props as any);
        expect(result).toEqual({ validateStatus: 'success' });
    });

    it('测试必填 失败', () => {
        const props = {
            value: { start: 'invalid-date' },
            required: true,
        };
        const result = validate(props as any);
        expect(result).toEqual({ validateStatus: 'error', help: trans("prop.required") });
    });

    it('测试 未到最小日期', () => {
        const props = {
            value: { start: '2023-08-10 12:00:00' },
            minDate: '2023-08-11 00:00:00',
        };
        const result = validate(props as any);
        console.log(result);
        expect(result).toEqual({ validateStatus: 'error', help: trans("prop.minDateTip") });
    });

    it('超过最大日期 失败', () => {
        const props = {
            value: { start: '2023-08-10 12:00:00', end: '2023-08-12 12:00:00', range: true },
            maxDate: '2023-08-11 00:00:00',
        };
        const result = validate(props as any);
        expect(result).toEqual({ validateStatus: 'error', help: trans('prop.maxDateTip') });
    });

    it('未到最小时间 失败', () => {
        const props = {
            value: { start: '2023-08-10 12:00:00', showTime: true },
            minTime: '14:00:00',
        };
        const result = validate(props as any);
        expect(result).toEqual({ validateStatus: 'error', help: trans('prop.minTimeTip') });
    });

    it('超过最大时间 失败', () => {
        const props = {
            value: { start: '2023-08-10 12:00:00', showTime: true },
            maxTime: '10:00:00',
        };
        const result = validate(props as any);
        expect(result).toEqual({ validateStatus: 'error', help: trans('prop.maxTimeTip') });
    });


    it('测试缺失结束时间', () => {
        const props = {
            value: { start: '2023-08-10 12:00:00', range: true },
            required: true,
        };
        const result = validate(props as any);
        expect(result).toEqual({ validateStatus: 'error', help: trans('prop.required') });
    });

    it('测试成功1', () => {
        const props = {
            value: { start: '2023-08-10 12:00:00', end: '2023-08-12 12:00:00', range: true },
            maxDate: '2023-08-13 00:00:00',
        };
        const result = validate(props as any);
        expect(result).toEqual({ validateStatus: 'success' });
    });

    it('测试成功2', () => {
        const props = {
            value: { start: '2023-08-10 12:00:00', end: '2023-08-10 13:00:00', range: true, showTime: true },
            minTime: '10:00:00',
            maxTime: '14:00:00',
        };
        const result = validate(props as any);
        expect(result).toEqual({ validateStatus: 'success' });
    });
});
