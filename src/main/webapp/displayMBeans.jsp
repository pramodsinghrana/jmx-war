<%@ page import="java.io.IOException"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.net.InetAddress"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.psr.jmx.model.MBeanData"%>
<%@ page import="com.psr.jmx.model.MBeanDomainData"%>
<%@ page import="static com.psr.jmx.servlet.MBeanAdaptorServlet.*"%>
<%@ page contentType="text/html"%>
<%
  request.getServerPort();
  String bindAddress = "";
  String serverName = "";
  try {

    bindAddress = request.getLocalAddr();
    serverName = request.getServerName();
  }
  catch (SecurityException se) {
  }

  String hostname = "";
  try {
    hostname = InetAddress.getLocalHost().getHostName();
  }
  catch (IOException e) {
  }

  String hostInfo = hostname;
  if (!bindAddress.equals("")) {
    hostInfo = hostInfo + " (" + bindAddress + ")";
  }
%>
<!DOCTYPE html>
<html lang="en">
<head>
<jsp:include page="header.jsp"></jsp:include>
<title>JMX Management Console - <%=hostInfo%></title>
</head>
<body>
  <div class="container-fluid">
    <div class="table-responsive">
      <table class="table table-borderless table-responsive table-sm">
        <tr>
          <td class="col-xs-1"><img alt="JMX" src="images/jmx-logo.png" class="img-thumbnail img-responsive"></td>
          <td>
            <h1>JMX Agent View</h1>
            <h3><%=hostInfo%>&nbsp;-&nbsp;<%=serverName%></h3>
          </td>
        </tr>
      </table>
    </div>
    <hr />
    <%
      if (request.getAttribute(FILTER_ERROR_ATTR_NAME) != null) {
    %>
    <div class="row">
      <div class="col-xs-12">
        <div class="alert alert-warning alert-dismissible">
          <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a><strong>Error!</strong>&nbsp;&nbsp;<%=request.getAttribute(FILTER_ERROR_ATTR_NAME)%>
        </div>
      </div>
    </div>
    <%
      }
    %>
    <form class="form" action="MBeanAdaptor?<%=ACTION_PARAM%>=displayMBeans" method="post" name="applyFilter" id="applyFilter">
      <div class="row">
        <div class="col-md-6">
          <div class="input-group">
            <div class="input-group-prepend">
              <span class="input-group-text">ObjectName Filter:</span>
            </div>
            <input type="text" name="<%=FILTER_PARAM%>" class="form-control" placeholder='jmx:*", "*:service=invoker,*'
              value='<%=request.getAttribute(FILTER_PARAM)%>'>
            <div class="input-group-append">
              <input type="submit" class="btn btn-info" value="Search">
            </div>
          </div>
        </div>
      </div>
    </form>
    <hr />
    <%
      Iterator<?> mbeans = (Iterator<?>) request.getAttribute(MBEAN_ATTR_NAME);
      while (mbeans.hasNext()) {
        MBeanDomainData domainData = (MBeanDomainData) mbeans.next();
    %>
    <h3><%=domainData.getDomainName()%></h3>
    <div class="row">
      <div class="col-md-12">
        <ul>
          <%
            MBeanData[] data = domainData.getData();
              for (int d = 0; d < data.length; d++) {
                String name = data[d].getObjectName().toString();
                String properties = data[d].getNameProperties();
                try {
                  name = URLEncoder.encode(name, "UTF-8");
                }
                catch (Exception e) {

                }
                try {
                  properties = URLDecoder.decode(properties, "UTF-8");
                }
                catch (Exception e) {

                }
          %>
          <li><a href="MBeanAdaptor?<%=ACTION_PARAM%>=<%=INSPECT_MBEAN_ACTION%>&<%=MBEAN_NAME_PARAM%>=<%=name%>"><%=properties%></a></li>
          <%
            }
          %>
        </ul>
      </div>
    </div>
    <%
      }
    %>
  </div>
</body>
</html>