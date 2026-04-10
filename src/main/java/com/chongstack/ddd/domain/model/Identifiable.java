package com.chongstack.ddd.domain.model;

/**
 * 可标识对象的通用接口。
 * <p>
 * 任何拥有唯一标识的对象都应实现此接口。
 *
 * @param <ID> 标识类型
 */
public interface Identifiable<ID extends Identifier> {

    ID getId();
}
