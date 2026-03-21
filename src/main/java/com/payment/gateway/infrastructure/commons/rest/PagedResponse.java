package com.payment.gateway.infrastructure.commons.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response wrapper")
public class PagedResponse<T> {

    @Schema(description = "Page content items")
    private List<T> content;

    @Schema(description = "Pagination metadata")
    private PageInfo pageInfo;

    public static <T> PagedResponse<T> of(List<T> content, PageInfo pageInfo) {
        return new PagedResponse<>(content, pageInfo);
    }

    public static <T> PagedResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return new PagedResponse<>(page.getContent(), PageInfo.of(page));
    }
}
