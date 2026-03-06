package com.payment.gateway.infrastructure.commons.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("PageInfo Tests")
class PageInfoTest {

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should build PageInfo with all fields")
        void shouldBuildWithAllFields() {
            PageInfo pageInfo = PageInfo.builder()
                    .page(2)
                    .size(20)
                    .totalElements(100)
                    .totalPages(5)
                    .first(false)
                    .last(false)
                    .build();

            assertThat(pageInfo.getPage()).isEqualTo(2);
            assertThat(pageInfo.getSize()).isEqualTo(20);
            assertThat(pageInfo.getTotalElements()).isEqualTo(100);
            assertThat(pageInfo.getTotalPages()).isEqualTo(5);
            assertThat(pageInfo.isFirst()).isFalse();
            assertThat(pageInfo.isLast()).isFalse();
        }

        @Test
        @DisplayName("should build PageInfo for first page")
        void shouldBuildForFirstPage() {
            PageInfo pageInfo = PageInfo.builder()
                    .page(0)
                    .size(10)
                    .totalElements(50)
                    .totalPages(5)
                    .first(true)
                    .last(false)
                    .build();

            assertThat(pageInfo.isFirst()).isTrue();
            assertThat(pageInfo.isLast()).isFalse();
        }

        @Test
        @DisplayName("should build PageInfo for last page")
        void shouldBuildForLastPage() {
            PageInfo pageInfo = PageInfo.builder()
                    .page(4)
                    .size(10)
                    .totalElements(50)
                    .totalPages(5)
                    .first(false)
                    .last(true)
                    .build();

            assertThat(pageInfo.isFirst()).isFalse();
            assertThat(pageInfo.isLast()).isTrue();
        }
    }

    @Nested
    @DisplayName("of(Page)")
    class OfPageTests {

        @Test
        @DisplayName("should create PageInfo from Spring Data Page")
        void shouldCreateFromSpringDataPage() {
            List<String> content = List.of("a", "b", "c");
            Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 25);

            PageInfo pageInfo = PageInfo.of(page);

            assertThat(pageInfo.getPage()).isEqualTo(0);
            assertThat(pageInfo.getSize()).isEqualTo(10);
            assertThat(pageInfo.getTotalElements()).isEqualTo(25);
            assertThat(pageInfo.getTotalPages()).isEqualTo(3);
            assertThat(pageInfo.isFirst()).isTrue();
            assertThat(pageInfo.isLast()).isFalse();
        }

        @Test
        @DisplayName("should create PageInfo from middle page")
        void shouldCreateFromMiddlePage() {
            List<String> content = List.of("d", "e", "f");
            Page<String> page = new PageImpl<>(content, PageRequest.of(1, 3), 9);

            PageInfo pageInfo = PageInfo.of(page);

            assertThat(pageInfo.getPage()).isEqualTo(1);
            assertThat(pageInfo.getSize()).isEqualTo(3);
            assertThat(pageInfo.getTotalElements()).isEqualTo(9);
            assertThat(pageInfo.getTotalPages()).isEqualTo(3);
            assertThat(pageInfo.isFirst()).isFalse();
            assertThat(pageInfo.isLast()).isFalse();
        }

        @Test
        @DisplayName("should create PageInfo from last page")
        void shouldCreateFromLastPage() {
            List<String> content = List.of("g");
            Page<String> page = new PageImpl<>(content, PageRequest.of(2, 3), 7);

            PageInfo pageInfo = PageInfo.of(page);

            assertThat(pageInfo.getPage()).isEqualTo(2);
            assertThat(pageInfo.getTotalPages()).isEqualTo(3);
            assertThat(pageInfo.isFirst()).isFalse();
            assertThat(pageInfo.isLast()).isTrue();
        }

        @Test
        @DisplayName("should create PageInfo from single page result")
        void shouldCreateFromSinglePage() {
            List<String> content = List.of("only-item");
            Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 1);

            PageInfo pageInfo = PageInfo.of(page);

            assertThat(pageInfo.getPage()).isEqualTo(0);
            assertThat(pageInfo.getSize()).isEqualTo(10);
            assertThat(pageInfo.getTotalElements()).isEqualTo(1);
            assertThat(pageInfo.getTotalPages()).isEqualTo(1);
            assertThat(pageInfo.isFirst()).isTrue();
            assertThat(pageInfo.isLast()).isTrue();
        }

        @Test
        @DisplayName("should create PageInfo from empty page")
        void shouldCreateFromEmptyPage() {
            Page<String> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            PageInfo pageInfo = PageInfo.of(page);

            assertThat(pageInfo.getPage()).isEqualTo(0);
            assertThat(pageInfo.getTotalElements()).isEqualTo(0);
            assertThat(pageInfo.getTotalPages()).isEqualTo(0);
            assertThat(pageInfo.isFirst()).isTrue();
            assertThat(pageInfo.isLast()).isTrue();
        }

        @Test
        @DisplayName("should create PageInfo from mocked Page")
        @SuppressWarnings("unchecked")
        void shouldCreateFromMockedPage() {
            Page<Object> page = mock(Page.class);
            when(page.getNumber()).thenReturn(3);
            when(page.getSize()).thenReturn(25);
            when(page.getTotalElements()).thenReturn(200L);
            when(page.getTotalPages()).thenReturn(8);
            when(page.isFirst()).thenReturn(false);
            when(page.isLast()).thenReturn(false);

            PageInfo pageInfo = PageInfo.of(page);

            assertThat(pageInfo.getPage()).isEqualTo(3);
            assertThat(pageInfo.getSize()).isEqualTo(25);
            assertThat(pageInfo.getTotalElements()).isEqualTo(200);
            assertThat(pageInfo.getTotalPages()).isEqualTo(8);
            assertThat(pageInfo.isFirst()).isFalse();
            assertThat(pageInfo.isLast()).isFalse();
        }
    }
}
