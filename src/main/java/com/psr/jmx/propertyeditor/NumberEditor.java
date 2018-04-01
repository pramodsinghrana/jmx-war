package com.psr.jmx.propertyeditor;

import java.beans.PropertyEditorSupport;

/**
 * Abstract Editor for Number
 *
 */
public abstract class NumberEditor extends PropertyEditorSupport {
  /**
   * Map the argument text into and {@link java.lang.Number}.
   */
  @Override
  public void setAsText(final String text) {
    Object value = PropertyEditors.isNull(text) ? null : decode(text);
    setValue(value);
  }

  /**
   * 
   * @see java.beans.PropertyEditorSupport#getJavaInitializationString()
   */
  @Override
  public String getJavaInitializationString() {
    Object value = getValue();
    return (value == null) ? "null" : value.toString();
  }

  /**
   * 
   * decode the text
   *
   * @param text
   * @return
   */
  protected abstract Object decode(String text);
}
