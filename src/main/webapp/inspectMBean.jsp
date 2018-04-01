<%@page import="java.lang.reflect.Array"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.Hashtable"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.management.Descriptor"%>
<%@page import="javax.management.MBeanAttributeInfo"%>
<%@page import="javax.management.MBeanInfo"%>
<%@page import="javax.management.MBeanOperationInfo"%>
<%@page import="javax.management.MBeanParameterInfo"%>
<%@page import="javax.management.modelmbean.ModelMBeanOperationInfo"%>
<%@page import="javax.management.ObjectName"%>
<%@page import="com.psr.jmx.control.MBeanManager"%>
<%@page import="com.psr.jmx.control.AttrResultInfo"%>
<%@page import="com.psr.jmx.model.MBeanData"%>
<%@page import="com.psr.jmx.propertyeditor.PropertyEditors"%>
<%@page import="static com.psr.jmx.servlet.MBeanAdaptorServlet.*"%>
<%@page contentType="text/html"%>
<!DOCTYPE html>
<html lang="en">
<%!public String fixDescription(String desc) {
    if (desc == null || desc.equals("")) {
      return "(no description)";
    }
    return desc;
  }

  public String quoteName(String name) {
    String sname = name.replace("\"", "&quot;");
    sname = name.replace("\'", "&apos;");
    return sname;
  }%>
<head>
<jsp:include page="header.jsp"></jsp:include>
<title>MBean Inspector</title>
</head>
<body>
  <jsp:useBean id='mbeanData' class='com.psr.jmx.model.MBeanData' scope='request' />
  <%
    if (mbeanData.getObjectName() == null) {
  %>
  <jsp:forward page="/MBeanAdaptor?<%=ACTION_PARAM%>=<%=DISPLAY_MBEANS_ACTION%>" />
  <%
    }
    ObjectName objectName = mbeanData.getObjectName();
    String objectNameString = mbeanData.getName();
    String quotedObjectNameString = quoteName(mbeanData.getName());
    MBeanInfo mbeanInfo = mbeanData.getMetaData();
    MBeanAttributeInfo[] attributeInfo = mbeanInfo.getAttributes();
    MBeanOperationInfo[] operationInfo = mbeanInfo.getOperations();
  %>
  <div class="container-fluid">
    <div class="table-responsive">
      <table class="table table-borderless table-sm table-responsive">
        <tr>
          <td class="col-xs-1"><img alt="JMX" src="images/jmx-logo.png" class="img-thumbnail img-responsive"></td>
          <td>
            <h1>JMX MBean View</h1>
          </td>
        </tr>
      </table>
    </div>
    <div class="table-responsive">
      <table class="table table-borderless table-sm table-responsive">
        <tr>
          <td>MBean Name:</td>
          <td><b>Domain Name:</b></td>
          <td><%=objectName.getDomain()%></td>
        </tr>
        <%
          Hashtable<String, String> properties = objectName.getKeyPropertyList();
          Iterator<String> it = properties.keySet().iterator();
          while (it.hasNext()) {
            String key = it.next();
            String value = properties.get(key);
        %>
        <tr>
          <td></td>
          <td><b><%=key%>: </b></td>
          <td><%=value%></td>
        </tr>
        <%
          }
        %>
        <tr>
          <td>MBean Java Class:</td>
          <td colspan="2"><jsp:getProperty name='mbeanData' property='className' /></td>
        </tr>
      </table>
    </div>
    <div class="table-responsive">
      <table class="table table-borderless table-sm table-responsive">
        <tr>
          <td><a href='MBeanAdaptor?<%=ACTION_PARAM%>=<%=DISPLAY_MBEANS_ACTION%>'>Back to Agent View</a></td>
          <td><a
            href='MBeanAdaptor?<%=ACTION_PARAM%>=<%=INSPECT_MBEAN_ACTION%>&<%=MBEAN_NAME_PARAM%>=<%=URLEncoder.encode(request.getParameter(MBEAN_NAME_PARAM), "UTF-8")%>'>Refresh
              MBean View</a></td>
        </tr>
      </table>
    </div>
    <hr>
    <h3>MBean description:</h3>
    <%=fixDescription(mbeanInfo.getDescription())%>
    <hr>
    <%
      if (attributeInfo.length > 0) {
        boolean activateSubmit = false;
    %>
    <h3>List of MBean attributes:</h3>
    <form class="form" method="post" action="MBeanAdaptor">
      <input type="hidden" name="<%=ACTION_PARAM%>" value="<%=UPDATE_ATTRIBUTES_ACTION%>" />
      <input type="hidden" name="<%=MBEAN_NAME_PARAM%>" value='<%=quotedObjectNameString%>' />
      <div class="table-responsive">
        <table class="table table-responsive table-bordered table-striped">
          <thead>
            <tr>
              <th>Name</th>
              <th>Type</th>
              <th>Access</th>
              <th>Value</th>
              <th>Description</th>
            </tr>
          </thead>
          <tbody>
            <%
              boolean hasWriteable = false;
                for (int a = 0; a < attributeInfo.length; a++) {
                  MBeanAttributeInfo attrInfo = attributeInfo[a];
                  String attrName = attrInfo.getName();
                  String attrType = attrInfo.getType();
                  AttrResultInfo attrResult = MBeanManager.getMBeanAttributeResultInfo(objectNameString, attrInfo);
                  String attrValue = attrResult.getAsText();
                  String access = "";
                  if (attrInfo.isReadable()) {
                    access += "R";
                  }
                  if (attrInfo.isWritable()) {
                    access += "W";
                    hasWriteable = true;
                    activateSubmit = true;
                  }
                  String attrDescription = fixDescription(attrInfo.getDescription());
            %>
            <tr>
              <td><%=attrName%></td>
              <td><%=attrType%></td>
              <td><%=access%></td>
              <td>
                <div class="form-group">
                  <%
                    if (hasWriteable) {
                          String readonly = attrResult.getEditor() == null ? "readonly" : "";
                          if (attrType.equals("boolean") || attrType.equals("java.lang.Boolean")) {
                            // Boolean true/false radio boxes
                            Boolean value = attrValue == null || "".equals(attrValue) ? null : Boolean.valueOf(attrValue);
                            String trueChecked = (Boolean.TRUE.equals(value) ? "checked" : "");
                            String falseChecked = (Boolean.FALSE.equals(value) ? "checked" : "");
                            String nullCheck = value == null ? "checked" : "";
                  %>
                  <div class="radio">
                    <label class="radio-inline">
                      <input type="radio" name="<%=attrName%>" value="True" <%=trueChecked%>>
                      TRUE
                    </label>
                    <label class="radio-inline">
                      <input type="radio" name="<%=attrName%>" value="False" <%=falseChecked%>>
                      FALSE
                    </label>
                    <%
                      // For wrappers, enable a 'null' selection 
                              if (attrType.equals("java.lang.Boolean") && PropertyEditors.isNullHandlingEnabled()) {
                    %>
                    <label class="radio-inline">
                      <input type="radio" name="<%=attrName%>" value="" <%=nullCheck%>>
                      NULL
                    </label>
                    <%
                      }
                    %>
                  </div>
                  <%
                    }
                          else if (attrInfo.isReadable()) { // Text fields for read-write string values
                            String avalue = (attrValue != null ? attrValue : "");
                            if (attrType.equals("javax.management.ObjectName"))
                              avalue = quoteName(avalue);
                            if (avalue.length() > 30) {
                  %>
                  <textarea class="form-control" rows="4" name="<%=attrName%>" <%=readonly%>><%=avalue%></textarea>
                  <%
                    }
                            else {
                  %>
                  <input class="form-control" type="text" name="<%=attrName%>" value='<%=avalue%>' <%=readonly%>>
                  <%
                    }
                          }
                          else {
                  %>
                  <input class="form-control" type="text" name="<%=attrName%>" <%=readonly%>>
                  <%
                    }
                        }
                        else {
                          if (attrType.equals("[Ljavax.management.ObjectName;")) {
                            // Array of Object Names
                            ObjectName[] names = (ObjectName[]) MBeanManager.getMBeanAttributeObject(objectNameString, attrName);
                            if (names != null) {
                  %>
                  <ul>
                    <%
                      for (int i = 0; i < names.length; i++) {
                    %>
                    <li><a
                      href='MBeanAdaptor?<%=ACTION_PARAM%>=<%=INSPECT_MBEAN_ACTION%>&<%=MBEAN_NAME_PARAM%>=<%=URLEncoder.encode((names[i] + ""), "UTF-8")%>'><%=(names[i] + "")%></a></li>
                    <%
                      }
                    %>
                  </ul>
                  <%
                    }
                          }
                          else if (attrType.endsWith("[]") || attrType.startsWith("[L")) {
                            Object arrayObject = MBeanManager.getMBeanAttributeObject(objectNameString, attrName);
                            if (arrayObject != null) {
                  %>
                  <ul>
                    <%
                      for (int i = 0; i < Array.getLength(arrayObject); ++i) {
                    %>
                    <li><%=Array.get(arrayObject, i)%></li>
                    <%
                      }
                    %>
                  </ul>
                  <%
                    }
                          }
                          else {
                  %>
                  <label><%=attrValue%></label>
                  <%
                    }
                        }
                        if ("javax.management.ObjectName".equals(attrType)) {
                  %>
                  <a href='MBeanAdaptor?<%=ACTION_PARAM%>=<%=INSPECT_MBEAN_ACTION%>&<%=MBEAN_NAME_PARAM%>=<%=URLEncoder.encode(attrValue, "UTF-8")%>'>View
                    MBean</a>
                  <%
                    }
                  %>
                </div>
              </td>
              <td><%=attrDescription%></td>
            </tr>
            <%
              }
            %>
          </tbody>
          <%
            if (activateSubmit) {
          %>
          <tfoot>
            <tr>
              <td colspan="5"><input type="submit" class="btn btn-info" value="Apply Changes"></td>
            </tr>
          </tfoot>
          <%
            }
          %>
        </table>
      </div>
    </form>
    <%
      }
      if (operationInfo.length > 0) {
    %>
    <hr>
    <h3>List of MBean operations:</h3>
    <%
      for (int a = 0; a < operationInfo.length; a++) {
          MBeanOperationInfo opInfo = operationInfo[a];
          boolean accept = true;
          if (opInfo instanceof ModelMBeanOperationInfo) {
            Descriptor desc = ((ModelMBeanOperationInfo) opInfo).getDescriptor();
            String role = (String) desc.getFieldValue("role");
            if ("getter".equals(role) || "setter".equals(role)) {
              accept = false;
            }
          }
          if (accept) {
            MBeanParameterInfo[] sig = opInfo.getSignature();
    %>
    <form class="form" method="post" action="MBeanAdaptor">
      <input type="hidden" name="<%=ACTION_PARAM%>" value="<%=INVOKE_OP_BY_NAME_ACTION%>">
      <input type="hidden" name="<%=MBEAN_NAME_PARAM%>" value='<%=quotedObjectNameString%>'>
      <input type="hidden" name="methodIndex" value="<%=a%>">
      <input type="hidden" name="<%=METHOD_NAME_PARM%>" value="<%=opInfo.getName()%>">
      <hr align='left' width='80'>
      <h4><%=opInfo.getReturnType() + " " + opInfo.getName() + "()"%></h4>
      <p><%=fixDescription(opInfo.getDescription())%></p>
      <%
        if (sig.length > 0) {
      %>
      <div class="table-responsive">
        <table class="table table-responsive table-bordered">
          <thead>
            <tr>
              <th>Parameter Name</th>
              <th>Parameter Type</th>
              <th>Parameter Value</th>
              <th>Parameter Description</th>
            </tr>
          </thead>
          <tbody>
            <%
              for (int p = 0; p < sig.length; p++) {
                        MBeanParameterInfo paramInfo = sig[p];
                        String pname = paramInfo.getName();
                        String ptype = paramInfo.getType();
                        if (pname == null || pname.length() == 0 || pname.equals(ptype)) {
                          pname = ARGUMENT_NAME_PREFIX + p;
                        }
            %>
            <tr>
              <td><%=pname%></td>
              <td><%=ptype%><input type="hidden" name="argType" value="<%=ptype%>"></td>
              <td>
                <%
                  if (ptype.equals("boolean") || ptype.equals("java.lang.Boolean")) {
                              // Boolean true/false radio boxes
                %>
                <div class="radio">
                  <label class="radio-inline">
                    <input type="radio" name="<%=ARGUMENT_NAME_PREFIX + p%>" value="True" checked>
                    TRUE
                  </label>
                  <label class="radio-inline">
                    <input type="radio" name="<%=ARGUMENT_NAME_PREFIX + p%>" value="False">
                    FALSE
                  </label>
                </div> <%
   }
             else {
 %> <input class="form-control" type="text" name="<%=ARGUMENT_NAME_PREFIX + p%>"> <%
   }
 %>
              </td>
              <td><%=fixDescription(paramInfo.getDescription())%></td>
            </tr>
            <%
              }
            %>
          </tbody>
        </table>
      </div>
      <%
        }
            }
      %>
      <input type="submit" class="btn btn-info" value="Invoke">
    </form>
    <%
      }
      }
    %>
  </div>
</body>
</html>