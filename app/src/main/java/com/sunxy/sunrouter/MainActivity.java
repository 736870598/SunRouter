package com.sunxy.sunrouter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.sunxy.router.annotation.Route;
import com.sunxy.router.annotation.model.RouteMeta;
import com.sunxy.router.core.RouterCore;

@Route(path = "/main/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RouterCore.getInstance().init(getApplication());


        findViewById(R.id.toOne).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouteMeta routeMeta = RouterCore.getInstance().loadRouteMeta("moudle1/MainActivity");
                Intent intent = new Intent(MainActivity.this, routeMeta.getDestination());
                startActivity(intent);
            }
        });

        findViewById(R.id.toTwo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouteMeta routeMeta = RouterCore.getInstance().loadRouteMeta("moudle2/MainActivity");
                Intent intent = new Intent(MainActivity.this, routeMeta.getDestination());
                startActivity(intent);
            }
        });
    }
}
