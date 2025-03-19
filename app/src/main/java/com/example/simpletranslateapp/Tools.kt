package com.example.simpletranslateapp
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class Tools {
    companion object {
        @JvmStatic
        fun TruncateTextIfNeeded(originalText: String): String {
            var text = originalText.replace('\n', ' ')
            return if (originalText.length > 33) {
                "${text.take(30)}..."
            } else {
                text
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

        private fun resizeImage(context: Context, uri: Uri, width: Int, height: Int): Bitmap? {
            return try {
                val sourceBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                Bitmap.createScaledBitmap(sourceBitmap, width, height, true)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        private fun getImageUri(inContext: Context?, inImage: Bitmap): Uri {

            val tempFile = File.createTempFile("temprentpk", ".png")
            val bytes = ByteArrayOutputStream()
            inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            val bitmapData = bytes.toByteArray()

            val fileOutPut = FileOutputStream(tempFile)
            fileOutPut.write(bitmapData)
            fileOutPut.flush()
            fileOutPut.close()
            return Uri.fromFile(tempFile)
        }

        fun processImage(uri : Uri, context : Context) : Uri {
            val p = getCorrectedImageResolution(context, uri)
            val resized = if (p!!.first > p.second) resizeImage(context, uri, 2048, 1536)
            else resizeImage(context, uri, 3024, 4032)
            return getImageUri(context, resized!!)
        }
        fun getCorrectedImageResolution(context: Context, uri: Uri): Pair<Int, Int>? {
            return try {
                val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null

                // Получаем исходные размеры
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(inputStream, null, options)
                val width = options.outWidth
                val height = options.outHeight

                // Сбросим inputStream, чтобы считать EXIF-данные
                inputStream.close()
                val exifStream = context.contentResolver.openInputStream(uri) ?: return null
                val exif = ExifInterface(exifStream)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                exifStream.close()

                // Корректируем ориентацию
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_ROTATE_270 ->
                        Pair(height, width) // Если фото повернуто, меняем местами
                    else ->
                        Pair(width, height) // Оставляем как есть
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


    }
}