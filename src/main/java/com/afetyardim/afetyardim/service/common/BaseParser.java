package com.afetyardim.afetyardim.service.common;

import com.afetyardim.afetyardim.model.ActiveStatus;
import com.afetyardim.afetyardim.model.Location;
import com.afetyardim.afetyardim.model.Site;
import com.afetyardim.afetyardim.model.SiteStatus;
import com.afetyardim.afetyardim.model.SiteType;
import com.afetyardim.afetyardim.model.SiteUpdate;
import com.afetyardim.afetyardim.service.SiteService;
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


@RequiredArgsConstructor
@Slf4j
public abstract class BaseParser {

    private final SpreadSheetUtils spreadSheetUtils;
    private final SiteService siteService;

    private static final String DISTRICT_DEFAULT_VALUE = "Unknown";
    private static final String ADDITIONAL_ADDRESS_DEFAULT_VALUE = "Unknown";
    private static final String CONTACT_INFORMATION_DEFAULT_VALUE ="Unknown";

    public abstract String getCityName();

    public abstract String getSpreadSheetId();

    public abstract String getSpreadSheetRange();

    public abstract int numberOfHeaderRows();

    public abstract Optional<String> getDistrict(RowData rowData);
    public abstract String getSiteName(RowData rowData);

    public abstract Optional<String> getMapUrl(RowData rowData);
    public abstract Optional<String> getAdditionalAddress(RowData rowData);

    public abstract Optional<String> getContactInformation(RowData rowData);
    public abstract Optional<String> getUpdateComment(RowData rowData);

    public abstract Optional<Color> getNeedLevelColor(RowData rowData);
    public abstract SiteStatus.SiteStatusLevel convertNeedColorToNeedLevel(Color color);

    public abstract List<SiteStatus> generateSiteStatus(SiteStatus.SiteStatusLevel needLevel);


    public void parseSpreadsheet() throws IOException {

        log.info("Start {} spread sheet parsing",getCityName());

        Spreadsheet spreadsheet = spreadSheetUtils.getSpreadSheet(getSpreadSheetId(), getSpreadSheetRange());
        List<RowData> rows = spreadsheet.getSheets().get(0).getData().get(0).getRowData();
        //Remove header row
        rows = rows.subList(numberOfHeaderRows(),rows.size());
        Collection<Site> existingSites = siteService.getSites(Optional.of(getCityName()));
        List<Site> newSites = new ArrayList<>();
        int updatedSiteCount = 0;

        for (int i = 0; i < rows.size(); i++) {
            RowData rowData = rows.get(i);
            try {
                String district = getDistrict(rowData).orElse(DISTRICT_DEFAULT_VALUE);
                String siteName = getSiteName(rowData);
                Optional<Site> existingSite = SiteUtils.findSiteByNameAndDistrict(siteName,district, existingSites);

                if (existingSite.isEmpty()) {
                    Optional<Site> newSite = createSite(rowData);
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
            ,rows.size(),existingSites.size(),newSites.size(),updatedSiteCount);
        existingSites.addAll(newSites);
        siteService.saveAllSites(existingSites);

    }

    public void updateSite(RowData rowData, Site site) {

        Optional<String> contactInformation = getContactInformation(rowData);
        if (contactInformation.isPresent()) {
            site.setContactInformation(contactInformation.get());
        }



        Optional<Color> needStatusColor = getNeedLevelColor(rowData);
        SiteStatus.SiteStatusLevel needLevel = needStatusColor.isPresent() ?
            convertNeedColorToNeedLevel(needStatusColor.get()) : SiteStatus.SiteStatusLevel.UNKNOWN;


        List<SiteStatus> newSiteStatuses = generateSiteStatus(needLevel);
        site.setLastSiteStatuses(newSiteStatuses);
        site.setActive(needLevel == SiteStatus.SiteStatusLevel.NEED_REQUIRED ||
            needLevel == SiteStatus.SiteStatusLevel.URGENT_NEED_REQUIRED ? true : false);
        site.setActiveStatus(needLevel == SiteStatus.SiteStatusLevel.UNKNOWN ? ActiveStatus.UNKNOWN : ActiveStatus.ACTIVE);

        Optional<SiteUpdate> newSiteUpdate =
            generateNewSiteUpdate(site, newSiteStatuses,rowData);
        if (newSiteUpdate.isPresent()) {
            site.getUpdates().add(newSiteUpdate.get());
        }
    }

    private Optional<SiteUpdate> generateNewSiteUpdate(Site site,
                                                       List<SiteStatus> siteStatuses,
                                                       RowData rowData) {

        String concatenatedNote = "";
        Optional<String> lastUpdateTime = getUpdateComment(rowData);
        if (lastUpdateTime.isPresent()) {
            concatenatedNote += "(" + lastUpdateTime.get() + ")";
        }
        Optional<String> updateComment = getUpdateComment(rowData);
        if(updateComment.isPresent()){
            concatenatedNote += " - " + updateComment.get();
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



    public Optional<Site> createSite(RowData rowData) {
        String siteName = getSiteName(rowData);
        Optional<Location> location = buildSiteLocation(rowData);
        if (Objects.isNull(siteName)) {
            return Optional.empty();
        }

        if (location.isEmpty()) {
            log.warn("Could not create location for {}", siteName);
            return Optional.empty();
        }

        Site site = new Site();
        site.setName(siteName);
        site.setActive(false);
        site.setContactInformation(getContactInformation(rowData).orElse(CONTACT_INFORMATION_DEFAULT_VALUE));
        site.setVerified(true);
        site.setType(SiteType.SUPPLY);
        site.setLocation(location.get());
        return Optional.of(site);
    }

    private Optional<Location> buildSiteLocation(RowData rowData) {
        Optional<String> mapUrl = getMapUrl(rowData);

        if (mapUrl.isEmpty()) {
            log.warn("Could not get mapUrl for site: {}",getSiteName(rowData));
            return Optional.empty();
        }
        String district = getDistrict(rowData).orElse(DISTRICT_DEFAULT_VALUE);
        Location location = new Location();
        location.setDistrict(district);
        location.setCity(getCityName());
        location.setAdditionalAddress(getAdditionalAddress(rowData).orElse(ADDITIONAL_ADDRESS_DEFAULT_VALUE));
        try {
            List<Double> coordinates = spreadSheetUtils.getCoordinatesByUrl(mapUrl.get());
            location.setLatitude(coordinates.get(0));
            location.setLongitude(coordinates.get(1));
        } catch (Exception exception) {
            log.error("Could not get coordinates by site {} from mapsUrl {}",getSiteName(rowData), mapUrl);
            return Optional.empty();
        }
        return Optional.of(location);
    }
}
