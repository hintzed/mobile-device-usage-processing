#install.packages('ggthemes', dependencies = TRUE)

require(ggplot2)
require(ggthemes)

# =================================================================================

svg(paste(session_result_folder, '/mean_daily_count_stacked_barchart_from_means.svg', sep='') ,width=barPlot3x2Width, height=barPlot3x2Height, bg=bg, family = family)
data <- summarise(group_by(byDevice, combinedContext, device, kind), value=mean(mean_count_day))
ggplot(data = data, aes(x = device, y = value, fill = combinedContext)) + 
  geom_bar(stat="identity", position = "dodge", color="black") +
  facet_wrap(~kind, nrow=1) +
  labs(x = "", y = "Mean number of sessions per day", fill="Context: ") +
  ggplotConfig
dev.off()

# =================================================================================
svg(paste(session_result_folder, '/median_daily_count_stacked_barchart_from_medians.svg', sep=''), width=barPlot3x2Width, height=barPlot3x2Height, bg=bg, family = family)
data <- summarise(group_by(byDevice, combinedContext, device, kind), value=median(median_count_day))
ggplot(data = data, aes(x = device, y = value, fill = combinedContext)) + 
  geom_bar(stat="identity", position = "dodge", color="black") +
  facet_wrap(~kind, nrow=1) +
  labs(x = "", y = "Median number of sessions per day", fill="Context: ") +
  ggplotConfig
dev.off()

# =================================================================================

svg(paste(session_result_folder, '/mean_daily_sum_stacked_barchart_from_means.svg', sep='') ,width=barPlot3x2Width, height=barPlot3x2Height, bg=bg, family = family)
data <- summarise(group_by(byDevice, combinedContext, device, kind), value=mean(mean_sum_day)/1000/60)
ggplot(data = data, aes(x = device, y = value, fill = combinedContext)) + 
  geom_bar(stat="identity", position = "dodge", color="black") +
  facet_wrap(~kind, nrow=1) +
  labs(x = "", y = "Mean device usage per day [min]", fill="Context: ") +
  ggplotConfig
dev.off()

# =================================================================================

svg(paste(session_result_folder, '/median_daily_sum_stacked_barchart_from_median.svg', sep='') ,width=barPlot3x2Width, height=barPlot3x2Height, bg=bg, family = family)
data <- summarise(group_by(byDevice, combinedContext, device, kind), value=median(median_sum_day)/1000/60)
ggplot(data = data, aes(x = device, y = value, fill = combinedContext)) + 
  geom_bar(stat="identity", position = "dodge", color="black") +
  facet_wrap(~kind, nrow=1) +
  labs(x = "", y = "Median device usage per day [min]", fill="Context: ") +
  ggplotConfig
dev.off()

# =================================================================================

svg(paste(session_result_folder, '/mean_daily_sum_stacked_barchart_from_medians.svg', sep='') ,width=barPlot3x2Width, height=barPlot3x2Height, bg=bg, family = family)
data <- summarise(group_by(byDevice, combinedContext, device, kind), value=mean(median_sum_day)/1000/60)
ggplot(data = data, aes(x = device, y = value, fill = combinedContext)) + 
  geom_bar(stat="identity", position = "dodge", color="black") +
  facet_wrap(~kind, nrow=1) +
  labs(x = "", y = "Median device usage per day [min]", fill="Context: ") +
  ggplotConfig
dev.off()

# =================================================================================

svg(paste(session_result_folder, '/mean_duration_dodge_barchart_from_means.svg', sep='') ,width=barPlot3x2Width, height=barPlot3x2Height, bg=bg, family = family)
data <- summarise(group_by(byDevice, combinedContext, device, kind), value=mean(mean_mean_lenght_day)/1000/60)
ggplot(data = data, aes(x = device, y = value, fill = combinedContext)) +
  geom_bar(stat="identity", position = "dodge", color="black") +
  facet_wrap(~kind, nrow=1) +
  labs(x = "", y = "Mean usage session duration [min]", fill="Context: ") +
  ggplotConfig
dev.off()

# =================================================================================

svg(paste(session_result_folder, '/median_duration_dodge_barchart_from_medians.svg', sep='') ,width=barPlot3x2Width, height=barPlot3x2Height, bg=bg, family = family)
data <- summarise(group_by(byDevice, combinedContext, device, kind), value=median(median_median_lenght_day)/1000/60)
ggplot(data = data, aes(x = device, y = value, fill = combinedContext)) +
  geom_bar(stat="identity", position = "dodge", color="black") +
  facet_wrap(~kind, nrow=1) +
  labs(x = "", y = "Mean usage session duration [min]", fill="Context: ") +
  ggplotConfig
dev.off()

# =================================================================================
# Boxplots
# =================================================================================
svg(paste(session_result_folder, '/count_boxplots_from_means.svg', sep='') ,width=barPlot3x2Width, height=barPlot3x2Height, bg=bg, family = family)

data1 <- byDevice[,c("device", "kind", "mean_count_day")]
data1$metric <- "Mean"

data2 <- byDevice[,c("device", "kind", "median_count_day")]
data2$metric <- "Median"

colnames(data1)  <- c("device", "kind", "value", "metric")
colnames(data2)  <- c("device", "kind", "value", "metric")

data <- rbind(data1, data2)

ggplot(data, aes(x=device, y = value, fill=device))  + 
  stat_boxplot(geom ='errorbar') + 
  geom_boxplot(outlier.shape = NA) + # shorthand for  stat_boxplot(geom='boxplot') +
  scale_y_continuous(breaks=c(0,1,3,10,30,100,300,1000,3000), trans="log1p") +
  coord_cartesian(ylim =c(0, 500)) +           
  labs(x = "", y = "Number of sessions per day", fill="Device: ") +
  facet_grid(metric~kind) +
  ggplotConfig
dev.off()

# =================================================================================

svg(paste(session_result_folder, '/sum_boxplots_from_means.svg', sep='') ,width=barPlot3x2Width, height=barPlot3x2Height, bg=bg, family = family)

data1 <- byDevice[,c("device", "kind", "mean_sum_day")]
data1$metric <- "Mean"

data2 <- byDevice[,c("device", "kind", "median_sum_day")]
data2$metric <- "Median"

colnames(data1)  <- c("device", "kind", "value", "metric")
colnames(data2)  <- c("device", "kind", "value", "metric")

data <- rbind(data1, data2)

ggplot(data, aes(x=device, y = value/1000/60, fill=device))  + 
  stat_boxplot(geom ='errorbar') + 
  geom_boxplot(outlier.shape = NA) + # shorthand for  stat_boxplot(geom='boxplot') +
  scale_y_continuous(breaks=c(0,1,3,10,30,100,300,1000,3000), trans="log1p") +
  coord_cartesian(ylim =c(0, 1500)) +           
  labs(x = "", y = "Device usage per day [min]", fill="Device: ") +
  facet_grid(metric~kind) +
  ggplotConfig
dev.off()

# =================================================================================

svg(paste(session_result_folder, '/length_boxplots_from_means.svg', sep='') ,width=barPlot3x2Width, height=barPlot3x2Height, bg=bg, family = family)

data1 <- byDevice[,c("device", "kind", "mean_mean_lenght_day")]
data1$metric <- "Mean"

data2 <- byDevice[,c("device", "kind", "median_median_lenght_day")]
data2$metric <- "Median"

colnames(data1)  <- c("device", "kind", "value", "metric")
colnames(data2)  <- c("device", "kind", "value", "metric")

data <- rbind(data1, data2)

ggplot(data, aes(x=device, y = value/1000/60, fill=device))  + 
  stat_boxplot(geom ='errorbar') + 
  geom_boxplot(outlier.shape = NA) + # shorthand for  stat_boxplot(geom='boxplot') +
  scale_y_continuous(breaks=c(0,1,3,10,30,100,300,1000,3000), trans="log1p") +
  coord_cartesian(ylim =c(0, 100)) +           
  labs(x = "", y = "Usage session duration [min]", fill="Device: ") +
  facet_grid(metric~kind) +
  ggplotConfig
dev.off()

# =================================================================================

svg(paste(session_result_folder, '/all_session_locked_length.svg', sep='') ,width=6, height=3.1, bg=bg, family = family)

ggplot(subset(all_session_result, all_session_result$session < 300000 & all_session_result$kind == "locked"), aes(x = session/1000)) + 
  geom_density() +
  coord_cartesian(xlim=c(0, 130)) +
  geom_vline(xintercept = 5,  colour="#a6611a", linetype = "longdash") +
  geom_vline(xintercept = 10,  colour="#a6611a", linetype = "longdash") +
  geom_vline(xintercept = 15,  colour="#a6611a", linetype = "longdash") +
  geom_vline(xintercept = 20,  colour="#a6611a", linetype = "longdash") +
  geom_vline(xintercept = 30,  colour="#a6611a", linetype = "longdash") +
  geom_vline(xintercept = 60,  colour="#a6611a", linetype = "longdash") +
  geom_vline(xintercept = 120,  colour="#a6611a", linetype = "longdash") +
  ggplotConfig +
  scale_x_continuous(breaks=c(5,10,15,20,30,60,120)) + 
  labs(x = "Session duration [sec]", y="")

dev.off()

svg(paste(session_result_folder, '/all_session_unlocked_length.svg', sep='') ,width=6, height=2, bg=bg, family = family)

ggplot(subset(all_session_result, all_session_result$session < 60*1000*60 & all_session_result$kind == "unlocked"), aes(x = session/1000/60)) + 
  geom_density() +
  coord_cartesian(xlim=c(0, 15)) +
  ggplotConfig +
  theme(legend.position='none') +
  geom_vline(xintercept = .5,  colour="#a6611a", linetype = "longdash") +
  geom_vline(xintercept = 1,  colour="#a6611a", linetype = "longdash") +
  geom_vline(xintercept = 2,  colour="#a6611a", linetype = "longdash") +
  geom_vline(xintercept = 5,  colour="#a6611a", linetype = "longdash") +
  geom_vline(xintercept = 10,  colour="#a6611a", linetype = "longdash") +
  
  scale_x_continuous(breaks=c(0.5, 1,2,5,10,20,30)) + 
  labs(x = "Session duration [min]", y="")

dev.off()


# =================================================================================

svg(paste(session_result_folder, '/session_count_hist_phone.svg', sep='') ,width=6, height=4, bg=bg, family = family)

ggplot(subset(byDevice, byDevice$device=="Phone"  & combinedContext!="Overall" & combinedContext != "OKind"), aes(x = mean_count_day, fill=kind)) + 
  geom_histogram(binwidth=.5, aes(y=..count../sum(..count..))) +
  facet_wrap(~combinedContext) +
  coord_cartesian(xlim=c(1, 50)) +
  #geom_vline(data = quantiles, aes(xintercept = quantil90, color=device), linetype = "longdash") +
  ggplotConfig +
  scale_fill_manual(values =c("#a6611a","#dfc27d", "#018571")) +
  labs(x = "Mean number of sessions per day/user for phones", y="", fill="Lockstate:")

dev.off()

svg(paste(session_result_folder, '/session_count_histtablet.svg', sep='') ,width=6, height=4, bg=bg, family = family)

ggplot(subset(byDevice, byDevice$device=="Tablet"  & combinedContext!="Overall" & combinedContext != "OKind"), aes(x = mean_count_day, fill=kind)) + 
  geom_histogram(binwidth=.5, aes(y=..count../sum(..count..))) +
  facet_wrap(~combinedContext) +
  coord_cartesian(xlim=c(1, 50)) +
  #geom_vline(data = quantiles, aes(xintercept = quantil90, color=device), linetype = "longdash") +
  ggplotConfig +
  scale_fill_manual(values =c("#a6611a","#dfc27d", "#018571")) +
  labs(x = "Mean number of sessions per day/user for tablets", y="", fill="Lockstate:")

dev.off()

# =================================================================================


svg(paste(session_result_folder, '/session_count_density_combined.svg', sep='') ,width=6, height=4, bg=bg, family = family)

ggplot(subset(byDevice, byDevice$device=="Phone"  & combinedContext=="OKind" & kind == "Unlocked") , aes(x = mean_count_day)) + 
  geom_density() +
  #geom_histogram(binwidth=1) +
  coord_cartesian(xlim=c(1, 250)) +
  #geom_vline(data = quantiles, aes(xintercept = quantil90, color=device), linetype = "longdash") +
  ggplotConfig 
  #scale_fill_manual(values =c("#a6611a","#dfc27d", "#018571")) +
  #labs(x = "Mean number of sessions per day/user for phones", y="", fill="Lockstate:")

dev.off()


dt <- subset(byDevice, byDevice$device=="Phone"  & combinedContext=="OKind" & kind == "Unlocked")

mean(dt$mean_count_day)

dens <- density(dt$mean_count_day)
df <- data.frame(x=dens$x, y=dens$y)
probs <- c(0, 0.25, 0.5, 0.75, 1)
quantiles <- quantile(dt$mean_count_day, prob=probs)
deciles <- quantile(dt$mean_count_day, prob = seq(0, 1, length = 11), type = 5)
df$quant <- factor(findInterval(df$x,quantiles))
ggplot(df, aes(x,y)) + geom_line() + geom_ribbon(aes(ymin=0, ymax=y, fill=quant)) + scale_x_continuous(breaks=quantiles) + scale_fill_brewer(guide="none")


rm(data, data1, data2)
