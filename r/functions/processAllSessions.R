processAllSessions <- function(deviceId) {
  require(data.table)
  library(lubridate)

  cat('processDevice(', deviceId,')\n')

  lockedSessionsCSV <- read.csv(paste(baseDir, "locked_sessions/", deviceId, ".csv", sep=""), header=TRUE, fileEncoding="cp1252")
  unlockedSessionsCSV <- read.csv(paste(baseDir, "unlocked_sessions/", deviceId, ".csv", sep=""), header=TRUE, fileEncoding="cp1252")

  lockedSessions <- data.table(lockedSessionsCSV)
  unlockedSessions <- data.table(unlockedSessionsCSV)

  setkey(lockedSessions, deviceId)
  setkey(unlockedSessions, deviceId)

  # Data contains negative session length if date reverses in the raw file (?!)
  lockedSessions <- subset(lockedSessions, lockedSessions$lockedSession >= 0 & !is.na(lockedSessions$lockedSession) & !is.nan(lockedSessions$lockedSession))
  unlockedSessions <-  subset(unlockedSessions, unlockedSessions$unlockedSession >= 0& !is.na(unlockedSessions$unlockedSession) & !is.nan(unlockedSessions$unlockedSession))

  lockedSessions <- subset(lockedSessions, lockedSessions$lockedSession < 1000 * 60 * 60 * 24)
  unlockedSessions <- subset(unlockedSessions, unlockedSessions$unlockedSession < 1000 * 60 * 60 *24)
  lockedSessions$kind <- "locked"
  unlockedSessions$kind <- "unlocked"

  lockedSessions$authenticationDuration <- NA
  overallSessions <- rbind(setnames(lockedSessions, c("lockedSession"), c("session")), setnames(unlockedSessions, c("unlockedSession"), c("session")), use.names=TRUE)

  setnames(lockedSessions, c("session"), c("lockedSession"))
  setnames(unlockedSessions, c("session"), c("unlockedSession"))

  overallSessions <-  subset(overallSessions, overallSessions$session >= 0)

  overallSessions$device <- ifelse(overallSessions$screensize < 7, "phone", "tablet")
  overallSessions$beginEvent <- NULL
  overallSessions$endEvent <- NULL
  overallSessions$screenOff <- NULL
  overallSessions$country <- NULL
  overallSessions$apiversion <- NULL
  overallSessions$duration <- NULL
  overallSessions$validDays <- NULL
  overallSessions$foulDays <- NULL
  overallSessions$locked <- NULL
  overallSessions$unlocked <- NULL
  overallSessions$patternLock <- NULL
  overallSessions$patternLockVisible <- NULL
  overallSessions$patternLockTactile <- NULL
  overallSessions$osstring <- NULL
  overallSessions$manufacturer <- NULL
  overallSessions$screensize <- NULL
  overallSessions$nonmarketapps <- NULL
  overallSessions$devicemodel <- NULL
  overallSessions$rooted <- NULL
  overallSessions$osbuildtype <- NULL
  overallSessions$authenticationDuration <- NULL
  overallSessions$starttime <- NULL
  overallSessions$endtime <- NULL
  overallSessions$date <- as.Date(overallSessions$beginDate)
  overallSessions$year <- year(overallSessions$beginDate)
  overallSessions$month <- month(overallSessions$beginDate)
  overallSessions$day <- weekdays(overallSessions$date)
  overallSessions$hour <-hour(ymd_hms(overallSessions$beginDate))
  overallSessions$beginDate <- NULL
  overallSessions$endDate <- NULL
  overallSessions$gsmContext <- NULL
  overallSessions$wifiContext <- NULL

  overallSessions
}
