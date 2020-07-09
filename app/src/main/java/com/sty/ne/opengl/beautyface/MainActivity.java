package com.sty.ne.opengl.beautyface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sty.ne.opengl.beautyface.util.PermissionUtils;
import com.sty.ne.opengl.beautyface.view.MyGLSurfaceView;
import com.sty.ne.opengl.beautyface.view.MyRecordButton;

public class MainActivity extends AppCompatActivity {
    private String[] needPermissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private MyGLSurfaceView mGLSurfaceView;
    private MyRecordButton mRecordButton;
    private RadioGroup rgRecordSpeed;
    private CheckBox cbBigEyes;
    private CheckBox cbStick;
    private CheckBox cbBeauty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!PermissionUtils.checkPermissions(this, needPermissions)) {
            PermissionUtils.requestPermissions(this, needPermissions);
        }
        initView();
    }

    private void initView() {
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mRecordButton = findViewById(R.id.btn_record);
        rgRecordSpeed = findViewById(R.id.rg_record_speed);
        cbBigEyes = findViewById(R.id.cb_big_eyes);
        cbStick = findViewById(R.id.cb_stick);
        cbBeauty = findViewById(R.id.cb_beauty);

        mRecordButton.setOnRecordListener(new MyRecordButton.OnRecordListener() {
            @Override
            public void onStartRecording() {
                //开始录制
                mGLSurfaceView.startRecording();
            }

            @Override
            public void onStopRecording() {
                //停止录制
                mGLSurfaceView.stopRecording();
                Toast.makeText(MainActivity.this, "录制完成", Toast.LENGTH_SHORT).show();
            }
        });

        rgRecordSpeed.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //选择录制模式
                switch (checkedId) {
                    case R.id.rbtn_record_speed_extra_slow: //极慢
                        mGLSurfaceView.setSpeedMode(MyGLSurfaceView.Speed.EXTRA_SLOW);
                        break;
                    case R.id.rbtn_record_speed_slow: //慢
                        mGLSurfaceView.setSpeedMode(MyGLSurfaceView.Speed.SLOW);
                        break;
                    case R.id.rbtn_record_speed_normal: //正常
                        mGLSurfaceView.setSpeedMode(MyGLSurfaceView.Speed.NORMAL);
                        break;
                    case R.id.rbtn_record_speed_fast: //快
                        mGLSurfaceView.setSpeedMode(MyGLSurfaceView.Speed.FAST);
                        break;
                    case R.id.rbtn_record_speed_extra_fast: //极快
                        mGLSurfaceView.setSpeedMode(MyGLSurfaceView.Speed.EXTRA_FAST);
                        break;
                    default:
                        break;
                }
            }
        });

        cbBigEyes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("sty", "isChecked: " + isChecked );
                mGLSurfaceView.enableBigEyes(isChecked);
            }
        });

        cbStick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //贴纸
                mGLSurfaceView.enableStick(isChecked);
            }
        });

        cbBeauty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //美颜
                mGLSurfaceView.enableBeauty(isChecked);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_PERMISSIONS_CODE) {
            if (!PermissionUtils.verifyPermissions(grantResults)) {
                PermissionUtils.showMissingPermissionDialog(this);
            } else {

            }
        }
    }

}