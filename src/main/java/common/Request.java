package common;

import java.io.Serializable;

public class Request implements Serializable{
    private RequestType requestType;
    private String title;

    public Request(RequestType requestType, String title) {
        this.requestType = requestType;
        this.title = title;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
