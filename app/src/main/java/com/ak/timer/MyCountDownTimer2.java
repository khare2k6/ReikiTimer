package com.ak.timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MyCountDownTimer2 {

	private long mTime,mElapsetTime,mInterval,mStartTimerTimeStamp;
	private ICountDownListener mListener;
	private SomeHandler mHandler;
	private Context mContext;
	public final static int TICK = 1, TIMER_EXPIRED = 2;

	private AlarmManager mAlarmManager;
	private static MyCountDownTimer2 mInstance = null;
	final static String TAG = MyCountDownTimer2.class.getSimpleName();
	private PendingIntent mServiceIntent ;
	
	private  MyCountDownTimer2(){
		mHandler = new SomeHandler();
	}


	  public void onFinish() {
		  /*null check added for the NPE. If App gets killed due to any reason
		  * by android framework and alarm is already set with alarmService, the
		  * app gets restarted without any mListener being set */
		  if(mListener !=null){
			  mListener.onTimerFinish();
		  }
		  if(mHandler != null){
			mHandler.removeMessages(TICK);
		}
	}

	public void onTick(long millisUntilFinished) {
		mListener.onTimeTick(millisUntilFinished);
	}
	
	/**
	 * Required by service which will be triggered when alarmManager
	 * timer will expire
	 * @return
	 */
	public static MyCountDownTimer2 getInstance(){
		if(mInstance == null){
			mInstance = new MyCountDownTimer2();
		}
		return mInstance;
	}
	
	
	public void setTimerParams(Context context,long millisInFuture, long countDownInterval,ICountDownListener callback){
		mTime = millisInFuture;
		mInterval = countDownInterval;
		mListener = callback;
		mContext = context;
		mContext = context;
		Intent intent = new Intent(mContext, TimerExpiredReceiver.class);
		intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		mServiceIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);//PendingIntent.getService(mContext, 0,new Intent(mContext, TimerExpiredService.class), 0);
		mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
	}
	public void start() {
		mStartTimerTimeStamp = System.currentTimeMillis();
		
		Message msg = mHandler.obtainMessage(TICK);
		mHandler.sendMessage(msg);
		// start alarm service here
		Log.d(TAG,"starting alarm service for "+mTime+" API level:"+Build.VERSION.SDK_INT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, mStartTimerTimeStamp
                    + mTime, mServiceIntent);
		}else{
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, mStartTimerTimeStamp
					+ mTime, mServiceIntent);

		}

	}
	
	public void cancel(){
		mStartTimerTimeStamp = 0;
		mHandler.removeMessages(TICK);
		//stop the alarmService Timer here
		mAlarmManager.cancel(mServiceIntent);
	}
	
	public Handler getHandler(){
		return mHandler;
	}
	private class SomeHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case TICK:
				mElapsetTime = System.currentTimeMillis() - mStartTimerTimeStamp;
				onTick(mTime - mElapsetTime);
				Message message = mHandler.obtainMessage(TICK);
				mHandler.sendMessageDelayed(message, mInterval);
				break;
			case TIMER_EXPIRED:
				onFinish();
				break;
			}
		}
		
	} 
}
