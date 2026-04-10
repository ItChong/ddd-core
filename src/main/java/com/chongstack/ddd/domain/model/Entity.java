package com.chongstack.ddd.domain.model;

/**
 * 实体标记接口。
 * <p>
 * 实体通过唯一标识进行区分，具有生命周期，状态可变。
 * 实体的相等性由 ID 决定，而非属性值。
 *
 * @param <ID> 标识类型
 */
public interface Entity<ID extends Identifier> extends Identifiable<ID> {

}
