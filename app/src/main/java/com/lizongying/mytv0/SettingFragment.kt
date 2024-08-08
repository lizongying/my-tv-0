package com.lizongying.mytv0

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import com.lizongying.mytv0.databinding.SettingBinding
import com.lizongying.mytv0.models.TVList
import kotlin.math.max
import kotlin.math.min


class SettingFragment : Fragment() {

    private var _binding: SettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var uri: Uri

    private lateinit var updateManager: UpdateManager

    private var server = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val application = requireActivity().applicationContext as MyTVApplication
        val context = requireContext()
        val mainActivity = (activity as MainActivity)

        _binding = SettingBinding.inflate(inflater, container, false)

        binding.versionName.text = "v${context.appVersionName}"
        binding.version.text = "https://github.com/lizongying/my-tv-0"

        val switchChannelReversal = _binding?.switchChannelReversal
        switchChannelReversal?.isChecked = SP.channelReversal
        switchChannelReversal?.setOnCheckedChangeListener { _, isChecked ->
            SP.channelReversal = isChecked
            mainActivity.settingActive()
        }

        val switchChannelNum = _binding?.switchChannelNum
        switchChannelNum?.isChecked = SP.channelNum
        switchChannelNum?.setOnCheckedChangeListener { _, isChecked ->
            SP.channelNum = isChecked
            mainActivity.settingActive()
        }

        val switchTime = _binding?.switchTime
        switchTime?.isChecked = SP.time
        switchTime?.setOnCheckedChangeListener { _, isChecked ->
            SP.time = isChecked
            mainActivity.settingActive()
        }

        val switchBootStartup = _binding?.switchBootStartup
        switchBootStartup?.isChecked = SP.bootStartup
        switchBootStartup?.setOnCheckedChangeListener { _, isChecked ->
            SP.bootStartup = isChecked
            mainActivity.settingActive()
        }

        val switchRepeatInfo = _binding?.switchRepeatInfo
        switchRepeatInfo?.isChecked = SP.repeatInfo
        switchRepeatInfo?.setOnCheckedChangeListener { _, isChecked ->
            SP.repeatInfo = isChecked
            mainActivity.settingActive()
        }

        val switchConfigAutoLoad = _binding?.switchConfigAutoLoad
        switchConfigAutoLoad?.isChecked = SP.configAutoLoad
        switchConfigAutoLoad?.setOnCheckedChangeListener { _, isChecked ->
            SP.configAutoLoad = isChecked
            mainActivity.settingActive()
        }

        val switchDefaultLike = _binding?.switchDefaultLike
        switchDefaultLike?.isChecked = SP.defaultLike
        switchDefaultLike?.setOnCheckedChangeListener { _, isChecked ->
            SP.defaultLike = isChecked
            mainActivity.settingActive()
        }

        val switchShowAllChannels = _binding?.switchShowAllChannels
        switchShowAllChannels?.isChecked = SP.showAllChannels
        switchShowAllChannels?.setOnCheckedChangeListener { _, isChecked ->
            SP.showAllChannels = isChecked
            TVList.groupModel.tvGroupModel.value?.let { TVList.groupModel.setTVListModelList(it) }
            mainActivity.update()
            mainActivity.settingActive()
        }

        binding.qrcode.setOnClickListener {
            val imageModalFragment = ModalFragment()
            val size = Utils.dpToPx(200)
            val img = QrCodeUtil().createQRCodeBitmap(server, size, size)
            val args = Bundle()
            args.putParcelable("bitmap", img);
            imageModalFragment.arguments = args

            imageModalFragment.show(requireFragmentManager(), ModalFragment.TAG)
            mainActivity.settingActive()
        }

        binding.checkVersion.setOnClickListener {
            requestInstallPermissions()
            mainActivity.settingActive()
        }

        binding.confirmConfig.setOnClickListener {
            confirmConfig()
        }

        binding.clear.setOnClickListener {
            SP.config = SP.DEFAULT_CONFIG_URL
            confirmConfig()
            SP.channel = SP.DEFAULT_CHANNEL
            confirmChannel()
            context.deleteFile(TVList.FILE_NAME)
            SP.deleteLike()
            SP.position = 0
            TVList.setPosition(0)
            SP.showAllChannels = SP.DEFAULT_SHOW_ALL_CHANNELS

            R.string.config_restored.showToast()
        }

        binding.appreciate.setOnClickListener {
            val imageModalFragment = ModalFragment()

            val args = Bundle()
            args.putInt(ModalFragment.KEY_DRAWABLE_ID, R.drawable.appreciate)
            imageModalFragment.arguments = args

            imageModalFragment.show(requireFragmentManager(), ModalFragment.TAG)
            mainActivity.settingActive()
        }

        binding.setting.setOnClickListener {
            hideSelf()
        }

        binding.exit.setOnClickListener {
            requireActivity().finishAffinity()
        }

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
        layoutParamsVersion.bottomMargin = application.px2Px(binding.version.marginBottom)
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
            application.px2Px(binding.versionName.layoutParams.width)
        val txtTextSize =
            application.px2PxFont(binding.versionName.textSize)

        binding.versionName.layoutParams.width = txtWidth
        binding.versionName.textSize = txtTextSize

        for (i in listOf(
            binding.qrcode,
            binding.confirmConfig,
            binding.clear,
            binding.checkVersion,
            binding.exit,
            binding.appreciate,
        )) {
            i.layoutParams.width = btnWidth
            i.layoutParams.height = btnHeight
            i.textSize = btnTextSize
            i.layoutParams = btnLayoutParams
            i.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    i.background = ColorDrawable(
                        ContextCompat.getColor(
                            context,
                            R.color.focus
                        )
                    )
                } else {
                    i.background = ColorDrawable(
                        ContextCompat.getColor(
                            context,
                            R.color.description_blur
                        )
                    )
                }
            }
        }

        val textSizeSwitch = application.px2PxFont(binding.switchChannelReversal.textSize)

        val layoutParamsSwitch =
            binding.switchChannelReversal.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsSwitch.topMargin =
            application.px2Px(binding.switchChannelReversal.marginTop)

        for (i in listOf(
            binding.switchChannelReversal,
            binding.switchChannelNum,
            binding.switchTime,
            binding.switchBootStartup,
            binding.switchRepeatInfo,
            binding.switchConfigAutoLoad,
            binding.switchDefaultLike,
            binding.switchShowAllChannels,
        )) {
            i.textSize = textSizeSwitch
            i.layoutParams = layoutParamsSwitch
            i.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    i.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.focus
                        )
                    )
                } else {
                    i.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.title_blur
                        )
                    )
                }
            }
        }

        updateManager = UpdateManager(context, context.appVersionCode)

        return binding.root
    }

    private fun confirmConfig() {
        if (SP.config == null) {
            return
        }

        var url = SP.config!!
        url = Utils.formatUrl(url)
        uri = Uri.parse(url)
        if (uri.scheme == "") {
            uri = uri.buildUpon().scheme("http").build()
        }
        if (uri.isAbsolute) {
            if (uri.scheme == "file") {
                requestReadPermissions()
            } else {
                TVList.parseUri(uri)
            }
        } else {
            R.string.invalid_config_address.showToast()
        }
        (activity as MainActivity).settingActive()
    }

    private fun confirmChannel() {
        SP.channel = min(max(SP.channel, 1), TVList.listModel.size)

        (activity as MainActivity).settingActive()
    }

    fun setServer(server: String) {
        this.server = "http://$server"
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
                R.string.authorization_failed.showToast()
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
                R.string.authorization_failed.showToast()
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

