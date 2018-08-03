package com.sunxy.module_2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.sunxy.router.annotation.Route;
import com.sunxy.router.annotation.model.RouteMeta;
import com.sunxy.router.core.RouterCore;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/3 0003.
 */
@Route(path = "moudle2/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        RouterCore.getInstance().init(getApplication());


        findViewById(R.id.toOne).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouteMeta routeMeta = RouterCore.getInstance().loadRouteMeta("main/MainActivity");
                Intent intent = new Intent(MainActivity.this, routeMeta.getDestination());
                startActivity(intent);
            }
        });

        findViewById(R.id.toTwo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouteMeta routeMeta = RouterCore.getInstance().loadRouteMeta("moudle1/MainActivity");
                Intent intent = new Intent(MainActivity.this, routeMeta.getDestination());
                startActivity(intent);
            }
        });
    }
}
