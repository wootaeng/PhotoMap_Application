package com.peng.plant.wattmap_kakaoapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.peng.plant.wattmap_kakaoapi.controller.TiltScrollController;
import com.peng.plant.wattmap_kakaoapi.data.ImageData;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdate;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapView;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener, MapView.POIItemEventListener , TiltScrollController.ScrollListener{//

    private static final String TAG = "MainActivity";

    //xml
    private MapView mMapView;
    private ViewGroup mMapViewContainer;
    private MapPoint mMapPoint;
    private MapPOIItem mCustomBmMarker, marker;
    private static final MapPoint CUSTOM_MARKER_POINT2 = MapPoint.mapPointWithGeoCoord(37.447229, 127.015515);


    private Button plusBtn, minusBtn, mapUp, mapDown, mapRight, mapLeft, myLoc, watt, LocCancel, sensorStop;
    private TextView zoomIn, zoomOut, map_Up, map_Down, map_Left, map_Right;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION};

    private double mCurrentLng;
    private double mCurrentLat;
    private double mMapX = 37.43225475043913;
    private double mMapY = 127.17844582341077;
    private double mLatlng = 0.0;
    private int mZoomLevel = 0;
    private float mZoomLevelfloat = (float)0.0;

    //센서 동작
    private boolean sensor_control = true;

    private Context mContext;

    private TiltScrollController mTiltScrollController;



    //이미지 값 가져오기?
    private ArrayList<ImageData> allimages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        mContext = this;

        mMapView = new MapView(this);

        mMapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mMapViewContainer.addView(mMapView);

        //Map 리스너
        //지도 이동/확대/축소 이벤트
        mMapView.setMapViewEventListener(this);

        //내 위치 추적
        mMapView.setCurrentLocationEventListener(this);

        //커스텀 마커 리스너
        mMapView.setPOIItemEventListener(this);

        //기본 위치 설정
        mMapPoint = MapPoint.mapPointWithGeoCoord(mMapX, mMapY);



        //커스텀 마커
        mMapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        createCustomBitmapMarker(mMapView);


        //동작센서
        mTiltScrollController = new TiltScrollController(getApplicationContext(), this);

        //기본마커
        marker = new MapPOIItem();
        marker.setItemName("와트");
        marker.setTag(0);
        marker.setMapPoint(mMapPoint);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);//기본 마커
        mMapView.addPOIItem(marker);



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
        sensorStop = findViewById(R.id.sensorStop);


        //GPS 못찾는다면 지도 중심점 //와트
        mMapView.setMapCenterPoint(mMapPoint,true);
        //GPS 확인
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }


        //방향 버튼 리스너
        plusBtn.setOnClickListener(ButtonZoomCon);
        minusBtn.setOnClickListener(ButtonZoomCon);
        zoomIn.setOnClickListener(ZoomControl);
        zoomOut.setOnClickListener(ZoomControl);
        mapUp.setOnClickListener(MapMoveControl);
        mapDown.setOnClickListener(MapMoveControl);
        mapRight.setOnClickListener(MapMoveControl);
        mapLeft.setOnClickListener(MapMoveControl);

        //내위치 리스너
        myLoc.setOnClickListener(myLocation);
        LocCancel.setOnClickListener(myLocation);
        //회사이동
        watt.setOnClickListener(myLocation);

        //이미지 가져오기
        getPathOfAllImg();


    }




    //커스텀 마커 담을 메소드
    private void createCustomBitmapMarker(MapView mMapView) {
        mCustomBmMarker = new MapPOIItem();
        String name = "커스텀 비트맵 마커";
        mCustomBmMarker.setItemName(name);
        mCustomBmMarker.setTag(2);
        mCustomBmMarker.setMapPoint(CUSTOM_MARKER_POINT2);

        mCustomBmMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.test_1);
        mCustomBmMarker.setCustomCalloutBalloonBitmap(bm);
        mCustomBmMarker.setCustomImageAutoscale(false);
        mCustomBmMarker.setCustomImageAnchor(0.5f,0.5f);

        mMapView.addPOIItem(mCustomBmMarker);
        mMapView.selectPOIItem(mCustomBmMarker,true);
        mMapView.setMapCenterPoint(CUSTOM_MARKER_POINT2, false);
    }

    //커스텀 마커 인터페이스
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            ((ImageView) mCalloutBalloon.findViewById(R.id.badge)).setImageResource(R.drawable.test_1);
            ((TextView) mCalloutBalloon.findViewById(R.id.imageTitle)).setText(poiItem.getItemName());
            ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText("Custom CalloutBalloon");
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }

    //이미지 담을 arrayList
    public ArrayList<String> getPathOfAllImg() {

        ArrayList<String> result = new ArrayList<>();
        //이미지 가져오기
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        //파일 정보 담기
        String[] projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.LATITUDE,
                MediaStore.Images.ImageColumns.LONGITUDE,
                MediaStore.MediaColumns.MIME_TYPE
        };

        //정렬하는 쿼리문
        Cursor cursor = getContentResolver().query(uri, projection, null, null, MediaStore.MediaColumns.DATE_ADDED + " desc");

        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
            Double latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE));
            Double longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE));

            if (!TextUtils.isEmpty(path)) {
                result.add(path);
                Log.d("cursor_path : ", path);
            }
            if (!TextUtils.isEmpty(name)) {
                Log.d("cursor_name : ", name);
            }
            Log.d("cursor_latitude : ", latitude+"");
            Log.d("cursor_longitude : ", longitude+"");

        }
        return result;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapViewContainer.removeAllViews();
    }

    //currentLocation
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        mMapX = mapPointGeo.latitude;
        mMapY = mapPointGeo.longitude;

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
                startTracking();
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
            mMapView.setMapCenterPoint(mMapPoint,true);
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

    //위치추적 시작
    private void startTracking(){
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
    }

    //위치추적 중지
    private void stopTracking(){
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
    }

    //MapviewEvent
    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
        MapPoint latlng= mapView.getMapCenterPoint();
        Log.d(TAG, "성공 lat+"+ latlng.getMapPointGeoCoord().latitude);
        Log.d(TAG, "성공 lng+"+ latlng.getMapPointGeoCoord().longitude);
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
        int zoomlevel = mapView.getZoomLevel();
        Log.d(TAG, "현재 Zoom Level : " + zoomlevel);

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

    //확대축소 리스너
    private View.OnClickListener ZoomControl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.zoomIn_text:
                    mMapView.zoomIn(true);
                    break;
                case R.id.zoomOut_text:
                    mMapView.zoomOut(true);
            }
        }
    };
    //확대축소 버튼 리스너
    private View.OnClickListener ButtonZoomCon = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.zoomIn:
                    mMapView.zoomIn(true);
                    break;
                case R.id.zoomOut:
                    mMapView.zoomOut(true);
                    break;
            }
        }
    };

    //지도 버튼 이동 리스너
    private View.OnClickListener MapMoveControl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.mapup:
                    mMapX = mMapX + 0.0035;
                    mMapPoint = MapPoint.mapPointWithGeoCoord(mMapX, mMapY);
                    break;
                case R.id.mapdown:
                    mMapX = mMapX - 0.0035;
                    mMapPoint = MapPoint.mapPointWithGeoCoord(mMapX, mMapY);
                    break;
                case R.id.mapleft:
                    mMapY = mMapY - 0.0035;
                    mMapPoint = MapPoint.mapPointWithGeoCoord(mMapX, mMapY);
                    break;
                case R.id.mapright:
                    mMapY = mMapY + 0.0035;
                    mMapPoint = MapPoint.mapPointWithGeoCoord(mMapX, mMapY);
                    break;

            }
            //현재 줌레벨 가져오기
            mZoomLevel = mMapView.getZoomLevel();
            //이동 좌표 update
            CameraUpdate cameraUpdate = CameraUpdateFactory.newMapPoint(mMapPoint,mZoomLevel);
            //좌표이동
            mMapView.moveCamera(cameraUpdate);
        }
    };

    private View.OnClickListener myLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.myLoc:
                    startTracking();
                    Log.d(TAG, "위치추적 시작");
                    LocCancel.setVisibility(View.VISIBLE);
                    myLoc.setVisibility(View.GONE);

                    break;
                case R.id.myLocCancel:
                    stopTracking();
                    Log.d(TAG,"위치추적 중지");
                    LocCancel.setVisibility(View.GONE);
                    myLoc.setVisibility(View.VISIBLE);
                    break;

                case R.id.watt:
                    mMapPoint = MapPoint.mapPointWithGeoCoord(37.43225475043913, 127.17844582341077);
                    //이동 좌표 update
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newMapPoint(mMapPoint,mZoomLevel);
                    //좌표이동
                    mMapView.moveCamera(cameraUpdate);
                    break;
            }
        }
    };

    //지도 센서 이동 메소드
    private void mapMove(int type) {
        if (type == UP) {
            mMapX = mMapX + 0.0005;
            mMapPoint = MapPoint.mapPointWithGeoCoord(mMapX, mMapY);
        } else if (type == DOWN) {
            mMapX = mMapX - 0.0005;
            mMapPoint = MapPoint.mapPointWithGeoCoord(mMapX, mMapY);
        } else if (type == RIGHT){
            mMapY = mMapY + 0.0005;
            mMapPoint = MapPoint.mapPointWithGeoCoord(mMapX, mMapY);
        } else if (type == LEFT){
            mMapY = mMapY- 0.0005;
            mMapPoint = MapPoint.mapPointWithGeoCoord(mMapX, mMapY);
        }
        //현재 줌레벨 가져오기
        mZoomLevelfloat = mMapView.getZoomLevelFloat();
        //이동 좌표 update
        CameraUpdate cameraUpdate = CameraUpdateFactory.newMapPoint(mMapPoint, mZoomLevelfloat);
        //좌표이동
        mMapView.moveCamera(cameraUpdate);
    }


    final int UP = 0;
    int DOWN = 1;
    int RIGHT = 2;
    int LEFT = 3;

    //틸트센서
    @Override
    public void onTilt(float x, float y) {
//        if (x > 0) {
//            mapMove(UP);
//        } else if ( x < 0) {
//            mapMove(DOWN);
//        } else if ( y > 0){
//            mapMove(RIGHT);
//        } else if ( y < 0) {
//            mapMove(LEFT);
//        }
    }

    private void stopSensor(){
        mTiltScrollController.releaseAllSensors();
    }

    //커스텀 마커 리스너
    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }



}