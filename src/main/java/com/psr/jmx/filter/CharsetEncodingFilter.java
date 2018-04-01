package com.psr.jmx.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.psr.jmx.context.JmxConfig;

/**
 * 
 * Charset Encoding filter
 * @author parmodsinghrana
 *
 */
public class CharsetEncodingFilter extends AbstractFilter {

  /**
   * (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    String charEncoding = JmxConfig.getProperty("charset.encoding", "UTF-8");
    request.setCharacterEncoding(charEncoding);
    response.setCharacterEncoding(charEncoding);
    chain.doFilter(request, response);
  }

}
