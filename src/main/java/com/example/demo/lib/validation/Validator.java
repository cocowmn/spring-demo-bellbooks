package com.example.demo.lib.validation;

import java.util.List;

public interface Validator<ValidationType> {

    boolean isValid(ValidationType value);

    void validate(ValidationType value) throws ValidationFailedException;

    void validateAll(List<ValidationType> value) throws ValidationFailedException;

}
