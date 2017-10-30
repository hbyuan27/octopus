package com.octopus.sf.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class WrappedHttpClient {

  private static final String HTTP_METHOD_TUNNELING = "X-HTTP-Method";

  public WrappedResponse execute(String uri, HttpParams params) {
    WrappedResponse result = new WrappedResponse();
    HttpUriRequest request = buildRequest(uri, params);
    if (request == null) {
      result.setSuccess(false);
      result.setMessage("Invalid HTTP request.");
      return result;
    }
    // execute the HTTP Request
    CloseableHttpClient client = HttpClientBuilder.create().build();
    try {
      HttpResponse response = client.execute(request);
      StatusLine statusLine = response.getStatusLine();
      int code = statusLine.getStatusCode();
      result.setMessage(String.valueOf(code) + " " + statusLine.getReasonPhrase());
      if (code >= 400 && code <= 599) {
        result.setSuccess(false);
      } else {
        result.setSuccess(true);
        String content = EntityUtils.toString(response.getEntity());
        result.setContent(content);
      }
    } catch (IOException e) {
      result.setSuccess(false);
      result.setMessage("Execute HTTP Request failed. Reason: " + e.getMessage());
      return result;
    } finally {
      CommonUtils.closeQuietly(client);
    }
    return result;
  }

  private HttpUriRequest buildRequest(String uri, HttpParams params) {
    HttpUriRequest request = null;
    HttpMethod httpMethod = params.getHttpMethod();
    switch (httpMethod) {
    case GET:
      request = new HttpGet(uri);
      break;
    case POST:
      request = new HttpPost(uri);
      String methodTunnel = params.getMethodTunnel();
      if (methodTunnel != null) {
        request.setHeader(HTTP_METHOD_TUNNELING, methodTunnel);
      }
      break;
    case PUT:
      request = new HttpPut(uri);
      break;
    case PATCH:
      request = new HttpPatch(uri);
      break;
    case DELETE:
      request = new HttpDelete(uri);
      break;
    default:
      break;
    }
    if (request == null) {
      return null;
    }
    // check request body if any
    String payload = params.getPayload();
    if (payload != null && request instanceof HttpEntityEnclosingRequestBase) {
      StringEntity entity = new StringEntity(payload, StandardCharsets.UTF_8);
      ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
    }
    // check HTTP headers
    String contentType = params.getContentType();
    if (contentType != null) {
      request.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
    }
    String acceptType = params.getAcceptType();
    if (acceptType != null) {
      request.setHeader(HttpHeaders.ACCEPT, acceptType);
    }
    String authorization = params.getAuthorization();
    if (authorization != null) {
      request.setHeader(HttpHeaders.AUTHORIZATION, authorization);
    }
    return request;
  }
}