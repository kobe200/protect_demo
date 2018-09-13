package carnetapp.usbmediadata.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: kobe
 * @date: 2018/8/6 11:08
 * @decribe:系统权限管理类：申请系统权限
 */

public class PermissionUtils {
    private static PermissionUtils permissionUtils = new PermissionUtils();
    private final String tag = PermissionUtils.class.getSimpleName();
    private final int SUCCES = 1000;
    private final int REQUEST_CODE = 1;
    private Context context;
    private List<PermissionBean> permissions = new ArrayList<>();

    public static PermissionUtils getInstance() {
        return permissionUtils;
    }

    private PermissionUtils() {
    }

    public void init(Context context) {
        this.context = context;
    }

    public boolean checkPermission(Activity activity,String permission) {
        LogUtils.i(tag,"==checkPermission==" + permission + "|" + activity);
        if(TextUtils.isEmpty(permission) || activity == null) {
            return false;
        }
        LogUtils.i(tag,"==checkPermission1==" + Build.VERSION.SDK_INT + "|" + Build.VERSION_CODES.M + "|" + permissions.size());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for(PermissionBean pb : permissions) {
                if(permission.equals(pb.permission)) {
                    return pb.permissionId == SUCCES;
                }
            }
            if(ContextCompat.checkSelfPermission(activity,permission) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(new PermissionBean(permission));
                LogUtils.i(tag,"==checkPermission2==" + permissions.size());
                return false;
            }
            permissions.add(new PermissionBean(permission,SUCCES));
        }
        LogUtils.i(tag,"==checkPermission3==" + permissions.size());
        return true;
    }

    public void requestPermission(Activity activity,List<String> permission) {
        LogUtils.i(tag,"==requestPermission==" + permission + "|" + activity);
        if(permission == null || activity == null) {
            return;
        }
        for(int i = permission.size() - 1; i >= 0; i--) {
            if(checkPermission(activity,permission.get(i))) {
                LogUtils.i(tag,"==requestPermission is yes ==" + permission.get(i));
                permission.remove(i);
            }
        }
        LogUtils.i(tag,"==requestPermission==" + permission.size());
        if(permission.size() < 1) {
            return;
        }
        String[] perm = permission.toArray(new String[permission.size()]);
        LogUtils.i(tag,"==requestPermission==" + perm.length);
        ActivityCompat.requestPermissions(activity,perm,REQUEST_CODE);
    }

    public void onRequestPermissionsResult(Activity activity,int requestCode,String[] permission,int[] grantResults) {
        if(requestCode == REQUEST_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("权限已申请");
                for(PermissionBean pb:permissions){
                    pb.permissionId = SUCCES;
                }
            } else {
                showToast("权限已拒绝");
            }
        }
    }

    private void showToast(String string) {
        if(context != null) {
            Toast.makeText(context,string,Toast.LENGTH_LONG).show();
        }
    }

    public class PermissionBean {
        public String permission = null;
        public int permissionId = -1;

        public PermissionBean(String permission) {
            this.permission = permission;
        }

        public PermissionBean(String permission,int permissionId) {
            this.permission = permission;
            this.permissionId = permissionId;
        }
    }


}
