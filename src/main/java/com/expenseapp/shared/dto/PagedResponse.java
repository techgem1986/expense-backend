package com.expenseapp.shared.dto;

import java.util.List;

/**
 * A generic response wrapper for paginated data.
 *
 * @param <T> the type of elements in the response
 */
public class PagedResponse<T> {

    private final List<T> content;
    private final int totalElements;
    private final int totalPages;
    private final int number;
    private final int size;
    private final boolean first;
    private final boolean last;

    public PagedResponse(List<T> content, int totalElements, int totalPages, int number, int size, boolean first, boolean last) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.number = number;
        this.size = size;
        this.first = first;
        this.last = last;
    }

    public List<T> getContent() {
        return content;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }
}