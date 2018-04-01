package com.psr.jmx.propertyeditor;

/**
 *  A property editor for {@link Integer}.
 *
 */
public class IntegerEditor extends NumberEditor {

  /**
   * @see NumberEditor#decode(java.lang.String)
   */
  @Override
  protected Object decode(String text) {
    return Integer.decode(text);
  }
}
