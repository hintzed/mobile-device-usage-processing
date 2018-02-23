echoLatexCmd <- function(name, value, numprint=FALSE, percentage=FALSE){
  if(numprint) {
    if(percentage) cat("\\newcommand{\\var", name, "}{\\numprint[\\%]{", value,"}}\n", sep='')
    else cat("\\newcommand{\\var", name, "}{\\numprint{", value,"}}\n", sep='')
  } else {
    cat("\\newcommand{\\var", name, "}{", value,"}\n", sep='')
  }
}

allDevices <- summarise(group_by(byDevice, combinedContext, device, kind), 
                        max_mean_mean_length_day = round(max(mean_mean_length_day/1000)/60),
                        
                        mean_mean_count_day = round(mean(mean_count_day)),
                        mean_median_count_day = round(mean(median_count_day)),
                        mean_mean_sum_day = round(mean(mean_sum_day/1000/60)),
                        mean_median_sum_day = round(mean(median_sum_day/1000/60)),
                        mean_mean_mean_length_day = round(mean(mean_mean_length_day/1000)),
                        mean_median_mean_length_day = round(mean(median_mean_length_day/1000)),
                        mean_mean_median_length_day = round(mean(mean_median_length_day/1000)),
                        mean_median_median_length_day = round(mean(median_median_length_day/1000)),
                        
                        median_mean_count_day = round(median(mean_count_day)),
                        median_median_count_day = round(median(median_count_day)),
                        median_mean_sum_day = round(median(mean_sum_day/1000/60)),
                        median_median_sum_day = round(median(median_sum_day/1000/60)),
                        median_mean_mean_length_day = round(median(mean_mean_length_day/1000)),
                        median_median_mean_length_day = round(median(median_mean_length_day/1000)),
                        median_mean_median_length_day = round(median(mean_median_length_day/1000)),
                        median_median_median_length_day = round(median(median_median_length_day/1000)))

allDevices[allDevices=="Other meaningful"]<-"OtherMeaningful"

sink(file='/home/hintzed/git/ubicomp-iswc-2014-programming-competition/tex-imwut/vars.tex')

printRow <- function(row) {
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MaxMeanLengthDay", sep=''), row["max_mean_mean_length_day"], TRUE)
  
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MeanMeanCountDay", sep=''), row["mean_mean_count_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MeanMedianCountDay", sep=''), row["mean_median_count_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MeanMeanSumDay", sep=''), row["mean_mean_sum_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MeanMedianSumDay", sep=''), row["mean_median_sum_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MeanMeanMeanLengthDay", sep=''), row["mean_mean_mean_length_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MeanMedianMeanLengthDay", sep=''), row["mean_median_mean_length_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MeanMeanMedianLengthDay", sep=''), row["mean_mean_median_length_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MeanMedianMedianLengthDay", sep=''), row["mean_median_median_length_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MedianMeanCountDay", sep=''), row["median_mean_count_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MedianMedianCountDay", sep=''), row["median_median_count_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MedianMeanSumDay", sep=''), row["median_mean_sum_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MedianMedianSumDay", sep=''), row["median_median_sum_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MedianMeanMeanLengthDay", sep=''), row["median_mean_mean_length_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MedianMedianMeanLengthDay", sep=''), row["median_median_mean_length_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MedianMeanMedianLengthDay", sep=''), row["median_mean_median_length_day"], TRUE)
  echoLatexCmd(paste(row["device"], row["combinedContext"], row["kind"], "MedianMedianMedianLengthDay", sep=''), row["median_median_median_length_day"], TRUE)
}

invisible(apply(allDevices, 1, printRow))

echoLatexCmd("PhoneScreenoffTimeout", 0, TRUE)
echoLatexCmd("TabletScreenoffTimeout", 0, TRUE)

echoLatexCmd("NumberOfAllDevices", nrow(devices), TRUE)
echoLatexCmd("NumberOfDevicesUsed", nrow(devices_filtered), TRUE)
echoLatexCmd("NumberOfPhonesUsed", nrow(subset(devices_filtered, device == "phone")), TRUE)
echoLatexCmd("NumberOfTabletsUsed", nrow(subset(devices_filtered, device == "tablet")), TRUE)

echoLatexCmd("NumberOfDevicesYearsOfUsage", round(sum(devices$duration/365, na.rm=TRUE)), TRUE)
echoLatexCmd("NumberOfDevicesUsedYearsOfUsage", round(sum(devices_filtered$duration/365, na.rm=TRUE)), TRUE)

echoLatexCmd("DeviceMoreThanOneMonth", nrow(subset(devices,validDays > 30)), TRUE)
echoLatexCmd("DeviceMoreThanOneYear", nrow(subset(devices,validDays > 365)), TRUE)

echoLatexCmd("NumberOfSessions", paste(format(round(sum(devices$locked + devices$unlocked) / 1e6, 1), trim = TRUE), "million"))
echoLatexCmd("NumberOfSessionsUsed",  paste(format(round(sum(devices_filtered$locked + devices_filtered$unlocked) / 1e6, 1), trim = TRUE), "million"))

echoLatexCmd("AverageDaysPhone", round(mean(subset(devices_filtered, device == 'phone')$duration)))
echoLatexCmd("AverageDaysTablet", round(mean(subset(devices_filtered, device == 'tablet')$duration)))

dfpc_p <- subset(devices_filtered_pre_context, device == 'phone');
dfpc_t <- subset(devices_filtered_pre_context, device == 'tablet');

echoLatexCmd("PercentPhoneHomeDetection", round(100 * nrow(subset(dfpc_p, homeContextsWifi + homeContextsGSM > 0))/nrow(dfpc_p)), TRUE, TRUE)
echoLatexCmd("PercentTabletHomeDetection", round(100 * nrow(subset(dfpc_t, homeContextsWifi + homeContextsGSM > 0))/nrow(dfpc_t)), TRUE, TRUE)
echoLatexCmd("PercentPhoneOfficeDetection", round(100 * nrow(subset(dfpc_p, officeContextsWifi + officeContextsGSM > 0))/nrow(dfpc_p)), TRUE, TRUE)
echoLatexCmd("PercentTabletOfficeDetection", round(100 * nrow(subset(dfpc_t, officeContextsWifi + officeContextsGSM > 0))/nrow(dfpc_t)), TRUE, TRUE)

allDevicesNormalContext <- subset(allDevices, combinedContext != "OKind" & combinedContext != "Overall")
allDevicesNormalContext_P <- subset(allDevicesNormalContext, device == "Phone")
allDevicesNormalContext_T <- subset(allDevicesNormalContext, device == "Tablet")

echoLatexCmd("PhoneInteractionsHomePercent", round(100 * sum(subset(allDevicesNormalContext_P,  combinedContext == "Home")$mean_mean_count_day)/sum(allDevicesNormalContext_P$mean_mean_count_day)), TRUE, TRUE)
echoLatexCmd("PhoneInteractionsOtherMeaningfullPercent", round(100 * sum(subset(allDevicesNormalContext_P,  combinedContext == "OtherMeaningful")$mean_mean_count_day)/sum(allDevicesNormalContext_P$mean_mean_count_day)), TRUE, TRUE)
echoLatexCmd("PhoneInteractionsOfficePercent", round(100 * sum(subset(allDevicesNormalContext_P,  combinedContext == "Office")$mean_mean_count_day)/sum(allDevicesNormalContext_P$mean_mean_count_day)), TRUE, TRUE)
echoLatexCmd("PhoneInteractionsElsewherePercent", round(100 * sum(subset(allDevicesNormalContext_P,  combinedContext == "Elsewhere")$mean_mean_count_day)/sum(allDevicesNormalContext_P$mean_mean_count_day)), TRUE, TRUE)

echoLatexCmd("AverageDailyUnlockedInteractionsSmartphonePercent", round(100 * sum(subset(allDevicesNormalContext_P,  kind == "Unlocked")$mean_mean_count_day)/sum(allDevicesNormalContext_P$mean_mean_count_day)), TRUE, TRUE)
echoLatexCmd("AverageDailyUnlockedInteractionsTabletPercent",  round(100 * sum(subset(allDevicesNormalContext_T,  kind == "Unlocked")$mean_mean_count_day)/sum(allDevicesNormalContext_T$mean_mean_count_day)), TRUE, TRUE)

sink(file = NULL)