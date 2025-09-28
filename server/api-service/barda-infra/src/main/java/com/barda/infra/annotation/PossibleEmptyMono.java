package com.barda.infra.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 用于标记返回可能为空的 Mono 对象的自定义注解。
 *
 * 该注解可用于方法上，以指示返回的 Mono 对象可能为空。
 * 它可以用作方法参数的验证或在代码中执行其他检查。
 *
 * 示例用法：
 *
 * <pre>
 * &#64;PossibleEmptyMono
 * public Mono&lt;User&gt; getUserById(String userId) {
 *     // 实现代码
 * }
 * </pre>
 *
 * 在上面的示例中，getUserById 方法返回的 Mono 对象可能为空。
 * 如果返回的 Mono 对象为空，则需要在使用该对象的代码中进行显式检查。
 *
 */
@Target(ElementType.METHOD)
public @interface PossibleEmptyMono {
}