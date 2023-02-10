package com.afetyardim.afetyardim.model;

import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class SiteStatus {

  private SiteStatusType siteStatusType;

  private SiteStatusLevel siteStatusLevel;

  public enum SiteStatusLevel {

    NO_NEED_REQUIRED, NEED_REQUIRED, URGENT_NEED_REQUIRED;
  }
}



