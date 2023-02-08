package com.afetyardim.afetyardim.model;

import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Location {

  private String city;

  private String district;

  private String additionalAddress;

  private Double longitude;

  private Double latitude;


}
