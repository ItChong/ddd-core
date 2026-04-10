package com.chongstack.ddd.infrastructure.diff;

import com.chongstack.ddd.domain.model.Identifiable;
import com.chongstack.ddd.domain.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 差异比较工具，用于对比快照和当前对象的差异。
 * <p>
 * 支持简单字段比较和集合字段的元素级 Diff。
 * 对于实体集合，通过 ID 匹配实现精确的增/删/改检测。
 */
public final class DiffUtils {

    private static final Logger log = LoggerFactory.getLogger(DiffUtils.class);

    private DiffUtils() {
    }

    /**
     * 对比两个对象的差异。
     *
     * @param snapshot 快照（旧值）
     * @param current  当前值
     * @return 差异结果
     */
    public static EntityDiff diff(Object snapshot, Object current) {
        if (snapshot == null && current == null) {
            return EntityDiff.EMPTY;
        }
        if (snapshot == null || current == null) {
            return new EntityDiff(Collections.emptyMap(), true);
        }
        if (snapshot.getClass() != current.getClass()) {
            return new EntityDiff(Collections.emptyMap(), true);
        }

        Map<String, Diff> fieldDiffs = new LinkedHashMap<>();
        boolean selfModified = false;

        Class<?> clazz = snapshot.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object oldVal = field.get(snapshot);
                    Object newVal = field.get(current);
                    String fieldName = field.getName();

                    if (isCollectionType(field.getType())) {
                        ListDiff listDiff = diffCollection(fieldName, oldVal, newVal);
                        fieldDiffs.put(fieldName, listDiff);
                    } else {
                        if (!Objects.equals(oldVal, newVal)) {
                            selfModified = true;
                            fieldDiffs.put(fieldName, Diff.modified(fieldName, oldVal, newVal));
                        }
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Cannot access field {}: {}", field.getName(), e.getMessage());
                }
            }
            clazz = clazz.getSuperclass();
        }

        return new EntityDiff(fieldDiffs, selfModified);
    }

    @SuppressWarnings("unchecked")
    private static ListDiff diffCollection(String fieldName, Object oldVal, Object newVal) {
        List<Object> oldList = toList(oldVal);
        List<Object> newList = toList(newVal);

        if (oldList.isEmpty() && newList.isEmpty()) {
            return new ListDiff(fieldName, Collections.emptyList());
        }

        boolean hasIdentity = (!oldList.isEmpty() && oldList.getFirst() instanceof Identifiable)
                || (!newList.isEmpty() && newList.getFirst() instanceof Identifiable);

        if (hasIdentity) {
            return diffIdentifiableList(fieldName, oldList, newList);
        } else {
            return diffSimpleList(fieldName, oldList, newList);
        }
    }

    @SuppressWarnings("unchecked")
    private static ListDiff diffIdentifiableList(String fieldName, List<Object> oldList, List<Object> newList) {
        List<Diff> diffs = new ArrayList<>();

        Map<Object, Object> oldMap = new LinkedHashMap<>();
        for (Object item : oldList) {
            Identifier id = ((Identifiable<? extends Identifier>) item).getId();
            if (id != null) {
                oldMap.put(id, item);
            }
        }

        Map<Object, Object> newMap = new LinkedHashMap<>();
        for (Object item : newList) {
            Identifier id = ((Identifiable<? extends Identifier>) item).getId();
            if (id != null) {
                newMap.put(id, item);
            } else {
                diffs.add(Diff.added(fieldName, item));
            }
        }

        for (Map.Entry<Object, Object> entry : oldMap.entrySet()) {
            Object id = entry.getKey();
            Object oldItem = entry.getValue();
            Object newItem = newMap.get(id);
            if (newItem == null) {
                diffs.add(Diff.removed(fieldName, oldItem));
            } else if (!deepEquals(oldItem, newItem)) {
                diffs.add(Diff.modified(fieldName, oldItem, newItem));
            }
        }

        for (Map.Entry<Object, Object> entry : newMap.entrySet()) {
            if (!oldMap.containsKey(entry.getKey())) {
                diffs.add(Diff.added(fieldName, entry.getValue()));
            }
        }

        return new ListDiff(fieldName, diffs);
    }

    private static ListDiff diffSimpleList(String fieldName, List<Object> oldList, List<Object> newList) {
        List<Diff> diffs = new ArrayList<>();
        Set<Object> oldSet = new LinkedHashSet<>(oldList);
        Set<Object> newSet = new LinkedHashSet<>(newList);

        for (Object item : oldList) {
            if (!newSet.contains(item)) {
                diffs.add(Diff.removed(fieldName, item));
            }
        }
        for (Object item : newList) {
            if (!oldSet.contains(item)) {
                diffs.add(Diff.added(fieldName, item));
            }
        }

        return new ListDiff(fieldName, diffs);
    }

    private static boolean deepEquals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.getClass() != b.getClass()) return false;

        Class<?> clazz = a.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    if (!Objects.equals(field.get(a), field.get(b))) {
                        return false;
                    }
                } catch (IllegalAccessException e) {
                    return false;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> toList(Object collection) {
        if (collection == null) {
            return Collections.emptyList();
        }
        if (collection instanceof List<?> list) {
            return (List<Object>) list;
        }
        if (collection instanceof Collection<?> coll) {
            return new ArrayList<>(coll);
        }
        return Collections.emptyList();
    }

    private static boolean isCollectionType(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }
}
