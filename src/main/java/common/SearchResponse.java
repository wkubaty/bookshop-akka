package common;

import java.io.Serializable;
import java.math.BigDecimal;

public class SearchResponse implements Serializable {
    private String title;
    private BigDecimal price;

    public SearchResponse(String title, BigDecimal price) {
        this.title = title;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
