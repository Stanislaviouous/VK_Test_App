package com.example.audiorec

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File


class Player : AppCompatActivity() {

    private var player: ExoPlayer? = null

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L
    private lateinit var video_view: PlayerView

    @SuppressLint("UnsafeOptInUsageError")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        video_view = findViewById(R.id.video_view)

        var letterId = File(intent?.extras?.getString("letter"))
        val mediaUri = Uri.fromFile(letterId)
        player = ExoPlayer.Builder(this)
            .build()
        video_view.player = player
        val mediaItem: MediaItem = MediaItem.fromUri(mediaUri)
        player!!.setMediaItem(mediaItem)
        player!!.prepare()
        player!!.play()
    }
}