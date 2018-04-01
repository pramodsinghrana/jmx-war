package com.psr.jmx.propertyeditor;

/**
 * A property editor for {@link Long}
 *
 */
public class LongEditor extends NumberEditor {

  /**
   * @see NumberEditor#decode(java.lang.String)
   */
  @Override
  protected Object decode(String text) {
    return Long.decode(text);
  }

  /**
   * 
   * @see NumberEditor#getJavaInitializationString()
   */
  @Override
  public String getJavaInitializationString() {
    Object value = getValue();
    return (value == null) ? "null" : value.toString() + "L";
  }

}
