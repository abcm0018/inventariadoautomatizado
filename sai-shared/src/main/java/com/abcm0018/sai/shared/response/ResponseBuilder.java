package com.abcm0018.sai.shared.response;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResponseBuilder {
    private ResponseBuilder() {}

    public static <T> StandardResponse<T> with(HttpStatus status, boolean resultStatus, String info) {
        StandardResponse<T> response = new StandardResponse<>();
		SuccessResult result = SuccessResult.builder()
				.status(resultStatus)
				.httpCode(status.value())
				.info(info)
				.traceId(generateUuid())
				.updatedElements(0)
				.createdElements(0)
				.deletedElements(0)
				.build();
		response.setResult(result);
		return response;
    }

    public static <T> StandardResponse<T> with(HttpStatus status, boolean resultStatus, String info, T data) {
        StandardResponse<T> response = with(status, resultStatus, info);
        response.setData(data);
        return response;
    }

    public static <T> StandardResponse<T> withError(HttpStatus status, ResponseError error, String info) {
        StandardResponse<T> response = new StandardResponse<>();

		List<ResponseError> errors = new ArrayList<>();
		errors.add(error);

		ErrorResult result = ErrorResult.builder()
				.status(false)
				.httpCode(status.value())
				.info(info)
				.traceId(generateUuid())
				.errors(errors)
				.build();
		response.setResult(result);
		return response;
    }

    public static <T> StandardResponse<T> withErrors(HttpStatus status, List<ResponseError> errors, String info) {
        StandardResponse<T> response = new StandardResponse<>();

		ErrorResult result = ErrorResult.builder()
				.status(false)
				.httpCode(status.value())
				.info(info)
				.traceId(generateUuid())
				.errors(errors)
				.build();
		response.setResult(result);
		return response;
    }

    public static <T> StandardResponse<T> withUpdatedElements(HttpStatus status, boolean resultStatus, int numElements, String info) {
        StandardResponse<T> response = with(status, resultStatus, info);

		((SuccessResult) response.getResult()).setUpdatedElements(numElements);
        return response;
    }

    public static <T> StandardResponse<T> withUpdatedElements(HttpStatus status, boolean resultStatus, int numElements, String info, T data) {
        StandardResponse<T> response = with(status, resultStatus, info, data);
		((SuccessResult) response.getResult()).setUpdatedElements(numElements);
        return response;
    }

	public static <T> StandardResponse<T> withDeletedElements(HttpStatus status, boolean resultStatus, int numElements, String info) {
		StandardResponse<T> response = with(status, resultStatus, info);
		((SuccessResult) response.getResult()).setDeletedElements(numElements);
		return response;
	}

	public static <T> StandardResponse<T> withCreatedElements(HttpStatus status, boolean resultStatus, int numElements, String info, T data) {
		StandardResponse<T> response = with(status, resultStatus, info, data);
		((SuccessResult) response.getResult()).setCreatedElements(numElements);
		return response;
	}

	public static <T> StandardResponse<T> withCreatedElements(HttpStatus status, boolean resultStatus, int numElements, String info) {
		StandardResponse<T> response = with(status, resultStatus, info);
		((SuccessResult) response.getResult()).setCreatedElements(numElements);
		return response;
	}

    private static String getTraceId() {
        return RequestContextHolder.getRequestAttributes() != null
                && RequestContextHolder.getRequestAttributes().getAttribute("TRACE_ID", 0) != null
                ? (String) RequestContextHolder.getRequestAttributes().getAttribute("TRACE_ID", 0) : "NO_TRACE_ID";
    }

    private static String generateUuid() {
        return UUID.randomUUID().toString();
    }
}
