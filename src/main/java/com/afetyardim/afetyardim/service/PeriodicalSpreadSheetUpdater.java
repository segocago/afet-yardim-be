package com.afetyardim.afetyardim.service;

import com.afetyardim.afetyardim.service.istanbul.IstanbulSitesParser;
import com.afetyardim.afetyardim.service.izmir.IzmirSitesParser;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PeriodicalSpreadSheetUpdater {

  private final long PARSER_PERIOD_IN_MILLIS = 10 * 60 * 1000;
  private final long INITIAL_SCHEDULED_JOB_DELAY_IN_MILLIS = 30 * 1000;

  private final AnkaraGoogleSheetsService ankaraGoogleSheetsService;
  private final IzmirSitesParser izmirSitesParser;
  private final IstanbulSitesParser istanbulSitesParser;

  @Scheduled(initialDelay = INITIAL_SCHEDULED_JOB_DELAY_IN_MILLIS, fixedRate = PARSER_PERIOD_IN_MILLIS)
  public void scheduleFixedRateTask() throws IOException {

    parseIstanbulSpreadsheet();
    parseAnkaraSpreadsheet();
    parseIzmirSpreadsheet();
  }

  private void parseAnkaraSpreadsheet() {
    log.info("Start ankara spreadsheet updates");
    try {
      ankaraGoogleSheetsService.updateSitesForAnkaraSpreadSheet();
    } catch (IOException e) {
      throw new RuntimeException("Exception while parsing ankara spreadsheet", e);
    }
    log.info("Finish ankara spreadsheet updates");
  }

  private void parseIzmirSpreadsheet() {
    log.info("Start izmir spreadsheet parsing");
    try {
      izmirSitesParser.parseSpreadsheet();
    } catch (IOException e) {
      throw new RuntimeException("Exception while parsing izmir spreadsheet", e);
    }
    log.info("Finish izmir spreadsheet parsing");
  }

  private void parseIstanbulSpreadsheet() {
    log.info("Start istanbul spreadsheet parsing");
    try {
      istanbulSitesParser.parseSpreadsheet();
    } catch (IOException e) {
      throw new RuntimeException("Exception while parsing izmir spreadsheet", e);
    }
    log.info("Finish istanbul spreadsheet parsing");
  }
}
