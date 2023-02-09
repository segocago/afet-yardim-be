package com.afetyardim.afetyardim.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SiteUpdate {

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDateTime createDateTime;

  private String update;

  private SiteStatus siteStatus = SiteStatus.DEFAULT;


}
