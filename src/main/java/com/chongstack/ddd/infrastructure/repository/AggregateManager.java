package com.chongstack.ddd.infrastructure.repository;

import com.chongstack.ddd.domain.model.Aggregate;
import com.chongstack.ddd.domain.model.Identifier;
import org.javers.core.diff.Diff;

/**
 * 聚合管理器接口，负责聚合的变更追踪。
 * <p>
 * 通过保存聚合快照并在保存时对比差异，
 * 实现只更新发生变更的部分，避免全量更新的性能浪费。
 * <p>
 * 变更检测基于 JaVers 深度对比，返回 {@link Diff} 原生结果，
 * 上层可通过 {@code diff.hasChanges()} / {@code diff.getChangesByType(...)}
 * 等 API 精细判断变更内容。
 *
 * @param <T>  聚合根类型
 * @param <ID> 标识类型
 */
public interface AggregateManager<T extends Aggregate<ID>, ID extends Identifier> {

    void attach(T aggregate);

    void detach(T aggregate);

    T find(ID id);

    /**
     * 检测聚合根相对于快照的变更。
     *
     * @return JaVers Diff，通过 {@code diff.hasChanges()} 判断是否有变更
     */
    Diff detectChanges(T aggregate);

    void merge(T aggregate);

    /**
     * 清除所有聚合快照，释放当前上下文。
     * <p>
     * 在基于线程池的环境中（如 Servlet 容器），必须在每次工作单元结束后调用，
     * 防止快照跨请求泄漏导致脏数据比较。
     */
    void clear();
}
