package com.psr.jmx.propertyeditor;

import java.beans.PropertyEditorSupport;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 *  A property editor for {@link javax.management.ObjectName}.
 *
 */
public class ObjectNameEditor extends PropertyEditorSupport {

  @Override
  public void setAsText(final String text) {
    if (PropertyEditors.isNull(text)) {
      setValue(null);
      return;
    }
    try {
      setValue(new ObjectName(text));
    }
    catch (MalformedObjectNameException e) {
      setValue(null);
    }
  }
}
