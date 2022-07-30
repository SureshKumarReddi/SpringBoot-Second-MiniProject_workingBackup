package com.suresh.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RegistrationExceptionHandler {

	@ExceptionHandler(value = RegistrationException.class)
	public ResponseEntity<AppError> RegistrationExceptionHandling(RegistrationException e) {
		
		AppError error = new AppError();
		error.setErrorCode("Registration Error");
		error.setErrorMsg("Internal Server Error");
		ResponseEntity.ok(error);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		
	}

}
