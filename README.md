# High-Pass Gaussian Filter


This plugin implements a High-Pass Gaussian filter on an imput 3D image.



## INSTALLATION


* At time of writing, the ImageJ Updater is down, so the easiest way to use this plugin, please download the pre-compiled JAR from the wiki, and place the JAR into your plugins folder in ImageJ.

    + **A pre-compiled JAR file of this plugin can be downloaded from the [wiki](https://github.com/stevenjwest/High_Pass_Gaussian_Filter/wiki).**


* Alternatively, clone this repo and build from source using Maven:


```bash

git clone https://github.com/stevenjwest/High_Pass_Gaussian_Filter.git
cd High_Pass_Gaussian_Filter
mvn clean package # cleans any target/ directory, then moves through all maven goals upto package

# NB: need Java Version: 1.8.0_101+ for SciJava Maven repository HTTPS support.


# compiled JAR will be available in the target/ directory -> High_Pass_Gaussian_Filter-0.1.0.jar

# NB for successful build need:

#$ java -version
#java version "1.8.0_221"
#Java(TM) SE Runtime Environment (build 1.8.0_221-b11)
#Java HotSpot(TM) 64-Bit Server VM (build 25.221-b11, mixed mode)

#$ git --version
#git version 2.27.0

#$ mvn --version
#Apache Maven 3.6.3 (cecedd343002696d0abb50b32b541b8a6ba2883f)
#Maven home: /usr/local/Cellar/maven/3.6.3_1/libexec
#Java version: 1.8.0_221, vendor: Oracle Corporation, runtime: /Library/Java/JavaVirtualMachines/jdk1.8.0_221.jdk/Contents/Home/jre
#Default locale: en_GB, platform encoding: UTF-8
#OS name: "mac os x", version: "10.13.6", arch: "x86_64", family: "mac"

# NOTE CHECK THE MAVEN JAVA VERSION - if using a version HIGHER than 1.8, should use export JAVA_HOME below to allow maven to see an appropriate
# version of Java:
# export JAVA_HOME var for mvn to use - example code here exports 1.8.0_221:
#export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_221.jdk/Contents/Home/

# you can download Java 1.8 for your machine at this or related link: https://docs.oracle.com/javase/10/install/installation-jdk-and-jre-macos.htm

```



## Why implement this plugin?


High-Pass Gaussian filters are useful for applying an isotropic high-pass image filter with a smooth transition.  These are particularly useful for removing very low frequency information from 3D images, such as auto-fluorescence seen throughout imaged tissue.


This plugin has been designed for use with the [StereoMate Plugins](https://github.com/stevenjwest/StereoMate):


* The recommended SM Threshold Manager workflow is to use a High-Pass Gaussian filter on 3D images to remove low-frequency background fluorescence information in input images.



