package com.chongstack.ddd.infrastructure.repository;

import com.chongstack.ddd.domain.model.Aggregate;
import com.chongstack.ddd.domain.model.Identifier;
import org.javers.core.Javers;
import org.javers.core.diff.Diff;

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

    /**
     * 使用 JaVers 对比快照与当前聚合根的差异。
     *
     * @return JaVers 原生 Diff 结果；若聚合无 ID 则返回空 Diff
     */
    Diff detectChanges(T aggregate) {
        if (aggregate.getId() == null) {
            return JaversRegistry.emptyDiff();
        }
        T snapshot = snapshotMap.get(aggregate.getId());
        if (snapshot == null) {
            attach(aggregate);
            snapshot = snapshotMap.get(aggregate.getId());
        }
        Javers javers = JaversRegistry.forType(aggregate.getClass());
        return javers.compare(snapshot, aggregate);
    }

    void merge(T aggregate) {
        if (aggregate.getId() != null) {
            T snapshot = SnapshotUtils.snapshot(aggregate);
            snapshotMap.put(aggregate.getId(), snapshot);
        }
    }

    void clear() {
        snapshotMap.clear();
    }
}
