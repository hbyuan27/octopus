package com.octopus.auth.dev;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.octopus.Profiles;
import com.octopus.auth.SessionParams;
import com.octopus.sf.SFAuthConstants;

@Component
@Profile(value = { Profiles.DEV, Profiles.TEST })
public class DevSessionParams implements SessionParams {

  @Autowired
  private DevSFDestination devSFDest;

  @PostConstruct
  private void init() {
    credentials.put(SFAuthConstants.CREDENTIAL_NAME, devSFDest.getAllProperties());
  }

  private final String userName = DevAuth.USER_NAME;

  private final Map<String, Map<String, String>> credentials = new HashMap<String, Map<String, String>>(1);

  @Override
  public String getUserName() {
    return userName;
  }

  @Override
  public Map<String, String> getCredential(String name) {
    return credentials.get(name);
  }

}
