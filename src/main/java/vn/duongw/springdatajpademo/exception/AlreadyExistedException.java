package vn.duongw.springdatajpademo.exception;

public class AlreadyExistedException extends RuntimeException {
    public AlreadyExistedException(String message){
        super(message);
    }

}
