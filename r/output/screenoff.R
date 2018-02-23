
require(data.table)
require(ggplot2)
require(ggthemes)

devices_with_screenoff <- subset(devices, screenoff != "null")
devices_with_screenoff <- subset(devices, screenoff != "na")

devices_with_screenoff$screenoff_num <- trunc(as.numeric(as.character(devices_with_screenoff$screenoff)))

devices_with_screenoff <- subset(devices_with_screenoff, screenoff_num >= 0 & screenoff_num < 20000000)

summary(devices_with_screenoff$screenoff_num)

hist(devices_with_screenoff$screenoff_num)

devices_with_screenoff <- data.table(devices_with_screenoff)


devices_with_screenoff$screenoff <- trunc(devices_with_screenoff$screenoff_num/1000)
devices_with_screenoff[screenoff == 121, screenoff := 120]

data <- devices_with_screenoff[,list(count=.N),by=c( "screenoff", "device")]
data <- subset(data, count > 5)
data <- subset(data, screenoff > 0)
data$screenoff <- as.factor(data$screenoff)
data <- na.omit(data)


data[device == "phone", count_rel := (count/sum(subset(data, device=="phone")$count))]
data[device == "tablet", count_rel := (count/sum(subset(data, device=="tablet")$count))]

svg(paste(session_result_folder, '/display_timeout.svg', sep='') ,width=8, height=4, bg=bg, family = family)

ggplot(data = data, aes(x = screenoff, y = count_rel, fill = device)) + 
  geom_bar(stat="identity", position = "dodge", color="black") +
  labs(x = "How long the screen stays active without user input before turning off [sec]", y = "", fill="Device: ") +
 scale_y_continuous(labels = scales::percent)   + 
  ggplotConfig

dev.off()
