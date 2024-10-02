package vn.duongw.springdatajpademo.dto.reponse;

public class ResponseError extends ResponseData {

    public ResponseError(int status, String message) {
        super(status, message);
    }
}
