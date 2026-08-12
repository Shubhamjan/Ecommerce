package com.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<Map<String,Object>> handleExist(ResourceAlreadyExistException ex){
        Map<String,Object> m = Map.of("error","Conflict","message",ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(m);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(ResourceNotFoundException ex){
        Map<String,Object> m = Map.of("error","Not Found","message",ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(m);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(MethodArgumentNotValidException ex){
        Map<String,Object> m = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err->m.put(err.getField(),err.getDefaultMessage()));
        Map<String,Object> body = Map.of("error","Validation Field","details",m);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleAll(Exception ex){
        Map<String,Object> m = Map.of("error","Internal Server error","message",ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(m);
    }
}
