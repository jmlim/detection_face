package io.jmlim.faces.detection.model.object.api;

import lombok.Data;
import org.apache.http.HttpStatus;

import java.io.Serializable;
import java.util.List;

@Data
public class ApiResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private int status;
    private String message;
    private String errorCode;
    private Object data;
    private List<?> list;

    public ApiResponse() {
        this(HttpStatus.SC_OK, "success");
    }

    public ApiResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.errorCode = "0000";
    }

    public ApiResponse(int status, String message, String errorCode) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
    }
}