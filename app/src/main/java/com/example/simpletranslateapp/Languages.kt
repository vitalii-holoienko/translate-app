package com.example.simpletranslateapp

class Languages {
    companion object{
        public val languages = mapOf<String, String>(
            "Albanian" to "sq",
            "Arabic" to "ar",
            "Armenian" to "hy",
            "Bashkir" to "ba",
            "Belarusian" to "be",
            "Catalan" to "ca",
            "Czech" to "cs",
            "Chinese" to "zh",
            "Danish" to "da",
            "Dutch" to "nl",
            "Greek" to "el",
            "English" to "en",
            "Esperanto" to "eo",
            "Persian" to "fa",
            "Finnish" to "fi",
            "French" to "fr",
            "Georgian" to "ka",
            "Hindi" to "hi",
            "Indonesian" to "id",
            "Italian" to "it",
            "Kazakh" to "kk",
            "Korean" to "ko",
            "Latvian" to "lv",
            "Lithuanian" to "lt",
            "Luxembourgish" to "lb",
            "Macedonian" to "mk",
            "Mongolian" to "mi",
            "Polish" to "pl",
            "Portuguese" to "pt",
            "Romanian" to "ro",
            "Russian" to "ru",
            "Russian" to "ru",
            "Slovak" to "sk",
            "Spanish" to "es",
            "Albanian" to "sq",
            "Serbian" to "sr",
            "Ukrainian" to "uk",
            "Uzbek" to "uz"
        )
        public fun getAmountOfLanguages():Int{
            return languages.size
        }
    }


}
