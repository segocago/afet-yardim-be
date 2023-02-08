package com.afetyardim.afetyardim;

import com.afetyardim.afetyardim.model.Location;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class SiteListDTO {
  private long id;

  private LocalDateTime createDateTime;

  private String name;

  private Location location;

  private String organizer;

  private String description;

  private String contactInformation;
}
