package com.psr.jmx.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * A property editor for Class[].
 *
 */
public class ClassArrayEditor extends PropertyEditorSupport {
  /** Build a Class[] from a comma/whitespace seperated list of classes
   * @param text - the class name list
   */
  @Override
  public void setAsText(final String text) throws IllegalArgumentException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    StringTokenizer tokenizer = new StringTokenizer(text, ", \t\r\n");
    ArrayList<Class<?>> classes = new ArrayList<>();
    while (tokenizer.hasMoreTokens() == true) {
      String name = tokenizer.nextToken();
      try {
        Class<?> c = loader.loadClass(name);
        classes.add(c);
      }
      catch (ClassNotFoundException e) {
        throw new IllegalArgumentException("Failed to find class: " + name);
      }
    }

    Class<?>[] theValue = new Class[classes.size()];
    classes.toArray(theValue);
    setValue(theValue);
  }

  /**
   * @return a comma seperated string of the class array
   */
  @Override
  public String getAsText() {
    Class<?>[] theValue = (Class[]) getValue();
    StringBuffer text = new StringBuffer();
    int length = theValue == null ? 0 : theValue.length;
    for (int n = 0; n < length; n++) {
      text.append(theValue[n].getName());
      text.append(',');
    }
    // Remove the trailing ','
    text.setLength(text.length() - 1);
    return text.toString();
  }
}
