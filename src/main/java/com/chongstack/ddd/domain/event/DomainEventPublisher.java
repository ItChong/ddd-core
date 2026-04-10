package com.chongstack.ddd.domain.event;

import java.util.List;

/**
 * 领域事件发布者接口。
 * <p>
 * 由 Infrastructure 层实现，可以对接 Spring Event、MQ 等。
 * Application Service 在持久化聚合后调用此接口发布事件。
 */
public interface DomainEventPublisher {

    void publish(DomainEvent event);

    default void publishAll(List<DomainEvent> events) {
        if (events != null) {
            events.forEach(this::publish);
        }
    }
}
