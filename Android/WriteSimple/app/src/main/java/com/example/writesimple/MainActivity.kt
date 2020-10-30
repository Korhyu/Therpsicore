package com.example.writesimple

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Process.THREAD_PRIORITY_AUDIO
import android.os.Process.setThreadPriority
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.System.arraycopy
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import kotlin.concurrent.thread
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    private val RxMaxBuf=120*16*10
    private val power = 1
    private var bufferRx = ByteArray(48100)
    private var bufferPlay = ShortArray(RxMaxBuf)
    private var prueba = ByteArray(999)
    private var outBufferRx:Int=0
    private var inBufferRx:Int=0
    private var countBufferRx:Int=0
    private var inBufferPlay:Int=0
    private var outBufferPlay:Int=0
    private var countBufferPlay:Int=0
    private var check=12
    var datoCrudo=ByteArray(500*2)
    var total=0
    var recp=0
    var plays=0
    var finalTime=0
    var carga=0

    var mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC, 40000, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, 2000,
            AudioTrack.MODE_STREAM
    )

    private val bufSin1 = createSinWaveBuffer(1000.0, 3000)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAudioTrack.play();                 //mAudioTrack.stop();
        Log.e("ERROR", "stado atrack ${mAudioTrack.state} y ${AudioTrack.PLAYSTATE_PLAYING}\n")
        var startTime = System.nanoTime()
        mAudioTrack.write(bufSin1, 0, bufSin1.size,AudioTrack.WRITE_BLOCKING)
        Log.e("ERROR", "stado atrack ${mAudioTrack.state} y ${AudioTrack.PLAYSTATE_PLAYING}\n")

        Log.e("Measure", "TASK took write 3seg: " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")
        rxRcv().execute()

        thread(start = true, priority = 5){ //THREAD_PRIORITY_AUDIO
            while (power==1){
//                check_buton()
                Thread.sleep(1000)
                Log.e("MEDIDA", "TOTAL $total \n")
//                Log.e("MEDIDA", "RECP $recp \n")
                Log.e("MEDIDA", "plays $plays \n")
                Log.e("MEDIDA", "countBufferPlay $countBufferPlay \n")
//                Log.e("MEDIDA", "tiempo de write $finalTime \n")
//                Log.e("MEDIDA", "tiempo de carga $carga \n")
            }
        } //Hilo para revizar los Cheklist de los canales

        thread(start = true, priority = THREAD_PRIORITY_AUDIO) { //THREAD_PRIORITY_AUDIO
            while(power==1){
                if(recp>0) {
                    arraycopy(bufSin1,0,bufferPlay,inBufferPlay,120)
                    inBufferPlay+=120
                    inBufferPlay %= RxMaxBuf
//                    for (index in outBufferRx+2..(481 * 2 - 2) step 8) {
//                        bufferPlay[inBufferPlay] = ((bufferRx[index].toUByte().toInt() + (bufferRx[index+1].toInt() shl 8))- 2047).toShort()
//                           // ((datoCrudo[index].toInt() + (datoCrudo[index + 1].toInt() shl 8))- 2047).toShort()  // ((((datoCrudo[index].toShort() + (datoCrudo[index + 1].toInt() shl 8)).toShort()) - 2047) * 16).toShort()
//                        inBufferPlay++
//                        inBufferPlay %= RxMaxBuf
//                    }
                    outBufferRx += 481*2
                    outBufferRx %= 48100//if (outBufferRx>RxMaxBuf*8){outBufferRx=0}//
                    recp--
                    countBufferPlay++

                }
            }
        }

        thread(start = true, priority = THREAD_PRIORITY_AUDIO){ //THREAD_PRIORITY_AUDIO
            while(power==1){
                if(countBufferPlay>=9) {
//                    var startTime = System.nanoTime()
                        mAudioTrack.write(bufferPlay, outBufferPlay, 120*8, AudioTrack.WRITE_BLOCKING)
//                    Log.e("Measure", "TASK took nada " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")
                        plays++
                        outBufferPlay += 120*8
                        outBufferPlay %= RxMaxBuf
                        countBufferPlay -= 8

                }
//                else{Log.e("FAlTA BUFF PLAY", "$countBufferPlay \n")}
            }
        }//Hilo que toma los datos del Buffer y los reproduce. La fucion Write es bloqueante a si que este Hilo solo se encarga de esto.
    }

    internal inner class rxRcv : AsyncTask<Void, Void, String>() {

        var hasInternet = false

        override fun doInBackground(vararg p0: Void?): String? {
            setThreadPriority(-16)
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



                while (power == 1) {
//                    if (countBufferRx < 10) {
//                        var startTime = System.nanoTime()
//                        Log.e("Measure", "TASK took nada " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")
                        datoCrudo = receive(s)

                        arraycopy(datoCrudo,0,bufferRx,inBufferRx,481*2)

                        recp++
                        total++
                        inBufferRx+=481*2
                        inBufferRx %= 48100//if(inBufferRx>=RxMaxBuf*8){inBufferRx==0}//
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
        val buf = ByteArray(481*2)
        val recv = DatagramPacket(buf, buf.size)
        s.receive(recv);
        return buf  //packetAsString
    }

    fun addAudioTrack() {
        when (check) {
            1 -> {
                //canales:1 2 3 4
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx]+bufferRx[outBufferRx+1]+bufferRx[outBufferRx+2]+bufferRx[outBufferRx+3])-8190)*4).toShort()
            }
            2 -> {
                //canales: 1 2 3
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx]+bufferRx[outBufferRx+1]+bufferRx[outBufferRx+2])-6142)*5.33).toShort()

            }
            3 -> {
                //canales: 1 2 4
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx]+bufferRx[outBufferRx+1]+bufferRx[outBufferRx+3])-6142)*5.33).toShort()

            }
            4 -> {
                //canales: 1 3 4
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx]+bufferRx[outBufferRx+2]+bufferRx[outBufferRx+3])-6142)*5.33).toShort()

            }
            5 -> {
                //canales: 2 3 4
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx+1]+bufferRx[outBufferRx+2]+bufferRx[outBufferRx+3])-6142)*5.33).toShort()

            }
            6 -> {
                //canales: 1 2
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx]+bufferRx[outBufferRx+1])-4095)*8).toShort()

            }
            7 -> {
                //canales: 1 3
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx]+bufferRx[outBufferRx+2])-4095)*8).toShort()

            }
            8 -> {
                //canales: 1 4
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx]+bufferRx[outBufferRx+3])-4095)*8).toShort()

            }
            9 -> {
                //canales: 2 3
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx+1]+bufferRx[outBufferRx+2])-4095)*8).toShort()

            }
            10 -> {
                //canales: 2 4
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx+1]+bufferRx[outBufferRx+3])-4095)*8).toShort()

            }
            11 -> {
                //canales: 3 4
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx+2]+bufferRx[outBufferRx+3])-4095)*8).toShort()

            }
            12 -> {
                //canales: 1
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx])-2047)*16).toShort()

            }
            13 -> {
                //canales: 2
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx+1])-2047)*16).toShort()

            }
            14 -> {
                //canales: 3
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx+2])-2047)*16).toShort()

            }
            15 -> {
                //canales: 4
                bufferPlay[inBufferPlay]= (((bufferRx[outBufferRx+3])-2047)*16).toShort()

            }
            16 -> {
                //ningun canal
                bufferPlay[inBufferPlay]=0
            }
            else -> { // Note the block
                print("error en los los canales")
            }
        }

    }

//    fun check_buton(){
//        if (switch1.isChecked) {
//            if (switch2.isChecked) {
//                if (switch3.isChecked) {
//                    if (switch4.isChecked) {
//                        //TODOS
//                        check=1
//                    } else {
//                        //4 NO
//                        check=2
//                    }
//                } else {
//                    if (switch4.isChecked) {
//                        //3 NO
//                        check=3
//                    } else {
//                        //3 4 NO
//                        check=6
//                    }
//                }
//            } else {
//                if (switch3.isChecked) {
//                    if (switch4.isChecked) {
//                        // 2 NO
//                        check=4
//                    } else {
//                        check=7
//                        //2 4 NO
//                    }
//                } else {
//                    if (switch4.isChecked) {
//                        // 2 3 NO
//                        check=8
//                    } else {
//                        // 2 3 4 NO
//                        check=12
//                    }
//                }
//            }
//        } else {
//            if (switch2.isChecked) {
//                if (switch3.isChecked) {
//                    if (switch4.isChecked) {
//                        //1 NO
//                        check=5
//                    } else {
//                        //1 4 NO
//                        check=9
//                    }
//                } else {
//                    if (switch4.isChecked) {
//                        // 1 3 NO
//                        check=10
//                    } else {
//                        //1 3 4 NO
//                        check=13
//                    }
//                }
//            } else {
//                if (switch3.isChecked) {
//                    if (switch4.isChecked) {
//                        //1 2 NO
//                        check=11
//                    } else {
//                        //1 2 4 NO
//                        check=14
//                    }
//                } else {
//                    if (switch4.isChecked) {
//                        //1 2 3 NO
//                        check=15
//                    } else {
//                        //1 2 3 4 NO
//                        check=16
//                    }
//                }
//            }
//        }
//    }


    private fun createSinWaveBuffer(freq: Double, ms: Int, sampleRate: Int = 40000): ShortArray {
        val samples = (ms * sampleRate / 1000)
        val output = ShortArray(samples)
        val period = sampleRate.toDouble() / freq
        for (i in output.indices) {
            val angle = 2.0 * Math.PI * i.toDouble() / period
            output[i] = ((sin(angle) * 2047f)+2047).toShort()
        }
        //output.forEach { println(it) }
        return output
    }




}