package com.psr.jmx.propertyeditor;

/**
 * A property editor for {@link Short}.
 *
 */
public class ShortEditor extends NumberEditor {

  /**
   * @see NumberEditor#decode(java.lang.String)
   */
  @Override
  protected Object decode(String text) {
    return Short.decode(text);
  }
  
  /**
   * 
   * @see NumberEditor#getJavaInitializationString()
   */
  @Override
  public String getJavaInitializationString() {
    Object value = getValue();
    return (value == null) ? "null" : "((short)" + value + ")";
  }
}
