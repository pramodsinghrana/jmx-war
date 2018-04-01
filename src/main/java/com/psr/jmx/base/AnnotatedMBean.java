package com.psr.jmx.base;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

/**
 * Class providing implementation of annotated interface or class
 *
 * @author parmodsinghrana
 *
 */
public class AnnotatedMBean extends AbstractMBean {

  /**
   * get annotated interface Mbean
   * @param implementation 
   * @param mbeanInterface
   * @return
   * @throws NotCompliantMBeanException
   * 
   * @see {@link StandardMBean#StandardMBean(Object, Class)}
   */
  public static <T> AnnotatedMBean getAnnotatedInterfaceMXBean(T implementation, Class<T> mbeanInterface)
    throws NotCompliantMBeanException {
    return new AnnotatedMBean(implementation, mbeanInterface, true, true);
  }

  /**
   * get annotated class MBean 
   * @param implementation
   * @param mbeanInterface
   * @return
   * @throws NotCompliantMBeanException
   */
  public static <T> AnnotatedMBean getAnnotatedClassMXBean(T implementation, Class<T> mbeanInterface)
    throws NotCompliantMBeanException {
    return new AnnotatedMBean(implementation, mbeanInterface, true, false);
  }

  /**
   * get annotated interface Mbean
   * @param implementation 
   * @param mbeanInterface
   * @return
   * @throws NotCompliantMBeanException
   * 
   * @see {@link StandardMBean#StandardMBean(Object, Class)}
   */
  public static <T> AnnotatedMBean getAnnotatedInterfaceMBean(T implementation, Class<T> mbeanInterface)
    throws NotCompliantMBeanException {
    return new AnnotatedMBean(implementation, mbeanInterface, false, true);
  }

  /**
   * get annotated class MBean 
   * @param implementation
   * @param mbeanInterface
   * @return
   * @throws NotCompliantMBeanException
   */
  public static <T> AnnotatedMBean getAnnotatedClassMBean(T implementation, Class<T> mbeanInterface)
    throws NotCompliantMBeanException {
    return new AnnotatedMBean(implementation, mbeanInterface, false, false);
  }

  /**
  * 
  * 
  */
  private <T> AnnotatedMBean(T implementation, Class<T> mbeanInterface, boolean isMXBean, boolean annotatedInterface)
    throws NotCompliantMBeanException {
    super(implementation, mbeanInterface, isMXBean, annotatedInterface);
  }

  /**
   * (non-Javadoc)
   * @see com.psr.jmx.base.AbstractMBean#getName()
   */
  @Override
  public String getName() {
    return getClassOrInterface().getSimpleName();
  }
}
