package com.lizongying.mytv0

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginEnd
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import com.lizongying.mytv0.databinding.SettingBinding
import com.lizongying.mytv0.models.TVList


class SettingFragment : Fragment() {

    private var _binding: SettingBinding? = null
    private val binding get() = _binding!!


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

        val updateManager = UpdateManager(context, this, context.appVersionCode)
        binding.checkVersion.setOnClickListener {
            OnClickListenerCheckVersion(updateManager)
            (activity as MainActivity).settingActive()
        }

        val uriEditText = binding.myEditText
        uriEditText.text = SP.config?.let { Editable.Factory.getInstance().newEditable(it) }
            ?: Editable.Factory.getInstance().newEditable("")
        binding.confirmButton.setOnClickListener {
            var uri = uriEditText.text.toString().trim()
            uri = Utils.formatUrl(uri)
            if (Uri.parse(uri).isAbsolute) {
                TVList.update(uri)
                SP.config = uri
            } else {
                uriEditText.error = "无效的地址"
            }
            (activity as MainActivity).settingActive()
        }

        binding.appreciate.setOnClickListener {
            val imageModalFragment = AppreciateModalFragment()

            val args = Bundle()
            args.putInt(AppreciateModalFragment.KEY, R.drawable.appreciate)
            imageModalFragment.arguments = args

            imageModalFragment.show(requireFragmentManager(), AppreciateModalFragment.TAG)
            (activity as MainActivity).settingActive()
        }

        binding.setting.setOnClickListener {
            hideSelf()
        }

        binding.exit.setOnClickListener {
            requireActivity().finishAffinity()
        }

        val application = requireActivity().applicationContext as MyTvApplication

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

        binding.checkVersion.layoutParams.width =
            application.px2Px(binding.checkVersion.layoutParams.width)
        binding.checkVersion.layoutParams.height =
            application.px2Px(binding.checkVersion.layoutParams.height)
        binding.checkVersion.textSize = application.px2PxFont(binding.checkVersion.textSize)
        val layoutParamsCheckVersion =
            binding.checkVersion.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsCheckVersion.marginEnd = application.px2Px(binding.checkVersion.marginEnd)
        binding.checkVersion.layoutParams = layoutParamsCheckVersion

        binding.versionName.textSize = application.px2PxFont(binding.versionName.textSize)


        binding.confirmButton.layoutParams.width =
            application.px2Px(binding.confirmButton.layoutParams.width)
        binding.confirmButton.layoutParams.height =
            application.px2Px(binding.confirmButton.layoutParams.height)
        binding.confirmButton.textSize = application.px2PxFont(binding.confirmButton.textSize)
        val layoutParamsConfirmButton =
            binding.confirmButton.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsConfirmButton.marginEnd = application.px2Px(binding.confirmButton.marginEnd)
        binding.confirmButton.layoutParams = layoutParamsConfirmButton

        binding.myEditText.layoutParams.width =
            application.px2Px(binding.myEditText.layoutParams.width)
        binding.myEditText.textSize = application.px2PxFont(binding.myEditText.textSize)


        binding.appreciate.layoutParams.width =
            application.px2Px(binding.appreciate.layoutParams.width)
        binding.appreciate.layoutParams.height =
            application.px2Px(binding.appreciate.layoutParams.height)
        binding.appreciate.textSize = application.px2PxFont(binding.appreciate.textSize)
        val layoutParamsAppreciate =
            binding.appreciate.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsAppreciate.topMargin =
            application.px2Px(binding.appreciate.marginTop)
        binding.appreciate.layoutParams = layoutParamsAppreciate

        binding.exit.layoutParams.width =
            application.px2Px(binding.exit.layoutParams.width)
        binding.exit.layoutParams.height =
            application.px2Px(binding.exit.layoutParams.height)
        binding.exit.textSize = application.px2PxFont(binding.exit.textSize)
        val layoutParamsExit =
            binding.exit.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsExit.topMargin =
            application.px2Px(binding.exit.marginTop)
        binding.exit.layoutParams = layoutParamsExit

        binding.switchChannelReversal.textSize =
            application.px2PxFont(binding.switchChannelReversal.textSize)
        val layoutParamsChannelReversal =
            binding.switchChannelReversal.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsChannelReversal.topMargin =
            application.px2Px(binding.switchChannelReversal.marginTop)
        binding.switchChannelReversal.layoutParams = layoutParamsChannelReversal

        binding.switchChannelNum.textSize = application.px2PxFont(binding.switchChannelNum.textSize)
        val layoutParamsChannelNum =
            binding.switchChannelNum.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsChannelNum.topMargin = application.px2Px(binding.switchChannelNum.marginTop)
        binding.switchChannelNum.layoutParams = layoutParamsChannelNum

        binding.switchTime.textSize = application.px2PxFont(binding.switchTime.textSize)
        val layoutParamsTime = binding.switchTime.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsTime.topMargin = application.px2Px(binding.switchTime.marginTop)
        binding.switchTime.layoutParams = layoutParamsTime

        binding.switchBootStartup.textSize =
            application.px2PxFont(binding.switchBootStartup.textSize)

        val layoutParamsBootStartup =
            binding.switchBootStartup.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsBootStartup.topMargin = application.px2Px(binding.switchBootStartup.marginTop)
        binding.switchBootStartup.layoutParams = layoutParamsBootStartup


        binding.switchRepeatInfo.textSize = application.px2PxFont(binding.switchRepeatInfo.textSize)

        val layoutParamsRepeatInfo =
            binding.switchRepeatInfo.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsRepeatInfo.topMargin = application.px2Px(binding.switchRepeatInfo.marginTop)
        binding.switchRepeatInfo.layoutParams = layoutParamsRepeatInfo


        binding.switchConfigAutoLoad.textSize =
            application.px2PxFont(binding.switchConfigAutoLoad.textSize)

        val layoutParamsConfigAutoLoad =
            binding.switchConfigAutoLoad.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsConfigAutoLoad.topMargin =
            application.px2Px(binding.switchConfigAutoLoad.marginTop)
        binding.switchConfigAutoLoad.layoutParams = layoutParamsConfigAutoLoad

        return binding.root
    }

    internal class OnClickListenerCheckVersion(private val updateManager: UpdateManager) :
        View.OnClickListener {
        override fun onClick(view: View?) {
            updateManager.checkAndUpdate()
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SettingFragment"
    }
}

