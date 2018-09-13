package carnetapp.usbmediadata.model;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import java.util.List;

import carnetapp.usbmediadata.utils.ConstantsUtils;
import carnetapp.usbmediadata.utils.DeviceUtils;
import carnetapp.usbmediadata.utils.LogUtils;
import carnetos.usbservice.aidl.IMediaClient;
import carnetos.usbservice.aidl.IMediaService;

import static carnetapp.usbmediadata.utils.DeviceUtils.DEVICE;
import static carnetapp.usbmediadata.utils.DeviceUtils.getMountedPath;

/**
 * @author kobe
 * @date 2018/5/22 13:56
 * 扫描服务客户端服务
 */
public class MediaUsbService extends Service {
    private static final String TAG = MediaUsbService.class.getSimpleName();
    public static IMediaService iMediaService;
    public static MediaUsbService mediaUsbService = null;
    private final long delayMillis = 200;
    private Context mContext;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LogUtils.i(TAG + "_handleMessage ->" + msg.what);
            switch(msg.what) {
                case ConstantsUtils.FileType.MUSIC:
                case ConstantsUtils.FileType.VIDEO:
                case ConstantsUtils.FileType.IMAGE:
                case ConstantsUtils.FileType.OTHER:
                    noticeMediaDataChanged(msg.what);
                    break;
                case ConstantsUtils.MEDIA_SCAN_FINISHED:
                    isFileScanFinished(msg.obj + "");
                    break;
                case ConstantsUtils.MEDIA_METADATA_RETRIEVED:
                    mediaParseFinished(msg);
                    break;
                case ConstantsUtils.MEDIA_USB_DISMOUNTED:
                    handleUsbDisUnMounted(msg.obj + "");
                    break;
                case ConstantsUtils.MEDIA_USB_MOUNTED:
                    handleUsbDiskMounted(msg.obj + "");
                    break;
            }
        }
    };

    /**
     * 客户端实现，由服务端调用
     */
    private IMediaClient.Stub iMediaClient = new IMediaClient.Stub() {

        @Override
        public void onAudioFilesAdded() throws RemoteException {
            LogUtils.i("scan",TAG + "_AudioFilesAdded");
            mHandler.removeMessages(ConstantsUtils.FileType.MUSIC);
            mHandler.sendEmptyMessageDelayed(ConstantsUtils.FileType.MUSIC,delayMillis);
        }

        @Override
        public void onImageFilesAdded() throws RemoteException {
            LogUtils.i("scan",TAG + "_ImageFilesAdded");
            mHandler.removeMessages(ConstantsUtils.FileType.IMAGE);
            mHandler.sendEmptyMessageDelayed(ConstantsUtils.FileType.IMAGE,delayMillis);
        }

        @Override
        public void onVideoFilesAdded() throws RemoteException {
            LogUtils.i("scan",TAG + "_VideoFilesAdded->");
            mHandler.removeMessages(ConstantsUtils.FileType.VIDEO);
            mHandler.sendEmptyMessageDelayed(ConstantsUtils.FileType.VIDEO,delayMillis);
        }

        @Override
        public void onOfficeFilesAdded() throws RemoteException {
            LogUtils.i("scan",TAG + "_onOfficeFilesAdded->");
            mHandler.removeMessages(ConstantsUtils.FileType.OTHER);
            mHandler.sendEmptyMessageDelayed(ConstantsUtils.FileType.OTHER,delayMillis);
        }

        @Override
        public void onMediaFilesRemoved(String path) throws RemoteException {
        }

        @Override
        public void onMediaFilesCleared(String rootPath) throws RemoteException {
        }

        @Override
        public void onFileScanFinished(String rootPath) throws RemoteException {
            LogUtils.i("scan",TAG + "_onFileScanFinished->" + rootPath);
            mHandler.obtainMessage(ConstantsUtils.MEDIA_SCAN_FINISHED,rootPath).sendToTarget();
        }

        @Override
        public void onMetadataRetrieved(int mediaType,String path) throws RemoteException {
            LogUtils.i("scan",TAG + "onMetadataRetrieved->" + mediaType + "|" + path);
            Message message = mHandler.obtainMessage(ConstantsUtils.MEDIA_METADATA_RETRIEVED);
            message.arg1 = mediaType;
            message.obj = path;
            message.sendToTarget();
        }

        @Override
        public void onBackgroundMetadataRetrieved(int mediaType,String path) throws RemoteException {
            LogUtils.i("scan",TAG + "onBackgroundMetadataRetrieved->" + mediaType + "|" + path);
            Message message = mHandler.obtainMessage(ConstantsUtils.MEDIA_METADATA_RETRIEVED);
            message.arg1 = mediaType;
            message.obj = path;
            message.sendToTarget();
        }

        @Override
        public void onAllMetadataRetrieved(int count) throws RemoteException {
        }

        @Override
        public void onUsbDiskMounted(String path) throws RemoteException {
            LogUtils.i("scan",TAG + "_onUsbDiskMounted->" + path);
            mHandler.obtainMessage(ConstantsUtils.MEDIA_USB_MOUNTED,path).sendToTarget();
        }

        @Override
        public void onUsbDiskUnMounted(String path) throws RemoteException {
            LogUtils.i("scan",TAG + "_onUsbDiskUnMounted->" + path);
            //由于从扫描库需要处理完本进程内的逻辑才会上发本事件，事件不能及时发送到UI层，因此此处采用监听系统广播的方式继续处理
//            mHandler.obtainMessage(ConstantsUtils.MEDIA_USB_DISMOUNTED,path).sendToTarget();
        }

    };
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            if(iMediaService == null) {
                return;
            }
            iMediaService.asBinder().unlinkToDeath(mDeathRecipient,0);
            iMediaService = null;
            bindUsbScanService();
        }
    };
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iMediaService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,IBinder service) {
            iMediaService = IMediaService.Stub.asInterface(service);
            LogUtils.i("==onServiceConnected==" + service + "|" + iMediaService);
            if(iMediaService != null) {
                try {
                    iMediaService.registerCallback(iMediaClient);
                    service.linkToDeath(mDeathRecipient,0);
//                    MusicScanManager.getInstance().handleRequestData();
//                    VideoScanManager.getInstance().handleRequestData();
//                    ImageScanManager.getInstance().handleRequestData();
                } catch(RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void handleUsbDiskMounted(String path) {
        if(TextUtils.isEmpty(path)) {
            return;
        }
        path = path.replace("file://","");
        DeviceUtils.setMounted(path);
        if(DeviceUtils.isUsbExist(true)) {
            List<String> mountedPath = getMountedPath(true);
            usbDiskMounted(mountedPath);
        }
    }

    private void handleUsbDisUnMounted(String path) {
        if(TextUtils.isEmpty(path)) {
            return;
        }
        path = path.replace("file://","");
        DeviceUtils.setUnMounted(path);
        usbDiskUnMounted(path);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i("MediaUsbService_onCreate ->" + mediaUsbService);
        if(mediaUsbService != null) {
            return;
        }
        mediaUsbService = this;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        LogUtils.i("MediaUsbService_init");
        mContext = this;
        bindUsbScanService();
        //注册USB插拔事件监听广播
        registerBroadcastEvents(mContext);
    }

    /**
     * 绑定扫描服务
     */
    private void bindUsbScanService() {
        LogUtils.i("MediaUsbService_bindUsbScanService");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("carnetos.usbmedia","carnetos.usbservice.aidl.UsbService"));
        startService(intent);
        bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
    }

    /**
     * 解绑扫描服务
     */
    private void unBindUsbScanService() {
        if(iMediaService != null) {
            try {
                iMediaService.unregisterCallback(iMediaClient);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
        Intent it = new Intent();
        it.setAction(ConstantsUtils.USB_BIND_ACTION);
        unbindService(serviceConnection);
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId) {
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unBindUsbScanService();
        //反注册USB插拔事件监听广播
        unregisterBroadcastEvents(mContext);
    }

    private void usbDiskMounted(List<String> mountedPath) {
        LogUtils.i("scan","==usbDiskMounted==" + mountedPath);
        if(MusicScanManager.getInstance().getMusicChangeListener() != null) {
            MusicScanManager.getInstance().getMusicChangeListener().onUsbDiskMounted(mountedPath);
        }
        if(VideoScanManager.getInstance().getVideoChangeListener() != null) {
            VideoScanManager.getInstance().getVideoChangeListener().onUsbDiskMounted(mountedPath);
        }
        if(ImageScanManager.getInstance().getImageChangeListener() != null) {
            ImageScanManager.getInstance().getImageChangeListener().onUsbDiskMounted(mountedPath);
        }
    }

    private void usbDiskUnMounted(String root) {
        LogUtils.i("scan","==usbDiskUnMounted==" + root);
        if(MusicScanManager.getInstance().getMusicChangeListener() != null) {
            MusicScanManager.getInstance().getMusicChangeListener().onUsbDiskUnMounted(root);
        }
        if(VideoScanManager.getInstance().getVideoChangeListener() != null) {
            VideoScanManager.getInstance().getVideoChangeListener().onUsbDiskUnMounted(root);
        }
        if(ImageScanManager.getInstance().getImageChangeListener() != null) {
            ImageScanManager.getInstance().getImageChangeListener().onUsbDiskUnMounted(root);
        }
    }

    private void isFileScanFinished(String rootPath) {
        if(DEVICE.get(rootPath) != null) {
            DEVICE.get(rootPath).setScanFinished(true);
        }
        if(ImageScanManager.getInstance().getImageChangeListener() != null) {
            ImageScanManager.getInstance().getImageChangeListener().onFileScanFinished(rootPath);
        }
        if(MusicScanManager.getInstance().getMusicChangeListener() != null) {
            MusicScanManager.getInstance().getMusicChangeListener().onFileScanFinished(rootPath);
        }
        if(VideoScanManager.getInstance().getVideoChangeListener() != null) {
            VideoScanManager.getInstance().getVideoChangeListener().onFileScanFinished(rootPath);
        }
    }

    private void mediaParseFinished(Message msg) {
        String path = (String) msg.obj;
        LogUtils.i(TAG,"==mediaParseFinished==" + msg.arg1 + "|" + path);
        switch(msg.arg1) {
            case ConstantsUtils.FileType.MUSIC:
                if(MusicScanManager.getInstance().getMusicChangeListener() != null) {
                    MusicScanManager.getInstance().getMusicChangeListener().OnMediaParseBack(path);
                }
                break;
            case ConstantsUtils.FileType.VIDEO:
                if(VideoScanManager.getInstance().getVideoChangeListener() != null) {
                    VideoScanManager.getInstance().getVideoChangeListener().OnMediaParseBack(path);
                }
                break;
            case ConstantsUtils.FileType.IMAGE:
                if(ImageScanManager.getInstance().getImageChangeListener() != null) {
                    ImageScanManager.getInstance().getImageChangeListener().OnMediaParseBack(path);
                }
                break;
        }
    }

    private void noticeMediaDataChanged(final int mediaTpye) {
        ThreadPoolManager.getSinglePool().execute(new Runnable() {

            @Override
            public void run() {
                if(mediaTpye == ConstantsUtils.FileType.IMAGE) {
                    noticeUsbImageDataChanged(false);
                } else if(mediaTpye == ConstantsUtils.FileType.MUSIC) {
                    noticeUsbAudioDataChanged(false);
                } else if(mediaTpye == ConstantsUtils.FileType.VIDEO) {
                    noticeUsbVideoDataChanged(false);
                } else if(mediaTpye == ConstantsUtils.FileType.OTHER) {
                    noticeUsbOtherDataChanged(false);
                }
            }
        });
    }

    private void noticeUsbAudioDataChanged(boolean isScanFinished) {
        LogUtils.i("==audio file add noticeUsbAudioDataChaged ==" + isScanFinished);
        if(MusicScanManager.getInstance().getMusicChangeListener() == null) {
            return;
        }
        //通知上层使用者音频文件添加了
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if(MusicScanManager.getInstance().getMusicChangeListener() != null) {
                    MusicScanManager.getInstance().getMusicChangeListener().onMediaDataChanged();
                }
            }
        });
    }

    private void noticeUsbVideoDataChanged(boolean isScanFinished) {
        LogUtils.i("==video file add noticeUsbAudioDataChaged ==" + isScanFinished);
        if(VideoScanManager.getInstance().getVideoChangeListener() == null) {
            return;
        }
        //通知上层使用者音频文件添加了
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if(VideoScanManager.getInstance().getVideoChangeListener() != null) {
                    VideoScanManager.getInstance().getVideoChangeListener().onMediaDataChanged();
                }
            }
        });
    }

    private void noticeUsbOtherDataChanged(boolean isScanFinished) {
        LogUtils.i("==noticeUsbOtherDataChanged ==" + isScanFinished);
        if(VideoScanManager.getInstance().getVideoChangeListener() == null) {
            return;
        }
        //通知上层使用者音频文件添加了
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                //TODO
            }
        });
    }

    private void noticeUsbImageDataChanged(boolean isScanFinished) {
        LogUtils.i("==image file add noticeUsbAudioDataChanged ==" + isScanFinished);
        if(ImageScanManager.getInstance().getImageChangeListener() == null) {
            return;
        }
        //通知上层使用者音频文件添加了
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if(ImageScanManager.getInstance().getImageChangeListener() != null) {
                    ImageScanManager.getInstance().getImageChangeListener().onMediaDataChanged();
                }
            }
        });
    }

    /**
     * 接收UDisk设备挂载与御载通知
     */
    private UsbDiskReceiver mUsbEventReceiver;

    /**
     * 注册接收UDisk设备挂载与御载通知
     * @param context 上下文
     */
    private void registerBroadcastEvents(Context context) {
        mUsbEventReceiver = new UsbDiskReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        //媒体文件发生改变
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        filter.addDataScheme("file");
        context.registerReceiver(mUsbEventReceiver, filter);
    }

    /**
     * 反注册接收UDisk设备挂载与御载通知
     * @param context 上下文
     */
    private void unregisterBroadcastEvents(Context context) {
        if (mUsbEventReceiver != null) {
            context.unregisterReceiver(mUsbEventReceiver);
            mUsbEventReceiver = null;
        }
    }

    /**
     * 由于从扫描库发过来的状态稍慢，因此通过监听系统广播方式进行处理
     * 接收UDisk设备挂载与御载通知类
     */
    private class UsbDiskReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String dataString = intent.getDataString();
            // file:///storage/00B1-6906
            LogUtils.i("usbdisk","==action===>" + action);
            LogUtils.i("usbdisk","==dataString===>" + dataString);
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                //TODO 处理设备挂载
            } else if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
                mHandler.obtainMessage(ConstantsUtils.MEDIA_USB_DISMOUNTED,dataString).sendToTarget();
            }
        }
    }

}
