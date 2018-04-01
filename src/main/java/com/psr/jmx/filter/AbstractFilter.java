package com.psr.jmx.filter;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * Abstract implementation class {@link Filter}
 */
public abstract class AbstractFilter implements Filter {

  /**
   * @see Filter#init(FilterConfig)
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
  }

  protected FilterConfig filterConfig;
}
