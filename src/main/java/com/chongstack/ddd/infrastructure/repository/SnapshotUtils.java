package com.chongstack.ddd.infrastructure.repository;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * 快照工具，通过 Jackson JSON 序列化/反序列化实现对象深拷贝。
 * <p>
 * 相比 Java 原生序列化 ({@code ObjectInputStream})，Jackson 在多 ClassLoader
 * 环境（如 Spring Boot DevTools）下不会出现 class-identity 不匹配问题，
 * 因为反序列化时通过 {@code source.getClass()} 和 Thread Context ClassLoader
 * 解析类型，始终与源对象保持同一 ClassLoader。
 */
final class SnapshotUtils {

    private static final ObjectMapper MAPPER = createMapper();

    private SnapshotUtils() {
    }

    @SuppressWarnings("unchecked")
    static <T> T snapshot(T source) {
        if (source == null) {
            return null;
        }
        try {
            byte[] bytes = MAPPER.writeValueAsBytes(source);
            return (T) MAPPER.readValue(bytes, source.getClass());
        } catch (Exception e) {
            throw new RuntimeException("Snapshot failed for " + source.getClass().getName(), e);
        }
    }

    private static ObjectMapper createMapper() {
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        ObjectMapper mapper = JsonMapper.builder()
                .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
                .build();
        mapper.findAndRegisterModules();
        return mapper;
    }
}
