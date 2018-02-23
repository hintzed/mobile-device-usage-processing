processSessions <- function(lockedSessions, unlockedSessions, overallSessions, context) {
  if(!is.na(context)) {
    lockedSessions <- subset(lockedSessions, lockedSessions$combinedContext == context)
    unlockedSessions <- subset(unlockedSessions, unlockedSessions$combinedContext == context)
    overallSessions <- subset(overallSessions, overallSessions$combinedContext == context)
  }
  
  lockedSessions <- subset(lockedSessions, lockedSessions$lockedSession >= 0)
  unlockedSessions <-  subset(unlockedSessions, unlockedSessions$unlockedSession >= 0)
  overallSessions <-  subset(overallSessions, overallSessions$session >= 0)
  
  lockedSessionsByDay <- lockedSessions[,list(mean=mean(lockedSession / 1000), count=length(lockedSession), sum=sum(lockedSession / 1000)), by=date]
  unlockedSessionsByDay <- unlockedSessions[,list(mean=mean(unlockedSession / 1000), count=length(unlockedSession), sum=sum(unlockedSession / 1000)), by=date]
  overallSessionsByDay <- overallSessions[,list(mean=mean(session / 1000), count=length(session), sum=sum(session / 1000)), by=date]
  
  lockedSessionsByDay$year <- year(lockedSessionsByDay$date)
  unlockedSessionsByDay$year <- year(unlockedSessionsByDay$date)
  overallSessionsByDay$year <- year(overallSessionsByDay$date)
  
  data.frame(
    locked_count_mean = mean(lockedSessionsByDay$count),
    locked_length_mean = mean(lockedSessions$lockedSession / 1000),
    locked_sum_mean = mean(lockedSessionsByDay$sum),
    
    locked_count_mean_by_year_2011 = subset(lockedSessionsByDay, year == 2011)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    locked_count_mean_by_year_2012 = subset(lockedSessionsByDay, year == 2012)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    locked_count_mean_by_year_2013 = subset(lockedSessionsByDay, year == 2013)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    locked_count_mean_by_year_2014 = subset(lockedSessionsByDay, year == 2014)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    locked_count_mean_by_year_2015 = subset(lockedSessionsByDay, year == 2015)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    locked_count_mean_by_year_2016 = subset(lockedSessionsByDay, year == 2016)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    
    unlocked_count_mean_by_year_2011 = subset(unlockedSessionsByDay, year == 2011)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    unlocked_count_mean_by_year_2012 = subset(unlockedSessionsByDay, year == 2012)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    unlocked_count_mean_by_year_2013 = subset(unlockedSessionsByDay, year == 2013)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    unlocked_count_mean_by_year_2014 = subset(unlockedSessionsByDay, year == 2014)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    unlocked_count_mean_by_year_2015 = subset(unlockedSessionsByDay, year == 2015)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    unlocked_count_mean_by_year_2016 = subset(unlockedSessionsByDay, year == 2016)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    
    
    overall_count_mean_by_year_2011 = subset(overallSessionsByDay, year == 2011)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    overall_count_mean_by_year_2012 = subset(overallSessionsByDay, year == 2012)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    overall_count_mean_by_year_2013 = subset(overallSessionsByDay, year == 2013)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    overall_count_mean_by_year_2014 = subset(overallSessionsByDay, year == 2014)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    overall_count_mean_by_year_2015 = subset(overallSessionsByDay, year == 2015)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    overall_count_mean_by_year_2016 = subset(overallSessionsByDay, year == 2016)[,list(mean_mean=mean(mean), count_mean=mean(count), sum_mean=mean(sum), mean_median=median(mean), count_median=median(count), sum_median=median(sum))],
    
    locked_count_median = median(lockedSessionsByDay$count),
    locked_length_median = median(lockedSessions$lockedSession / 1000),
    locked_sum_median = median(lockedSessionsByDay$sum),
    
    unlocked_count_mean = mean(unlockedSessionsByDay$count),
    unlocked_length_mean = mean(unlockedSessions$unlockedSession / 1000),
    unlocked_sum_mean = mean(unlockedSessionsByDay$sum),
    
    unlocked_count_median = median(unlockedSessionsByDay$count),
    unlocked_length_median = median(unlockedSessions$unlockedSession / 1000),
    unlocked_sum_median = median(unlockedSessionsByDay$sum),
    
    overall_count_mean = mean(overallSessionsByDay$count),
    overall_count_mean_concat = mean(lockedSessionsByDay$count) + mean(unlockedSessionsByDay$count),
    overall_length_mean = mean(overallSessions$session / 1000),
    overall_sum_mean = mean(overallSessionsByDay$sum),
    overall_sum_mean_concat = mean(lockedSessionsByDay$sum) + mean(unlockedSessionsByDay$sum),
    
    overall_count_median = median(overallSessionsByDay$count),
    overall_length_median = median(overallSessions$session / 1000),
    overall_sum_median = median(overallSessionsByDay$sum)
  )
}