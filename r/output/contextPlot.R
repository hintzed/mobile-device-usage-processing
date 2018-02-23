svg(paste(resultDir, '/context_detection_result.svg', sep='') ,width=6, height=3.5, bg=bg, family = family)

dp <- subset(devices_filtered_pre_context, device == 'phone')
dt <- subset(devices_filtered_pre_context, device == 'tablet')

plotData <- data.frame("data" = c(
  nrow(subset(dp, dp$homeContextsWifi > 0)) / nrow(dp),
  nrow(subset(dp, dp$officeContextsWifi > 0)) / nrow(dp),
  nrow(subset(dp, dp$otherContextsWifi > 0)) / nrow(dp),
  nrow(subset(dp, dp$homeContextsGSM > 0)) / nrow(dp),
  nrow(subset(dp, dp$officeContextsGSM > 0)) / nrow(dp),
  nrow(subset(dp, dp$otherContextsGSM > 0)) / nrow(dp),
  nrow(subset(dp, dp$homeContextsWifi + dp$homeContextsGSM> 0)) / nrow(dp),
  nrow(subset(dp, dp$officeContextsWifi + dp$officeContextsGSM > 0)) / nrow(dp),
  nrow(subset(dp, dp$otherContextsWifi + dp$otherContextsGSM > 0)) / nrow(dp),
  
  nrow(subset(dt, dt$homeContextsWifi > 0)) / nrow(dt),
  nrow(subset(dt, dt$officeContextsWifi > 0)) / nrow(dt),
  nrow(subset(dt, dt$otherContextsWifi > 0)) / nrow(dt),
  nrow(subset(dt, dt$homeContextsGSM > 0)) / nrow(dt),
  nrow(subset(dt, dt$officeContextsGSM > 0)) / nrow(dt),
  nrow(subset(dt, dt$otherContextsGSM > 0)) / nrow(dt),
  nrow(subset(dt, dt$homeContextsWifi + dt$homeContextsGSM > 0)) / nrow(dt),
  nrow(subset(dt, dt$officeContextsWifi + dt$officeContextsGSM > 0)) / nrow(dt),
  nrow(subset(dt, dt$otherContextsWifi + dt$otherContextsGSM > 0)) / nrow(dt)
), 
"device" = rep(c("Phone", "Tablet"), each=9),
"c_source" =  factor(rep(c("Wi-Fi", "Cell", "Combined"), each=3, times=2), levels=(c( "Combined", "Wi-Fi", "Cell"))),
"context" = factor(rep(c("Home", "Office", "Other mean."), each=1, times=6), levels=(c("Home", "Office", "Other mean." )))
)

ggplot(data = plotData,
       color="black",
       aes(x = factor(c_source), y = data, fill=c_source)) +
  geom_bar(stat="identity", position = "dodge", color="black") +
  #geom_bar(stat="identity", position = "dodge", color="black" , show_guide=FALSE) +
  facet_grid(context~device) +
  labs(x = "", y = "", fill="Context Source: ") +
  scale_y_continuous(labels = percent) +
  theme(legend.position = 'bottom',  legend.spacing = unit(-1, "cm"), axis.text.x = element_text(angle = 90, vjust = 0.5, hjust=1), panel.spacing = unit(.6, "lines")) + 
  guides(fill = guide_legend(reverse=TRUE)) +
  coord_flip(ylim =c(0, 1.0)) +
  ggplotConfig

dev.off()

rm(dp,dt)