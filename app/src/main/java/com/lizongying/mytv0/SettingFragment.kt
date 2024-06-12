package com.lizongying.mytv0

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import com.lizongying.mytv0.databinding.SettingBinding
import com.lizongying.mytv0.models.TVList


class SettingFragment : Fragment() {

    private var _binding: SettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var uri: Uri

    private lateinit var updateManager: UpdateManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()

        _binding = SettingBinding.inflate(inflater, container, false)

        binding.versionName.text = "当前版本: v${context.appVersionName}"
        binding.version.text = "https://github.com/lizongying/my-tv-0"

        val switchChannelReversal = _binding?.switchChannelReversal
        switchChannelReversal?.isChecked = SP.channelReversal
        switchChannelReversal?.setOnCheckedChangeListener { _, isChecked ->
            SP.channelReversal = isChecked
            (activity as MainActivity).settingActive()
        }

        val switchChannelNum = _binding?.switchChannelNum
        switchChannelNum?.isChecked = SP.channelNum
        switchChannelNum?.setOnCheckedChangeListener { _, isChecked ->
            SP.channelNum = isChecked
            (activity as MainActivity).settingActive()
        }

        val switchTime = _binding?.switchTime
        switchTime?.isChecked = SP.time
        switchTime?.setOnCheckedChangeListener { _, isChecked ->
            SP.time = isChecked
            (activity as MainActivity).settingActive()
        }

        val switchBootStartup = _binding?.switchBootStartup
        switchBootStartup?.isChecked = SP.bootStartup
        switchBootStartup?.setOnCheckedChangeListener { _, isChecked ->
            SP.bootStartup = isChecked
            (activity as MainActivity).settingActive()
        }

        val switchRepeatInfo = _binding?.switchRepeatInfo
        switchRepeatInfo?.isChecked = SP.repeatInfo
        switchRepeatInfo?.setOnCheckedChangeListener { _, isChecked ->
            SP.repeatInfo = isChecked
            (activity as MainActivity).settingActive()
        }

        val switchConfigAutoLoad = _binding?.switchConfigAutoLoad
        switchConfigAutoLoad?.isChecked = SP.configAutoLoad
        switchConfigAutoLoad?.setOnCheckedChangeListener { _, isChecked ->
            SP.configAutoLoad = isChecked
            (activity as MainActivity).settingActive()
        }

        val switchDefaultLike = _binding?.switchDefaultLike
        switchDefaultLike?.isChecked = SP.defaultLike
        switchDefaultLike?.setOnCheckedChangeListener { _, isChecked ->
            SP.defaultLike = isChecked
            (activity as MainActivity).settingActive()
        }

        binding.qrcode.setOnClickListener {
            val imageModalFragment = ModalFragment()
            val size = Utils.dpToPx(200)
            val img = QrCodeUtil().createQRCodeBitmap(binding.server.text.toString(), size, size)
            val args = Bundle()
            args.putParcelable("bitmap", img);
            imageModalFragment.arguments = args

            imageModalFragment.show(requireFragmentManager(), ModalFragment.TAG)
            (activity as MainActivity).settingActive()
        }

        binding.checkVersion.setOnClickListener {
            requestInstallPermissions()
            (activity as MainActivity).settingActive()
        }

        val config = binding.config
        config.text = SP.config?.let { Editable.Factory.getInstance().newEditable(it) }
            ?: Editable.Factory.getInstance().newEditable("")
        binding.confirmConfig.setOnClickListener {
            var url = config.text.toString().trim()
            url = Utils.formatUrl(url)
            uri = Uri.parse(url)
            Log.i(TAG, "uri.scheme ${uri.scheme}")
            if (uri.scheme == "") {
                uri = uri.buildUpon().scheme("http").build()
            }
            Log.i(TAG, "Uri $uri")
            if (uri.isAbsolute) {
                Log.i(TAG, "Uri ok")
                if (uri.scheme == "file") {
                    requestReadPermissions()
                } else {
                    TVList.parseUri(uri)
                }
            } else {
                config.error = "无效的地址"
            }
            (activity as MainActivity).settingActive()
        }

        val defaultChannel = binding.channel
        defaultChannel.text =
            SP.channel.let { Editable.Factory.getInstance().newEditable(it.toString()) }
                ?: Editable.Factory.getInstance().newEditable("")
        binding.confirmChannel.setOnClickListener {
            val c = defaultChannel.text.toString().trim()
            var channel = 0
            try {
                channel = c.toInt()
            } catch (e: NumberFormatException) {
                println(e)
            }
            if (channel > 0 && channel <= TVList.listModel.size) {
                SP.channel = channel
            } else {
                defaultChannel.error = "无效的频道"
            }
            (activity as MainActivity).settingActive()
        }

        val defaultProxy = binding.proxy
        defaultProxy.text =
            SP.proxy.let { Editable.Factory.getInstance().newEditable(it) }
                ?: Editable.Factory.getInstance().newEditable("")
        binding.confirmProxy.setOnClickListener {
            val c = defaultProxy.text.toString().trim()
            var proxy = ""
            try {
                proxy = Uri.parse(c).toString()
            } catch (e: Exception) {
                println(e)
            }
            if (proxy != "") {
                SP.proxy = proxy
                "代理配置成功".showToast(Toast.LENGTH_LONG)
            } else {
                defaultProxy.error = "无效的代理"
            }
            (activity as MainActivity).settingActive()
        }

        binding.clear.setOnClickListener {
            SP.config = ""
            config.text = Editable.Factory.getInstance().newEditable("")
            SP.channel = 0
            defaultChannel.text = Editable.Factory.getInstance().newEditable("")
            context.deleteFile(TVList.FILE_NAME)
            SP.deleteLike()
            SP.position = 0
            TVList.setPosition(0)
            "已恢复默认".showToast(Toast.LENGTH_LONG)
        }

        binding.appreciate.setOnClickListener {
            val imageModalFragment = ModalFragment()

            val args = Bundle()
            args.putInt(ModalFragment.KEY_DRAWABLE_ID, R.drawable.appreciate)
            imageModalFragment.arguments = args

            imageModalFragment.show(requireFragmentManager(), ModalFragment.TAG)
            (activity as MainActivity).settingActive()
        }

        binding.setting.setOnClickListener {
            hideSelf()
        }

        binding.exit.setOnClickListener {
            requireActivity().finishAffinity()
        }

        val application = requireActivity().applicationContext as MyTVApplication

        binding.content.layoutParams.width =
            application.px2Px(binding.content.layoutParams.width)
        binding.content.setPadding(
            application.px2Px(binding.content.paddingLeft),
            application.px2Px(binding.content.paddingTop),
            application.px2Px(binding.content.paddingRight),
            application.px2Px(binding.content.paddingBottom)
        )

        binding.name.textSize = application.px2PxFont(binding.name.textSize)
        binding.version.textSize = application.px2PxFont(binding.version.textSize)
        val layoutParamsVersion = binding.version.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsVersion.topMargin = application.px2Px(binding.version.marginTop)
        binding.version.layoutParams = layoutParamsVersion

        val btnWidth =
            application.px2Px(binding.confirmConfig.layoutParams.width)
        val btnHeight =
            application.px2Px(binding.confirmConfig.layoutParams.height)
        val btnTextSize =
            application.px2PxFont(binding.confirmConfig.textSize)
        val btnLayoutParams =
            binding.confirmConfig.layoutParams as ViewGroup.MarginLayoutParams
        btnLayoutParams.marginEnd = application.px2Px(binding.confirmConfig.marginEnd)

        val txtWidth =
            application.px2Px(binding.config.layoutParams.width)
        val txtTextSize =
            application.px2PxFont(binding.config.textSize)

        binding.checkVersion.layoutParams.width = btnWidth
        binding.checkVersion.layoutParams.height = btnHeight
        binding.checkVersion.textSize = btnTextSize
        binding.checkVersion.layoutParams = btnLayoutParams

        binding.versionName.layoutParams.width = txtWidth
        binding.versionName.textSize = txtTextSize

        binding.qrcode.layoutParams.width = btnWidth
        binding.qrcode.layoutParams.height = btnHeight
        binding.qrcode.textSize = btnTextSize
        binding.qrcode.layoutParams = btnLayoutParams

        binding.server.layoutParams.width = txtWidth
        binding.server.textSize = txtTextSize

        binding.confirmConfig.layoutParams.width = btnWidth
        binding.confirmConfig.layoutParams.height = btnHeight
        binding.confirmConfig.textSize = btnTextSize
        binding.confirmConfig.layoutParams = btnLayoutParams

        binding.config.layoutParams.width = txtWidth
        binding.config.textSize = txtTextSize

        binding.confirmChannel.layoutParams.width = btnWidth
        binding.confirmChannel.layoutParams.height = btnHeight
        binding.confirmChannel.textSize = btnTextSize
        binding.confirmChannel.layoutParams = btnLayoutParams

        binding.channel.layoutParams.width = txtWidth
        binding.channel.textSize = txtTextSize

        binding.confirmProxy.layoutParams.width = btnWidth
        binding.confirmProxy.layoutParams.height = btnHeight
        binding.confirmProxy.textSize = btnTextSize
        binding.confirmProxy.layoutParams = btnLayoutParams

        binding.proxy.layoutParams.width = txtWidth
        binding.proxy.textSize = txtTextSize

        binding.clear.layoutParams.width = btnWidth
        binding.clear.layoutParams.height = btnHeight
        binding.clear.textSize = btnTextSize
        binding.clear.layoutParams = btnLayoutParams

        binding.appreciate.layoutParams.width = btnWidth
        binding.appreciate.layoutParams.height = btnHeight
        binding.appreciate.textSize = btnTextSize
        binding.appreciate.layoutParams = btnLayoutParams

        binding.exit.layoutParams.width = btnWidth
        binding.exit.layoutParams.height = btnHeight
        binding.exit.textSize = btnTextSize
        binding.exit.layoutParams = btnLayoutParams

        val textSize = application.px2PxFont(binding.switchChannelReversal.textSize)

        val layoutParamsChannelReversal =
            binding.switchChannelReversal.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsChannelReversal.topMargin =
            application.px2Px(binding.switchChannelReversal.marginTop)

        binding.switchChannelReversal.textSize = textSize
        binding.switchChannelReversal.layoutParams = layoutParamsChannelReversal

        binding.switchChannelNum.textSize = textSize
        binding.switchChannelNum.layoutParams = layoutParamsChannelReversal

        binding.switchTime.textSize = textSize
        binding.switchTime.layoutParams = layoutParamsChannelReversal

        binding.switchBootStartup.textSize = textSize
        binding.switchBootStartup.layoutParams = layoutParamsChannelReversal

        binding.switchRepeatInfo.textSize = textSize
        binding.switchRepeatInfo.layoutParams = layoutParamsChannelReversal

        binding.switchConfigAutoLoad.textSize = textSize
        binding.switchConfigAutoLoad.layoutParams = layoutParamsChannelReversal

        binding.switchDefaultLike.textSize = textSize
        binding.switchDefaultLike.layoutParams = layoutParamsChannelReversal

        updateManager = UpdateManager(context, context.appVersionCode)

        return binding.root
    }

    fun setServer(server: String) {
        binding.server.text = "http://$server"
    }

    fun setVersionName(versionName: String) {
        binding.versionName.text = versionName
    }

    private fun hideSelf() {
        requireActivity().supportFragmentManager.beginTransaction()
            .hide(this)
            .commit()
        (activity as MainActivity).showTime()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            val config = binding.config
            config.text = SP.config?.let { Editable.Factory.getInstance().newEditable(it) }
                ?: Editable.Factory.getInstance().newEditable("")
        }
    }

    private fun requestInstallPermissions() {
        val context = requireContext()
        val permissionsList: MutableList<String> = ArrayList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            permissionsList.add(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsList.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsList.toTypedArray<String>(),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            updateManager.checkAndUpdate()
        }
    }

    private fun requestReadPermissions() {
        val context = requireContext()
        val permissionsList: MutableList<String> = ArrayList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsList.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsList.toTypedArray<String>(),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            TVList.parseUri(uri)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                TVList.parseUri(uri)
            } else {
                "权限授权失败".showToast(Toast.LENGTH_LONG)
            }
        }
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                updateManager.checkAndUpdate()
            } else {
                "权限授权失败".showToast(Toast.LENGTH_LONG)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SettingFragment"
        const val PERMISSIONS_REQUEST_CODE = 1
        const val PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE = 2
    }
}

