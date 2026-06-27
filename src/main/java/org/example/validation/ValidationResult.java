package org.example.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable value object that accumulates validation errors.
 *
 * <pre>
 *   ValidationResult r = StudentValidator.validateGender(gender)
 *       .and(StudentValidator.validateScore("math_score", math))
 *       .and(StudentValidator.validateScore("reading_score", reading));
 *
 *   if (!r.isValid()) throw new ValidationException(r);
 * </pre>
 */
public final class ValidationResult {

    private final List<String> errors;

    private ValidationResult(List<String> errors) {
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    // ── factory ────────────────────────────────────────────────────────────

    public static ValidationResult ok() {
        return new ValidationResult(List.of());
    }

    public static ValidationResult of(String error) {
        if (error == null || error.isBlank()) {
            throw new IllegalArgumentException("error message must not be blank");
        }
        return new ValidationResult(List.of(error));
    }

    // ── combinator ─────────────────────────────────────────────────────────

    /** Returns a new result that contains errors from both this and {@code other}. */
    public ValidationResult and(ValidationResult other) {
        if (this.isValid() && other.isValid()) return ok();
        List<String> combined = new ArrayList<>(this.errors);
        combined.addAll(other.errors);
        return new ValidationResult(combined);
    }

    // ── query ──────────────────────────────────────────────────────────────

    public boolean isValid() {
        return errors.isEmpty();
    }

    /** All collected error messages in insertion order. */
    public List<String> getErrors() {
        return errors;
    }

    /** All errors joined by "; " — suitable for log output. */
    public String getErrorMessage() {
        return String.join("; ", errors);
    }

    // ── action ─────────────────────────────────────────────────────────────

    /** Throws {@link ValidationException} if this result is not valid. */
    public void throwIfInvalid() {
        if (!isValid()) throw new ValidationException(this);
    }

    @Override
    public String toString() {
        return isValid() ? "ValidationResult{OK}" : "ValidationResult{errors=" + errors + "}";
    }
}
