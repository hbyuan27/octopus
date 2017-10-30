package com.octopus.sf.odata.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeSet implements BatchRequestPart {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final String boundary = "changeset_" + UUID.randomUUID().toString();

  private List<ChangeRequest> requests = new ArrayList<ChangeRequest>();

  @Override
  public String getRawString() {
    if (requests.size() == 0) {
      logger.error("the ChangeSet contains no ChangeRequest");
      return "";
    }
    StringBuilder sb = new StringBuilder();
    sb.append("Content-Type: multipart/mixed; boundary=").append(boundary).append("\n\n");
    for (ChangeRequest changeRequest : requests) {
      sb.append("--").append(boundary).append("\n");
      sb.append("Content-Type:application/http").append("\n");
      sb.append("Content-Transfer-Encoding:binary").append("\n\n");
      sb.append(changeRequest.getRawString()).append("\n");
    }
    sb.append("--").append(boundary).append("--");
    return sb.toString();
  }

  public void addChangeRequest(ChangeRequest changeRequest) {
    requests.add(changeRequest);
  }

}
