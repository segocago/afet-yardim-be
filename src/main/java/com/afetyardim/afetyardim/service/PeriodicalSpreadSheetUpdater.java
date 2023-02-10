package com.afetyardim.afetyardim.service;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PeriodicalSpreadSheetUpdater {

  private final long ANKARA_UPDATE_PERIOD_IN_MILLIS = 10 * 60 * 1000;
  private final long INITIAL_SCHEDULED_JOB_DELAY_IN_MILLIS = 30 * 1000;

  private final GoogleSheetsService googleSheetsService;

  @Scheduled(initialDelay = INITIAL_SCHEDULED_JOB_DELAY_IN_MILLIS, fixedRate = ANKARA_UPDATE_PERIOD_IN_MILLIS)
  public void scheduleFixedRateTask() throws IOException {
    log.info("Start ankara spreadsheet updates");
    googleSheetsService.updateSitesForAnkaraSpreadSheet();
    log.info("Finish ankara spreadsheet updates");
  }
}
