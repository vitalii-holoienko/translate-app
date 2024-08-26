package com.example.simpletranslateapp

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
    }
}