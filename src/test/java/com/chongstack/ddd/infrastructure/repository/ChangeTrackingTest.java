package com.chongstack.ddd.infrastructure.repository;

import com.chongstack.ddd.domain.model.BaseAggregate;
import com.chongstack.ddd.domain.model.BaseEntity;
import com.chongstack.ddd.domain.model.Identifier;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证基于 JaVers 的 Change-Tracking 核心场景：
 * 主子订单模型下，修改子订单数量同时改变主订单状态，
 * JaVers 应精确识别出变更的部分。
 */
class ChangeTrackingTest {

    // -- 测试用 ID 类型 --

    record OrderId(Long value) implements Identifier {
    }

    record ItemId(Long value) implements Identifier {
    }

    // -- 测试用领域模型 --

    enum OrderStatus {CREATED, PAID}

    static class LineItem extends BaseEntity<ItemId> implements Serializable {
        private Long itemId;
        private int quantity;
        private int price;

        LineItem() {
        }

        LineItem(ItemId id, Long itemId, int quantity, int price) {
            super(id);
            this.itemId = itemId;
            this.quantity = quantity;
            this.price = price;
        }

        void changeQuantity(int newQuantity) {
            this.quantity = newQuantity;
        }

        int getQuantity() {
            return quantity;
        }

        int getPrice() {
            return price;
        }
    }

    static class Order extends BaseAggregate<OrderId> implements Serializable {
        private Long userId;
        private OrderStatus status = OrderStatus.CREATED;
        private List<LineItem> lineItems = new ArrayList<>();

        Order() {
        }

        Order(OrderId id, Long userId) {
            super(id);
            this.userId = userId;
        }

        void addLineItem(LineItem item) {
            lineItems.add(item);
        }

        void pay() {
            this.status = OrderStatus.PAID;
        }

        OrderStatus getStatus() {
            return status;
        }

        List<LineItem> getLineItems() {
            return lineItems;
        }
    }

    // -- 测试用内存仓储 --

    static class InMemoryOrderRepository extends DbRepositorySupport<Order, OrderId> {
        int insertCount = 0;
        int updateCount = 0;
        int deleteCount = 0;
        private final java.util.Map<Long, Order> store = new java.util.HashMap<>();
        private long nextId = 1000;

        @Override
        protected void onInsert(Order aggregate) {
            insertCount++;
            long id = ++nextId;
            setAggregateId(aggregate, new OrderId(id));
            store.put(id, aggregate);
        }

        @Override
        protected Order onSelect(OrderId id) {
            return store.get(id.value());
        }

        @Override
        protected void onUpdate(Order aggregate, Diff diff) {
            updateCount++;
            store.put(aggregate.getId().value(), aggregate);
        }

        @Override
        protected void onDelete(Order aggregate) {
            deleteCount++;
            store.remove(aggregate.getId().value());
        }
    }

    @Test
    void save_newAggregate_shouldInsert() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order(null, 100L);
        order.addLineItem(new LineItem(null, 1L, 5, 10));

        repo.save(order);

        assertThat(order.getId()).isNotNull();
        assertThat(repo.insertCount).isEqualTo(1);
        assertThat(repo.updateCount).isEqualTo(0);
    }

    @Test
    void save_unchangedAggregate_shouldSkipUpdate() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order(null, 100L);
        repo.save(order);

        repo.save(order);

        assertThat(repo.insertCount).isEqualTo(1);
        assertThat(repo.updateCount).isEqualTo(0);
    }

    @Test
    void save_modifiedAggregate_shouldDetectChanges() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order(null, 100L);
        order.addLineItem(new LineItem(new ItemId(1L), 10L, 5, 100));
        order.addLineItem(new LineItem(new ItemId(2L), 20L, 2, 200));
        repo.save(order);

        order.getLineItems().getFirst().changeQuantity(3);
        order.pay();
        repo.save(order);

        assertThat(repo.insertCount).isEqualTo(1);
        assertThat(repo.updateCount).isEqualTo(1);
    }

    @Test
    void detectChanges_shouldReportValueChangesOnRootAndChild() {
        Order snapshot = new Order(new OrderId(1L), 100L);
        snapshot.addLineItem(new LineItem(new ItemId(1L), 10L, 5, 100));
        snapshot.addLineItem(new LineItem(new ItemId(2L), 20L, 2, 200));

        Order current = SnapshotUtils.snapshot(snapshot);
        current.pay();
        current.getLineItems().getFirst().changeQuantity(3);

        Diff diff = JaversRegistry.forType(Order.class).compare(snapshot, current);

        assertThat(diff.hasChanges()).isTrue();

        List<ValueChange> valueChanges = diff.getChangesByType(ValueChange.class);

        assertThat(valueChanges)
                .anyMatch(vc -> "status".equals(vc.getPropertyName()));

        assertThat(valueChanges)
                .anyMatch(vc -> "quantity".equals(vc.getPropertyName()));
    }

    @Test
    void snapshot_shouldCreateIndependentCopy() {
        Order original = new Order(new OrderId(1L), 100L);
        original.addLineItem(new LineItem(new ItemId(1L), 10L, 5, 100));

        Order copy = SnapshotUtils.snapshot(original);

        original.pay();
        original.getLineItems().getFirst().changeQuantity(99);

        assertThat(copy.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(copy.getLineItems().getFirst().getQuantity()).isEqualTo(5);
    }

    @Test
    void find_shouldReturnTrackedAggregate() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order(null, 100L);
        repo.save(order);

        Order found = repo.find(order.getId());
        assertThat(found).isNotNull();

        repo.save(found);
        assertThat(repo.updateCount).isEqualTo(0);
    }

    @Test
    void remove_shouldDeleteAndDetach() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        Order order = new Order(null, 100L);
        repo.save(order);

        repo.remove(order);
        assertThat(repo.deleteCount).isEqualTo(1);
    }
}
