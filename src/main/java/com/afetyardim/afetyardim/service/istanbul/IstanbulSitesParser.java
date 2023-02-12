package com.afetyardim.afetyardim.service.istanbul;

import com.afetyardim.afetyardim.model.*;
import com.afetyardim.afetyardim.service.SiteService;
import com.afetyardim.afetyardim.service.common.SpreadSheetUtils;
import com.afetyardim.afetyardim.service.common.SiteUtils;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.afetyardim.afetyardim.service.common.SiteUtils.compareFloats;

@Service
@RequiredArgsConstructor
@Slf4j
public class IstanbulSitesParser {

    private final SpreadSheetUtils spreadSheetUtils;
    private final SiteService siteService;

    private final static String ISTANBUL_SPREADSHEET_ID = "1B0epkFl-4dF4FINjTSONWbHivOu702RBIWYnOM1gWkg";
    private final static String ISTANBUL_SPREADSHEET_RANGE = "A1:G300";
    private final static String CITY_NAME = "İstanbul";

    public void parseSpreadsheet() throws IOException {
        log.info("Start Istanbul spread sheet parsing");

        Spreadsheet spreadsheet = spreadSheetUtils.getSpreadSheet(ISTANBUL_SPREADSHEET_ID, ISTANBUL_SPREADSHEET_RANGE);
        List<RowData> rows = spreadsheet.getSheets().get(0).getData().get(0).getRowData();
        //Remove header row
        rows.remove(0);
        Collection<Site> istanbulSites = siteService.getSites(Optional.of(CITY_NAME), Optional.empty());
        List<Site> newSites = new ArrayList<>();
        int updatedSiteCount = 0;

        for (int i = 0; i < rows.size(); i++) {
            RowData row = rows.get(i);
            try {

                String siteName = (String) row.getValues().get(2).get("formattedValue");
                if (siteName == null) {
                    log.info("Could not get siteName, returning");
                    return;
                }

                String district;
                try{
                    district = (String) row.getValues().get(0).get("formattedValue");
                }catch(Exception ex){
                    district = null;
                }

                if (siteName == null || district == null) {
                    continue;
                }
                Optional<Site> existingSite = SiteUtils.findSiteByNameAndDistrict(siteName, district, istanbulSites);

                if (existingSite.isEmpty()) {
                    Optional<Site> newSite = createIstanbulSite(row);
                    if(newSite.isPresent()){
                        newSites.add(newSite.get());
                    }

                } else {
                    updateSite(row, existingSite.get());
                    updatedSiteCount++;
                }
            }catch (Exception exception){
                log.warn("Error while parsing row, {}", row, exception);
            }
        }

        log.info("Total rows in excel: {}, Total sites in db before: {}, Created site count: {}, Updated site count: {}"
                ,rows.size(),istanbulSites.size(),newSites.size(),updatedSiteCount);
        istanbulSites.addAll(newSites);
        siteService.saveAllSites(istanbulSites);

    }

    public void updateSite(RowData row, Site site) {
        Color activeColor;
        try{
            activeColor = row.getValues().get(1).getUserEnteredFormat().getBackgroundColor();
        }catch(Exception ex){
            activeColor = null;
        }
        boolean active = convertColorToActive(activeColor);

        Color activeNoteColor;
        try {
            activeNoteColor = row.getValues().get(2).getUserEnteredFormat().getBackgroundColor();
        }catch(Exception ex){
            activeNoteColor = null;
        }

        String activeNote = null;
        // This can be improved
        // rgb(255, 153, 0)
        if(activeNoteColor != null){
            if(activeNoteColor.getGreen() != null && compareFloats(activeNoteColor.getGreen(), 0.6f) &&
                    activeNoteColor.getRed() != null && compareFloats(activeNoteColor.getRed(), 1.0f)){
                activeNote = "7/24 açık";
            }
        }

        Color need = row.getValues().get(1).getUserEnteredFormat().getBackgroundColor();
        SiteStatus.SiteStatusLevel needLevel = convertToSiteStatusLevel(need);

        String note;
        try{
            note = (String) row.getValues().get(1).get("formattedValue");
        }catch(Exception ex){
            note = null;
        }

        String phone;
        try{
            phone = (String) row.getValues().get(4).get("formattedValue");
        }catch(Exception ex){
            phone = "";
        }

        if (phone != null) {
            site.setContactInformation(phone);
        }

        String lastUpdateTime;
        try {
            lastUpdateTime = (String) row.getValues().get(5).get("formattedValue");
        }catch(Exception ex){
            lastUpdateTime = "";
        }
        List<SiteStatus> newSiteStatuses = generateSiteStatus(needLevel);
        site.setLastSiteStatuses(newSiteStatuses);
        site.setActive(active);

        Optional<SiteUpdate> newSiteUpdate =
                generateNewSiteUpdate(site, newSiteStatuses, lastUpdateTime, activeNote, note);
        if (newSiteUpdate.isPresent()) {
            site.getUpdates().add(newSiteUpdate.get());
        }
        log.info("Updated site: {} ", site.getName());
    }

    private Optional<SiteUpdate> generateNewSiteUpdate(Site site,
                                                       List<SiteStatus> siteStatuses,
                                                       String lastUpdateTime,
                                                       String needStatusText,
                                                       String note) {

        String concatenatedNote = "";
        if (lastUpdateTime != null && !lastUpdateTime.isBlank()) {
            concatenatedNote += "(" + lastUpdateTime + ")";
        }
        if (needStatusText != null && !needStatusText.isBlank()) {
            if(!concatenatedNote.isBlank()){
                concatenatedNote += " - ";
            }
            concatenatedNote += needStatusText;
        }
        if (note != null && !note.isBlank()) {
            if(!concatenatedNote.isBlank()){
                concatenatedNote += " - ";
            }
            concatenatedNote += note;
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

    private SiteStatus.SiteStatusLevel convertToSiteStatusLevel(Color color) {

        if (color == null) {
            return SiteStatus.SiteStatusLevel.UNKNOWN;
        }
        // Green, help needed
        if (color.getGreen() != null && compareFloats(color.getGreen(), 1.0f)) {
            return SiteStatus.SiteStatusLevel.NEED_REQUIRED;
        }
        //Red, help not needed
        if (color.getRed() != null && compareFloats(color.getRed(), 1.0f)) {
            return SiteStatus.SiteStatusLevel.NO_NEED_REQUIRED;
        }
        return SiteStatus.SiteStatusLevel.UNKNOWN;
    }

    public Optional<Site> createIstanbulSite(RowData row) {

        String siteName;
        try{
            siteName = (String) row.getValues().get(2).get("formattedValue");
            if (siteName == null) {
                log.info("Could not get siteName, skipping.");
                return Optional.empty();
            }
        }catch(Exception ex){
            log.info("Could not get siteName, skipping.");
            return Optional.empty();
        }


        Optional<Location> location = buildSiteLocation(row);
        if(location.isEmpty()){
            log.info("Could not get Location, skipping.");
            return Optional.empty();
        }
        Site site = new Site();
        Color activeColor;
        try{
            activeColor = row.getValues().get(1).getUserEnteredFormat().getBackgroundColor();
        }catch(Exception ex){
            activeColor = null;
        }
        boolean active = convertColorToActive(activeColor);

        Color activeNoteColor;
        try {
            activeNoteColor = row.getValues().get(2).getUserEnteredFormat().getBackgroundColor();
        }catch(Exception ex){
            activeNoteColor = null;
        }

        String activeNote = "";
        // This can be improved
        // rgb(255, 153, 0)
        if(activeNoteColor != null){
            if(activeNoteColor.getGreen() != null && compareFloats(activeNoteColor.getGreen(), 0.6f) &&
                    activeNoteColor.getRed() != null && compareFloats(activeNoteColor.getRed(), 1.0f)){
                activeNote = "7/24 açık";
            }
        }

        Color need = row.getValues().get(1).getUserEnteredFormat().getBackgroundColor();
        SiteStatus.SiteStatusLevel needLevel = convertToSiteStatusLevel(need);

        String note;
        try{
            note = (String) row.getValues().get(1).get("formattedValue");
        }catch(Exception ex){
            note = null;
        }

        String phone;
        try{
            phone = (String) row.getValues().get(3).get("formattedValue");
        }catch(Exception ex){
            phone = null;
        }

        if (phone != null) {
            site.setContactInformation(phone);
        }

        String lastUpdateTime;
        try {
            lastUpdateTime = (String) row.getValues().get(5).get("formattedValue");
        }catch(Exception ex){
            lastUpdateTime = null;
        }

        List<SiteStatus> newSiteStatuses = generateSiteStatus(needLevel);
        site.setLastSiteStatuses(newSiteStatuses);
        site.setActive(active);

        Optional<SiteUpdate> newSiteUpdate =
                generateNewSiteUpdate(site, newSiteStatuses, lastUpdateTime, activeNote, note);
        if (newSiteUpdate.isPresent()) {
            site.getUpdates().add(newSiteUpdate.get());
        }

        site.setName(siteName);
        site.setOrganizer("Bilinmiyor");
        site.setType(SiteType.SUPPLY);
        site.setLocation(location.get());
        site.setVerified(true);

        siteService.createSite(site);
        log.info("Created site: {}", siteName);

        return Optional.of(site);
    }

    private Optional<Location> buildSiteLocation(RowData rowData) {
        String mapUrl;
        try{
            mapUrl = (String) rowData.getValues().get(4).get("formattedValue");
        }catch(Exception ex){
            log.info("Invalid map url");
            return Optional.empty();
        }
        if (Objects.isNull(mapUrl)) {
            return Optional.empty();
        }
        String district = (String) rowData.getValues().get(0).get("formattedValue");
        Location location = new Location();
        location.setDistrict(district);
        location.setCity(CITY_NAME);
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

    private boolean convertColorToActive(Color color){
        if (color == null)
            return false;

        if (color.getGreen() != null && compareFloats(color.getGreen(), 1.0f)) {
            return true;
        }
        return false;
    }

}

