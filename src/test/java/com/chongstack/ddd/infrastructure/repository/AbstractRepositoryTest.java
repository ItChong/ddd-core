package com.chongstack.ddd.infrastructure.repository;

import com.chongstack.ddd.domain.event.DomainEvent;
import com.chongstack.ddd.domain.model.Aggregate;
import com.chongstack.ddd.domain.model.BaseAggregate;
import com.chongstack.ddd.domain.model.BaseEntity;
import com.chongstack.ddd.domain.model.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbstractRepositoryTest {

    // -- ID types --

    record OrderId(Long value) implements Identifier {}

    record ItemId(Long value) implements Identifier {}

    // -- Domain model --

    enum OrderStatus { CREATED, PAID }

    static class OrderPaid extends DomainEvent {}

    static class LineItem extends BaseEntity<ItemId> {
        private int quantity;

        LineItem() {}

        LineItem(ItemId id, int quantity) {
            super(id);
            this.quantity = quantity;
        }

        void changeQuantity(int newQuantity) { this.quantity = newQuantity; }
        int getQuantity() { return quantity; }
    }

    static class Order extends BaseAggregate<OrderId> {
        private Long userId;
        private OrderStatus status = OrderStatus.CREATED;
        private List<LineItem> lineItems = new ArrayList<>();

        Order() {}

        Order(OrderId id, Long userId) {
            super(id);
            this.userId = userId;
        }

        void addLineItem(LineItem item) { lineItems.add(item); }

        void pay() {
            this.status = OrderStatus.PAID;
            registerEvent(new OrderPaid());
        }

        OrderStatus getStatus() { return status; }
        List<LineItem> getLineItems() { return lineItems; }
    }

    // -- In-memory repository --

    static class InMemoryOrderRepository extends AbstractRepository<Order, OrderId> {
        int insertCount = 0;
        int updateCount = 0;
        int deleteCount = 0;
        private final Map<Long, Order> store = new HashMap<>();
        private long nextId = 1000;

        @Override
        protected void onInsert(Order aggregate) {
            insertCount++;
            long id = ++nextId;
            store.put(id, aggregate);
        }

        @Override
        protected Order onSelect(OrderId id) {
            return store.get(id.value());
        }

        @Override
        protected void onUpdate(Order aggregate) {
            updateCount++;
            store.put(aggregate.getId().value(), aggregate);
        }

        @Override
        protected void onDelete(Order aggregate) {
            deleteCount++;
            store.remove(aggregate.getId().value());
        }
    }

    // -- Repository with preset-ID support (overrides isNew) --

    static class PresetIdOrderRepository extends InMemoryOrderRepository {
        private boolean forceNew = false;

        void setForceNew(boolean forceNew) { this.forceNew = forceNew; }

        @Override
        protected boolean isNew(Order aggregate) {
            if (forceNew) return true;
            return super.isNew(aggregate);
        }
    }

    // -- Tests --

    @Test
    void save_newAggregate_shouldInsert() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order(null, 100L);
        order.addLineItem(new LineItem(null, 5));

        repo.save(order);

        assertThat(order.getId()).isNotNull();
        assertThat(repo.insertCount).isEqualTo(1);
        assertThat(repo.updateCount).isEqualTo(0);
    }

    @Test
    void save_existingAggregate_shouldUpdate() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order(null, 100L);
        repo.save(order);

        order.pay();
        repo.save(order);

        assertThat(repo.insertCount).isEqualTo(1);
        assertThat(repo.updateCount).isEqualTo(1);
    }

    @Test
    void save_presetId_withIsNewOverride_shouldInsert() {
        PresetIdOrderRepository repo = new PresetIdOrderRepository();
        Order order = new Order(new OrderId(999L), 100L);

        repo.setForceNew(true);
        repo.save(order);

        assertThat(repo.insertCount).isEqualTo(1);
        assertThat(repo.updateCount).isEqualTo(0);
    }

    @Test
    void find_shouldDelegateToOnSelect() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order(null, 100L);
        repo.save(order);

        Optional<Order> found = repo.find(order.getId());

        assertThat(found).isPresent().containsSame(order);
    }

    @Test
    void find_nonExistent_shouldReturnEmpty() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();

        Optional<Order> found = repo.find(new OrderId(999L));

        assertThat(found).isEmpty();
    }

    @Test
    void find_nullId_shouldThrowNPE() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();

        assertThatThrownBy(() -> repo.find(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void save_null_shouldThrowNPE() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();

        assertThatThrownBy(() -> repo.save(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void remove_null_shouldThrowNPE() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();

        assertThatThrownBy(() -> repo.remove(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void remove_shouldDelegateToOnDelete() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order(null, 100L);
        repo.save(order);

        repo.remove(order);

        assertThat(repo.deleteCount).isEqualTo(1);
        assertThat(repo.find(order.getId())).isEmpty();
    }

    @Test
    void extractDomainEvents_shouldReturnAndClearEvents() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order(null, 100L);
        repo.save(order);

        order.pay();
        List<DomainEvent> events = repo.extractDomainEvents(order);

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(OrderPaid.class);
        assertThat(order.getDomainEvents()).isEmpty();
    }

    @Test
    void extractDomainEvents_noEvents_shouldReturnEmptyList() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order(null, 100L);
        repo.save(order);

        List<DomainEvent> events = repo.extractDomainEvents(order);

        assertThat(events).isEmpty();
    }

    @Test
    void setAggregateId_shouldWriteIdToAggregate() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order(null, 100L);

        repo.save(order);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getId().value()).isGreaterThan(1000L);
    }

    @Test
    void pullDomainEvents_shouldReturnCopyAndClear() {
        Order order = new Order(new OrderId(1L), 100L);
        order.pay();

        List<DomainEvent> pulled = order.pullDomainEvents();

        assertThat(pulled).hasSize(1);
        assertThat(pulled.getFirst()).isInstanceOf(OrderPaid.class);
        assertThat(order.getDomainEvents()).isEmpty();
    }

    @Test
    void pullDomainEvents_noEvents_shouldReturnEmptyList() {
        Order order = new Order(new OrderId(1L), 100L);

        List<DomainEvent> pulled = order.pullDomainEvents();

        assertThat(pulled).isEmpty();
    }
}
