package vn.duongw.springdatajpademo.exception;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data

public class ErrorResponse {

    private Date timestamp;
    private int status;
    private String path;
    private String error;
    private String message;

    public ErrorResponse() {
        this.timestamp = new Date();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = new Date();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

}
