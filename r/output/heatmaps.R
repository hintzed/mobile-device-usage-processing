require(data.table)
require(ggplot2)
require(dplyr)
library(lubridate)

################################ COUNT ################################ 

byDeviceAndDay <- summarise(group_by(all_session_result, deviceId, combinedContext, hour, day, kind, device), count = n(), sum=sum(session))
byDeviceAndDay <- subset(byDeviceAndDay, !is.na(device))

sumOfCounts <- summarise(group_by(byDeviceAndDay, deviceId, combinedContext, kind, device), sum_count = sum(count), sum_sum = sum(sum))

byDeviceAndDayWithSum <- full_join(byDeviceAndDay, sumOfCounts)

byDeviceAndDayWithSum$count_norm <- byDeviceAndDayWithSum$count/byDeviceAndDayWithSum$sum_count
byDeviceAndDayWithSum$sum_norm <- byDeviceAndDayWithSum$sum/byDeviceAndDayWithSum$sum_sum

byDayAndHour <- summarise(group_by(byDeviceAndDayWithSum, combinedContext, hour, day, kind, device), value_count = sum(count_norm), value_sum = sum(sum_norm))
byDayAndHour$day <- factor(byDayAndHour$day, levels=c("Sunday", "Saturday", "Friday", "Thursday", "Wednesday", "Tuesday", "Monday"), ordered=T)
byDayAndHour$hour <- factor(byDayAndHour$hour, levels=rep(0:23), ordered=T)
byDayAndHour$combinedContext <- factor(byDayAndHour$combinedContext, levels=c("HOME", "OFFICE", "OTHER_MEANINGFUL", "ELSEWHERE", "Overall"), ordered=T, labels=c("Home", "Office", "Other meaningful", "Elsewhere", "Overall"))

phones <- subset(byDayAndHour, device=="phone");
tablets <- subset(byDayAndHour, device=="tablet");

phones$value_count = phones$value_count / max(phones$value_count)
tablets$value_count = tablets$value_count / max(tablets$value_count)
phones$value_sum = phones$value_sum / max(phones$value_sum)
tablets$value_sum = tablets$value_sum / max(tablets$value_sum)

byMonthAndYear <- summarise(group_by(subset(all_session_result, deviceId=="c48d275ef86afac0e7dbe11a3cb4c4c114f4ada9"), combinedContext, month, year, kind, device) , count = n(), sum=sum(session))
byMonthAndYear$yearMonth = paste(byMonthAndYear$year, sprintf("%02d", byMonthAndYear$month), sep="-" )
byMonthAndYear$combinedContext <- factor(byMonthAndYear$combinedContext, levels=c("HOME", "OFFICE", "OTHER_MEANINGFUL", "ELSEWHERE", "Overall"), ordered=T, labels=c("Home", "Office", "Other meaningful", "Elsewhere", "Overall"))

sum(byMonthAndYear$count)

byMonthAndYear <- subset(byMonthAndYear, yearMonth > "2012-07" & yearMonth < "2016-02")

tmp <- summarise(group_by(byMonthAndYear, year), sum=sum(sum)/1000/60/60)

sum(subset(byDeviceAndDay, device == "phone")$sum)/1000/60/60/24/365
sum(subset(byDeviceAndDay, device == "tablet")$sum)/1000/60/60/24/365

svg(paste(resultDir, 'sample_monthly_usage.svg', sep='') ,width=12, height=4, bg=bg)
ggplot(byMonthAndYear,aes(x=factor(yearMonth),y=sum/1000/60/60, fill=combinedContext)) +
  geom_bar(stat="identity", position=position_stack()) +
  ggplotConfig + theme(axis.text.x = element_text(angle = 90, vjust = 0)) +
  labs(x = "", y = "Phone usage [hours]", fill="Context: ") 
dev.off()
                   
colorLow = "#f4d7ba"
colorHigh = "#6b3f11"

svg(paste(resultDir, 'time_count_unlocked_heatmap_phone.svg', sep='') ,width=6, height=9, bg='white')
gg <- ggplot(phones, aes(x=hour, y=day, fill=value_count))
gg <- gg + geom_tile(color="transparent")
gg <- gg + coord_equal() 
gg <- gg + facet_wrap(~combinedContext, ncol = 1)
gg <- gg + theme_minimal()
gg <- gg + scale_fill_gradient2(low  = colorLow,  high = colorHigh, space = "Lab", na.value = "transparent", guide = "colourbar")
gg <- gg + theme(legend.position = "bottom", axis.title.x = element_blank(), axis.title.y = element_blank())
gg <- gg + labs(x = "", y = "", fill="Intensity") 
gg
dev.off()

svg(paste(resultDir, 'time_count_unlocked_heatmap_tablet.svg', sep='') ,width=6, height=9, bg='white')
gg <- ggplot(tablets, aes(x=hour, y=day, fill=value_count))
gg <- gg + geom_tile(color="transparent")
gg <- gg + coord_equal()
gg <- gg + facet_wrap(~combinedContext, ncol = 1)
gg <- gg + scale_fill_gradient2(low = colorLow, high = colorHigh,space = "Lab", na.value = "transparent", guide = "colourbar")
gg <- gg + theme_minimal()
gg <- gg + theme(legend.position = "bottom", axis.title.x = element_blank(), axis.title.y = element_blank())
gg <- gg + labs(x = "", y = "", fill="Intensity") 
gg
dev.off()

svg(paste(resultDir, 'time_sum_unlocked_heatmap_phone.svg', sep='') ,width=6, height=9, bg='white')
gg <- ggplot(phones, aes(x=hour, y=day, fill=value_sum))
gg <- gg + geom_tile(color="transparent")
gg <- gg + coord_equal()
gg <- gg + facet_wrap(~combinedContext, ncol = 1)
gg <- gg + scale_fill_gradient2(low = colorLow, high = colorHigh,space = "Lab", na.value = "transparent", guide = "colourbar")
gg <- gg + theme_minimal()
gg <- gg + theme(legend.position = "bottom", axis.title.x = element_blank(), axis.title.y = element_blank())
gg <- gg + labs(x = "", y = "", fill="Intensity") 
gg
dev.off()

svg(paste(resultDir, 'time_sum_unlocked_heatmap_tablet.svg', sep='') ,width=6, height=9, bg='white')
gg <- ggplot(tablets, aes(x=hour, y=day, fill=value_sum))
gg <- gg + geom_tile(color="transparent")
gg <- gg + coord_equal()
gg <- gg + facet_wrap(~combinedContext, ncol = 1)
gg <- gg + scale_fill_gradient2(low = colorLow, high = colorHigh,space = "Lab", na.value = "transparent", guide = "colourbar")
gg <- gg + theme_minimal()
gg <- gg + theme(legend.position = "bottom", axis.title.x = element_blank(), axis.title.y = element_blank())
gg <- gg + labs(x = "", y = "", fill="Intensity") 
gg
dev.off()