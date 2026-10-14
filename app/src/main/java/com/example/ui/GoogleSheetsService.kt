package com.example.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Service to push alerts and summaries to a Google Sheet.
 * Note: Direct Google Sheets API from Android requires OAuth2 or exposing a Service Account Key (which is insecure).
 * The recommended approach is to use a Google Apps Script Webhook deployed as a Web App.
 * 
 * Apps Script Code Example:
 * function doPost(e) {
 *   var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
 *   var data = JSON.parse(e.postData.contents);
 *   sheet.appendRow([new Date(), data.type, data.message, data.value, data.details]);
 *   return ContentService.createTextOutput(JSON.stringify({"status": "success"})).setMimeType(ContentService.MimeType.JSON);
 * }
 */
object GoogleSheetsService {

    suspend fun pushAlertToSheet(
        webhookUrl: String,
        type: String,
        message: String,
        value: String,
        details: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        if (webhookUrl.isBlank()) return@withContext false

        try {
            val jsonPayload = JSONObject().apply {
                put("type", type)
                put("message", message)
                put("value", value)
                put("details", details)
            }

            val connection = URL(webhookUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            connection.outputStream.use { os ->
                val input = jsonPayload.toString().toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            connection.disconnect()

            return@withContext responseCode in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
