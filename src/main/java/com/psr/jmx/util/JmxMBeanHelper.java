package com.psr.jmx.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

/**
 * Class helper
 *
 * @author parmodsinghrana
 *
 */
public final class JmxMBeanHelper {

  private JmxMBeanHelper() {
  }

  /**
   * Like Class.forName, but supports primitive types.
   * 
   * @param className class name - or can be a primitive type
   * @return Class for the given name
   * @throws ClassNotFoundException
   */
  public static Class<?> forName(String className) throws ClassNotFoundException {
    Class<?> primitiveClass = primitiveTypeToClassType.get(className);
    return primitiveClass != null ? primitiveClass : Class.forName(className);
  }

  /**
   * Returns the "getter" ("getFoo" or "isFoo") for the given property
   * 
   * @param clazz Class from which to acquire getter
   * @param propName acquire "getter" method corresponding to the given property
   * @return "getter" method corresponding to the given property
   */
  public static Method findGetterForProperty(Class<?> clazz, String propName) {
    String propertyName = propName.substring(0, 1).toUpperCase()
      + ((propName.length() == 1) ? "" : propName.substring(1, propName.length()));
    try {
      return clazz.getMethod("get" + propertyName, new Class<?>[0]);
    }
    catch (NoSuchMethodException e) {
      try {
        return clazz.getMethod("is" + propertyName, new Class<?>[0]);
      }
      catch (NoSuchMethodException npe) {
      }
    }
    throw new IllegalArgumentException("No such property: " + propertyName + " on " + clazz.getName());
  }

  public static Constructor<?> getConstructor(Class<?> clazz, MBeanConstructorInfo info) {
    try {
      return clazz.getConstructor(getParameterTypes(info.getSignature()));
    }
    catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }

  public static Method getMethod(Class<?> clazz, MBeanOperationInfo info) {
    try {
      return clazz.getMethod(info.getName(), getParameterTypes(info.getSignature()));
    }
    catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }
  
  @SuppressWarnings("unchecked")
  public static <T extends Annotation> T getAnnotation(Executable executable, Class<T> annotationType, int sequence) {
    Annotation[] annotations = executable.getParameterAnnotations()[sequence];
    for (Annotation annotation : annotations) {
      if (annotationType.isInstance(annotation)) {
        return (T) annotation;
      }
    }
    return null;
  }

  public static Class<?>[] getParameterTypes(MBeanParameterInfo[] paramInfos) {
    try {
      Class<?>[] paramTypes = new Class[paramInfos.length];
      int index = 0;
      for (MBeanParameterInfo paramInfo : paramInfos) {
        paramTypes[index++] = JmxMBeanHelper.forName(paramInfo.getType());
      }
      return paramTypes;
    }
    catch (ClassNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  private static Map<String, Class<?>> primitiveTypeToClassType = new HashMap<String, Class<?>>();
  static {
    primitiveTypeToClassType.put(Byte.TYPE.getName(), Byte.TYPE);
    primitiveTypeToClassType.put(Short.TYPE.getName(), Short.TYPE);
    primitiveTypeToClassType.put(Integer.TYPE.getName(), Integer.TYPE);
    primitiveTypeToClassType.put(Character.TYPE.getName(), Character.TYPE);
    primitiveTypeToClassType.put(Long.TYPE.getName(), Long.TYPE);
    primitiveTypeToClassType.put(Float.TYPE.getName(), Float.TYPE);
    primitiveTypeToClassType.put(Double.TYPE.getName(), Double.TYPE);
    primitiveTypeToClassType.put(Boolean.TYPE.getName(), Boolean.TYPE);
  }

}
