package com.techtown.ainglish.customView;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class PermissionUtils {
    //String...은 가변인자로 매개변수가 0개부터 여러개, 배열을 받을 수 있다는 의미
    public static boolean requestPermission(
            Activity activity, int requestCode, String... permissions) {
        boolean granted = true;
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        //여러개 요청받은 권한들에 대해서 하나하나 for문으로 돈다.
        for (String s : permissions) {
            //권한이 있는지 self로 물어본다.
            int permissionCheck = ContextCompat.checkSelfPermission(activity, s);
            //permissionCheck된 값이 허용된 값이라면 hasPermission은 true가 된다.
            boolean hasPermission = (permissionCheck == PackageManager.PERMISSION_GRANTED);
            //&=연산자로 hasPermission을 granted에 대입
            granted &= hasPermission;
            //권한 false라면 add한다. 그리고 else로 ActivityCompat으로 request한다.
            if (!hasPermission) {
                permissionsNeeded.add(s);
            }
        }

        if (granted) {
            return true;
        } else {
            //실제 권한을 요청하는 코드
            /**
             * @param
             * permissionsNeeded배열을 갖고 요청을 한다.
             * requestCode는
             */
            ActivityCompat.requestPermissions(activity,
                    permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                    requestCode);
            return false;
        }
    }


    public static boolean permissionGranted(
            int requestCode, int permissionCode, int[] grantResults) {
        return requestCode == permissionCode && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }
}
