# Intro Idea Hackathon Challenge by THG x MedINT

## Special Session 4:  Android & Lab-Kit (Google Sheet)

### Requirements
- Need to add Google Account from Android Phone/Android Phone Emulator
- Install Google Play for Android Emulator 
  - Instruction how to install Playstor on Emulator is [here](https://stackoverflow.com/questions/71815181/how-can-i-get-google-play-to-work-on-android-emulator-in-android-studio-bumblebe)
- Install Wear OS on Android Phone/Android Phone Emulator
- Pair Android Phone Emulator and Android Watch together


### Google Cloud API Preparation
- Set the Google Cloud Project
- Enable Google Sheet API
- Set OAUTH 2.0 Authentication
  - Find the package name
  - Use the command below to find SHA1 key from your project
  ```
    gradle signingReport

    RESULT

    9:57:37 PM: Executing 'signingReport'...

    Executing tasks: [signingReport] in project /Users/lalita/Documents/Playground/android-project/MeasureData_HRCond


    > Task :app:signingReport
    Variant: debug
    Config: debug
    Store: /Users/lalita/.android/debug.keystore
    Alias: AndroidDebugKey
    MD5: 00:B2:D3:...:E8:F3
    SHA1: 14:35:0...:BC:C7
    SHA-256: 1D:AA:...:D8:23
    Valid until: Wednesday, August 30, 2051
    ----------
    Variant: release
    Config: null
    Store: null
    Alias: null
    ----------
    Variant: debugAndroidTest
    Config: debug
    Store: /Users/lalita/.android/debug.keystore
    Alias: AndroidDebugKey
    MD5: 00:B2:...:E8:F3
    SHA1: 14:35:...:BC:C7
    SHA-256: 1D:AA:...:5F:D8:23
    Valid until: Wednesday, August 30, 2051
    ----------

    BUILD SUCCESSFUL in 3s
    1 actionable task: 1 executed

    Build Analyzer results available
    9:57:41 PM: Execution finished 'signingReport'.

  ```
  
### Google Sheet Preparation
- Create Google sheet according to the figure below
- Update the permission to allow "anyone with link" to be able to edit the sheet.
- Copy the Sheet ID
![Google Sheet](https://github.com/lalitanar/healthtechHackathon/blob/GoogleSheet/pic_gs_01.png?raw=true)


## Sending static data to Google sheet
- Run the application and allow google permission
- Update these code below
  ```
    private fun appendToSpreadsheet(service: Sheets) {

        val spreadsheetId = "1BIp...pc"
        val sheetName = "Sheet1"
        val rows = listOf(listOf("001", "001", "01", "08/03/2023", "78", "90", "1", "0"))
        val valueInputOption = "RAW"
        val range = "'$sheetName'!A1"


        launch(Dispatchers.Default) {
            var result: AppendValuesResponse? = null
            val body = ValueRange().setValues(rows)
            result = service.spreadsheets().values().append(spreadsheetId, range, body)
                .setValueInputOption(valueInputOption)
                .execute()

            // Prints the No. of cells in record
            Log.d("TAG:", "Number of cells: "+result.updates.updatedCells.toString())
        }
    }
  ```
- Add Internet permission to Android Manifest
  ```
  <uses-permission android:name="android.permission.INTERNET"/>
  ```
- Add dependencies to Build.Gradle
  ```
    implementation 'com.google.android.gms:play-services-auth:20.4.1'

    // https://developers.google.com/gsuite/guides/android
    // https://mvnrepository.com/artifact/com.google.api-client/google-api-client-android
    implementation('com.google.api-client:google-api-client-android:1.28.0') {
        exclude group: 'org.apache.httpcomponents'
        exclude module: 'guava-jdk5'

    }
    // https://mvnrepository.com/artifact/com.google.apis/google-api-services-sheets
    implementation('com.google.apis:google-api-services-sheets:v4-rev571-1.25.0') {
        exclude group: 'org.apache.httpcomponents'
        exclude module: 'guava-jdk5'


    }

    implementation 'com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'

  ```

## Practice 2
- Send Current Date/Time to the Google Sheet
![Sheet with current Date/Time](https://github.com/lalitanar/healthtechHackathon/blob/GoogleSheet/pic_gs_01.png?raw=true)

## References
[1] [Setup Google Sheets API for Android (Java/Kotlin)](https://code.luasoftware.com/tutorials/google-sheets-api/setup-google-sheets-api-for-android)
