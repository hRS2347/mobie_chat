package com.example.mobilechat.main.chat_page.sheet

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.example.mobilechat.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.mobilechat.databinding.SheetRecordBinding
import com.example.mobilechat.main.chat_page.ChatFragment
import com.example.mobilechat.utils.network.ServerConnector
import com.example.mobilechat.utils.record.Recorder
import java.util.concurrent.Executors
import java.util.concurrent.Future
import com.example.mobilechat.utils.record.Recorder.RecordState
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class RecordSheet : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "ModalBottomSheet"
    }

    private lateinit var bind: SheetRecordBinding
    private lateinit var connector: ServerConnector
    private var state = MutableLiveData<Recorder.RecordState>(Recorder.RecordState.EMPTY_AVAILABLE)
    private val content = MutableLiveData<String>("")
    private val recorder = Recorder.instance
    private lateinit var future: Future<String?>
    private val executor = Executors.newSingleThreadExecutor()

    private val onClickListener = View.OnClickListener {
        state.postValue(RecordState.EMPTY_AVAILABLE)
    }

    lateinit var listener: ChatFragment.OnSubmitListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = SheetRecordBinding.inflate(inflater, container, false).apply {
        bind = this
        connector = ServerConnector.instance
    }.root

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        state.postValue(RecordState.EMPTY_AVAILABLE)
        content.observe(viewLifecycleOwner) {
            bind.text.text = Editable.Factory.getInstance().newEditable(it)
        }
        content.postValue("")
        state.observe(viewLifecycleOwner) {
            when (it) {
                RecordState.ERROR -> {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    state.postValue(RecordState.EMPTY_AVAILABLE)
                }

                RecordState.EMPTY_AVAILABLE -> {
                    bind.btn.setOnClickListener(null)
                    bind.btn.isEnabled = true
                    bind.btn.visibility = View.VISIBLE
                    bind.progressBar.visibility = View.INVISIBLE
                    bind.btn.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.rec_button))
                }

                RecordState.RECORDING -> {
                    val startRecord = recorder.startRecord(executor)
                    future = startRecord
                    bind.btn.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.rec_ing))
                }

                RecordState.ANALYZING -> {
                    bind.btn.visibility = View.INVISIBLE
                    bind.progressBar.visibility = View.VISIBLE
                    bind.progressBar.animate()
                    recorder.stopRecord()
                    val get = future.get()
                    connector.analyzeVoice(get as String, object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            state.postValue(RecordState.EMPTY_AVAILABLE)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            content.postValue(response.body?.string())
                            state.postValue(RecordState.RELOAD)
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
                    }

                    android.view.MotionEvent.ACTION_UP -> {
                        state.postValue(RecordState.ANALYZING)
                    }
                }
                true
            }

            //press cancel to close this sheet
            bind.tvCancel.setOnClickListener {
                dismiss()
            }

            //press cancel to close this sheet
            bind.tvSubmit.setOnClickListener {
                listener.onSubmit(content.value!!)
                dismiss()
            }

        }
    }


}