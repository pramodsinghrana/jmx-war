package com.psr.jmx.propertyeditor;

/**
 * A property editor for {@link org.w3c.dom.Element}.
 *
 */
public class ElementEditor extends DocumentEditor {

  /**
   * Sets as an Element created by a String.
   */
  @Override
  public void setAsText(String text) {
    setValue(convertToDocument(text).getDocumentElement());
  }
}
