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
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.peng.plant.wattmap_kakaoapi.activity.ImageActivity;
import com.peng.plant.wattmap_kakaoapi.adapter.CustomCalloutBalloonAdapter;
import com.peng.plant.wattmap_kakaoapi.controller.TiltScrollController;
import com.peng.plant.wattmap_kakaoapi.data.ImageData;
import com.peng.plant.wattmap_kakaoapi.view.CommandView;

import net.daum.mf.map.api.CameraUpdate;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ted.gun0912.clustering.TedClustering;
import ted.gun0912.clustering.TedMap;


public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener, MapView.POIItemEventListener , TiltScrollController.ScrollListener{//

    private static final String TAG = "MainActivity";

    private Context mContext;

    //xml
    private MapView mMapView;
    private ViewGroup mMapViewContainer;

    private MapPoint mMapPoint;

    private MapPOIItem mMoveMarker;

    private Button plusBtn, minusBtn, mapUp, mapDown, mapRight, mapLeft, myLoc, watt, LocCancel, sensorStop, sensorStart,
            circle500, circle1000, circle3000, circle5000, removeC;
    private TextView zoomIn, zoomOut, map_Up, map_Down, map_Left, map_Right, locOn, locOff, wattH,
            circleVal,circle1,circle2,circle3,circle4,circleR, Loc1,Loc2,Loc3,Loc4,Loc5,LocMakDel;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private double mCurrentLng, mCurrentLat, mMapX, mMapY;
    private int mZoomLevel = 0;
    private float mZoomLevelfloat = (float)0.0;

    //?????? ??????
    private boolean sensor_control = false;
    private TiltScrollController mTiltScrollController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        //???????????? view ??????
        mMapView = new MapView(this);
        mMapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mMapViewContainer.addView(mMapView);


        //?????? ?????? ??????
        mMapPoint = MapPoint.mapPointWithGeoCoord(37.43225475043913, 127.17844582341077);

        //GPS ??????????????? ?????? ????????? //??????
        mMapView.setMapCenterPoint(mMapPoint,true);

        //view init
        init();

        mContext = this;

        //?????? ????????? ?????? ??????
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

        //GPS ??????
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }

        //????????? ?????? adapter ??????
        mMapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter(this));
        //?????? ????????? list
        ArrayList<ImageData> list = getPathOfAllImg();
        Log.d("listsize",list.size()+"");

        //?????????list ??????list??? ??????
        MapPOIItem[] poiList = new MapPOIItem[list.size()];
        //list ???????????? ????????? ?????? ????????? ??????
        for (int i=0; i < list.size(); i++) {
            MapPOIItem item = createCustomBitmapMarker(list.get(i) , i);
            poiList[i] = item;

            int picNumber = i + 1;
            CommandView cmdView = new CommandView(this , String.format("?????? %d" , picNumber) , (R.id.id_marker_image + i));
            cmdView.setOnClickListener(pictureControl);
            cmdView.setTag(item.getItemName());
        }

        //?????? ?????? ????????? -> ??????
//       for (int i=0; i < list.size(); i++){
//           int finalI = i;
//           mMapView.selectPOIItem(poiList[finalI],false);
//       }

        //map??? ?????? ??????
        mMapView.addPOIItems(poiList);

        //???????????? ??????
//        mMapView.selectPOIItem(poiList[1], false);

        mMapView.setMapCenterPoint(mMapPoint, false);


    }

    /**
     * ?????? ?????? ?????????
     * @param mapView
     * @param mapPoint
     */
    //?????? ????????? ?????? ?????????
    private void createMarker(MapView mapView, MapPoint mapPoint){
        mMoveMarker = new MapPOIItem();
        String name = "??????";
        mMoveMarker.setItemName(name);
        mMoveMarker.setTag(0);
        mMoveMarker.setMapPoint(mapPoint);
        mMoveMarker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        mMoveMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        mMapView.addPOIItem(mMoveMarker);
    }

    ///????????? ?????? ?????????
    private MapPOIItem createCustomBitmapMarker(ImageData data , int pos) {
        MapPOIItem m = new MapPOIItem();
        m.setItemName(data.getPath());
        m.setTag(pos);

        MapPoint point = MapPoint.mapPointWithGeoCoord(data.getLatitude(), data.getLongitude());
        m.setMapPoint(point);

        m.setMarkerType(MapPOIItem.MarkerType.CustomImage);

        File imgFile = new  File(data.getPath());
        Bitmap bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

        m.setCustomImageBitmap(resizingBitmap(bm));
        m.setCustomImageAutoscale(false);
        m.setCustomImageAnchor(0.5f, 0.5f);
        //?????? ?????? ????????????????????? ???????????? ?????? ??????????????? ?????? ?????? ??????
        //90?????? ????????? ?????? ?????? ?????? ????????? // ????????? ??????????????? ????????? ??????????????? ??????
//        m.setRotation(90f);

        return m;
    }

    //Local gallery ???????????? ????????? ?????? ????????????
    //????????? ?????? arrayList
    public ArrayList<ImageData> getPathOfAllImg() {
        ArrayList<ImageData> imageList = new ArrayList<ImageData>();

        //????????? ????????????
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        //?????? ?????? ??????
        String[] projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.LATITUDE,
                MediaStore.Images.ImageColumns.LONGITUDE,
                MediaStore.MediaColumns.MIME_TYPE
        };

        //???????????? ?????????
        Cursor cursor = getContentResolver().query(uri, projection, null, null, MediaStore.MediaColumns.DATE_ADDED + " desc");

        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));//????????????
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));//????????????
            Double latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE));//??????
            Double longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE));//??????

            ImageData data = new ImageData();

            if (!TextUtils.isEmpty(path)) {
                Log.d("cursor_path : ", path);
                data.setPath(path);
            }
            if (!TextUtils.isEmpty(name)) {
                Log.d("cursor_name : ", name);
                data.setName(name);
            }

            data.setLatitude(latitude);
            data.setLongitude(longitude);

            Log.d("cursor_latitude : ", latitude.toString()+"");
            Log.d("cursor_longitude : ", longitude.toString()+"");

            imageList.add(data);
        }

        return imageList;
    }

    //?????? ???????????? ?????????
    private View.OnClickListener pictureControl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String itemName = (String)v.getTag();
            Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
            intent.putExtra("image",itemName);
            startActivity(intent);

        }
    };

    //bitmap ???????????? ?????????
    private Bitmap resizingBitmap(Bitmap bm) {
        int maxHeight = 80;
        int maxWidth = 80;
        float scale = Math.min(((float)maxHeight / bm.getWidth()), ((float)maxWidth / bm.getHeight()));
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        return bitmap;
    }

    /****
     *
     * ?????? ????????? POIItemEventListener
     * @param mapView
     * @param mapPOIItem
     */
    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        Log.d("TAGTAGTAG", mapPOIItem.getItemName());


        Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
        intent.putExtra("image",mapPOIItem.getItemName());
        startActivity(intent);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapViewContainer.removeAllViews();
    }

    /**
     * ????????? ????????? CurrentLocationEventListener
     * @param mapView
     * @param mapPoint
     * @param v
     */
    //currentLocation ????????? ??????
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        //?????? ?????? ??? ?????? ??? ???????????????
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

    /**
     * ????????? ?????? ?????? ??????
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    //requestPermission ??? ????????? ????????? ????????? ?????? ?????? ?????????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length){
            //?????? ????????? PERMISSION_REQUEST_CODE , ????????? ????????? ???????????? ????????????
            boolean check_result = true;

            //????????? ?????? ??????
            for (int result : grantResults){
                if (result != PackageManager.PERMISSION_GRANTED){
                    check_result = false;
                    break;
                }
            }

            if (check_result){
                //????????? ????????????
                startTracking();
            }else {
                //????????? ????????? ??? ??????
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,REQUIRED_PERMISSIONS[0])){
                    Toast.makeText(MainActivity.this, "?????????????????? ?????????????????????. ?????? ??????????????? ??????????????????.",Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    Toast.makeText(MainActivity.this, "?????????????????? ?????????????????????. ???????????? ?????????????????????.",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //????????? ????????? ??????
    void checkRunTimePermission(){
        //?????? ????????? ??????
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED){
            //???????????? ????????? ?????????
            //????????? ?????????
            mMapView.setMapCenterPoint(mMapPoint,true);
        }else {//????????? ????????? ????????????
            //???????????? ????????? ?????? ??? ??????
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])){
                Toast.makeText(MainActivity.this, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                //????????? ??????
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }else {
                //????????? ????????? ?????? ?????? ?????? ?????? ??????
                ActivityCompat.requestPermissions(MainActivity.this,REQUIRED_PERMISSIONS,PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    //GPS ????????? ?????????
    private void showDialogForLocationServiceSetting(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n" +"?????? ????????? ?????????????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
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
                //???????????? GPS ????????? ???????????? ??????
                if (checkLocationServicesStatus()){
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@","onActivityResult: GPS ????????? ???");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }
    //GPS ?????? ?????????
    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //???????????? ??????
    private void startTracking(){
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
    }

    //???????????? ??????
    private void stopTracking(){
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
    }

    /*****
     * MapviewEvent
     *
     * ****/
    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
        //?????? ????????? ????????? ?????? ??????
        MapPoint latlng= mapView.getMapCenterPoint();
        Log.d(TAG, "?????? lat+"+ latlng.getMapPointGeoCoord().latitude);
        Log.d(TAG, "?????? lng+"+ latlng.getMapPointGeoCoord().longitude);
    }
    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
        //?????? ????????? ??????
        int zoomlevel = mapView.getZoomLevel();
        Log.d(TAG, "?????? Zoom Level : " + zoomlevel);

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




    //???????????? ???????????????
    private View.OnClickListener ZoomControl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.zoomIn_text:
                    mMapView.zoomIn(true);
                    break;
                case R.id.zoomOut_text:
                    mMapView.zoomOut(true);
                    break;
            }
        }
    };

    //?????? ?????? ?????? ?????????
    private View.OnClickListener MapMoveControl_v = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Double lat = mMapView.getMapCenterPoint().getMapPointGeoCoord().latitude;
            Double lng = mMapView.getMapCenterPoint().getMapPointGeoCoord().longitude;
            switch (v.getId()){
                case R.id.map_up:
                    mMapX = lat + 0.0035;
                    mMapY = lng;
                    break;
                case R.id.map_down:
                    mMapX = lat - 0.0035;
                    mMapY = lng;
                    break;
                case R.id.map_left:
                    mMapX = lat;
                    mMapY = lng - 0.0035;
                    break;
                case R.id.map_right:
                    mMapX = lat;
                    mMapY = lng + 0.0035;
                    break;
            }
            mMapPoint = MapPoint.mapPointWithGeoCoord(mMapX, mMapY);
            //?????? ????????? ????????????
            mZoomLevel = mMapView.getZoomLevel();
            //?????? ?????? update
            CameraUpdate cameraUpdate = CameraUpdateFactory.newMapPoint(mMapPoint,mZoomLevel);
            //????????????
            mMapView.moveCamera(cameraUpdate);
        }
    };



    //????????? ?????? ?????????
    private View.OnClickListener myLocation_v = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.locOn:
                    startTracking();
                    Log.d(TAG, "???????????? ??????");
                    LocCancel.setVisibility(View.VISIBLE);
                    myLoc.setVisibility(View.GONE);
                    break;
                case R.id.locOff:
                    stopTracking();
                    Log.d(TAG,"???????????? ??????");
                    LocCancel.setVisibility(View.GONE);
                    myLoc.setVisibility(View.VISIBLE);
                    break;
                //?????? ??????
                case R.id.wattH:
                    mMapPoint = MapPoint.mapPointWithGeoCoord(37.43225475043913, 127.17844582341077);
                    //?????? ????????? ????????????
                    mZoomLevel = mMapView.getZoomLevel();
                    //?????? ?????? update
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newMapPoint(mMapPoint,mZoomLevel);
                    //????????????
                    mMapView.moveCamera(cameraUpdate);
                    break;
            }
        }
    };

    //?????? ?????? ?????? on/off ?????????
    private View.OnClickListener Sensor_control = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.sensorStop:
                    sensor_control = false;
                    sensorStop.setVisibility(View.GONE);
                    sensorStart.setVisibility(View.VISIBLE);
                    Log.d("Sensor","????????????");
                    break;
                case R.id.sensorStart:
                    sensor_control = true;
                    sensorStart.setVisibility(View.GONE);
                    sensorStop.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    //?????? ?????? ????????? ?????????
    private View.OnClickListener moveLcaMarker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MapPoint mapPoint = null;
            CameraUpdate cameraUpdate = null;
            switch (v.getId()) {
                case R.id.loc1:
                    mapPoint = MapPoint.mapPointWithGeoCoord(37.43148342074998, 127.17725706501436);
                    createMarker(mMapView,mapPoint);
                    break;
                case R.id.loc2:
                    mapPoint = MapPoint.mapPointWithGeoCoord(37.431413210499265, 127.17269882116143);
                    createMarker(mMapView,mapPoint);
                    break;
                case R.id.loc3:
                    mapPoint = MapPoint.mapPointWithGeoCoord(37.43185622683739, 127.16886862674598);
                    createMarker(mMapView,mapPoint);
                    break;
                case R.id.loc4:
                    mapPoint = MapPoint.mapPointWithGeoCoord(37.432699654540876, 127.16558560292407);
                    createMarker(mMapView,mapPoint);
                    break;
                case R.id.loc5:
                    mapPoint = MapPoint.mapPointWithGeoCoord(37.43307808592833, 127.15858072823039);
                    createMarker(mMapView,mapPoint);
                    break;
//                case R.id.locMarkDel: //?????? ?????? ??????
//
//                    break;
            }
            mZoomLevel = mMapView.getZoomLevel();
            //?????? ?????? update
            cameraUpdate = CameraUpdateFactory.newMapPoint(mapPoint,mZoomLevel);
            //????????????
            mMapView.moveCamera(cameraUpdate);
        }
    };




    //???????????? ?????? ?????????
    private View.OnClickListener circle_voice = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.circle1:
                    customCircle(500);
                    circleVal.setText("?????? ?????? : 500??????");
                    break;
                case R.id.circle2:
                    customCircle(1000);
                    circleVal.setText("?????? ?????? : 1Km");
                    break;
                case R.id.circle3:
                    customCircle(3000);
                    circleVal.setText("?????? ?????? : 3Km");
                    break;
                case R.id.circle4:
                    customCircle(5000);
                    circleVal.setText("?????? ?????? : 5Km");
                    break;
                case R.id.circleR:
                    removeCircle();
                    circleVal.setText("");
                    break;
            }
        }
    };

    //?????? ?????? ?????????
    private void customCircle(int radius){
        //?????? ??????
        MapCircle[] circle = mMapView.getCircles();
        if (circle.length > 0) { //??? ????????? ?????? ?????? ??????
            mMapView.removeCircle(circle[0]);
        }
        //?????? ?????? ?????????
        mMapX = mMapView.getMapCenterPoint().getMapPointGeoCoord().latitude;
        mMapY = mMapView.getMapCenterPoint().getMapPointGeoCoord().longitude;
        mMapPoint = MapPoint.mapPointWithGeoCoord(mMapX, mMapY);
        mMapView.setMapCenterPoint(mMapPoint,true);
        //?????? ?????????
        MapCircle circle1 = new MapCircle(
                mMapPoint,//??????
                radius,//??????
                Color.argb(128,255,0,0),//strokecolor?????????
                Color.argb(0,0,0,0)//fillcolor????????????

        );
        circle1.setTag(1);
        //??????????????? ?????? ????????? ?????? ??????
        MapPointBounds[] mapPointBoundsArray = {circle1.getBound()};
        MapPointBounds mapPointBounds = new MapPointBounds(mapPointBoundsArray);
        int padding = 50;
        mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds,padding));

        mMapView.addCircle(circle1);
    }
    //?????? ??????
    private void removeCircle(){
        mMapView.removeAllCircles();
    }

    //????????????
    @Override
    public void onTilt(float x, float y) {
        if(sensor_control){
            //???????????? ??????
            mCurrentLat = x /1000;
            mCurrentLng = y /1000;
            //?????? ?????? ?????? ????????????
            double lat = mMapView.getMapCenterPoint().getMapPointGeoCoord().latitude;
            double lng = mMapView.getMapCenterPoint().getMapPointGeoCoord().longitude;
            mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lat - mCurrentLng , lng + mCurrentLat),false);
        }

    }

    //init View
    private void init(){

        //?????? ?????? ??????
        plusBtn = (Button)findViewById(R.id.zoomIn);
        minusBtn = (Button)findViewById(R.id.zoomOut);
        zoomIn = (TextView)findViewById(R.id.zoomIn_text);
        zoomOut = (TextView)findViewById(R.id.zoomOut_text);
        //?????? ?????? ????????????
        map_Up = (TextView)findViewById(R.id.map_up);
        map_Down = (TextView)findViewById(R.id.map_down);
        map_Left = (TextView)findViewById(R.id.map_left);
        map_Right = (TextView)findViewById(R.id.map_right);
        //????????????
        locOn = (TextView)findViewById(R.id.locOn);
        locOff = (TextView)findViewById(R.id.locOff);
        wattH = (TextView)findViewById(R.id.wattH);
        watt = (Button) findViewById(R.id.watt);
        myLoc = (Button)findViewById(R.id.myLoc);
        LocCancel = (Button)findViewById(R.id.myLocCancel);
        sensorStop = (Button)findViewById(R.id.sensorStop);
        sensorStart = (Button)findViewById(R.id.sensorStart);

        circleVal = (TextView)findViewById(R.id.circleVal);
        //circle ??????
        circle1 = (TextView)findViewById(R.id.circle1);
        circle2 = (TextView)findViewById(R.id.circle2);
        circle3 = (TextView)findViewById(R.id.circle3);
        circle4 = (TextView)findViewById(R.id.circle4);
        circleR = (TextView)findViewById(R.id.circleR);
        //????????????
        Loc1 = (TextView)findViewById(R.id.loc1);
        Loc2 = (TextView)findViewById(R.id.loc2);
        Loc3 = (TextView)findViewById(R.id.loc3);
        Loc4 = (TextView)findViewById(R.id.loc4);
        Loc5 = (TextView)findViewById(R.id.loc5);
        LocMakDel = (TextView)findViewById(R.id.locMarkDel);
        //???????????? ?????????
        Loc1.setOnClickListener(moveLcaMarker);
        Loc2.setOnClickListener(moveLcaMarker);
        Loc3.setOnClickListener(moveLcaMarker);
        Loc4.setOnClickListener(moveLcaMarker);
        Loc5.setOnClickListener(moveLcaMarker);
        LocMakDel.setOnClickListener(moveLcaMarker);
        //Map ?????????
        //?????? ??????/??????/?????? ?????????
        mMapView.setMapViewEventListener(this);
        //??? ?????? ??????
        mMapView.setCurrentLocationEventListener(this);
        //????????? ?????? ?????????
        mMapView.setPOIItemEventListener(this);
        //??? ?????? ?????????
        zoomIn.setOnClickListener(ZoomControl);
        zoomOut.setOnClickListener(ZoomControl);
        //?????? ?????? ?????????
        map_Up.setOnClickListener(MapMoveControl_v);
        map_Down.setOnClickListener(MapMoveControl_v);
        map_Right.setOnClickListener(MapMoveControl_v);
        map_Left.setOnClickListener(MapMoveControl_v);
        //????????? ?????? ?????????
        locOn.setOnClickListener(myLocation_v);
        locOff.setOnClickListener(myLocation_v);
        //?????? ??????
        wattH.setOnClickListener(myLocation_v);
        //?????? ??????
        sensorStop.setOnClickListener(Sensor_control);
        sensorStart.setOnClickListener(Sensor_control);
        //???????????? ????????????
        circle1.setOnClickListener(circle_voice);
        circle2.setOnClickListener(circle_voice);
        circle3.setOnClickListener(circle_voice);
        circle4.setOnClickListener(circle_voice);
        circleR.setOnClickListener(circle_voice);

        mTiltScrollController = new TiltScrollController(getApplicationContext(), this);


        //????????? ???????????? ?????? ?????? view & ?????????//
        mapUp = (Button)findViewById(R.id.mapup);
        mapDown = (Button)findViewById(R.id.mapdown);
        mapRight = (Button)findViewById(R.id.mapright);
        mapLeft = (Button)findViewById(R.id.mapleft);
        //circle
        circle500 = (Button)findViewById(R.id.circle500);
        circle1000 = (Button)findViewById(R.id.circle1000);
        circle3000 = (Button)findViewById(R.id.circle3000);
        circle5000 = (Button)findViewById(R.id.circle5000);
        removeC = findViewById(R.id.removeC);
        //???????????? ?????? ?????????
        circle500.setOnClickListener(circle_control);
        circle1000.setOnClickListener(circle_control);
        circle3000.setOnClickListener(circle_control);
        circle5000.setOnClickListener(circle_control);
        removeC.setOnClickListener(circle_control);
        //????????? ?????????
        myLoc.setOnClickListener(myLocation);
        LocCancel.setOnClickListener(myLocation);
        //????????????
        watt.setOnClickListener(myLocation);
        //?????? ?????? ?????????
        mapUp.setOnClickListener(MapMoveControl);
        mapDown.setOnClickListener(MapMoveControl);
        mapRight.setOnClickListener(MapMoveControl);
        mapLeft.setOnClickListener(MapMoveControl);
        //??? ?????? ?????????
        plusBtn.setOnClickListener(ButtonZoomCon);
        minusBtn.setOnClickListener(ButtonZoomCon);

    }

    //?????? ????????? ?????? ?????????
    //???????????? ?????? ?????????
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

    //?????? ?????? ?????? ?????????
    private View.OnClickListener MapMoveControl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Double lat = mMapView.getMapCenterPoint().getMapPointGeoCoord().latitude;
            Double lng = mMapView.getMapCenterPoint().getMapPointGeoCoord().longitude;

            switch (v.getId()){
                case R.id.mapup:
                    mMapX = lat + 0.0035;
                    mMapY = lng;
                    break;
                case R.id.mapdown:
                    mMapX = lat - 0.0035;
                    mMapY = lng;
                    break;
                case R.id.mapleft:
                    mMapX = lat;
                    mMapY = lng - 0.0035;
                    break;
                case R.id.mapright:
                    mMapX = lat;
                    mMapY = lng + 0.0035;
                    break;
            }
            mMapPoint = MapPoint.mapPointWithGeoCoord(mMapX, mMapY);
            //?????? ????????? ????????????
            mZoomLevel = mMapView.getZoomLevel();
            //?????? ?????? update
            CameraUpdate cameraUpdate = CameraUpdateFactory.newMapPoint(mMapPoint,mZoomLevel);
            //????????????
            mMapView.moveCamera(cameraUpdate);
        }
    };

    //???????????? ?????? ?????????
    private View.OnClickListener circle_control = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.circle500:
                    customCircle(500);
                    circleVal.setText("?????? ?????? : 500??????");
                    break;
                case R.id.circle1000:
                    customCircle(1000);
                    circleVal.setText("?????? ?????? : 1Km");
                    break;
                case R.id.circle3000:
                    customCircle(3000);
                    circleVal.setText("?????? ?????? : 3Km");
                    break;
                case R.id.circle5000:
                    customCircle(5000);
                    circleVal.setText("?????? ?????? : 5Km");
                    break;
                case R.id.removeC:
                    removeCircle();
                    circleVal.setText("");
                    break;
            }
        }
    };

    //????????? ?????? ?????????
    private View.OnClickListener myLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.myLoc:
                    startTracking();
                    Log.d(TAG, "???????????? ??????");
                    LocCancel.setVisibility(View.VISIBLE);
                    myLoc.setVisibility(View.GONE);
                    break;
                case R.id.myLocCancel:
                    stopTracking();
                    Log.d(TAG,"???????????? ??????");
                    LocCancel.setVisibility(View.GONE);
                    myLoc.setVisibility(View.VISIBLE);
                    break;
                //watt ??????
                case R.id.watt:
                    mMapPoint = MapPoint.mapPointWithGeoCoord(37.43225475043913, 127.17844582341077);
                    //?????? ????????? ????????????
                    mZoomLevel = mMapView.getZoomLevel();
                    //?????? ?????? update
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newMapPoint(mMapPoint,mZoomLevel);
                    //????????????
                    mMapView.moveCamera(cameraUpdate);
                    break;
            }
        }
    };

}