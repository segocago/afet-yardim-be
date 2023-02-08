package com.afetyardim.afetyardim.model;


import java.time.LocalDateTime;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;


@Entity
@Getter
@Setter
public class Site {

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @CreationTimestamp
  private LocalDateTime createDateTime;

  private String name;

  @Embedded
  private Location location;

  private String organizer;

  private String description;

  private String contactInformation;

  @OneToMany(mappedBy = "site", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
  private Set<SiteUpdate> updates;


}
