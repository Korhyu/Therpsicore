package com.example.conexionmasstream

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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



    var mAudioTrack: AudioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT, 16000,
        AudioTrack.MODE_STREAM
    )

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this
        getQuestions().execute()

        //        Thread.sleep(2000)
        mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, 300000,
            AudioTrack.MODE_STREAM
        )
        logId.append("\n")

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

                while(!mStop)
                {
                    val datoCrudo = receive(s)

                    var canal1:ShortArray=ShortArray(1000)
                    var canal2:ShortArray=ShortArray(1000)
                    var canal3:ShortArray=ShortArray(1000)
                    var canal4:ShortArray=ShortArray(1000)

                    //Todo: Agregar encabezado que traiga informacion desde la rasp
                    //Todo: Si la informacion traida modifica alguno de los textos, disparar la funcion "updateInfo"

                    if (datoCrudo[0] < 240)
                    {
                        for(index in 0 .. datoCrudo.size-10 step 9)
                        {
                            canal1[datoCrudo[index].toInt()] = (ByteBuffer.wrap(
                                datoCrudo,
                                index + 1,
                                2
                            ).short - 2047).toShort()
                            canal2[datoCrudo[index].toInt()] = (ByteBuffer.wrap(
                                datoCrudo,
                                index + 3,
                                2
                            ).short - 2047).toShort()
                            canal3[datoCrudo[index].toInt()] = (ByteBuffer.wrap(
                                datoCrudo,
                                index + 5,
                                2
                            ).short - 2047).toShort()
                            canal4[datoCrudo[index].toInt()] = (ByteBuffer.wrap(
                                datoCrudo,
                                index + 7,
                                2
                            ).short - 2047).toShort()
                        }
                        var bufSin= ShortArray(1000)
                        logId.append("Agregar 1000 muestras al Buffer\n")
                        //
                        for (i:Int in bufSin.indices)
                        {
                            if(switch1.isChecked){

                                bufSin[i]=((bufSin[i]+canal1[i]*seekBar1.progress)/10).toShort()
                            }
                            if(switch2.isChecked){

                                bufSin[i]=((bufSin[i]+canal2[i]*seekBar2.progress)/10).toShort()
                            }
                            if(switch3.isChecked){

                                bufSin[i]=((bufSin[i]+canal3[i]*seekBar3.progress)/10).toShort()
                            }
                            if(switch4.isChecked){

                                bufSin[i]=((bufSin[i]+canal4[i]*seekBar4.progress)/10).toShort()
                            }
                        }
                        //                val bufSin = createSinWaveBuffer(30.0, 1000)
                        mAudioTrack.write(bufSin, 0, bufSin.size)
    //                    logId.append("canal 1 tiene: ${canal1.toString()}\n")
    //                    logId.append("canal 2 tiene: ${canal2.toString()}\n")
    //                    logId.append("canal 3 tiene: ${canal3.toString()}\n")
    //                    logId.append("canal 4 tiene: ${canal4.toString()}\n")
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


}