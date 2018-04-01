<%@page import="java.net.URLEncoder"%>
<%@page import="com.psr.jmx.control.OpResultInfo"%>
<%@page import="static com.psr.jmx.servlet.MBeanAdaptorServlet.*"%>
<%@page contentType="text/html"%>
<%!private void outputFuction(String indent, Object result, javax.servlet.jsp.JspWriter out) throws java.io.IOException {
    if (result instanceof Object[]) {
      for (Object obj : (Object[]) result) {
        outputFuction("  " + indent, obj, out);
      }
    }
    else {
      if (result instanceof javax.management.openmbean.CompositeData) {
        outputCompositData((javax.management.openmbean.CompositeData) result, out);
      }
      else {
        String opResultString = String.valueOf(result);
        boolean hasPreTag = opResultString.startsWith("<pre>");
        if (!hasPreTag)
          out.println("<pre>");
        out.println(opResultString);
        if (!hasPreTag)
          out.println("</pre>");
      }
    }
  }

  private void outputCompositData(
    javax.management.openmbean.CompositeData compostiData,
    javax.servlet.jsp.JspWriter out)
    throws java.io.IOException {
    javax.management.openmbean.CompositeType compositType = compostiData.getCompositeType();

    out.println("<div class=\"table-responsive\">");
    out.println("<table class=\"table table-responsive table-bordered\">");
    out.println("<thead>");
    out.println("<tr");
    out.println("<th>Name</th>");
    out.println("<th>Value</th>");
    out.println("<th>Type</th>");
    out.println("<th>Description</th>");
    out.println("</tr>");
    out.println("</thead>");
    out.println("</tbody>");
    out.println("<tr>");
    out.println("<td><b>Name :</b></td>");
    out.println("<td>" + compositType.getTypeName() + "</td>");
    out.println("<td>" + compositType.getTypeName() + "</td>");
    out.println("<td>" + compositType.getDescription() + "</td>");
    out.println("</tr>");

    for (String key : compositType.keySet()) {
      Object value = compostiData.get(key);
      javax.management.openmbean.OpenType<?> type = compositType.getType(key);
      out.println("<tr>");
      out.println("<td><b>" + key + " :</b></td>");
      out.println("<td>" + value + "</td>");
      out.println("<td>" + type.getTypeName() + "</td>");
      out.println("<td>" + type.getDescription() + "</td>");
      out.println("</tr>");
    }
    out.println("</tbody>");
    out.println("</table>");
    out.println("</div>");
  }%>
<!DOCTYPE html>
<html>
<head>
<title>Operation Results</title>
<jsp:include page="header.jsp"></jsp:include>
</head>
<body>
  <jsp:useBean id='opResultInfo' class='com.psr.jmx.control.OpResultInfo' type='com.psr.jmx.control.OpResultInfo' scope='request' />
  <%
    if (opResultInfo.getName() == null || opResultInfo.getMethodName() == null) {
  %>
  <jsp:forward page="/MBeanAdaptor?<%=ACTION_PARAM%>=<%=DISPLAY_MBEANS_ACTION%>" />
  <%
    }

    String mbeanName = URLEncoder.encode(opResultInfo.getName(), "UTF-8");
  %>
  <div class="container-fluid">
    <div class="table-responsive">
      <table class="table table-borderless table-responsive table-sm">
        <tr>
          <td class="col-xs-1"><img alt="JMX" src="images/jmx-logo.png" class="img-thumbnail img-responsive"></td>
          <td>
            <h1>JMX MBean Operation Result</h1>
            <h3>
              <code>
                <%=opResultInfo.getMethodName()%>()
              </code>
            </h3>
          </td>
        </tr>
      </table>
    </div>
    <div class="table-responsive">
      <table class="table table-borderless table-sm table-responsive">
        <tr>
          <td><a href='MBeanAdaptor?<%=ACTION_PARAM%>=<%=DISPLAY_MBEANS_ACTION%>'>Back to Agent View</a></td>
          <td><a
            href='MBeanAdaptor?<%=ACTION_PARAM%>=<%=INSPECT_MBEAN_ACTION%>&<%=MBEAN_NAME_PARAM%>=<%=URLEncoder.encode(request.getParameter(MBEAN_NAME_PARAM), "UTF-8")%>'>Back
              to MBean View</a></td>
          <td>
            <%
              StringBuilder sb = new StringBuilder("MBeanAdaptor");
              sb.append("?").append(ACTION_PARAM).append("=").append(INVOKE_OP_BY_NAME_ACTION);
              sb.append("&").append(MBEAN_NAME_PARAM).append("=").append(mbeanName);
              sb.append("&").append(METHOD_NAME_PARM).append("=").append(opResultInfo.getMethodName());
              for (int i = 0; i < opResultInfo.getArguments().length; i++) {
                sb.append("&argType=").append(opResultInfo.getSignature()[i]);
                sb.append("&").append(ARGUMENT_NAME_PREFIX).append(i).append("=").append(opResultInfo.getArguments()[i]);
              }
            %> <a href='<%=sb.toString()%>'>Reinvoke MBean Operation</a>
          </td>
        </tr>
      </table>
    </div>
    <hr />
    <%
      if (opResultInfo.getResult() == null) {
    %>
    <div class="alert alert-success">Operation completed successfully without a return value.</div>
    <%
      }
      else {
        outputFuction("", opResultInfo.getResult(), out);
      }
    %>
  </div>
</body>
</html>
