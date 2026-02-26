/*
 * The MIT License
 * Copyright © 2026 Landeshauptstadt München | it@M
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.muenchen.oss.kfreimvgeai.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for REST controllers.
 * <p>
 * This class provides centralized exception handling for all REST API endpoints, allowing for consistent error responses and improved maintainability. It
 * handles specific exceptions and translates them into appropriate HTTP responses.
 *
 * @author felix.haala
 */
@RestControllerAdvice
@Slf4j
public class DefaultExceptionHandler {

    /**
     * Handles MethodArgumentTypeMismatchException for REST API endpoints.
     * <p>
     * This method is triggered when a method argument type mismatch occurs, such as when a request parameter cannot be converted to the expected type. It
     * returns a meaningful error response to the client, indicating the nature of the type mismatch.
     *
     * @param ex the MethodArgumentTypeMismatchException that was thrown
     * @return a ResponseEntity containing an error message and HTTP status 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid argument [name=%s, value=%s]".formatted(ex.getName(), ex.getValue());
        log.warn(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

}