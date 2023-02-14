package com.afetyardim.afetyardim.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SiteUpdateDTO {

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDateTime createDateTime;

  private String update;

}
