package com.psr.jmx.propertyeditor;

/**
 * A property editor for {@link Double}.
 *
 */
public class DoubleEditor extends NumberEditor {

  /**
   * @see NumberEditor#decode(java.lang.String)
   */
  @Override
  protected Object decode(String text) {
    return Double.valueOf(text);
  }

}
