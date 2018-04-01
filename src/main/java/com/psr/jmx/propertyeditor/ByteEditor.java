package com.psr.jmx.propertyeditor;

/**
 * A property editor for {@link Byte}
 *
 */
public class ByteEditor extends NumberEditor {

  /**
   * @see NumberEditor#decode(java.lang.String)
   */
  @Override
  protected Object decode(String text) {
    return Byte.decode(text);
  }

  /**
   * 
   * @see NumberEditor#getJavaInitializationString()
   */
  @Override
  public String getJavaInitializationString() {
    Object value = getValue();
    return (value == null) ? "null" : "((byte)" + value + ")";
  }

}
