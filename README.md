# Intro Idea Hackathon Challenge by THG x MedINT

## Special Session 1:  Android & Lab-Kit (Environment setup)

> This section is to prepare the Android Watch development environment.
### The basic requirement for Wear OS development
- Android Studio lectric Eel | 2022.1.1 Patch 2
- Android SDK min version 30 (Android 11)


## Software Installation
- Download [Android Studio Electric Eel | 2022.1.1(Patch2)](https://developer.android.com/studio?gclid=Cj0KCQjwlPWgBhDHARIsAH2xdNfvdH5EMEdGkqUGXwW89EqSO6DGgllXc1X19QPRL1eULS8USLOWCyYaAj1eEALw_wcB&gclsrc=aw.ds)
- Install Android SDK: Android 11.0(R) (API 30) 
  - Select show package detail of Android 11 and install Google Play and Google API
    ![Google Play and API](https://github.com/lalitanar/healthtechHackathon/blob/main/pic08.png)
- Install Wear OS Emulator: Wear OS Rectangular
  - Select the system image: Android 11.0 (Wear OS 3) 
- Install Android Phone Emulator: Pixel 6
  - Select the system image: Android 11.0 (R) (API 30)

   

### Install Wear OS Emulator
- Select at icon "Device Manager"
  ![Device Manager](https://github.com/lalitanar/healthtechHackathon/blob/main/pic01.png)
-  Select hardware
-  
   ![Select Hardware](https://github.com/lalitanar/healthtechHackathon/blob/main/pic02.png)
   
-  Select system image 
-  
   ![System Image](https://github.com/lalitanar/healthtechHackathon/blob/main/pic03.png)
   
-  Verify configuration

   ![Verify Configuration](https://github.com/lalitanar/healthtechHackathon/blob/main/pic04.png)
   

## Special Session 2:  Android & Lab-Kit (Always on Kotlin)

### Prepare "Always Ons Kotlin" Project

1. Go to menu bar and select File -> New -> Import Sample
2. At "Select a sample to Import", unser the "Wearable" section, Select "Always On Kotlin"

   ![Always On Kotlin](https://github.com/lalitanar/healthtechHackathon/blob/main/pic05.png)
   
3. Then click "Next" and "Finish"
4. Wait until the project is succeessfully build.
> Please read "README.md" for more details.

### Run "Always On Kotlin" Project"
1. At "Select/Debug Configuration", Select "views" at the dropdown
2. At the "Available Devices", Select Wear OD Rectangular API 30 at the dropdown
3. Cleck Run button
4. Run Always On Kotlin
   ![Run Always On Kotlin](https://github.com/lalitanar/healthtechHackathon/blob/main/pic06.png)
   

5. See the result on Emulator
   ![Always On Kotlin application](https://github.com/lalitanar/healthtechHackathon/blob/main/pic07.png)
   
## References
- [Keep your app visible on wear](https://developer.android.com/training/wearables/views/always-on)
- [Always On Kotlin](https://github.com/android/wear-os-samples/tree/master/AlwaysOnKotlin) sample codes on Github
