package com.example.pruebaasyncpriority

import android.app.ProgressDialog
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process.THREAD_PRIORITY_FOREGROUND
import android.os.Process.setThreadPriority
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.nio.ByteBuffer
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    private val bufSin1 = createSinWaveBuffer(3000.0, 1000)
    var aWrite=0
    var mAudioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT, 16*30,
        AudioTrack.MODE_STREAM
    )

    val s = Semaphore(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAudioTrack.play();
        getQuestions().execute()
        val aux=1
        thread(start = true, priority = THREAD_PRIORITY_FOREGROUND){
            val aux=1
            logId.append("Inicia tarea\n")
            while(aux==1) {
                s.acquireUninterruptibly()
                mAudioTrack.write(bufSin1, 0, 16*30) //bufSin
            }
            logId.append("tarea salio while\n")
        }
    }

    internal inner class getQuestions : AsyncTask<Void, Void, String>() {

        lateinit var progressDialog: ProgressDialog
        var hasInternet = false

        override fun onPreExecute() {
            super.onPreExecute()
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun doInBackground(vararg p0: Void?): String {

//                    mAudioTrack.write(bufSin1, 0, 3000, WRITE_BLOCKING) //bufSin
           setThreadPriority(THREAD_PRIORITY_FOREGROUND)
            val aux=1
            logId.append("Inicia Async\n")
            while(aux==1) {
                Thread.sleep(9)
                s.release()
            }

            return "hola"
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
        }


    }

    private fun createSinWaveBuffer(freq: Double, ms: Int, sampleRate: Int = 48000): ShortArray {
        val samples = (ms * sampleRate / 1000)
        val output = ShortArray(samples)
        val period = sampleRate.toDouble() / freq
        for (i in output.indices) {
            val angle = 2.0 * Math.PI * i.toDouble() / period
            output[i] = (sin(angle) * 32767f).toShort()
        }
        //output.forEach { println(it) }
        return output
    }


}