package com.afetyardim.afetyardim.controller;

import com.afetyardim.afetyardim.model.Site;
import com.afetyardim.afetyardim.model.SiteUpdate;
import com.afetyardim.afetyardim.service.SiteService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/sites")
@AllArgsConstructor
public class SiteController {

  private final SiteService siteService;

  @PostMapping
  public Site createSite(@RequestBody Site newSite) {
    newSite.setId(0);
    newSite.setCreateDateTime(null);
    return siteService.createSite(newSite);
  }

  @GetMapping
  public Collection<Site> getSites(@RequestParam Optional<String> cityFilter) {
    Collection<Site> sites = siteService.getSites(cityFilter);
    return sites;
  }

  @GetMapping("/{siteId}")
  public Site getSite(@PathVariable long siteId) {
    return siteService.getSite(siteId);
  }

  @PostMapping("/{siteId}/updates")
  public SiteUpdate addSiteUpdate(@PathVariable long siteId, @RequestBody SiteUpdate newSiteUpdate) {
    newSiteUpdate.setId(0);
    newSiteUpdate.setCreateDateTime(null);
    return siteService.addSiteUpdate(siteId, newSiteUpdate);
  }

}
