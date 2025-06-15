package org.lovepeaceharmony.androidapp.utility

import android.content.Context
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MP3DownloadManager(private val context: Context) {
    companion object {
        private const val TAG = "MP3DownloadManager"
        private const val FIREBASE_BUCKET = "gs://love-peace-harmony.appspot.com"
        private const val FIREBASE_SONGS_PATH = "Songs"
        private const val LOCAL_MP3_DIR = "songs"
        private const val DEFAULT_SONG = "01_Mandarin_Soul_Language_English.mp3"
    }

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val songsRef: StorageReference = storage.getReferenceFromUrl(FIREBASE_BUCKET).child(FIREBASE_SONGS_PATH)
    private val localMp3Dir: File = File(context.filesDir, LOCAL_MP3_DIR)

    // Updated mapping to match actual file names
    val displayToFileNameMap: Map<String, String> = mapOf(
        "Mandarin Soul Language and English" to "01_Mandarin_Soul_Language_English.mp3",
        "Instrumental" to "02_Instrumental.mp3",
        "Hindi Soul Language English" to "04_Hindi_Soul_Language_English.mp3",
        "Spanish" to "05_Spanish.mp3",
        "Mandarin English German" to "06_Mandarin_English_German.mp3",
        "French" to "07_French.mp3",
        "French Antillean Creole" to "08_French_Antillean_Creole.mp3",
        "Aloha Maluhai Lokahi LPH In Hawaiian" to "09_Aloha_Maluhai_Lokahi_LPH_In_Hawaiian.mp3",
        "Lu La Li Version English and Hawaiian" to "10_Lu_La_Li_Version_English_and_Hawaiian.mp3",
        "Love Peace Harmony In English" to "11_Love_Peace_Harmony_in_English.mp3"
    )

    init {
        initializeLocalDirectory()
        copyDefaultSongFromAssets()
        ensureAllSongsInDb()
        ensureAtLeastOneSongEnabled()
    }

    /**
     * Initialize the local directory for storing MP3 files
     */
    private fun initializeLocalDirectory() {
        try {
            if (!localMp3Dir.exists()) {
                val created = localMp3Dir.mkdirs()
                if (!created) {
                    Log.e(TAG, "Failed to create local MP3 directory")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing local MP3 directory", e)
        }
    }

    /**
     * Copy the default song from assets to internal storage
     */
    private fun copyDefaultSongFromAssets() {
        try {
            val defaultSongFile = File(localMp3Dir, DEFAULT_SONG)
            if (!defaultSongFile.exists()) {
                context.assets.open("songs/$DEFAULT_SONG").use { input ->
                    FileOutputStream(defaultSongFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d(TAG, "Default song copied from assets to ${defaultSongFile.absolutePath}")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error copying default song from assets", e)
        }
    }

    /**
     * Ensure all songs from the map are present in the SongsModel database
     */
    private fun ensureAllSongsInDb() {
        val allDisplayNames = getAllSongDisplayNames()
        val existingSongs = org.lovepeaceharmony.androidapp.model.SongsModel.getSongsModelList(context)?.map { it.songTitle } ?: emptyList()
        allDisplayNames.forEachIndexed { i, displayName ->
            val fileName = getFileName(displayName)
            if (fileName != null && !existingSongs.contains(fileName)) {
                val song = org.lovepeaceharmony.androidapp.model.SongsModel().apply {
                    id = i
                    songTitle = fileName
                    songPath = "songs/$fileName"
                    isChecked = (fileName == "01_Mandarin_Soul_Language_English.mp3")
                    isToolTip = false
                }
                org.lovepeaceharmony.androidapp.model.SongsModel.insertSong(context, song)
            }
        }
    }

    /**
     * Ensure at least one song is enabled (01 as fallback)
     */
    private fun ensureAtLeastOneSongEnabled() {
        val dbSongs = org.lovepeaceharmony.androidapp.model.SongsModel.getSongsModelList(context) ?: emptyList()
        val anyEnabled = dbSongs.any { it.isChecked }
        if (!anyEnabled) {
            org.lovepeaceharmony.androidapp.model.SongsModel.updateIsEnabled(context, "01_Mandarin_Soul_Language_English.mp3", true)
        }
    }

    /**
     * Check if a file exists locally
     * @param displayName The display name of the song
     * @return Boolean indicating if the file exists locally
     */
    fun isFileDownloaded(displayName: String): Boolean {
        val fileName = getFileName(displayName) ?: return false
        val localFile = File(localMp3Dir, fileName)
        val exists = localFile.exists()
        Log.d(TAG, "isFileDownloaded: displayName='$displayName', fileName='$fileName', path='${localFile.absolutePath}', exists=$exists")
        return exists
    }

    /**
     * Get the local file path for a song
     * @param displayName The display name of the song
     * @return String representing the local file path
     */
    fun getLocalFilePath(displayName: String): String {
        val fileName = getFileName(displayName) ?: return ""
        return File(localMp3Dir, fileName).absolutePath
    }

    /**
     * Get the Firebase Storage reference for a song
     * @param displayName The display name of the song
     * @return StorageReference for the song in Firebase Storage
     */
    fun getFirebaseReference(displayName: String): StorageReference? {
        val fileName = getFileName(displayName) ?: return null
        return songsRef.child(fileName)
    }

    /**
     * Convert display name to file name
     * @param displayName The display name of the song
     * @return String representing the file name, or null if not found
     */
    fun getFileName(displayName: String): String? {
        val fileName = displayToFileNameMap[displayName]
        Log.d(TAG, "getFileName: Looking up displayName='$displayName', result='$fileName'")
        return fileName
    }

    /**
     * Check if a specific song is downloaded
     * @param displayName The display name of the song to check
     * @return Boolean indicating if the song is downloaded
     */
    fun checkSongDownloaded(displayName: String): Boolean {
        val fileName = getFileName(displayName) ?: return false
        val file = File(localMp3Dir, fileName)
        val exists = file.exists()
        Log.d(TAG, "checkSongDownloaded: displayName='$displayName', fileName='$fileName', path='${file.absolutePath}', exists=$exists")
        return exists
    }

    /**
     * Get all available song display names
     * @return List of all song display names
     */
    fun getAllSongDisplayNames(): List<String> {
        return displayToFileNameMap.keys.toList()
    }

    /**
     * Get the local MP3 directory
     * @return File representing the local MP3 directory
     */
    fun getLocalMp3Directory(): File {
        return localMp3Dir
    }

    /**
     * Check for downloaded songs in the local directory
     * @return List of display names of downloaded songs
     */
    fun getDownloadedSongs(): List<String> {
        val downloadedSongs = displayToFileNameMap.entries
            .filter { (_, fileName) ->
                val file = File(localMp3Dir, fileName)
                val exists = file.exists()
                Log.d(TAG, "getDownloadedSongs: fileName='$fileName', path='${file.absolutePath}', exists=$exists")
                exists
            }
            .map { it.key }
        Log.d(TAG, "getDownloadedSongs: Found ${downloadedSongs.size} downloaded songs: ${downloadedSongs.joinToString()}")
        return downloadedSongs
    }

    /**
     * Check for downloaded songs and log the results
     */
    private fun checkDownloadedSongs() {
        val downloadedSongs = getDownloadedSongs()
        Log.d(TAG, "Found ${downloadedSongs.size} downloaded songs: ${downloadedSongs.joinToString()}")
    }
}

// Top-level function for copying the default song from assets to internal storage
fun copyDefaultSongToInternalStorageIfNeeded(context: Context) {
    val fileName = "01_Mandarin_Soul_Language_English.mp3"
    val internalDir = File(context.filesDir, "songs")
    if (!internalDir.exists()) {
        val created = internalDir.mkdirs()
        Log.d("MP3DownloadManager", "Created internal songs directory: $created at ${internalDir.absolutePath}")
    }
    val internalFile = File(internalDir, fileName)
    if (internalFile.exists()) {
        Log.d("MP3DownloadManager", "Default song already exists in internal storage: ${internalFile.absolutePath}")
        return // Already copied
    }
    try {
        Log.d("MP3DownloadManager", "Copying default song from assets/songs/$fileName to internal storage...")
        context.assets.open("songs/$fileName").use { input ->
            FileOutputStream(internalFile).use { output ->
                input.copyTo(output)
            }
        }
        Log.d("MP3DownloadManager", "Successfully copied default song to: ${internalFile.absolutePath}")
    } catch (e: Exception) {
        Log.e("MP3DownloadManager", "Failed to copy default song: ${e.message}", e)
    }
} 