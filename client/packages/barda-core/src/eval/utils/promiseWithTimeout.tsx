/**
 * 该函数返回一个新的 Promise，该 Promise 在原始 Promise 成功解析或在指定超时时间后解析为默认值。
 *
 * @param promise - 要进行超时处理的原始 Promise。
 * @param timeout - 超时时间（以毫秒为单位）。
 * @param defaultValue - 超时后返回的默认值。
 * @param timeoutMessage - 超时时返回的错误消息（可选，默认为 "timeout"）。
 * @returns 一个新的 Promise，该 Promise 在原始 Promise 成功解析或在指定超时时间后解析为默认值。
 */
export function promiseWithDefaultOnTimeout<T>(promise: Promise<T>, timeout: number, defaultValue: T, timeoutMessage: string = "timeout"): Promise<T> {
    // 用于存储 setTimeout 的返回值，以便在 finally 块中清除计时器。
    let timer: NodeJS.Timeout;

    // 创建一个新的 Promise，在超时时解析为 defaultValue。
    const timeoutPromise = new Promise<T>((resolve) => {
        timer = setTimeout(() => resolve(defaultValue), timeout);
    });

    // 使用 Promise.race 并行运行 timeoutPromise 和原始 promise，并在 finally 块中清除计时器。
    return Promise.race([timeoutPromise, promise])
        .finally(() => clearTimeout(timer));
}

/**
 * 该函数返回一个新的 Promise，该 Promise 在原始 Promise 成功解析或在指定超时时间后拒绝。
 *
 * @param promise - 要进行超时处理的原始 Promise。
 * @param timeout - 超时时间（以毫秒为单位）。
 * @param timeoutMessage - 超时时返回的错误消息（可选，默认为 "timeout"）。
 * @returns 一个新的 Promise，该 Promise 在原始 Promise 成功解析或在指定超时时间后拒绝。
 */
export function promiseWithTimeout<T>(promise: Promise<T>, timeout: number, timeoutMessage: string = "timeout") {
    // 用于存储 setTimeout 的返回值，以便在 finally 块中清除计时器。
    let timer: NodeJS.Timeout;

    // 创建一个新的 Promise，在超时时拒绝并返回 timeoutMessage。
    const timeoutPromise = new Promise((_, reject) => {
        timer = setTimeout(() => reject(new Error(timeoutMessage)), timeout);
    });

    // 使用 Promise.race 并行运行 timeoutPromise 和原始 promise，并在 finally 块中清除计时器。
    return Promise.race([timeoutPromise, promise])
        .finally(() => clearTimeout(timer));
}