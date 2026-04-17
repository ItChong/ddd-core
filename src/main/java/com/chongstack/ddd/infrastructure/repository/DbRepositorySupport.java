package com.chongstack.ddd.infrastructure.repository;

import com.chongstack.ddd.domain.model.Aggregate;
import com.chongstack.ddd.domain.model.BaseAggregate;
import com.chongstack.ddd.domain.model.BaseEntity;
import com.chongstack.ddd.domain.model.Identifier;
import com.chongstack.ddd.domain.event.DomainEvent;
import com.chongstack.ddd.domain.repository.Repository;
import org.javers.core.diff.Diff;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * 数据库仓储支撑基类，内置基于 JaVers 的 Snapshot 变更追踪。
 * <p>
 * 子类只需实现四个模板方法：
 * <ul>
 *   <li>{@link #onInsert(Aggregate)} - 新增聚合</li>
 *   <li>{@link #onSelect(Identifier)} - 根据 ID 查询</li>
 *   <li>{@link #onUpdate(Aggregate, Diff)} - 根据 JaVers Diff 更新变更部分</li>
 *   <li>{@link #onDelete(Aggregate)} - 删除聚合</li>
 * </ul>
 * <p>
 * save 方法自动处理新增/更新的判断逻辑，以及变更检测：
 * <pre>
 * - 无 ID → onInsert → attach
 * - 有 ID → JaVers compare → 有变更则 onUpdate → merge
 * </pre>
 * <p>
 * onUpdate 接收的 {@link Diff} 是 JaVers 原生对象，可通过如下 API 精细判断变更：
 * <pre>
 * diff.hasChanges()                         // 是否有任何变更
 * diff.getChangesByType(ValueChange.class)  // 简单属性变更
 * diff.getChangesByType(ListChange.class)   // 集合变更（含元素级增/删/改）
 * diff.getPropertyChanges("fieldName")      // 指定属性的变更
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

    /**
     * 根据 JaVers Diff 结果执行增量更新。
     *
     * @param aggregate 当前聚合根状态
     * @param diff      JaVers 深度对比结果，包含所有变更的详细信息
     */
    protected abstract void onUpdate(T aggregate, Diff diff);

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
            aggregateManager.merge(aggregate);
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

        Diff diff = aggregateManager.detectChanges(aggregate);
        if (!diff.hasChanges()) {
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
     * 清除当前上下文中所有聚合快照。
     * <p>
     * 在线程池环境中，应在每次工作单元（事务/请求）结束后调用，
     * 防止快照跨请求泄漏导致变更检测不准确。
     * 典型用法：在 Application Service 的 finally 块或 AOP 后置通知中调用。
     */
    protected void clear() {
        aggregateManager.clear();
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
