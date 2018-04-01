package com.psr.jmx.control;

import java.beans.PropertyEditor;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.psr.jmx.context.JmxConfig;
import com.psr.jmx.model.MBeanData;
import com.psr.jmx.model.MBeanDomainData;
import com.psr.jmx.propertyeditor.PropertyEditors;

/**
 * Managing and Invoking MBean
 *
 * @author parmodsinghrana
 *
 */
public final class MBeanManager {

  /**
   * Get {@link MBeanData}
   * @param name A string representation of the object name
   * @return
   * @throws JMException
   */
  public static MBeanData getMBeanData(String name) throws JMException {
    ObjectName objName = new ObjectName(name);
    MBeanInfo info = getMBeanServer().getMBeanInfo(objName);
    return new MBeanData(objName, info);
  }

  /**
   * Gets the value of a specific attribute of a named MBean. The MBean is identified by its object name.
   * @param name A string representation of the object name
   * @param attrName A String specifying the name of the attribute to be retrieved
   * @return
   * @throws JMException
   */
  public static Object getMBeanAttributeObject(String name, String attrName) throws JMException {
    ObjectName objName = new ObjectName(name);
    return getMBeanServer().getAttribute(objName, attrName);
  }

  /**
   * Gets the value of a specific attribute of a named MBean. The MBean is identified by its object name.
   * @param name A string representation of the object name
   * @param attrName A String specifying the name of the attribute to be retrieved
   * @return
   * @throws JMException
   */
  public static String getMBeanAttribute(String name, String attrName) throws JMException {
    ObjectName objName = new ObjectName(name);
    String value = null;
    try {
      Object attr = getMBeanServer().getAttribute(objName, attrName);
      if (attr != null)
        value = attr.toString();
    }
    catch (JMException e) {
      LOG.error(e);
      value = e.getMessage();
    }
    return value;
  }

  /**
   * get {@link Iterator} of {@link MBeanDomainData}
   * @param filter a string pattern representing MBean
   * @return
   * @throws JMException
   */
  public static Iterator<MBeanDomainData> getDomainData(String filter) throws JMException {
    MBeanServer server = getMBeanServer();
    TreeMap<String, MBeanDomainData> domainData = new TreeMap<>();
    if (server != null) {
      ObjectName filterName = null;
      if (filter != null)
        filterName = new ObjectName(filter);
      Set<ObjectName> objectNames = server.queryNames(filterName, null);
      Iterator<ObjectName> objectNamesIter = objectNames.iterator();
      while (objectNamesIter.hasNext()) {
        ObjectName name = objectNamesIter.next();
        MBeanInfo info = server.getMBeanInfo(name);
        String domainName = name.getDomain();
        MBeanData mbeanData = new MBeanData(name, info);
        MBeanDomainData data = domainData.get(domainName);
        if (data == null) {
          data = new MBeanDomainData(domainName);
          domainData.put(domainName, data);
        }
        data.addData(mbeanData);
      }
    }
    return domainData.values().iterator();
  }

  /**
   * Get {@link AttrResultInfo}
   * @param name A string representation of the object name
   * @param attrInfo
   * @return
   * @throws JMException
   */
  public static AttrResultInfo getMBeanAttributeResultInfo(String name, MBeanAttributeInfo attrInfo)
    throws JMException {
    MBeanServer server = getMBeanServer();
    ObjectName objName = new ObjectName(name);
    String attrName = attrInfo.getName();
    String attrType = attrInfo.getType();
    Object value = null;
    Throwable throwable = null;
    if (attrInfo.isReadable() == true) {
      try {
        value = server.getAttribute(objName, attrName);
      }
      catch (Throwable t) {
        throwable = t;
      }
    }
    PropertyEditor editor = PropertyEditors
      .findEditor(attrType == null && attrInfo.isIs() ? Boolean.class.getName() : attrType);

    return new AttrResultInfo(attrName, editor, value, throwable);
  }

  /**
   * Get {@link AttributeList}
   * @param name A string representation of the object name
   * @param attributes
   * @return
   * @throws JMException
   */
  public static AttributeList setAttributes(String name, HashMap<String, String> attributes) throws JMException {
    MBeanServer server = getMBeanServer();
    ObjectName objName = new ObjectName(name);
    MBeanInfo info = server.getMBeanInfo(objName);
    MBeanAttributeInfo[] attributesInfo = info.getAttributes();
    AttributeList newAttributes = new AttributeList();
    for (int a = 0; a < attributesInfo.length; a++) {
      MBeanAttributeInfo attrInfo = attributesInfo[a];
      String attrName = attrInfo.getName();
      if (attributes.containsKey(attrName) == false)
        continue;
      String value = attributes.get(attrName);
      if (value.equals("null") && server.getAttribute(objName, attrName) == null) {
        LOG.trace("ignoring 'null' for " + attrName);
        continue;
      }
      String attrType = attrInfo.getType();
      Attribute attr = null;
      Object realValue = PropertyEditors
        .convertValue(attrType == null && attrInfo.isIs() ? Boolean.TYPE.getName() : attrType, value);
      attr = new Attribute(attrName, realValue);

      server.setAttribute(objName, attr);
      newAttributes.add(attr);
    }
    return newAttributes;
  }

  /**
   * 
   * invoke operation by method name, method name should be unique
   *
   * @param onameStr
   * @param operation
   * @param paramValues
   * @return
   * @throws OperationsException
   * @throws MBeanException
   * @throws ReflectionException
   */
  public static OpResultInfo invokeOpByName(String onameStr, String operation, String[] paramValues)
    throws OperationsException, MBeanException, ReflectionException {
    ObjectName oName = new ObjectName(onameStr);
    return invoke(oName, getMethodInfo(oName, operation), paramValues);
  }

  private static OpResultInfo invoke(ObjectName oName, MBeanOperationInfo operationInfo, String[] paramValues)
    throws InstanceNotFoundException, ReflectionException, MBeanException {
    MBeanParameterInfo[] paramInfos = operationInfo.getSignature();
    Object[] parameterValues = new Object[paramInfos.length];
    String[] signatureTypes = new String[paramInfos.length];

    for (int i = 0; i < paramInfos.length; i++) {
      MBeanParameterInfo paramInfo = paramInfos[i];
      signatureTypes[i] = paramInfo.getType();
      parameterValues[i] = PropertyEditors.convertValue(paramInfo.getType(), paramValues[i]);
    }
    Object opReturn = getMBeanServer().invoke(oName, operationInfo.getName(), parameterValues, signatureTypes);
    return new OpResultInfo(oName.toString(), operationInfo.getName(), signatureTypes, paramValues, opReturn);

  }

  /**
   * Find the operation info for a method
   *
   * @param oname The bean name
   * @param opName The operation name
   * @return the operation info for the specified operation
   */
  public static MBeanOperationInfo getMethodInfo(ObjectName oname, String opName) {
    return getMehodInfo(oname, opName, null);
  }

  /**
   * 
   * Find the operation info for a method, with name and signature type
   *
   * @param objectName
   * @param methodName
   * @param signatureType
   * @return
   */
  public static MBeanOperationInfo getMehodInfo(ObjectName objectName, String methodName, String[] signatureType) {
    boolean signatureExist = signatureType != null && signatureType.length > 0;
    MBeanInfo info = null;
    try {
      info = getMBeanServer().getMBeanInfo(objectName);
    }
    catch (Exception e) {
      LOG.info("Can't find metadata " + objectName.toString());
      return null;
    }
    MBeanOperationInfo[] attInfos = info.getOperations();
    outer_loop: for (int i = 0; i < attInfos.length; i++) {
      MBeanOperationInfo attInfo = attInfos[i];
      if (methodName.equals(attInfo.getName())) {
        if (signatureExist) {
          if (signatureType.length != attInfo.getSignature().length) {
            continue;
          }
          for (int j = 0; j < signatureType.length; j++) {
            MBeanParameterInfo paramInfo = attInfo.getSignature()[j];
            if (!signatureType[j].equals(paramInfo.getType())) {
              continue outer_loop;
            }
          }
        }
        return attInfo;
      }
    }
    return null;
  }

  /**
   * Registers the given bean with an appropriate MBean server.
   */
  public static void register(Object mbean, ObjectName objName) {
    try {
      getMBeanServer().registerMBean(mbean, objName);
    }
    catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
      throw new RuntimeException(e);
    }
  }

  public static void unRegister(ObjectName objName) {
    try {
      getMBeanServer().unregisterMBean(objName);
    }
    catch (MBeanRegistrationException | InstanceNotFoundException e) {
      throw new RuntimeException();
    }
  }

  /**
   * get {@link MBeanServer}
   * @return
   */
  public static MBeanServer getMBeanServer() {
    if (server == null) {
      synchronized (lock) {
        if (server == null) {
          try {
            long start = System.currentTimeMillis();
            List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
            if (servers.size() > 0) {
              String domain = JmxConfig.getProperty("jmx.server.domain");
              server = servers.get(0);
              if (domain != null && domain.trim().length() > 0) {
                for (MBeanServer mBeanServer : servers) {
                  if (mBeanServer.getDefaultDomain() != null && domain.equals(mBeanServer.getDefaultDomain())) {
                    server = mBeanServer;
                    break;
                  }
                }
              }
              if (LOG.isDebugEnabled()) {
                LOG.debug("Using existing MBeanServer " + (System.currentTimeMillis() - start));
              }
            }
            else {
              server = ManagementFactory.getPlatformMBeanServer();
              if (LOG.isDebugEnabled()) {
                LOG.debug("Creating MBeanServer" + (System.currentTimeMillis() - start));
              }
            }

          }
          catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    return server;
  }

  private MBeanManager() {
  }

  private static MBeanServer server;
  private static final Object lock = new Object();

  private static Log LOG = LogFactory.getLog(MBeanManager.class);
}
