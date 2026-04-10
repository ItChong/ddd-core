package com.chongstack.ddd.infrastructure.repository;

import com.chongstack.ddd.domain.model.Aggregate;
import com.chongstack.ddd.domain.model.Identifier;
import com.chongstack.ddd.infrastructure.diff.DiffUtils;
import com.chongstack.ddd.infrastructure.diff.EntityDiff;
import com.chongstack.ddd.infrastructure.diff.SnapshotUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 聚合上下文，维护聚合的快照映射。
 * <p>
 * 每个线程独立持有自己的 AggregateContext（通过 ThreadLocal），
 * 避免多线程下的共享状态问题。
 */
class AggregateContext<T extends Aggregate<ID>, ID extends Identifier> {

    private final Map<ID, T> snapshotMap = new HashMap<>();

    void attach(T aggregate) {
        if (aggregate.getId() != null && !snapshotMap.containsKey(aggregate.getId())) {
            merge(aggregate);
        }
    }

    void detach(T aggregate) {
        if (aggregate.getId() != null) {
            snapshotMap.remove(aggregate.getId());
        }
    }

    T find(ID id) {
        return snapshotMap.get(id);
    }

    EntityDiff detectChanges(T aggregate) {
        if (aggregate.getId() == null) {
            return EntityDiff.EMPTY;
        }
        T snapshot = snapshotMap.get(aggregate.getId());
        if (snapshot == null) {
            attach(aggregate);
            snapshot = snapshotMap.get(aggregate.getId());
        }
        return DiffUtils.diff(snapshot, aggregate);
    }

    void merge(T aggregate) {
        if (aggregate.getId() != null) {
            T snapshot = SnapshotUtils.snapshot(aggregate);
            snapshotMap.put(aggregate.getId(), snapshot);
        }
    }
}
