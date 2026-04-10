package com.chongstack.ddd.infrastructure.repository;

import com.chongstack.ddd.domain.model.Aggregate;
import com.chongstack.ddd.domain.model.BaseEntity;
import com.chongstack.ddd.domain.model.Identifier;
import com.chongstack.ddd.domain.repository.Repository;

import java.lang.reflect.Field;

/**
 * 简单仓储基类，不带变更追踪。
 * <p>
 * 适用于简单场景：聚合内部结构简单，不需要精细化的变更检测。
 * 每次 save 直接判断有无 ID 来决定 insert 或 update。
 * <p>
 * 如果需要变更追踪能力（避免全量更新），请使用 {@link DbRepositorySupport}。
 *
 * @param <T>  聚合根类型
 * @param <ID> 标识类型
 */
public abstract class AbstractRepository<T extends Aggregate<ID>, ID extends Identifier>
        implements Repository<T, ID> {

    protected abstract void onInsert(T aggregate);

    protected abstract T onSelect(ID id);

    protected abstract void onUpdate(T aggregate);

    protected abstract void onDelete(T aggregate);

    @Override
    public T find(ID id) {
        return onSelect(id);
    }

    @Override
    public void save(T aggregate) {
        if (aggregate.getId() == null) {
            onInsert(aggregate);
        } else {
            onUpdate(aggregate);
        }
    }

    @Override
    public void remove(T aggregate) {
        onDelete(aggregate);
    }

    @SuppressWarnings("unchecked")
    protected void setAggregateId(T aggregate, ID id) {
        if (aggregate instanceof BaseEntity) {
            try {
                Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(aggregate, id);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to set aggregate ID", e);
            }
        }
    }
}
