package com.peng.plant.wattmap_kakaoapi.view;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;

public class CommandView extends View {

    public CommandView(Activity context , String cmd , int resId) {
        super(context);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(1,1 ,1 ,1,1);
        this.setContentDescription(String.format("hf_no_number|%s",cmd));
        this.setId(resId);
        context.addContentView(this , lp);
    }
}
