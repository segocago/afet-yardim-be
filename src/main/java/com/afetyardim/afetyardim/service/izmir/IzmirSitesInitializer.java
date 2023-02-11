package com.afetyardim.afetyardim.service.izmir;

import com.afetyardim.afetyardim.model.Location;
import com.afetyardim.afetyardim.model.Site;
import com.afetyardim.afetyardim.model.SiteType;
import com.afetyardim.afetyardim.service.SiteService;
import com.afetyardim.afetyardim.service.common.SpreadSheetUtils;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class IzmirSitesInitializer {
    @Value("${google.api.key}")
    private String API_KEY;

    private final SpreadSheetUtils spreadSheetUtils;
    private final SiteService siteService;

    private final static String IZMIR_SPREAD_SHEET_ID = "1pAUwGOfuu6mRUnsHs7uQrggAu8GQm6Z-r6M25lgBCNY";

    private final static String IZMIR_SPREAD_SHEET_RANGE = "A1:H100";

    public void createIzmirSites() throws IOException {

        log.info("Start Izmir spread sheet create");

        Spreadsheet spreadsheet = spreadSheetUtils.getSpreadSheet(IZMIR_SPREAD_SHEET_ID, IZMIR_SPREAD_SHEET_RANGE);
        List<RowData> rows = spreadsheet.getSheets().get(0).getData().get(0).getRowData();
        //Remove header row
        rows.remove(0);
        List<Site> sites = new ArrayList<>();
        for (int i = 0; i < rows.size() - 2; i++) {
            Site site = createIzmirSite(rows.get(i));
            if (Objects.isNull(site)) {
                break;
            }
            sites.add(site);
        }
    }

    private Site createIzmirSite(RowData rowData) {
        String siteName = (String) rowData.getValues().get(1).get("formattedValue");
        if (siteName == null) {
            return null;
        }
        String phone = (String) rowData.getValues().get(3).get("formattedValue");
        String description = (String) rowData.getValues().get(5).get("formattedValue");

        Site site = new Site();
        site.setName(siteName);
        site.setActive(false);
        site.setContactInformation(phone);
        site.setVerified(true);
        site.setType(SiteType.SUPPLY);
        site.setDescription(description);

        site.setLocation(buildSiteLocation(rowData));
        return site;
    }

    private Location buildSiteLocation(RowData rowData) {
        String siteName = (String) rowData.getValues().get(1).get("formattedValue");
        String district = (String) rowData.getValues().get(0).get("formattedValue");
        String mapUrl = (String) rowData.getValues().get(2).get("formattedValue");
        Location location = new Location();
        location.setDistrict(district);
        location.setCity("İzmir");
        location.setAdditionalAddress(siteName);
        location.setLongitude(null);
        location.setLatitude(null);
        return location;
    }

}
