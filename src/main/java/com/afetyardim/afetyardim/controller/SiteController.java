package com.afetyardim.afetyardim.controller;

import com.afetyardim.afetyardim.SiteListDTO;
import com.afetyardim.afetyardim.model.Site;
import com.afetyardim.afetyardim.model.SiteUpdate;
import com.afetyardim.afetyardim.service.SiteService;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sites")
@AllArgsConstructor
public class SiteController {

  private final SiteService siteService;

  @PostMapping("")
  public Site createSite(@RequestBody Site newSite) {
    newSite.setId(0);
    newSite.setCreateDateTime(null);
    return siteService.createSite(newSite);
  }

  @GetMapping("")
  public Collection<SiteListDTO> getSites(@RequestParam Optional<String> cityFilter) {
    Collection<Site> sites = siteService.getSites(cityFilter);
    return convertModelsToDTO(sites);
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

  private Collection<SiteListDTO> convertModelsToDTO(Collection<Site> sites) {
    return sites.stream().map(this::convertModelToDTO).collect(Collectors.toSet());
  }

  private SiteListDTO convertModelToDTO(Site site) {
    SiteListDTO siteListDTO = new SiteListDTO();
    siteListDTO.setId(site.getId());
    siteListDTO.setCreateDateTime(site.getCreateDateTime());
    siteListDTO.setName(site.getName());
    siteListDTO.setLocation(site.getLocation());
    siteListDTO.setOrganizer(site.getOrganizer());
    siteListDTO.setDescription(site.getOrganizer());
    siteListDTO.setContactInformation(site.getContactInformation());
    return siteListDTO;
  }


}
