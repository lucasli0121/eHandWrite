package com.libwriting.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

/**
 *
 */
public class PermissionUtil {

    public static final int REQUEST_CODE_ASK_PERMISSIONS = 100;

    public static boolean checkPermission(Activity activity, String permission){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int storagePermission = ActivityCompat.checkSelfPermission(activity, permission);
            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    public static void showPermissionDialog(final Activity activity, String permission) {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,permission)) {

            ActivityCompat.requestPermissions(activity, new String[]{permission},
                    PermissionUtil.REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }

        ActivityCompat.requestPermissions(activity,new String[]{permission},
                PermissionUtil.REQUEST_CODE_ASK_PERMISSIONS);

    }

    public static void showPermissionGroupDialog(final Activity activity, String[] permissions) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, permissions, PermissionUtil.REQUEST_CODE_ASK_PERMISSIONS);
        }
    }


    public static void showAppSettingDialog(final Activity activity){
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setMessage("为了在Android M上正常运行，需要您的授权.")
                .setNegativeButton("取消",null)
                .setPositiveButton("确定",null)
                .show();
      /*  Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, PermissionUtil.REQUEST_PERMISSION_SETTING);*/
    }

}
