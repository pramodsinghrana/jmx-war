package com.psr.jmx.server;

/**
 * TODO complete the class documentation
 *
 * @author parmodsinghrana
 *
 */
public interface ServerInfoMBean {
  /** The default ObjectName */
  final String OBJECT_NAME_STR = "JMX.system:type=ServerInfo";

  // Attributes ----------------------------------------------------

  String getJavaVersion();

  String getJavaVendor();

  String getJavaVMName();

  String getJavaVMVersion();

  String getJavaVMVendor();

  String getOSName();

  String getOSVersion();

  String getOSArch();

  Integer getActiveThreadCount();

  Integer getActiveThreadGroupCount();

  /** @return <tt>Runtime.getRuntime().maxMemory()</tt> */
  Long getMaxMemory();

  Long getTotalMemory();

  Long getFreeMemory();

  /** @return <tt>Runtime.getRuntime().availableProcessors()</tt> */
  Integer getAvailableProcessors();

  /** @return InetAddress.getLocalHost().getHostName(); */
  String getHostName();

  /** @return InetAddress.getLocalHost().getHostAddress(); */
  String getHostAddress();

  // Operations ----------------------------------------------------

  /**
   * Return a listing of the thread pools.
   * @param fancy produce a text-based graph when true
   * @return the memory pools
   */
  String listMemoryPools(boolean fancy);

  /**
   * Return a listing of the active threads and thread groups,
   * and a full stack trace for every thread.
   * @return the thread dump
   */
  String listThreadDump();

  /**
   * Return a sort list of thread cpu utilization.
   * @return the cpu utilization
   */
  String listThreadCpuUtilization();

  /**
   * Display the java.lang.Package info for the pkgName
   * @param pkgName the package name
   * @return the package info
   */
  String displayPackageInfo(String pkgName);

}
