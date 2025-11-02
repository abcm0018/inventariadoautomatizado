package com.abcm0018.sai.shared.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.BindingResult;

import com.abcm0018.sai.shared.response.ResponseError;

public class ValidationErrorExtractor {

	private ValidationErrorExtractor() {
		throw new IllegalStateException("Utility class");
	}

	public static List<ResponseError> extract(BindingResult bindingResult) {
		List<ResponseError> errors = new ArrayList<>();

		// Errores de campos
		bindingResult.getFieldErrors().forEach(error ->
				errors.add(new ResponseError(400,
						String.format("Campo '%s': %s", error.getField(), error.getDefaultMessage())))
		);

		// Errores globales
		bindingResult.getGlobalErrors().forEach(error ->
				errors.add(new ResponseError(400, error.getDefaultMessage()))
		);

		return errors;
	}
}
