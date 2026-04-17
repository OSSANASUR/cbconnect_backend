package com.ossanasur.cbconnect.utils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;
import java.util.Date; import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class PaginatedResponse<T> {
    private Date timestamp; private boolean isSuccess; private String message; private int code; private List<T> data;
    @Builder.Default private int currentPage = 0; @Builder.Default private int pageSize = 10; @Builder.Default private long totalElements = 0L;
    @Builder.Default private int totalPages = 1; @Builder.Default private boolean hasNext = false; @Builder.Default private boolean hasPrevious = false;
    public static <T> PaginatedResponse<T> fromPage(Page<T> page, String message) {
        return PaginatedResponse.<T>builder().timestamp(new Date()).isSuccess(true).message(message).code(200)
                .data(page.getContent()).currentPage(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious()).build();
    }
}
