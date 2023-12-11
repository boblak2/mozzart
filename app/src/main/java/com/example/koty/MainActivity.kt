package com.example.koty

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object{
        val tag = "MainActivity"
        fun myData(){
            println("test myData fun")
        }
    }

    private var adapter: ItemAdapter? = null
    private var player: MediaPlayer? = null
    private var selItem: Item? = null
    private var index: Int? = null
    private val data: ArrayList<Item> = arrayListOf<Item>()
    private var length: Int? = null
    private var btnPlay: Button? = null
    private var seekbar: SeekBar? = null
    private var isPause: Boolean = false
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listview = findViewById<ListView>(R.id.listview)
        btnPlay = findViewById(R.id.btnPlay)
        seekbar = findViewById(R.id.seekbar)

        adapter = ItemAdapter(this, R.layout.row_item, data)
        listview.adapter = adapter
        listview.setOnItemClickListener { parent, view, position, id ->
            selItem = data[position]
            selItem?.let {
                index = position
                play(it.path.toString())
            }
        }

        //Read from Music folder
        val pathDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        if (pathDir.exists()){
            for (it in pathDir.listFiles()){
                if (it.name.lowercase().endsWith("mp3") || it.name.lowercase().endsWith("m4a")){
                    val el = Item()
                    el.name = it.name
                    el.path = "${it.path}"
                    data.add(el)
                }
            }
        }
        adapter?.notifyDataSetChanged()

        //Set index
        if (data.isNotEmpty()){
            index = 0
            selItem = data[index!!]
        }

        //with apply also run?(with+let) let?

        //seekbar?.setOnSeekBarChangeListener = SeekBar.OnSeekBarChangeListener{
        //}

        val seekBarListener = object :SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    player?.let {
                        it.seekTo(progress)
                        startTimer()
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                println("onstart")
                stopTimer()
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                println("onstop")
            }
        }
        seekbar?.setOnSeekBarChangeListener(seekBarListener)
    }

    private fun initPlayer(){
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setWakeMode(this@MainActivity, PowerManager.PARTIAL_WAKE_LOCK)
            //prepare()
            //start()
        }
        player?.setOnCompletionListener {
            println("song finished")

            if (index!! == data.size-1){
                //last
                index = 0
                selItem = data[index!!]
                adapter?.setSelected(index!!)
                adapter?.notifyDataSetChanged()
                stop()
            }else if (index!! <= data.size-2){
                //predzadnji
                index = index!! + 1
                selItem = data[index!!]
                play(selItem?.path!!)
            }
        }
    }

    private fun play(url: String){

        index?.let {
            adapter?.setSelected(it)
            adapter?.notifyDataSetChanged()
        }

        stopTimer()

        if (player != null && player!!.isPlaying()){
            player?.stop()
            player?.reset()
            player?.release()
        }
        initPlayer()
        player?.setDataSource(url)
        player?.prepare()
        player?.start()
        extractImage()
        isPause = false
        btnPlay?.setBackgroundResource(R.drawable.ic_pause_foreground)

        //Seekbar
        selItem?.let {
            seekbar?.max = player!!.duration
            seekbar?.progress = 0

            startTimer()
        }
    }

    private fun stop(){
        stopTimer()
        player?.stop()
        isPause = false
        seekbar?.progress = 0
        btnPlay?.setBackgroundResource(R.drawable.ic_play_foreground)
    }

    private fun pause(){
        player?.let {
            if (it.isPlaying()){
                it.pause()
                length = it.currentPosition
                isPause = true
                btnPlay?.setBackgroundResource(R.drawable.ic_play_foreground)
            }
        }
    }

    private fun resume(){
        player?.let {
            length?.let {
                player?.seekTo(length!!)
                player?.start()
                length = null
                isPause = false;
                btnPlay?.setBackgroundResource(R.drawable.ic_pause_foreground)
            }
        }
    }

    fun didPressBtn(view: View) {
        when(view.id){
            R.id.btnPlay -> {

                selItem?.let {

                    if (player == null){
                        play(selItem?.path.toString())
                    }else if (player!!.isPlaying)
                        pause()
                    else if (isPause)
                        resume()
                    else
                        play(selItem?.path.toString())
                }
            }

            R.id.btnStop -> {
                stop()
            }

            R.id.btnPrev -> {
                index?.let {
                    if (it == 0)
                        return

                    index = it - 1
                    selItem = data[index!!]
                    play(selItem?.path!!)
                }
            }

            R.id.btnNext -> {
                index?.let {
                    if (it >= data.size-1)
                        return
                    index = it + 1
                    selItem = data[index!!]
                    play(selItem?.path!!)
                }
            }
        }
    }

    private fun extractImage(){
        if (selItem == null) return
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(selItem?.path)
        val bytes = mmr.embeddedPicture
        //var artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val img = findViewById<ImageView>(R.id.imgThumb)
        if (bytes != null && bytes.isNotEmpty()){
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            img.setImageBitmap(bitmap)
        }else {
            img.setImageBitmap(null)
            img.setBackgroundResource(R.drawable.ic_next_foreground)
            Toast.makeText(this, "Image not found", Toast.LENGTH_LONG).show()
        }
        //artist?.let { Toast.makeText(this, "Artist $artist", Toast.LENGTH_LONG).show() }
    }


    private fun startTimer(){
        stopTimer()
        timer = Timer()
        timer?.start()
    }

    private fun stopTimer(){
        timer?.let {
            it.stopTimer()
            it.join()
            timer = null
        }
    }

    inner class Timer : Thread(){
        @Volatile private var isRunning: Boolean = true
        override fun run() {
            super.run()

            try {
                while (isRunning){
                    Thread.sleep(500)
                    player?.let {

                        seekbar?.progress = it.currentPosition
                    }
                }
            }catch (e: InterruptedException){
                isRunning = false
            }
        }

        fun stopTimer(){
            isRunning = false
            interrupt()
        }
    }
}

