package com.lizongying.mytv0

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lizongying.mytv0.databinding.SettingBinding


class SettingFragment(
    private val versionName: String,
    private val versionCode: Long,
) :
    Fragment() {

    private var _binding: SettingBinding? = null
    private val binding get() = _binding!!

//    private lateinit var updateManager: UpdateManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingBinding.inflate(inflater, container, false)

        _binding?.version?.text =
            "当前版本: $versionName\n获取最新: https://github.com/lizongying/my-tv/releases/"

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

        val switchBootStartup = _binding?.switchBootStartup
        switchBootStartup?.isChecked = SP.bootStartup
        switchBootStartup?.setOnCheckedChangeListener { _, isChecked ->
            SP.bootStartup = isChecked
            (activity as MainActivity).settingActive()
        }

//        _binding?.checkVersion?.setOnClickListener(OnClickListenerCheckVersion(updateManager))

        return binding.root
    }

    fun setVersionName(versionName: String) {
        binding.versionName.text = versionName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
//        updateManager.destroy()
    }

    companion object {
        const val TAG = "SettingFragment"
    }
}

