package com.octopus.sf;

import static com.octopus.sf.SFAuthConstants.*;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import com.alibaba.fastjson.JSONObject;
import com.octopus.auth.SessionParams;
import com.octopus.sf.common.CommonUtils;
import com.octopus.sf.common.HttpParams;
import com.octopus.sf.common.WrappedHttpClient;
import com.octopus.sf.common.WrappedResponse;

@Component
public class SFAuthentication {

  @Autowired
  private SessionParams sessionParams;

  @Autowired
  private WrappedHttpClient httpClient;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private String authType;

  // baisc auth only
  private String user;

  private String pwd;

  // oauth only
  private String companyId;

  private String clientKey;

  private String privateKey;

  private String tokenServiceURL;

  private String tokenServiceUser;

  private String accessToken;

  private String assertion;

  private void init(Map<String, String> credential) {
    // common properties
    authType = credential.get(PROP_AUTH_TYPE);
    // basic authentication
    if (authType.equals(AUTH_TYPE_BASIC)) {
      user = credential.get(PROP_USER);
      pwd = credential.get(PROP_PWD);
    }
    // OAuth authentication
    if (authType.equals(AUTH_TYPE_OAUTH)) {
      companyId = credential.get(PROP_COMPANY_ID);
      clientKey = credential.get(PROP_CLIENT_KEY);
      privateKey = credential.get(PROP_PRIVATE_KEY);
      tokenServiceURL = credential.get(PROP_TOKEN_SERVICE_URL);
      tokenServiceUser = credential.get(PROP_TOKEN_SERVICE_USER);
    }
  }

  /**
   * SMAL assertion string is used to generate the OAuth access token. The private key generated during OAuth client
   * registration is required for generating the assertion. The default validity of registration is 365 days.
   */
  private String generateSMALAssertion() {
    String idpURL = tokenServiceURL.split("/token")[0] + "/idp";
    StringBuilder sb = new StringBuilder();
    sb.append("client_id=").append(clientKey);
    sb.append("&user_id=").append(tokenServiceUser);
    sb.append("&token_url=").append(tokenServiceURL);
    sb.append("&private_key=").append(privateKey);
    String payload = sb.toString();
    HttpParams params = new HttpParams(HttpMethod.POST);
    params.setContentType(MediaType.APPLICATION_FORM_URLENCODED.toString());
    params.setPayload(payload);
    WrappedResponse response = httpClient.execute(idpURL, params);
    if (!response.isSuccess()) {
      logger.error("Failed to generate SAML assertion, message: " + response.getMessage());
      return null;
    }
    return response.getContent();
  }

  private String generateAccessToken() {
    // no need to send post request again if smal2 assertion already exists
    if (assertion == null) {
      assertion = generateSMALAssertion();
    }
    StringBuilder sb = new StringBuilder();
    sb.append("company_id=").append(companyId);
    sb.append("&client_id=").append(clientKey);
    sb.append("&grant_type=urn:ietf:params:oauth:grant-type:saml2-bearer");
    sb.append("&assertion=").append(assertion);
    String payload = sb.toString();
    // send HTTP post request to get the access token
    HttpParams params = new HttpParams(HttpMethod.POST);
    params.setContentType(MediaType.APPLICATION_FORM_URLENCODED.toString());
    params.setAcceptType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    params.setPayload(payload);
    WrappedResponse response = httpClient.execute(tokenServiceURL, params);
    if (!response.isSuccess()) {
      logger.error("Request OAuth Access Token failed. Reason: " + response.getMessage());
      return null;
    }
    String jsonString = response.getContent();
    JSONObject jo = JSONObject.parseObject(jsonString);
    return (jo == null) ? null : (String) jo.get("access_token");
  }

  private boolean isAccessTokenValid(String accessToken, String validateURL) {
    if (accessToken == null) {
      return false;
    }
    String authString = "Bearer " + accessToken;
    HttpParams params = new HttpParams(HttpMethod.GET);
    params.setAuthorization(authString);
    params.setAcceptType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    WrappedResponse response = httpClient.execute(validateURL, params);
    return response.isSuccess();
  }

  /**
   * Get authentication string from destination configuration
   * 
   * @return authentication string
   */
  public String getAuthentication() {
    Map<String, String> credential = sessionParams.getCredential(CREDENTIAL_NAME);
    if (credential == null) {
      logger.error("Failed to build authentication: credentail not configured.");
      return null;
    }

    init(credential);

    String result = null;
    if (AUTH_TYPE_BASIC.equals(authType)) {
      String raw = user + ":" + pwd;
      result = "Basic " + Base64Utils.encodeToString(raw.getBytes());
    } else if (AUTH_TYPE_OAUTH.equals(authType)) {
      String validateURL = tokenServiceURL.split("/token")[0] + "/validate";
      if (accessToken == null || !isAccessTokenValid(accessToken, validateURL)) {
        accessToken = generateAccessToken();
      }
      result = "Bearer " + accessToken;
    }
    return result;
  }

  // get service root URI for building absolute UIR of SF OData query
  public String getServiceRootURI() {
    Map<String, String> credential = sessionParams.getCredential(CREDENTIAL_NAME);
    if (credential == null) {
      logger.error("Failed to build authentication: credential not configured.");
      return null;
    }
    return CommonUtils.formatURL(credential.get(PROP_SERVICE_URL));
  }

}
