package com.minikorp.grove

import android.util.Log
import androidx.core.util.Pools
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * Logger that writes asynchronously to a file.
 *
 * Create a new FileTree instance that will write in a background thread
 * any incoming logs as long as the level is at least `minLevel`.
 *
 * @param file     The file this logger will write.
 * @param minLevel The minimum message level that will be written (inclusive).
 */
class FileLogTree(val file: File, private val minLevel: Int = Log.VERBOSE, append: Boolean = true) : Tree {
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(dispatcher + Job())
    private val channel = Channel<LogLine>(Channel.UNLIMITED)
    private val writer = FileOutputStream(file, append).writer()
    private val logLinePool = Pools.SynchronizedPool<LogLine>(5)

    init {
        scope.launch {
            try {
                while (isActive) {
                    val logLine = channel.receive()
                    val lines = logLine.format()
                    for (line in lines) {
                        writer.write(line)
                    }
                    logLinePool.release(logLine)
                }
            } finally {
                writer.use {
                    flush()
                }
            }
        }
    }

    /**
     * Flush the file, this call is required before application terminates or the file will be empty.
     */
    fun flush() {
        try {
            writer.flush()
        } catch (e: IOException) {
            Grove.e(e) { "Flush failed" }
        }
    }

    /**
     * Close the file and exit. This is a blocking call to ensure contents are flushed.
     */
    fun close() {
        runBlocking {
            scope.coroutineContext[Job]!!.cancelAndJoin()
        }
    }

    override fun log(priority: Int, tag: String, message: String) {
        val logLine = logLinePool.acquire() ?: LogLine()
        logLine.tag = tag
        logLine.message = message
        logLine.level = priority
        logLine.date.time = System.currentTimeMillis()
        channel.offer(logLine)
    }

    private class LogLine {
        companion object {
            private val LOG_FILE_DATE_FORMAT = SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.US)
        }

        val date = Date()
        var level: Int = 0
        var message: String? = null
        var tag: String? = null

        internal fun clear() {
            message = null
            tag = null
            date.time = System.currentTimeMillis()
            level = 0
        }

        internal fun format(): List<String> {
            message?.let { msg ->
                val lines = msg.split('\n').dropLastWhile(String::isEmpty)
                val levelString = when (level) {
                    Log.VERBOSE -> "V"
                    Log.DEBUG -> "D"
                    Log.INFO -> "I"
                    Log.WARN -> "W"
                    Log.ERROR -> "E"
                    Log.ASSERT -> "A"
                    else -> "V"
                }
                //[29-04-1993 01:02:34.567 D/SomeTag: The value to Log]
                val prelude = "[${LOG_FILE_DATE_FORMAT.format(date)}] $levelString/$tag"
                return lines.map { "$prelude $it \r\n" }
            }
            return emptyList()
        }
    }
}