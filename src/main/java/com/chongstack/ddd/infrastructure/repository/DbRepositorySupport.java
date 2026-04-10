package com.chongstack.ddd.infrastructure.repository;

import com.chongstack.ddd.domain.model.Aggregate;
import com.chongstack.ddd.domain.model.BaseAggregate;
import com.chongstack.ddd.domain.model.BaseEntity;
import com.chongstack.ddd.domain.model.Identifier;
import com.chongstack.ddd.domain.event.DomainEvent;
import com.chongstack.ddd.domain.repository.Repository;
import com.chongstack.ddd.infrastructure.diff.EntityDiff;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * 数据库仓储支撑基类，内置 Snapshot 变更追踪。
 * <p>
 * 子类只需实现四个模板方法：
 * <ul>
 *   <li>{@link #onInsert(Aggregate)} - 新增聚合</li>
 *   <li>{@link #onSelect(Identifier)} - 根据 ID 查询</li>
 *   <li>{@link #onUpdate(Aggregate, EntityDiff)} - 根据 Diff 更新变更部分</li>
 *   <li>{@link #onDelete(Aggregate)} - 删除聚合</li>
 * </ul>
 * <p>
 * save 方法自动处理新增/更新的判断逻辑，以及变更检测：
 * <pre>
 * - 无 ID → onInsert → attach
 * - 有 ID → detectChanges → 有变更则 onUpdate → merge
 * </pre>
 *
 * @param <T>  聚合根类型
 * @param <ID> 标识类型
 */
public abstract class DbRepositorySupport<T extends Aggregate<ID>, ID extends Identifier>
        implements Repository<T, ID> {

    private final AggregateManager<T, ID> aggregateManager;

    protected DbRepositorySupport() {
        this.aggregateManager = new ThreadLocalAggregateManager<>();
    }

    protected AggregateManager<T, ID> getAggregateManager() {
        return aggregateManager;
    }

    protected abstract void onInsert(T aggregate);

    protected abstract T onSelect(ID id);

    protected abstract void onUpdate(T aggregate, EntityDiff diff);

    protected abstract void onDelete(T aggregate);

    @Override
    public void attach(T aggregate) {
        aggregateManager.attach(aggregate);
    }

    @Override
    public void detach(T aggregate) {
        aggregateManager.detach(aggregate);
    }

    @Override
    public T find(ID id) {
        T aggregate = onSelect(id);
        if (aggregate != null) {
            attach(aggregate);
        }
        return aggregate;
    }

    @Override
    public void save(T aggregate) {
        if (aggregate.getId() == null) {
            onInsert(aggregate);
            attach(aggregate);
            return;
        }

        EntityDiff diff = aggregateManager.detectChanges(aggregate);
        if (diff.isEmpty()) {
            return;
        }

        onUpdate(aggregate, diff);
        aggregateManager.merge(aggregate);
    }

    /**
     * 获取并清除聚合根上累积的领域事件。
     * 应在 Application Service 持久化聚合后调用，用于发布事件。
     */
    protected List<DomainEvent> extractDomainEvents(T aggregate) {
        if (aggregate instanceof BaseAggregate<?> baseAggregate) {
            List<DomainEvent> events = baseAggregate.getDomainEvents();
            if (events.isEmpty()) {
                return Collections.emptyList();
            }
            List<DomainEvent> copy = List.copyOf(events);
            baseAggregate.clearDomainEvents();
            return copy;
        }
        return Collections.emptyList();
    }

    @Override
    public void remove(T aggregate) {
        onDelete(aggregate);
        detach(aggregate);
    }

    /**
     * 在 onInsert 中使用，将数据库生成的 ID 回写到聚合根。
     * 由于 BaseEntity.setId 是 protected，跨包的 Repository 实现需要通过此方法设置。
     */
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
