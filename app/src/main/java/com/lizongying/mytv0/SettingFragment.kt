package com.lizongying.mytv0

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val switchBootStartup = _binding?.switchBootStartup
        switchBootStartup?.isChecked = SP.bootStartup
        switchBootStartup?.setOnCheckedChangeListener { _, isChecked ->
            SP.bootStartup = isChecked
            (activity as MainActivity).settingActive()
        }

        val updateManager = UpdateManager(context, this, context.appVersionCode)
        binding.checkVersion.setOnClickListener {
            OnClickListenerCheckVersion(updateManager)
            (activity as MainActivity).settingActive()
        }

        binding.confirmButton.setOnClickListener {
            val uriEditText = binding.myEditText
            var uri = uriEditText.text.toString()

            uri = Utils.formatUrl(uri)
            if (Uri.parse(uri).isAbsolute) {
                TVList.update(uri)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SettingFragment"
    }
}

