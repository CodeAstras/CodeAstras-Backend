package com.codeastras.backend.codeastras.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BannedWordValidator.class)
public @interface NotBanned {
    String message() default "This value is not allowed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default  {};
}
