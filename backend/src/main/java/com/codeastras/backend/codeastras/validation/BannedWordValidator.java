package com.codeastras.backend.codeastras.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;
import java.util.Set;

public class BannedWordValidator implements ConstraintValidator<NotBanned, String> {

    private Set<String> banned = Set.of("admin","support","root","system","postmaster");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx){
        if (value == null) return true;
        String lower = value.trim().toLowerCase(Locale.ROOT);
        for(String b : banned) {
            if(lower.equals(b) || lower.contains(b)){
                return false;
            }

        }
        return true;
    }

}
