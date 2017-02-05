package com.kitspl.kh2139.lyricsmusic;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;

public class MusicPlayer extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    ArrayList<File> songNames;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songNames = (ArrayList) bundle.getParcelableArrayList("songlist");
        int position = bundle.getInt("pos",0);

        Uri uri = Uri.parse(songNames.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();
    }
}
