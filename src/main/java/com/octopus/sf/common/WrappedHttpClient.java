package com.octopus.sf.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class WrappedHttpClient {
  private static final String HTTP_METHOD_TUNNELING = "X-HTTP-Method";

  private static final int MAX_REQUEST_BODY_LEN = 1 * 1024 * 1024; // 3 MB for UTF-8 payload

  private static final int MAX_RESPONSE_BODY_LEN = 10 * 1024 * 1024; // 30 MB for UTF-8 response body

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public WrappedResponse execute(String uri, HttpParams params) {
    WrappedResponse response = new WrappedResponse();
    // initiate HTTP connection
    HttpURLConnection conn = initConnection(uri, params);
    if (conn == null) {
      response.setSuccess(false);
      response.setMessage("Failed to initiate an HttpURLConnection.");
      return response;
    }
    // get response
    BufferedReader br = null;
    try {
      conn.connect();
      int code = conn.getResponseCode();
      response.setMessage("HttpResponseCode: " + code);
      InputStream is = null;
      if (code < 300) {
        response.setSuccess(true);
        is = conn.getInputStream();
      } else if (code >= 400) {
        response.setSuccess(false);
        is = conn.getErrorStream();
      }
      br = new BufferedReader(new InputStreamReader(is));
      StringBuilder sb = new StringBuilder();
      int charCode;
      while ((charCode = br.read()) != -1) {
        char c = (char) charCode;
        if (sb.length() > MAX_RESPONSE_BODY_LEN) {
          response.setSuccess(false);
          response.setMessage("The size of response body exceeds the limit (characters): " + MAX_RESPONSE_BODY_LEN);
          return response;
        }
        sb.append(c);
      }
      response.setContent(sb.toString());
    } catch (IOException e) {
      response.setSuccess(false);
      response.setMessage("Failed to get an HTTP response. Reason: " + e.getMessage());
    } finally {
      CommonUtils.closeQuietly(br);
    }
    return response;
  }

  private HttpURLConnection initConnection(String uri, HttpParams params) {
    HttpURLConnection conn = null;
    BufferedWriter bw = null;
    try {
      URL url = new URL(uri);
      conn = (HttpURLConnection) url.openConnection();
      HttpMethod httpMethod = params.getHttpMethod();
      conn.setRequestMethod(httpMethod.toString());
      String contentType = params.getContentType();
      if (contentType != null) {
        conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, contentType);
      }
      String acceptType = params.getAcceptType();
      if (acceptType != null) {
        conn.setRequestProperty(HttpHeaders.ACCEPT, acceptType);
      }
      String authorization = params.getAuthorization();
      if (authorization != null) {
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, authorization);
      }
      // In many scenarios clients are limited to the HTTP GET and POST methods only. With Method-Tunneling, a client
      // sets up a request uses POST as the HTTP method instead of the actual required one. It then adds one more
      // header, "X-HTTP-Method", and gives it the value MERGE, PUT or DELETE.
      if (HttpMethod.POST == httpMethod) {
        String methodTunnel = params.getMethodTunnel();
        if (methodTunnel != null) {
          conn.setRequestProperty(HTTP_METHOD_TUNNELING, methodTunnel);
        }
      }
      // process request body
      if (HttpMethod.POST == httpMethod || HttpMethod.PUT == httpMethod || HttpMethod.PATCH == httpMethod
          || HttpMethod.DELETE == httpMethod) {
        conn.setDoOutput(true);
        String payload = params.getPayload();
        if (payload != null) {
          if (payload.length() > MAX_REQUEST_BODY_LEN) {
            logger.error("The size of payload exceeds the limit: {} (characters)", MAX_REQUEST_BODY_LEN);
            return null;
          }
          bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
          bw.write(payload);
        }
      }
    } catch (IOException e) {
      logger.error("Failed to initiate an HttpURLConnection. Reason: " + e.getMessage());
      return null;
    } finally {
      CommonUtils.closeQuietly(bw);
    }
    return conn;
  }

}
