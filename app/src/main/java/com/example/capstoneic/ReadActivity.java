package com.example.capstoneic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class ReadActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private TextToSpeech tts;
    static private String description;
    static private MediaPlayer mediaPlayer;
    private int created;
    private int paused;
    public File file;
    private Button start;
    private Button stop;
    private Button pause;
    private ScrollView sv;
    private LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        Button start = findViewById(R.id.button3);
        Button stop = findViewById(R.id.button4);
        Button pause = findViewById(R.id.button5);
        ImageView image = findViewById(R.id.imageView);

        //sv = findViewById(R.id.scrollView);
        //ll = findViewById(R.id.linearLayout);
        //TextView tv = new TextView(this);
        //tv.setText(description);
        // ll.addView(tv);

        Bundle bundle = getIntent().getExtras();
        String photo = bundle.getString("file");
        description = bundle.getString("desc");

        Bitmap bitmap = BitmapFactory.decodeFile(photo);
        image.setImageBitmap(bitmap);
        mediaPlayer = new MediaPlayer();
        created = 0;
        paused = 0;
        tts = new TextToSpeech(this, this);
        file = new File(Environment.getExternalStorageDirectory(), "audio2.wav");
        System.out.println("~~~~~~~~~~~>"+description);

        sv = findViewById(R.id.scrollView);
        ll = findViewById(R.id.linearLayout);
        TextView tv = new TextView(getApplicationContext());
        tv.setGravity(Gravity.LEFT);
        tv.setTextSize(24);
        tv.setTypeface(null, Typeface.BOLD_ITALIC);
        //tv.setBackgroundColor(Color.rgb(173,220,240));
        tv.setText(description);
        ll.addView(tv);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(created==0) {
                    try {
                        String p = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio2.wav";
                        Uri uri = Uri.parse(p);
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(getApplicationContext(), uri);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        start.setText("Replay");

                        created = 1;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    mediaPlayer.stop();
                    try {
                        mediaPlayer.prepare();
                        pause.setText("Pause");
                        paused = 0;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(created == 1 && paused == 0) {
                    mediaPlayer.pause();
                    pause.setText("Resume");
                    paused = 1;
                }
                else{
                    mediaPlayer.start();
                    pause.setText("Pause");
                    paused = 0;
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void makeAudio(File file) {
        HashMap<String, String> params = new HashMap<>();
        if(tts.synthesizeToFile(description, params, file.getPath()) == TextToSpeech.SUCCESS){
            Toast.makeText(getBaseContext(),"Sound file created", Toast.LENGTH_SHORT).show();
            mediaPlayer = new MediaPlayer();
        }else {
            Toast.makeText(getBaseContext(),"Oops! Sound file not created",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInit(int status) {
        if(status!=TextToSpeech.ERROR){
            tts.setLanguage(Locale.ENGLISH);
        }
            makeAudio(file);

    }
}