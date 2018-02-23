processDevice <- function(deviceId) {
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
  
  lockedSessions$authenticationDuration <- NA
  overallSessions <- rbind(setnames(lockedSessions, c("lockedSession"), c("session")), setnames(unlockedSessions, c("unlockedSession"), c("session")), use.names=TRUE)
  
  setnames(lockedSessions, c("session"), c("lockedSession"))
  setnames(unlockedSessions, c("session"), c("unlockedSession"))
  
  overallSessions$date <- as.Date(overallSessions$beginDate)
  lockedSessions$date <- as.Date(lockedSessions$beginDate)
  unlockedSessions$date <- as.Date(unlockedSessions$beginDate)
  
  overallSessions <-  subset(overallSessions, overallSessions$session >= 0)
  
  # hard cutoff unlock duration, then remove "outliers" (any reason to not apply bigger quantiles instead?) 
  patternunlock_duration <- with(unlockedSessions, authenticationDuration[!is.na(patternLock) & patternLock==1])
  patternunlock_duration <- patternunlock_duration[patternunlock_duration <= 30000]
  patternunlock_duration <- patternunlock_duration[patternunlock_duration > quantile(patternunlock_duration, 0.025) & patternunlock_duration < quantile(patternunlock_duration, 0.975)]
  
  nopatternunlock_duration <- with(unlockedSessions, authenticationDuration[!is.na(patternLock) & patternLock==0])
  nopatternunlock_duration <- nopatternunlock_duration[nopatternunlock_duration <= 30000]
  nopatternunlock_duration <- nopatternunlock_duration[nopatternunlock_duration > quantile(nopatternunlock_duration, 0.025) & nopatternunlock_duration < quantile(nopatternunlock_duration, 0.975)]
  
  overallunlock_duration <- with(unlockedSessions, authenticationDuration)
  overallunlock_duration <- overallunlock_duration[overallunlock_duration <= 30000]
  overallunlock_duration <- overallunlock_duration[overallunlock_duration > quantile(overallunlock_duration, 0.025) & overallunlock_duration < quantile(overallunlock_duration, 0.975)]
  
  # rooted / patternlock
  rooted <- c(lockedSessions$rooted, unlockedSessions$rooted)
  rootedTrue <- if(all(is.na(rooted))) NA else sum(rooted==1)
  rootedFalse <- if(all(is.na(rooted))) NA else sum(rooted==0)
  patternLock <- c(lockedSessions$patternLock, unlockedSessions$patternLock)
  patternLockTrue <- if(all(is.na(patternLock))) NA else sum(patternLock==1)
  patternLockFalse <- if(all(is.na(patternLock))) NA else sum(patternLock==0)
  patternLockVisible <- c(lockedSessions$patternLockVisible, unlockedSessions$patternLockVisible)
  patternLockVisibleTrue <- if(all(is.na(patternLockVisible))) NA else sum(patternLockVisible==1)
  patternLockVisibleFalse <- if(all(is.na(patternLockVisible))) NA else sum(patternLockVisible==0)
  patternLockTactile <- c(lockedSessions$patternLockTactile, unlockedSessions$patternLockTactile)
  patternLockTactileTrue <- if(all(is.na(patternLockTactile))) NA else sum(patternLockTactile==1)
  patternLockTactileFalse <- if(all(is.na(patternLockTactile))) NA else sum(patternLockTactile==0)
  
  data.frame(
    deviceId = deviceId,
    count_locked = nrow(lockedSessions),
    count_unlocked = nrow(unlockedSessions),
    screensize = max(c(lockedSessions$screensize, unlockedSessions$screensize)),
    all_sessions=processSessions(lockedSessions, unlockedSessions, overallSessions, NA),
    home_sessions=processSessions(lockedSessions, unlockedSessions, overallSessions, 'HOME'),
    office_sessions=processSessions(lockedSessions, unlockedSessions, overallSessions, 'OFFICE'),
    other_meaningfull_sessions=processSessions(lockedSessions, unlockedSessions, overallSessions, 'OTHER_MEANINGFUL'),
    elsewhere_sessions=processSessions(lockedSessions, unlockedSessions, overallSessions, 'ELSEWHERE'),
    
    pattern = data.frame(
      patternunlock_duration_mean = mean(patternunlock_duration) / 1000,
      nopatternunlock_duration_mean = mean(nopatternunlock_duration) / 1000,
      overallunlock_duration_mean = mean(overallunlock_duration) / 1000,
      
      patternunlock_duration_median = median(patternunlock_duration) / 1000,
      nopatternunlock_duration_median = median(nopatternunlock_duration) / 1000,
      overallunlock_duration_median = median(overallunlock_duration) / 1000,
      
      rootedTrue = rootedTrue,
      rootedFalse = rootedFalse,
      patternLockTrue = patternLockTrue,
      patternLockFalse = patternLockFalse,
      patternLockVisibleTrue = patternLockVisibleTrue,
      patternLockVisibleFalse = patternLockVisibleFalse,
      patternLockTactileTrue = patternLockTactileTrue,
      patternLockTactileFalse = patternLockTactileFalse)
  )
}

