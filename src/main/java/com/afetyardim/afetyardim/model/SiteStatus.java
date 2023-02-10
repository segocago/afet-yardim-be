package com.afetyardim.afetyardim.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteStatus {

  public SiteStatus() {
  }

  public SiteStatus(SiteStatusType type, SiteStatusLevel level) {
    this.siteStatusType = type;
    this.siteStatusLevel = level;
  }

  private SiteStatusType siteStatusType;

  private SiteStatusLevel siteStatusLevel;

  public enum SiteStatusLevel {

    NO_NEED_REQUIRED, NEED_REQUIRED, URGENT_NEED_REQUIRED
  }
}



