package com.example.capstoneic;

import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.camerakit.CameraKitView;

import edu.cmu.pocketsphinx.*;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.aprilapps.easyphotopicker.EasyImage;

public class MainActivity extends AppCompatActivity implements RecognitionListener {
    private CameraKitView cameraKitView;
    private Button readButton;
    private Button exploreButton;
    private ProgressDialog pd;
    private ImageButton gallery;
    private int option;
    private final int RESULT_IMG = 1;
    private String real_path;

    private String url = "http://"+"192.168.0.195"+":"+5000+"/";

    /* We only need the keyphrase to start recognition, one menu with list of choices,
      and one word that is required for method switchSearch - it will bring recognizer
      back to listening for the keyphrase*/
    private static final String KWS_SEARCH = "wakeup";
    private static final String MENU_SEARCH = "menu";
    /* Keyword we are looking for to activate recognition */
    private static final String KEYPHRASE = "hello";

    /* Recognition object */
    private SpeechRecognizer recognizer;

    //private static final int SPEECH_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        runRecognizerSetup();

        setContentView(R.layout.activity_main);
        cameraKitView = findViewById(R.id.camera);
        readButton = findViewById(R.id.button);
        exploreButton = findViewById(R.id.button2);
        gallery = findViewById(R.id.gallery);
        readButton.setOnClickListener(photoOnClickListener);
        exploreButton.setOnClickListener(objectOnClickListener);
        gallery.setOnClickListener(optionsOnClickListener);

        //displaySpeechRecognizer();
    }

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
                    Assets assets = new Assets(MainActivity.this);
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
            Toast.makeText(MainActivity.this, "start", Toast.LENGTH_SHORT).show();
            switchSearch(MENU_SEARCH);
        }else if (text.equals("read")) {
            Toast.makeText(MainActivity.this, "Read", Toast.LENGTH_SHORT).show();
            readButton.performClick();
        }else if (text.equals("explore")) {
            Toast.makeText(MainActivity.this, "Explore", Toast.LENGTH_SHORT).show();
            exploreButton.performClick();
        } else if (text.equals("good morning")) {
            System.out.println("Good morning to you too!");
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
            System.out.println(hypothesis.getHypstr());
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

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private File savedPhoto;

    private View.OnClickListener optionsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(MainActivity.this, gallery);
            popup.getMenuInflater().inflate(R.menu.gallery_options, popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId()==R.id.one) {
                        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        i.setType("image/*");
                        option = 1;
                        startActivityForResult(i, RESULT_IMG);
                        return true;
                    }
                    else{
                        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        i.setType("image/*");
                        option = 2;
                        startActivityForResult(i, RESULT_IMG);
                        return true;
                    }
                }
            });

            popup.show();
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        /*if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            System.out.println("{{{{{{{{{{{{{ "+spokenText);
            // Do something with spokenText
        }
        super.onActivityResult(requestCode, resultCode, data);*/

        System.out.println("resultcode "+resultCode+", requestcode "+requestCode);
        switch (requestCode){
            case RESULT_IMG:
                if(resultCode==RESULT_OK){
                    try{
                        Uri imguri = data.getData();
                        String path = imguri.toString();
                        System.out.println("####path"+path);
                        real_path = getRealPathFromUri(MainActivity.this, imguri);
                        System.out.println(real_path);
                        getLoading();
                        savedPhoto = new File(real_path);
                        if(option == 1)
                            postRequest(savedPhoto, url+"ocr");
                        else
                            postRequest(savedPhoto, url+"caption");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    private String getRealPathFromUri(Context context, Uri imguri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(imguri, proj, null, null, null);
            int colind = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(colind);
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }
    }

    private View.OnClickListener objectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                @Override
                public void onImage(CameraKitView cameraKitView, final byte[] photo) {
                    getLoading();
                    savedPhoto = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
                    try {
                        FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                        outputStream.write(photo);
                        System.out.println(Arrays.toString(photo));
                        outputStream.close();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                        Log.e("CKDemo", "Exception in photo callback");
                    }
                    postRequest(savedPhoto, url+"caption");
                }
            });
        }
    };

    private View.OnClickListener photoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                @Override
                public void onImage(CameraKitView cameraKitView, final byte[] photo) {
                    getLoading();
                    savedPhoto = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
                    try {
                        FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                        System.out.println("______________>>"+savedPhoto.getPath());
                        outputStream.write(photo);
                        System.out.println(Arrays.toString(photo));
                        outputStream.close();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                        Log.e("CKDemo", "Exception in photo callback");
                    }
                    postRequest(savedPhoto, url+"ocr");
                }
            });
        }
    };

    public void getLoading(){
        pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("Obtaining the results...");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setIndeterminate(true);
        pd.setProgress(0);
        pd.show();
    }

    public void openNewActivity(String description){
        Intent intent = new Intent(this, ReadActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("desc", description);
        bundle.putString("file", savedPhoto.getAbsolutePath());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private RequestBody requestBody;

    private void postRequest(File file, String URL){
        RequestBody requestBody = buildRequestBody(file);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .callTimeout(0, TimeUnit.SECONDS)
                .build();
        Request request = new Request
            .Builder()
            .post(requestBody)
            .url(URL)
            .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Something went wrong:" + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        call.cancel();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException{
                if(!response.isSuccessful()){
                    throw new IOException("Unexpected Code" + response);
                } else{
                    openNewActivity(getResponse(response));
                }
            }
        });
    }

    public String getResponse(Response response) throws IOException {
        String resp = response.body().string();
        return resp;
    }

    private RequestBody buildRequestBody(File file) {
        requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("text/csv"), file))
                .addFormDataPart("some-field", "some-value")
                .build();
        return requestBody;
    }

}
