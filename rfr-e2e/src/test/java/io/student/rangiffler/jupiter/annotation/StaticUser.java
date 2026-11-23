package io.student.rangiffler.jupiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter marker for a pooled static user resolved by {@code UsersQueueExtension}.
 * Register the extension on the test class with {@code @ExtendWith(UsersQueueExtension.class)}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface StaticUser {
}
