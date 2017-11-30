package me.majiajie.barcodereader.ui;


import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

public class CameraUtils {

    private CameraUtils() {
    }

    /**
     * 检查是否存在摄像头
     */
    public static boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 获取后置摄像头实例
     *
     * @return 如果发生异常或者没有后置的摄像头就返回null
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    /**
     * 获取前置摄像头实例
     *
     * @return 如果发生异常或者没有前置的摄像头就返回null
     */
    public static Camera getFrontCameraInstance() {
        try {
            int numberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    return Camera.open(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断摄像头是否是背部的
     */
    public static boolean isBack(int id) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(id, cameraInfo);

        return cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     * 判断摄像方向，返回值是 0、90、180、270 。 顺时针旋转，0表示正常状态(默认是横向的)
     */
    public static int orientation(int id) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(id, cameraInfo);

        return cameraInfo.orientation;
    }

    /**
     * 判断是否可以通过{@link Camera#enableShutterSound(boolean)}禁用相机的快门声音
     */
    public static boolean canDisableShutterSound(int id) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(id, cameraInfo);
        return cameraInfo.canDisableShutterSound;
    }

}
