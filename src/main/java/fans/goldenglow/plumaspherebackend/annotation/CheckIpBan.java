package fans.goldenglow.plumaspherebackend.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Annotation to check if the IP address of the request is banned.
 * This annotation can be applied to methods in controllers to enforce IP ban checks.
 */
@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckIpBan {
}