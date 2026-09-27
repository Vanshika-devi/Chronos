package com.chronos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleJobNotFound(JobNotFoundException ex){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND).body(Map.of(
                        "error",ex.getMessage()
                ));
    }
}