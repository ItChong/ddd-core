package com.chongstack.ddd.domain.model;

import com.chongstack.ddd.domain.specification.Specification;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpecificationTest {

    @Test
    void basicSpecification_shouldEvaluateCorrectly() {
        Specification<Integer> isPositive = n -> n > 0;
        Specification<Integer> isEven = n -> n % 2 == 0;

        assertThat(isPositive.isSatisfiedBy(5)).isTrue();
        assertThat(isPositive.isSatisfiedBy(-1)).isFalse();
        assertThat(isEven.isSatisfiedBy(4)).isTrue();
    }

    @Test
    void and_shouldCombineSpecifications() {
        Specification<Integer> isPositive = n -> n > 0;
        Specification<Integer> isEven = n -> n % 2 == 0;
        Specification<Integer> positiveAndEven = isPositive.and(isEven);

        assertThat(positiveAndEven.isSatisfiedBy(4)).isTrue();
        assertThat(positiveAndEven.isSatisfiedBy(3)).isFalse();
        assertThat(positiveAndEven.isSatisfiedBy(-2)).isFalse();
    }

    @Test
    void or_shouldCombineSpecifications() {
        Specification<Integer> isPositive = n -> n > 0;
        Specification<Integer> isEven = n -> n % 2 == 0;
        Specification<Integer> positiveOrEven = isPositive.or(isEven);

        assertThat(positiveOrEven.isSatisfiedBy(3)).isTrue();
        assertThat(positiveOrEven.isSatisfiedBy(-2)).isTrue();
        assertThat(positiveOrEven.isSatisfiedBy(-3)).isFalse();
    }

    @Test
    void not_shouldNegateSpecification() {
        Specification<Integer> isPositive = n -> n > 0;
        Specification<Integer> isNotPositive = isPositive.not();

        assertThat(isNotPositive.isSatisfiedBy(-1)).isTrue();
        assertThat(isNotPositive.isSatisfiedBy(1)).isFalse();
    }
}
