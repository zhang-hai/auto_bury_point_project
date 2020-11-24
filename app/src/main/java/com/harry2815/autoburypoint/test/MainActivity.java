package com.harry2815.autoburypoint.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;

import com.sxw.burypoint.api.annotation.AutoLog;
import com.sxw.burypoint.api.annotation.FastClickIntercept;
import com.sxw.burypoint.api.annotation.MethodTraceTime;


public class MainActivity extends AppCompatActivity {

    @AutoLog(method = AutoLog.D,tag = "MainActivity",msg = "onCreate "+content)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_click).setOnClickListener(new View.OnClickListener() {
            @Override
            @FastClickIntercept
            public void onClick(View v) {
                System.out.println("点击了按钮  ");
                int p = 1;
                System.out.println(" 0 ---> " + p);

            }
        });

        init();
    }

    @FastClickIntercept(intervalTime = 1000)
    private void onItemChildClick(Adapter adapter,View view,int position){
        init();
    }

    private final String content = " ABC";

    @AutoLog(tag = "MainActivity",msg = "init "+content)
    @MethodTraceTime
    private void init(){
        int p = 0;
        for (int i = 0;i < 30000;i++){
            p += i;
        }
        Log.i("Tag","(0 + 1 + 2 + ... + 30000) = " + p );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
