package com.afetyardim.afetyardim.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.vladmihalcea.hibernate.type.json.JsonType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;


@Entity
@Getter
@Setter
@TypeDef(name = "json", typeClass = JsonType.class)
@Table(indexes = @Index(name = "city_index", columnList = "city"))
public class Site {

    public Site() {

      this.createDateTime = LocalDateTime.now();
      this.description = "Bilinmiyor";
      this.contactInformation="Bilinmiyor";
      this.organizer = "Bilinmiyor";
      this.verified = true;
      this.type = SiteType.SUPPLY;
    }

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

  @Column(length = 1024)
  private String description;

  private String contactInformation;

  @Type(type = "json")
  @Column(columnDefinition = "jsonb")
  private List<SiteUpdate> updates = new ArrayList<>();

  @Type(type = "json")
  @Column(columnDefinition = "jsonb")
  private List<SiteStatus> lastSiteStatuses = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  private SiteType type = SiteType.SUPPLY;

  private boolean verified = false;

  @Column(columnDefinition = "boolean default false")
  @Deprecated
  private Boolean active = false;

  @Column(length = 32, columnDefinition = "varchar(255) default 'UNKNOWN'")
  @Enumerated(EnumType.STRING)
  private ActiveStatus activeStatus = ActiveStatus.UNKNOWN;

  public boolean didSiteHaveUpdateInLastPeriod(){
    Optional<SiteUpdate> lastSiteUpdate = getLastSiteUpdate();
    LocalDateTime lastDay = LocalDateTime.now().minusDays(1);
    if(lastSiteUpdate.isPresent()){
      return lastSiteUpdate.get().getCreateDateTime().isAfter(lastDay);
    }
    return getCreateDateTime().isAfter(lastDay);

  }

  public Optional<SiteUpdate> getLastSiteUpdate(){
    return updates.size() == 0 ? Optional.empty() :  Optional.of(updates.get(updates.size() - 1));
  }



  public void addSiteUpdate(SiteUpdate siteUpdate) {
    if (getUpdates() == null) {
      updates = new ArrayList<>();
    }
    updates.add(siteUpdate);
  }

}
