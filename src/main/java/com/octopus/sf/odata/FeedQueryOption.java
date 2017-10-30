package com.octopus.sf.odata;

import java.util.LinkedHashSet;
import java.util.Set;

public class FeedQueryOption extends EntryQueryOption {

  private Set<String> orderByOptions;

  private boolean orderByDesc = false;

  private Integer top;

  private Integer skip;

  private FilterOption filterOption;

  public Set<String> getOrderByOptions() {
    return orderByOptions;
  }

  public void addOrderByOption(String option) {
    if (orderByOptions == null) {
      orderByOptions = new LinkedHashSet<String>();
    }
    orderByOptions.add(option);
  }

  public boolean isOrderByDesc() {
    return orderByDesc;
  }

  public void setOrderByDesc(boolean orderByDesc) {
    this.orderByDesc = orderByDesc;
  }

  public Integer getTop() {
    return top;
  }

  public void setTop(Integer top) {
    this.top = top;
  }

  public Integer getSkip() {
    return skip;
  }

  public void setSkip(Integer skip) {
    this.skip = skip;
  }

  public FilterOption getFilterOption() {
    return filterOption;
  }

  public void setFilterOption(FilterOption filterOption) {
    this.filterOption = filterOption;
  }

}
