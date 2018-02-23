library(data.table)
library(ggplot2)
library(plyr)
library(scales)
library(grid)


# =================================================================================
# Process single device:
# =================================================================================

processDevice <- function(deviceId) {
  cat('processDevice(', deviceId,')\n')
  
  unlockedSessionsCSV <- read.csv(paste(baseDir, "unlocked_sessions/", deviceId, ".csv", sep=""), header=TRUE, fileEncoding="cp1252")
  unlockedSessions <- data.table(unlockedSessionsCSV)
  setkey(unlockedSessions, deviceId)
  
  # Data contains negative session length if date reverses in the raw file (?!)
  unlockedSessions <-  subset(unlockedSessions, unlockedSessions$unlockedSession >= 0& !is.na(unlockedSessions$unlockedSession) & !is.nan(unlockedSessions$unlockedSession)& !is.nan(unlockedSessions$authenticationDuration))

  data.frame(
    deviceId = unlockedSessions$deviceId,
    authenticationDuration  = unlockedSessions$authenticationDuration,
    screensize = unlockedSessions$screensize,
    patternLock = unlockedSessions$patternLock)
}

auth_result <- NULL
  for(deviceId in devices_filtered$deviceId) {
  auth_result <- rbind(auth_result, processDevice(deviceId))   
}


# =================================================================================
# Save or load result to speed up development
# =================================================================================

auth_result <- readRDS(paste(resultDir, '/auth_result.rds', sep=''))

# =================================================================================

auth_result <- subset(auth_result, auth_result$authenticationDuration >= 0 & auth_result$authenticationDuration > 0& !is.na(auth_result$patternLock))

nrow(auth_result)

auth_result$seconds <- auth_result$authenticationDuration/1000
auth_result <- subset(auth_result, auth_result$seconds <= 10)

auth_result$device <- ifelse(test = auth_result$screensize >= 7, yes = "Tablet", no = "Phone")
auth_result$lock <- factor(ifelse(test = auth_result$patternLock == 1, yes = "Pattern", no = "Other"), levels=c("Pattern",  "Other"))

mean(subset(auth_result, lock=="Pattern" & device == "Phone" & unlocked > 0)$seconds)
median(subset(auth_result, lock=="Pattern"& device == "Phone" & unlocked > 0)$seconds)
mean(subset(auth_result, lock=="Other"& device == "Phone" & unlocked > 0)$seconds)
median(subset(auth_result, lock=="Other"& device == "Phone" & unlocked > 0)$seconds)


mean(subset(auth_result, lock=="Pattern" & device == "Tablet" & unlocked > 0)$seconds)
median(subset(auth_result, lock=="Pattern"& device == "Tablet" & unlocked > 0)$seconds)
mean(subset(auth_result, lock=="Other"& device == "Tablet" & unlocked > 0)$seconds)
median(subset(auth_result, lock=="Other"& device == "Tablet" & unlocked > 0)$seconds)

palette <- "Spectral"
plotWidth5 <- 2.5
plotHeight5 <- 6
family = "serif"

# =================================================================================

svg(paste(resultDir, '/auth_density.svg', sep='') ,width=6, height=4.4, bg=bg, family = family)

ggplot(auth_result, aes(x=seconds, fill=factor(device))) + 
geom_density(alpha=.7) +
  coord_cartesian(xlim = c(0,10.5)) +
  labs(y ="Density", x = "Duration [s]", fill="Device: ") +
  ggplotConfig +
  facet_wrap(~lock, ncol = 1) +
  theme(legend.position = 'bottom')
dev.off()

#=================================================================================================================

svg(paste(resultDir, '/auth_config_dodge.svg', sep='') ,width=6, height=4, bg=bg, family = family)


nrow(subset(devices_auth, patternLockVisible == 1 & patternLock == 1))/nrow(subset(devices_auth,  patternLock == 1))

nrow(subset(p_devices_auth, patternLock == 0 & unlocked == 0))/nrow(p_devices_auth)
nrow(subset(t_devices_auth,patternLock == 0 & unlocked == 0))/nrow(t_devices_auth)

devices_auth <- subset(devices, 
                     #                           
                     !is.na(devices$patternLock) &
                     !is.na(devices$patternLockVisible) & 
                     !is.na(devices$patternLockTactile) &
                      devices$validDays > 0) 

p_devices_auth <- subset(devices_auth, screensize < 7)
t_devices_auth <- subset(devices_auth, screensize >= 7)

plotData <- data.frame("data" = c(
  nrow(subset(p_devices_auth, patternLock == 1))/nrow(p_devices_auth),
  nrow(subset(p_devices_auth, patternLock == 0 & unlocked > 0))/nrow(p_devices_auth),
  nrow(subset(p_devices_auth, patternLock == 0 & unlocked == 0))/nrow(p_devices_auth),
  
  nrow(subset(t_devices_auth, patternLock == 1))/nrow(t_devices_auth),
  nrow(subset(t_devices_auth, patternLock == 0 & unlocked > 0))/nrow(t_devices_auth),
  nrow(subset(t_devices_auth, patternLock == 0 & unlocked == 0))/nrow(t_devices_auth)
),
"device" = rep(c("Phone", "Tablet"), each=3),
"lock" = factor(rep(c("Pattern", "Other", "None"), each=1, times=2), levels=c("Pattern", "Other", "None"))
)

ggplot(data = plotData,
       aes(y = data, x = factor(device), fill=lock))+
  geom_bar(stat="identity", position = "dodge", color="black") +
  labs(x = "", y = "", fill="Lock: ") +
  ggplotConfig +
  scale_y_continuous(labels = percent) +
  theme(legend.position = 'bottom', legend.margin = unit(-1, "cm")) 
dev.off()

write.csv(file=paste(auth_result_folder, '/auth_config_dodge.csv', sep=''), x=plotData)
