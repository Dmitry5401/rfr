package io.student.rangiffler.jupiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter marker for a test user created by {@code UserExtension}.
 * Register the extension on the test class with {@code @ExtendWith(UserExtension.class)}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface User {
  int friends() default 0;

  int incomeInvitations() default 0;

  int outcomeInvitations() default 0;
}
