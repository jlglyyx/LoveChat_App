package com.yang.lovechat.util

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit


fun getMessageTime(date: Date, format: String = "yyyy/MM/dd"): String {
    val currentTime = Calendar.getInstance()
    currentTime[Calendar.HOUR_OF_DAY] = 0
    currentTime[Calendar.SECOND] = 0
    currentTime[Calendar.MINUTE] = 0
    currentTime[Calendar.MILLISECOND] = 0
    if (date.time >= currentTime.getTimeInMillis() && date.time < currentTime.getTimeInMillis() + 86400000) {

        return date.dateFormat("HH:mm")
    }
    val lastTime = Calendar.getInstance()
    lastTime.time = date
    lastTime[Calendar.HOUR_OF_DAY] = 0
    lastTime[Calendar.MINUTE] = 0
    lastTime[Calendar.SECOND] = 0
    val instance = Calendar.getInstance()
    val l = instance.timeInMillis - lastTime.timeInMillis
    val days = TimeUnit.MILLISECONDS.toDays(l)
    return if (days <= 1) {
        "昨天 " + date.dateFormat("HH:mm")
    } else {
        date.dateFormat(format)
    }
}


/**
 * 计算时间 20:23:16
 */
fun getTimeSecond(second: Int): String {
    var h = 0
    var d = 0
    var s = 0
    val temp = second % 3600
    if (second > 3600) {
        h = second / 3600
        if (temp != 0) {
            if (temp > 60) {
                d = temp / 60
                if (temp % 60 != 0) {
                    s = temp % 60
                }
            } else {
                s = temp
            }
        }
    } else {
        d = second / 60
        if (second % 60 != 0) {
            s = second % 60
        }
    }

    if (h == 0) {
        return needZero(d) + ":" + needZero(s)
    }
    return needZero(h) + ":" + needZero(d) + ":" + needZero(s)
}

/**
 * 计算时间 20:06:01  补0
 */
fun needZero(time: Int): String {
    if (time < 10) {
        return "0$time"
    }
    return time.toString() + ""
}
