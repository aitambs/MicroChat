package com.orxor.micro_chat;

public class ServerResponse {
    private boolean success;
    private String response;

    public ServerResponse(boolean success, String response) {
        this.success = success;
        this.response = response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
