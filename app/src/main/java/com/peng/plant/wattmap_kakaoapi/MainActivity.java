package com.peng.plant.wattmap_kakaoapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.animation.TimeInterpolator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.peng.plant.wattmap_kakaoapi.controller.TiltScrollController;

import net.daum.android.map.coord.MapCoordLatLng;
import net.daum.mf.map.api.CameraUpdate;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;



public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener{//, TiltScrollController.ScrollListener

    private static final String TAG = "MainActivity";

    //xml
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private Button plusBtn, minusBtn, mapUp, mapDown, mapRight, mapLeft, myLoc, watt, LocCancel;
    private TextView zoomIn, zoomOut, map_Up, map_Down, map_Left, map_Right;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION};

    private double mCurrentLng;
    private double mCurrentLat;
    private double x;
    private double y;

    private MapPoint mapPoint;



    private TiltScrollController mTiltScrollController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mapView = new MapView(this);

        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        //Map 리스너
        //지도 이동/확대/축소 이벤트
        mapView.setMapViewEventListener(this);
        //내 위치 추적
        mapView.setCurrentLocationEventListener(this);


//        //동작센서
//        mTiltScrollController = new TiltScrollController(getApplicationContext(), this);


        //기본 위치 설정
        mapPoint = MapPoint.mapPointWithGeoCoord(37.43225475043913, 127.17844582341077);
        //위치 마커
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("와트");
        marker.setTag(0);
        marker.setMapPoint(mapPoint);
        marker.setMarkerType(MapPOIItem.MarkerType.RedPin);
        mapView.addPOIItem(marker);

        //확대 축소 버튼
        plusBtn = findViewById(R.id.zoomIn);
        minusBtn = findViewById(R.id.zoomOut);
        zoomIn = findViewById(R.id.zoomIn_text);
        zoomOut = findViewById(R.id.zoomOut_text);
        //지도 이동 버튼
        map_Up = findViewById(R.id.map_up);
        map_Down = findViewById(R.id.map_down);
        map_Left = findViewById(R.id.map_left);
        map_Right = findViewById(R.id.map_right);
        mapUp = findViewById(R.id.mapup);
        mapDown = findViewById(R.id.mapdown);
        mapRight = findViewById(R.id.mapright);
        mapLeft = findViewById(R.id.mapleft);
        watt = findViewById(R.id.watt);
        myLoc = findViewById(R.id.myLoc);
        LocCancel = findViewById(R.id.myLocCancel);


        //GPS 못찾는다면 지도 중심점 //와트
        mapView.setMapCenterPoint(mapPoint,true);
        //GPS 확인
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }

        //카메라 이동범위 설정?


        //방향 버튼 리스너
        plusBtn.setOnClickListener(ButtonZoomCon);
        minusBtn.setOnClickListener(ButtonZoomCon);
//        zoomIn.setOnClickListener(ZoomControl);
//        zoomOut.setOnClickListener(ZoomControl);
        mapUp.setOnClickListener(MapMoveControl);
        mapDown.setOnClickListener(MapMoveControl);
        mapRight.setOnClickListener(MapMoveControl);
        mapLeft.setOnClickListener(MapMoveControl);
        watt.setOnClickListener(MapMoveControl);
        //내위치 리스너
        myLoc.setOnClickListener(myLocation);


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapViewContainer.removeAllViews();
    }

    //currentLocation
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        Log.d(TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, v));
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }



    //requestPermission 을 사용한 퍼미션 요청의 결과 리턴 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length){
            //요청 코드가 PERMISSION_REQUEST_CODE , 요청한 퍼미션 개수만큼 수신되면
            boolean check_result = true;

            //퍼미션 허용 체크
            for (int result : grantResults){
                if (result != PackageManager.PERMISSION_GRANTED){
                    check_result = false;
                    break;
                }
            }

            if (check_result){
                //위치값 가져오기
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
            }else {
                //퍼미션 거부시 앱 종료
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,REQUIRED_PERMISSIONS[0])){
                    Toast.makeText(MainActivity.this, "위치서비스가 거부되었습니다. 앱을 재실행하여 허용해주세요.",Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    Toast.makeText(MainActivity.this, "위치서비스가 거부되었습니다. 설정에서 허용해야합니다.",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //런타임 퍼미션 확인
    void checkRunTimePermission(){
        //위치 퍼미션 체크
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED){
            //퍼미션을 가지고 있다면
            //위치값 가져옴
            mapView.setMapCenterPoint(mapPoint,true);
        }else {//퍼미션 미허용 상태라면
            //사용자가 퍼미션 거부 한 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])){
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                //퍼미션 요청
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }else {
                //퍼미션 거부한 적이 없는 경우 바로 요청
                ActivityCompat.requestPermissions(MainActivity.this,REQUIRED_PERMISSIONS,PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    //GPS 활성화 메소드
    private void showDialogForLocationServiceSetting(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" +"위치 설정을 허용하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성화 시켰는지 확인
                if (checkLocationServicesStatus()){
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@","onActivityResult: GPS 활성화 됨");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }
    //GPS 확인 메소드
    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //MapviewEvent
    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

//    //확대축소 리스너
//    private View.OnClickListener ZoomControl = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()){
//                case R.id.zoomIn_text:
//                    mapView.zoomIn(true);
//                    break;
//                case R.id.zoomOut_text:
//                    mapView.zoomOut(true);
//            }
//        }
//    };
    //확대축소 버튼 리스너
    private View.OnClickListener ButtonZoomCon = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.zoomIn:
                    mapView.zoomIn(true);
                    break;
                case R.id.zoomOut:
                    mapView.zoomOut(true);
                    break;
            }
        }
    };

    //지도 이동 리스너
    private View.OnClickListener MapMoveControl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CameraUpdate cameraUpdate;
            switch (v.getId()){
                case R.id.mapup:
                    mapPoint = MapPoint.mapPointWithGeoCoord(37.434273838609236, 127.17819906019075);
                    cameraUpdate = CameraUpdateFactory.newMapPoint(mapPoint, 2f);
                    mapView.moveCamera(cameraUpdate);
                    break;
                case R.id.mapdown:
                    mapPoint = MapPoint.mapPointWithGeoCoord(37.42943475131018, 127.1786067559549);
                    cameraUpdate = CameraUpdateFactory.newMapPoint(mapPoint, 2f);
                    mapView.moveCamera(cameraUpdate);
                    break;
                case R.id.mapleft:
                    mapPoint = MapPoint.mapPointWithGeoCoord(37.43153911115641, 127.17491603644513);
                    cameraUpdate = CameraUpdateFactory.newMapPoint(mapPoint, 2f);
                    mapView.moveCamera(cameraUpdate);
                    break;
                case R.id.mapright:
                    mapPoint = MapPoint.mapPointWithGeoCoord(37.43216955562511, 127.18132115132998);
                    cameraUpdate = CameraUpdateFactory.newMapPoint(mapPoint, 2f);
                    mapView.moveCamera(cameraUpdate);
                    break;
                case R.id.watt:
                    mapPoint = MapPoint.mapPointWithGeoCoord(37.43225475043913, 127.17844582341077);
                    cameraUpdate = CameraUpdateFactory.newMapPoint(mapPoint,2f);
                    mapView.moveCamera(cameraUpdate);
                    break;

            }

        }
    };

    private View.OnClickListener myLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.myLoc:
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
                    Log.d(TAG, "start");
                    break;
                case R.id.myLocCancel:
                    
            }
        }
    };



//    //틸트센서
//    @Override
//    public void onTilt(float x, float y) {
//        mapView.setTranslationX(x);
//        mapView.setTranslationY(y);
//    }
}