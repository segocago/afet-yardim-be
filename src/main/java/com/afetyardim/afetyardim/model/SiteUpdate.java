package com.afetyardim.afetyardim.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteUpdate {

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDateTime createDateTime;

  private String update;

  private List<SiteStatus> siteStatuses;


}
