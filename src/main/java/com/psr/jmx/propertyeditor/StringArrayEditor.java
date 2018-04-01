package com.psr.jmx.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * A property editor for String[]. The text format of a string array is a
 * comma or \n, \r separated list with \, representing an escaped comma to
 * include in the string element.
 *
 */
public class StringArrayEditor extends PropertyEditorSupport {
  Pattern commaDelim = Pattern.compile("','|[^,\r\n]+");

  static String[] parseList(String text) {
    ArrayList<String> list = new ArrayList<String>();
    StringBuffer tmp = new StringBuffer();
    for (int n = 0; n < text.length(); n++) {
      char c = text.charAt(n);
      switch (c) {
        case '\\':
          tmp.append(c);
          if (n < text.length() && text.charAt(n + 1) == ',') {
            tmp.setCharAt(tmp.length() - 1, ',');
            n++;
          }
          break;
        case ',':
        case '\n':
        case '\r':
          if (tmp.length() > 0)
            list.add(tmp.toString());
          tmp.setLength(0);
          break;
        default:
          tmp.append(c);
          break;
      }
    }
    if (tmp.length() > 0)
      list.add(tmp.toString());

    String[] x = new String[list.size()];
    list.toArray(x);
    return x;
  }

  /** Build a String[] from comma or eol seperated elements with a \,
   * representing a ',' to include in the current string element.
   *
   */
  @Override
  public void setAsText(final String text) {
    String[] theValue = parseList(text);
    setValue(theValue);
  }

  /**
   * @return a comma seperated string of the array elements
   */
  @Override
  public String getAsText() {
    String[] theValue = (String[]) getValue();
    StringBuffer text = new StringBuffer();
    int length = theValue == null ? 0 : theValue.length;
    for (int n = 0; n < length; n++) {
      String s = theValue[n];
      if (s.equals(","))
        text.append('\\');
      text.append(s);
      text.append(',');
    }
    // Remove the trailing ','
    if (text.length() > 0)
      text.setLength(text.length() - 1);
    return text.toString();
  }
}
