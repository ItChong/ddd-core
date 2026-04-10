package com.chongstack.ddd.domain.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 实体基类，提供 ID 管理和基于 ID 的相等性判断。
 * <p>
 * 实现 Serializable 以支持变更追踪中的快照机制。
 *
 * @param <ID> 标识类型
 */
public abstract class BaseEntity<ID extends Identifier> implements Entity<ID>, Serializable {

    private ID id;

    protected BaseEntity() {
    }

    protected BaseEntity(ID id) {
        this.id = id;
    }

    @Override
    public ID getId() {
        return id;
    }

    protected void setId(ID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
