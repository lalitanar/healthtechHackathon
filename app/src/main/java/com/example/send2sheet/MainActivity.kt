package com.example.send2sheet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.send2sheet.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.*




class MainActivity : Activity(), CoroutineScope by MainScope() {

    lateinit var service: Sheets
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val REQUEST_SIGN_IN = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Binding to the submitBtn object
        val submitButton = findViewById<Button>(R.id.submitBtn)

        //Get Google Sheet API permission
        requestSignIn(this.applicationContext)


        submitButton.setOnClickListener {

            //Send Data to Google Sheet
            appendToSpreadsheet(service)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener { account ->
                        val scopes = listOf(SheetsScopes.SPREADSHEETS)
                        val credential = GoogleAccountCredential.usingOAuth2(this.applicationContext, scopes)
                        credential.selectedAccount = account.account
                        val jsonFactory = JacksonFactory.getDefaultInstance()
                        // GoogleNetHttpTransport.newTrustedTransport()
                        val httpTransport = NetHttpTransport()
                        service = Sheets.Builder(httpTransport, jsonFactory, credential)
                            .setApplicationName(getString(R.string.app_name))
                            .build()
                        //appendToSpreadsheet(service)
                    }
                    .addOnFailureListener { e ->
                        Log.e("error",e.toString())
                    }
            }
        }
    }

    private fun requestSignIn(context: Context) {

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // .requestEmail()
            // .requestScopes(Scope(SheetsScopes.SPREADSHEETS_READONLY))
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
            .build()
        val client = GoogleSignIn.getClient(context, signInOptions)
        startActivityForResult(client.signInIntent, REQUEST_SIGN_IN)
    }

    private fun appendToSpreadsheet(service: Sheets) {

        val spreadsheetId = "1BIpgYdiroSyp028mOfevyz4DNmlhm9x_4pHMkL-QFpc"
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
}