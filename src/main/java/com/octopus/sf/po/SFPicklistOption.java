package com.octopus.sf.po;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sf_picklist_option")
public class SFPicklistOption {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private Long id;

  @Column(name = "option_id", nullable = false)
  private String optionId;

  @Column(name = "picklist_id", nullable = false)
  private String picklistId;

  @Column(name = "default_label", nullable = false)
  private String defaultLabel;

  public String getPicklistId() {
    return picklistId;
  }

  public void setPicklistId(String picklistId) {
    this.picklistId = picklistId;
  }

  public String getOptionId() {
    return optionId;
  }

  public void setOptionId(String optionId) {
    this.optionId = optionId;
  }

  public String getDefaultLabel() {
    return defaultLabel;
  }

  public void setDefaultLabel(String defaultLabel) {
    this.defaultLabel = defaultLabel;
  }

}
