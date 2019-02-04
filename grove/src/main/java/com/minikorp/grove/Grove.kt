package com.minikorp.grove

import android.util.Log.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.regex.Pattern


/**
 * Logging for lazy people.
 * Adapted from Jake Wharton's Timber
 * [https://github.com/JakeWharton/timber]
 */
object Grove {

    private val ANONYMOUS_CLASS_PATTERN = Pattern.compile("(\\$\\d+)+$")

    val defaultTag = "Grove"
    var debugTags = true
    var forest: Array<Tree> = emptyArray()
    private var explicitTag = ThreadLocal<String>()

    fun plant(tree: Tree) {
        forest += tree
    }

    fun uproot(tree: Tree) {
        forest = forest.filter { it != tree }.toTypedArray()
    }

    inline fun v(throwable: Throwable? = null, crossinline msg: () -> Any?) = log(VERBOSE, throwable, msg)
    fun v(throwable: Throwable) = log(VERBOSE, throwable) { "Error" }

    inline fun d(throwable: Throwable? = null, crossinline msg: () -> Any?) = log(DEBUG, throwable, msg)
    fun d(throwable: Throwable) = log(DEBUG, throwable) { "Error" }

    inline fun i(throwable: Throwable? = null, crossinline msg: () -> Any?) = log(INFO, throwable, msg)
    fun i(throwable: Throwable) = log(INFO, throwable) { "Error" }

    inline fun w(throwable: Throwable? = null, crossinline msg: () -> Any?) = log(WARN, throwable, msg)
    fun w(throwable: Throwable) = log(WARN, throwable) { "Error" }

    inline fun e(throwable: Throwable? = null, crossinline msg: () -> Any?) = log(ERROR, throwable, msg)
    fun e(throwable: Throwable) = log(ERROR, throwable) { "Error" }

    inline fun wtf(throwable: Throwable? = null, crossinline msg: () -> Any?) = log(ASSERT, throwable, msg)
    fun wtf(throwable: Throwable) = log(ASSERT, throwable) { "Error" }

    inline fun log(priority: Int, throwable: Throwable? = null, msg: () -> Any?) {
        val tag = consumeTag()
        var message: String? = null //Lazy evaluation
        for (tree in forest) {
            if (!tree.isLoggable(tag, priority)) continue
            if (message == null) {
                message = msg().toString()
                if (throwable != null) message += "\n${getStackTraceString(throwable)}"
            }
            tree.log(priority, tag, message.orEmpty())
        }
    }

    fun tag(tag: String): Grove {
        explicitTag.set(tag)
        return this
    }

    fun consumeTag(): String {
        val tag = explicitTag.get()
        return when {
            tag != null -> {
                explicitTag.remove()
                tag
            }
            debugTags -> createDebugTag(2)
            else -> defaultTag
        }
    }

    private fun createDebugTag(stackIndex: Int): String {
        val stackTrace = Throwable().stackTrace
        if (stackTrace.size <= stackIndex) {
            throw IllegalStateException(
                "Synthetic stacktrace didn't have enough elements: are you using proguard?"
            )
        }
        var i = stackIndex
        do {
            val tag = createStackElementTag(stackTrace[i])
            if (tag != "Grove") return tag
            i++
        } while (i < stackTrace.size)
        return "Grove"
    }

    fun getStackTraceString(t: Throwable): String {
        // Don't replace this with Log.getStackTraceString() - it hides
        // UnknownHostException, which is not what we want.
        val sw = StringWriter(256)
        val pw = PrintWriter(sw, false)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    private fun createStackElementTag(element: StackTraceElement): String {
        var tag = element.className
        val m = ANONYMOUS_CLASS_PATTERN.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        return tag.substring(tag.lastIndexOf('.') + 1)
    }
}

interface Tree {
    fun isLoggable(tag: String, priority: Int): Boolean = true
    fun log(priority: Int, tag: String, message: String)
}

