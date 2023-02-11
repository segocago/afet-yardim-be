package com.afetyardim.afetyardim.service.izmir;

import com.afetyardim.afetyardim.model.Location;
import com.afetyardim.afetyardim.model.Site;
import com.afetyardim.afetyardim.model.SiteType;
import com.afetyardim.afetyardim.service.SiteService;
import com.afetyardim.afetyardim.service.common.SpreadSheetUtils;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class IzmirSitesInitializer {
    private final SpreadSheetUtils spreadSheetUtils;

    public Optional<Site> createIzmirSite(RowData rowData) throws IOException {

      String siteName = (String) rowData.getValues().get(1).get("formattedValue");
      if (Objects.isNull(siteName)) {
        return Optional.empty();
      }
      Location location = buildSiteLocation(rowData);
      if(location == null){
        log.warn("Could not create a new site for: {}",siteName);
        return Optional.empty();
      }

      String phone = (String) rowData.getValues().get(3).get("formattedValue");
      Site site = new Site();
      site.setName(siteName);
      site.setActive(false);
      site.setContactInformation(phone);
      site.setVerified(true);
      site.setType(SiteType.SUPPLY);
      site.setLocation(location);
      return Optional.of(site);
    }

    private Location buildSiteLocation(RowData rowData) throws IOException {
        String mapUrl = (String) rowData.getValues().get(2).get("formattedValue");

        if (Objects.isNull(mapUrl)) {
            return null;
        }
        String district = (String) rowData.getValues().get(0).get("formattedValue");
        Location location = new Location();
        location.setDistrict(district);
        location.setCity("İzmir");
        location.setAdditionalAddress("Bu alana adres tarifi al butonunu kullanınız.");
        List<Double> coordinates = spreadSheetUtils.getCoordinatesByUrl(mapUrl);
        if (coordinates.size() < 2) {
            return null;
        }
        location.setLatitude(coordinates.get(0));
        location.setLongitude(coordinates.get(1));
        return location;
    }

}
