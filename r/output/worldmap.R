#install.packages("spam")
#install.packages("rworldmap")
#install.packages("rgdal")
#install.packages("igraph")
#install.packages("treemap")
#install.packages("classInt")

require(treemap)
require(rgdal)
require(rworldmap)
require(data.table)
require(RColorBrewer)
require(classInt)
require(plyr)

# =================================================================================
# Worldmap
# =================================================================================
svg(paste(resultDir, 'worldmap.svg', sep='') ,width=10, height=10, bg=bg, family = family)

data.frame(table(devices$country))

countries <- data.frame(table(devices$country))
countries <- subset(countries, Var1 != "")

cutVector = c(0,10,20,50,100,200,500,1000,2000,2500,3000,4000)
colorVector= c("#f9e8d7",
               "#f4d7ba",
               "#f0c69c",
               "#ebb67f",
               "#e6a562",
               "#e29444",
               "#dd8327",
               "#c3721f",
               "#a6611a",
               "#895015",
               "#6b3f11",
               "#4e2e0c")


malDF <- data.frame(countries)
malMap <- joinCountryData2Map(malDF, joinCode = "NAME", nameJoinColumn = "Var1", verbose = TRUE)
mapCountryData(malMap, nameColumnToPlot="Freq", catMethod = cutVector, missingCountryCol = gray(.8), addLegend='FALSE', colourPalette=colorVector, mapTitle="")

addMapLegend(colourVector = colorVector,
             cutVector = cutVector, 
             legendLabels="all",
             legendIntervals="page",
             legendWidth=0.5,
             legendMar = 12)

dev.off()

# =================================================================================
# Manufacturer treemap
# =================================================================================

devices[devices=="samsung"]<-"Samsung"
devices[devices=="motorola"]<-"Motorola"
devices[devices=="zte"]<-"ZTE"
devices[devices=="symphony"]<-"Symphony"
devices[devices=="Sony Ericsson"]<-"Sony"
devices[devices=="htc"]<-"HTC"
devices[devices=="acer"]<-"Acer"
devices[devices=="asus"]<-"Asus"
devices[devices=="HUAWEI"]<-"Huawei"
devices[devices=="huawei"]<-"Huawei"
devices[devices=="WALTON"]<-"Walton"
devices[devices=="LENOVO"]<-"Lenovo"
devices[devices=="unknown"]<-""

manufacturer <- summarise(group_by(devices, manufacturer), count=n())
manufacturer$color  <- sample(-50:50, nrow(manufacturer), replace=T)
manufacturer <- subset(manufacturer, manufacturer != "" )


svg(paste(resultDir, 'treemap.svg', sep='') ,width=9, height=6, bg=bg, family = family)
treemap(manufacturer,   # run treemap plot of data set car.sales
        title = "",
        index=c("manufacturer"),    # builds boxes out of values in Category:
        vSize=c("count"), # sets the size of each rectangle 
        vColor=c("color"), # sets the color shift (red to green) 
        type="value", # based on the numerical value in Change
        #range=c(0,100),  # sets the outer edges of the scale.
        palette=brewer.pal(11,'BrBG'), # uses a pre-defined diverging scale from color brewer
        algorithm="squarified", # the formula used to draw the rectangles
        border.lwds=0.1,
        overlap.labels = 0.2,
        position.legend="none",
        bg.labels = 0,
        sortID="-size") # arranges the rectangle from largest to smallest
dev.off()

rm(countries, cutVector, colorVector, malDF, malMap, manufacturer)
