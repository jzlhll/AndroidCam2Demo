package com.allan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.allan.cam2api.MainActivity;
import com.allan.cam2api.R;

public class EnterActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
    }

    public void onClickedEnterButton(View v) {
        if (v.getId() == R.id.enterBtn1) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        } else if (v.getId() == R.id.enterBtn2) {
           // startActivity(new Intent(getApplicationContext(), com.allan.cam2buffer.MainActivity.class));
        }
    }
}
