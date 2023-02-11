package com.afetyardim.afetyardim.service.izmir;

import com.afetyardim.afetyardim.model.Location;
import com.afetyardim.afetyardim.model.Site;
import com.afetyardim.afetyardim.model.SiteType;
import com.afetyardim.afetyardim.service.common.SpreadSheetUtils;
import com.google.api.services.sheets.v4.model.RowData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class IzmirSitesInitializer {
    private final SpreadSheetUtils spreadSheetUtils;

    public Optional<Site> createIzmirSite(RowData rowData) {
        String siteName = (String) rowData.getValues().get(1).get("formattedValue");
        Optional<Location> location = buildSiteLocation(rowData);
        if (Objects.isNull(siteName)) {
            return Optional.empty();
        }

        if (location.isEmpty()) {
            log.warn("Location is null for {}", siteName);
            return Optional.empty();
        }

        String phone = (String) rowData.getValues().get(3).get("formattedValue");
        Site site = new Site();
        site.setName(siteName);
        site.setActive(false);
        site.setContactInformation(phone);
        site.setVerified(true);
        site.setType(SiteType.SUPPLY);
        site.setLocation(location.get());
        return Optional.of(site);
    }

    private Optional<Location> buildSiteLocation(RowData rowData) {
        String mapUrl = (String) rowData.getValues().get(2).get("formattedValue");

        if (Objects.isNull(mapUrl)) {
            return Optional.empty();
        }
        String district = (String) rowData.getValues().get(0).get("formattedValue");
        Location location = new Location();
        location.setDistrict(district);
        location.setCity("İzmir");
        location.setAdditionalAddress("Bu alana adres tarifi al butonunu kullanınız.");
        try {
            List<Double> coordinates = spreadSheetUtils.getCoordinatesByUrl(mapUrl);
            location.setLatitude(coordinates.get(0));
            location.setLongitude(coordinates.get(1));
        } catch (Exception exception) {
            log.error("Could not get coordinates by map url {}", mapUrl, exception);
            return Optional.empty();
        }
        return Optional.of(location);
    }

}
