package org.athlium.bookings.presentation.dto;

import java.util.List;

public class BookingPageResponse {

    private List<BookingResponse> items;
    private int page;
    private int limit;
    private long total;

    public BookingPageResponse(List<BookingResponse> items, int page, int limit, long total) {
        this.items = items;
        this.page = page;
        this.limit = limit;
        this.total = total;
    }

    public List<BookingResponse> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }

    public long getTotal() {
        return total;
    }
}
