package com.kitspl.kh2139.lyricsmusic;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MusicPlayer extends AppCompatActivity implements View.OnClickListener{
    static MediaPlayer mediaPlayer;
    ArrayList<String> songNames;
    int position;
    SeekBar seekBar;
    Thread updateSeekBar;
    Button playPause,fastBackward,fastForward,nextButton, prevButton;
    private static final String TAG = "MainActivity";
    TextView currentTime,totalTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);


        playPause = (Button)findViewById(R.id.playPause);
        fastBackward = (Button)findViewById(R.id.fastBackward);
        fastForward = (Button)findViewById(R.id.fastForward);
        nextButton = (Button)findViewById(R.id.nextButton);
        prevButton = (Button)findViewById(R.id.prevButton);
        currentTime = (TextView)findViewById(R.id.currentTime);
        totalTime = (TextView)findViewById(R.id.totalTime);

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
        songNames = (ArrayList) bundle.getParcelableArrayList("songNames");
        position = bundle.getInt("pos",0);

        Uri uri;
        uri = getUriBasedOnSongNumber(position);
        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();
        updateSeekBar.start();
        setTitle(songNames.get(position));
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
        Uri uri;
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
                uri = getUriBasedOnSongNumber(++position);
                position = getPosition(position);
                setTitle(songNames.get(position));
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                int totalDuration = mediaPlayer.getDuration();
                totalTime.setText(milliSecondsToTime(totalDuration));
                mediaPlayer.start();
                break;
            case R.id.prevButton:
                mediaPlayer.stop();
                mediaPlayer.release();
                uri = getUriBasedOnSongNumber(--position);
                position = getPosition(position);
                setTitle(songNames.get(position));
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                totalDuration = mediaPlayer.getDuration();
                totalTime.setText(milliSecondsToTime(totalDuration));
                mediaPlayer.start();
                break;
        }
    }

    public Uri getUriBasedOnSongNumber(int position){
        Uri uri;
        ArrayList<String> allSongsUri = findAllSongsUri();
        ArrayList<File> allDeviceSongs = findSongsOnDevice(Environment.getExternalStorageDirectory());
        position = getPosition(position);
        if(position < allSongsUri.size())
            uri = Uri.parse(allSongsUri.get(position));
        else {
            position -= allSongsUri.size();
            uri = Uri.parse(allDeviceSongs.get(position).toString());
        }
        return uri;
    }

    public int getPosition(int position){
        ArrayList<String> allSongsUri = findAllSongsUri();
        ArrayList<File> allDeviceSongs = findSongsOnDevice(Environment.getExternalStorageDirectory());
        position = (position)%(allSongsUri.size()+allDeviceSongs.size());
        if(position<0)
            position = allSongsUri.size()+allDeviceSongs.size()-1;
        return position;
    }

    public void changeSeekBarPosition(){
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#FFA500"), PorterDuff.Mode.SRC_IN);
        updateSeekBar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                totalTime.setText(milliSecondsToTime(totalDuration));
                int currentPosition = 0;
                while (currentPosition<totalDuration){
                    try{
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        currentTime.setText(milliSecondsToTime(currentPosition));
                        seekBar.setProgress(currentPosition);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    /**
     * Method to get the time in minutes format to display song status
     * @param milliseconds
     * @return
     */
    public String milliSecondsToTime(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

    /**
     * Method to return all Songs Uri to get the file path
     * @return arraylist of Uri
     */
    public ArrayList<String> findAllSongsUri(){
        ContentResolver cr = this.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        ArrayList<String> songList = new ArrayList<>();
        int count = 0;
        if(cur != null)
        {
            count = cur.getCount();
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    songList.add(data);
                }
            }
        }
        cur.close();
        return songList;
    }


    /**
     * method to get the song files on a device
     * @param storageDirectory
     * @return
     */
    public ArrayList<File> findSongsOnDevice(File storageDirectory){
        ArrayList<File> fileList = new ArrayList<>();
        File[] files = storageDirectory.listFiles();
        System.out.print("file lenght:");
        System.out.print(files.length);
        for(File singleFile: files){
            if(singleFile.isDirectory() && !singleFile.isHidden()){
                fileList.addAll(findSongsOnDevice(singleFile));
            }
            else{
                if(singleFile.getName().endsWith(".mp3"))
                    fileList.add(singleFile);
            }
        }
        return fileList;
    }
}
