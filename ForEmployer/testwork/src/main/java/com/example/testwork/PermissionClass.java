package com.example.testwork;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionClass {
    public final static String[] PERMISSION_ARRAY_WRITE_DIRECTORY={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
    public final static int[] PERMISSION_ARRAY_DIALOG={1};

    public static boolean checkedPermission(Context context, String[] PERMISSION_ARRAY){
        for (String s:PERMISSION_ARRAY) {
            if((ContextCompat.checkSelfPermission(context,s))!=(PackageManager.PERMISSION_GRANTED)){
                return false;
            }
        }
        return true;
    }

    public static void createPermissionDialog(Activity activity, String[] PERMISSION_ARRAY, int PERMISSION_DIALOG){
        ActivityCompat.requestPermissions(activity,PERMISSION_ARRAY,PERMISSION_DIALOG);
    }

}
