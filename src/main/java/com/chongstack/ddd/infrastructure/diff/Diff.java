package com.chongstack.ddd.infrastructure.diff;

/**
 * 单个属性的变更记录。
 */
public class Diff {

    private final DiffType type;
    private final String fieldName;
    private final Object oldValue;
    private final Object newValue;

    public Diff(DiffType type, String fieldName, Object oldValue, Object newValue) {
        this.type = type;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public static Diff added(String fieldName, Object newValue) {
        return new Diff(DiffType.ADDED, fieldName, null, newValue);
    }

    public static Diff removed(String fieldName, Object oldValue) {
        return new Diff(DiffType.REMOVED, fieldName, oldValue, null);
    }

    public static Diff modified(String fieldName, Object oldValue, Object newValue) {
        return new Diff(DiffType.MODIFIED, fieldName, oldValue, newValue);
    }

    public static Diff unchanged(String fieldName, Object value) {
        return new Diff(DiffType.UNCHANGED, fieldName, value, value);
    }

    public DiffType getType() {
        return type;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public boolean isChanged() {
        return type != DiffType.UNCHANGED;
    }
}
