package com.afetyardim.afetyardim.service;


import com.afetyardim.afetyardim.model.Site;
import com.afetyardim.afetyardim.model.SiteStatus;
import com.afetyardim.afetyardim.model.SiteUpdate;
import com.afetyardim.afetyardim.repository.SiteRepository;
import com.afetyardim.afetyardim.repository.SiteUpdateRepository;
import java.util.Collection;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SiteService {

  private final SiteRepository siteRepository;

  private final SiteUpdateRepository siteUpdateRepository;

  public Site createSite(Site newSite) {
    return siteRepository.save(newSite);
  }

  public Collection<Site> getSites(Optional<String> cityFilter) {

    if (cityFilter.isPresent()) {
      return siteRepository.findByLocationCity(cityFilter.get());

    }
    return siteRepository.findAll();
  }

  public SiteUpdate addSiteUpdate(long siteId, SiteUpdate newSiteUpdate) {
    Site site = getSite(siteId);
    newSiteUpdate.setSite(site);

    if (newSiteUpdate.getSiteStatus() != SiteStatus.DEFAULT) {
      site.setLastSiteStatus(newSiteUpdate.getSiteStatus());
    }
    return siteUpdateRepository.save(newSiteUpdate);
  }

  public Site getSite(long siteId) {
    Optional<Site> site = siteRepository.findById(siteId);
    if (site.isEmpty()) {
      throw new RuntimeException(String.format("Site not found with id: %s", siteId));
    }
    return site.get();
  }
}
