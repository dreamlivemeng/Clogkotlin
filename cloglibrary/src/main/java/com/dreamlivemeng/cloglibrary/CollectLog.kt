package com.dreamlivemeng.cloglibrary

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * collect android  crashes the log<br>
 * UncaughtException class,When the program occurs Uncaught exception, there are such procedures to take over.<br>
 * This is a tool that write the Android crash log to the sd card.
 * The default is Android / data / package name / files / logs below
 * put in this path, do not need any permissions
 * <p>
 * Created by dreamlivemeng on 2014/11/11.
 */
class CollectLog : Thread.UncaughtExceptionHandler {
    companion object {
        val TAG: String? = CollectLog::class.java.canonicalName
        val INSTANCE: CollectLog = CollectLog()

        fun getInstance(): CollectLog {
            return INSTANCE
        }
    }

    var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    var mContext: Context? = null
    val infos = mutableMapOf<String, String>()

    val formatter: DateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
    var filePath: String = ""

    fun init(context: Context) {
        mContext = context
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    fun init(context: Context, path: String) {
        init(context)
        filePath = path
    }

    override fun uncaughtException(thread: Thread?, ex: Throwable?) {
        if (!handleException(ex!!) && mDefaultHandler != null) {
            // If the user does not deal with the system is the default exception handler to handle
            mDefaultHandler!!.uncaughtException(thread, ex)
        } else {
            try {
                Thread.sleep(3000)
            } catch (e: InterruptedException) {
//                Log.e(TAG, "error : ", e);
            }
            // exit app
            Process.killProcess(Process.myPid())
            //            System.exit(0);
        }
    }

    fun handleException(ex: Throwable): Boolean {
        if (null == ex) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            collectDeviceInfo(mContext!!)
        }
        var str: String? = saveCrashInfo2File(ex)
        Log.e(TAG, str)
        return false
    }

    /**
     * Collect device parameter infomation
     * @param ctx Context
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun collectDeviceInfo(ctx: Context) {
        try {
            var pm: PackageManager = ctx.packageManager
            var pi: PackageInfo = pm.getPackageInfo(ctx.packageName, PackageManager.GET_ACTIVITIES)
            if (pi != null) {
                val versionName =
                    if (pi.versionName == null) "null" else pi.versionName
                val versionCode = pi.longVersionCode.toString() + ""

                infos["versionName"] = versionName
                infos["versionCode"] = versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {

        }
        val fields = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                infos[field.name] = field[null].toString()
            } catch (e: Exception) {
            }
        }
    }

    private fun saveCrashInfo2File(ex: Throwable): String? {
        val sb = StringBuffer()
        for ((key, value) in infos) {
            sb.append("[$key, $value]\n")
        }

        sb.append("\n${getStackTraceString(ex)}")
        try {
            val time = formatter.format(Date())
            val fileName = "CRS_$time.txt"
            var sdDir: File? = null
            sdDir = mContext!!.getExternalFilesDir("logs")!!.absoluteFile
            var file: File? = null
            file = if (!TextUtils.isEmpty(filePath)) {
                val files = File(filePath)
                if (!files.exists()) {
                    //Create a directory
                    files.mkdirs()
                }
                File(filePath + File.separator + fileName)
            } else {
                File(sdDir.toString() + File.separator + fileName)
            }
            if (file == null) {
                file = File(sdDir.toString() + File.separator + fileName)
            }
            val fos = FileOutputStream(file)
            fos.write(sb.toString().toByteArray())
            fos.close()
            return file.absolutePath
        } catch (e: java.lang.Exception) {
        }
        return null
    }

    fun getStackTraceString(tr: Throwable): String? {
        try {
            if (tr == null) {
                return ""
            }
            var t: Throwable = tr
            while (t != null) {
                if (t is UnknownHostException) {
                    return ""
                }
                t = t.cause!!
            }
            var sw: StringWriter = StringWriter()
            var pw: PrintWriter = PrintWriter(sw)
            tr.printStackTrace(pw)
            return sw.toString()
        } catch (e: Exception) {
            return ""
        }
    }
}