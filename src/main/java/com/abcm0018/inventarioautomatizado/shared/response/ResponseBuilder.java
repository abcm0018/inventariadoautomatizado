package com.abcm0018.inventarioautomatizado.shared.response;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResponseBuilder {
    private ResponseBuilder(){}
    public static <T> StandardResponse<T> with(HttpStatus status, boolean resultStatus, String info){
        StandardResponse<T> response = new StandardResponse<>();
        StandardResult result = new StandardResult();
        result.setStatus(resultStatus);
        result.setInfo(info);
        result.setHttpCode(status.value());
        result.setTraceId(getTracedIdV2());
        response.setResult(result);
        return response;
    }

    public static <T> StandardResponse<T> with(HttpStatus status, boolean resultStatus, String info, T data){
        StandardResponse<T> response = with(status, resultStatus, info);
        response.setData(data);
        return response;
    }

    public static <T> StandardResponse<T> withError(HttpStatus status, ResponseError error, String info){
        StandardResponse<T> response = new StandardResponse<>();
        StandardResult result = new StandardResult();
        result.setStatus(false);
        result.setInfo(info);
        result.setHttpCode(status.value());
        result.setTraceId(getTracedIdV2());
        List<ResponseError> errors = new ArrayList<>();
        errors.add(error);
        result.setErrors(errors);
        response.setResult(result);
        return response;
    }

    public static <T> StandardResponse<T> withErrors(HttpStatus status, List<ResponseError> errors, String info){
        StandardResponse<T> response = new StandardResponse<>();
        StandardResult result = new StandardResult();
        result.setStatus(false);
        result.setInfo(info);
        result.setHttpCode(status.value());
        result.setTraceId(getTracedIdV2());
        result.setErrors(errors);
        response.setResult(result);
        return response;
    }

    public static <T> StandardResponse<T> withUpdatedElements(HttpStatus status, boolean resultStatus, int numElements, String info){
        StandardResponse<T> response = with(status, resultStatus, info);
        response.getResult().setUpdatedElements(numElements);
        return response;
    }

    public static <T> StandardResponse<T> withUpdatedElements(HttpStatus status, boolean resultStatus, int numElements, String info, T data){
        StandardResponse<T> response = with(status, resultStatus, info, data);
        response.getResult().setUpdatedElements(numElements);
        return response;
    }

    private static String getTraceId(){
        return RequestContextHolder.getRequestAttributes() != null
                && RequestContextHolder.getRequestAttributes().getAttribute("TRACE_ID", 0) != null
                ? (String) RequestContextHolder.getRequestAttributes().getAttribute("TRACE_ID", 0) : "NO_TRACE_ID";
    }

    private static String getTracedIdV2(){
        return UUID.randomUUID().toString();
    }
}
