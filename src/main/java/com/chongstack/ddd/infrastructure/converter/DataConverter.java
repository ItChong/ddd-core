package com.chongstack.ddd.infrastructure.converter;

/**
 * 数据转换器接口（Infrastructure 层）。
 * <p>
 * 负责 Domain Entity 与 Persistence DO 之间的双向转换。
 * 这是隔离领域模型和数据模型的关键桥梁：
 * <ul>
 *   <li>领域模型（Entity）反映业务语言，字段和结构由业务决定</li>
 *   <li>数据模型（DO）反映存储结构，字段和类型由数据库决定</li>
 *   <li>两者可能有完全不同的字段名、类型和嵌套关系</li>
 * </ul>
 *
 * @param <E>  Domain Entity 类型
 * @param <DO> Persistence Data Object 类型
 */
public interface DataConverter<E, DO> {

    /**
     * 将 DO 转换为 Domain Entity。
     */
    E fromData(DO dataObject);

    /**
     * 将 Domain Entity 转换为 DO。
     */
    DO toData(E entity);
}
