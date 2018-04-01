package com.psr.jmx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Retains Parameter, Constructor and Field names and description
 * at runtime to make JMX instrumentation more readable.
 * 
 * @author parmodsinghrana
 *
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JmxParameter {

  /**
   * @return Parameter display name
   */
  String name();

  /**
   * @return Parameter description
   */
  String description() default "";

}
