package com.octopus.sf.odata.batch;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchRequest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final String boundary;

  private final String payload;

  public BatchRequest(String payload, String boundary) {
    this.payload = payload;
    this.boundary = boundary;
  }

  public BatchRequest(List<BatchRequestPart> parts) {
    this.boundary = "batch_" + UUID.randomUUID().toString();
    this.payload = initPayload(parts);
  }

  private String initPayload(List<BatchRequestPart> parts) {
    if (parts == null || parts.isEmpty()) {
      logger.error("invalid BatchRequest, contains no BatchPart.");
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (BatchRequestPart part : parts) {
      sb.append("--").append(boundary).append("\n");
      sb.append(part.getRawString()).append("\n\n");
    }
    sb.append("--").append(boundary).append("--");
    return sb.toString();
  }

  public String getPayload() {
    return payload;
  }

  public String getBoundary() {
    return boundary;
  }

}
