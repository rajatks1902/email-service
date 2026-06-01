package com.gemalto.telecom.ota.email.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailResponse {

    @JsonProperty("success")
    private final boolean success;

    @JsonProperty("message")
    private final String message;

    public EmailResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage()  { return message; }
}
