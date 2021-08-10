package com.peng.plant.wattmap_kakaoapi;

import android.view.View;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;

public class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
    @Override
    public View getCalloutBalloon(MapPOIItem mapPOIItem) {
        return null;
    }

    @Override
    public View getPressedCalloutBalloon(MapPOIItem mapPOIItem) {
        return null;
    }
}
