package com.afetyardim.afetyardim.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class SiteStatus {

  private SiteStatusType siteStatusType;

  private SiteStatusLevel siteStatusLevel;

  public enum SiteStatusLevel {

    NO_NEED_REQUIRED, NEED_REQUIRED, URGENT_NEED_REQUIRED, UNKNOWN;
  }
}



