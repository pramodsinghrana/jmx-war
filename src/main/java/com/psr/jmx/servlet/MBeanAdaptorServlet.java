package com.psr.jmx.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.JMException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.psr.jmx.control.MBeanManager;
import com.psr.jmx.control.OpResultInfo;
import com.psr.jmx.model.MBeanData;
import com.psr.jmx.model.MBeanDomainData;

/**
 * Servlet implementation class HtmlMBeanAdaptor
 */
public class MBeanAdaptorServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public MBeanAdaptorServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter(ACTION_PARAM);

    if (action == null || DISPLAY_MBEANS_ACTION.equals(action)) {
      displayMBeans(request, response);
    }
    else if (INSPECT_MBEAN_ACTION.equals(action)) {
      inspectMBean(request, response);
    }
    else if (UPDATE_ATTRIBUTES_ACTION.equals(action)) {
      updateAttributes(request, response);
    }
    else if (INVOKE_OP_BY_NAME_ACTION.equals(action)) {
      invokeOpByName(request, response);
    }
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  /**
  * Display an mbeans attributes and operations
  */
  private void inspectMBean(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    String name = request.getParameter(MBEAN_NAME_PARAM);
    LOG.trace("inspectMBean, name=" + name);
    try {
      MBeanData data = MBeanManager.getMBeanData(name);
      request.setAttribute(MBEAN_DATA_ATTR_NAME, data);
      RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/inspectMBean.jsp");
      rd.forward(request, response);
    }
    catch (JMException e) {
      throw new ServletException("Failed to get MBean data", e);
    }
  }

  /** 
   * Update the writable attributes of an mbean
   */
  private void updateAttributes(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    String name = request.getParameter(MBEAN_NAME_PARAM);
    LOG.trace("updateAttributes, name=" + name);
    Enumeration<?> paramNames = request.getParameterNames();
    HashMap<String, String> attributes = new HashMap<>();
    while (paramNames.hasMoreElements()) {
      String param = (String) paramNames.nextElement();
      if (param.equals(MBEAN_NAME_PARAM) || param.equals(ACTION_PARAM))
        continue;
      String value = request.getParameter(param);
      LOG.trace("name=" + param + ", value='" + value + "'");
      // Ignore null values, these are empty write-only fields
      if (value == null || value.length() == 0)
        continue;
      attributes.put(param, value);
    }

    try {
      MBeanManager.setAttributes(name, attributes);
      MBeanData data = MBeanManager.getMBeanData(name);
      request.setAttribute(MBEAN_DATA_ATTR_NAME, data);
      RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/inspectMBean.jsp");
      rd.forward(request, response);
    }
    catch (JMException e) {
      throw new ServletException("Failed to update attributes", e);
    }
  }

  /** 
   * Invoke an mbean operation given the method name and its signature.
   */
  private void invokeOpByName(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    String name = request.getParameter(MBEAN_NAME_PARAM);
    LOG.trace("invokeOpByName, name=" + name);
    String[] args = getArgs(request);
    String methodName = request.getParameter(METHOD_NAME_PARM);
    if (methodName == null)
      throw new ServletException("No methodName given in invokeOpByName form");
    try {
      OpResultInfo opResult = MBeanManager.invokeOpByName(name, methodName, args);
      request.setAttribute(OP_RESULT_INFO_ATTR_NAME, opResult);
      RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/displayOpResult.jsp");
      rd.forward(request, response);
    }
    catch (JMException e) {
      throw new ServletException("Failed to invoke operation", e);
    }
  }

  /** 
   * Extract the argN values from the request into a String[]
  */
  private String[] getArgs(HttpServletRequest request) {
    ArrayList<String> argList = new ArrayList<>();
    String argName;
    String argValue;
    for (int i = 0; true; i++) {
      argName = ARGUMENT_NAME_PREFIX + i;
      argValue = request.getParameter(argName);
      if (argValue == null)
        break;
      argList.add(argValue);
      LOG.trace(argName + "=" + argValue);
    }
    return argList.toArray(new String[argList.size()]);
  }

  /**
   * Display all MBean categorized by domain
   */
  private void displayMBeans(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    // get ObjectName filter from request or session context
    HttpSession session = request.getSession(false);
    String filter = request.getParameter(FILTER_PARAM);

    if (filter == null && session != null) {
      // try using previously provided filter from session context
      filter = (String) session.getAttribute(FILTER_PARAM);
    }

    if (filter != null && filter.length() > 0) {
      // Strip any enclosing quotes
      if (filter.charAt(0) == '"')
        filter = filter.substring(1);
      if (filter.charAt(filter.length() - 1) == '"')
        filter = filter.substring(0, filter.length() - 2);

      // be a litte it tolerant to user input
      String domain = "*";
      String props = "*,*";

      int separator = filter.indexOf(':');
      int assignment = filter.indexOf('=');

      if (separator == -1 && assignment != -1) {
        // assume properties only
        props = filter.trim();
      }
      else if (separator == -1 && assignment == -1) {
        // assume domain name only
        domain = filter.trim();
      }
      else {
        // domain and properties
        domain = filter.substring(0, separator).trim();
        props = filter.substring(separator + 1).trim();
      }

      if (domain.equals(""))
        domain = "*";

      if (props.equals(""))
        props = "*,*";
      if (props.endsWith(","))
        props += "*";
      if (!props.endsWith(",*"))
        props += ",*";
      if (props.equals("*,*"))
        props = "*";

      filter = domain + ":" + props;

      if (filter.equals("*:*"))
        filter = "";
    }
    else {
      filter = "";
    }

    // update request filter and store filter in session context,
    // so it can be used when no filter has been submitted in
    // current request
    request.setAttribute(FILTER_PARAM, filter);

    if (session != null) {
      session.setAttribute(FILTER_PARAM, filter);
    }

    Iterator<MBeanDomainData> mbeans;
    try {
      mbeans = MBeanManager.getDomainData(filter);
    }
    catch (JMException e) {
      request.setAttribute(FILTER_ERROR_ATTR_NAME, e.getMessage());
      try {
        mbeans = MBeanManager.getDomainData("");
      }
      catch (JMException e1) {
        throw new ServletException("Failed to get MBeans", e);
      }
    }
    request.setAttribute(MBEAN_ATTR_NAME, mbeans);
    RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/displayMBeans.jsp");
    rd.forward(request, response);
  }

  public static final String ARGUMENT_NAME_PREFIX = "arg";
  public static final String ACTION_PARAM = "action";
  public static final String FILTER_PARAM = "filter";
  public static final String METHOD_NAME_PARM = "methodName";
  public static final String MBEAN_NAME_PARAM = "name";

  public static final String DISPLAY_MBEANS_ACTION = "displayMBeans";
  public static final String INSPECT_MBEAN_ACTION = "inspectMBean";
  public static final String UPDATE_ATTRIBUTES_ACTION = "updateAttributes";
  public static final String INVOKE_OP_BY_NAME_ACTION = "invokeOpByName";

  public static final String MBEAN_DATA_ATTR_NAME = "mbeanData";
  public static final String MBEAN_ATTR_NAME = "mbean";
  public static final String FILTER_ERROR_ATTR_NAME = "filterError";
  public static final String OP_RESULT_INFO_ATTR_NAME = "opResultInfo";

  private static final Log LOG = LogFactory.getLog(MBeanAdaptorServlet.class);

}
