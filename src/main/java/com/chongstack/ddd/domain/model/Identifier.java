package com.chongstack.ddd.domain.model;

import java.io.Serializable;

/**
 * ID 类型的标记接口。
 * <p>
 * 所有聚合根和实体的 ID 都应实现此接口，
 * 用于将业务含义的 ID 与原始类型（如 Long、String）区分开来，
 * 避免在方法签名中传递裸类型导致的参数混淆。
 */
public interface Identifier extends Serializable {

}
