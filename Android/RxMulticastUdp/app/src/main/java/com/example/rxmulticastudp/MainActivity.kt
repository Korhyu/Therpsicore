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

class MainActivity : AppCompatActivity() {

    private val power = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rxRcv().execute()


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
                while (power == 1) {

                        datoCrudo = receive(s)

                        var startTime = System.nanoTime()
                        datoCrudo = receive(s)
                        logId.append("dato" + datoCrudo[0] + datoCrudo[1]+"\n")
                        Log.e("Measure", "TASK took for " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")

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
        val buf:ByteArray = ByteArray(3840*2)
        val recv = DatagramPacket(buf, buf.size)
        s.receive(recv);
        return buf  //packetAsString
    }




}