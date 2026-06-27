package org.example.validation;

/**
 * Unchecked exception thrown when a {@link ValidationResult} is not valid.
 * Callers do not need to declare it in throws clauses.
 */
public class ValidationException extends RuntimeException {

    private final ValidationResult result;

    public ValidationException(ValidationResult result) {
        super(result.getErrorMessage());
        this.result = result;
    }

    /** The full result that triggered this exception (contains all error messages). */
    public ValidationResult getValidationResult() {
        return result;
    }
}
