package com.peng.plant.wattmap_kakaoapi.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.peng.plant.wattmap_kakaoapi.R;
import com.peng.plant.wattmap_kakaoapi.activity.ImageActivity;

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
////        ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText("Custom CalloutBalloon");
//        ImageView imageView = mCalloutBalloon.findViewById(R.id.badge);
//        imageView.setImageBitmap(poiItem.getCustomCalloutBalloonBitmap());
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Context context = view.getContext();
//                //여기서 이미지 액티비티로 이동하는 코드 넣으면 됨
//                Intent intent = new Intent(view.getContext(), ImageActivity.class);
//                intent.putExtra("image",poiItem.getItemName());
//                context.startActivity(intent);
//            }
//        });
        return mCalloutBalloon;
    }

    @Override
    public View getPressedCalloutBalloon(MapPOIItem poiItem) {
        return null;
    }




}