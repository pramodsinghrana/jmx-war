package com.psr.jmx.propertyeditor;

import java.beans.*;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A collection of PropertyEditor utilities. Provides the same interface
 * as PropertyEditorManager plus more...
 *
 */
public class PropertyEditors {
  private static final String NULL = "null";

  /** Whether we handle nulls */
  private static boolean disableIsNull = false;

  /** Whether or not initialization of the editor search path has been done */
  private static boolean initialized = false;

  static {
    init();
  }

  /** 
   * Augment the PropertyEditorManager search path to incorporate the JMX
   * specific editors by appending the com.psr.jmx.util.propertyeditor package
   * to the PropertyEditorManager editor search path.
   */
  public synchronized static void init() {
    if (initialized == false) {
      AccessController.doPrivileged(Initialize.instance);
      initialized = true;
    }
  }

  /**
   * Whether a string is interpreted as the null value,
   * including the empty string.
   * 
   * @param value the value
   * @return true when the string has the value null
   */
  public static final boolean isNull(final String value) {
    return isNull(value, true, true);
  }

  /**
   * Whether a string is interpreted as the null value
   * 
   * @param value the value
  * @param trim whether to trim the string
  * @param empty whether to include the empty string as null
  * @return true when the string has the value null
  */
  public static final boolean isNull(final String value, final boolean trim, final boolean empty) {
    // For backwards compatibility
    if (disableIsNull)
      return false;
    // No value?
    if (value == null)
      return true;
    // Trim the text when requested
    String trimmed = trim ? value.trim() : value;
    // Is the empty string null?
    if (empty && trimmed.length() == 0)
      return true;
    // Just check it.
    return NULL.equalsIgnoreCase(trimmed);
  }

  /**
   * Will the standard editors return null from their
   * {@link PropertyEditor#setAsText(String)} method for non-primitive targets?
   *
   * @return True if nulls can be returned; false otherwise.
   */
  public static boolean isNullHandlingEnabled() {
    return !disableIsNull;
  }

  /**
   * Locate a value editor for a given target type.
   *
   * @param type   The class of the object to be edited.
   * @return       An editor for the given type or null if none was found.
   */
  public static PropertyEditor findEditor(final Class<?> type) {
    return PropertyEditorManager.findEditor(type);
  }

  /**
   * Locate a value editor for a given target type.
   *
   * @param typeName    The class name of the object to be edited.
   * @return            An editor for the given type or null if none was found.
   * @throws ClassNotFoundException when the class could not be found
   */
  public static PropertyEditor findEditor(final String typeName) {
    // see if it is a primitive type first
    Class<?> type = CLASS_NAME_TYPE_MAP.get(typeName);
    if (type == null) {
      // nope try look up
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try {
        type = loader.loadClass(typeName);
      }
      catch (ClassNotFoundException e) {
        return null;
      }
    }

    return PropertyEditorManager.findEditor(type);
  }

  /**
   * Get a value editor for a given target type.
   *
   * @param type    The class of the object to be edited.
   * @return        An editor for the given type.
   *
   * @throws RuntimeException   No editor was found.
   */
  public static PropertyEditor getEditor(final Class<?> type) {
    PropertyEditor editor = findEditor(type);
    if (editor == null) {
      throw new RuntimeException("No property editor for type: " + type);
    }

    return editor;
  }

  /**
   * Get a value editor for a given target type.
   *
   * @param typeName    The class name of the object to be edited.
   * @return            An editor for the given type.
   *
   * @throws RuntimeException   No editor was found.
   * @throws ClassNotFoundException when the class is not found
   */
  public static PropertyEditor getEditor(final String typeName) throws ClassNotFoundException {
    PropertyEditor editor = findEditor(typeName);
    if (editor == null) {
      throw new RuntimeException("No property editor for type: " + typeName);
    }

    return editor;
  }

  /**
   * Register an editor class to be used to editor values of a given target class.
   *
   * @param type         The class of the objetcs to be edited.
   * @param editorType   The class of the editor.
   */
  public static void registerEditor(final Class<?> type, final Class<?> editorType) {
    PropertyEditorManager.registerEditor(type, editorType);
  }

  /**
   * Register an editor class to be used to editor values of a given target class.
   *
   * @param typeName         The classname of the objetcs to be edited.
   * @param editorTypeName   The class of the editor.
   * @throws ClassNotFoundException when the class could not be found
   */
  public static void registerEditor(final String typeName, final String editorTypeName) throws ClassNotFoundException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Class<?> type = loader.loadClass(typeName);
    Class<?> editorType = loader.loadClass(editorTypeName);

    PropertyEditorManager.registerEditor(type, editorType);
  }

  /**
   *  Convert a string value into the true value for typeName using the
   * PropertyEditor associated with typeName.
   *
   * @param type the string representation of the value. This is passed to
   * the PropertyEditor.setAsText method.
   * @param value the fully qualified class name of the true value type
   * @return the PropertyEditor.getValue() result
   * @exception ClassNotFoundException thrown if the typeName class cannot
   *    be found
   * @exception IntrospectionException thrown if a PropertyEditor for typeName
   *    cannot be found
   */
  public static Object convertValue(String type, String value) {
    // see if it is a primitive type first
    Class<?> typeClass = CLASS_NAME_TYPE_MAP.get(type);
    if (typeClass == null) {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try {
        typeClass = loader.loadClass(type);
      }
      catch (ClassNotFoundException e) {
        return value;
      }
    }

    return convertValue(typeClass, value);
  }

  /**
   *  Convert a string value into the true value for typeName using the
   * PropertyEditor associated with typeName.
   *
   * @param clazz the type of value. This is passed to
   * the PropertyEditor.setAsText method.
   * @param value the fully qualified class name of the true value type
   * @return the PropertyEditor.getValue() result
   * @exception ClassNotFoundException thrown if the typeName class cannot
   *    be found
   * @exception IntrospectionException thrown if a PropertyEditor for typeName
   *    cannot be found
   */

  public static Object convertValue(Class<?> clazz, String value) {
    PropertyEditor editor = PropertyEditorManager.findEditor(clazz);
    if (editor == null) {
      return value;
    }
    editor.setAsText(value);
    return editor.getValue();
  }

  /**
   * This method takes the properties found in the given beanProps
   * to the bean using the property editor registered for the property.
   * Any property in beanProps that does not have an associated java bean
   * property will result in an IntrospectionException. The string property
   * values are converted to the true java bean property type using the
   * java bean PropertyEditor framework. If a property in beanProps does not
   * have a PropertyEditor registered it will be ignored.
   *
   * @param bean - the java bean instance to apply the properties to
   * @param beanProps - map of java bean property name to property value.
   * @throws IntrospectionException thrown on introspection of bean and if
   *    a property in beanProps does not map to a property of bean.
   */
  public static void mapJavaBeanProperties(Object bean, Properties beanProps) throws IntrospectionException {
    mapJavaBeanProperties(bean, beanProps, true);
  }

  /**
   * This method takes the properties found in the given beanProps
   * to the bean using the property editor registered for the property.
   * Any property in beanProps that does not have an associated java bean
   * property will result in an IntrospectionException. The string property
   * values are converted to the true java bean property type using the
   * java bean PropertyEditor framework. If a property in beanProps does not
   * have a PropertyEditor registered it will be ignored.
   *
   * @param bean - the java bean instance to apply the properties to
   * @param beanProps - map of java bean property name to property value.
   * @param isStrict - indicates if should throw exception if bean property can not
   * be matched.  True for yes, false for no.
   * @throws IntrospectionException thrown on introspection of bean and if
   *    a property in beanProps does not map to a property of bean.
   */
  public static void mapJavaBeanProperties(Object bean, Properties beanProps, boolean isStrict)
    throws IntrospectionException {

    HashMap<String, PropertyDescriptor> propertyMap = new HashMap<String, PropertyDescriptor>();
    BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
    PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
    for (int p = 0; p < props.length; p++) {
      String fieldName = props[p].getName();
      propertyMap.put(fieldName, props[p]);
    }

    boolean trace = LOG.isTraceEnabled();
    Iterator<?> keys = beanProps.keySet().iterator();
    if (trace)
      LOG.trace("Mapping properties for bean: " + bean);
    while (keys.hasNext()) {
      String name = (String) keys.next();
      String text = beanProps.getProperty(name);
      PropertyDescriptor pd = propertyMap.get(name);
      if (pd == null) {
        /*
         * Try the property name with the first char uppercased to handle a
         * property name like dLQMaxResent whose expected introspected property
         * name would be DLQMaxResent since the JavaBean Introspector would view
         * setDLQMaxResent as the setter for a DLQMaxResent property whose
         * Introspector.decapitalize() method would also return "DLQMaxResent".
         */
        if (name.length() > 1) {
          char first = name.charAt(0);
          String exName = Character.toUpperCase(first) + name.substring(1);
          pd = propertyMap.get(exName);

          // Be lenient and check the other way around, e.g. ServerName ->
          // serverName
          if (pd == null) {
            exName = Character.toLowerCase(first) + name.substring(1);
            pd = propertyMap.get(exName);
          }
        }

        if (pd == null) {
          if (isStrict) {
            String msg = "No property found for: " + name + " on JavaBean: " + bean;
            throw new IntrospectionException(msg);
          }
          else {
            // since is not strict, ignore that this property was not found
            continue;
          }
        }
      }
      Method setter = pd.getWriteMethod();
      if (trace)
        LOG.trace("Property editor found for: " + name + ", editor: " + pd + ", setter: " + setter);
      if (setter != null) {
        Class<?> ptype = pd.getPropertyType();
        PropertyEditor editor = PropertyEditorManager.findEditor(ptype);
        if (editor == null) {
          if (trace)
            LOG.trace("Failed to find property editor for: " + name);
        }
        try {
          editor.setAsText(text);
          Object args[] = { editor.getValue() };
          setter.invoke(bean, args);
        }
        catch (Exception e) {
          if (trace)
            LOG.trace("Failed to write property", e);
        }
      }
    }
  }

  /**
   * Gets the package names that will be searched for property editors.
   *
   * @return   The package names that will be searched for property editors.
   */
  public String[] getEditorSearchPath() {
    return PropertyEditorManager.getEditorSearchPath();
  }

  /**
   * Sets the package names that will be searched for property editors.
   *
   * @param path   The serach path.
   */
  public void setEditorSearchPath(final String[] path) {
    PropertyEditorManager.setEditorSearchPath(path);
  }

  private static class Initialize implements PrivilegedAction<Object> {
    static Initialize instance = new Initialize();

    @Override
    public Object run() {
      String[] currentPath = PropertyEditorManager.getEditorSearchPath();
      int length = currentPath != null ? currentPath.length : 0;
      String[] newPath = new String[length + 2];
      System.arraycopy(currentPath, 0, newPath, 2, length);
      // Put the JMX editor path first
      // The default editors are not very flexible
      newPath[0] = "com.psr.jmx.propertyeditor";
      newPath[1] = "com.psr.jmx.propertyeditor.propertyeditor";
      PropertyEditorManager.setEditorSearchPath(newPath);

      /*
       * Register the editor types that will not be found using the standard
       * class name to editor name algorithm. For example, the type String[] has
       * a name '[Ljava.lang.String;' which does not map to a XXXEditor name.
       */
      PropertyEditorManager.registerEditor(String[].class, StringArrayEditor.class);
      PropertyEditorManager.registerEditor(Class[].class, ClassArrayEditor.class);
      PropertyEditorManager.registerEditor(int[].class, IntArrayEditor.class);
      PropertyEditorManager.registerEditor(byte[].class, ByteArrayEditor.class);

      // adding char editor.
      PropertyEditorManager.registerEditor(Character.TYPE, CharacterEditor.class);

      try {
        if (System.getProperty("com.psr.jmx.propertyeditor.disablenull") != null)
          disableIsNull = true;
      }
      catch (Throwable ignored) {
        LOG.trace("Error retrieving system property com.psr.jmx.propertyeditor.disablenull", ignored);
      }
      return null;
    }
  }

  /** Primitive type name -> class map. */
  private static final Map<String, Class<?>> CLASS_NAME_TYPE_MAP = new HashMap<>();

  /** Setup the primitives map. */
  static {
    CLASS_NAME_TYPE_MAP.put(Boolean.TYPE.getName(), Boolean.TYPE);
    CLASS_NAME_TYPE_MAP.put(Byte.TYPE.getName(), Byte.TYPE);
    CLASS_NAME_TYPE_MAP.put(Short.TYPE.getName(), Short.TYPE);
    CLASS_NAME_TYPE_MAP.put(Integer.TYPE.getName(), Integer.TYPE);
    CLASS_NAME_TYPE_MAP.put(Character.TYPE.getName(), Character.TYPE);
    CLASS_NAME_TYPE_MAP.put(Long.TYPE.getName(), Long.TYPE);
    CLASS_NAME_TYPE_MAP.put(Float.TYPE.getName(), Float.TYPE);
    CLASS_NAME_TYPE_MAP.put(Double.TYPE.getName(), Double.TYPE);
    CLASS_NAME_TYPE_MAP.put(ObjectName.class.getSimpleName(), ObjectName.class);

  }

  private static Log LOG = LogFactory.getLog(PropertyEditors.class);
}
