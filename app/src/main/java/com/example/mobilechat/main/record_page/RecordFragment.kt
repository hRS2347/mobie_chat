package com.example.mobilechat.main.record_page

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.example.mobilechat.R
import com.example.mobilechat.databinding.FragmentRecordBinding
import com.example.mobilechat.utils.network.ServerConnector
import com.example.mobilechat.utils.record.Recorder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import com.example.mobilechat.utils.record.Recorder.RecordState

class RecordFragment : Fragment() {
    companion object {
    }

    private lateinit var bind: FragmentRecordBinding
    private lateinit var connector: ServerConnector
    private var state = MutableLiveData(RecordState.EMPTY_AVAILABLE)
    private val content = MutableLiveData<String>("")
    private val recorder = Recorder.instance
    private lateinit var future: Future<String?>
    private val executor = Executors.newSingleThreadExecutor()

    private val onClickListener = View.OnClickListener {
        state.postValue(RecordState.EMPTY_AVAILABLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentRecordBinding.inflate(layoutInflater)
        connector = ServerConnector.instance
        return bind.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        state.postValue(RecordState.EMPTY_AVAILABLE)
        content.observe(viewLifecycleOwner) {
            bind.text.text = Editable.Factory.getInstance().newEditable(it)
        }

        state.observe(viewLifecycleOwner) {
            when (it) {
                RecordState.ERROR -> {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    state.postValue(RecordState.EMPTY_AVAILABLE)
                }

                RecordState.EMPTY_AVAILABLE -> {
                    Log.d("RecordFragment", "EMPTY_AVAILABLE")
                    bind.btn.setOnClickListener(null)
                    bind.btn.isEnabled = true
                    bind.btn.visibility = View.VISIBLE
                    bind.progressBar.visibility = View.INVISIBLE
                    bind.btn.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.rec_button))
                }

                RecordState.RECORDING -> {
                    val d = Log.d("RecordFragment", "RECORDING")
                    future = recorder.startRecord(executor)
                    bind.btn.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.rec_ing))
                }

                RecordState.ANALYZING -> {
                    Log.d("RecordFragment", "ANALYZING")
                    bind.btn.visibility = View.INVISIBLE
                    bind.progressBar.visibility = View.VISIBLE
                    bind.progressBar.animate()
                    recorder.stopRecord()
                    //打印录音的大小
                    val get = future.get()
                    Log.d("RecordFragment", "future: $get")
                    connector.analyzeVoice(get as String, object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            state.postValue(RecordState.EMPTY_AVAILABLE)
                            Log.d("RecordFragment", "onFailure: $e")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            try {
                                content.postValue(response.body?.string())
                                state.postValue(RecordState.RELOAD)
                            } catch (e: Exception) {
                                state.postValue(RecordState.EMPTY_AVAILABLE)
                                Log.d("RecordFragment", "onResponse: $e")
                            }
                        }
                    })


                }

                RecordState.RELOAD -> {
                    bind.btn.visibility = View.VISIBLE
                    bind.progressBar.visibility = View.INVISIBLE
                    bind.btn.setOnClickListener(onClickListener)
                    bind.btn.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.rec_button))
                }

                else -> state.postValue(RecordState.ERROR)
            }

            //long press to record, release to analyze
            bind.btn.setOnTouchListener { _, event ->
                when (event?.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        //perform haptic feedback
                        bind.btn.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        state.postValue(RecordState.RECORDING)
                        Log.d("RecordFragment", "onViewCreated: ACTION_DOWN")
                    }

                    android.view.MotionEvent.ACTION_UP -> {
                        state.postValue(RecordState.ANALYZING)
                        Log.d("RecordFragment", "onViewCreated: ACTION_UP")
                    }
                }
                true
            }

            //press copy btn to copy text
            bind.copyBtn.setOnClickListener {
                val clipboard =
                    context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("text", content.value)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
            }

            //press return btn to return
            bind.back.setOnClickListener {
                Navigation.findNavController(view).navigateUp()
            }
        }
    }
}

