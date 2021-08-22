package com.peng.plant.wattmap_kakaoapi.adapter;


import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.peng.plant.wattmap_kakaoapi.R;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;

// CalloutBalloonAdapter 인터페이스 구현
public class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
    private final View mCalloutBalloon;

    public CustomCalloutBalloonAdapter(Activity activity) {
        Log.d("TAGTAGTAG", "activity");
        mCalloutBalloon = activity.getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
    }

    @Override
    public View getCalloutBalloon(MapPOIItem poiItem) {
        Log.d("TAGTAGTAG", poiItem.getItemName());
        ((ImageView) mCalloutBalloon.findViewById(R.id.badge)).setImageBitmap(poiItem.getCustomImageBitmap());
//        ((TextView) mCalloutBalloon.findViewById(R.id.imageTitle)).setText(poiItem.getItemName());
//        ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText("Custom CalloutBalloon");
        return mCalloutBalloon;
    }

    @Override
    public View getPressedCalloutBalloon(MapPOIItem poiItem) {
        return null;
    }
}