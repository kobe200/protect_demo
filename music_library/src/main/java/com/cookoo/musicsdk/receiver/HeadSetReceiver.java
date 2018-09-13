package com.cookoo.musicsdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import com.cookoo.musicsdk.utils.LogUtils;

/**
 * 
 * @ClassName: HeadSetReceiver
 * @Description: 耳塞线控处理
 * @author Deson
 * @date 2017年9月25日 下午3:17:39
 *
 */
public class HeadSetReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String intentAction = intent.getAction();
		
		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {

			LogUtils.print("收到线控事件");
			
			 //获得KeyEvent对象    
            KeyEvent keyEvent = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);   
			// 线控
//			CookooBTMusicManager.getInstance().notifyKeyEventChange(keyEvent);
			

		}
	}

}
