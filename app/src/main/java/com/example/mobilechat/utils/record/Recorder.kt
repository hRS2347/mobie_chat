package com.example.mobilechat.utils.record

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import com.example.mobilechat.MyApplication
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class Recorder(var context: Context) {
    companion object {
        val instance: Recorder by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Recorder(context = MyApplication.instance)
        }

        const val AUDIO_SAMPLE_RATE = 16000
        const val AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT

    }

    val ROOT = Environment.getExternalStorageDirectory().absolutePath + "/mobile_chat/"
    val Name =
        Environment.getExternalStorageDirectory().absolutePath + "/mobile_chat/" + "recorded_audio.pcm"
    val Name_WAV =
        Environment.getExternalStorageDirectory().absolutePath + "/mobile_chat/" + "recorded_audio.wav"

    var isRecording = false

    enum class RecordState {
        EMPTY_AVAILABLE,
        RECORDING,
        ANALYZING,
        RELOAD,
        ERROR
    }

    private lateinit var recorder: AudioRecord
    private val outputPath =
        Name_WAV

    //record
    @SuppressLint("MissingPermission")
    fun startRecord(executor: ExecutorService): Future<String?> {
        //init recorder with wav format
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            AUDIO_SAMPLE_RATE,
            AUDIO_CHANNEL,
            AUDIO_ENCODING,
            AudioRecord.getMinBufferSize(
                AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL,
                AUDIO_ENCODING
            )
        )

        isRecording = true
        recorder.startRecording()

        val call = Callable {
            try {
                writeDataTOFile()
                return@Callable outputPath
            } catch (e: IOException) {
                e.printStackTrace()
            }
            null
        }
        return executor.submit(call)
    }

    private fun writeDataTOFile() {
        //新建文件以及创建输出流
        try {
            val file = File(ROOT)
            if (!file.exists()) {
                Log.i("RecordFragment", "ROOT:$ROOT")
                file.mkdirs()
            }
            val pcmFile = File(Name)
            val wavFile = File(Name_WAV)
            //delete original file
            if (pcmFile.exists()) pcmFile.delete()
            if (wavFile.exists()) wavFile.delete()
            wavFile.createNewFile()
            pcmFile.createNewFile()
            val dos = DataOutputStream(BufferedOutputStream(FileOutputStream(pcmFile, true)))
            val byteData = ByteArray(
                AudioRecord.getMinBufferSize(
                    AUDIO_SAMPLE_RATE,
                    AUDIO_CHANNEL,
                    AUDIO_ENCODING
                )
            )
            while (isRecording) {
                val read = recorder.read(byteData, 0, byteData.size)
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    dos.write(byteData, 0, read)
                }
                Log.d("RecordFragment", "writeDataTOFile: $read")
            }
            dos.flush()
            dos.close()
            WavUtils.convertPcm2Wav(
                Name,
                Name_WAV,
                AUDIO_SAMPLE_RATE,
                1,
                16
            )
        } catch (e: IOException) {
            Log.e("RecordFragment", "writeDataTOFile: " + e)
        }
    }

    fun stopRecord(): String {
        //stop recording
        recorder.stop()
        isRecording = false
        recorder.release()
        return outputPath
    }
}

