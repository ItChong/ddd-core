package com.chongstack.ddd.infrastructure.diff;

import java.util.Collections;
import java.util.Map;

/**
 * 聚合根级别的变更差异汇总。
 * <p>
 * 用于 Repository 保存时判断哪些部分发生了变化，
 * 从而只执行必要的数据库操作，避免全量更新。
 */
public class EntityDiff {

    public static final EntityDiff EMPTY = new EntityDiff(Collections.emptyMap(), false);

    private final Map<String, Diff> fieldDiffs;
    private final boolean selfModified;

    public EntityDiff(Map<String, Diff> fieldDiffs, boolean selfModified) {
        this.fieldDiffs = fieldDiffs;
        this.selfModified = selfModified;
    }

    /**
     * 聚合根自身的简单字段是否有变更。
     */
    public boolean isSelfModified() {
        return selfModified;
    }

    /**
     * 是否完全没有变更。
     */
    public boolean isEmpty() {
        return !selfModified && fieldDiffs.values().stream().noneMatch(Diff::isChanged);
    }

    /**
     * 获取指定字段的变更记录。
     * 对于集合类型的字段，返回的是 {@link ListDiff}。
     */
    public Diff getDiff(String fieldName) {
        return fieldDiffs.get(fieldName);
    }

    public Map<String, Diff> getAllDiffs() {
        return Collections.unmodifiableMap(fieldDiffs);
    }
}
