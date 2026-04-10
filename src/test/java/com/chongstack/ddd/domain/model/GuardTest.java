package com.chongstack.ddd.domain.model;

import com.chongstack.ddd.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class GuardTest {

    @Test
    void notNull_shouldThrowOnNull() {
        assertThatThrownBy(() -> Guard.notNull(null, "不能为空"))
                .isInstanceOf(DomainException.class)
                .hasMessage("不能为空");
    }

    @Test
    void notNull_shouldPassOnNonNull() {
        assertThatCode(() -> Guard.notNull("hello", "不能为空"))
                .doesNotThrowAnyException();
    }

    @Test
    void notEmpty_string_shouldThrowOnBlank() {
        assertThatThrownBy(() -> Guard.notEmpty("  ", "不能为空"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void notEmpty_collection_shouldThrowOnEmpty() {
        assertThatThrownBy(() -> Guard.notEmpty(Collections.emptyList(), "集合不能为空"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void isTrue_shouldThrowOnFalse() {
        assertThatThrownBy(() -> Guard.isTrue(false, "条件必须为真"))
                .isInstanceOf(DomainException.class)
                .hasMessage("条件必须为真");
    }

    @Test
    void greaterThan_shouldThrowWhenNotGreater() {
        assertThatThrownBy(() -> Guard.greaterThan(5, 10, "必须大于10"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void greaterThan_shouldPassWhenGreater() {
        assertThatCode(() -> Guard.greaterThan(15, 10, "必须大于10"))
                .doesNotThrowAnyException();
    }
}
