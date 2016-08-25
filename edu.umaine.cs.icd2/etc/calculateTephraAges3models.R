#################################
##Example of data interpolation
#Last time used Feb. 8,  2014
#by Andrei Kurbatov
#RICE Time scale Interpolatrion for tephra layers
#required signal package
########load depth age time scale file 1
require(signal)
#Make it TRUE for PDF file generation
#DO not forget add at the end dev(off)
plotToPDF = TRUE
outputName="RICE_tephra"
################
dataSetName="flank"
dataSetName1="NoThinning"
dataSetName2="3k300m"
workFolder="./"
############################
pdfFileName=paste("./",outputName,".pdf",sep="")
#####
tephraDepths=c(165.00, 165.02, 345.44, 391.71, 422.375, 508.78, 622.89,729.595)
#tephraDepths=c(165, 277, 346, 396, 423)
tephraNames=c("antt16", "antt15", "antt9", "antt13", "antt11","antt266", "antt267", "antt265")
#Adjust plot size by changing parameters on the next line
if(plotToPDF){pdf(pdfFileName, width=11, height=9)}
#Install and Load package signal see help
#Add points from time scale
#read time scale data file 
dataFile=paste(workFolder,dataSetName,".csv",sep="")
dataFile1=paste(workFolder,dataSetName1,".csv",sep="")
dataFile2=paste(workFolder,dataSetName2,".csv",sep="")
###Set 1
testconn <- file(dataFile, open="r")
csize <- 10000
nolines <- 0
 while((readnlines <- length(readLines(testconn,csize))) >0 ) Nlines<- nolines+readnlines
close(testconn)
#
###Set 2
testconn <- file(dataFile1, open="r")
csize <- 10000
nolines <- 0
readnlines<-0
 while((readnlines <- length(readLines(testconn,csize))) >0 ) Nlines1<- nolines+readnlines
close(testconn)
#
###Set 2
testconn <- file(dataFile2, open="r")
csize <- 10000
nolines <- 0
readnlines<-0
 while((readnlines <- length(readLines(testconn,csize))) >0 ) Nlines2<- nolines+readnlines
close(testconn)
#

depthagefile<-read.table(dataFile,skip=0,sep=",", na.strings = "-99", fill=TRUE, header = TRUE, nrow=Nlines)
print(depthagefile)
depthagefile1<-read.table(dataFile1,skip=0,sep=",", na.strings = "-99", fill=TRUE, header = TRUE, nrow=Nlines1)
print(depthagefile1)
depthagefile2<-read.table(dataFile2,skip=0,sep=",", na.strings = "-99", fill=TRUE, header = TRUE, nrow=Nlines2)
print(depthagefile2)
print("NO errors")
plot(depthagefile$DepthM,depthagefile$AgeCE, xlab="Depth m", ylab="Age C.E.", col="green",pch=20, main="RICE ice core",sub="")
lines(depthagefile1$DepthM,depthagefile1$AgeCE,col="black")
lines(depthagefile2$DepthM,depthagefile2$AgeCE,col="blue")

#Set  calculations
xp <- depthagefile$DepthM
yp <- depthagefile$AgeCE
xf <- tephraDepths
extrap <- TRUE
#lin  <- interp1(xp, yp, xf, 'linear', extrap = extrap)
#use spline function for interpolation
spl  <- interp1(xp, yp, xf, 'spline', extrap = extrap)
#pch  <- interp1(xp, yp, xf, 'pchip', extrap = extrap)
#cub  <- interp1(xp, yp, xf, 'cubic', extrap = extrap)
#near <- interp1(xp, yp, xf, 'nearest', extrap = extrap)
#plot(xp, yp, xlim = c(0, 11))
#points(xf, lin, col = "red")
points(xf, spl, col = "red", pch=22)
#points(xf, pch, col = "orange")
#points(xf, cub, col = "blue")
#points(xf, near, col = "purple")
#print calculated ages
calcAge=round(spl, digits=1)
print(calcAge)
text(xf,spl,paste(calcAge,"C.E.", sep=" "),pos=4)

#Set 1  calculations
xp <- depthagefile1$DepthM
yp <- depthagefile1$AgeCE
xf <- tephraDepths
extrap <- TRUE
#lin  <- interp1(xp, yp, xf, 'linear', extrap = extrap)
#use spline function for interpolation
spl  <- interp1(xp, yp, xf, 'spline', extrap = extrap)
#pch  <- interp1(xp, yp, xf, 'pchip', extrap = extrap)
#cub  <- interp1(xp, yp, xf, 'cubic', extrap = extrap)
#near <- interp1(xp, yp, xf, 'nearest', extrap = extrap)
#plot(xp, yp, xlim = c(0, 11))
#points(xf, lin, col = "red")
points(xf, spl, col = "red", pch=22)
#points(xf, pch, col = "orange")
#points(xf, cub, col = "blue")
#points(xf, near, col = "purple")
#print calculated ages
calcAge=round(spl, digits=1)
print(calcAge)
text(xf,spl,paste(calcAge,"C.E.", sep=" "),pos=1)

#Set2  calculations
xp <- depthagefile2$DepthM
yp <- depthagefile2$AgeCE
xf <- tephraDepths
extrap <- TRUE
#lin  <- interp1(xp, yp, xf, 'linear', extrap = extrap)
#use spline function for interpolation
spl  <- interp1(xp, yp, xf, 'spline', extrap = extrap)
#pch  <- interp1(xp, yp, xf, 'pchip', extrap = extrap)
#cub  <- interp1(xp, yp, xf, 'cubic', extrap = extrap)
#near <- interp1(xp, yp, xf, 'nearest', extrap = extrap)
#plot(xp, yp, xlim = c(0, 11))
#points(xf, lin, col = "red")
points(xf, spl, col = "red", pch=22)
#points(xf, pch, col = "orange")
#points(xf, cub, col = "blue")
#points(xf, near, col = "purple")
#print calculated ages
calcAge=round(spl, digits=1)
print(calcAge)
text(xf,spl,paste(tephraNames,calcAge,"C.E.", sep=" "),pos=2)

#turn off PDF file
if(plotToPDF){dev.off()
# next line will open pdf file in Mac OSX Comment it for other OS's
	system(paste("open", pdfFileName))
}
