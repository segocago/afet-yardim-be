package com.afetyardim.afetyardim.service.izmir;

import static com.afetyardim.afetyardim.util.SiteUtils.compareFloats;
import com.afetyardim.afetyardim.model.Location;
import com.afetyardim.afetyardim.model.Site;
import com.afetyardim.afetyardim.model.SiteStatus;
import com.afetyardim.afetyardim.model.SiteStatusType;
import com.afetyardim.afetyardim.model.SiteType;
import com.afetyardim.afetyardim.model.SiteUpdate;
import com.afetyardim.afetyardim.service.SiteService;
import com.afetyardim.afetyardim.service.common.SpreadSheetUtils;
import com.afetyardim.afetyardim.util.SiteUtils;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IzmirSitesParser {
    @Value("${google.api.key}")
    private String API_KEY;

    private final SpreadSheetUtils spreadSheetUtils;
    private final SiteService siteService;

    private final static String IZMIR_SPREAD_SHEET_ID = "1pAUwGOfuu6mRUnsHs7uQrggAu8GQm6Z-r6M25lgBCNY";

    private final static String IZMIR_SPREAD_SHEET_RANGE = "A1:G100";

    public void parseIzmirSpreadsheet() throws IOException {

        log.info("Start Izmir spread sheet parsing");

        Spreadsheet spreadsheet = spreadSheetUtils.getSpreadSheet(IZMIR_SPREAD_SHEET_ID, IZMIR_SPREAD_SHEET_RANGE);
        List<RowData> rows = spreadsheet.getSheets().get(0).getData().get(0).getRowData();
        //Remove header row
        rows.remove(0);
        Collection<Site> izmirSites = siteService.getSites(Optional.of("İzmir"), Optional.empty());
        List<Site> newSites = new ArrayList<>();


        for (int i = 0; i < rows.size(); i++) {
            RowData rowData = rows.get(i);
            try {

                String siteName = (String) rowData.getValues().get(1).get("formattedValue");
                if (siteName == null) {
                    continue;
                }
                Optional<Site> existingSite = SiteUtils.findSiteByName(siteName, izmirSites);

                if (existingSite.isEmpty()) {
                    Site newSite = createIzmirSite(rowData);
                    newSites.add(newSite);
                } else {
                    updateSite(rowData, existingSite.get());
                }
            }catch (Exception exception){
                log.warn("Error while parsing row, {}",rowData,exception);
            }
        }
//        izmirSites.addAll(newSites);
        siteService.saveAllSites(izmirSites);

    }

    public void updateSite(RowData rowData, Site site) {

        String phone = (String) rowData.getValues().get(3).get("formattedValue");
        if (phone != null) {
            site.setContactInformation(phone);
        }
        String lastUpdateTime = (String) rowData.getValues().get(4).get("formattedValue");

        String needStatusText = (String) rowData.getValues().get(5).get("formattedValue");
        Color needStatusColor = rowData.getValues().get(5).getUserEnteredFormat().getBackgroundColor();
        SiteStatus.SiteStatusLevel needLevel = convertToSiteStatusLevelForIzmir(needStatusColor);

        String note ="";
        try {
            note = (String) rowData.getValues().get(6).get("formattedValue");
        }catch(Exception exception){
            log.warn("Error while parsing note column for izmir site: {}",site.getName(),exception);
        }


        List<SiteStatus> newSiteStatuses = generateSiteStatus(needLevel);
        site.setLastSiteStatuses(newSiteStatuses);
        site.setActive(needLevel == SiteStatus.SiteStatusLevel.NEED_REQUIRED ||
            needLevel == SiteStatus.SiteStatusLevel.URGENT_NEED_REQUIRED ? true : false);

        Optional<SiteUpdate> newSiteUpdate =
            generateNewSiteUpdate(site, newSiteStatuses, lastUpdateTime, needStatusText, note);
        if (newSiteUpdate.isPresent()) {
            site.getUpdates().add(newSiteUpdate.get());
        }
    }

    private Optional<SiteUpdate> generateNewSiteUpdate(Site site,
                                                       List<SiteStatus> siteStatuses,
                                                       String lastUpdateTime,
                                                       String needStatusText,
                                                       String note) {

        String concatenatedNote = "";
        if (lastUpdateTime != null) {
            concatenatedNote += "(" + lastUpdateTime + ")";
        }
        if (needStatusText != null) {
            concatenatedNote += " - " + needStatusText;
        }
        if (note != null) {
            concatenatedNote += " - " + note;
        }

        if (site.getUpdates().size() != 0 &&
            site.getUpdates().get(site.getUpdates().size() - 1).getUpdate().equals(concatenatedNote)) {
            return Optional.empty();
        }

        SiteUpdate newSiteUpdate = new SiteUpdate();
        newSiteUpdate.setUpdate(concatenatedNote);
        newSiteUpdate.setSiteStatuses(siteStatuses);

        //TODO : Remove minus 3 hours before merge
        newSiteUpdate.setCreateDateTime(LocalDateTime.now().minusHours(3));

        return Optional.of(newSiteUpdate);
    }

    private List<SiteStatus> generateSiteStatus(SiteStatus.SiteStatusLevel needLevel) {

        return List.of(new SiteStatus(SiteStatusType.MATERIAL, needLevel),
            new SiteStatus(SiteStatusType.HUMAN_HELP, needLevel),
            new SiteStatus(SiteStatusType.FOOD, needLevel),
            new SiteStatus(SiteStatusType.PACKAGE, needLevel));
    }

    private SiteStatus.SiteStatusLevel convertToSiteStatusLevelForIzmir(Color color) {

        if (color == null) {
            return SiteStatus.SiteStatusLevel.UNKNOWN;
        }

        // Red, not needed
        if (color.getRed() != null && compareFloats(color.getRed(), Float.valueOf(1.0f))) {

            return SiteStatus.SiteStatusLevel.NO_NEED_REQUIRED;
        }

        // Green, help needed
        if (color.getGreen() != null && compareFloats(color.getGreen(), Float.valueOf(1.0f))) {
            return SiteStatus.SiteStatusLevel.NEED_REQUIRED;
        }
        return SiteStatus.SiteStatusLevel.UNKNOWN;
    }

    private static Color getNotNeededColor(){
        Color notNeededColor = new Color();
        notNeededColor.setRed(1.0F);
        return notNeededColor;
    }

    private static Color getNeededColor(){
        Color neededColor = new Color();
        neededColor.setGreen(1.0F);
        return neededColor;
    }

    private static Color getUnknownColor(){
        Color notNeededColor = new Color();
        notNeededColor.setRed(1.0F);
        return notNeededColor;
    }

    //Create site /////////////////////
    public Site createIzmirSite(RowData rowData) {
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
