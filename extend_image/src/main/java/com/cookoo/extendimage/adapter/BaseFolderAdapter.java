package com.cookoo.extendimage.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.cookoo.extendimage.LogUtils;
import com.cookoo.extendimage.R;

import java.util.ArrayList;

import carnetapp.usbmediadata.bean.MediaItemInfo;

/**
 *
 * @author lsf
 * @date 2018/4/17
 */

public abstract class BaseFolderAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private static final String TAG = "FolderAdapter";
    protected Context mContext;
    protected LayoutInflater mInflater;
    protected ListView mListView;
    protected ArrayList<String> folderPathList;
    protected ArrayList<MediaItemInfo> fileItemList;
    protected int folderType;
    protected static final String FILE_KEY = "media";
    protected static final String FOLDER_KEY = "folder";

    public BaseFolderAdapter(Context context, ListView listView,int folderType) {
        this.mContext = context;
        this.mListView = listView;
        this.folderType = folderType;
        this.mInflater = LayoutInflater.from(mContext);
        mListView.setOnItemClickListener(this);
        folderPathList = new ArrayList<>();
        fileItemList = new ArrayList<>();
        initData();
    }

    public void updateAdapter(){
        initData();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return folderPathList.size() + fileItemList.size();
    }

    /**
     * 获取文件名
     * @param position
     * @return
     */
    @Override
    public String getItem(int position) {
        if (position < folderPathList.size()) {
            String[] strs = folderPathList.get(position).split("/");
            return strs[strs.length - 1];
        } else {
            return fileItemList.get(position - folderPathList.size()).getName();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder ;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.folder_item, null);
            viewHolder.textView = convertView.findViewById(R.id.text);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(getItem(position));
        return convertView;
    }

    static class ViewHolder{
        TextView textView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        LogUtils.print(TAG, "---->>>onItemClick  position: "+position);
        if (position < folderPathList.size()) {
            onClickFolder(position);
        }else{
            onClickFile(position);
        }
    }

    /**
     * 初始化数据
     */
    protected abstract void initData();

    /**
     * 点击文件夹
     * @param position
     */
    protected abstract void onClickFolder(int position);

    /**
     * 点击文件
     * @param position
     */
    protected abstract  void onClickFile(int position);

}
