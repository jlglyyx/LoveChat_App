@file:JvmName("AppUtil")

package com.yang.lovechat.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Property
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hjq.shape.view.ShapeTextView
import com.yang.lovechat.R
import com.yang.lovechat.app.BaseApplication
import com.yang.lovechat.constant.AppConstant
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern
import kotlin.jvm.java


val gson: Gson = GsonBuilder().create()

inline fun <reified T> T.toJson(): String {
    return gson.toJson(this, object : TypeToken<T>() {}.type)
}


/**
 * 使用前确保一定是json数据
 * @return 解析Json
 */
inline fun <reified T> String.fromJson(): T {

    return gson.fromJson(this, object : TypeToken<T>() {}.type)
}

/**
 * 使用前确保一定是json数据
 * @return 解析Json List
 */
inline fun <reified T> String.formatListJson(): MutableList<T> {

    return gson.fromJson(this, object : TypeToken<MutableList<T>>() {}.type)

}


fun showShort(msg: Any?) {
    if (null == msg) {
        return
    }
    ToastUtils.showShort(msg.toString())
}

fun showLong(msg: Any?) {
    if (null == msg) {
        return
    }
    ToastUtils.showLong(msg.toString())

}


fun Date.dateFormat(
    format: String = "yyyy.MM.dd HH:mm:ss",
    locale: Locale = Locale.US,
    timeZone: TimeZone? = null
): String {
    return SimpleDateFormat(format, locale).apply {

        if (null != timeZone) {
            this.timeZone = timeZone
        }

    }.format(this)
}


fun View.edgeToEdgeAll(type: Int = WindowInsetsCompat.Type.systemBars()) {

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBars = insets.getInsets(type)
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
    }
}

fun View.edgeToEdgeTop(type: Int = WindowInsetsCompat.Type.systemBars()) {

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBars = insets.getInsets(type)
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
        insets
    }
}

fun View.edgeToEdgeBottom(type: Int = WindowInsetsCompat.Type.systemBars()) {

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBars = insets.getInsets(type)
        v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
        insets
    }
}


/**
 * @return 宽高集合
 */
fun getScreenPx(context: Context): IntArray {
    val resources = context.resources
    val displayMetrics = resources.displayMetrics
    val widthPixels = displayMetrics.widthPixels
    val heightPixels = displayMetrics.heightPixels
    return intArrayOf(widthPixels, heightPixels)
}

/**
 * @return 根据手机的分辨率从 dp 的单位 转成为 px(像素)
 */
fun Float.dip2px(context: Context): Int {
    val scale = context.resources.displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

/**
 * @return 根据手机的分辨率从 px(像素) 的单位 转成为 dp
 */
fun Float.px2dip(context: Context): Int {
    val scale = context.resources.displayMetrics.density
    return (this / scale + 0.5f).toInt()
}


fun View.mRotation(type:Property<View, Float> = View.ROTATION,vararg array: Float): ObjectAnimator {
    return ObjectAnimator.ofFloat(this, type, *array)
}

fun View.mTranslation(type:Property<View, Float> = View.TRANSLATION_X, vararg array: Float): ObjectAnimator {
    return ObjectAnimator.ofFloat(this, type, *array)
}

fun View.mAlpha(vararg array: Float): ObjectAnimator {
    return ObjectAnimator.ofFloat(this, View.ALPHA, *array)
}

fun View.mScale(type:Property<View, Float> = View.SCALE_X, vararg array: Float): ObjectAnimator {
    return ObjectAnimator.ofFloat(this, type, *array)
}


/**
 * @return 返回xx,xx
 */
fun MutableList<String>.formatWithSymbol(symbol: String = ","): String {
    val stringBuilder = StringBuilder()
    this.forEachIndexed { index, s ->
        if (index == this.size - 1) {
            stringBuilder.append(s)
        } else {
            stringBuilder.append(s).append(symbol)
        }
    }
    return stringBuilder.toString()
}

/**
 * @return 解析xx,xx
 */
fun String?.symbolToList(symbol: String = ","): MutableList<String> {

    if (this.isNullOrEmpty()) return mutableListOf()

    return this.split(symbol).toMutableList()

}

fun View.clicks(duration: Long = AppConstant.Constant.CLICK_TIME, onClick: (v: View?) -> Unit) {

    ClickUtils.applySingleDebouncing(this, duration) { v -> onClick(v) }

}




/**
 * 是否是手机号
 */
fun String.isPhone(): Boolean {
    val pattern = Pattern.compile("^1[3-9]\\d{9}")
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

/**
 * 隐藏手机号
 */
fun String.hidePhone(symbol: String = "*", count: Int = 4): String {

    if (this.length < 11) {

        return this
    }

    val startPosition = 3

    var resultSymbol = ""

    for (i in 0 until count) {

        resultSymbol += symbol
    }

    return this.replaceRange(startPosition, startPosition + count, resultSymbol)
}


/**
 * 是否视频
 */

fun String?.isVideo(): Boolean {

    if (this.isNullOrEmpty()) {
        return false
    }

    return this.endsWith(".mp4", true) || this.endsWith(".mkv", true)
            || this.endsWith(".avi", true)
            || this.endsWith(".mov", true)
            || this.endsWith(".wmv", true)
            || this.endsWith(".flv", true)
            || this.endsWith(".webm", true)
            || this.endsWith(".mpeg", true)
            || this.endsWith(".3gp", true)

}


/**
 * 是否图片
 */
fun String?.isImage(): Boolean {

    if (this.isNullOrEmpty()) {
        return false
    }

    return this.endsWith(".jpg", true) || this.endsWith(".png", true)
            || this.endsWith(".gif", true)
            || this.endsWith(".tiff", true)
            || this.endsWith(".bmp", true)
            || this.endsWith(".webp", true)
            || this.endsWith(".ico", true)

}


/**
 * 获取字体
 */
fun getTypeFace(path: String = "font/font_1.otf"): Typeface {
    val assetManager = BaseApplication.mApplication.assets
    return Typeface.createFromAsset(assetManager, path)
}

/**
 * 设置字体
 */
fun TextView.changeTypeface(path: String = "font/font_1.otf") {
    val tf: Typeface = getTypeFace(path)
    this.typeface = tf
}

/**
 * @return 格式化文件大小格式
 */
fun Int.formatNumUnit(): String {

    val fNum = this.toFloat()
    return if (fNum / 10000 < 1) {
        this.toString()
    } else if (fNum / 10000 < 1000) {
        DecimalFormat("0.00万").format(fNum / 10000)
    } else if (fNum / 10000000 < 10) {
        DecimalFormat("0.00千万").format(fNum / 10000000)
    } else {
        DecimalFormat("0.00亿").format(fNum / 100000000)
    }

}

fun Double.formatAmount(): String {

    val decimalFormat = DecimalFormat("#,##0.00")

    return when {
        this >= 1_000_000_000 -> "${decimalFormat.format(this / 1_000_000_000)} 亿"
        this >= 1_000_000 -> "${decimalFormat.format(this / 1_000_000)} 百万"
        this >= 1_000 -> "${decimalFormat.format(this / 1_000)} 千"
        else -> decimalFormat.format(this)
    }
}

fun Double?.formatPrice(): String {

    if (this == null) return "0"

    return BigDecimal.valueOf(this)
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

fun String?.formatPrice(): String {
    return try {
        BigDecimal(this)
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
    } catch (e: Exception) {
        "0.00"
    }
}


/**
 * View统一设置可见状态
 */

fun viewVisibility(status: Int, vararg view: View) {
    view.forEach {
        it.visibility = status
    }
}






/**
 * 复制粘贴板
 */
fun Context.copyContent(text: String) {
    val mClipboardManager: ClipboardManager =
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    mClipboardManager.setPrimaryClip(ClipData.newPlainText("LPG", text))

    showShort("已复制到粘贴板")

}

/**
 * 打开权限管理器
 */
fun Context.openPermissionSetting() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}


/**
 *
 * 是否在前台
 */

fun isAppInForeground(): Boolean {
    return BaseApplication.isAppForeground
}


/**
 * 在延时操作做更新ui先判断当前页面是否存活
 */
fun Lifecycle.isAlive(): Boolean {

    return this.currentState.isAtLeast(Lifecycle.State.STARTED)
}


fun Fragment.getColor(@ColorRes id:Int,theme: Resources.Theme? = null): Int{

    return this.resources.getColor(id,theme)
}


fun checkRegex(text: String,mRegex: String = "^(?![a-z]+\$)(?![A-Z]+\$)(?!\\d+\$)(?![\\W_]+\$).{8,}\$"): Boolean {

    val regex = Regex(mRegex)

    return regex.matches(text)
}




fun highlightContacts(textView: TextView, text: String, textColor: Int) {

    val spannable = SpannableString(text)

    // 定义类型、正则和颜色
    val patterns = listOf(
        Triple("qq", Regex("""\+?qq\d{5,}""", RegexOption.IGNORE_CASE), textColor),
        Triple("phone", Regex("""(?<!\d)(\d{2,4}-\d{5,8}|\d{6,11})(?!\d)"""), textColor),
        Triple("email", Regex("""\b[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+\b"""), textColor),
        Triple("username", Regex("""@\w+"""), textColor),
        Triple("number", Regex("""\b\d+\b"""), textColor), // 新增数字匹配
        Triple("number", Regex("""\d+"""), textColor) // 新增数字匹配
    )

    for ((type, regex, color) in patterns) {
        val matches = regex.findAll(text)
        for (match in matches) {
            val matchedText = match.value

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // 你可以在这里处理点击不同类型的逻辑
                    Log.d("ClickableSpan", "点击了$type: $matchedText")
                    // 示例：Toast.makeText(widget.context, "点击了$type: $matchedText", Toast.LENGTH_SHORT).show()

                    textView.context.copyContent(matchedText)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = color
                    ds.isUnderlineText = false
                }
            }

            spannable.setSpan(
                clickableSpan,
                match.range.first,
                match.range.last + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    textView.text = spannable
    textView.movementMethod = LinkMovementMethod.getInstance()
    textView.highlightColor = Color.TRANSPARENT
}


/**
 * 关闭键盘
 * @param context
 * @param window
 */
fun View.hideSoftInput(context: Context, show: Boolean = false) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (show) {
        this.requestFocus()
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    } else {
        inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
    }
}

inline fun <reified T: Parcelable> Intent.getParcelableData(key: String = AppConstant.Constant.DATA): T? {

   return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
       this.getParcelableExtra(key,T::class.java)
    }else{
        this.getParcelableExtra<T>(key)
    }
}

inline fun <reified T: Parcelable> Bundle.getParcelableData(key: String = AppConstant.Constant.DATA): T? {

   return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
       this.getParcelable(key,T::class.java)
    }else{
        this.getParcelable<T>(key)
    }
}


inline fun <reified T : Parcelable> Intent.getParcelableArrayListData(key: String = AppConstant.Constant.DATA): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArrayListExtra(key)
    }
}

inline fun <reified T : Parcelable> Bundle.getParcelableArrayListData(key: String = AppConstant.Constant.DATA): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayList(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArrayList(key)
    }
}




fun Long?.isNull(): Boolean {

    if (null == this){

        return true
    }

    if (this == -1L){

        return true
    }

    return false
}

fun Fragment.getColor(color: Int): Int{

    return ColorUtils.getColor(color)
}

fun <T> Context.createIntent(clazz: Class<T>): Intent {

    return Intent(this, clazz)
}

fun <T> Fragment.createIntent(clazz: Class<T>): Intent {

    return Intent(requireContext(), clazz)
}


fun Intent.startActivity(context: Context) {

    try {
        context.startActivity(this)
    }catch (e: Exception){
        e.printStackTrace()
    }


}

fun Intent.startActivity(activity: Activity, finish: Boolean = false) {

    try {
        activity.startActivity(this)

        if (finish) {

            activity.finish()
        }

    }catch (e: Exception){
        e.printStackTrace()
    }

}


fun isVpnConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networks = connectivityManager.allNetworks

    for (network in networks) {
        val caps = connectivityManager.getNetworkCapabilities(network)
        if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
            return true
        }
    }
    return false
}


fun Date?.toZodiac(context: Context): String {
    if (this == null) return ""
    val cal = Calendar.getInstance()

    cal.time = this
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)

    return when (month) {
        1 -> context.getString(if (day < 20) R.string.capricorn else R.string.aquarius)
        2 -> context.getString(if (day < 19) R.string.aquarius else R.string.pisces)
        3 -> context.getString(if (day < 21) R.string.pisces else R.string.aries)
        4 -> context.getString(if (day < 20) R.string.aries else R.string.taurus)
        5 -> context.getString(if (day < 21) R.string.taurus else R.string.gemini)
        6 -> context.getString(if (day < 21) R.string.gemini else R.string.cancer)
        7 -> context.getString(if (day < 23) R.string.cancer else R.string.leo)
        8 -> context.getString(if (day < 24) R.string.leo else R.string.virgo)
        9 -> context.getString(if (day < 24) R.string.virgo else R.string.libra)
        10 -> context.getString(if (day < 24) R.string.libra else R.string.scorpio)
        11 -> context.getString(if (day < 23) R.string.scorpio else R.string.sagittarius)
        12 -> context.getString(if (day < 22) R.string.sagittarius else R.string.capricorn)
        else -> ""
    }
}


fun getFileName(context: Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) return cursor.getString(index)
            }
        }
    }
    return uri.path?.substringAfterLast('/')
}


fun ShapeTextView.withAnimate():AnimatorSet{

    val mScaleX = this.mScale(View.SCALE_X, 1f, 1.03f, 1f)
    val mScaleY = this.mScale(View.SCALE_Y, 1f, 1.03f, 1f)

    val set = AnimatorSet()

    set.duration = 800L

    mScaleX.repeatCount = ValueAnimator.INFINITE

    set.playTogether(mScaleX,mScaleY)

    set.start()

    return set
}
