package com.octopus.sf.odata;

public enum QueryFilterEnum {
  EQUAL("eq"), NOT_EQUAL("ne"), GREATER_THAN("gt"), GREATER_THAN_OR_EQUAL("ge"), LESS_THAN("lt"), LESS_THAN_OR_EQUAL(
      "le");

  private final String value;

  private QueryFilterEnum(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
