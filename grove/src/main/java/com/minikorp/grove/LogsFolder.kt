package com.minikorp.grove

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper to manage log files.
 *
 * Multiple instances of this class over the same folder are not safe.
 *
 */
class LogsFolder(val folder: File) {

    companion object {
        private val FILE_NAME_DATE_FORMAT = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
    }

    var lastLogFile: File? = null
        private set

    init {
        if (!folder.exists()) folder.mkdirs()
    }

    /**
     * Create a new log file, and keep track of it.
     */
    fun newLogFile(fileName: String = "log-${FILE_NAME_DATE_FORMAT.format(Date())}.txt"): File {
        val file = File(folder, fileName)
        lastLogFile = file
        return file
    }

    /**
     * Delete any log files created under [folder] older that `maxAge` in ms.
     *
     * This operation may block, consider executing it in another thread.
     *
     * Current log file wont be deleted.
     *
     * @param maxAge max log file age, in ms
     * @param maxCount max number of log files to keep, regardless of age
     * @param filter to ignore files, defaults to all except last open log file.
     * @return Deleted files count.
     */
    fun deleteOldLogs(maxAge: Long,
                      maxCount: Int = Int.MAX_VALUE,
                      filter: (File) -> Boolean = { it != lastLogFile }): Int {
        var deleted = 0
        val files = folder.listFiles() ?: emptyArray()
        files.filter(filter)
            .sortedByDescending(File::lastModified)
            .apply {
                for ((i, file) in this.withIndex()) {
                    val age = System.currentTimeMillis() - file.lastModified()
                    if (age > maxAge || i > maxCount) {
                        if (file.delete()) deleted++
                    }
                }
            }
        return deleted
    }
}