#install.packages("caret")
install.packages("xtable")

require(data.table)
require(caret)
require(dplyr)
library(lubridate)

users <- read.csv("/home/hintzed/CrowdSignals/CrowdSignals-Pilot-Docs/CrowdSignals-Participant-Survey.csv",  header=TRUE, fileEncoding="cp1252")

data <- read.csv( "/home/hintzed/git/ubicomp-iswc-2014-programming-competition/java/DeviceAnalyzerDownloadClient/data.csv", header=FALSE, fileEncoding="cp1252")
data <- read.csv( "/home/hintzed/git/ubicomp-iswc-2014-programming-competition/java/DeviceAnalyzerDownloadClient/data2.csv", header=FALSE, fileEncoding="cp1252")
data <- read.csv( "/home/hintzed/git/ubicomp-iswc-2014-programming-competition/java/DeviceAnalyzerDownloadClient/data3.csv", header=FALSE, fileEncoding="cp1252")

names(data) <- c("Date", "User", "Value", "Truth", "Combined", "WIFI", "GSM", "interacted")

data <- data[!grepl("NO_DATA", data$Combined),] # REMOVE missing data points
data <- data.table(data)
users <- data.table(users)

data$Combined <- droplevels(data$Combined)

data$User <- as.factor(data$User)
data$Value <- as.factor(data$Value)
data$Truth <- as.factor(data$Truth)
data$Combined <- as.factor(data$Combined)
data$WIFI <- as.factor(data$WIFI)
data$GSM <- as.factor(data$GSM)

data <- data[!grepl("User12", data$User),] # no home
data <- data[!grepl("User23", data$User),] # no home
data <- data[!grepl("User24", data$User),] # no home


data <- data[!grepl("User1$", data$User),] # REMOVE not enough data 
data <- data[!grepl("User2$", data$User),] # REMOVE not enough data
data <- data[!grepl("User31", data$User),] # REMOVE not enough data
data <- data[!grepl("User32", data$User),] # REMOVE not enough data
data <- data[!grepl("User6$", data$User),] # REMOVE not enough data

data <- data[!grepl("User1$", data$User),] # REMOVE wrong reports
data <- data[!grepl("User2$", data$User),] # REMOVE wrong reports
data <- data[!grepl("User24", data$User),] # REMOVE wrong reports
data <- data[!grepl("User32", data$User),] # REMOVE wrong reports
data <- data[!grepl("User36", data$User),] # REMOVE wrong reports
data <- data[!grepl("User42", data$User),] # REMOVE wrong reports


confusionMatrix(reference = data$Truth, data = data$Combined)
data[User != "User16"]

summary(clean$Combined)
summary(clean$Combined)

 tmp <- data[,list(count=.N),by=c( "Value")]
 
  users[,list(count=.N),by=c("Which.of.the.following.categories.best.describes.your.employment.status.")]

user <- "User1" # 0.367
user <- "User2" # 0.48
user <- "User3" # 0.57
user <- "User4" # 0.60
user <- "User8" # 0.07
user <- "User9" # 0.004
user <- "User10" # 0.64
user <- "User11" # 0.11
user <- "User16" # 0.38
user <- "User19" # 0.84
user <- "User20" # 0.64
user <- "User21" # 0.02
user <- "User25" # 0.06
user <- "User26" # 0.76
user <- "User27" # 0.47
user <- "User28" # 0.25
user <- "User29" # 0.85
user <- "User30" # 0.63
user <- "User34" # 0.61
user <- "User35" # 0.88
user <- "User37" # 0.35
user <- "User38" # 0.37
user <- "User39" # 0.62
user <- "User41" # 0.65
user <- "User42" # 0.69
user <- "User43" # 0.03
ss <- data[grepl(user, data$User),]
confusionMatrix(reference = ss$Truth, data = ss$Combined)

