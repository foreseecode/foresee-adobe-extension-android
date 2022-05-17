package com.verint.xm.adobeextensionsample;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.adobe.marketing.mobile.MobileCore;
import com.verint.xm.sdk.Core;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        adobeTrackActivity(this);
    }

    public void resetCounters(View view) {
        // Reset the SDK state
        Core.resetState();
    }

    private static void adobeTrackActivity(Activity activity) {
        Map<String, String> contextData = new HashMap<>();

        contextData.put("screenName", (String) activity.getTitle());
        MobileCore.trackState("newScreen", contextData);
    }
}
