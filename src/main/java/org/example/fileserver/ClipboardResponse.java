package org.example.fileserver;

import java.util.List;

public class ClipboardResponse {

    private List<ClipboardItem> items;

    private Integer totalItems;

    private Integer totalItemsInCurrentPage;

    public List<ClipboardItem> getItems() {
        return items;
    }

    public void setItems(List<ClipboardItem> items) {
        this.items = items;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getTotalItemsInCurrentPage() {
        return totalItemsInCurrentPage;
    }

    public void setTotalItemsInCurrentPage(Integer totalItemsInCurrentPage) {
        this.totalItemsInCurrentPage = totalItemsInCurrentPage;
    }
}
