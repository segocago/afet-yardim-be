package com.afetyardim.afetyardim.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;


@Entity
@Getter
@Setter
public class SiteUpdate {

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @CreationTimestamp
  private LocalDateTime createDateTime;

  private String update;

  @Enumerated(EnumType.STRING)
  private SiteUpdateType siteUpdateType = SiteUpdateType.DEFAULT;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "site_id", nullable = false)
  @JsonIgnore
  private Site site;

}
