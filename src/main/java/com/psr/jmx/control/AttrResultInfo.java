package com.psr.jmx.control;

import java.beans.PropertyEditor;

/**
 *  simple tuple of an mbean operation name, sigature and result.
 *
 * @author parmodsinghrana
 *
 */
public class AttrResultInfo {

  public AttrResultInfo(String name, PropertyEditor editor, Object result, Throwable throwable) {
    this.name = name;
    this.editor = editor;
    this.result = result;
    this.throwable = throwable;
  }

  public String getAsText() {
    if (throwable != null) {
      return throwable.toString();
    }
    if (result != null) {
      try {
        if (editor != null) {
          editor.setValue(result);
          return editor.getAsText();
        }
        else {
          return result.toString();
        }
      }
      catch (Exception e) {
        return "String representation of " + name + "unavailable";
      } // end of try-catch
    }
    return null;
  }
  
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the editor
   */
  public PropertyEditor getEditor() {
    return editor;
  }

  /**
   * @return the result
   */
  public Object getResult() {
    return result;
  }

  /**
   * @return the throwable
   */
  public Throwable getThrowable() {
    return throwable;
  }

  private String name;
  private PropertyEditor editor;
  private Object result;
  private Throwable throwable;
}
