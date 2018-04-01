package com.psr.jmx.model;

import javax.management.MBeanInfo;
import javax.management.ObjectName;

/**
 *
 * @author parmodsinghrana
 *
 */
public class MBeanData implements Comparable<MBeanData> {
  private ObjectName objectName;
  private MBeanInfo metaData;

  public MBeanData() {
  }

  /** Creates a new instance of MBeanInfo */
  public MBeanData(ObjectName objectName, MBeanInfo metaData) {
    this.objectName = objectName;
    this.metaData = metaData;
  }

  /** Getter for property objectName.
   * @return Value of property objectName.
   */
  public ObjectName getObjectName() {
    return objectName;
  }

  /** Setter for property objectName.
   * @param objectName New value of property objectName.
   */
  public void setObjectName(ObjectName objectName) {
    this.objectName = objectName;
  }

  /** Getter for property metaData.
   * @return Value of property metaData.
   */
  public MBeanInfo getMetaData() {
    return metaData;
  }

  /** Setter for property metaData.
   * @param metaData New value of property metaData.
   */
  public void setMetaData(MBeanInfo metaData) {
    this.metaData = metaData;
  }

  /**
   * @return The ObjectName.toString()
   */
  public String getName() {
    return objectName.toString();
  }

  /**
   * @return The canonical key properties string
   */
  public String getNameProperties() {
    return objectName.getCanonicalKeyPropertyListString();
  }

  /**
   * @return The MBeanInfo.getClassName() value
   */
  public String getClassName() {
    return metaData.getClassName();
  }

  /** 
   * Compares MBeanData based on the ObjectName domain name and canonical
   * key properties
   *
   * @param the MBeanData to compare against
   * @return < 0 if this is less than o, > 0 if this is greater than o,
   *    0 if equal.
   */
  @Override
  public int compareTo(MBeanData md) {
    String d1 = objectName.getDomain();
    String d2 = md.objectName.getDomain();
    int compare = d1.compareTo(d2);
    if (compare == 0) {
      String p1 = objectName.getCanonicalKeyPropertyListString();
      String p2 = md.objectName.getCanonicalKeyPropertyListString();
      compare = p1.compareTo(p2);
    }
    return compare;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof MBeanData))
      return false;
    return this == o || (this.compareTo((MBeanData) o) == 0);
  }
}
