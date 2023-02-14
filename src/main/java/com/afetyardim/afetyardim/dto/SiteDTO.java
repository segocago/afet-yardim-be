package com.afetyardim.afetyardim.dto;

import com.afetyardim.afetyardim.model.ActiveStatus;
import com.afetyardim.afetyardim.model.Location;
import com.afetyardim.afetyardim.model.SiteStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SiteDTO {

  private long id;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDateTime createDateTime;

  private String name;

  private Location location;

  private String description;

  private String contactInformation;

  private List<SiteUpdateDTO> updates = new ArrayList<>();

  private List<SiteStatus> lastSiteStatuses = new ArrayList<>();

  private ActiveStatus activeStatus = ActiveStatus.UNKNOWN;
}
