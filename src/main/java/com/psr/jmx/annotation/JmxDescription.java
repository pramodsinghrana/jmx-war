package com.psr.jmx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Retains Class, Constructor, Method description
 * at runtime to make JMX instrumentation more readable.
 *
 * @author parmodsinghrana
 *
 */
@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JmxDescription {
  /**
   * 
   * @return JMX entity description
   */
  String value();
}
