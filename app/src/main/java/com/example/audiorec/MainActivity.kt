package com.example.audiorec

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiorec.list.Adapter
import com.example.audiorec.list.AudioRecord
import com.example.audiorec.list.OnItemClickListener
import com.example.audiorec.stopwatch.StopWatch
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.Collection


class MainActivity : AppCompatActivity(), StopWatch.OnTimerTickListener, OnItemClickListener {

    private var permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false

    private var inProcess = false
    private var inStop = false

    lateinit var btnStartPause: ImageButton
    lateinit var btnDone: ImageButton
    lateinit var btnDelete: ImageButton
    lateinit var textStopWatch: TextView

    lateinit var recorder: MediaRecorder

    private  var dirPath = ""
    private  var filename = ""
    private  var fileDate = ""
    private  var fileDuration = ""

    private lateinit var timer: StopWatch

    private lateinit var plank: BottomSheetBehavior<LinearLayout>

    private lateinit var textInputEditText: TextInputEditText

    private lateinit var btnCan: Button
    private lateinit var btnCom: Button
    private lateinit var linearLayoutPlank: LinearLayout

    lateinit var recordList: ArrayList<AudioRecord>
    lateinit var newAdapter: Adapter
    lateinit var recycleView: RecyclerView

    lateinit var mediaPlayer: MediaPlayer

    var itemIsPlayed = false
    var itemPosition = -1

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionGranted = ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED

        if(!permissionGranted) {
            ActivityCompat.requestPermissions( this, permissions, 200)
        }

        recordList = ArrayList()


        recordList.addAll(onDragList("${externalCacheDir?.absolutePath}/"))

        newAdapter = Adapter(recordList, this)

        recycleView = findViewById(R.id.recyclerView)
        recycleView.apply {
            adapter = newAdapter
            layoutManager = LinearLayoutManager(context)
        }

        prepareAllItems()


        textInputEditText = findViewById(R.id.fileDate)
        linearLayoutPlank = findViewById(R.id.botom)
        linearLayoutPlank.visibility = View.GONE
        hideKeyBoard(textInputEditText)
        plank = BottomSheetBehavior.from(findViewById(R.id.botom))
        plank.peekHeight = 0
        plank.state = BottomSheetBehavior.STATE_COLLAPSED

        textStopWatch = findViewById(R.id.text_stopwatch)

        timer = StopWatch(this)

        // Listen
        btnStartPause = findViewById(R.id.btn_start_pause)
        btnStartPause.setOnClickListener{
            if (! (inStop || inProcess)){
                onStartRec()
            }
            else if (inProcess && !inStop){
                if (timer.toString().length > 3) {
                    onPauseRec()
                }
            }
            else if (inStop && !inProcess){
                if (timer.toString().length > 3) {
                    onResumeRec()
                }
            }
            else {

            }
        }

        // Listen
        btnDone = findViewById(R.id.btn_done)
        btnDone.setOnClickListener{

            if (inProcess && !inStop || (!inProcess && inStop)){
                if (timer.toString().length > 3) {
                    onDoneRec()
                }
            }
            else{

            }
        }

        // Listen
        btnDelete = findViewById(R.id.btn_delete)
        btnDelete.setOnClickListener{
            if (inProcess && !inStop || (!inProcess && inStop)){
                if (timer.toString().length > 3) {
                    onDeleteRec()
                }
            }
        }

        btnCan = findViewById(R.id.btnCancel)
        btnCan.setOnClickListener {
            File("$dirPath$filename.mp3").delete()
            dismiss()
            linearLayoutPlank.visibility = View.GONE
        }

        btnCom = findViewById(R.id.btnComplete)
        btnCom.setOnClickListener {
            dismiss()
            save()
            linearLayoutPlank.visibility = View.GONE
        }

    }

    private fun onDragList(path: String): ArrayList<AudioRecord> {

        var array: kotlin.collections.ArrayList<AudioRecord> = ArrayList()
        var simpleDateFormat = SimpleDateFormat("dd.MM.YY HH:mm")
        File(path).walk().forEach {
            val attributes = Files.readAttributes(it.toPath(), BasicFileAttributes::class.java)
            val toName = it.name
            var toDate = simpleDateFormat.format(attributes.creationTime().toInstant().toEpochMilli())
            val toDirecory = "${externalCacheDir?.absolutePath}/" + "$toName.mp3"
            array.add(AudioRecord(toName.replace(".mp3", ""), toDate ,  "50:00", toDirecory, 0))

        }
        array.removeAt(0)
        return ArrayList(array.toList().reversed())

    }


    private fun prepareAllItems() {
    }

    private fun save() {
        val newFilename = textInputEditText.text.toString()
        if(newFilename != filename){
            var newFile = File("$dirPath$newFilename.mp3")
            File("$dirPath$filename.mp3").renameTo(newFile)
        }
        GlobalScope.launch {
            runOnUiThread {
                recordList.add(AudioRecord(newFilename, fileDate,fileDuration, "$dirPath$filename.mp3", 3))
                newAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun dismiss() {
        hideKeyBoard(textInputEditText)
        Handler(Looper.getMainLooper()).postDelayed({
            plank.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 100)
    }

    private fun onStartRec(){
        if (!permissionGranted){
            ActivityCompat.requestPermissions( this, permissions, 200)
            return
        }

        dirPath = "${externalCacheDir?.absolutePath}/"
        var simpleDateFormat = SimpleDateFormat( "dd.MM.YY HH:mm.ss")
        var date : String = simpleDateFormat.format(Date())
        filename = "$date"
        fileDate = date.substring(0,date.length-3)

        recorder = MediaRecorder()
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder. AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")
            recorder.prepare()
            recorder.start()
        }
        btnStartPause.setImageResource(R.drawable.round_pause_24)
        timer.start()

        inProcess = true
        inStop = false
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    fun onPauseRec(){
        btnStartPause.setImageResource(R.drawable.baseline_play_arrow_24)
        recorder.pause()
        timer.pause()
        inProcess = false
        inStop = true
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    fun onResumeRec(){
        btnStartPause.setImageResource(R.drawable.round_pause_24)
        inProcess = true
        inStop = false
        recorder.resume()
        timer.start()
    }

    fun onDoneRec(){
        println()
        btnStartPause.setImageResource(R.drawable.round_keyboard_voice_24)
        textStopWatch.text = "00:00:00"

        fileDuration = timer.format()

        timer.stop()
        plank.state = BottomSheetBehavior.STATE_EXPANDED
        linearLayoutPlank.visibility = View.VISIBLE
        recorder.apply {
            stop()
            release()
        }
        textInputEditText.setText(filename)
        inProcess = false
        inStop = false

    }

    fun onDeleteRec(){
        recorder.apply {
            stop()
            release()
        }
        btnStartPause.setImageResource(R.drawable.round_keyboard_voice_24)
        textStopWatch.text = "00:00:00"
        timer.stop()
        File("$dirPath$filename.mp3").delete()

        inProcess = false
        inStop = false
    }

    fun onPostRec(){

    }

    private fun hideKeyBoard(view: View){
        val pay = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        pay.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 200){
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }

    }

    override fun onTimerTick(duration: String) {
        textStopWatch.text = duration
    }

    override fun onItemClickListener(position: Int) {
        val intent = Intent(this, Player::class.java)
        println(File("$dirPath${recordList[position].name}.mp3").absolutePath.toString())
        println("sdsdfdfd")
        intent.putExtra("letter", File("$dirPath${recordList[position].name}.mp3").toString())
        this.startActivity(intent)
    }

    override fun onItemLongClickListener(position: Int) {
    }

    override fun onRestart() {
        super.onRestart()
        recordList.clear()
        newAdapter.notifyDataSetChanged()
        recordList.addAll(onDragList("${externalCacheDir?.absolutePath}/"))
        newAdapter.notifyDataSetChanged()
    }

}