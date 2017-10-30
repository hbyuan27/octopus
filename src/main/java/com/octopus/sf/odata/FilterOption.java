package com.octopus.sf.odata;

public class FilterOption {

  private final String filterClause;

  private final String filterablePropName;

  private final QueryFilterEnum operator;

  private final String filterValue;

  public FilterOption(String filterablePropName, QueryFilterEnum operator, String filterValue) {
    this.filterablePropName = filterablePropName;
    this.operator = operator;
    this.filterValue = filterValue;
    this.filterClause = null;
  }

  public FilterOption(String filterClause) {
    this.filterClause = filterClause;
    this.filterablePropName = null;
    this.operator = null;
    this.filterValue = null;
  }

  public String getFilterablePropName() {
    return filterablePropName;
  }

  public QueryFilterEnum getOperator() {
    return operator;
  }

  public String getFilterValue() {
    return filterValue;
  }

  public String getFilterClause() {
    return filterClause;
  }

}
