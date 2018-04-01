package com.psr.jmx.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * A property editor for {@link org.w3c.dom.Document}.
 *
 */
public class DocumentEditor extends PropertyEditorSupport {

  /**
   * @see java.beans.PropertyEditorSupport#getAsText()
   */
  @Override
  public String getAsText() {
    return convertToString(getValue());
  }

  /**
   * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
   */
  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    setValue(convertToDocument(text));
  }

  /**
   * 
   * Convert {@link String} to {@link Document}
   *
   * @param text
   * @return
   */
  protected Document convertToDocument(String text) {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      StringReader sr = new StringReader(text);
      InputSource is = new InputSource(sr);
      return db.parse(is);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 
   * Convert {@link Document} to {@link String}
   *
   * @param text
   * @return
   */
  private String convertToString(Object value) {
    if (!(value instanceof Node)) {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer;
      Node node = (Node) value;
      try {
        transformer = tf.newTransformer();
        // below code to remove XML declaration
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.getBuffer().toString();
      }
      catch (TransformerException e) {
        throw new RuntimeException(e);
      }
    }
    return value != null ? value.toString() : "";
  }

}
