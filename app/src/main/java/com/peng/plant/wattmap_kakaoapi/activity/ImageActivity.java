package com.peng.plant.wattmap_kakaoapi.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.peng.plant.wattmap_kakaoapi.R;

import java.io.FileInputStream;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageView imageView = findViewById(R.id.image);

        Glide.with(this).load(getIntent().getStringExtra("image")).fitCenter().into(imageView);

//        String path = getIntent().getStringExtra("image");
//        Bitmap bm = BitmapFactory.decodeFile(path);
//
//        imageView.setImageBitmap(bm);


    }


}
