package com.orxor.micro_chat;

import java.util.Map;

public class FcmDto {
    private String to;
    private Map<String,String> data;

    public FcmDto(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
