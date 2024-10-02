package vn.duongw.springdatajpademo.utils.enumvalidator.gender;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import vn.duongw.springdatajpademo.enums.Gender;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenderValidator.class)
public @interface UserGender {
    Gender[] anyOf();

    String message() default "must be any of {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
