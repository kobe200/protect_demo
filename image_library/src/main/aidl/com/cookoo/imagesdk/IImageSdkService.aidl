// IImageSdkService.aidl
package com.cookoo.imagesdk;
import com.cookoo.imagesdk.IImageServiceCallback;
import carnetapp.usbmediadata.bean.UsbDevice;
import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaListData;
// Declare any non-default types here with import statements
//提供给远程操作方法
interface IImageSdkService {

          /**
             * 开始幻灯片播放
             */
            void startSlide();

            /**
             * 结束幻灯片播放
             */
            void endSlidePlay();

            /**
             * 清理图片数据
             * @param usbPath 清除对应usb下的数据
             */
            void clearUsbImage(in String usbPath);

            /**
             * 缩小图片
             */
            void zoomOut();

            /**
             * 放大图片
             */
            void zoomIn();

            /**
             * 旋转图片，
             *
             * @param angle 旋转角度
             */
            void rotate(int angle);

            /**
             * 上一个图片
             */
            void preImage();

            /**
             * 下一个图片
             */
            void nextImage();

            /**
             * 判断是否为当前播放的对象
             * @param itemInfo
             * @return 判断是否为正在播放的对象
             */
            boolean isCurrentPlayItem(in MediaData itemInfo);

            /**
             * 判断当前usb是否挂载
             * @param usbPath
             * @return 返回当前usb是否挂载
             */
            boolean isCurrentUsbMount(in String usbPath);

            /**
             * 判断当前机器是否没有usb设备
             * @return 返回所有usb是否都卸载
             */
            boolean isAllUsbUnMount();
           /**
            * 判断所有usb是否扫描完毕
            * @param isFilterSdcard 是否过来sdk扫描
            * @return 返回所有usb是否扫描完成
            */
           boolean isAllDeviceScanFinished(boolean isFilterSdcard);

           /**
            * 判断某个usb是否扫描完毕
            * @param usbPath 当前usb路径
            * @return 放回当前usb是否扫描完成
            */
           boolean isCurrentDeviceScanFinished(String usbPath);

            /**
             * 设置优先加载音乐文件
             * @param isRefrain 是否抑制加载
             */
            void setCPURefrain(boolean isRefrain);

            /**
              * 获取所有的音乐数据
              * @param path 如果为null，表示获取所有usb数据，如果为usb根目录，则获取对应usb的数据，否则获取对应文件夹的数据
              * @param dataType 请求数据类型
              */
             void requestImageData(in String path, int dataType);

            /**
             * 当数据改变的时候，需要调用这个方法进行更新获取相应数据
             * @param usbPath 表示获取对应usb的相应数据
             */
            void handleImageDataChange(in String usbPath);

          /**
            * 获取记忆保存的对象
            * @return 返回上次保存的记忆对象
            */
            MediaData getSavePlayMediaItem();

            /**
             * 获取当前播放对象
             * @return 返回当前播放对象
             */
            MediaData getCurrentPlayMediaItem() ;

            /**
             * 设置当前播放对象
             * @param currentPlayMediaItem
             */
            void setCurrentPlayMediaItem(in MediaData currentPlayMediaItem);

            /**
             * 更新音乐的名称
             * @param itemInfo
             * @param isCollection 是否更新收藏列表
             * @return 返回是否更新成功
             */
            boolean updateMediaItemName(in MediaData itemInfo, boolean isCollection);

               /**
                * 添加收藏
                *  mediaData的isCollected值 表示收藏，false表示移除收藏
                * @param mediaData
                * @return 返回是否删除成功
                */
               boolean collected(in MediaData mediaData);
           
               /**
                * 取消收藏
                *  mediaData的isCollected值 表示收藏，false表示移除收藏
                * @param mediaData
                * @return 返回是否删除成功
                */
               boolean unCollected(in MediaData mediaData);

            /**
             * 获取音乐当前播放列表位置
             * @return
             */
            int getCurrentListPosition();

            /**
             * 设置列表位置
             * @param listPosition
             */
            void setCurrentListPosition(int listPosition);


            /**
             * 获取当前列表高亮位置，如果当前播放的数据不再当前列表中，则默认是0
             * @return
             */
            int getHeightLightPosition();

            /**
             *设置播放列表
             * @param playList
             */
            void setPlayList(in List<MediaData> playList);

            /**
             *获取播放列表
             * @return 返回播放列表数据
             */
            List<MediaData> getPlayList();

            /**
             * 获取最新获取到的数据
             * @return
             */
            List<MediaData> getNewData();

            /**
             * 获取缓存的原始数据
             * @return
             **/
            List<MediaListData> getOriginalData();

            /**
              * 获取usb设备列表
              * @return
              */
            List<UsbDevice> getUsbDeviceList();

           /**
             * 获取当前数据列表类型
             * @return
             */
            int getCurrentDataListType();

           /**
             * 获取当前显示的列表路径
             * @return  返回当前显示文件的路径
             */
            String getCurrentShowDataPath();

           /**
             * 获取当前显示的列表的上一级目录
             * @param currentPath 当前显示文件的路径
             * @return  返回当前显示文件的上一级目录
             */
           String getUpperLevelFolderPath(in String currentPath);

          /**
            * 返回上一级目录
            * @param usbPath 当传的路径为null时，代表获取的数据不区分usb
            */
           void upperLevel(in String usbPath);

          /***注册callback**/
          boolean registerImageServiceCallback(IImageServiceCallback cb);

          /**反注册callback**/
          boolean unregisterImageServiceCallback(IImageServiceCallback cb);
}
