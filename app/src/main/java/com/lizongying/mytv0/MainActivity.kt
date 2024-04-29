package com.lizongying.mytv0

import android.content.Context
import android.media.AudioManager
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
import androidx.fragment.app.FragmentActivity
import com.lizongying.mytv0.models.TVList


class MainActivity : FragmentActivity() {

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
    private val delayHideMenu = 10000L
    private val delayHideSetting = 60000L

    private var doubleBackToExitPressedOnce = false

    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

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

        if (!TVList.setPosition(SP.position)) {
            TVList.setPosition(0)
        }

        showTime()
    }

    fun ready(tag: String) {
        Log.i(TAG, "ready $tag")
        ok++
        if (ok == 3) {
            Log.i(TAG, "watch")
            TVList.groupModel.tvGroupModel.observe(this) { _ ->
                if (TVList.groupModel.tvGroupModel.value != null) {
                    watch()
                    menuFragment.update()
                }
            }
        }
    }

    private fun watch() {
        TVList.listModel.forEach { tvModel ->
            tvModel.errInfo.observe(this) { _ ->
                if (tvModel.errInfo.value != null
                    && tvModel.tv.id == TVList.position.value
                ) {
                    Log.i(TAG, "errInfo ${tvModel.tv.title} ${tvModel.errInfo.value}")
                    if (tvModel.errInfo.value == "") {
                        Log.i(TAG, "hideErrorFragment ${tvModel.errInfo.value.toString()}")
                        hideErrorFragment()
                        hideLoadingFragment()
                        showPlayerFragment()
                    } else {
                        Log.i(TAG, "showErrorFragment ${tvModel.errInfo.value.toString()}")
                        hidePlayerFragment()
                        hideLoadingFragment()
                        showErrorFragment(tvModel.errInfo.value.toString())
                    }
                }
            }

            tvModel.ready.observe(this) { _ ->

                // not first time && channel is not changed
                if (tvModel.ready.value != null
                    && tvModel.tv.id == TVList.position.value
                ) {
                    Log.i(TAG, "loading ${tvModel.tv.title}")
                    hideErrorFragment()
                    showLoadingFragment()
                    playerFragment.play(tvModel)
                    infoFragment.show(tvModel)
                    if (SP.channelNum) {
                        channelFragment.show(tvModel)
                    }
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
            Log.i(TAG, "onSingleTapConfirmed showMenu")
            showMenu()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            showSetting()
            return true
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
        val tvModel = TVList.getTVModelCurrent()
        if (SP.repeatInfo) {
            infoFragment.show(tvModel)
            if (SP.channelNum) {
                channelFragment.show(tvModel)
            }
        }
    }

    fun play(position: Int) {
        if (position > -1 && position < TVList.size()) {
            TVList.setPosition(position)
        } else {
            Toast.makeText(this, "频道不存在", Toast.LENGTH_LONG).show()
        }
    }

    fun prev() {
        var position = TVList.position.value?.dec() ?: 0
        if (position == -1) {
            position = TVList.size() - 1
        }
        TVList.setPosition(position)
    }

    fun next() {
        var position = TVList.position.value?.inc() ?: 0
        if (position == TVList.size()) {
            position = 0
        }
        TVList.setPosition(position)
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
        if (!settingFragment.isHidden) {
            supportFragmentManager.beginTransaction().hide(settingFragment).commitNow()
            showTime()
        }
    }

    fun showTime() {
        if (SP.time) {
            showTimeFragment()
        } else {
            hideTimeFragment()
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
        Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    fun switchMainFragment() {
        val transaction = supportFragmentManager.beginTransaction()

        if (menuFragment.isHidden) {
//            menuFragment.setPosition()
            transaction.show(menuFragment)
            menuActive()
        } else {
            transaction.hide(menuFragment)
        }

        transaction.commit()
    }


    private fun showMenu() {
        if (!settingFragment.isHidden) {
            return
        }

        supportFragmentManager.beginTransaction()
            .show(menuFragment)
            .commit()
        menuActive()
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
        Log.i(TAG, "SP.time ${SP.time}")
    }

    private fun hideSettingFragment() {
        supportFragmentManager.beginTransaction()
            .hide(settingFragment)
            .commit()
        showTime()
    }

    private fun showErrorFragment(msg: String) {
        errorFragment.show(msg)
        if (!errorFragment.isHidden) {
            return
        }

        supportFragmentManager.beginTransaction()
            .show(errorFragment)
            .commitNow()
    }

    private fun hideErrorFragment() {
        errorFragment.show("hide")
        if (errorFragment.isHidden) {
            return
        }

        supportFragmentManager.beginTransaction()
            .hide(errorFragment)
            .commitNow()
    }

    private fun showLoadingFragment() {
        if (!loadingFragment.isHidden) {
            return
        }

        supportFragmentManager.beginTransaction()
            .show(loadingFragment)
            .commitNow()
    }

    private fun hideLoadingFragment() {
        if (loadingFragment.isHidden) {
            return
        }

        supportFragmentManager.beginTransaction()
            .hide(loadingFragment)
            .commitNow()
    }

    private fun showTimeFragment() {
        if (!timeFragment.isHidden) {
            return
        }

        supportFragmentManager.beginTransaction()
            .show(timeFragment)
            .commitNow()
    }

    private fun hideTimeFragment() {
        if (timeFragment.isHidden) {
            return
        }

        supportFragmentManager.beginTransaction()
            .hide(timeFragment)
            .commitNow()
    }

    private fun showPlayerFragment() {
        if (!playerFragment.isHidden) {
            return
        }

        supportFragmentManager.beginTransaction()
            .show(playerFragment)
            .commit()
    }

    private fun hidePlayerFragment() {
        if (playerFragment.isHidden) {
            return
        }

        supportFragmentManager.beginTransaction()
            .hide(playerFragment)
            .commit()
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
                switchMainFragment()
            }

            KeyEvent.KEYCODE_DPAD_CENTER -> {
                switchMainFragment()
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
                showMenu()
//                return true
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

    override fun onResume() {
        super.onResume()
        showTime()
    }

    override fun onStop() {
        super.onStop()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}