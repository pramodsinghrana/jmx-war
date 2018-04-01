package com.psr.jmx.control;

/**
 * A simple tuple of an MBean operation name,
 * signature, arguments and operation result.
 *
 * @author parmodsinghrana
 *
 */
public class OpResultInfo {

  public OpResultInfo() {

  }

  public OpResultInfo(String name, String methodName, String[] signature, String[] arguments, Object result) {
    this.name = name;
    this.methodName = methodName;
    this.signature = signature;
    this.arguments = arguments;
    this.result = result;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the signature
   */
  public String[] getSignature() {
    return signature;
  }

  /**
   * @return the arguments
   */
  public String[] getArguments() {
    return arguments;
  }

  /**
   * @return the result
   */
  public Object getResult() {
    return result;
  }

  /**
   * @return the methodName
   */
  public String getMethodName() {
    return methodName;
  }

  private String name;
  private String methodName;
  private String[] signature;
  private String[] arguments;
  private Object result;

}
