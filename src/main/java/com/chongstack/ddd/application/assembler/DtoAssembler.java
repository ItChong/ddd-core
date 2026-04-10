package com.chongstack.ddd.application.assembler;

/**
 * DTO 组装器接口（Application 层）。
 * <p>
 * 负责将一个或多个 Domain Entity 组装为 DTO，
 * 以及将 DTO/Command 转换为 Domain Entity。
 * <p>
 * 组装器的核心价值：
 * <ul>
 *   <li>将复杂的对象转化逻辑收敛到一个类中</li>
 *   <li>避免业务对象变成万能大对象</li>
 *   <li>便于单独测试转换逻辑</li>
 * </ul>
 * <p>
 * 推荐使用 MapStruct 来实现，编译时生成映射代码，性能与手写一致。
 *
 * @param <E> Domain Entity 类型
 * @param <D> DTO 类型
 */
public interface DtoAssembler<E, D> {

    /**
     * 将 Domain Entity 转换为 DTO。
     */
    D toDTO(E entity);

    /**
     * 将 DTO 转换为 Domain Entity。
     */
    E toEntity(D dto);
}
