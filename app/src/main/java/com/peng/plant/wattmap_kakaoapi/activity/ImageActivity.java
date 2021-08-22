package com.peng.plant.wattmap_kakaoapi.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.peng.plant.wattmap_kakaoapi.R;

import java.io.FileInputStream;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageView image = findViewById(R.id.image);
//
//        Bitmap bitmap = (Bitmap)getIntent().getParcelableExtra("image");
//
//        image.setImageBitmap(bitmap);

        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        image.setImageBitmap(bmp);

    }


}
