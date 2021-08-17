package com.peng.plant.wattmap_kakaoapi;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;

class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
    private final View mCalloutBalloon;

    public CustomCalloutBalloonAdapter(Activity activity) {
        mCalloutBalloon = activity.getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
    }

    public CustomCalloutBalloonAdapter(View mCalloutBalloon) {

        this.mCalloutBalloon = mCalloutBalloon;
    }

    @Override
    public View getCalloutBalloon(MapPOIItem poiItem) {
        Log.d("asdfasdfasdfasd", poiItem.getItemName());
        ((ImageView) mCalloutBalloon.findViewById(R.id.badge)).setImageResource(R.drawable.ic_launcher_foreground);
//        ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText("Custom CalloutBalloon");
        return mCalloutBalloon;
    }

    @Override
    public View getPressedCalloutBalloon(MapPOIItem poiItem) {
        return null;
    }
}