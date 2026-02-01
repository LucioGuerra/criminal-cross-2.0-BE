package org.athlium.auth.infrastructure.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark endpoints that require authentication.
 * Can specify required roles for authorization.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticated {
    
    /**
     * Required roles. If empty, any authenticated user is allowed.
     * If specified, user must have at least one of the roles.
     */
    String[] roles() default {};
    
    /**
     * If true, all specified roles are required.
     * If false (default), any of the roles is sufficient.
     */
    boolean requireAll() default false;
}
