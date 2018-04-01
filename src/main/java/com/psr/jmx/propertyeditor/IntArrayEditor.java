package com.psr.jmx.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.StringTokenizer;

/**
 * A property editor for int[].
 *
 */
public class IntArrayEditor extends PropertyEditorSupport {
  /** Build a int[] from comma or eol seperated elements
   *
   */
  @Override
  public void setAsText(final String text) {
    StringTokenizer stok = new StringTokenizer(text, ",\r\n");
    int[] theValue = new int[stok.countTokens()];
    int i = 0;
    while (stok.hasMoreTokens()) {
      theValue[i++] = Integer.decode(stok.nextToken()).intValue();
    }
    setValue(theValue);
  }

  /**
   * @return a comma seperated string of the array elements
   */
  @Override
  public String getAsText() {
    int[] theValue = (int[]) getValue();
    StringBuffer text = new StringBuffer();
    int length = theValue == null ? 0 : theValue.length;
    for (int n = 0; n < length; n++) {
      if (n > 0)
        text.append(',');
      text.append(theValue[n]);
    }
    return text.toString();
  }
}
