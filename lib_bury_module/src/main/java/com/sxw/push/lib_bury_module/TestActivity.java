package com.sxw.push.lib_bury_module;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.sxw.burypoint.api.annotation.FastClickIntercept;

/**
 * Created by zhanghai on 2019/10/17.
 * function：
 */
public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_button_1;
    private Button btn_button_2;
    private Button btn_button_3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        initView();
    }

    private void initView(){
        btn_button_1 = findViewById(R.id.btn_button_1);
        btn_button_2 = findViewById(R.id.btn_button_2);
        btn_button_3 = findViewById(R.id.btn_button_3);
    }

    @FastClickIntercept
    @Override
    public void onClick(View v) {
        System.out.println("按下了按钮 ====》 " + v.getId());
    }
}
