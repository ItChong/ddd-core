package com.chongstack.ddd.infrastructure.repository;

import com.chongstack.ddd.domain.model.Aggregate;
import com.chongstack.ddd.domain.model.BaseAggregate;
import com.chongstack.ddd.domain.model.Identifier;
import com.chongstack.ddd.domain.event.DomainEvent;
import com.chongstack.ddd.domain.repository.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 仓储支撑基类。
 * <p>
 * 子类只需实现四个模板方法：
 * <ul>
 *   <li>{@link #onInsert(Aggregate)} — 新增聚合到存储</li>
 *   <li>{@link #onSelect(Identifier)} — 根据 ID 查询聚合</li>
 *   <li>{@link #onUpdate(Aggregate)} — 更新聚合到存储</li>
 *   <li>{@link #onDelete(Aggregate)} — 从存储中删除聚合</li>
 * </ul>
 * <p>
 * save 通过 {@link #isNew(Aggregate)} 判断新增还是更新，默认按 {@code id == null} 区分。
 * 若聚合使用业务预分配 ID，子类可覆盖 {@link #isNew(Aggregate)} 提供自定义判断逻辑。
 * <p>
 * 持久化策略（全量更新、局部 SQL、upsert、子表重建等）由具体仓储实现决定，
 * 基类不做自动变更检测、不持有跨调用状态。
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

    /**
     * 判断聚合是否为新建（尚未持久化）。
     * <p>
     * 默认实现：{@code id == null} 视为新建。
     * 若聚合使用业务预分配 ID，子类应覆盖此方法。
     */
    protected boolean isNew(T aggregate) {
        return aggregate.getId() == null;
    }

    @Override
    public Optional<T> find(ID id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(onSelect(id));
    }

    @Override
    public void save(T aggregate) {
        Objects.requireNonNull(aggregate, "aggregate must not be null");
        if (isNew(aggregate)) {
            onInsert(aggregate);
        } else {
            onUpdate(aggregate);
        }
    }

    @Override
    public void remove(T aggregate) {
        Objects.requireNonNull(aggregate, "aggregate must not be null");
        onDelete(aggregate);
    }

    /**
     * 获取并清除聚合根上累积的领域事件。
     * 委托给 {@link BaseAggregate#pullDomainEvents()}。
     */
    protected List<DomainEvent> extractDomainEvents(T aggregate) {
        if (aggregate instanceof BaseAggregate<?> baseAggregate) {
            return baseAggregate.pullDomainEvents();
        }
        return Collections.emptyList();
    }
}
