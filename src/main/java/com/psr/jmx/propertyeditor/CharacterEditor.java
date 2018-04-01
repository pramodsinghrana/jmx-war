package com.psr.jmx.propertyeditor;

import java.beans.PropertyEditorSupport;

/**
 *  A property editor for byte[].
 *
 */
public class CharacterEditor extends PropertyEditorSupport {
  /** Map the argument text into and Byte using Byte.decode.
   */
  @Override
  public void setAsText(final String text) {
    if (PropertyEditors.isNull(text, false, false)) {
      setValue(null);
      return;
    }
    Object newValue = text.getBytes();
    setValue(newValue);
  }
}
