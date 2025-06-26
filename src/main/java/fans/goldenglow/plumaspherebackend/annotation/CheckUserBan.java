package fans.goldenglow.plumaspherebackend.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Annotation to check if the user is banned.
 * This annotation can be applied to methods in controllers to enforce user ban checks.
 */
@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckUserBan {
}