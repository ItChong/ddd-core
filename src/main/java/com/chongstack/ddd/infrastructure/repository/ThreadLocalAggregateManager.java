package com.chongstack.ddd.infrastructure.repository;

import com.chongstack.ddd.domain.model.Aggregate;
import com.chongstack.ddd.domain.model.Identifier;
import org.javers.core.diff.Diff;

/**
 * 基于 ThreadLocal 的聚合管理器实现。
 * <p>
 * 每个线程持有独立的 AggregateContext，
 * 确保多线程环境下快照互不干扰。
 */
class ThreadLocalAggregateManager<T extends Aggregate<ID>, ID extends Identifier>
        implements AggregateManager<T, ID> {

    private final ThreadLocal<AggregateContext<T, ID>> context =
            ThreadLocal.withInitial(AggregateContext::new);

    @Override
    public void attach(T aggregate) {
        context.get().attach(aggregate);
    }

    @Override
    public void detach(T aggregate) {
        context.get().detach(aggregate);
    }

    @Override
    public T find(ID id) {
        return context.get().find(id);
    }

    @Override
    public Diff detectChanges(T aggregate) {
        return context.get().detectChanges(aggregate);
    }

    @Override
    public void merge(T aggregate) {
        context.get().merge(aggregate);
    }

    @Override
    public void clear() {
        context.get().clear();
        context.remove();
    }
}
