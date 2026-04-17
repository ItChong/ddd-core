package com.chongstack.ddd.infrastructure.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 快照工具，用于创建对象的深拷贝。
 * <p>
 * 优先使用 Java 序列化进行深拷贝，若对象未实现 Serializable 则回退到反射拷贝。
 * 快照是 Change-Tracking 的基础：查询时保存一份快照，保存时与当前状态对比。
 */
final class SnapshotUtils {

    private static final Logger log = LoggerFactory.getLogger(SnapshotUtils.class);

    private SnapshotUtils() {
    }

    @SuppressWarnings("unchecked")
    static <T> T snapshot(T source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Serializable) {
            return (T) deepCopyBySerialization((Serializable) source);
        }
        return (T) deepCopyByReflection(source, new IdentityHashMap<>());
    }

    private static Object deepCopyBySerialization(Serializable source) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(source);
            }
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
                return ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            log.warn("Serialization-based snapshot failed, falling back to reflection: {}", e.getMessage());
            return deepCopyByReflection(source, new IdentityHashMap<>());
        }
    }

    @SuppressWarnings("unchecked")
    private static Object deepCopyByReflection(Object source, IdentityHashMap<Object, Object> visited) {
        if (source == null) {
            return null;
        }
        if (visited.containsKey(source)) {
            return visited.get(source);
        }
        Class<?> clazz = source.getClass();
        if (isImmutable(clazz) || clazz.isRecord()) {
            return source;
        }
        if (clazz.isArray()) {
            return copyArray(source, visited);
        }
        if (source instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            visited.put(source, copy);
            for (Object item : list) {
                copy.add(deepCopyByReflection(item, visited));
            }
            return copy;
        }
        if (source instanceof Set<?> set) {
            Set<Object> copy = new LinkedHashSet<>(set.size());
            visited.put(source, copy);
            for (Object item : set) {
                copy.add(deepCopyByReflection(item, visited));
            }
            return copy;
        }
        if (source instanceof Map<?, ?> map) {
            Map<Object, Object> copy = new LinkedHashMap<>(map.size());
            visited.put(source, copy);
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                copy.put(
                        deepCopyByReflection(entry.getKey(), visited),
                        deepCopyByReflection(entry.getValue(), visited)
                );
            }
            return copy;
        }
        return copyObject(source, clazz, visited);
    }

    private static Object copyArray(Object source, IdentityHashMap<Object, Object> visited) {
        int length = Array.getLength(source);
        Class<?> componentType = source.getClass().getComponentType();
        Object copy = Array.newInstance(componentType, length);
        visited.put(source, copy);
        for (int i = 0; i < length; i++) {
            Array.set(copy, i, deepCopyByReflection(Array.get(source, i), visited));
        }
        return copy;
    }

    private static Object copyObject(Object source, Class<?> clazz, IdentityHashMap<Object, Object> visited) {
        try {
            Object copy = allocateInstance(clazz);
            visited.put(source, copy);

            Class<?> current = clazz;
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = field.get(source);
                    field.set(copy, deepCopyByReflection(value, visited));
                }
                current = current.getSuperclass();
            }
            return copy;
        } catch (Exception e) {
            log.warn("Reflection copy failed for {}: {}", clazz.getName(), e.getMessage());
            return source;
        }
    }

    private static Object allocateInstance(Class<?> clazz) throws Exception {
        try {
            var ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException e) {
            var unsafeClass = Class.forName("sun.misc.Unsafe");
            var unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            var unsafe = unsafeField.get(null);
            var allocate = unsafeClass.getMethod("allocateInstance", Class.class);
            return allocate.invoke(unsafe, clazz);
        }
    }

    private static boolean isImmutable(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == String.class
                || clazz == Boolean.class
                || Number.class.isAssignableFrom(clazz)
                || clazz == Character.class
                || clazz.isEnum()
                || clazz == UUID.class
                || java.time.temporal.Temporal.class.isAssignableFrom(clazz)
                || clazz == java.time.Duration.class
                || clazz == java.time.Period.class
                || clazz == java.math.BigDecimal.class
                || clazz == java.math.BigInteger.class
                || clazz == java.net.URI.class
                || clazz == java.net.URL.class;
    }
}
