package com.example.simpletranslateapp
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

        fun formatTimestampToDateString(timestamp: Long): String {
            val date = Date(timestamp)
            val dateTimeFormat = SimpleDateFormat("HH:mm \ndd/MM/yy", Locale.getDefault())
            return dateTimeFormat.format(date)
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


    }
}