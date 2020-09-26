package com.example.treshilosrxmixerplay

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process.THREAD_PRIORITY_AUDIO
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    private val RxMaxBuf=10000
    private val power = 1
    private var bufferRx = ShortArray(RxMaxBuf)
    private var bufferPlay = ShortArray(RxMaxBuf)
    private var prueba = ByteArray(999)
    private var outBufferRx:Int=0
    private var inBufferRx:Int=0
    private var inBufferPlay:Int=0
    private var outBufferPlay:Int=0
    private var check=12


    var contador=0
    var contadorPlayin=0

    var mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, 4000,
            AudioTrack.MODE_STREAM
    )

    private val bufSin1 = createSinWaveBuffer(1000.0, 3000)
    private val bufSin2 = createSinWaveBuffer(6000.0, 1000)
    private val bufSin3 = createSinWaveBuffer(9000.0, 1000)
    private val bufSin4 = createSinWaveBuffer(12000.0, 1000)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAudioTrack.play();                 //mAudioTrack.stop();
        var startTime = System.nanoTime()
        mAudioTrack.write(bufSin1, 0, bufSin1.size,AudioTrack.WRITE_BLOCKING)
        Log.e("Measure", "TASK took write 3seg: " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")
//        startTime = System.nanoTime()
//        mAudioTrack.write(bufSin1, 0, bufSin1.size,AudioTrack.WRITE_BLOCKING)
//        Log.e("Measure", "TASK took write 3seg: " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")
//        Log.e("INICIO", "Bufer " + bufferRx[0] + "\n")

//        thread(start = true, priority = 5) { //THREAD_PRIORITY_AUDIO
//            while(power==1){
//                mAudioTrack.write(bufSin1, 0, 480,AudioTrack.WRITE_BLOCKING)
////                mAudioTrack.write(bufSin2, 0, bufSin2.size,AudioTrack.WRITE_BLOCKING)
//            }
//        }
//        thread(start = true, priority = 5) { //THREAD_PRIORITY_AUDIO
//            while(power==1){
////                Thread.sleep(500)
////                mAudioTrack.write(bufSin2, 0, bufSin2.size,AudioTrack.WRITE_BLOCKING)
//                        var startTime = System.nanoTime()
//        for (index in 0..999 step 1) {
//            bufferRx[inBufferRx] = bufSin1[index]
//            bufferRx[inBufferRx + 1] = bufSin2[index]
//            bufferRx[inBufferRx + 2] = bufSin3[index]
//            bufferRx[inBufferRx + 3] = bufSin4[index]
//            inBufferRx += 4
//            if (inBufferRx >= RxMaxBuf) {
//                inBufferRx = 0
//            }
//        }
//        Log.e("Measure", "TASK took for buffer sinwave: " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")
//            }
//        }

//        var startTime = System.nanoTime()
//        for (index in 0..999 step 1) {
//            bufferRx[inBufferRx] = bufSin1[index]
//            bufferRx[inBufferRx + 1] = bufSin2[index]
//            bufferRx[inBufferRx + 2] = bufSin3[index]
//            bufferRx[inBufferRx + 3] = bufSin4[index]
//            inBufferRx += 4
//            if (inBufferRx >= RxMaxBuf) {
//                inBufferRx = 0
//            }
//        }
//        Log.e("Measure", "TASK took for buffer sinwave: " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")
//
//        startTime = System.nanoTime()
//        for (index in 0 .. prueba.size - 2 step 2) {
//            bufferRx[inBufferRx] = (prueba[index].toShort() + (prueba[index + 1].toInt() shl 8)).toShort()
//            inBufferRx++
//            if (inBufferRx == RxMaxBuf) inBufferRx = 0
//        }
//        Log.e("Measure", "TASK took byte to short : " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")



        thread(start = true, priority = 5) //THREAD_PRIORITY_AUDIO
        {
            Log.e("INICIO", "TASK Rx\n")
            var aux=0
//            bufferRx=bufSin1.copyOf(10000)
            while (power == 1) {
                //recibo el bufferRx
//                val startTime = System.nanoTime()
//                Thread.sleep(5)
//                Log.e("Measure", "TASK took : " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")
//                val startTime = System.nanoTime()
//                for (index in 0 .. prueba.size - 2 step 2) {
//                    bufferRx[inBufferRx] = (prueba[index].toShort() + (prueba[index + 1].toInt() shl 8)).toShort()
//                    inBufferRx++
//                    if (inBufferRx == RxMaxBuf) inBufferRx = 0
//                }

//                inBufferRx+=999
//                if (inBufferRx>=RxMaxBuf){inBufferRx=0}
                var auxbufout = outBufferRx
                if (((inBufferRx+(480*4)) > auxbufout ) && (inBufferRx < auxbufout)) {
                    Log.e("ERROR", "ESTOY ALCANZANDO EL BUFFER RX\n")
//                            Log.e("ERROR", "$inBufferRx $outBufferRx\n")
                }
                else {
//                    var startTime = System.nanoTime()
                    for (index in 0..480*4 step 1) {
                        bufferRx[inBufferRx] = bufSin1[index]
//                        bufferRx[inBufferRx + 1] = bufSin2[index]
//                        bufferRx[inBufferRx + 2] = bufSin3[index]
//                        bufferRx[inBufferRx + 3] = bufSin4[index]
                        inBufferRx += 4
                        if (inBufferRx >= RxMaxBuf) {inBufferRx = 0 }
                    }
//                    aux++
//                    aux%=480
//                    Log.e("Measure", "TASK took for buffer sinwave: " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")
                }
//                Log.e("Measure", "TASK took : " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")
            }
        }
        thread(start = true, priority = 5) //THREAD_PRIORITY_AUDIO
        {
            var auxBin=0
            while (power == 1) {
//                Thread.sleep(0,10)
                var auxbufout = outBufferPlay
                if (((inBufferPlay+480) > auxbufout ) && (inBufferPlay < auxbufout)) {
                    Log.e("ERROR", "ESTOY ALCANZANDO EL BUFFER PLAY\n")
//                            Log.e("ERROR", "$inBufferRx $outBufferRx\n")
                }
                else {
                    auxBin=inBufferRx
                    if (auxBin != outBufferRx) {
                        addAudioTrack()
                        contadorPlayin++
                        outBufferRx += 4
                        if (outBufferRx >= RxMaxBuf) outBufferRx = 0
                        inBufferPlay++
                        if (inBufferPlay == RxMaxBuf) inBufferPlay = 0
                    }
                }
//                if (inBufferPlay > outBufferPlay + 999) {
//                    check_buton()
//                    prueba = mAudioTrack.write(bufferPlay, outBufferPlay, 999,AudioTrack.WRITE_BLOCKING)
//                    contador += prueba
//                    outBufferPlay += 999
//
//                } else {
//                    if (inBufferPlay == 0 && RxMaxBuf - outBufferPlay < 999) {
//                        prueba =
//                            mAudioTrack.write(bufferPlay, outBufferPlay, RxMaxBuf - outBufferPlay,AudioTrack.WRITE_BLOCKING)
//                        contador += prueba
//                        outBufferPlay = 0
//
//                    }
//                }
            }
        }
        thread(start = true, priority = THREAD_PRIORITY_AUDIO) //THREAD_PRIORITY_AUDIO
        {
            var prueba:Int=0
            var auxBin=0
            while (power == 1) {
                auxBin=inBufferPlay
                if (auxBin > outBufferPlay + 480 && outBufferPlay < RxMaxBuf-480) {
                    check_buton()
                    prueba = mAudioTrack.write(bufferPlay, outBufferPlay, 480,AudioTrack.WRITE_BLOCKING)
                    contador += prueba
                    outBufferPlay += 480

                } else {
                    if (auxBin < outBufferPlay && RxMaxBuf - outBufferPlay < 480) {
                        prueba =
                            mAudioTrack.write(bufferPlay, outBufferPlay, RxMaxBuf - outBufferPlay,AudioTrack.WRITE_BLOCKING)
                        contador += prueba
                        outBufferPlay = 0

                    }
                    if((auxBin < outBufferPlay && RxMaxBuf - outBufferPlay > 480)){
                        prueba = mAudioTrack.write(bufferPlay, outBufferPlay, 480,AudioTrack.WRITE_BLOCKING)
                        contador += prueba
                        outBufferPlay += 480
                    }

                }
            }
        }
        thread(start = true, priority = THREAD_PRIORITY_AUDIO) //THREAD_PRIORITY_AUDIO
        {
            var heaadpos=0
            while(power==1){
                Thread.sleep(10000)

                val asd=bufferPlay.copyOf(RxMaxBuf)
                val asdrx=bufferRx.copyOf(RxMaxBuf)
                Log.e("Size", "playbackHeadPosition: "+ (mAudioTrack.playbackHeadPosition-heaadpos)+ "\n")
                Thread.sleep(10)
                Log.e("Size", "playbackHeadPosition despues de sleep: "+ (mAudioTrack.playbackHeadPosition)+ "\n")
                heaadpos=mAudioTrack.playbackHeadPosition
                Log.e("CUENTA", "Muestras Cargadas en AudioTrack $contador \n")
                Log.e("CUENTA", "Muestras Cargadas en BufferPlay $contadorPlayin \n")
            }
        }

    }

    fun check_buton(){
        if (switch1.isChecked) {
            if (switch2.isChecked) {
                if (switch3.isChecked) {
                    if (switch4.isChecked) {
                        //TODOS
                        check=1
                    } else {
                        //4 NO
                        check=2
                    }
                } else {
                    if (switch4.isChecked) {
                        //3 NO
                        check=3
                    } else {
                        //3 4 NO
                        check=6
                    }
                }
            } else {
                if (switch3.isChecked) {
                    if (switch4.isChecked) {
                        // 2 NO
                        check=4
                    } else {
                        check=7
                        //2 4 NO
                    }
                } else {
                    if (switch4.isChecked) {
                        // 2 3 NO
                        check=8
                    } else {
                        // 2 3 4 NO
                        check=12
                    }
                }
            }
        } else {
            if (switch2.isChecked) {
                if (switch3.isChecked) {
                    if (switch4.isChecked) {
                        //1 NO
                        check=5
                    } else {
                        //1 4 NO
                        check=9
                    }
                } else {
                    if (switch4.isChecked) {
                        // 1 3 NO
                        check=10
                    } else {
                        //1 3 4 NO
                        check=13
                    }
                }
            } else {
                if (switch3.isChecked) {
                    if (switch4.isChecked) {
                        //1 2 NO
                        check=11
                    } else {
                        //1 2 4 NO
                        check=14
                    }
                } else {
                    if (switch4.isChecked) {
                        //1 2 3 NO
                        check=15
                    } else {
                        //1 2 3 4 NO
                        check=16
                    }
                }
            }
        }
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

    private fun createSinWaveBuffer(freq: Double, ms: Int, sampleRate: Int = 48000): ShortArray {
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