package com.example.videoplayer

import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat.Surface
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.videoplayer.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(),SurfaceHolder.Callback {
    private var binding: ActivityMainBinding? = null
    private var mediaPlayer: MediaPlayer? = null
    private var videoJob: Job? = null
    private var playingSpeed = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.vdVideoSurface?.holder?.addCallback(this)


    }

    private fun setUpButtons(mp: MediaPlayer) {
        binding?.btnPlay?.setOnClickListener{
            if (mp.isPlaying){
                stopVideo(mp)
                binding?.btnPlay?.setBackgroundResource(R.drawable.ic_play)
            }else{
                startVideo(mp)
                binding?.btnPlay?.setBackgroundResource(R.drawable.ic_pause)
            }
        }

        binding?.btnForward?.setOnClickListener{
            playingSpeed += .1f
            changePlaybackSpeed(playingSpeed,mp)

        }
        binding?.btnBackward?.setOnClickListener{
            playingSpeed -= .1f
            changePlaybackSpeed(playingSpeed,mp)
        }

        binding?.sbVideoSeekbar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mp.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }


    private fun startVideo(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
        videoJob = CoroutineScope(Dispatchers.Main).launch {
            while (mediaPlayer.isPlaying) {
                binding?.sbVideoSeekbar?.progress = mediaPlayer.currentPosition
                delay(500) // Update every 0.5 seconds
            }
        }
    }
    private fun stopVideo(mediaPlayer: MediaPlayer) {
        mediaPlayer.pause()
        videoJob?.cancel()
    }
    private fun changePlaybackSpeed(speed: Float,mediaPlayer: MediaPlayer) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(speed)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mediaPlayer = MediaPlayer().apply {
            setDisplay(holder) // Set the SurfaceHolder for video display
            val afd: AssetFileDescriptor = resources.openRawResourceFd(R.raw.lal_tamatar)
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            prepare() // Prepare the MediaPlayer synchronously
            afd.close() // Close the AssetFileDescriptor after setting the data source
            binding?.sbVideoSeekbar?.max = duration
        }

        mediaPlayer?.let { mp ->
            setUpButtons(mp)
        }
    }


    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mediaPlayer?.release()
        videoJob?.cancel()
    }
}