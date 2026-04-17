package com.ossanasur.cbconnect.utils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.Date;
@Data @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class DataResponse<T> {
    private Date timestamp;
    private boolean isSuccess;
    private String message;
    private int code;
    private T data;
    public static <T> DataResponse<T> success(T data) {
        return DataResponse.<T>builder().timestamp(new Date()).isSuccess(true).message("Success").code(200).data(data).build(); }
    public static <T> DataResponse<T> success(String message, T data) {
        return DataResponse.<T>builder().timestamp(new Date()).isSuccess(true).message(message).code(200).data(data).build(); }
    public static <T> DataResponse<T> created(String message, T data) {
        return DataResponse.<T>builder().timestamp(new Date()).isSuccess(true).message(message).code(201).data(data).build(); }
    public static <T> DataResponse<T> error(String message, int code) {
        return DataResponse.<T>builder().timestamp(new Date()).isSuccess(false).message(message).code(code).data(null).build(); }
    public static <T> DataResponse<T> error(String message) { return error(message, 500); }
}
