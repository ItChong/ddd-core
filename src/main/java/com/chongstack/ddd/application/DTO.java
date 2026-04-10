package com.chongstack.ddd.application;

import java.io.Serializable;

/**
 * 数据传输对象标记接口。
 * <p>
 * DTO 用于 Application 层的入参和出参，
 * 适配不同的业务场景，避免领域对象变成万能大对象。
 * DTO 需要可序列化以支持网络传输。
 * <p>
 * 推荐使用 Java Record 来实现 DTO。
 */
public interface DTO extends Serializable {

}
