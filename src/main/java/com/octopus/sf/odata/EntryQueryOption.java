package com.octopus.sf.odata;

import java.util.LinkedHashSet;
import java.util.Set;

public class EntryQueryOption {

  private Set<String> selectOptions;

  private Set<String> expandOptions;

  private String acceptFormat;

  public enum AcceptFormatEnum {
    XML("xml"), JSON("json"), ATOM("atom");

    private final String value;

    private AcceptFormatEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }
  
  public Set<String> getSelectOptions() {
    return selectOptions;
  }

  public void addSelectOption(String option) {
    if (selectOptions == null) {
      selectOptions = new LinkedHashSet<String>();
    }
    selectOptions.add(option);
  }

  public Set<String> getExpandOptions() {
    return expandOptions;
  }

  public void addExpandOption(String option) {
    if (expandOptions == null) {
      expandOptions = new LinkedHashSet<String>();
    }
    expandOptions.add(option);
  }

  public String getAcceptFormat() {
    return acceptFormat;
  }

  // to overwrite the content of accept header used by an OData HTTP request
  public void setAcceptFormat(AcceptFormatEnum acceptFormat) {
    this.acceptFormat = acceptFormat.toString();
  }

}
