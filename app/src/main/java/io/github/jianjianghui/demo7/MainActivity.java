package io.github.jianjianghui.demo7;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.github.jianjianghui.bridge.BridgeCaller;
import io.github.jianjianghui.bridge.BridgeFactory;
import io.github.jianjianghui.bridge.BridgeRegister;
import kotlin.Pair;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    BridgeRegister register;
    BridgeCaller caller;

    Button button;
    WebView myWebView;

    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        button.setOnClickListener(this);
        myWebView = findViewById(R.id.webView);
        myWebView.loadUrl("file:///android_asset/demo.html");
        Pair<BridgeRegister, BridgeCaller> bridgeCallerPair = BridgeFactory.get(myWebView);
        if (Objects.isNull(bridgeCallerPair)) {
            throw new IllegalArgumentException("bridge不受支持");
        }

        register = bridgeCallerPair.getFirst();

        register.register("sayHi", result ->{
            Toast.makeText(this.getApplicationContext(),result,Toast.LENGTH_LONG).show();
           return  "{\"code\":0,\"message\":\"收到 JS》》》》Hello,I am native.\"}";
        });

        // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result0 -> {
                    if (result0.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result0.getData();
                        try {
                            FileInputStream in = new FileInputStream(currentPhotoPath);
                            byte[] bytes = new byte[in.available()];
                            in.read(bytes);
                            in.close();
                            String s = Base64.getEncoder().encodeToString(bytes);

                            OkHttpClient client = new OkHttpClient().newBuilder()
                                    .build();
                            MediaType mediaType = MediaType.parse("text/plain");
                            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                    .addFormDataPart("file","test.jpg",
                                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                                    new File(currentPhotoPath)))
                                    .build();
                            Request request = new Request.Builder()
                                    .url("https://file.io/~/"+"test.jpg")
                                    .method("POST", body)
                                    .addHeader("authority", "file.io")
                                    .addHeader("accept", "application/json, text/plain, */*")
                                    .addHeader("accept-language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                                    .addHeader("authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI4NGNkMDUwMC1hYjc3LTExZWQtYjVkOC0zZjkyZjIxNWVkNmUiLCJpYXQiOjE2NzYyNzY1MzMsImV4cCI6MTY3NjM2MjkzM30.pT8Vln-pvXZuBAOsTEwaQFM6UPYITH4HhLheFRAOG00")
                                    .addHeader("origin", "https://www.file.io")
                                    .addHeader("referer", "https://www.file.io/")
                                    .addHeader("sec-ch-ua", "\"Not_A Brand\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"")
                                    .addHeader("sec-ch-ua-mobile", "?0")
                                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                                    .addHeader("sec-fetch-dest", "empty")
                                    .addHeader("sec-fetch-mode", "cors")
                                    .addHeader("sec-fetch-site", "same-site")
                                    .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
                                    .build();
                            AtomicReference<String> string = new AtomicReference<>();

                                    Thread thread = new Thread(() -> {
                                Response response = null;
                                try {
                                    response = client.newCall(request).execute();

                                    string.set(response.body().string());
                                    sendImage(string.get());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            });
                            thread.start();

//
//                            caller.call("sayHi", "{\"code\":0,\"message\":\"Hello,I am native.\"}", result -> {
//                                Toast.makeText(this.getApplicationContext(),result,Toast.LENGTH_LONG).show();
//                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });


        register.register("openCapture",result->{
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    someActivityResultLauncher.launch(intent);
                }
            }

            return "";
        });
        caller = bridgeCallerPair.getSecond();
    }

    private void sendImage(String params) {
        MainActivity.this.runOnUiThread(()->{
                caller.call("loadImage", params, result -> {
                    Toast.makeText(this.getApplicationContext(),result,Toast.LENGTH_LONG).show();
                });
        });

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onClick(View v) {
        if(!v.equals(this.button)) {
            return;
        }

        caller.call("sayHi", "{\"code\":0,\"message\":\"Hello,I am native.\"}", result -> {
            Toast.makeText(this.getApplicationContext(),result,Toast.LENGTH_LONG).show();
        });
    }
}