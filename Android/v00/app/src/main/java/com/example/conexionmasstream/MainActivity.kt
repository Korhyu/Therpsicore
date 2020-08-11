package com.example.conexionmasstream

import android.app.ProgressDialog
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.ConnectivityManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.nio.ByteBuffer
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    private var mStop: Boolean = false
    private val bufSin1 = createSinWaveBuffer(30.0, 1000)
    private val bufSin2 = createSinWaveBuffer(300.0, 1000)
    private val bufSin3 = createSinWaveBuffer(3000.0, 1000)
    private val bufSin4 = createSinWaveBuffer(30000.0, 1000)



    var mAudioTrack: AudioTrack = AudioTrack(AudioManager.STREAM_MUSIC,44100, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, 16000,
            AudioTrack.MODE_STREAM)

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this
        getQuestions().execute()

        //        Thread.sleep(2000)
        mAudioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,44100, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 300000,
                AudioTrack.MODE_STREAM)
        logId.append("\n")
        if(switch1.isChecked){
            logId.append("Guitarra on\n")
        }else {
            logId.append("Guitarra off\n")
        }
        val aux1:ByteArray= ByteArray(2)
        aux1[0]=1
        aux1[1]=60
        var aux3:Short
        aux3= ByteBuffer.wrap(aux1).short
        logId.append("$aux3 \n")
    }
    internal inner class getQuestions : AsyncTask<Void, Void, String>() {

        lateinit var progressDialog: ProgressDialog
        var hasInternet = false

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Downloading Questions... ")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        override fun doInBackground(vararg p0: Void?): String {
            if (isNetworkAvailable()){
                hasInternet=true
//                val client = OkHttpClient()
//                val url = "https://script.googleusercontent.com/macros/echo?user_content_key=1tgBN-ES-vsiLin8Lggs7R094sUSEWlBY3Lv7yLt0KnrexUuaTvreORsTenxGH0HaPDQ0rUkXVqmkc903P_gQrpXCbi98gcsm5_BxDlH2jW0nuo2oDemN9CCS2h10ox_1xSncGQajx_ryfhECjZEnBg4Wj9So2Q_mI0_S0Bm21-AGmcRnplmVaRcxvVzvCi9cnQQJegsnVb9TgJzPufw35cdv3aNHr6K&lib=MKMzvVvSFmMa3ZLOyg67WCThf1WVRYg6Z"
//                val request = Request.Builder().url(url).build()
//                val response = client.newCall(request).execute()

                logId.append("Configurando Puerto\n")
                val group = InetAddress.getByName("226.1.1.1")
                println("puerto bien")
                logId.append("Configurando Socket\n")
                val s = MulticastSocket(4321)
                println("socket bien")
                logId.append("Uniendome al grupo\n")
                s.joinGroup(group)
                logId.append("Esperando Recepcion\n")
                progressDialog.dismiss()

                while(!mStop) {
                    val datoCrudo = receive(s)
//                    var tb=0

                    var guitarra:ShortArray=ShortArray(1000)
                    var bajo:ShortArray=ShortArray(1000)
                    var voz1:ShortArray=ShortArray(1000)
                    var voz2:ShortArray=ShortArray(1000)
//                    logId.append("hasta aca\n")
//                    logId.append("$datoCrudo\n")
                    for(index in 0 .. datoCrudo.size-10 step 9){
//                        var indp = index/9
                        guitarra[datoCrudo[index].toInt()]= (ByteBuffer.wrap(datoCrudo,index+1,2).short - 2047).toShort()
                        bajo[datoCrudo[index].toInt()]= (ByteBuffer.wrap(datoCrudo,index+3,2).short - 2047).toShort()
                        voz1[datoCrudo[index].toInt()]= (ByteBuffer.wrap(datoCrudo,index+5,2).short - 2047).toShort()
                        voz2[datoCrudo[index].toInt()]= (ByteBuffer.wrap(datoCrudo,index+7,2).short - 2047).toShort()
//                        tb++
                    }
                    var bufSin= ShortArray(1000)
                    logId.append("Agregar 1000 muestras al Buffer\n")
                    //
                    for (i:Int in bufSin.indices) {
                        if(switch1.isChecked){

                            bufSin[i]=((bufSin[i]+guitarra[i]*seekBar1.progress)/10).toShort()
                        }
                        if(switch2.isChecked){

                            bufSin[i]=((bufSin[i]+bajo[i]*seekBar2.progress)/10).toShort()
                        }
                        if(switch3.isChecked){

                            bufSin[i]=((bufSin[i]+voz1[i]*seekBar3.progress)/10).toShort()
                        }
                        if(switch4.isChecked){

                            bufSin[i]=((bufSin[i]+voz2[i]*seekBar4.progress)/10).toShort()
                        }
//                    bufSin[i] = (bufSin1[i]*0.5+bufSin2[i] + bufSin3[i] + bufSin4[i]).toByte()
                    }
                    //                val bufSin = createSinWaveBuffer(30.0, 1000)
                    mAudioTrack.write(bufSin,0, bufSin.size)
//                    logId.append("canal 1 tiene: ${guitarra.toString()}\n")
//                    logId.append("canal 2 tiene: ${bajo.toString()}\n")
//                    logId.append("canal 3 tiene: ${voz1.toString()}\n")
//                    logId.append("canal 4 tiene: ${voz2.toString()}\n")
                }

                return "terminamo"//response.body?.string().toString()
            }else{
                return ""
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)


            if (hasInternet){
                logId.append(result)
            }else{
                logId.append("No internet")
            }
        }


    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected


    }

    //s:MulticastSocket
    fun receive(s: MulticastSocket):ByteArray {
        // get their responses!
        val buf:ByteArray = ByteArray(1000)
        val recv = DatagramPacket(buf, buf.size)
        //tvResult.append("Esperando Recepcion\n")
        s.receive(recv);
//        val buffer = recv.data;
//        val packetAsString= String(buffer, 0, recv.length);
//        tvResult.append("recibi\n")
        //tvResult.append("$recv\n")
        return buf  //packetAsString
    }


    fun stop() {

        logId.append("Stop\n")
        mStop = true;
        mAudioTrack.stop();

    }

    fun start(view: View) {
        logId.append("Start\n")
        mStop = false;

        /* 8000 bytes per second*/
        mAudioTrack.play();


    }

    private fun createSinWaveBuffer(freq: Double, ms: Int, sampleRate: Int = 44100): ShortArray {
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

    fun mas(view: View) {
        Thread(Runnable{
            this.runOnUiThread(java.lang.Runnable {
                Thread.currentThread().priority = Thread.MIN_PRIORITY;
//                while(!mStop) {
//                var bufSin= ShortArray(30001)
//                logId.append("Agregar 30000 muestras al Buffer\n")
//                //
//                for (i:Int in 0 .. 30000) {
//                    if(switch1.isChecked){
//
//                        bufSin[i]=((bufSin[i]+guitarra[i]*seekBar1.progress)/10).toShort()
//                    }
//                    if(switch2.isChecked){
//
//                        bufSin[i]=((bufSin[i]+bajo[i]*seekBar2.progress)/10).toShort()
//                    }
//                    if(switch3.isChecked){
//
//                        bufSin[i]=((bufSin[i]+voz1[i]*seekBar3.progress)/10).toShort()
//                    }
//                    if(switch4.isChecked){
//
//                        bufSin[i]=((bufSin[i]+voz2[i]*seekBar4.progress)/10).toShort()
//                    }
////                    bufSin[i] = (bufSin1[i]*0.5+bufSin2[i] + bufSin3[i] + bufSin4[i]).toByte()
//                }
//                //                val bufSin = createSinWaveBuffer(30.0, 1000)
//                mAudioTrack.write(bufSin,0, bufSin.size)
//                    Thread.sleep(2000)
//                }
                //Thread.interrupted()

            })
        }).start()
    }


}