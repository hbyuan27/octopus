package com.octopus.sf.odata.batch;

public class BatchResponsePart {

  private Object responseBody;

  private String responseCode;

  private String contentType;

  public Object getResponseBody() {
    return responseBody;
  }

  public void setResponseBody(Object responseBody) {
    this.responseBody = responseBody;
  }

  public String getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(String responseCode) {
    this.responseCode = responseCode;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

}
