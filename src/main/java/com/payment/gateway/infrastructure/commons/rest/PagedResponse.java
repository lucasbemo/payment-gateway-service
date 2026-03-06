package com.payment.gateway.infrastructure.commons.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper for paginated response data.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private PageInfo pageInfo;

    public static <T> PagedResponse<T> of(List<T> content, PageInfo pageInfo) {
        return new PagedResponse<>(content, pageInfo);
    }

    public static <T> PagedResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return new PagedResponse<>(page.getContent(), PageInfo.of(page));
    }
}
