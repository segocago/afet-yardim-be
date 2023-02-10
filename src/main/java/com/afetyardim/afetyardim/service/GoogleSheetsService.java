package com.afetyardim.afetyardim.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.api.services.sheets.v4.model.RowData;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleSheetsService {

  @Value("${google.api.key}")
  private String API_KEY;

  private final static ObjectMapper mapper = new ObjectMapper();


  public Object getValues(String spreadsheetId, String range) throws IOException {

    RestTemplate restTemplate = new RestTemplate();
    String sheetUrl =
        "https://sheets.googleapis.com/v4/spreadsheets/" + spreadsheetId + "?ranges=" + range +
            "&includeGridData=true&key=" +
            API_KEY;
    ResponseEntity<JsonNode> response
        = restTemplate.getForEntity(sheetUrl, JsonNode.class);

    JsonNode rowData = response.getBody().path("sheets").get(0).get("data").get(0).get("rowData");
    ObjectReader reader = mapper.readerFor(new TypeReference<List<RowData>>() {
    });

    List<RowData> list = reader.readValue(rowData);
    return list;

  }
}