package com.leibown.myinspectwechatfriend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Toast;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button btnStartCheck;

    public static final String LauncherUI = "com.tencent.mm.ui.LauncherUI";
    public static final String MM = "com.tencent.mm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStartCheck = (Button) findViewById(R.id.btn_start_check);
        btnStartCheck.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_check:
                startCheck();
                break;
        }
    }

    /**
     * 开始检查
     */
    private void startCheck() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(new AccessibilityManager.AccessibilityStateChangeListener() {
            @Override
            public void onAccessibilityStateChanged(boolean b) {
                if(b){
                    Intent intent = new Intent();
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    intent.setClassName(MM, LauncherUI);
                    InspectWechatFriendService.hasComplete = false;
                    InspectWechatFriendService.allComplete = false;
                    startActivity(intent);
                }else{
                    try {
                        //打开系统设置中辅助功能
                        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                        Toast.makeText(MainActivity.this, "找到检测被删好友辅助，然后开启服务即可", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        if(!accessibilityManager.isEnabled()){
            try {
                //打开系统设置中辅助功能
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                Toast.makeText(MainActivity.this, "找到检测被删好友辅助，然后开启服务即可", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Intent intent = new Intent();
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(MM, LauncherUI);
            InspectWechatFriendService.hasComplete = false;
            InspectWechatFriendService.allComplete = false;
            startActivity(intent);
        }
    }
}
