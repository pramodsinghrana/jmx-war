package com.psr.jmx.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import com.psr.jmx.annotation.JmxDescription;
import com.psr.jmx.annotation.JmxParameter;
import com.psr.jmx.util.JmxMBeanHelper;

/**
 * Base class for MBean implementations.
 * 
 * @author parmodsinghrana
 *
 */
public abstract class AbstractMBean extends StandardMBean {

  /**
   *
   * @param mbeanInterface
   * @throws NotCompliantMBeanException
   */
  protected <T> AbstractMBean(Class<T> mbeanInterface) throws NotCompliantMBeanException {
    this(mbeanInterface, false);
  }

  /**
  *
  * @param mbeanInterface
  * @param annotatedInterface
  * @throws NotCompliantMBeanException
  */
  protected <T> AbstractMBean(Class<T> mbeanInterface, boolean annotatedInterface) throws NotCompliantMBeanException {
    super(mbeanInterface);
    this.annotatedInterface = annotatedInterface;
    validate();
  }

  /**
   * TODO complete constructor documentation
   * @throws NotCompliantMBeanException 
   *
   */
  public <T> AbstractMBean(T implementation, Class<T> mbeanInterface, boolean isMXBean, boolean annotatedInterface)
    throws NotCompliantMBeanException {
    super(implementation, mbeanInterface, isMXBean);
    this.annotatedInterface = annotatedInterface;
    validate();
  }

  /**
   * get name of MBean
   * @return
   */
  public abstract String getName();

  /**
   * (non-Javadoc)
   * @see javax.management.StandardMBean#getParameterName(javax.management.MBeanOperationInfo, javax.management.MBeanParameterInfo, int)
   */
  @Override
  protected String getParameterName(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
    try {
      Method method = getClassOrInterface()
        .getMethod(op.getName(), JmxMBeanHelper.getParameterTypes(op.getSignature()));
      return getNameFromAnnotation(method, sequence, () -> super.getParameterName(op, param, sequence));
    }
    catch (NoSuchMethodException ex) {
      throw new IllegalStateException(ex);
    }
  }

  /**
   * (non-Javadoc)
   * @see javax.management.StandardMBean#getParameterName(javax.management.MBeanConstructorInfo, javax.management.MBeanParameterInfo, int)
   */
  @Override
  protected String getParameterName(MBeanConstructorInfo ctor, MBeanParameterInfo param, int sequence) {
    try {
      Constructor<?> constructor = getClassOrInterface()
        .getConstructor(JmxMBeanHelper.getParameterTypes(ctor.getSignature()));
      return getNameFromAnnotation(constructor, sequence, () -> super.getParameterName(ctor, param, sequence));
    }
    catch (NoSuchMethodException ex) {
      throw new IllegalStateException(ex);
    }
  }

  /**
   * (non-Javadoc)
   * @see javax.management.StandardMBean#getDescription(javax.management.MBeanInfo)
   */
  @Override
  protected String getDescription(MBeanInfo info) {
    return getDescriptionFromAnnotation(getClassOrInterface().getAnnotations(), () -> super.getDescription(info));
  }

  /**
   * (non-Javadoc)
   * @see javax.management.StandardMBean#getDescription(javax.management.MBeanAttributeInfo)
   */
  @Override
  protected String getDescription(MBeanAttributeInfo info) {
    Method method = JmxMBeanHelper.findGetterForProperty(getClassOrInterface(), info.getName());
    return getDescriptionFromAnnotation(method.getAnnotations(), () -> super.getDescription(info));
  }

  /**
   * (non-Javadoc)
   * @see javax.management.StandardMBean#getDescription(javax.management.MBeanConstructorInfo, javax.management.MBeanParameterInfo, int)
   */
  @Override
  protected String getDescription(MBeanConstructorInfo ctor, MBeanParameterInfo param, int sequence) {
    return getDescriptionFromAnnotation(
      JmxMBeanHelper.getConstructor(getClassOrInterface(), ctor),
      sequence,
      () -> super.getDescription(ctor, param, sequence));
  }

  /**
   * (non-Javadoc)
   * @see javax.management.StandardMBean#getDescription(javax.management.MBeanConstructorInfo)
   */
  @Override
  protected String getDescription(MBeanConstructorInfo info) {
    return getDescriptionFromAnnotation(
      JmxMBeanHelper.getConstructor(getClassOrInterface(), info).getAnnotations(),
      () -> super.getDescription(info));
  }

  /**
   * (non-Javadoc)
   * @see javax.management.StandardMBean#getDescription(javax.management.MBeanOperationInfo)
   */
  @Override
  protected String getDescription(MBeanOperationInfo info) {
    return getDescriptionFromAnnotation(
      JmxMBeanHelper.getMethod(getClassOrInterface(), info).getAnnotations(),
      () -> super.getDescription(info));
  }

  /**
   * (non-Javadoc)
   * @see javax.management.StandardMBean#getDescription(javax.management.MBeanOperationInfo, javax.management.MBeanParameterInfo, int)
   */
  @Override
  protected String getDescription(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
    return getDescriptionFromAnnotation(
      JmxMBeanHelper.getMethod(getClassOrInterface(), op),
      sequence,
      () -> super.getDescription(op, param, sequence));
  }

  protected void validate() throws NotCompliantMBeanException {
    for (Constructor<?> ctor : getClassOrInterface().getConstructors()) {
      validateParamAnnotations(ctor);
    }

    for (Method method : getClassOrInterface().getMethods()) {
      validateParamAnnotations(method);
    }

  }

  protected static String getDescriptionFromAnnotation(
    Executable executable,
    int sequence,
    Supplier<String> defaultDescSupplier) {
    JmxParameter jmxParameter = JmxMBeanHelper.getAnnotation(executable, JmxParameter.class, sequence);
    return jmxParameter == null ? defaultDescSupplier.get() : jmxParameter.name();
  }

  protected static String getNameFromAnnotation(
    Executable executable,
    int sequence,
    Supplier<String> defaultNameSupplier) {
    JmxParameter jmxParameter = JmxMBeanHelper.getAnnotation(executable, JmxParameter.class, sequence);
    return jmxParameter == null ? defaultNameSupplier.get() : jmxParameter.name();
  }

  protected static String getDescriptionFromAnnotation(Annotation[] anns, Supplier<String> defaultDesSupplier) {
    for (Annotation ann : anns) {
      if (ann instanceof JmxDescription) {
        return ((JmxDescription) ann).value();
      }
    }
    return defaultDesSupplier.get();
  }

  protected static void validateParamAnnotations(Executable executable) throws NotCompliantMBeanException {
    String name = executable.getName();
    Annotation[][] paramAnnotations = executable.getParameterAnnotations();
    int count = 0;
    for (Annotation[] annotations : paramAnnotations) {
      for (Annotation annotation : annotations) {
        if (annotation instanceof JmxParameter) {
          count++;
        }
      }
    }
    if(count == 0) {
      return;
    }
    if (executable.getParameterTypes().length != count) {
      throw new NotCompliantMBeanException("Number of JmxParameter annotation did not match number of parameters in "
        + (executable instanceof Constructor ? "constructor " : "") + name);
    }
  }

  protected Class<?> getClassOrInterface() {
    return annotatedInterface ? getMBeanInterface() : getImplementationClass();
  }

  private boolean annotatedInterface;
}
