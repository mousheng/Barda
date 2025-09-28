package com.barda.infra.conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.barda.infra.conditional.ConditionalOnPropertyNotBlank.OnPropertyNotBlankCondition;

/**
 * 当指定的属性值不为空时生效的条件注解。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnPropertyNotBlankCondition.class)
public @interface ConditionalOnPropertyNotBlank {

    /**
     * 要检查的属性名列表。
     */
    String[] value() default {};

    /**
     * 属性名的前缀。
     */
    String prefix() default "";

    /**
     * 内部类，用于定义条件逻辑。
     */
    class OnPropertyNotBlankCondition implements Condition {

        /**
         * 检查属性值是否不为空。
         *
         * @param context  条件上下文。
         * @param metadata 注解元数据。
         * @return 如果所有指定属性值都不为空，则返回 true，否则返回 false。
         */
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Map<String, Object> attrs = metadata.getAnnotationAttributes(ConditionalOnPropertyNotBlank.class.getName());
            if (attrs == null) {
                return false;
            }
            String[] properties = (String[]) attrs.get("value");
            String prefix = (String) attrs.get("prefix");
            for (String property : properties) {
                if (StringUtils.isBlank(property)) {
                    return false;
                }
                String value = context.getEnvironment().getProperty(concat(prefix, property));
                if (StringUtils.isBlank(value)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 拼接属性名和前缀。
         *
         * @param prefix   属性名的前缀。
         * @param property 属性名。
         * @return 拼接后的属性名。
         */
        private String concat(@Nullable String prefix, @NotBlank String property) {
            if (StringUtils.isBlank(prefix)) {
                return property;
            }
            return prefix + "." + property;
        }
    }
}
