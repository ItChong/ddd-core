package com.chongstack.ddd.domain.model;

import com.chongstack.ddd.domain.exception.DomainException;

import java.util.Collection;

/**
 * 领域不变量守护工具。
 * <p>
 * 在领域对象的行为方法中使用，实现 fail-fast 的不变量检查。
 * 替代大量的 if-throw 样板代码，让领域规则表达更加清晰。
 * <p>
 * 使用示例：
 * <pre>
 * public void pay(Money amount) {
 *     Guard.notNull(amount, "支付金额不能为空");
 *     Guard.isTrue(this.status == OrderStatus.CREATED, "只有待支付的订单才能付款");
 *     Guard.isTrue(amount.isPositive(), "支付金额必须大于零");
 *     // 执行业务逻辑...
 * }
 * </pre>
 */
public final class Guard {

    private Guard() {
    }

    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new DomainException(message);
        }
    }

    public static void notEmpty(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new DomainException(message);
        }
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new DomainException(message);
        }
    }

    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new DomainException(message);
        }
    }

    public static <T extends Comparable<T>> void greaterThan(T value, T threshold, String message) {
        notNull(value, message);
        if (value.compareTo(threshold) <= 0) {
            throw new DomainException(message);
        }
    }

    public static <T extends Comparable<T>> void greaterThanOrEqual(T value, T threshold, String message) {
        notNull(value, message);
        if (value.compareTo(threshold) < 0) {
            throw new DomainException(message);
        }
    }
}
