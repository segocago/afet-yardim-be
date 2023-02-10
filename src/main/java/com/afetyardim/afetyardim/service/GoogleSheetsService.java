package com.afetyardim.afetyardim.service;

import com.afetyardim.afetyardim.model.Site;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleSheetsService {

  @Value("${google.api.key}")
  private String API_KEY;

  private final static ObjectMapper mapper = new ObjectMapper();

  private final SiteService siteService;


  //İsim,aktiflik,malzeme,insan,gıda,koli,not
  public List<Site> getValues(String spreadsheetId, String range) throws IOException {

    Collection<Site> ankaraSites = siteService.getSites(Optional.of("Ankara"), Optional.empty());
    Spreadsheet spreadsheet = getSpreadSheet(spreadsheetId, range);

    List<RowData> rows = spreadsheet.getSheets().get(0).getData().get(0).getRowData();
    rows.remove(0);

    return rows.stream().map(rowData -> {
      return convertRowDataToSite(rowData, ankaraSites);
    }).toList();


  }

  private Site convertRowDataToSite(RowData rowData, Collection<Site> ankaraSites) {

    String siteName = (String) rowData.getValues().get(0).get("formattedValue");

    Optional<Site> existingSite = ankaraSites.stream().filter(site -> site.getName().equals(siteName)).findAny();

    if (existingSite.isPresent()) {
      Site site = existingSite.get();
      site.setLastSiteStatuses(null);
    }
    Site site = new Site();

    return site;
  }

  public Spreadsheet getSpreadSheet(String spreadsheetId, String range) throws IOException {

    List<String> ranges = List.of(range);

    boolean includeGridData = true;

    Sheets sheetsService = getSheets();
    Sheets.Spreadsheets.Get request = sheetsService.spreadsheets().get(spreadsheetId);
    request.setRanges(ranges);
    request.setIncludeGridData(includeGridData);

    return request.execute();
  }

  private Sheets getSheets() {
    NetHttpTransport transport = new NetHttpTransport.Builder().build();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer httpRequestInitializer = request -> {
      request.setInterceptor(intercepted -> intercepted.getUrl().set("key", API_KEY));
    };

    return new Sheets.Builder(transport, jsonFactory, httpRequestInitializer)
        .setApplicationName("s")
        .build();
  }
}