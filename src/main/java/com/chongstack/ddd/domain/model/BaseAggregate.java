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

    public List<DomainEvent> getDomainEvents() {
        if (domainEvents == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        if (domainEvents != null) {
            domainEvents.clear();
        }
    }
}
