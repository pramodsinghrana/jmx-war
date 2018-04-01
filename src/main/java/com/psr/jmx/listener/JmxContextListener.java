package com.psr.jmx.listener;

import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.psr.jmx.context.JmxConfig;
import com.psr.jmx.control.MBeanManager;
import com.psr.jmx.server.ServerInfo;

/**
 * Application Lifecycle Listener implementation class JmxContextListener
 *
 */
public class JmxContextListener implements ServletContextListener {

  /**
   * @throws NotCompliantMBeanException 
   *
   */
  public JmxContextListener() throws NotCompliantMBeanException {
    jmxConfig = new JmxConfig();
    serverInfo = new ServerInfo();
  }

  /**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
  public void contextDestroyed(ServletContextEvent sce) {
    try {
      MBeanManager.unRegister(new ObjectName(jmxConfig.getName()));
    }
    catch (Exception e) {
      LOG.error("jmx config ", e);
    }
    try {
      MBeanManager.unRegister(new ObjectName(serverInfo.getName()));
    }
    catch (Exception e) {
      LOG.error("server info ", e);
    }
  }

  /**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
  public void contextInitialized(ServletContextEvent sce) {
    try {
      MBeanManager.register(jmxConfig, new ObjectName(jmxConfig.getName()));
      MBeanManager.register(serverInfo, new ObjectName(serverInfo.getName()));
    }
    catch (MalformedObjectNameException e) {
      throw new RuntimeException(e);
    }
  }

  JmxConfig jmxConfig;
  ServerInfo serverInfo;

  private static final Log LOG = LogFactory.getLog(JmxContextListener.class);
}
