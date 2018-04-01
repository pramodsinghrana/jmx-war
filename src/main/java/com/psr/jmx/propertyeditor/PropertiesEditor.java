package com.psr.jmx.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * A property editor for {@link java.util.Properties}. The text format of a string array is a
 * comma or \n, \r separated list with \, representing an escaped comma to
 * include in the string element.
 *
 */
public class PropertiesEditor extends PropertyEditorSupport {

  /** 
   * Build a {@link java.util.Properties} from 
   * Format of the string must be: <br/>
   * <pre>
   * &lt;props&gt;
   *  &nbsp;&lt;prop key="key.prop.name1"&gt;value1&lt;/prop&gt;
   *  &nbsp;&lt;prop key="key.prop.name2"&gt;value2&lt;/prop&gt;
   *  &nbsp;&lt;prop key="key.prop.name3"&gt;value3&lt;/prop&gt;
   * &lt;/prop&gt;
   * </pre>
   */
  @Override
  public void setAsText(final String text) {
    Properties theValue = new Properties();
    if (text != null && text.length() > 0) {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder;
      try {
        docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new InputSource(new StringReader(text)));
        NodeList nodeList = doc.getElementsByTagName(PROPS_TAG);
        for (int i = 0; i < nodeList.getLength(); i++) {
          Node node = nodeList.item(i);
          NodeList childNodeList = node.getChildNodes();
          for (int j = 0; j < childNodeList.getLength(); j++) {
            Node propNode = childNodeList.item(j);
            if (PROP_TAG.equals(propNode.getNodeName()) && propNode.hasAttributes()) {
              Attr attrKey = (Attr) propNode.getAttributes().getNamedItem(KEY_ATTRIBUTE);
              String value = propNode.getChildNodes().item(0).getNodeValue();
              theValue.setProperty(attrKey.getValue(), value == null ? "" : value);
            }
          }
        }
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    setValue(theValue);
  }

  /**
   * @return a string of pattern
   * <pre>
   * &lt;props&gt;
   *  &nbsp;&lt;prop key="key.prop.name1"&gt;value1&lt;/prop&gt;
   *  &nbsp;&lt;prop key="key.prop.name2"&gt;value2&lt;/prop&gt;
   *  &nbsp;&lt;prop key="key.prop.name3"&gt;value3&lt;/prop&gt;
   * &lt;/prop&gt;
   * </pre>
   */
  @Override
  public String getAsText() {
    Properties theValue = (Properties) getValue();
    StringBuilder textBuilder = new StringBuilder("<" + PROPS_TAG + ">");
    for (Enumeration<?> e = theValue.propertyNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      textBuilder.append("\n\t<").append(PROP_TAG).append(" ").append(KEY_ATTRIBUTE).append("=\"").append(key).append("\">");
      textBuilder.append(theValue.getProperty(key)).append("</prop>\n");
    }
    textBuilder.append("\n</").append(PROPS_TAG).append(">");
    return textBuilder.toString();
  }

  private static final String PROPS_TAG = "props";
  private static final String PROP_TAG = "prop";
  private static final String KEY_ATTRIBUTE = "key";

}
