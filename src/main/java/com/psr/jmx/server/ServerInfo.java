package com.psr.jmx.server;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.psr.jmx.annotation.JmxDescription;
import com.psr.jmx.annotation.JmxParameter;
import com.psr.jmx.base.AbstractMBean;

/**
 * information about server
 *
 * @author parmodsinghrana
 *
 */
public class ServerInfo extends AbstractMBean implements ServerInfoMBean {

  /**
   * Default constructor
   *
   * @throws NotCompliantMBeanException
   */
  @JmxDescription("provides a view of system information for the server in which it is deployed")
  public ServerInfo() throws NotCompliantMBeanException {
    super(ServerInfoMBean.class);
  }

  /**
   * (non-Javadoc)
   * @see com.psr.jmx.base.util.AbstractMBean#getName()
   */
  @Override
  public String getName() {
    return OBJECT_NAME_STR;
  }

  @Override
  public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
    StringBuilder sb = new StringBuilder("\r\nBasic JVM & OS info \r\n");
    sb.append("Java version: ").append(System.getProperty("java.version")).append(", ")
      .append(System.getProperty("java.vendor"));

    sb.append("\r\nJava Runtime: ").append(System.getProperty("java.runtime.name")).append(" (build ")
      .append(System.getProperty("java.runtime.version")).append(")");

    sb.append("\r\nJava VM: ").append(System.getProperty("java.vm.name"))
      .append(" " + System.getProperty("java.vm.version")).append(",").append(System.getProperty("java.vm.vendor"));

    sb.append("\n\rOS-System: ").append(System.getProperty("os.name")).append(" ")
      .append(System.getProperty("os.version")).append(",").append(System.getProperty("os.arch"));

    sb.append("\r\nVM arguments: ").append(getVMArguments());

    LOG.info(sb.toString());

    // Dump out the entire system properties
    if (LOG.isDebugEnabled()) {
      StringBuilder sysProp = new StringBuilder("\nFull System Properties Dump");
      for (Map.Entry<?, ?> entry : new TreeMap<Object, Object>(System.getProperties()).entrySet()) {
        sysProp.append("\n    ").append(entry.getKey()).append(':').append(entry.getValue());
      }
      LOG.debug(sysProp.toString());
    }
    try {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();

      Class<?> clazz = ThreadMXBean.class;
      Method method = clazz.getMethod("isObjectMonitorUsageSupported", NO_PARAMS_SIG);
      isObjectMonitorUsageSupported = (Boolean) method.invoke(threadMXBean, NO_PARAMS);

      method = clazz.getMethod("isSynchronizerUsageSupported", NO_PARAMS_SIG);
      isSynchronizerUsageSupported = (Boolean) method.invoke(threadMXBean, NO_PARAMS);

      this.findDeadlockedThreads = clazz.getMethod("findDeadlockedThreads", NO_PARAMS_SIG);
      method = clazz.getMethod("dumpAllThreads", new Class[] { Boolean.TYPE, Boolean.TYPE });
      this.getThreadInfoWithSyncInfo = clazz
        .getMethod("getThreadInfo", new Class[] { long[].class, Boolean.TYPE, Boolean.TYPE });

      clazz = ThreadInfo.class;
      clazz.getMethod("getLockInfo", NO_PARAMS_SIG);
      this.getLockedMonitors = clazz.getMethod("getLockedMonitors", NO_PARAMS_SIG);
      this.getLockedSynchronizers = clazz.getMethod("getLockedSynchronizers", NO_PARAMS_SIG);

      clazz = cl.loadClass("java.lang.management.LockInfo");
      clazz.getMethod("getClassName", NO_PARAMS_SIG);
      clazz.getMethod("getIdentityHashCode", NO_PARAMS_SIG);

      clazz = cl.loadClass("java.lang.management.MonitorInfo");
      clazz.getMethod("from", new Class[] { CompositeData.class });
      this.getLockedStackDepth = clazz.getMethod("getLockedStackDepth", NO_PARAMS_SIG);
      clazz.getMethod("getLockedStackFrame", NO_PARAMS_SIG);
    }
    catch (Exception e) {
      LOG.error("Cannot access platform ThreadMXBean", e);
    }

    return name == null ? new ObjectName(OBJECT_NAME_STR) : name;
  }

  @Override
  public void postRegister(Boolean registrationDone) {
    // empty
  }

  @Override
  public void preDeregister() throws Exception {
    // empty
  }

  @Override
  public void postDeregister() {
    // empty
  }

  @Override
  @JmxDescription("Java version")
  public String getJavaVersion() {
    return System.getProperty("java.version");
  }

  @Override
  @JmxDescription("Java vendor")
  public String getJavaVendor() {
    return System.getProperty("java.vendor");
  }

  @Override
  @JmxDescription("Java virtual Machine name")
  public String getJavaVMName() {
    return System.getProperty("java.vm.name");
  }

  @Override
  @JmxDescription("Java virtual Machine version")
  public String getJavaVMVersion() {
    return System.getProperty("java.vm.version");
  }

  @Override
  @JmxDescription("Java virtual machine vendor")
  public String getJavaVMVendor() {
    return System.getProperty("java.vm.vendor");
  }

  @Override
  @JmxDescription("Machine OS name")
  public String getOSName() {
    return System.getProperty("os.name");
  }

  @Override
  @JmxDescription("Machine OS version")
  public String getOSVersion() {
    return System.getProperty("os.version");
  }

  @Override
  @JmxDescription("OS Achitecture")
  public String getOSArch() {
    return System.getProperty("os.arch");
  }

  @Override
  @JmxDescription("Total memory")
  public Long getTotalMemory() {
    return new Long(Runtime.getRuntime().totalMemory());
  }

  @Override
  @JmxDescription("Free memory")
  public Long getFreeMemory() {
    return new Long(Runtime.getRuntime().freeMemory());
  }

  /**
   * Returns <tt>Runtime.getRuntime().maxMemory()<tt> on 
   */
  @Override
  @JmxDescription("Maximum memory available")
  public Long getMaxMemory() {
    try {
      Runtime rt = Runtime.getRuntime();
      Method m = rt.getClass().getMethod("maxMemory", NO_PARAMS_SIG);
      return (Long) m.invoke(rt, NO_PARAMS);
    }
    catch (Exception e) {
      LOG.error("Operation failed", e);
    }
    return new Long(-1);
  }

  /**
   * Returns <tt>Runtime.getRuntime().availableProcessors()</tt> on 
   */
  @Override
  @JmxDescription("number of processor available")
  public Integer getAvailableProcessors() {
    try {
      Runtime rt = Runtime.getRuntime();
      Method m = rt.getClass().getMethod("availableProcessors", NO_PARAMS_SIG);
      return (Integer) m.invoke(rt, NO_PARAMS);
    }
    catch (Exception e) {
      LOG.error("Operation failed", e);
    }
    return new Integer(-1);
  }

  /**
   * Returns InetAddress.getLocalHost().getHostName();
   */
  @Override
  @JmxDescription("Server hostname")
  public String getHostName() {
    if (hostName == null) {
      try {
        hostName = java.net.InetAddress.getLocalHost().getHostName();
      }
      catch (java.net.UnknownHostException e) {
        LOG.error("Error looking up local hostname", e);
        hostName = "<unknown>";
      }
    }

    return hostName;
  }

  /**
   * Returns InetAddress.getLocalHost().getHostAddress();
   */
  @Override
  @JmxDescription("Server host IP address")
  public String getHostAddress() {
    if (hostAddress == null) {
      try {
        hostAddress = java.net.InetAddress.getLocalHost().getHostAddress();
      }
      catch (java.net.UnknownHostException e) {
        LOG.error("Error looking up local address", e);
        hostAddress = "<unknown>";
      }
    }

    return hostAddress;
  }

  /**
   * Return a listing of the thread pools.
   * 
   * @param fancy produce a text-based graph when true
   */
  @Override
  @JmxDescription("Return a listing of the thread pools")
  public String listMemoryPools(
    @JmxParameter(name = "fancy", description = "produce a text-based graph when true") boolean fancy) {
    StringBuffer sbuf = new StringBuffer(4196);

    // get the pools
    List<MemoryPoolMXBean> poolList = ManagementFactory.getMemoryPoolMXBeans();
    sbuf.append("<b>Total Memory Pools:</b> ").append(poolList.size());
    sbuf.append("<blockquote>");
    for (MemoryPoolMXBean pool : poolList) {
      // MemoryPoolMXBean instance
      String name = pool.getName();
      // enum MemoryType
      MemoryType type = pool.getType();
      sbuf.append("<b>Pool: ").append(name);
      sbuf.append("</b> (").append(type).append(")");

      // PeakUsage/CurrentUsage
      MemoryUsage peakUsage = pool.getPeakUsage();
      MemoryUsage usage = pool.getUsage();

      sbuf.append("<blockquote>");
      if (usage != null && peakUsage != null) {
        Long init = peakUsage.getInit();
        Long used = peakUsage.getUsed();
        Long committed = peakUsage.getCommitted();
        Long max = peakUsage.getMax();

        sbuf.append("Peak Usage    : ");
        sbuf.append("init:").append(init);
        sbuf.append(", used:").append(used);
        sbuf.append(", committed:").append(committed);
        sbuf.append(", max:").append(max);
        sbuf.append("<br/>");

        init = usage.getInit();
        used = usage.getUsed();
        committed = usage.getCommitted();
        max = usage.getMax();

        sbuf.append("Current Usage : ");
        sbuf.append("init:").append(init);
        sbuf.append(", used:").append(used);
        sbuf.append(", committed:").append(committed);
        sbuf.append(", max:").append(max);

        if (fancy) {
          TextGraphHelper.poolUsage(sbuf, used.longValue(), committed.longValue(), max.longValue());
        }
      }
      else {
        sbuf.append("Memory pool NOT valid!");
      }
      sbuf.append("</blockquote><br/>");
    }

    return sbuf.toString();
  }

  @Override
  @JmxDescription("number of active threads")
  public Integer getActiveThreadCount() {
    return new Integer(getRootThreadGroup().activeCount());
  }

  @Override
  @JmxDescription("number of active thread groups")
  public Integer getActiveThreadGroupCount() {
    return new Integer(getRootThreadGroup().activeGroupCount());
  }

  /**
   * Return a listing of the active threads and thread groups.
   */
  @Override
  @JmxDescription("Return a listing of the active threads and thread groups")
  public String listThreadDump() {
    ThreadGroup root = getRootThreadGroup();

    // Count the threads/groups during our traversal
    // rather than use the often inaccurate ThreadGroup
    // activeCount() and activeGroupCount()
    ThreadGroupCount count = new ThreadGroupCount();

    StringBuffer rc = new StringBuffer();

    // Find deadlocks, if there're any first, so that they're visible first
    // thing
    findDeadlockedThreads(rc);

    // Traverse thread dump
    getThreadGroupInfo(root, count, rc);

    // Attach counters
    String threadDump = new StringBuilder("<b>Total Threads:</b> ").append(count.threads).append("<br/>")
      .append("<b>Total Thread Groups:</b> ").append(count.groups).append("<br/>").append("<b>Timestamp:</b> ")
      .append(dateFormat.format(new Date())).append("<br/>").append(rc).toString();

    return threadDump;
  }

  /**
   * Return a listing of the active threads and thread groups.
   */
  @Override
  @JmxDescription("Return a listing of the active threads and thread groups cpu utilization")
  public String listThreadCpuUtilization() {
    Set<ThreadCPU> threads = getThreadCpuUtilization();

    if (threads == null) {
      return ("Thread cpu utilization requires J2SE5+");
    }
    else {
      long totalCPU = 0;
      StringBuffer buffer = new StringBuffer();
      buffer.append("<table><tr><th>Thread Name</th><th>CPU (milliseconds)</th></tr>");
      Iterator<ThreadCPU> threadItr = threads.iterator();
      while (threadItr.hasNext()) {
        ThreadCPU thread = threadItr.next();
        buffer.append("<tr><td>").append(thread.name).append("</td><td>");
        buffer.append(thread.cpuTime).append("</td></tr>");
        totalCPU += thread.cpuTime;
      }
      buffer.append("<tr><td>&nbsp;</td><td>&nbsp;</td></tr><tr><td>Total</td><td>");
      buffer.append(totalCPU).append("</td></tr></table>");
      return buffer.toString();
    }
  }

  /**
   * Get the Thread cpu utilization
   * 
   * @return an ordered 
   */
  private Set<ThreadCPU> getThreadCpuUtilization() {
    TreeSet<ThreadCPU> result = new TreeSet<ThreadCPU>();

    long[] threads = threadMXBean.getAllThreadIds();
    for (int i = 0; i < threads.length; ++i) {
      Long id = new Long(threads[i]);
      Long cpuTime = threadMXBean.getThreadCpuTime(id);
      ThreadInfo threadInfo = threadMXBean.getThreadInfo(id, ZERO);
      if (threadInfo != null) {
        String name = threadInfo.getThreadName();
        result.add(new ThreadCPU(name, cpuTime.longValue()));
      }
    }
    return result;
  }

  /*
   * Traverse to the root thread group
   */
  private ThreadGroup getRootThreadGroup() {
    ThreadGroup group = Thread.currentThread().getThreadGroup();
    while (group.getParent() != null) {
      group = group.getParent();
    }

    return group;
  }

  /*
   * Recurse inside ThreadGroups to create the thread dump
   */
  private void getThreadGroupInfo(ThreadGroup group, ThreadGroupCount count, StringBuffer rc) {
    if (isObjectMonitorUsageSupported || isSynchronizerUsageSupported) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "Generate a thread dump [show monitors = " + isObjectMonitorUsageSupported + ", show ownable synchronizers = "
            + isSynchronizerUsageSupported + "]");
      }
      getThreadGroupInfoWithLocks(group, count, rc);
    }
    else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Generate a thread dump without locks.");
      }
      getThreadGroupInfoWithoutLocks(group, count, rc);
    }
  }

  private void getThreadGroupInfoWithoutLocks(ThreadGroup group, ThreadGroupCount count, StringBuffer rc) {
    // Visit one more group
    count.groups++;

    rc.append("<br/><b>");
    rc.append("Thread Group: " + group.getName());
    rc.append("</b> : ");
    rc.append("max priority:" + group.getMaxPriority() + ", demon:" + group.isDaemon());

    rc.append("<blockquote>");
    Thread threads[] = new Thread[group.activeCount()];
    group.enumerate(threads, false);
    for (int i = 0; i < threads.length && threads[i] != null; i++) {
      // Visit one more thread
      count.threads++;

      rc.append("<b>");
      rc.append("Thread: " + threads[i].getName());
      rc.append("</b> : ");
      rc.append("priority:" + threads[i].getPriority() + ", demon:" + threads[i].isDaemon() + ", ");
      outputThreadMXBeanInfo(rc, threads[i]);
    }

    ThreadGroup groups[] = new ThreadGroup[group.activeGroupCount()];
    group.enumerate(groups, false);
    for (int i = 0; i < groups.length && groups[i] != null; i++) {
      getThreadGroupInfoWithoutLocks(groups[i], count, rc);
    }
    rc.append("</blockquote>");
  }

  private void getThreadGroupInfoWithLocks(ThreadGroup group, ThreadGroupCount count, StringBuffer rc) {
    // Visit one more group
    count.groups++;

    rc.append("<br/><b>");
    rc.append("Thread Group: " + group.getName());
    rc.append("</b> : ");
    rc.append("max priority:" + group.getMaxPriority() + ", demon:" + group.isDaemon());

    rc.append("<blockquote>");
    Thread threads[] = new Thread[group.activeCount()];
    group.enumerate(threads, false);

    long[] idsTmp = new long[threads.length];
    int numberNonNullThreads = 0;
    for (int i = 0; i < threads.length && threads[i] != null; i++) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Adding " + threads[i] + " with id=" + threads[i].getId());
      }
      idsTmp[i] = threads[i].getId();
      numberNonNullThreads++;
    }

    long[] ids = new long[numberNonNullThreads];
    System.arraycopy(idsTmp, 0, ids, 0, numberNonNullThreads);

    if (LOG.isTraceEnabled()) {
      LOG.trace("List of ids after trimming " + Arrays.toString(ids));
    }

    try {
      ThreadInfo[] infos = (ThreadInfo[]) getThreadInfoWithSyncInfo
        .invoke(threadMXBean, new Object[] { ids, isObjectMonitorUsageSupported, isSynchronizerUsageSupported });

      for (int i = 0; i < infos.length && threads[i] != null; i++) {
        // Visit one more thread
        count.threads++;

        rc.append("<b>");
        rc.append("Thread: " + infos[i].getThreadName());
        rc.append("</b> : ");
        rc.append("priority:" + threads[i].getPriority() + ", demon:" + threads[i].isDaemon() + ", ");
        // Output extra info
        outputThreadMXBeanInfo(rc, infos[i]);
      }

      ThreadGroup groups[] = new ThreadGroup[group.activeGroupCount()];
      group.enumerate(groups, false);
      for (int i = 0; i < groups.length && groups[i] != null; i++) {
        getThreadGroupInfoWithLocks(groups[i], count, rc);
      }
    }
    catch (Exception ignore) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Exception to be ignored", ignore);
      }
    }

    rc.append("</blockquote>");
  }

  /*
   * Complete the output of thread info, with optional stuff when running under
   * change line.
   */
  private void outputThreadMXBeanInfo(StringBuffer sbuf, Thread thread) {
    // Get the threadId
    Long threadId = thread.getId();

    // Get the ThreadInfo object for that threadId, max StackTraceElement depth
    ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId, new Integer(Integer.MAX_VALUE));

    outputWithoutMonitorThreadMXBeanInfo(sbuf, threadInfo);
  }

  /*
   * Complete the output of thread info, with optional stuff when running
   * without object monitor usage and object synchronizer usage capabilities, or
   * just change line.
   */
  private void outputWithoutMonitorThreadMXBeanInfo(StringBuffer sbuf, ThreadInfo threadInfo) {
    // JBAS-3838, thread might not be alive
    if (threadInfo != null) {
      // get misc info from ThreadInfo
      Thread.State threadState = threadInfo.getThreadState(); // enum
      String lockName = threadInfo.getLockName();
      StackTraceElement[] stackTrace = threadInfo.getStackTrace();

      Long threadId = threadInfo.getThreadId();
      sbuf.append("threadId:").append(threadId);
      sbuf.append(", threadState:").append(threadState);
      sbuf.append("<br/>");
      if (stackTrace.length > 0) {
        sbuf.append("<blockquote>");

        printLockName(sbuf, "waiting on", lockName);

        for (int i = 0; i < stackTrace.length; i++) {
          sbuf.append(stackTrace[i]).append("<br/>");
        }
        sbuf.append("</blockquote>");
      }
    }
    else {
      sbuf.append("<br/>");
    }
  }

  /*
   * Complete the output of thread info, with optional stuff
   */
  private void outputThreadMXBeanInfo(StringBuffer sbuf, ThreadInfo threadInfo) throws Exception {

    if (threadInfo != null) {
      // get the threadId
      Long threadId = threadInfo.getThreadId();
      sbuf.append("threadId:").append(threadId);

      // get misc info from ThreadInfo
      Thread.State threadState = threadInfo.getThreadState(); // enum
      String lockName = threadInfo.getLockName();
      StackTraceElement[] stackTrace = threadInfo.getStackTrace();
      Object[] monitors = (Object[]) getLockedMonitors.invoke(threadInfo, NO_PARAMS);

      sbuf.append(", threadState:").append(threadState);
      sbuf.append("<br/>");
      if (stackTrace.length > 0) {
        sbuf.append("<blockquote>");

        printLockName(sbuf, "waiting on", lockName);

        for (int i = 0; i < stackTrace.length; i++) {
          sbuf.append(stackTrace[i]).append("<br/>");
          for (Object monitor : monitors) {
            int lockedStackDepth = (Integer) getLockedStackDepth.invoke(monitor, NO_PARAMS);
            if (lockedStackDepth == i) {
              printLockName(sbuf, "locked", monitor.toString());
            }
          }
        }

        Object[] synchronizers = (Object[]) getLockedSynchronizers.invoke(threadInfo, NO_PARAMS);
        if (synchronizers.length > 0) {
          sbuf.append("<br/>").append("<b>Locked synchronizers</b> : ").append("<br/>");
          for (Object synchronizer : synchronizers) {
            printLockName(sbuf, "locked", synchronizer.toString());
          }
        }

        sbuf.append("</blockquote>");
      }
    }
    else {
      sbuf.append("<br/>");
    }
  }

  private void printLockName(StringBuffer sbuf, String status, String lockName) {
    if (lockName != null) {
      String[] lockInfo = lockName.split("@");
      sbuf.append("- " + status + " <0x" + lockInfo[1] + "> (a " + lockInfo[0] + ")").append("<br/>");
    }
  }

  private void findDeadlockedThreads(StringBuffer rc) {
    if (isSynchronizerUsageSupported) {
      findDeadlockedThreadsMonitorsOrSynchronisers(rc);
    }
    else {
      findDeadlockedThreadsOnlyMonitors(rc);
    }
  }

  private void findDeadlockedThreadsMonitorsOrSynchronisers(StringBuffer sb) {
    try {
      long[] ids = (long[]) findDeadlockedThreads.invoke(threadMXBean, NO_PARAMS);
      if (ids == null) {
        return;
      }

      ThreadInfo[] threadsInfo = (ThreadInfo[]) getThreadInfoWithSyncInfo
        .invoke(threadMXBean, new Object[] { ids, isObjectMonitorUsageSupported, isSynchronizerUsageSupported });

      sb.append("<br/><b>Found deadlock(s)</b> : <br/><br/>");

      for (ThreadInfo threadInfo : threadsInfo) {
        sb.append("<b>");
        sb.append("Thread: " + threadInfo.getThreadName());
        sb.append("</b> : ");
        outputThreadMXBeanInfo(sb, threadInfo);
      }
    }
    catch (Exception ignore) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Exception to be ignored", ignore);
      }
    }
  }

  private void findDeadlockedThreadsOnlyMonitors(StringBuffer sb) {
    try {
      long[] ids = threadMXBean.findMonitorDeadlockedThreads();
      if (ids == null) {
        return;
      }

      ThreadInfo[] threadsInfo = threadMXBean.getThreadInfo(ids, Integer.MAX_VALUE);

      sb.append("<br/><b>Found deadlock(s)</b> : <br/><br/>");

      for (ThreadInfo threadInfo : threadsInfo) {
        sb.append("<b>");
        sb.append("Thread: " + threadInfo.getThreadName());
        sb.append("</b> : ");
        outputWithoutMonitorThreadMXBeanInfo(sb, threadInfo);
      }
    }
    catch (Exception ignore) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Exception to be ignored", ignore);
      }
    }
  }

  private String getVMArguments() {
    RuntimeMXBean rmBean = ManagementFactory.getRuntimeMXBean();
    List<String> inputArguments = rmBean.getInputArguments();
    StringBuilder sb = new StringBuilder();
    for (String arg : inputArguments) {
      sb.append(arg).append('\n');
    }
    return sb.toString();
  }

  /**
   * Display the java.lang.Package info for the pkgName
   */
  @Override
  public String displayPackageInfo(String pkgName) {
    Package pkg = Package.getPackage(pkgName);
    StringBuffer info = new StringBuffer("<h2>Package: ");
    if (pkg == null) {
      info.append(pkgName).append(" Not Found!</h2>");
    }
    else {
      info.append(pkgName).append("</h2>");
      displayPackageInfo(pkg, info);
    }
    return info.toString();
  }

  private void displayPackageInfo(Package pkg, StringBuffer info) {
    info.append("<pre>\n");
    info.append("SpecificationTitle: " + pkg.getSpecificationTitle());
    info.append("\nSpecificationVersion: " + pkg.getSpecificationVersion());
    info.append("\nSpecificationVendor: " + pkg.getSpecificationVendor());
    info.append("\nImplementationTitle: " + pkg.getImplementationTitle());
    info.append("\nImplementationVersion: " + pkg.getImplementationVersion());
    info.append("\nImplementationVendor: " + pkg.getImplementationVendor());
    info.append("\nisSealed: " + pkg.isSealed());
    info.append("</pre>\n");
  }

  /*
   * Inner Helper class for fancy text graphs
   * 
   */
  private static class TextGraphHelper {
    // number conversions
    static final DecimalFormat formatter = new DecimalFormat("#.##");
    static final long KILO = 1024;
    static final long MEGA = 1024 * 1024;
    static final long GIGA = 1024 * 1024 * 1024;

    // how many dashes+pipe is 100%
    static final int factor = 70;
    static char[] fixedline;
    static char[] baseline;
    static char[] barline;
    static char[] spaces;
    static {
      // cache a couple of Strings
      StringBuffer sbuf0 = new StringBuffer();
      StringBuffer sbuf1 = new StringBuffer();
      StringBuffer sbuf2 = new StringBuffer();
      StringBuffer sbuf3 = new StringBuffer();
      sbuf0.append('+');
      sbuf1.append('|');
      sbuf2.append('|');
      for (int i = 1; i < factor; i++) {
        sbuf0.append('-');
        sbuf1.append('-');
        sbuf2.append('/');
        sbuf3.append(' ');
      }
      sbuf0.append('+');
      fixedline = sbuf0.toString().toCharArray();
      baseline = sbuf1.toString().toCharArray();
      barline = sbuf2.toString().toCharArray();
      spaces = sbuf3.toString().toCharArray();
    }

    private TextGraphHelper() {
      // do not instantiate
    }

    /*
     * Make a text graph of a memory pool usage:
     * 
     * +---------------------------| committed:10Mb
     * +-------------------------------------------------+ |//////////////// | |
     * max:20Mb +-------------------------------------------------+
     * +---------------| used:3Mb
     *
     * When max is unknown assume max == committed
     * 
     * |-------------------------------------------------| committed:10Mb
     * +-------------------------------------------------+ |//////////////// |
     * max:-1 +-------------------------------------------------+
     * |---------------| used:3Mb
     */
    public static void poolUsage(StringBuffer sbuf, long used, long committed, long max) {
      // there is a chance that max is not provided (-1)
      long assumedMax = (max == -1) ? committed : max;
      // find out bar lengths
      int localUsed = (int) (factor * used / assumedMax);
      int localCommitted = (int) (factor * committed / assumedMax);
      int localMax = factor;

      sbuf.append("<blockquote><br/>");
      sbuf.append(baseline, 0, localCommitted).append("| committed:").append(outputNumber(committed)).append("<br/>");
      sbuf.append(fixedline).append("<br/>");

      // the difficult part
      sbuf.append(barline, 0, localUsed);
      if (localUsed < localCommitted) {
        sbuf.append(localUsed > 0 ? '/' : '|');
        sbuf.append(spaces, 0, localCommitted - localUsed - 1);
      }
      sbuf.append('|');
      if (localCommitted < localMax) {
        sbuf.append(spaces, 0, localMax - localCommitted - 1);
        sbuf.append('|');
      }
      sbuf.append(" max:").append(outputNumber(max)).append("<br/>");

      sbuf.append(fixedline).append("<br/>");
      sbuf.append(baseline, 0, localUsed).append("| used:").append(outputNumber(used));
      sbuf.append("</blockquote>");
    }

    private static String outputNumber(long value) {
      if (value >= GIGA) {
        return formatter.format((double) value / GIGA) + "Gb";
      }
      else if (value >= MEGA) {
        return formatter.format((double) value / MEGA) + "Mb";
      }
      else if (value >= KILO) {
        return formatter.format((double) value / KILO) + "Kb";
      }
      else if (value >= 0) {
        return value + "b";
      }
      else {
        return Long.toString(value);
      }
    }
  }

  private static class ThreadCPU implements Comparable<ThreadCPU> {
    public String name;
    public long cpuTime;

    public ThreadCPU(String name, long cpuTime) {
      this.name = name;
      this.cpuTime = cpuTime / 1000000; // convert to millis
    }

    @Override
    public int compareTo(ThreadCPU other) {
      long value = cpuTime - other.cpuTime;
      if (value > 0)
        return -1;
      else if (value < 0)
        return +1;
      else
        return name.compareTo(other.name);
    }
  }

  /*
   * Simple data holder
   */
  private static class ThreadGroupCount {
    public int threads;
    public int groups;
  }

  //// private double getJavaVer() {
  //// String version = getJavaVersion();
  //// if (LOG.isDebugEnabled()) {
  //// LOG.debug("version:" + version);
  //// }
  //// int pos = 0, count = 0;
  //// for (; pos < version.length() && count < 2; pos++) {
  //// if (version.charAt(pos) == '.') {
  //// count++;
  //// }
  //// }
  //// --pos; // EVALUATE double
  ////
  //// return Double.parseDouble(version.substring(0, pos));
  //// }
  //
  // /** JAVA version */
  // private double javaVersion = getJavaVer();

  /** Zero */
  private static final Integer ZERO = new Integer(0);

  /** Empty parameter signature for reflective calls */
  private static final Class<?>[] NO_PARAMS_SIG = new Class[0];

  /** Empty parameter list for reflective calls */
  private static final Object[] NO_PARAMS = new Object[0];

  /** used for formating timestamps (date attribute) */
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

  /** Entry point for the management of the thread system */
  private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

  /** The cached host name for the server. */
  private String hostName;

  /** The cached host address for the server. */
  private String hostAddress;

  /** The cached jdk6+ ThreadMXBean methods */
  private boolean isObjectMonitorUsageSupported;
  private boolean isSynchronizerUsageSupported;
  private Method findDeadlockedThreads;
  private Method getThreadInfoWithSyncInfo;

  /** The cached jdk6+ ThreadInfo methods */
  private Method getLockedMonitors;
  private Method getLockedSynchronizers;

  /** The cached jdk6+ MonitorInfo methods */
  private Method getLockedStackDepth;

  /** Class logger. */
  private static final Log LOG = LogFactory.getLog(ServerInfo.class);

}