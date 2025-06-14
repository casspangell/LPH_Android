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
    private val displayToFileNameMap: Map<String, String> = mapOf(
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
     * Check if a file exists locally
     * @param displayName The display name of the song
     * @return Boolean indicating if the file exists locally
     */
    fun isFileDownloaded(displayName: String): Boolean {
        val fileName = getFileName(displayName) ?: return false
        val localFile = File(localMp3Dir, fileName)
        return localFile.exists()
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
        return displayToFileNameMap[displayName]
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
        Log.d(TAG, "Checking song: $displayName (${file.absolutePath}) - Exists: $exists")
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
                File(localMp3Dir, fileName).exists() 
            }
            .map { it.key }
        Log.d(TAG, "Checking directory: ${localMp3Dir.absolutePath}")
        Log.d(TAG, "Directory exists: ${localMp3Dir.exists()}")
        Log.d(TAG, "Directory contents: ${localMp3Dir.listFiles()?.map { it.name }?.joinToString()}")
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