package com.example.simpletranslateapp
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class Tools {
    companion object {
        @JvmStatic
        fun TruncateTextIfNeeded(originalText: String): String {
            return if (originalText.length > 33) {
                "${originalText.take(30)}..."
            } else {
                originalText
            }
        }
        fun fromTimeStampGetHM(timestamp: Long) : String {
            val date = Date(timestamp)
            val dateTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            return dateTimeFormat.format(date)
        }
        fun fromTimeStampGetDMY(timestamp: Long) : String {
            val date = Date(timestamp)
            val dateTimeFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            return dateTimeFormat.format(date)
        }


        public fun isInternetAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val networkCapabilities = connectivityManager.activeNetwork ?: return false

            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }


    }
}