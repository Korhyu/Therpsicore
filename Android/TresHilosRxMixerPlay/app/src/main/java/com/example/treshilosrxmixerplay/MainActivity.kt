package com.example.treshilosrxmixerplay

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process.THREAD_PRIORITY_AUDIO
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    private val RxMaxBuf=10000
    private val power = 1
    private var bufferRx = ShortArray(RxMaxBuf)
    private var bufferPlay = ShortArray(RxMaxBuf)
    private var prueba = ByteArray(16*16*2)
    private var outBufferRx:Int=0
    private var inBufferRx:Int=0
    private var inBufferPlay:Int=0
    private var outBufferPlay:Int=0
    private var check=1

    var mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, 2024,
            AudioTrack.MODE_STREAM
    )

    private val bufSin1 = createSinWaveBuffer(3000.0, 1000)
    private val bufSin2 = createSinWaveBuffer(6000.0, 1000)
    private val bufSin3 = createSinWaveBuffer(9000.0, 1000)
    private val bufSin4 = createSinWaveBuffer(12000.0, 1000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAudioTrack.play();                 //mAudioTrack.stop();

//        prueba[0] = 0x2
//        prueba[1] = 0x2
//        bufferRx[0] = (prueba[0].toShort() + (prueba[1].toInt() shl 8)).toShort()
        Log.e("INICIO", "Bufer " + bufferRx[0] + "\n")


        thread(start = true, priority = THREAD_PRIORITY_AUDIO) //THREAD_PRIORITY_AUDIO
        {
            Log.e("INICIO", "TASK Rx\n")
            while (power == 1) {
                //recibo el bufferRx
                Thread.sleep(4)
//                val startTime = System.nanoTime()
//                for (index in 0 .. prueba.size - 2 step 2) {
//                    bufferRx[inBufferRx] = (prueba[index].toShort() + (prueba[index + 1].toInt() shl 8)).toShort()
//                    inBufferRx++
//                    if (inBufferRx == RxMaxBuf) inBufferRx = 0
//                }
                for (index in 0 .. prueba.size - 1 step 1) {
                    bufferRx[inBufferRx]=bufSin1[index]
                    inBufferRx++
                    if (inBufferRx == RxMaxBuf) inBufferRx = 0
                    bufferRx[inBufferRx]=bufSin2[index]
                    inBufferRx++
                    if (inBufferRx == RxMaxBuf) inBufferRx = 0
                    bufferRx[inBufferRx]=bufSin3[index]
                    inBufferRx++
                    if (inBufferRx == RxMaxBuf) inBufferRx = 0
                    bufferRx[inBufferRx]=bufSin4[index]
                    inBufferRx++
                    if (inBufferRx == RxMaxBuf) inBufferRx = 0
                }
//                Log.e("Measure", "TASK took : " + ((System.nanoTime() - startTime) / 1000000) + "mS\n")
            }
        }
        thread(start = true, priority = THREAD_PRIORITY_AUDIO) //THREAD_PRIORITY_AUDIO
        {
            Log.e("INICIO", "TASK Mixer\n")
            while (power == 1) {

                if(inBufferRx!=outBufferRx) {

                    addAudioTrack()
                    inBufferPlay++
                    if (inBufferPlay == RxMaxBuf) inBufferPlay = 0
                    outBufferRx+=4
                    if (outBufferRx >= RxMaxBuf) outBufferRx = 0
                }
                else{
                    Thread.sleep(1)
                }

            }
        }
        thread(start = true, priority = THREAD_PRIORITY_AUDIO) //THREAD_PRIORITY_AUDIO
        {
            Log.e("INICIO", "TASK Play\n")
            while (power==1){
                check_buton()
                if(inBufferPlay!=outBufferPlay) {
                    if(inBufferPlay<outBufferPlay){
                        mAudioTrack.write(bufferPlay, outBufferPlay, RxMaxBuf-outBufferPlay)
                        outBufferPlay=0
                        mAudioTrack.write(bufferPlay, outBufferPlay, inBufferPlay)
                        outBufferPlay=inBufferPlay

                    }
                    else{
                        mAudioTrack.write(bufferPlay, outBufferPlay, inBufferPlay-outBufferPlay)
                        outBufferPlay=inBufferPlay
                    }
                }
                Thread.sleep(2)
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