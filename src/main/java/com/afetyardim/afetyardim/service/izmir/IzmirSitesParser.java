package com.afetyardim.afetyardim.service.izmir;

import static com.afetyardim.afetyardim.service.common.SiteUtils.compareFloats;
import com.afetyardim.afetyardim.model.ActiveStatus;
import com.afetyardim.afetyardim.model.Location;
import com.afetyardim.afetyardim.model.Site;
import com.afetyardim.afetyardim.model.SiteStatus;
import com.afetyardim.afetyardim.model.SiteStatusType;
import com.afetyardim.afetyardim.model.SiteType;
import com.afetyardim.afetyardim.model.SiteUpdate;
import com.afetyardim.afetyardim.service.SiteService;
import com.afetyardim.afetyardim.service.common.SiteUtils;
import com.afetyardim.afetyardim.service.common.SpreadSheetUtils;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IzmirSitesParser {

    private final SpreadSheetUtils spreadSheetUtils;
    private final SiteService siteService;

    public String getCityName(){
        return "İzmir";
    }

    public String getIzmirSpreadSheetId(){
        return "1pAUwGOfuu6mRUnsHs7uQrggAu8GQm6Z-r6M25lgBCNY";
    }

    public String getSpreadSheetRange(){
        return "A1:G100";
    }

    public void parseSpreadsheet() throws IOException {

        log.info("Start {} spread sheet parsing",getCityName());

        Spreadsheet spreadsheet = spreadSheetUtils.getSpreadSheet(getIzmirSpreadSheetId(), getSpreadSheetRange());
        List<RowData> rows = spreadsheet.getSheets().get(0).getData().get(0).getRowData();
        //Remove header row
        rows.remove(0);
        Collection<Site> izmirSites = siteService.getSites(Optional.of(getCityName()), Optional.empty());
        List<Site> newSites = new ArrayList<>();
        int updatedSiteCount = 0;

        for (int i = 0; i < rows.size(); i++) {
            RowData rowData = rows.get(i);
            try {

                String district = (String) rowData.getValues().get(0).get("formattedValue");
                String siteName = (String) rowData.getValues().get(1).get("formattedValue");
                if (siteName == null || district == null) {
                    continue;
                }
                Optional<Site> existingSite = SiteUtils.findSiteByNameAndDistrict(siteName,district, izmirSites);

                if (existingSite.isEmpty()) {
                    Optional<Site> newSite = createIzmirSite(rowData);
                    if(newSite.isPresent()){
                        newSites.add(newSite.get());
                    }

                } else {
                    updateSite(rowData, existingSite.get());
                    updatedSiteCount++;
                }
            }catch (Exception exception){
                log.warn("Error while parsing row, {}",rowData,exception);
            }
        }

        log.info("Total rows in excel: {}, Total sites in db before: {}, Created site count: {}, Updated site count: {}"
            ,rows.size(),izmirSites.size(),newSites.size(),updatedSiteCount);
        izmirSites.addAll(newSites);
        siteService.saveAllSites(izmirSites);

    }

    public void updateSite(RowData rowData, Site site) {

        String phone = (String) rowData.getValues().get(3).get("formattedValue");
        if (phone != null) {
            site.setContactInformation(phone);
        }
        String lastUpdateTime = (String) rowData.getValues().get(4).get("formattedValue");

        String needStatusText = (String) rowData.getValues().get(5).get("formattedValue");
        Color needStatusColor = null;
        try {
             needStatusColor = rowData.getValues().get(5).getUserEnteredFormat().getBackgroundColor();
        }catch (Exception exception){
            log.warn("Error while parsing need status column color for {} site: {}",getCityName(),site.getName());
        }
        SiteStatus.SiteStatusLevel needLevel = convertToSiteStatusLevelForIzmir(needStatusColor);


        String note ="";
        try {
            note = (String) rowData.getValues().get(6).get("formattedValue");
        }catch(Exception exception){
            log.warn("Error while parsing note column for {} site: {}",getCityName(),site.getName());
        }


        List<SiteStatus> newSiteStatuses = generateSiteStatus(needLevel);
        site.setLastSiteStatuses(newSiteStatuses);
        site.setActive(needLevel == SiteStatus.SiteStatusLevel.NEED_REQUIRED ||
            needLevel == SiteStatus.SiteStatusLevel.URGENT_NEED_REQUIRED ? true : false);
        site.setActiveStatus(needLevel == SiteStatus.SiteStatusLevel.UNKNOWN ? ActiveStatus.UNKNOWN : ActiveStatus.ACTIVE);

        Optional<SiteUpdate> newSiteUpdate =
            generateNewSiteUpdate(site, newSiteStatuses, lastUpdateTime, needStatusText, note);
        if (newSiteUpdate.isPresent()) {
            site.getUpdates().add(newSiteUpdate.get());
        }else {
            if(!site.didSiteHaveUpdateInLastPeriod() && site.getActiveStatus() != ActiveStatus.UNKNOWN){
                log.warn("Site {} did not get any update in last 24 hours. Moving to unknown state.", site.getName());
                site.setActiveStatus(ActiveStatus.UNKNOWN);
            }
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

    public Optional<Site> createIzmirSite(RowData rowData) {
        String siteName = (String) rowData.getValues().get(1).get("formattedValue");
        Optional<Location> location = buildSiteLocation(rowData);
        if (Objects.isNull(siteName)) {
            return Optional.empty();
        }

        if (location.isEmpty()) {
            log.warn("Could not create location for {}", siteName);
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
        location.setCity(getCityName());
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
