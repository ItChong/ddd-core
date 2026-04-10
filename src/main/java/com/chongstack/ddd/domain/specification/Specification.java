package com.chongstack.ddd.domain.specification;

/**
 * 规约模式接口。
 * <p>
 * 用于封装可复用的业务规则判断逻辑，
 * 支持通过 and/or/not 进行组合。
 * <p>
 * 示例：
 * <pre>
 * Specification&lt;Order&gt; isPaid = order -> order.getStatus() == OrderStatus.PAID;
 * Specification&lt;Order&gt; isRecent = order -> order.getCreatedAt().isAfter(cutoff);
 * Specification&lt;Order&gt; paidAndRecent = isPaid.and(isRecent);
 * </pre>
 *
 * @param <T> 被校验的对象类型
 */
@FunctionalInterface
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);

    default Specification<T> and(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }

    default Specification<T> or(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    default Specification<T> not() {
        return candidate -> !this.isSatisfiedBy(candidate);
    }
}
