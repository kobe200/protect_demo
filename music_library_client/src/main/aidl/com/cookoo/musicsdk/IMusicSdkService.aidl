// IMusicSdkService.aidl
package com.cookoo.musicsdk;
import com.cookoo.musicsdk.IMusicServiceCallback;
import carnetapp.usbmediadata.bean.UsbDevice;
import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaItemInfo;
import carnetapp.usbmediadata.bean.MediaListData;
// Declare any non-default types here with import statements
//提供给远程操作方法
interface IMusicSdkService {

     /**
         * 获取总时长
         * @return
         */
        int getTotalTime();

        /**
         * 获取当前时长
         * @return
         */
        int getCurrentTime();

        /**
         * 上一曲
         */
        void preMusic();

        /**
         * 下一曲
         * @param isUser 是否为用户切换下一首
         */
        void nextMusic(boolean isUser);

        /**
         * 播放或暂停
         */
        void playOrPause();

        /**
         * 开始播放
         */
        void startMusic();

        /**
         * 播放音乐
         * @param MediaData
         */
        void playMusic(in MediaData MediaData);

        /**
         * 暂停
         */
        void pauseMusic();

        /**
         * 暂停
         */
        void stopMusic();

        /**
         * 快退
         */
        void backForward();

        /**
         * 快进
         */
        void fastForward();

        /**
         * 移动到指定位置进行播放
         * @param position ：指定播放位置
         */
        void seekTo(int position);

        /**
         * 清除数据
         * @param usbPath
         */
        void clearUsbMusic(in String usbPath);

        /**
         * 保存播放模式
         * @param position
         */
        void savePlayMode(int position);

        /**
         * 获取当前播放模式
         * @return 返回播放模式
         */
        int getCurrentPlayMode();

        /**
         * 设置当前播放模式：全部循环、单曲播放、随机播放
         * @param currentPlayMode @com.cookoo.musicsdk.MusicSdkConstants.PlayMode
         */
        void setCurrentPlayMode(int currentPlayMode);

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
         * @param usbPath
         * @return 返回当前usb是否扫描完
         */
        boolean isCurrentDeviceScanFinished(in String usbPath);

        /**
         * 设置优先加载音乐文件
         * @param isRefrain 是否抑制加载
         */
        void setCPURefrain(boolean isRefrain);

       /**
        * 获取所有的音乐数据,如果传的usb路径usbPath为null，
        * 则获取所有usb的数据，否则获取usbPath的所有数据
        * @param columnContentStr 请求专辑或作者数据的需要的参数
        * @param dataType 请求数据类型
        * @param usbPath
        */
       void requestMusicData(in String usbPath,in String columnContentStr,int dataType);

       /**
         * 当数据改变的时候，需要调用这个方法进行更新获取相应数据
         * @param usbPath 表示获取对应usb的相应数据
         */
       void handleMusicDataChange(in String usbPath);

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
         * 获取当前播放对象
         * @return 返回当前播放对象
         */
        MediaData getCurrentPlayMediaItem();

        /**
         * 设置当前播放对象
         * @param mediaData
         */
        void setCurrentPlayMediaItem(in MediaData mediaData);

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
         * 更新音乐的名称
         * @param isCollection 是否更新收藏列表
         * @param itemInfo
         * @return
         */
        boolean updateMediaItemName(in MediaData itemInfo,boolean isCollection);

        /**
         * 获取音乐当前播放状态
         * @return
         */
        int getCurrentPlayState();

        /**
         * 设置当前播放状态
         * @param currentPlayState
         */
        void setCurrentPlayState(int currentPlayState);

        /**
         * 获取当前数据列表类型
         * @return
         */
        int getCurrentDataListType();

        /**
          * 获取记忆保存的播放对象路径
          * @return 返回上次保存的记忆播放对象的路径
          */
         String getSavePlayMediaItemPath();
        /**
          * 获取记忆保存的播放对象的数据类型
          * @return 返回上次保存的记忆播放对象的数据类型
          */
         int getSavePlayMediaItemDataType();
        /**
          * 获取记忆保存的播放进度条
          * @return 返回上次保存的记忆播放进度条
          */
         int getSavePlayProgress();
         /**
           * 获取记忆保存的播放模式
           * @return 返回上次保存的记忆播放模式
           */
         int getSavePlayMode();

        /**
         * 获取音乐当前播放列表位置
         * @return
         */
        int getCurrentListPosition();

        /**
         * 设置当前播放歌曲位于列表中的位置
         * @param listPosition
         */
        void setCurrentListPosition(int listPosition);

        /**
         * 获取音乐当前播放时长
         * @return
         */
        int getCurrentPlayPosition();

        /**
         * 设置当前播放位置
         * @param currentPlayPosition
         */
        void setCurrentPlayPosition(int currentPlayPosition);

        /**
         * 获取当前列表高亮位置，如果当前播放的数据不再当前列表中，则默认是0
         * @return
         */
        int getHeightLightPosition();

        /**
         * 获取缓存的原始数据
         * @return
         **/
        List<MediaListData> getOriginalData();

        /**
         * 获取所有数据更新后的新数据
         * @return
         */
        List<MediaData> getNewData();

        /**
         * 获取当前播放列表
         * @return
         */
        List<MediaData> getPlayList();

        /**
         * 设置当前播放列表数据
         * @param playList
         */
        void setPlayList(in List<MediaData> playList);

        /**
         * 获取所有USB设备列表
         * @return
         */
        List<UsbDevice> getUsbDeviceList();

        /**
         * 返回上一级目录
         * @param usbPath 当传的路径为null时，代表获取的数据不区分usb
         */
        void upperLevel(in String usbPath);

        /**
          * 获取MediaItemInfo对象
          * @param filePath
          * @return
          */
         MediaItemInfo getMediaItemInfo(String filePath);

        /**
          * 获取当前浏览列表文件夹路径
          * @return
          */
         String getCurrentPlayPath();

         /**
           * 获取当前播放列表文件夹路径
           * @param currentPlayPath
           */
          void setCurrentPlayPath(String currentPlayPath);

        /**
          * 获取当前查询字段信息，例如：专辑名称、艺术家名称、两级文件夹名称
          * @return
          */
        String getColumnContentStr();
           /**
             * 处理记忆播放逻辑
             * @return
             */
            void handleMemoryPlayback();
        /**
           * 动态设置淡入淡出效果
           * @param fadeInNndOut true为淡入淡出，false为非淡入淡出
           */
          void setFadeInNndOut(boolean fadeInNndOut);
          /**
           * 是否淡入淡出效果
           */
          boolean isFadeInNndOut();

        /***注册callback**/
        boolean registerMusicServiceCallback(IMusicServiceCallback cb);

       /**反注册callback**/
       boolean unregisterMusicServiceCallback(IMusicServiceCallback cb);

}
