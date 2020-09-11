package com.example.conexionmasstream

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.AudioTrack.WRITE_BLOCKING
import android.media.AudioTrack.WRITE_NON_BLOCKING
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Process.THREAD_PRIORITY_AUDIO
import android.os.Process.setThreadPriority
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.nio.ByteBuffer
import kotlin.concurrent.thread
import kotlin.math.sin


class MainActivity : AppCompatActivity() {

    private var mStop: Boolean = false
    private val bufSin1 = createSinWaveBuffer(3000.0, 1000)
    private val bufSin2 = createSinWaveBuffer(300.0, 1000)
    private val bufSin3 = createSinWaveBuffer(3000.0, 1000)
    private val bufSin4 = createSinWaveBuffer(30000.0, 1000)
    private var bufSin= ShortArray(1000)
    private var auxWrite=0
    private var auxCount=0

//    val buferasd = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

    var mAudioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT, 30000,
        AudioTrack.MODE_STREAM
    )



    lateinit var context: Context

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this
        getQuestions().execute()

        //        Thread.sleep(2000)

        logId.append("\n")



        thread(start = true, priority = THREAD_PRIORITY_AUDIO){
//            var threadId = Thread.currentThread().getId().toInt()
//            setThreadPriority(threadId , -19)
            println("${Thread.currentThread()} has run.")
            logId.append("iniciando Hilo\n")
            var auxCount1 = 0
            var ee1 = 0
            while(!mStop){
                if(auxWrite==1){
                    ee1 = mAudioTrack.write(bufSin, 0, 500) //bufSin
                    Thread.sleep(1)
//                    logId.append("$ee1\n")
//                    auxCount1 += 255
//                    if(auxCount1>=(bufSin1.size - 255)){
//                        auxCount1=0
                    }

                    auxWrite=0
//                }
            }
        }

        // Verifico que la red sea la correspondiente
        val wifiManager =  getApplicationContext().getSystemService (WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        val ssid = info.ssid
        /*if (ssid. )
        {
            openDialog()
        }*/


        if(switch1.isChecked)
        { logId.append("Canal 1: ON\n") }
        else { logId.append("Canal 1: OFF\n") }

        if(switch2.isChecked)
        { logId.append("Canal 2: ON\n") }
        else { logId.append("Canal 2: OFF\n") }

        if(switch3.isChecked)
        { logId.append("Canal 3: ON\n") }
        else { logId.append("Canal 3: OFF\n") }

        if(switch4.isChecked)
        { logId.append("Canal 4: ON\n") }
        else { logId.append("Canal 4: OFF\n") }

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

        @RequiresApi(Build.VERSION_CODES.M)
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

                var datoCrudo = ByteArray(1024*2)
                while(!mStop)
                {
                    if(auxWrite==0) {
                        datoCrudo = receive(s)//ByteArray(1024*2)
                    }
//                    mAudioTrack.write(bufSin1, 0, 3000, WRITE_BLOCKING) //bufSin
//                    Thread.sleep(4)


                    //Todo: Agregar encabezado que traiga informacion desde la rasp
                    //Todo: Si la informacion traida modifica alguno de los textos, disparar la funcion "updateInfo"||

                    if (datoCrudo[0] < 240)
                    {

                            var count = 0;

                            for (index in 2..datoCrudo.size - 8 step 8) {
                                bufSin[count] = 0
                                if (switch1.isChecked) {

                                    bufSin[count] = ((bufSin[count] + (datoCrudo[index].toUByte()
                                        .toInt() + (datoCrudo[index + 1].toInt() shl 8) - 2047).toShort() * seekBar1.progress) / 10).toShort()
                                }
                                if (switch2.isChecked) {

                                    bufSin[count] =
                                        ((bufSin[count] + (datoCrudo[index + 2].toUByte()
                                            .toInt() + (datoCrudo[index + 3].toInt() shl 8) - 2047).toShort() * seekBar2.progress) / 10).toShort()
                                }
                                if (switch3.isChecked) {

                                    bufSin[count] =
                                        ((bufSin[count] + (datoCrudo[index + 4].toUByte()
                                            .toInt() + (datoCrudo[index + 5].toInt() shl 8) - 2047).toShort() * seekBar3.progress) / 10).toShort()
                                }
                                if (switch4.isChecked) {

                                    bufSin[count] =
                                        ((bufSin[count] + (datoCrudo[index + 6].toUByte()
                                            .toInt() + (datoCrudo[index + 7].toInt() shl 8) - 2047).toShort() * seekBar4.progress) / 10).toShort()
                                }
                                count++
                            }
                            auxCount = count
                            auxWrite = 1
                            logId.append("Agregar 1000 muestras al Buffer\n")

                    }
                    else
                    {
                        when (datoCrudo[0].toInt())
                        {
                            //Cambio de titulo general
                            240 -> textView.text = ((ByteBuffer.wrap(
                                datoCrudo,
                                datoCrudo[0] + 1,
                                datoCrudo.size
                            )).toString())
                            //Cambio nombre Canal1
                            241 -> switch1.text = ((ByteBuffer.wrap(
                                datoCrudo,
                                datoCrudo[0] + 1,
                                datoCrudo.size
                            )).toString())
                            //Cambio nombre Canal2
                            242 -> switch2.text = ((ByteBuffer.wrap(
                                datoCrudo,
                                datoCrudo[0] + 1,
                                datoCrudo.size
                            )).toString())
                            //Cambio nombre Canal3
                            243 -> switch3.text = ((ByteBuffer.wrap(
                                datoCrudo,
                                datoCrudo[0] + 1,
                                datoCrudo.size
                            )).toString())
                            //Cambio nombre Canal4
                            244 -> switch4.text = ((ByteBuffer.wrap(
                                datoCrudo,
                                datoCrudo[0] + 1,
                                datoCrudo.size
                            )).toString())

                            //Todo: Quiza poner un numero de una toast?

                            else -> logId.append("Codigo de servicio incorrecto " + datoCrudo[0].toString() + "\n")
                        }
                    }

                }

                return "terminamo"//response.body?.string().toString()

            }
            else
            {
                //Todo: Si no esta conectado a la red saltar el cartel de conectarse de nuevo
                return ""
            }


        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)


            if (hasInternet){
                logId.append(result)
            }
            else
            {
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
        val buf:ByteArray = ByteArray(1024*2)
        val recv = DatagramPacket(buf, buf.size)
        //tvResult.append("Esperando Recepcion\n")
        s.receive(recv);
//        val buffer = recv.data;
//        val packetAsString= String(buffer, 0, recv.length);
//        tvResult.append("recibi\n")
        //tvResult.append("$recv\n")
        return buf  //packetAsString
    }


    fun stop(view: View) {

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

    fun test(view: View) {

        openDialog()

        //Cambio de titulo general
        textView.text = ("Los de").toString()
        //Cambio nombre Canal1
        switch1.text = ("chaca son").toString()
        //Cambio nombre Canal2
        switch2.text = ("todos").toString()
        //Cambio nombre Canal3
        switch3.text = ("putos").toString()
        //Cambio nombre Canal4
        //244 -> switch4.text = ((ByteBuffer.wrap(datoCrudo, datoCrudo[0] + 1, datoCrudo.size)).toString())
         
    }

    fun openDialog(){
        val dialogo = AlertDialog.Builder(this)
        dialogo.setTitle("Conexion WIFI ausente")
        dialogo.setMessage(
            "Por favor conectese a la red correspondiende\nNombre:\"Los de Chaca son\"\nPass: \"Todo_putos\"" +
                    "\n\nLa contraseña es sin espacios y sin comillas. Por favor loguee en la red correspondiente para recibir el streaming"
        )
        dialogo.setNeutralButton("Ok", { dialogInterface: DialogInterface, i: Int -> })
        dialogo.show()
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

    fun mas(view: View) {
        Thread(Runnable {
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

//    fun thread(
//        start: Boolean = true,
//        isDaemon: Boolean = false,
//        contextClassLoader: ClassLoader? = null,
//        name: String? = null,
//        priority: Int = -1,
//        block: () -> Unit
//    ): Thread {
//        //            var threadId = Thread.currentThread().getId().toInt()
////            setThreadPriority(threadId , -19)
//        println("${Thread.currentThread()} has run.")
//        logId.append("iniciando Hilo\n")
//        var auxCount1 = 0
//        var ee1 = 0
//        while(!mStop){
//            if(auxWrite==1){
//                ee1 = mAudioTrack.write(bufSin, 0, 500) //bufSin
//                Thread.sleep(1)
////                    logId.append("$ee1\n")
////                    auxCount1 += 255
////                    if(auxCount1>=(bufSin1.size - 255)){
////                        auxCount1=0
//            }
//
//            auxWrite=0
////                }
//        }
//
//    }


}