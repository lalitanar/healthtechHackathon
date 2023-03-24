# Intro Idea Hackathon Challenge by THG x MedINT

## Special Session 4:  Android & Lab-Kit (Google Sheet)

### Google Cloud API Preparation
- Set the Google Cloud Project
- Enable Google Sheet API
- Set OAUTH 2.0 Authentication
  - Find the package name
  - Use the command below to find SHA1 key from your project
  '''
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

  '''
  
### Google Sheet Preparation
- Create Google sheet
- Update the permission to allow "anyone with link" to be able to edit the sheet.
- Copy the Sheet ID

References
[1] [Setup Google Sheets API for Android (Java/Kotlin)](https://code.luasoftware.com/tutorials/google-sheets-api/setup-google-sheets-api-for-android)
