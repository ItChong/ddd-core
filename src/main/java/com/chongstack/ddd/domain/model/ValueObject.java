package com.chongstack.ddd.domain.model;

/**
 * 值对象标记接口。
 * <p>
 * 值对象没有唯一标识，通过属性值进行相等性判断。
 * 值对象必须是不可变的，创建后不可修改。
 * <p>
 * 推荐使用 Java Record 来实现值对象，天然满足不可变和值相等的要求。
 */
public interface ValueObject {

}
