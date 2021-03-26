package com.example.capstoneic;

import android.os.AsyncTask;
import android.util.Log;
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
import com.camerakit.CameraKitView;
import edu.cmu.pocketsphinx.*;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class ReadActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, RecognitionListener {

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

    /* We only need the keyphrase to start recognition, one menu with list of choices,
      and one word that is required for method switchSearch - it will bring recognizer
      back to listening for the keyphrase*/
    private static final String KWS_SEARCH = "wakeup";
    private static final String MENU_SEARCH = "menu";
    /* Keyword we are looking for to activate recognition */
    private static final String KEYPHRASE = "help me";

    /* Recognition object */
    private SpeechRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        runRecognizerSetup();
        setContentView(R.layout.activity_read);
        start = findViewById(R.id.button3);
        stop = findViewById(R.id.button4);
        pause = findViewById(R.id.button5);
        start.setOnClickListener(playOnClickListener);
        pause.setOnClickListener(pauseOnClickListener);
        stop.setOnClickListener(stopOnClickListener);
        ImageView image = findViewById(R.id.imageView);

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

        /*start.setOnClickListener(new View.OnClickListener() {
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
        });*/
    }

    private View.OnClickListener playOnClickListener = new View.OnClickListener() {
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
    };

    private View.OnClickListener pauseOnClickListener = new View.OnClickListener() {
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
        };

    private View.OnClickListener stopOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mediaPlayer.stop();
            mediaPlayer.release();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    };

    private void switchSearch(String searchName) {
        recognizer.stop();
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(ReadActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }
            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    System.out.println(result.getMessage());
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();

    }


    @Override
    public void onStop() {
        super.onStop();
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)){
            Toast.makeText(ReadActivity.this, "start", Toast.LENGTH_SHORT).show();
            switchSearch(MENU_SEARCH);
        }/*else if (text.equals("read")) {
            Toast.makeText(MainActivity.this, "Read", Toast.LENGTH_SHORT).show();
            readButton.performClick();
        }else if (text.equals("explore")) {
            Toast.makeText(MainActivity.this, "Explore", Toast.LENGTH_SHORT).show();
            exploreButton.performClick();
        } */ else {
            System.out.println(hypothesis.getHypstr());
        }
    }

    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            if (text.equals("play")) {
                Toast.makeText(ReadActivity.this, "play", Toast.LENGTH_SHORT).show();
                start.performClick();
            } else if (text.equals("pause")) {
                Toast.makeText(ReadActivity.this, "pause", Toast.LENGTH_SHORT).show();
                pause.performClick();
                System.out.println(text);
            }else if (text.equals("go back")) {
                Toast.makeText(ReadActivity.this, "go back", Toast.LENGTH_SHORT).show();
                stop.performClick();
            }
        }
    }
    @Override
    public void onError(Exception error) {
        System.out.println(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                // Disable this line if you don't want recognizer to save raw
                // audio files to app's storage
                //.setRawLogDir(assetsDir)
                .getRecognizer();
        recognizer.addListener(this);
        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        // Create your custom grammar-based search
        File menuGrammar = new File(assetsDir, "mymenu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
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