package com.sunxy.sunbutterknife;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.sunxy.sunbutterknife.annotation.BindView;
import com.sunxy.sunbutterknife.annotation.UnBind;
import com.sunxy.sunbutterknife.core.SunBfCore;

public class SunButterKnifeActivity extends AppCompatActivity {

    @BindView(R.id.textview)
    TextView textView;
    @BindView(R.id.textview2)
    TextView textView2;

    private UnBind unBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sun_butter_knife);
        unBind = SunBfCore.bind(this);
        textView.setText("0-----sd----00");
        textView2.setText("textView2textView2");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBind.unBind();
    }
}
