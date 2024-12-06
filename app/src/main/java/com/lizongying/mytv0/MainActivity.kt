package com.lizongying.mytv0

import MainViewModel
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private var ok = 0
    private var playerFragment = PlayerFragment()
    private val errorFragment = ErrorFragment()
    private val loadingFragment = LoadingFragment()
    private var infoFragment = InfoFragment()
    private var channelFragment = ChannelFragment()
    private var timeFragment = TimeFragment()
    private var menuFragment = MenuFragment()
    private var settingFragment = SettingFragment()

    private val handler = Handler(Looper.myLooper()!!)
    private val delayHideMenu = 10 * 1000L
    private val delayHideSetting = 3 * 60 * 1000L

    private var doubleBackToExitPressedOnce = false

    private lateinit var gestureDetector: GestureDetector

    private var server: SimpleServer? = null

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        requestWindowFeature(FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            windowInsetsController.let { controller ->
                controller.isAppearanceLightNavigationBars = true
                controller.isAppearanceLightStatusBars = true
                controller.hide(WindowInsetsCompat.Type.statusBars())
                controller.hide(WindowInsetsCompat.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = Color.TRANSPARENT
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.setAttributes(lp)
        }

        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.init(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.main_browse_fragment, playerFragment)
                .add(R.id.main_browse_fragment, errorFragment)
                .add(R.id.main_browse_fragment, loadingFragment)
                .add(R.id.main_browse_fragment, timeFragment)
                .add(R.id.main_browse_fragment, infoFragment)
                .add(R.id.main_browse_fragment, channelFragment)
                .add(R.id.main_browse_fragment, menuFragment)
                .add(R.id.main_browse_fragment, settingFragment)
                .hide(menuFragment)
                .hide(settingFragment)
                .hide(errorFragment)
                .hide(loadingFragment)
                .hide(timeFragment)
                .commitNow()
        }

        gestureDetector = GestureDetector(this, GestureListener(this))

        showTime()
    }

    fun update() {
        menuFragment.update()
    }

    fun updateMenuSize() {
        menuFragment.updateSize()
    }

    fun ready(tag: String) {
        Log.i(TAG, "ready $tag")
        ok++
        if (ok == 5) {
            Log.i(TAG, "all ready")
            viewModel.groupModel.change.observe(this) { _ ->
                Log.i(TAG, "group changed")
                if (viewModel.groupModel.tvGroup.value != null) {
                    watch()
                    menuFragment.update()
                }
            }

//            if (SP.defaultLike) {
//                TVList.groupModel.setPosition(0)
//                val tvModel = TVList.listModel.find { it.like.value as Boolean }
//                TVList.setPosition(tvModel?.tv?.id ?: 0)
//                "播放收藏频道".showToast()
//            }

            viewModel.channelsOk.observe(this) { it ->
                if (it) {
                    val prevGroup = viewModel.groupModel.positionValue
                    val tvModel = if (SP.channel > 0) {
                        val position = if (SP.channel < viewModel.listModel.size) {
                            // R.string.play_default_channel.showToast()
                            SP.channel - 1
                        } else {
                            // R.string.default_channel_out_of_range.showToast()
                            SP.channel = 0
                            0
                        }
                        Log.i(TAG, "播放默認頻道")
                        viewModel.groupModel.getPosition(position)
                    } else {
//                if (SP.position < 0 || SP.position >= TVList.groupModel.getAllList()!!
//                        .size()
//                ) {
//                    // R.string.last_channel_out_of_range.showToast()
//                    0
//                } else {
//                    // R.string.play_last_channel.showToast()
//                    SP.position
//                }
                        Log.i(TAG, "播放上次頻道")
                        viewModel.groupModel.getCurrent()
                    }
                    viewModel.groupModel.setPlaying()
                    viewModel.groupModel.getCurrentList()
                        ?.let {
                            Log.i(TAG, "當前組 ${it.getName()}")
                            it.setPlaying()
                        }
                    tvModel?.setReady()

                    val currentGroup = viewModel.groupModel.positionValue
                    if (currentGroup != prevGroup) {
                        Log.i(TAG, "group change")
                        menuFragment.updateList(currentGroup)
                    }

                    viewModel.groupModel.isInLikeMode =
                        SP.defaultLike && viewModel.groupModel.positionValue == 0
                    if (viewModel.groupModel.isInLikeMode) {
//                R.string.favorite_mode.showToast()
                    } else {
//                R.string.standard_mode.showToast()
                    }

                    // TODO group position
                    viewModel.updateEPG()
                }
            }

            Utils.isp.observe(this) {
                Log.i(TAG, "isp $it")
//                val id = R.raw.mobile
                val id = when (it) {
                    ISP.CHINA_MOBILE -> R.raw.mobile
                    else -> 0
                }

                if (id == 0) {
                    return@observe
                }

                resources.openRawResource(id).bufferedReader()
                    .use { i ->
                        val channels = i.readText()
                        if (channels.isNotEmpty()) {
                            viewModel.tryStr2Channels(channels, null, "")
                        } else {
                            Log.w(TAG, "$it is empty")
                        }
                    }
            }

            server = SimpleServer(this, viewModel)

            viewModel.updateConfig()
        }
    }

    private fun watch() {
        viewModel.listModel.forEach { tvModel ->
            tvModel.errInfo.observe(this) { _ ->

                if (tvModel.errInfo.value != null
//                    && tvModel.tv.id == TVList.positionValue
                ) {
                    hideFragment(loadingFragment)
                    if (tvModel.errInfo.value == "") {
                        Log.i(TAG, "${tvModel.tv.title} playing")
                        hideFragment(errorFragment)
                        showFragment(playerFragment)
                    } else {
                        Log.i(TAG, "${tvModel.tv.title} ${tvModel.errInfo.value.toString()}")
                        hideFragment(playerFragment)
                        errorFragment.setMsg(tvModel.errInfo.value.toString())
                        showFragment(errorFragment)
                    }
                }
            }

            tvModel.ready.observe(this) { _ ->

                // not first time && channel is not changed
                if (tvModel.ready.value != null
//                    && tvModel.tv.id == TVList.positionValue
                ) {
                    Log.i(TAG, "${tvModel.tv.title} 嘗試播放")
                    hideFragment(errorFragment)
                    showFragment(loadingFragment)
                    playerFragment.play(tvModel)
                    infoFragment.show(tvModel)
                    if (SP.channelNum) {
                        channelFragment.show(tvModel)
                    }
                }
            }

            tvModel.like.observe(this) { _ ->
                if (tvModel.like.value != null && tvModel.tv.id != -1) {
                    val liked = tvModel.like.value as Boolean
                    if (liked) {
                        viewModel.groupModel.getFavoritesList()?.replaceTVModel(tvModel)
                    } else {
                        viewModel.groupModel.getFavoritesList()
                            ?.removeTVModel(tvModel.tv.id)
                    }
                    SP.setLike(tvModel.tv.id, liked)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            gestureDetector.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    private inner class GestureListener(private val context: Context) :
        GestureDetector.SimpleOnGestureListener() {

        private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            showFragment(menuFragment)
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            showSetting()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            Log.i(TAG, "onLongPress")
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if ((e1?.x ?: 0f) > windowManager.defaultDisplay.width / 3
                && (e1?.x ?: 0f) < windowManager.defaultDisplay.width * 2 / 3
            ) {
                if (velocityY > 0) {
                    if (menuFragment.isHidden && settingFragment.isHidden) {
                        prev()
                    }
                }
                if (velocityY < 0) {
                    if (menuFragment.isHidden && settingFragment.isHidden) {
                        next()
                    }
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY)
        }

//        override fun onScroll(
//            e1: MotionEvent?,
//            e2: MotionEvent,
//            distanceX: Float,
//            distanceY: Float
//        ): Boolean {
//            val deltaY = e1?.y?.let { e2.y.minus(it) } ?: 0f
//            val deltaX = e1?.x?.let { e2.x.minus(it) } ?: 0f
//
//            if (abs(deltaY) > abs(deltaX)) {
//                if ((e1?.x ?: 0f) > windowManager.defaultDisplay.width * 2 / 3) {
//                    adjustVolume(deltaY)
//                }
//            }
//
//            return super.onScroll(e1, e2, distanceX, distanceY)
//        }

        private fun adjustVolume(deltaY: Float) {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val deltaVolume = deltaY / 1000 * maxVolume / windowManager.defaultDisplay.height

            var newVolume = currentVolume + deltaVolume
            if (newVolume < 0) {
                newVolume = 0F
            } else if (newVolume > maxVolume) {
                newVolume = maxVolume.toFloat()
            }

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume.toInt(), 0)

            // 可以添加一个toast来显示当前音量
            Toast.makeText(context, "Volume: $newVolume / $maxVolume", Toast.LENGTH_SHORT).show()
        }

//        private fun changeBrightness(deltaBrightness: Float) {
//            brightness += deltaBrightness
//            if (brightness < 0) {
//                brightness = 0f
//            } else if (brightness > 1) {
//                brightness = 1f
//            }
//
//            val layoutParams = windowManager.attributes
//            layoutParams.screenBrightness = brightness
//            windowManager.attributes = layoutParams
//
//            // 可以添加一个toast来显示当前亮度
//            Toast.makeText(context, "Brightness: $brightness", Toast.LENGTH_SHORT).show()
//        }
    }

    fun onPlayEnd() {
        val tvModel = viewModel.groupModel.getCurrent()!!
        if (SP.repeatInfo) {
            infoFragment.show(tvModel)
            if (SP.channelNum) {
                channelFragment.show(tvModel)
            }
        }
    }

    fun play(position: Int) {
        if (position > -1 && position < viewModel.groupModel.getAllList()!!.size()) {
            val prevGroup = viewModel.groupModel.positionValue
            val tvModel = viewModel.groupModel.getPosition(position)

            tvModel?.setReady()
            viewModel.groupModel.setPlaying()
            viewModel.groupModel.getCurrentList()?.setPlaying()

            val currentGroup = viewModel.groupModel.positionValue
            if (currentGroup != prevGroup) {
                menuFragment.updateList(currentGroup)
            }
        } else {
            R.string.channel_not_exist.showToast()
        }
    }

    fun prev() {
        val prevGroup = viewModel.groupModel.positionValue
        val tvModel =
            if (SP.defaultLike && viewModel.groupModel.isInLikeMode && viewModel.groupModel.getFavoritesList() != null
            ) {
                viewModel.groupModel.getPrev(true)
            } else {
                viewModel.groupModel.getPrev()
            }

        tvModel?.setReady()
        viewModel.groupModel.setPlaying()
        viewModel.groupModel.getCurrentList()?.setPlaying()

        val currentGroup = viewModel.groupModel.positionValue
        if (currentGroup != prevGroup) {
            menuFragment.updateList(currentGroup)
        }
    }

    fun next() {
        val prevGroup = viewModel.groupModel.positionValue
        val tvModel =
            if (SP.defaultLike && viewModel.groupModel.isInLikeMode && viewModel.groupModel.getFavoritesList() != null
            ) {
                viewModel.groupModel.getNext(true)
            } else {
                viewModel.groupModel.getNext()
            }

        tvModel?.setReady()
        viewModel.groupModel.setPlaying()
        viewModel.groupModel.getCurrentList()?.setPlaying()

        val currentGroup = viewModel.groupModel.positionValue
        if (currentGroup != prevGroup) {
            menuFragment.updateList(currentGroup)
        }
    }

    private fun showFragment(fragment: Fragment) {
        if (!fragment.isHidden) {
            return
        }

        supportFragmentManager.beginTransaction()
            .show(fragment)
            .commitNow()
    }

    private fun hideFragment(fragment: Fragment) {
        if (fragment.isHidden) {
            return
        }

        supportFragmentManager.beginTransaction()
            .hide(fragment)
            .commitNow()
    }

    fun menuActive() {
        handler.removeCallbacks(hideMenu)
        handler.postDelayed(hideMenu, delayHideMenu)
    }

    private val hideMenu = Runnable {
        if (!isFinishing && !supportFragmentManager.isStateSaved) {
            if (!menuFragment.isHidden) {
                supportFragmentManager.beginTransaction().hide(menuFragment).commit()
            }
        }
    }

    fun settingActive() {
        handler.removeCallbacks(hideSetting)
        handler.postDelayed(hideSetting, delayHideSetting)
    }

    private val hideSetting = Runnable {
        if (!isFinishing && !isDestroyed && !supportFragmentManager.isDestroyed) {
            if (settingFragment.isAdded && !settingFragment.isHidden) {
                try {
                    supportFragmentManager.beginTransaction()
                        .hide(settingFragment)
                        .commitNow()
                    showTime()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun showTime() {
        if (SP.time) {
            showFragment(timeFragment)
        } else {
            hideFragment(timeFragment)
        }
    }

    private fun showChannel(channel: String) {
        if (!menuFragment.isHidden) {
            return
        }

        if (settingFragment.isVisible) {
            return
        }

//        if (SP.channelNum) {
//            channelFragment.show(channel)
//        }
        channelFragment.show(channel)
    }


    private fun channelUp() {
        if (menuFragment.isHidden && settingFragment.isHidden) {
            if (SP.channelReversal) {
                next()
                return
            }
            prev()
        }
    }

    private fun channelDown() {
        if (menuFragment.isHidden && settingFragment.isHidden) {
            if (SP.channelReversal) {
                prev()
                return
            }
            next()
        }
    }

    private fun back() {
        if (!menuFragment.isHidden) {
            hideMenuFragment()
            return
        }

        if (!settingFragment.isHidden) {
            hideSettingFragment()
            return
        }

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        doubleBackToExitPressedOnce = true
        R.string.press_again_to_exit.showToast()

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    private fun showSetting() {
        if (!menuFragment.isHidden) {
            return
        }

        supportFragmentManager.beginTransaction()
            .show(settingFragment)
            .commit()
        settingActive()
    }

    fun hideMenuFragment() {
        supportFragmentManager.beginTransaction()
            .hide(menuFragment)
            .commit()
    }

    private fun hideSettingFragment() {
        supportFragmentManager.beginTransaction()
            .hide(settingFragment)
            .commit()
        showTime()
    }

    fun onKey(keyCode: Int): Boolean {
        Log.d(TAG, "keyCode $keyCode")
        when (keyCode) {
            KeyEvent.KEYCODE_0 -> {
                showChannel("0")
                return true
            }

            KeyEvent.KEYCODE_1 -> {
                showChannel("1")
                return true
            }

            KeyEvent.KEYCODE_2 -> {
                showChannel("2")
                return true
            }

            KeyEvent.KEYCODE_3 -> {
                showChannel("3")
                return true
            }

            KeyEvent.KEYCODE_4 -> {
                showChannel("4")
                return true
            }

            KeyEvent.KEYCODE_5 -> {
                showChannel("5")
                return true
            }

            KeyEvent.KEYCODE_6 -> {
                showChannel("6")
                return true
            }

            KeyEvent.KEYCODE_7 -> {
                showChannel("7")
                return true
            }

            KeyEvent.KEYCODE_8 -> {
                showChannel("8")
                return true
            }

            KeyEvent.KEYCODE_9 -> {
                showChannel("9")
                return true
            }

            KeyEvent.KEYCODE_ESCAPE -> {
                back()
                return true
            }

            KeyEvent.KEYCODE_BACK -> {
                back()
                return true
            }

            KeyEvent.KEYCODE_BOOKMARK -> {
                showSetting()
                return true
            }

            KeyEvent.KEYCODE_UNKNOWN -> {
                showSetting()
                return true
            }

            KeyEvent.KEYCODE_HELP -> {
                showSetting()
                return true
            }

            KeyEvent.KEYCODE_SETTINGS -> {
                showSetting()
                return true
            }

            KeyEvent.KEYCODE_MENU -> {
                showSetting()
                return true
            }

            KeyEvent.KEYCODE_ENTER -> {
                showFragment(menuFragment)
            }

            KeyEvent.KEYCODE_DPAD_CENTER -> {
                showFragment(menuFragment)
            }

            KeyEvent.KEYCODE_DPAD_UP -> {
                channelUp()
            }

            KeyEvent.KEYCODE_CHANNEL_UP -> {
                channelUp()
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                channelDown()
            }

            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                channelDown()
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (settingFragment.isHidden) {
                    showFragment(menuFragment)
                }
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                showSetting()
//                return true
            }
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (onKey(keyCode)) {
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
    }

    override fun attachBaseContext(base: Context) {
        try {
            val locale = Locale.TRADITIONAL_CHINESE
            val config = Configuration()
            config.setLocale(locale)
            super.attachBaseContext(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    base.createConfigurationContext(config)
                } else {
                    val resources = base.resources
                    resources.updateConfiguration(config, resources.displayMetrics)
                    base
                }
            )
        } catch (_: Exception) {
            super.attachBaseContext(base)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}