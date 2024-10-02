package vn.duongw.springdatajpademo.utils.enumvalidator.gender;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.duongw.springdatajpademo.enums.Gender;

import java.util.Arrays;

public class GenderValidator implements ConstraintValidator<UserGender, Gender> {
    private Gender[] genders;

    @Override
    public void initialize(UserGender constraint) {
        this.genders = constraint.anyOf();
    }

    @Override
    public boolean isValid(Gender value, ConstraintValidatorContext context) {
        return value == null || Arrays.asList(genders).contains(value);
    }
}
