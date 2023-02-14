package com.afetyardim.afetyardim.service;


import com.afetyardim.afetyardim.model.Site;
import com.afetyardim.afetyardim.model.SiteUpdate;
import com.afetyardim.afetyardim.repository.SiteRepository;
import java.util.Collection;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class SiteService {

  private final SiteRepository siteRepository;
  private final CacheManager cacheManager;

  private final String SITES_CACHE = "sites";
  private final long SITES_CACHE_EVICTION_PERIOD_IN_MILLIS =  3 * 60 * 1000;

  public Site createSite(Site newSite) {
    return siteRepository.save(newSite);
  }

  @Cacheable(value = SITES_CACHE)
  public Collection<Site> getSites(Optional<String> cityFilter) {

    if (cityFilter.isPresent()) {
      return siteRepository.findByLocationCity(cityFilter.get());
    }
    return siteRepository.findAll();
  }

  public Site addSiteUpdate(long siteId, SiteUpdate newSiteUpdate) {
    Site site = getSite(siteId);
    site.addSiteUpdate(newSiteUpdate);
    site.setLastSiteStatuses(newSiteUpdate.getSiteStatuses());
    return siteRepository.save(site);
  }

  public Site getSite(long siteId) {
    Optional<Site> site = siteRepository.findById(siteId);
    if (site.isEmpty()) {
      throw new RuntimeException(String.format("Site not found with id: %s", siteId));
    }
    return site.get();
  }

  public void saveAllSites(Collection<Site> sites) {
    siteRepository.saveAll(sites);
  }

  @Scheduled(fixedRate = SITES_CACHE_EVICTION_PERIOD_IN_MILLIS)
  public void clearCache() {
    cacheManager.getCache(SITES_CACHE).clear();
    log.info("Cleared sites cache");
  }
}
