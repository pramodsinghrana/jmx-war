package com.psr.jmx.propertyeditor;

import java.beans.PropertyEditorSupport;

/**
 *  A property editor for {@link java.lang.Character}.
 *
 */
public class ByteArrayEditor extends PropertyEditorSupport {

  @Override
  public void setAsText(final String text) {
    if (PropertyEditors.isNull(text)) {
      setValue(null);
      return;
    }
    if (text.length() != 1)
      throw new IllegalArgumentException("Too many (" + text.length() + ") characters: '" + text + "'");
    Object newValue = new Character(text.charAt(0));
    setValue(newValue);
  }
}
