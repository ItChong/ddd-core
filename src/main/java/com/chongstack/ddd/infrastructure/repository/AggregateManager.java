package com.chongstack.ddd.infrastructure.repository;

import com.chongstack.ddd.domain.model.Aggregate;
import com.chongstack.ddd.domain.model.Identifier;
import com.chongstack.ddd.infrastructure.diff.EntityDiff;

/**
 * 聚合管理器接口，负责聚合的变更追踪。
 * <p>
 * 通过保存聚合快照并在保存时对比差异，
 * 实现只更新发生变更的部分，避免全量更新的性能浪费。
 *
 * @param <T>  聚合根类型
 * @param <ID> 标识类型
 */
public interface AggregateManager<T extends Aggregate<ID>, ID extends Identifier> {

    void attach(T aggregate);

    void detach(T aggregate);

    T find(ID id);

    EntityDiff detectChanges(T aggregate);

    void merge(T aggregate);
}
