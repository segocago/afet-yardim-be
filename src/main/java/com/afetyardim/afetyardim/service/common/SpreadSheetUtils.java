package com.afetyardim.afetyardim.service.common;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpreadSheetUtils {

    @Value("${google.api.key}")
    private String API_KEY;

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