package com.afetyardim.afetyardim.service.common;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Preconditions;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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

    private final CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build();

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

    private String expandMapUrl(String url) throws IOException {
        HttpHead request = null;
        try {
            request = new HttpHead(url);
            HttpResponse httpResponse = client.execute(request);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 301 && statusCode != 302) {
                return url;
            }
            Header[] headers = httpResponse.getHeaders(HttpHeaders.LOCATION);
            Preconditions.checkState(headers.length == 1);
            String newUrl = headers[0].getValue();
            return newUrl;
        } catch (IllegalArgumentException uriEx) {
            return url;
        } finally {
            if (request != null) {
                request.releaseConnection();
            }
        }
    }

    public List<Double> getCoordinatesByUrl(String mapUrl) throws IOException {
        String url = expandMapUrl(mapUrl);
        String[] firstArr = url.split("@");
        if (firstArr.length < 2) {
            return List.of();
        }
        String[] urlArr = firstArr[1].split(",");
        return List.of(Double.valueOf(urlArr[0]), Double.valueOf(urlArr[1]));
    }
}