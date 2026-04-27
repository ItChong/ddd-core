package com.chongstack.ddd.domain.repository;

import com.chongstack.ddd.domain.model.Aggregate;
import com.chongstack.ddd.domain.model.Identifier;

import java.util.Optional;

/**
 * 仓储基础接口。
 * <p>
 * Repository 是聚合根的持久化抽象，屏蔽底层存储细节。
 * 接口命名使用领域语言（find/save/remove），而非数据库语法（select/insert/delete），
 * 使业务代码与底层存储实现完全解耦。
 * <p>
 * 此接口定义在 Domain 层，实现在 Infrastructure 层。
 *
 * @param <T>  聚合根类型
 * @param <ID> 标识类型
 */
public interface Repository<T extends Aggregate<ID>, ID extends Identifier> {

    /**
     * 根据 ID 查找聚合根。
     *
     * @param id 聚合根 ID，不能为 null
     * @return 包含聚合根的 Optional，不存在时返回 empty
     */
    Optional<T> find(ID id);

    /**
     * 保存聚合根（新增或更新）。
     * <p>
     * 使用统一的 save 而非区分 insert/update，
     * 具体由实现层根据 ID 是否存在来判断。
     */
    void save(T aggregate);

    /**
     * 移除聚合根。
     */
    void remove(T aggregate);
}
