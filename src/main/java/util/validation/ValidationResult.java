package util.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationResult {
    private boolean valid = true;
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final Map<String, String> fieldErrors = new HashMap<>();

    public boolean isValid() {
        return valid;
    }

    public void addError(String error) {
        if (error != null && !error.trim().isEmpty()) {
            this.errors.add(error.trim());
            this.valid = false;
        }
    }

    public void addFieldError(String field, String error) {
        if (field != null && error != null) {
            this.fieldErrors.put(field.trim(), error.trim());
            this.valid = false;
        }
    }

    public void addWarning(String warning) {
        if (warning != null && !warning.trim().isEmpty()) {
            this.warnings.add(warning.trim());
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public String getFirstError() {
        if (!errors.isEmpty()) {
            return errors.get(0);
        }
        if (!fieldErrors.isEmpty()) {
            return fieldErrors.values().iterator().next();
        }
        return "";
    }
}
