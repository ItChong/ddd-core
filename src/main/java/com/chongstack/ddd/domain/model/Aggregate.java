package com.chongstack.ddd.domain.model;

/**
 * 聚合根标记接口。
 * <p>
 * 聚合根是一组相关对象的一致性边界入口。
 * 外部只能通过聚合根访问聚合内部的实体和值对象。
 * Repository 仅针对聚合根进行操作。
 *
 * @param <ID> 标识类型
 */
public interface Aggregate<ID extends Identifier> extends Entity<ID> {

}
