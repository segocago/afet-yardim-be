package com.afetyardim.afetyardim.mapper;

import com.afetyardim.afetyardim.dto.SiteDTO;
import com.afetyardim.afetyardim.dto.SiteUpdateDTO;
import com.afetyardim.afetyardim.model.Site;
import com.afetyardim.afetyardim.model.SiteUpdate;
import java.util.List;

public class SiteMapper {

  private static final int MAX_NUMBER_OF_SITE_UPDATES = 5;

  public static SiteDTO convertModelToDTO(Site site){

    SiteDTO siteDTO = new SiteDTO();
    siteDTO.setId(site.getId());
    siteDTO.setCreateDateTime(site.getCreateDateTime());
    siteDTO.setName(site.getName());
    siteDTO.setLocation(site.getLocation());
    siteDTO.setDescription(site.getDescription());
    siteDTO.setContactInformation(site.getContactInformation());

    List<SiteUpdate> updates = site.getUpdates();
    siteDTO.setUpdates(site.getUpdates()
        .subList(Math.max(updates.size()-MAX_NUMBER_OF_SITE_UPDATES,0),updates.size())
        .stream().map(siteUpdate -> convertModelToDTO(siteUpdate))
        .toList());
    siteDTO.setLastSiteStatuses(site.getLastSiteStatuses());
    siteDTO.setActiveStatus(site.getActiveStatus());
    return siteDTO;

  }

  public static SiteUpdateDTO convertModelToDTO(SiteUpdate siteUpdate){

    SiteUpdateDTO siteUpdateDTO = new SiteUpdateDTO();
    siteUpdateDTO.setCreateDateTime(siteUpdate.getCreateDateTime());
    siteUpdateDTO.setUpdate(siteUpdate.getUpdate());
    return siteUpdateDTO;

  }
}
