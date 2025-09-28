package com.barda.infra.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 用于标记返回非空 Mono 对象的自定义注解。
 *
 * 该注解可用于方法上，以指示返回的 Mono 对象不应为空。
 * 它可以用作方法参数的验证或在代码中执行其他检查。
 *
 * 示例用法：
 *
 * <pre>
 * &#64;NonEmptyMono
 * public Mono&lt;User&gt; getUserById(String userId) {
 *     // 实现代码
 * }
 * </pre>
 *
 * 在上面的示例中，getUserById 方法返回的 Mono 对象不应为空。
 * 如果返回的 Mono 对象为空，则会引发运行时异常。
 *
 */
@Target(ElementType.METHOD)
public @interface NonEmptyMono {
}
