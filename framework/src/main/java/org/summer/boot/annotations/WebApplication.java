package org.summer.boot.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface WebApplication {
    String[] getScanBasePackages() default {};
}

