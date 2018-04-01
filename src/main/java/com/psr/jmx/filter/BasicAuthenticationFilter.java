package com.psr.jmx.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

/**
 * Servlet Filter implementation class BasicAuthenticationFilter
 * 
 * See https://gist.github.com/neolitec/8953607
 */
public class BasicAuthenticationFilter extends AbstractFilter implements Filter {

  /**
   * 
   * (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
    throws IOException, ServletException {

    if (!enabled) {
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    String authHeader = request.getHeader("Authorization");
    if (authHeader != null) {
      StringTokenizer st = new StringTokenizer(authHeader);
      if (st.hasMoreTokens()) {
        String basic = st.nextToken();

        if (basic.equalsIgnoreCase("Basic")) {
          try {
            String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
            int p = credentials.indexOf(":");
            if (p != -1) {
              String _username = credentials.substring(0, p).trim();
              String _password = credentials.substring(p + 1).trim();

              if (!username.equals(_username) || !password.equals(_password)) {
                unauthorized(response, "Bad credentials");
              }

              filterChain.doFilter(servletRequest, servletResponse);
            }
            else {
              unauthorized(response, "Invalid authentication token");
            }
          }
          catch (UnsupportedEncodingException e) {
            throw new Error("Couldn't retrieve authentication", e);
          }
        }
      }
    }
    else {
      unauthorized(response);
    }
  }

  @Override
  public void destroy() {
  }

  private void unauthorized(HttpServletResponse response, String message) throws IOException {
    response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
    response.sendError(401, message);
  }

  private void unauthorized(HttpServletResponse response) throws IOException {
    unauthorized(response, "Unauthorized");
  }

  /**
   * @see Filter#init(FilterConfig)
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    super.init(filterConfig);
    username = filterConfig.getInitParameter("username");
    password = filterConfig.getInitParameter("password");
    enabled = "true".equals(filterConfig.getInitParameter("enabled"));
    String paramRealm = filterConfig.getInitParameter("realm");
    if (paramRealm != null && paramRealm.length() > 0) {
      realm = paramRealm;
    }

  }

  private String username = "";
  private String password = "";
  private boolean enabled;

  private String realm = "Protected";

}
