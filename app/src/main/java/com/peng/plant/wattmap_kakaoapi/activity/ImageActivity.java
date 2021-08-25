package com.peng.plant.wattmap_kakaoapi.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.peng.plant.wattmap_kakaoapi.MainActivity;
import com.peng.plant.wattmap_kakaoapi.R;
import com.peng.plant.wattmap_kakaoapi.controller.TiltScrollController;
import com.peng.plant.wattmap_kakaoapi.controller.ZoomController;

import java.io.FileInputStream;

public class ImageActivity extends AppCompatActivity implements TiltScrollController.ScrollListener{//

    private ImageView imageView, miniView;
    private TiltScrollController mTiltScrollController;
    private boolean sensor_control = false;
    private Button Zoom1,Zoom2,Zoom3,Zoom4,Zoom5,sensorM, sensorP;
    private TextView ZoomV1,ZoomV2,ZoomV3,ZoomV4,ZoomV5;
    private ZoomController mZoomcontrol;
    private RelativeLayout mRelativeLayout;
    private View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        imageView = findViewById(R.id.image);



        Glide.with(this).load(getIntent().getStringExtra("image")).fitCenter().into(imageView);

        //bitmap 으로 가져오기
//        String path = getIntent().getStringExtra("image");
//        Bitmap bm = BitmapFactory.decodeFile(path);
//        imageView.setImageBitmap(bm);
        //View
        init();
        //
        MiniMapZoomContrl();
        //미니맵 그리기
        miniMapDraw();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ImageActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


    //줌 버튼 리스너
    private View.OnClickListener ZoomLevel_moveControll = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.zoom1:
                    Zoomlevel(1f);
                    break;
                case R.id.zoom2:
                    Zoomlevel(2f);
                    break;
                case R.id.zoom3:
                    Zoomlevel(3f);
                    break;
                case R.id.zoom4:
                    Zoomlevel(4f);
                    break;
                case R.id.zoom5:
                    Zoomlevel(5f);
                    break;
            }
        }
    };

    //줌 음성 리스너
    private View.OnClickListener ZoomLevel_Move_Voice = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.sensorStart:
                    sensor_control = true;
                    sensorP.setVisibility(View.VISIBLE);
                    break;
                case R.id.sensorStop:
                    sensor_control = false;
                    sensorM.setVisibility(View.VISIBLE);
                    break;
                case R.id.zoomV1:
                    Zoomlevel(1f);
                    break;
                case R.id.zoomV2:
                    Zoomlevel(2f);
                    break;
                case R.id.zoomV3:
                    Zoomlevel(3f);
                    break;
                case R.id.zoomV4:
                    Zoomlevel(4f);
                    break;
                case R.id.zoomV5:
                    Zoomlevel(5f);
                    break;
            }
        }
    };



    private void init(){
        //미니맵뷰
        mRelativeLayout = findViewById(R.id.mRelativelayout);
        //줌 버튼
        Zoom1 = findViewById(R.id.zoom1);
        Zoom2 = findViewById(R.id.zoom2);
        Zoom3 = findViewById(R.id.zoom3);
        Zoom4 = findViewById(R.id.zoom4);
        Zoom5 = findViewById(R.id.zoom5);
        //센서 버튼
        sensorM = findViewById(R.id.imageStart);
        sensorP = findViewById(R.id.imageStop);
        //센서 리스너
        sensorM.setOnClickListener(ZoomLevel_Move_Voice);
        sensorP.setOnClickListener(ZoomLevel_Move_Voice);
        //줌 텍스트
        ZoomV1 = findViewById(R.id.zoomV1);
        ZoomV2 = findViewById(R.id.zoomV2);
        ZoomV3 = findViewById(R.id.zoomV3);
        ZoomV4 = findViewById(R.id.zoomV4);
        ZoomV5 = findViewById(R.id.zoomV5);
        //줌 버튼 리스너
        Zoom1.setOnClickListener(ZoomLevel_moveControll);
        Zoom2.setOnClickListener(ZoomLevel_moveControll);
        Zoom3.setOnClickListener(ZoomLevel_moveControll);
        Zoom4.setOnClickListener(ZoomLevel_moveControll);
        Zoom5.setOnClickListener(ZoomLevel_moveControll);
        //줌 음성 리스너
        ZoomV1.setOnClickListener(ZoomLevel_Move_Voice);
        ZoomV2.setOnClickListener(ZoomLevel_Move_Voice);
        ZoomV3.setOnClickListener(ZoomLevel_Move_Voice);
        ZoomV4.setOnClickListener(ZoomLevel_Move_Voice);
        ZoomV5.setOnClickListener(ZoomLevel_Move_Voice);


        mTiltScrollController = new TiltScrollController(getApplicationContext(),this);


    }

    //미니맵 그리기 메소드
    private void miniMapDraw(){
        Glide.with(this).load(getIntent().getStringExtra("image")).fitCenter().into(miniView);
    }
    //미니맵 줌 컨트롤러 적용
    private void MiniMapZoomContrl(){
        //뷰 에 올리기
        v = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.image_minimap, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        miniView = v.findViewById(R.id.minimapV);
        //줌컨트롤 연결
        mZoomcontrol = new ZoomController(this);
        mZoomcontrol.addView(v);
        mZoomcontrol.setLayoutParams(layoutParams);
        //미니맵 표시
        mZoomcontrol.setMiniMapEnabled(true);
        //최대 줌
        mZoomcontrol.setMaxZoom(7f);
        //미니맵 크기지정
        mZoomcontrol.setMiniMapHeight(200);
        //미니맵 추가
        mRelativeLayout.addView(mZoomcontrol);
    }
    //틸트센서 리스너 메소드
    @Override
    public void onTilt(float x, float y) {
//        if (sensor_control){
//            //틸트 위아래 좌표적용
////            imageView.setTranslationX(-x*64);
////            imageView.setTranslationY(-y*64);
            mZoomcontrol.Move_Sensor(-x*64, -y*64);
//        }
    }
    //줌레벨 적용 메소드
    private void Zoomlevel(float num){
        mZoomcontrol.zoomlevel(num);
    }
}
