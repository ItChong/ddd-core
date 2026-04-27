package com.chongstack.ddd.domain.model;

import com.chongstack.ddd.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类。
 * <p>
 * 在 BaseEntity 基础上增加了领域事件的收集能力。
 * 聚合根内发生的业务行为可以通过 {@link #registerEvent(DomainEvent)} 注册领域事件，
 * 由 Application 层在持久化后统一发布。
 *
 * @param <ID> 标识类型
 */
public abstract class BaseAggregate<ID extends Identifier> extends BaseEntity<ID> implements Aggregate<ID> {

    private transient List<DomainEvent> domainEvents;

    protected BaseAggregate() {
    }

    protected BaseAggregate(ID id) {
        super(id);
    }

    protected void registerEvent(DomainEvent event) {
        if (domainEvents == null) {
            domainEvents = new ArrayList<>();
        }
        domainEvents.add(event);
    }

    /**
     * 返回已注册领域事件的只读视图（不清除）。
     */
    public List<DomainEvent> getDomainEvents() {
        if (domainEvents == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 取出已注册的领域事件并清空。
     * <p>
     * Application Service 在持久化聚合后调用此方法获取事件用于发布。
     * 返回值是独立副本，调用后聚合不再持有这些事件。
     */
    public List<DomainEvent> pullDomainEvents() {
        if (domainEvents == null || domainEvents.isEmpty()) {
            return Collections.emptyList();
        }
        List<DomainEvent> snapshot = List.copyOf(domainEvents);
        domainEvents.clear();
        return snapshot;
    }

    /**
     * 清除所有已注册的领域事件。
     * <p>
     * 框架生命周期方法，通常不需要在业务代码中直接调用；
     * 优先使用 {@link #pullDomainEvents()} 取出并清空。
     */
    public void clearDomainEvents() {
        if (domainEvents != null) {
            domainEvents.clear();
        }
    }
}
