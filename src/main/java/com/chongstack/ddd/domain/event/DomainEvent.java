package com.chongstack.ddd.domain.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件基类。
 * <p>
 * 领域事件描述领域中已经发生的事实（过去时），
 * 例如 OrderPaid、TaskScheduled、ProjectCreated。
 * <p>
 * 事件一旦创建即不可变。
 */
public abstract class DomainEvent implements Serializable {

    private final String eventId;
    private final Instant occurredOn;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    /**
     * 事件类型名称，默认使用类名。
     * 子类可覆盖以自定义事件类型标识。
     */
    public String getEventType() {
        return getClass().getSimpleName();
    }
}
