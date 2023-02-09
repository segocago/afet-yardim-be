package com.afetyardim.afetyardim.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.vladmihalcea.hibernate.type.json.JsonType;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;


@Entity
@Getter
@Setter
@TypeDef(name = "json", typeClass = JsonType.class)
public class Site {

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @CreationTimestamp
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDateTime createDateTime;

  private String name;

  @Embedded
  private Location location;

  private String organizer;

  private String description;

  private String contactInformation;

  @Type(type = "json")
  @Column(columnDefinition = "jsonb")
  private Set<SiteUpdate> updates = new HashSet<>();

  @Enumerated(EnumType.STRING)
  private SiteStatus lastSiteStatus = SiteStatus.DEFAULT;

  private boolean isVerified = false;

  public void addSiteUpdate(SiteUpdate siteUpdate) {
    if (getUpdates() == null) {
      updates = new HashSet<>();
    }
    updates.add(siteUpdate);

  }


}
