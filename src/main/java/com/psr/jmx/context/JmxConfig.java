package com.psr.jmx.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.function.Supplier;

import javax.management.NotCompliantMBeanException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.psr.jmx.annotation.JmxDescription;
import com.psr.jmx.annotation.JmxParameter;
import com.psr.jmx.base.AbstractMBean;

/**
 * TODO complete the class documentation
 *
 * @author parmodsinghrana
 *
 */
@JmxDescription("Jmx war configuration Mbean")
public class JmxConfig extends AbstractMBean implements JmxConfigMBean {

  /**
   * TODO complete constructor documentation
   * @throws NotCompliantMBeanException 
   *
   */
  public JmxConfig() throws NotCompliantMBeanException {
    super(JmxConfigMBean.class);
  }
  
  /**
   * (non-Javadoc)
   * @see com.psr.jmx.base.util.AbstractMBean#getName()
   */
  @Override
  public String getName() {
    return OBJECT_NAME_STR;
  }
  
  /**
  * (non-Javadoc)
  * @see com.psr.jmx.context.JmxConfigMBean#reloadJmxConfig()
  */
  @JmxDescription("Reload the Jmx war configuration properties")
  public void reloadJmxConfig() {
    properties.clear();
    loadConfig();
  }

  private static void loadConfig() {
    URL fileUrl = JmxConfig.class.getResource("/context.properties");
    if (fileUrl == null) {
      LOG.error("context.properties resource not found");
      return;
    }
    try {
      properties.load(new FileInputStream(new File(fileUrl.getFile())));
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }

  /**
   * (non-Javadoc)
   * @see com.psr.jmx.context.JmxConfigMBean#getPropertyValue(java.lang.String)
   */
  @Override
  @JmxDescription("Searches for the property with the specified key in this property list")
  public String getPropertyValue(
    @JmxParameter(name = "key", description = "the property key") String key) {
    return getProperty(key);
  }

  /**
   * @see java.util.Properties#getProperty(String)
   * @param key
   * @return value
   */
  public static String getProperty(String key) {
    return properties.getProperty(key);
  }

  /**
   * @see java.util.Properties#getProperty(String, String)
   * @param the hashtable key
   * @param defaultValue a default value {@link Supplier}
   * @return
   */
  public static String getProperty(String key, Supplier<String> defaultValue) {
    String value = getProperty(key);
    return value == null ? defaultValue.get() : value;
  }

  /**
   * @see java.util.Properties#getProperty(String, String)
   * @param the hashtable key
   * @param defaultValue a default value
   * @return
   */
  public static String getProperty(String key, String defaultValue) {
    return getProperty(key, () -> defaultValue);
  }
  
  private static Properties properties = new Properties(System.getProperties());
  private static final Log LOG = LogFactory.getLog(JmxConfig.class);

  static {
    loadConfig();
  }

}
