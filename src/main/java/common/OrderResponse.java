package common;

import java.io.Serializable;

public class OrderResponse implements Serializable {
    private String title;
    private boolean isOrderConfirmed;

    public OrderResponse(String title, boolean isOrderConfirmed) {
        this.title = title;
        this.isOrderConfirmed = isOrderConfirmed;
    }

    public String getTitle() {
        return title;
    }

    public boolean isOrderConfirmed() {
        return isOrderConfirmed;
    }
}
