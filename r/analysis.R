# =================================================================================
# Working directory
# =================================================================================
setwd("~/mobile-device-usage-processing/r")

# =================================================================================
# Load Libraries
# =================================================================================
require(data.table)
require(scales)
require(ggplot2)
require(ggthemes)
require(dplyr)

require(parallel)
require(foreach)
require(doParallel)

#install.packages("data.table")
#install.packages("scales")
#install.packages("ggplot2")
#install.packages("ggthemes")
#install.packages("dplyr")

# =================================================================================
# Load functions:
# =================================================================================
source("./functions/processDevice.R")
source("./functions/processSessions.R")
source("./functions/processAllSessions.R")
        
# =================================================================================
# Setup and config:
# =================================================================================

baseDir = "~/device_analyzer_results"
resultDir = "~/mobile-device-usage-processing/results/"
session_result_folder <- paste(resultDir, 'sessions', sep='')

ggplotConfig <- list(scale_fill_brewer(palette="BrBG"),theme_minimal(), theme(legend.position = 'bottom'))

devices = read.csv(paste(baseDir, "devices/devices.csv", sep=""), header=TRUE, fileEncoding="cp1252")
devices$device <- ifelse(devices$screensize < 7, "phone", "tablet")

sum(devices$duration, na.rm = TRUE) - sum(devices$validDays, na.rm = TRUE)
sum(devices$validDays, na.rm = TRUE)

devices_filtered_pre_context <- subset(devices, validDays >= 7 & locked > 10 & unlocked > 10 ) 
devices_filtered <- subset(devices_filtered_pre_context, homeContextsWifi + homeContextsGSM > 0) 

nrow(devices_filtered_pre_context) - nrow(devices_filtered)

nrow(devices) - nrow(subset(devices, locked > 10 & unlocked > 10 ) )

t <- subset(devices_filtered, screensize < 7)
mean(t$validDays/t$duration)

p <- subset(devices_filtered, screensize >= 7)
mean(p$validDays/p$duration)

bg <- 'white'  # plot backgrounds, white for debug, transparent for paper

length(unique(devices$manufacturer))

plotWidth5 <- 2.5
plotHeight5 <- 6
family = "serif"

barPlot3x2Width <- 5.5
barPlot3x2Height <- 5

# ================================================================================
# Get all sessions
# ================================================================================
all_session_result <- NULL

cluster <- makeCluster(7, outfile="")
registerDoParallel(cluster)

all_session_result <- foreach(device=devices_filtered$deviceId,
        .combine = rbind)  %dopar%  
  processAllSessions(device)

object.size(all_session_result)

stopCluster(cluster)

saveRDS(all_session_result, paste(resultDir, '/all_session_result_1-10533', sep=''))
#saveRDS(all_session_result, paste(resultDir, '/all_session_result_small', sep=''))
all_session_result <- readRDS(paste(resultDir, '/all_session_result_1-10533', sep=''))
all_session_result <- readRDS(paste(resultDir, '/all_session_result_small', sep=''))

# ================================================================================
# Processing:
# ================================================================================

byDeviceAndDay <- summarise(group_by(all_session_result, deviceId, combinedContext, date, device, kind), 
                            count = n(), 
                            median_length_day=median(session), 
                            mean_length_day=mean(session), 
                            sum_day=sum(session))

byDeviceAndDayAllContext <- summarise(group_by(all_session_result, deviceId, date, device, combinedContext), 
                            count = n(), 
                            median_length_day=median(session), 
                            mean_length_day=mean(session), 
                            sum_day=sum(session))

#byDeviceAndDayAllContext$combinedContext <- "Overall"
byDeviceAndDayAllContext$kind <- "overall"

byDeviceAndDayAllContextKind <- summarise(group_by(all_session_result, deviceId, date, device, kind), 
                                      count = n(), 
                                      median_length_day=median(session), 
                                      mean_length_day=mean(session), 
                                      sum_day=sum(session))
byDeviceAndDayAllContextKind$combinedContext <- "OKind"

byDeviceAndDay <- rbind(byDeviceAndDay, byDeviceAndDayAllContext, byDeviceAndDayAllContextKind)

byDevice <- summarise(group_by(byDeviceAndDay, deviceId, combinedContext, device, kind), 
                      mean_count_day=mean(count),
                      median_count_day=median(count),
                      mean_sum_day=mean(sum_day),
                      median_sum_day=median(sum_day),
                      mean_mean_length_day=mean(mean_length_day),
                      median_mean_length_day=median(mean_length_day),
                      mean_median_length_day=mean(median_length_day),
                      median_median_length_day=median(median_length_day)
)

byDevice <- subset(byDevice, !is.na(device))
byDevice$device = factor(byDevice$device, levels=c("phone", "tablet"), labels=c("Phone", "Tablet"))
byDevice$kind = factor(byDevice$kind, levels=c("overall", "locked", "unlocked"), labels=c("Overall", "Locked", "Unlocked"))
byDevice$combinedContext = factor(byDevice$combinedContext, levels=c("HOME", "OFFICE",  "OTHER_MEANINGFUL", "OtherMeaningful", "ELSEWHERE", "Overall", "OKind"), ordered=T, labels=c("Home", "Office", "Other meaningful", "OtherMeaningful", "Elsewhere", "Overall", "OKind"))

saveRDS(byDevice, paste(resultDir, '/byDevice', sep=''))
byDevice <- readRDS(paste(resultDir, '/byDevice', sep=''))

# ================================================================================
# Create graphs
# ================================================================================

source("./output/sessionGraphs.R", print.eval=TRUE)
source("./output/worldmap.R", print.eval=TRUE)
source("./output/heatmaps.R", print.eval=TRUE)
source("./output/contextPlot.R", print.eval=TRUE)
source("./output/authentication.R", print.eval=TRUE)

# ================================================================================
# LaTex Constants:
# ================================================================================
source("./output/latexConstants.R", print.eval=TRUE)

# ================================================================================