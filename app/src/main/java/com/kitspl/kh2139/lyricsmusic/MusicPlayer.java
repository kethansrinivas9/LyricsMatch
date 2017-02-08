package com.kitspl.kh2139.lyricsmusic;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.File;
import java.util.ArrayList;

public class MusicPlayer extends AppCompatActivity implements View.OnClickListener{
    static MediaPlayer mediaPlayer;
    ArrayList<File> songNames;
    int position;
    SeekBar seekBar;
    Thread updateSeekBar;
    Button playPause,fastBackward,fastForward,nextButton, prevButton;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        playPause = (Button)findViewById(R.id.playPause);
        fastBackward = (Button)findViewById(R.id.fastBackward);
        fastForward = (Button)findViewById(R.id.fastForward);
        nextButton = (Button)findViewById(R.id.nextButton);
        prevButton = (Button)findViewById(R.id.prevButton);

        playPause.setOnClickListener(this);
        fastForward.setOnClickListener(this);
        fastBackward.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);

        changeSeekBarPosition();
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songNames = (ArrayList) bundle.getParcelableArrayList("songlist");
        position = bundle.getInt("pos",0);

        Uri uri = Uri.parse(songNames.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();
        updateSeekBar.start();
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    @Override
    public void onClick(View view) {
        int option = view.getId();
        switch (option){
            case R.id.playPause:
                if(mediaPlayer.isPlaying()) {
                    playPause.setBackgroundResource(R.drawable.ic_media_play);
                    mediaPlayer.pause();
                }
                else{
                    playPause.setBackgroundResource(R.drawable.ic_media_pause);
                    mediaPlayer.start();
                }
                break;
            case R.id.fastForward:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+5000);
                break;
            case R.id.fastBackward:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-5000);
                break;
            case R.id.nextButton:
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (position+1)%songNames.size();
                Uri uri = Uri.parse(songNames.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                mediaPlayer.start();
                break;
            case R.id.prevButton:
                mediaPlayer.stop();
                mediaPlayer.release();
                if(position!=0)
                    position = (position-1)%songNames.size();
                else
                    position = songNames.size()-1;
                uri = Uri.parse(songNames.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                mediaPlayer.start();
                break;
        }
    }

    public void changeSeekBarPosition(){
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        updateSeekBar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                while (currentPosition<totalDuration){
                    try{
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}