package ua.kpi.blindpig.common;

import java.util.List;

/** page is zero-based; totalElements is the total before pagination. */
public record PageResult<T>(List<T> content, long totalElements, int page, int size) {}
