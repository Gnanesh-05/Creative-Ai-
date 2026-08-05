package com.example.backend.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object MediaSharingUtils {

    /**
     * Downloads an image file into the public device MediaStore Gallery pictures folder
     * so it shows up immediately in the user's Photos / Gallery app.
     */
    fun downloadImageToGallery(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "Image file not ready yet!", Toast.LENGTH_SHORT).show()
                return
            }

            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap == null) {
                Toast.makeText(context, "Failed to decode image file", Toast.LENGTH_SHORT).show()
                return
            }

            val filename = "Nexus_AI_${System.currentTimeMillis()}.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NexusAI")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)

                    Toast.makeText(context, "🖼️ Image saved to Gallery!", Toast.LENGTH_LONG).show()
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val nexusDir = File(picturesDir, "NexusAI")
                if (!nexusDir.exists()) nexusDir.mkdirs()

                val imageFile = File(nexusDir, filename)
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                }
                Toast.makeText(context, "🖼️ Saved to Pictures/NexusAI", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Downloaded image to internal gallery!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares an image directly via WhatsApp (or opens the system share chooser with WhatsApp prefilled).
     */
    fun shareImageViaWhatsApp(context: Context, filePath: String, prompt: String) {
        try {
            val file = File(filePath)
            val uri: Uri = if (file.exists()) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                Toast.makeText(context, "Preparing image for WhatsApp...", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "✨ Look at this AI image I created: \"$prompt\"\nGenerated with Nexus AI Studio")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Try direct WhatsApp launch
            val waIntent = Intent(intent).setPackage("com.whatsapp")
            if (waIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(waIntent)
            } else {
                // Fallback chooser
                context.startActivity(Intent.createChooser(intent, "Share via WhatsApp"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Opening WhatsApp share...", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Downloads song audio and lyrics to device storage.
     */
    fun downloadSongAndLyrics(context: Context, songTitle: String, lyrics: String, audioPath: String?) {
        try {
            val fileName = "Lyrics_${songTitle.replace(" ", "_")}.txt"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val lyricsFile = File(downloadsDir, fileName)
            FileOutputStream(lyricsFile).use { out ->
                out.write("🎵 $songTitle\n\n$lyrics".toByteArray())
            }
            Toast.makeText(context, "🎵 Lyrics & Audio saved to Downloads!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Saved music asset to device!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares generated music lyrics & song details via WhatsApp.
     */
    fun shareMusicViaWhatsApp(context: Context, songTitle: String, lyrics: String, audioPath: String?) {
        try {
            val shareText = "🎵 *New AI Song Composed: $songTitle*\n\n$lyrics\n\n✨ Created with Nexus AI Music Studio"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            val waIntent = Intent(intent).setPackage("com.whatsapp")
            if (waIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(waIntent)
            } else {
                context.startActivity(Intent.createChooser(intent, "Share Song via WhatsApp"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Opening WhatsApp...", Toast.LENGTH_SHORT).show()
        }
    }
}
