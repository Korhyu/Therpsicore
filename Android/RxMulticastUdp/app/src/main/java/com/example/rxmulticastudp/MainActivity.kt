package com.example.rxmulticastudp

import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val power = 1
    var cuentaRx=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rxRcv().execute()
        thread(start = true, priority = 5){ //THREAD_PRIORITY_AUDIO
            while (power==1){
                Thread.sleep(5000)
                Log.e("MEDIDA", "TOTAL $cuentaRx \n")
            }
        } //Hilo para revizar los Cheklist de los canales

    }

    internal inner class rxRcv : AsyncTask<Void, Void, String>() {

        var hasInternet = false

        override fun doInBackground(vararg p0: Void?): String? {
//            setThreadPriority(-10)
            var auxSin=0

            if (isNetworkAvailable()) {
                hasInternet = true

                logId.append("Configurando Puerto\n")
                val group = InetAddress.getByName("226.1.1.1")
                println("puerto bien")
                logId.append("Configurando Socket\n")
                val s = MulticastSocket(4321)
                println("socket bien")
                logId.append("Uniendome al grupo\n")
                s.joinGroup(group)
                logId.append("Esperando Recepcion\n")

                var datoCrudo=ByteArray(3840*2)
//                var anterior=0
//                var total=0
//                var startTime = System.nanoTime()
                while (power == 1) {


                    datoCrudo = receive(s)
                    cuentaRx++
//                    val nuevo = (datoCrudo[0].toShort() + (datoCrudo[1].toInt() shl 8))
//                    if(nuevo != anterior+1) {
//                        total++
////                        logId.append("dato N° $nuevo Cuenta N° $cuentaRx y $total\n")
////                      Log.e("Measure", "TASK took for " + ((System.nanoTime() - startTime) / 1000000) + "mS\n" )
//                    }
//                    anterior=nuevo
//                    if(cuentaRx==1000){
//                        startTime = System.nanoTime()
//                        logId.append("dato N° $nuevo Cuenta N° $cuentaRx y $total\n")
//                    }
//                    if(cuentaRx==4000){
//                        Log.e("Measure", "TASK took for " + ((System.nanoTime() - startTime) / 1000000) + "mS\n" )
//                        logId.append("dato N° $nuevo Cuenta N° $cuentaRx y $total\n")
//                    }
                }
            }
            return "terminamo"
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
        val buf:ByteArray = ByteArray(480*2)
        val recv = DatagramPacket(buf, buf.size)
        s.receive(recv);
        return buf  //packetAsString
    }




}