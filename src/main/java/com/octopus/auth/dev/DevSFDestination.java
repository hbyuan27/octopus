package com.octopus.auth.dev;

import static com.octopus.sf.SFAuthConstants.*;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.octopus.Profiles;

@Component
@PropertySource("classpath:dev-destination-sf.properties")
@Profile({ Profiles.DEV, Profiles.TEST })
public class DevSFDestination {

  private Map<String, String> properties = new HashMap<String, String>();

  @Value("${AUTH_TYPE}")
  private String authType;

  @Value("${CLIENT_KEY}")
  private String clientKey;

  @Value("${COMPANY_ID}")
  private String companyId;

  @Value("${PRIVATE_KEY}")
  private String privateKey;

  @Value("${PWD}")
  private String pwd;

  @Value("${SERVICE_URL}")
  private String serviceUrl;

  @Value("${TOKEN_SERVICE_URL}")
  private String tokenServiceUrl;

  @Value("${TOKEN_SERVICE_USER}")
  private String tokenServiceUser;

  @Value("${USER}")
  private String user;

  @PostConstruct
  private void initProperties() {
    properties.put(PROP_AUTH_TYPE, authType);
    properties.put(PROP_CLIENT_KEY, clientKey);
    properties.put(PROP_COMPANY_ID, companyId);
    properties.put(PROP_PRIVATE_KEY, privateKey);
    properties.put(PROP_PWD, pwd);
    properties.put(PROP_SERVICE_URL, serviceUrl);
    properties.put(PROP_TOKEN_SERVICE_URL, tokenServiceUrl);
    properties.put(PROP_TOKEN_SERVICE_USER, tokenServiceUser);
    properties.put(PROP_USER, user);
  }

  public String getProperty(String paramString) {
    return properties.get(paramString);
  }

  public Map<String, String> getAllProperties() {
    return properties;
  }

}
