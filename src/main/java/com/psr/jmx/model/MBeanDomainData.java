package com.psr.jmx.model;

import java.util.Arrays;
import java.util.TreeSet;

/**
 * The MBeanData for a given JMX domain name
 *
 * @author parmodsinghrana
 *
 */
public class MBeanDomainData {
  String domainName;
  TreeSet<MBeanData> domainData = new TreeSet<>();

  /** Creates a new instance of MBeanInfo */
  public MBeanDomainData(String domainName) {
    this.domainName = domainName;
  }

  public MBeanDomainData(String domainName, MBeanData[] data) {
    this.domainName = domainName;
    domainData.addAll(Arrays.asList(data));
  }

  @Override
  public int hashCode() {
    return domainName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || domainName.equals(((MBeanDomainData) obj).domainName);
  }

  public String getDomainName() {
    return domainName;
  }

  public MBeanData[] getData() {
    return domainData.toArray(new MBeanData[domainData.size()]);
  }

  public void addData(MBeanData data) {
    domainData.add(data);
  }

}
