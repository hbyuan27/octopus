package com.octopus.sf;

public final class SFAuthConstants {

  private SFAuthConstants() {
  }

  public static final String CREDENTIAL_NAME = "sf_odata";

  /**
   * destination authentication type
   */
  public static final String AUTH_TYPE_BASIC = "BasicAuthentication";

  public static final String AUTH_TYPE_OAUTH = "OAuth2SAMLBearerAssertion";

  /**
   * SF OData Destination Property Name
   */
  public static final String PROP_AUTH_TYPE = "Authentication";

  public static final String PROP_SERVICE_URL = "URL";

  public static final String PROP_USER = "User";

  public static final String PROP_PWD = "Password";

  public static final String PROP_COMPANY_ID = "companyId";

  public static final String PROP_CLIENT_KEY = "clientKey";

  public static final String PROP_PRIVATE_KEY = "privateKey";

  public static final String PROP_TOKEN_SERVICE_URL = "tokenServiceURL";

  public static final String PROP_TOKEN_SERVICE_USER = "tokenServiceUser";
}
