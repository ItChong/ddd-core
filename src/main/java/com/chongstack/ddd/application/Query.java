package com.chongstack.ddd.application;

import java.io.Serializable;

/**
 * 查询标记接口。
 * <p>
 * 查询代表一次读操作意图，属于 Application 层的入参。
 * 查询不应引起任何副作用（CQRS 读写分离原则）。
 * <p>
 * 推荐使用 Java Record 来实现 Query。
 */
public interface Query extends Serializable {

}
