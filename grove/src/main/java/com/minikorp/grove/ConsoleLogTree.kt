package com.minikorp.grove

import android.util.Log

/** A [Tree] that uses [Log] or [System.out] for printing. */
class ConsoleLogTree(val useSystemOut: Boolean = false) : Tree {

    private val MAX_LOG_LENGTH = 4000

    /**
     * Break up `message` into maximum-length chunks (if needed) and send to either
     * [Log.println()][Log.println] or
     * [Log.wtf()][Log.wtf] for logging.

     * {@inheritDoc}
     */
    override fun log(priority: Int, tag: String, message: String) {
        if (message.length < MAX_LOG_LENGTH) {
            if (priority == Log.ASSERT) {
                Log.wtf(tag, message)
            } else {
                Log.println(priority, tag, message)
            }
            return
        }

        // Split by line, then ensure each line can fit into Log's maximum length.
        var i = 0
        val length = message.length
        while (i < length) {
            var newline = message.indexOf('\n', i)
            newline = if (newline != -1) newline else length
            do {
                val end = Math.min(newline, i + MAX_LOG_LENGTH)
                val part = message.substring(i, end)
                if (useSystemOut) {
                    System.out.println("$tag: $part")
                } else {
                    if (priority == Log.ASSERT) {
                        Log.wtf(tag, part)
                    } else {
                        Log.println(priority, tag, part)
                    }
                }
                i = end
            } while (i < newline)
            i++
        }
    }
}