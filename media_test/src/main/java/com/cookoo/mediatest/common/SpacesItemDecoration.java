package com.cookoo.mediatest.common;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;


/**
 * @author lsf
 */
public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        ListAdapter adapter = (ListAdapter) parent.getAdapter();
        int position = parent.getChildAdapterPosition(view);
        if (adapter.getItemCount() <= 0){
            return;
        }
        int listShowStyle = adapter.getShowListStyle();
        int firstNoFolderItem = adapter.getFirstNoFolderItem();

        if (listShowStyle == 0){
            setItemOffsets(position,1,outRect);
        }else{
            if (position < firstNoFolderItem || firstNoFolderItem == -1){
                setItemOffsets(position,1,outRect);
            }else{
                setItemOffsets(position - firstNoFolderItem,2,outRect);
            }
        }
    }

    private void setItemOffsets(int position,int spanCount, Rect outRect){
        int column = position % spanCount;
        outRect.left = column * 5 / spanCount;
        outRect.right = 5 - (column + 1) * 5 / spanCount;
        if (position >= spanCount) {
            outRect.top = 5;
        }
    }


}