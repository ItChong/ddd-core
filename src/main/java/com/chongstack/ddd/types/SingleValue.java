package com.chongstack.ddd.types;

import com.chongstack.ddd.domain.model.Identifier;

import java.util.Objects;

/**
 * 单值包装基类，用于创建强类型的 ID 或值对象。
 * <p>
 * 避免在方法签名中使用裸类型（如 Long、String），
 * 防止参数传递错误。
 * <p>
 * 使用示例：
 * <pre>
 * public class OrderId extends SingleValue&lt;Long&gt; implements Identifier {
 *     public OrderId(Long value) { super(value); }
 * }
 *
 * // 或者直接用 Java Record（推荐）：
 * public record OrderId(Long value) implements Identifier {}
 * </pre>
 *
 * @param <T> 包装的值类型
 */
public abstract class SingleValue<T> implements Identifier {

    private final T value;

    protected SingleValue(T value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleValue<?> that = (SingleValue<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + value + ")";
    }
}
