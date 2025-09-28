/**
 * Input/TextArea 的默认防抖处理
 */
export const INPUT_DEFAULT_ONCHANGE_DEBOUNCE = 100;

/**
 * 启用动作的优先级
 */
export const ENABLE_ACTION_PRIORITY = true;

/**
 *  * 清除动作队列的超时时间，仅在启用动作优先级
 */
export const CLEAR_ACTION_QUEUE_TIMEOUT = 500;

/**
 *  * 如果在该时间间隔内没有分发任何动作，则认为应用程序处于CalmDown状态
 */
export const CALM_DOWN_TIMEOUT = 3000;
