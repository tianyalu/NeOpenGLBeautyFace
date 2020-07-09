package com.sty.ne.opengl.beautyface.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;


import com.sty.ne.opengl.beautyface.BuildConfig;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


/**
 * Created by Shi Tianyi on 2020/2/3/0003.
 */
public class PermissionUtils {
    public static final int REQUEST_PERMISSIONS_CODE = 23654;
    public static final int REQUEST_PERMISSIONS_SETTING_CODE = 23655;


    /**
     * 检查权限
     * @param activity
     * @param permissions
     * @return
     */
    public static boolean checkPermissions(Activity activity, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> needRequestPermissionList = findDeniedPermissions(activity, permissions);
            if (null != needRequestPermissionList
                    && needRequestPermissionList.size() > 0) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissions(Activity activity, String... permissions) {
        List<String> needRequestPermissionList = findDeniedPermissions(activity, permissions);
        if (null != needRequestPermissionList
                && needRequestPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(activity,
                    needRequestPermissionList.toArray(
                            new String[needRequestPermissionList.size()]),
                    REQUEST_PERMISSIONS_CODE);
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     * @param activity
     * @param permissions
     * @return
     */
    public static List<String> findDeniedPermissions(Activity activity, String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String perm : permissions) {
                if (ContextCompat.checkSelfPermission(activity,
                        perm) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, perm)) {
                    needRequestPermissionList.add(perm);
                }
            }
        }
        return needRequestPermissionList;
    }

    /**
     * 检测是否所有的权限都已经授权
     *
     * @param grantResults
     * @return
     */
    public static boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void startAppDetailSettingIntent(Context context, int code){
        Intent localIntent = new Intent(); Intent intent = new Intent(); intent.putExtra("11", "11");
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", BuildConfig.APPLICATION_ID);
        }
        if (context instanceof Activity){
            try {
                Activity activity = (Activity) context;
                activity.startActivityForResult(localIntent, code);
            }catch (Exception e){
                context.startActivity(localIntent);
            }
        }else {
            context.startActivity(localIntent);
        }
    }

    public static void startAppSettings(Activity activity) {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
//        context.startActivity(intent);
        activity.startActivityForResult(intent, REQUEST_PERMISSIONS_SETTING_CODE);
    }

    public static void showMissingPermissionDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("提示");
        builder.setMessage("当前应用缺少必要权限。\n\n请点击\"设置\"-\"权限\"-打开所需权限");

        // 拒绝
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.setPositiveButton("设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        startAppDetailSettingIntent(context, REQUEST_PERMISSIONS_SETTING_CODE);
                        startAppSettings(activity);
                    }
                });
        builder.setCancelable(false);

        builder.show();
    }
}
