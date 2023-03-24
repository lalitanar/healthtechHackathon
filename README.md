# Intro Idea Hackathon Challenge by THG x MedINT

## Special Session 3:  Android & Lab-Kit (Health Services)

### Requirements
Wear OS 3, corresponding to Android 11 (API level 30), includes a service called Health Services. Health Services acts as an intermediary to the various sensors and related algorithms on the device to provide apps with high-quality data related to activity, exercise, and health.

The Health Services API is in beta and is ready for production use.

### Installation
1. Download "Measure Data" project from the [Github link](https://github.com/android/health-samples/tree/main/health-services)
2. Open "Measure Data" project on Android Studio 
3. Run the application to see the result as the figure below.

![Measure Data Application](https://github.com/lalitanar/healthtechHackathon/blob/HealthServices/pic_hr_01.png)

## About Android Health Service
### How Health Services helps app developers [2]
**The content in this section is referred from Android Health Services Document**
Without Health Services, apps must connect to one or multiple sensors, configure each of them appropriately, receive raw sensor data, and use their own algorithms to derive meaningful information. For example, an app might register for updates of Sensor.TYPE_PRESSURE to get the current air pressure, use it to compute the current altitude, and aggregate this data over time to show the elevation changes during a user's activity session.

Health Services automatically configures all fitness and health related sensors appropriately for the use-case, collects sensor data, and computes metrics like heart rate, distance, calories, elevation, floors, speed, pace, and more. Apps can register for this data directly from Health Services.

![Health Services](https://github.com/lalitanar/healthtechHackathon/blob/HealthServices/pic_hr_02.png)

### Practice 1
Showing the notificatin (TOAST) when the **__heart rate result higher than 70 bpm__**. See the figure below.


![Heart rate > 70](https://github.com/lalitanar/healthtechHackathon/blob/HealthServices/pic_hr_03.png?raw=true)


## References
[1] [Health Services on Wear OS](https://developer.android.com/training/wearables/health-services)
[2] [Health Services samples repository](https://github.com/android/health-samples/tree/main/health-services)
