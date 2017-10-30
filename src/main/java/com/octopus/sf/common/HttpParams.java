package com.octopus.sf.common;

import org.springframework.http.HttpMethod;

public class HttpParams {

  private final HttpMethod httpMethod;

  private String contentType;

  private String acceptType;

  private String authorization;

  private String payload;

  private String methodTunnel;

  public HttpParams(HttpMethod httpMethod) {
    this.httpMethod = httpMethod;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getAcceptType() {
    return acceptType;
  }

  public void setAcceptType(String acceptType) {
    this.acceptType = acceptType;
  }

  public String getAuthorization() {
    return authorization;
  }

  public void setAuthorization(String authorization) {
    this.authorization = authorization;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public HttpMethod getHttpMethod() {
    return httpMethod;
  }

  public String getMethodTunnel() {
    return methodTunnel;
  }

  public void setMethodTunnel(String methodTunnel) {
    this.methodTunnel = methodTunnel;
  }

}
