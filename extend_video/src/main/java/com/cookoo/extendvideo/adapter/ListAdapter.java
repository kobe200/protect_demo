package com.cookoo.extendvideo.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cookoo.extendvideo.R;
import com.cookoo.videosdkclient.load.CacheUtil;
import com.cookoo.videosdkclient.manager.VideoAidlManager;
import com.cookoo.videosdkclient.utils.LogUtils;
import com.cookoo.videosdkclient.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.bean.MediaData;
import carnetapp.usbmediadata.bean.MediaItemInfo;
import carnetapp.usbmediadata.utils.ConstantsUtils;

/**
 * @author lsf
 */
public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = ListAdapter.class.getSimpleName();
    private Context context;
    private RecyclerView.ViewHolder holder;
    private List<MediaData> listData;
    /**列表显示的风格，0表示listView类型，1表示gridView类型**/
    private int listShowStyle = 0;
    private OnClickEventListener onClickEventListener;

    public ListAdapter(Context context, List<MediaData> listData) {
        this.context = context;
        this.listData = new ArrayList<>();
        this.listData.addAll(listData);
    }

    public void setShowListStyle(int showListStyle){
        this.listShowStyle = showListStyle;
    }

    public int getShowListStyle(){
        return listShowStyle;
    }

    public synchronized void updateAdapter(List<MediaData> listData) {
        if(listData != null){
            this.listData.clear();
            this.listData.addAll(listData);
            this.notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LogUtils.print(TAG,"--->>>onCreateViewHolder() viewType: "+viewType+" listShowStyle: "+listShowStyle);
        if (viewType == 0){
            holder = new ListViewHolder(LayoutInflater.from(context).inflate(R.layout.playlistitem, parent, false));
        }else if (viewType == 1){
            holder = new GridViewHolder(LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        LogUtils.print(TAG,"--->>>onBindViewHolder() position: "+position+" listShowStyle: "+listShowStyle);
        if (listData == null || listData.size() <= 0){
            return;
        }
        final MediaData mi = listData.get(position);
        if (getItemViewType(position) == 0){
            ListViewHolder listViewHolder = (ListViewHolder)holder;
            listViewHolder.fileLayout.setVisibility(View.VISIBLE);
            listViewHolder.folderLayout.setVisibility(View.GONE);
            listViewHolder.itemView.findViewById(R.id.playlistitem_artist).setVisibility(View.GONE);
            listViewHolder.itemView.findViewById(R.id.playlistitem_album).setVisibility(View.GONE);
            listViewHolder.number.setText(position + "");
            listViewHolder.name.setText(mi.getName());
            if (mi.isFolder()){
                listViewHolder.number.setVisibility(View.GONE);
                listViewHolder.addCollect.setVisibility(View.GONE);
            }
            if (mi.getDataType() == ConstantsUtils.ListType.COLLECTION_TYPE){
                listViewHolder.addCollect.setBackground(context.getResources().getDrawable(R.mipmap.del_collect_list));
            }else {
                listViewHolder.addCollect.setBackground(context.getResources().getDrawable(R.mipmap.add_collect_list));
            }
            listViewHolder.fileLayout.setBackgroundColor(context.getResources().getColor(R.color.playListItemBg_normal));
            if (VideoAidlManager.getInstance().isCurrentPlayItem(mi)){
                listViewHolder.fileLayout.setBackgroundColor(context.getResources().getColor(R.color.playListItemBg_select));
            }
            listViewHolder.addCollect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickEventListener != null){
                        onClickEventListener.handleClickCollectEvent(mi);
                    }
                }
            });
        }else if (getItemViewType(position) == 1){
            GridViewHolder gridViewHolder = (GridViewHolder)holder;
            MediaItemInfo mediaItemInfo = VideoAidlManager.getInstance().getMediaItemInfo(mi.getFilePath());
            gridViewHolder.tv_title.setText(mi.getName());
            LogUtils.print(TAG, "--->> getView getDuration: "+mediaItemInfo.getDuration()+" getName: "+mi.getName());
            gridViewHolder.tv_time.setText(TimeUtil.formatDurationLong(mediaItemInfo.getDuration()));
            CacheUtil.getInstance().loadBitmapToView(mi.getFilePath(), gridViewHolder.imageView, new CacheUtil.ImageCallback() {
                @Override
                public void imageLoaded(String imagePath, int parseState) {
                }
            });
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onClickEventListener != null) {
                    if(mi.isFolder()) {
                        onClickEventListener .handleClickFolderEvent(mi);
                    } else {
                        onClickEventListener.handleClickFileEvent(position,mi);
                    }
                }
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

                @Override
                public int getSpanSize(int position) {
                    if (listData.size() <= 0){
                        return 3;
                    }
                    MediaData mi = listData.get(position);
                    if (mi.isFolder() || listShowStyle == 0){
                        return 3;
                    }else {
                        return 1;
                    }
                }
            });

        }



    }

    /**
     * 0表示listView类型显示，1表示gridView类型显示
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        super.getItemViewType(position);
        if (listData.size() <= 0){
            return 0;
        }
        final MediaData mi = listData.get(position);
        if (mi.isFolder() || listShowStyle == 0){
            return 0;
        }else{
            return 1;
        }
    }

    public int getFirstNoFolderItem(){
        for (int i = 0;i < listData.size() - 1;i++){
            if (!listData.get(i).isFolder()){
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    public List<MediaData> getData() {
        return listData;
    }

    class GridViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView tv_title;
        public TextView tv_time;

        public GridViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_title = itemView.findViewById(R.id.tv_title);
        }
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        public Button addCollect;
        public RelativeLayout fileLayout;
        public RelativeLayout folderLayout;
        public TextView folderName;
        public TextView name ;
        public TextView number ;

        public ListViewHolder(View itemView) {
            super(itemView);
            addCollect = itemView.findViewById(R.id.btn_add_collect);
            fileLayout = itemView.findViewById(R.id.playlistitem_file_layout);
            folderLayout = itemView.findViewById(R.id.playlistitem_folder_layout);
            folderName = itemView.findViewById(R.id.playlistitem_folder_layout_name);
            name = itemView.findViewById(R.id.playlistitem_name);
            number = itemView.findViewById(R.id.playlistitem_no);
        }
    }

    public void setOnClickEventListener(OnClickEventListener onClickEventListener){
        this.onClickEventListener = onClickEventListener;
    }

    public interface OnClickEventListener{
        /**
         * 处理点击文件事件
         * @param position
         * @param md
         */
        void handleClickFileEvent(int position, MediaData md);
        /**
         * 处理点击收藏事件
         * @param md
         */
        void handleClickCollectEvent(MediaData md);
        /**
         * 处理点击文件夹事件
         * @param md
         */
        void handleClickFolderEvent(MediaData md);
    }

}
