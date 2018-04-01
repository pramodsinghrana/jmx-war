package com.psr.jmx.context;

/**
 * TODO complete the class documentation
 *
 * @author parmodsinghrana
 *
 */
public interface JmxConfigMBean {

  /**
   * Reload JMX configuration
   */
  void reloadJmxConfig();
  /**
   * Searches for the property with the specified key in this property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * {@code null} if the property is not found.
   * 
   * @param key the property key
   * @return the value in this property list with the specified key value.
   */
  String getPropertyValue(String key);

  
  public static final String OBJECT_NAME_STR = "JMX.war:type=Configuration";
}
