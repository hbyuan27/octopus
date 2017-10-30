package com.octopus.sf.odata.batch;

import java.util.ArrayList;
import java.util.List;

public class BatchResponse {

  private List<BatchResponsePart> parts = new ArrayList<BatchResponsePart>();

  public void addPart(BatchResponsePart part) {
    this.parts.add(part);
  }

  public void addParts(List<BatchResponsePart> parts) {
    this.parts.addAll(parts);
  }

  public List<BatchResponsePart> getParts() {
    return parts;
  }

  public BatchResponsePart getPart(int index) {
    return parts.get(index);
  }
}
