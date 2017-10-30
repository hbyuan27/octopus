package com.octopus.sf.common;

import java.io.Closeable;
import java.io.IOException;

public class CommonUtils {

  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
      }
    }
  }

  /**
   * if a url string is not end with "/", concatenate one
   * 
   * @param url
   * @return
   */
  public static String formatURL(String url) {
    if (url != null && !url.endsWith("/")) {
      url = url.concat("/");
    }
    return url;
  }
}
