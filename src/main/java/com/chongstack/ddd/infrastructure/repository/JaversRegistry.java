package com.chongstack.ddd.infrastructure.repository;

import com.chongstack.ddd.domain.model.Identifiable;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.metamodel.clazz.EntityDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JaVers 实例注册中心。
 * <p>
 * 按聚合根类型缓存 JaVers 实例，首次使用时自动扫描聚合内部所有
 * 实现 {@link Identifiable} 的具体类并注册为 JaVers Entity（按 "id" 属性匹配），
 * 确保实体按 ID 对比、值对象按值对比。
 */
final class JaversRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(JaversRegistry.class);
    private static final Map<Class<?>, Javers> CACHE = new ConcurrentHashMap<>();
    private static final Javers BASELINE = JaversBuilder.javers().build();
    private static final Diff EMPTY_DIFF = BASELINE.compare(new EmptyVO(), new EmptyVO());

    private static final class EmptyVO {
    }

    private JaversRegistry() {
    }

    /**
     * 获取或创建指定聚合根类型对应的 JaVers 实例。
     * <p>
     * 缓存以 Class 对象为 key。在 Spring Boot DevTools 热重启后，
     * RestartClassLoader 产出全新的 Class 对象，天然与旧缓存隔离，
     * 不会命中旧 ClassLoader 对应的 Javers 实例。
     * 旧条目会随旧 Class 被 GC 回收（ConcurrentHashMap 的 key 是强引用，
     * 但 DevTools 会卸载整个旧 RestartClassLoader 树）。
     * <p>
     * 若需主动清理，可调用 {@link #clearCache()}。
     */
    static Javers forType(Class<?> rootClass) {
        return CACHE.computeIfAbsent(rootClass, clazz -> {
            JaversBuilder builder = JaversBuilder.javers();
            Set<Class<?>> visited = new HashSet<>();
            scanAndRegisterEntityTypes(builder, clazz, visited);
            return builder.build();
        });
    }

    /**
     * 清除所有缓存的 JaVers 实例。
     * 在 ClassLoader 发生变更（如 DevTools 热重启）时调用，
     * 防止旧 ClassLoader 的类残留在缓存中。
     */
    static void clearCache() {
        CACHE.clear();
    }

    /**
     * 返回一个不包含任何变更的空 Diff 实例。
     */
    static Diff emptyDiff() {
        return EMPTY_DIFF;
    }

    /**
     * 递归扫描类型层次结构，将所有实现 {@link Identifiable} 的具体类
     * 注册为 JaVers Entity，以 "id" 作为标识属性。
     */
    private static void scanAndRegisterEntityTypes(JaversBuilder builder, Class<?> clazz, Set<Class<?>> visited) {
        if (clazz == null || clazz == Object.class || !visited.add(clazz)) {
            return;
        }
        if (Identifiable.class.isAssignableFrom(clazz)
                && !clazz.isInterface()
                && !Modifier.isAbstract(clazz.getModifiers())) {
            builder.registerEntity(new EntityDefinition(clazz, "id"));
        }

        scanAndRegisterEntityTypes(builder, clazz.getSuperclass(), visited);

        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                extractAndRegisterTypes(builder, field.getGenericType(), visited);
            }
            current = current.getSuperclass();
        }
    }

    private static void extractAndRegisterTypes(JaversBuilder builder, Type type, Set<Class<?>> visited) {
        if (type instanceof Class<?> clazz) {
            if (!clazz.isPrimitive() && !clazz.isArray() && !clazz.getName().startsWith("java.")) {
                scanAndRegisterEntityTypes(builder, clazz, visited);
            }
        } else if (type instanceof ParameterizedType pt) {
            for (Type arg : pt.getActualTypeArguments()) {
                extractAndRegisterTypes(builder, arg, visited);
            }
        } else if (type instanceof WildcardType wt) {
            for (Type bound : wt.getUpperBounds()) {
                extractAndRegisterTypes(builder, bound, visited);
            }
        }
    }
}
