package io.github.jianjianghui.demo7;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeWebView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

import io.github.jianjianghui.bridge.BridgeCaller;
import io.github.jianjianghui.bridge.BridgeFactory;
import io.github.jianjianghui.bridge.BridgeRegister;
import kotlin.Pair;

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

                            System.out.println("向JS发送图片数据"+s);
                            caller.call("loadImage", currentPhotoPath, result -> {
                                Toast.makeText(this.getApplicationContext(),result,Toast.LENGTH_LONG).show();
                            });
                            caller.call("sayHi", "{\"code\":0,\"message\":\"Hello,I am native.\"}", result -> {
                                Toast.makeText(this.getApplicationContext(),result,Toast.LENGTH_LONG).show();
                            });
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