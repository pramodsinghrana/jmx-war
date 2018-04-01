package com.psr.jmx.propertyeditor;

/**
 * A property editor for {@link Float}.
 *
 */
public class FloatEditor extends NumberEditor {

  /**
   * @see NumberEditor#decode(java.lang.String)
   */
  @Override
  protected Object decode(String text) {
    return Float.valueOf(text);
  }

  @Override
  public String getJavaInitializationString() {
    Object value = getValue();
    return (value == null) ? "null" : value.toString() + "F";
  }
}
