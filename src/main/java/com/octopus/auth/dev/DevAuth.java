package com.octopus.auth.dev;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.octopus.Profiles;
import com.octopus.auth.Authentication;

@Component
@Profile({ Profiles.DEV, Profiles.TEST })
public class DevAuth implements Authentication {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public static final String USER_NAME = "dev";

  @Override
  public String login() {
    // TODO initiate login session parameters
    return USER_NAME;
  }

  @Override
  public void logout(HttpServletRequest request) {
    try {
      if (request.getSession() != null) {
        request.getSession().invalidate();
      }
      logger.info("You have successfully logged out.");
    } catch (IllegalStateException e) {
      logger.info("Failed to log out. Reason: " + e.getMessage());
    }
  }

}
