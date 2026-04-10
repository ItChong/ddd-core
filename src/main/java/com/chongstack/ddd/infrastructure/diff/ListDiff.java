package com.chongstack.ddd.infrastructure.diff;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 集合属性的变更记录。
 * <p>
 * 内部包含每个元素级别的 Diff（新增/修改/删除），
 * 用于支持聚合内子实体集合的精细化变更追踪。
 */
public class ListDiff extends Diff implements Iterable<Diff> {

    private final List<Diff> elementDiffs;

    public ListDiff(String fieldName, List<Diff> elementDiffs) {
        super(
                elementDiffs.stream().anyMatch(Diff::isChanged) ? DiffType.MODIFIED : DiffType.UNCHANGED,
                fieldName,
                null,
                null
        );
        this.elementDiffs = elementDiffs != null ? elementDiffs : Collections.emptyList();
    }

    public List<Diff> getElementDiffs() {
        return Collections.unmodifiableList(elementDiffs);
    }

    @Override
    public Iterator<Diff> iterator() {
        return elementDiffs.iterator();
    }

    @Override
    public boolean isChanged() {
        return elementDiffs.stream().anyMatch(Diff::isChanged);
    }
}
