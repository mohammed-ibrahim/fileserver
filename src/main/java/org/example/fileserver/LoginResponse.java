package org.example.fileserver;

public class LoginResponse {

    private Boolean success;

    private String token;

    public LoginResponse(Boolean success, String token) {
        this.success = success;
        this.token = token;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
