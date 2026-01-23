import { promiseWithDefaultOnTimeout, promiseWithTimeout } from './promiseWithTimeout';

describe('promiseWithDefaultOnTimeout', () => {
    it('测试返回默认值', async () => {
        const timeout = 1000;
        const expectedResult = {
            code: '0'
        };
        const timeoutResult = {
            code: 1
        };

        const promise = new Promise<object>((resolve) => {
            setTimeout(() => resolve(expectedResult), timeout);
        });

        const Result1 = await promiseWithDefaultOnTimeout(promise, timeout - 10, timeoutResult);
        const Result2 = await promiseWithDefaultOnTimeout(promise, timeout + 10, timeoutResult);

        expect(Result1).toBe(timeoutResult);
        expect(Result2).toBe(expectedResult);
    });

    it('测试超时抛出异常', async () => {
        const mockPromise = new Promise((resolve) => {
            setTimeout(resolve, 1000);
        });

        const mockPromise2 = new Promise((resolve) => {
            setTimeout(resolve, 1000);
        });

        const timeoutMessage = "超时了";

        await expect(promiseWithTimeout(mockPromise, 990, timeoutMessage)).rejects.toThrow(timeoutMessage);
    });

});