package com.payment.gateway.infrastructure.commons.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BaseController Tests")
class BaseControllerTest {

    private TestController controller;

    @BeforeEach
    void setUp() {
        controller = new TestController();
    }

    /**
     * Concrete subclass for testing the abstract BaseController.
     */
    static class TestController extends BaseController {

        public <T> ResponseEntity<ApiResponse<T>> callOk(T data) {
            return ok(data);
        }

        public <T> ResponseEntity<ApiResponse<T>> callOkWithMessage(String message, T data) {
            return ok(message, data);
        }

        public <T> ResponseEntity<ApiResponse<T>> callCreated(T data) {
            return created(data);
        }

        public <T> ResponseEntity<ApiResponse<T>> callNoContent() {
            return noContent();
        }

        public <T> ResponseEntity<ApiResponse<PagedResponse<T>>> callOkPaged(PagedResponse<T> data) {
            return okPaged(data);
        }
    }

    @Nested
    @DisplayName("ok(T)")
    class OkTests {

        @Test
        @DisplayName("should return 200 with data wrapped in success response")
        void shouldReturnOkWithData() {
            String data = "test-data";

            ResponseEntity<ApiResponse<String>> response = controller.callOk(data);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getData()).isEqualTo("test-data");
            assertThat(response.getBody().getMessage()).isEqualTo("Success");
        }

        @Test
        @DisplayName("should return 200 with null data")
        void shouldReturnOkWithNullData() {
            ResponseEntity<ApiResponse<Object>> response = controller.callOk(null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getData()).isNull();
        }
    }

    @Nested
    @DisplayName("ok(String, T)")
    class OkWithMessageTests {

        @Test
        @DisplayName("should return 200 with custom message and data")
        void shouldReturnOkWithMessageAndData() {
            ResponseEntity<ApiResponse<Integer>> response = controller.callOkWithMessage("Found it", 42);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Found it");
            assertThat(response.getBody().getData()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("created(T)")
    class CreatedTests {

        @Test
        @DisplayName("should return 201 with data wrapped in success response")
        void shouldReturnCreatedWithData() {
            String data = "new-resource-id";

            ResponseEntity<ApiResponse<String>> response = controller.callCreated(data);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Created");
            assertThat(response.getBody().getData()).isEqualTo("new-resource-id");
        }
    }

    @Nested
    @DisplayName("noContent()")
    class NoContentTests {

        @Test
        @DisplayName("should return 204 with no body")
        void shouldReturnNoContent() {
            ResponseEntity<ApiResponse<Object>> response = controller.callNoContent();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();
        }
    }

    @Nested
    @DisplayName("okPaged(PagedResponse)")
    class OkPagedTests {

        @Test
        @DisplayName("should return 200 with paged response data")
        void shouldReturnOkWithPagedData() {
            PageInfo pageInfo = PageInfo.builder()
                    .page(0)
                    .size(10)
                    .totalElements(25)
                    .totalPages(3)
                    .first(true)
                    .last(false)
                    .build();
            PagedResponse<String> pagedResponse = PagedResponse.of(
                    List.of("item1", "item2", "item3"), pageInfo);

            ResponseEntity<ApiResponse<PagedResponse<String>>> response =
                    controller.callOkPaged(pagedResponse);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getData().getContent()).hasSize(3);
            assertThat(response.getBody().getData().getPageInfo().getTotalElements()).isEqualTo(25);
        }
    }
}
