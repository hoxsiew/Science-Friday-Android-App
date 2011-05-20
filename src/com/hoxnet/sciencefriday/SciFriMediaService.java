/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hoxnet.sciencefriday;

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
//import android.os.Binder;
//import android.os.ConditionVariable;
//import android.os.Handler;
import android.os.IBinder;
//import android.os.Message;
//import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
//import android.widget.RemoteViews;
//import android.widget.Toast;
//import android.os.Process;

public class SciFriMediaService extends Service implements OnBufferingUpdateListener,OnCompletionListener,OnPreparedListener{

  final RemoteCallbackList<IMediaServiceCallback> mCallbacks=new RemoteCallbackList<IMediaServiceCallback>();
  private MediaPlayer mMediaPlayer=null;
  private int mBufferPosition=0;
  private boolean isPaused=false,phoneActive=false;
  private String mMediaPath=new String();
  private String mPodTitle=new String();
  private TelephonyManager mTelMan=null;
  private NotificationManager notifier = null;
  private Notification notify = null;
  

  @Override
  public void onCreate(){
    super.onCreate();

    notifier = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    //notifier = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    notify = new Notification();
    
    //Toast.makeText(getBaseContext(),"Service: OnCreate " + this.toString(),Toast.LENGTH_LONG).show();
    if(mMediaPlayer==null){
      mMediaPlayer=new MediaPlayer();
    //}else{
      //mMediaPlayer.get
      //Toast.makeText(getBaseContext(),"Mediaplayer not null",Toast.LENGTH_LONG).show();
    }
    //Toast.makeText(getBaseContext(),"service PID: "+android.os.Process.myPid(),Toast.LENGTH_SHORT).show();
    //android.widget.Toast.makeText(getApplicationContext(),"Service Created "+ this.toString(),android.widget.Toast.LENGTH_SHORT).show();
     mTelMan=(TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
     phoneActive=mTelMan.getCallState()!=TelephonyManager.CALL_STATE_IDLE;
     mTelMan.listen(mPhoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);
  }
        
  private PhoneStateListener mPhoneStateListener=new PhoneStateListener(){
    public void onCallStateChanged(int state,String incomingNumber){
      switch(state){
        case TelephonyManager.CALL_STATE_IDLE:
          if(phoneActive){
            phoneActive=false;
            if(isPaused){
              isPaused=false;
              mMediaPlayer.start();
            }
          }
          break;
        case TelephonyManager.CALL_STATE_RINGING:
        case TelephonyManager.CALL_STATE_OFFHOOK:
          if(mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
            isPaused=true;
            phoneActive=true;
          }
          break;
      }
      SendCallbackMessage(2002,state==TelephonyManager.CALL_STATE_IDLE?1:0);
    }
  };
  
//  @Override
//  public void onStart(Intent intent,int startId){
//    //notifier.cancel(1);
//    super.onStart(intent,startId);
//  }

  public void SendCallbackMessage(int msg,int value){
    final int N=mCallbacks.beginBroadcast();
    for(int i=0;i<N;i++){
      try{
        mCallbacks.getBroadcastItem(i).valueChanged(msg,value);
      }catch(RemoteException e){
        // The RemoteCallbackList will take care of removing
        // the dead object for us.
      }
    }
    mCallbacks.finishBroadcast();
  }

  private void SetMediaSource(String path,String title)
  {
    mMediaPath=path;
    mPodTitle=title;
    if(mMediaPlayer==null){
      mMediaPlayer=new MediaPlayer();
    }
    try{
      mMediaPlayer.setDataSource(mMediaPath);
      mMediaPlayer.prepare();
    }catch(IllegalArgumentException e){
      e.printStackTrace();
      SendCallbackMessage(2007,0);
      return;
    }catch(IllegalStateException e){
      e.printStackTrace();
      SendCallbackMessage(2007,1);
      return;
    }catch(IOException e){
      e.printStackTrace();
      SendCallbackMessage(2007,2);
      return;
    }
    mMediaPlayer.setOnBufferingUpdateListener(this);
    mMediaPlayer.setOnCompletionListener(this);
    mMediaPlayer.setOnPreparedListener(this);
  }
  
  @Override
  public void onDestroy(){
    notifier.cancel(1);
    if(mMediaPlayer!=null){
      mMediaPlayer.release();
      mMediaPlayer=null;
    }
    super.onDestroy();
  }

//  private Runnable mTask=new Runnable(){
//    public void run(){
//      try{
//        Thread.sleep(1000);
//      }catch(InterruptedException e){
//        e.printStackTrace();
//      }
//    }
//  };

  @Override
  public IBinder onBind(Intent intent){
    // Select the interface to return. If your service only implements
    // a single interface, you can just return it here without checking
    // the Intent.
    if(IMediaService.class.getName().equals(intent.getAction())){
      return mBinder;
    }
    if(IMediaSec.class.getName().equals(intent.getAction())){
      return mSecondaryBinder;
    }
    return null;
  }

  /**
   * The IMediaService is defined through IDL
   */
  private final IMediaService.Stub mBinder=new IMediaService.Stub(){
    public void registerCallback(IMediaServiceCallback cb){
      if(cb!=null)
        mCallbacks.register(cb);
    }

    public void unregisterCallback(IMediaServiceCallback cb){
      if(cb!=null)
        mCallbacks.unregister(cb);
    }
  };

  /**
   * A secondary interface to the service.
   */
  private final IMediaSec.Stub mSecondaryBinder=new IMediaSec.Stub(){
    @Override
    public int getIntegerValue() throws RemoteException{
      if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
        int dur=mMediaPlayer.getDuration();
        return dur;
      }
      return 0;
    }

    @Override
    public int getBufferedPosition() throws RemoteException{
      if(mMediaPlayer!=null){//&&mMediaPlayer.isPlaying()){
        return mBufferPosition;
      }
      return 0;
    }

    @Override
    public int getCurrentPosition() throws RemoteException{
      if(mMediaPlayer!=null){//&&mMediaPlayer.isPlaying()){
        return mMediaPlayer.getCurrentPosition();
      }
      return 0;
    }

    @Override
    public int getDuration() throws RemoteException{
      if(mMediaPlayer!=null){//&&mMediaPlayer.isPlaying()){
        return mMediaPlayer.getDuration();
      }
      return 0;
    }

    @Override
    public void SetMediaSourcePath(String s,String d) throws RemoteException{
      SetMediaSource(s,d);    
      
    }

    @Override
    public boolean getIsPlaying() throws RemoteException{
      if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
        return true;
      }else  if(isPaused){
        return true;
      }
      return false;
    }
    
    @Override
    public boolean getIsPaused() throws RemoteException{
      return isPaused;
    }

    @Override
    public boolean StartPlayer() throws RemoteException{
      if(mMediaPlayer!=null){//&&mMediaPlayer.isPlaying()){
        mMediaPlayer.start();
        isPaused=false;

        
      notify.icon = R.drawable.scifriicon;
      notify.tickerText = "Playing..";
      notify.when = System.currentTimeMillis();
      notify.flags|=android.app.Notification.FLAG_ONGOING_EVENT;
      
      Intent toLaunch = new Intent(SciFriMediaService.this, SciFriPlayer.class);
      toLaunch.putExtra("url",mMediaPath);      
      toLaunch.putExtra("media",1);      
      toLaunch.putExtra("title",mPodTitle);      
      toLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      toLaunch.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);      
      PendingIntent intentBack = 
        PendingIntent.getActivity(SciFriMediaService.this, 0, toLaunch, PendingIntent.FLAG_CANCEL_CURRENT);
      notify.setLatestEventInfo(SciFriMediaService.this, "SciFri", "Playing...", intentBack);      
      notifier.notify(1, notify);
        
        
        
        return true;
      }
      return false;
    }
    
    @Override
    public void PausePlayer(boolean pause) throws RemoteException{
      if(mMediaPlayer!=null){
        if(pause){
          isPaused=true;
          mMediaPlayer.pause();
        }else{
          isPaused=false;
          mMediaPlayer.start();
        }
      }
    }
    @Override
    public void ResetPlayer()throws RemoteException{
      if(mMediaPlayer!=null){
        mMediaPlayer.reset();
        //mMediaPlayer.release();
      }
      if(notifier!=null){
        notifier.cancel(1);
      }
    }
  
  
    @Override
    public String GetCurrentPath()throws RemoteException{
      return new String(mMediaPath);
    }
    
    public String GetCurrentDesc()throws RemoteException{
      return new String(mPodTitle);
    }
    
    @Override
    public void SeekTo(int pos_ms)throws RemoteException{
      if(mMediaPlayer!=null){
        int total=mMediaPlayer.getDuration();        
        //
        int max=(total*mBufferPosition)/100;
        if(pos_ms>max){
          //Toast.makeText(getBaseContext(),"Seeking to end. : ",Toast.LENGTH_SHORT).show();
          mMediaPlayer.seekTo(max-100);
        }else{
        //int cur=mMediaPlayer.getCurrentPosition();
        //if(pos_ms
          mMediaPlayer.seekTo(pos_ms);
        }
      }
    }    
  };
  
  

//  private static final int REPORT_MSG=1;
//
//  /**
//   * Our Handler used to execute operations on the main thread. This is used to
//   * schedule increments of our value.
//   */
//  private final Handler mHandler=new Handler(){
//    @Override
//    public void handleMessage(Message msg){
//      switch(msg.what){
//        case REPORT_MSG:
//          break;
//        default:
//          super.handleMessage(msg);
//      }
//    }
//  };

  @Override
  public void onBufferingUpdate(MediaPlayer mp,int percent){
    mBufferPosition=percent;
    SendCallbackMessage(2001,percent);
//    final int N=mCallbacks.beginBroadcast();
//    for(int i=0;i<N;i++){
//      try{
//        mCallbacks.getBroadcastItem(i).valueChanged(2001,percent);
//      }catch(RemoteException e){
//        // The RemoteCallbackList will take care of removing
//        // the dead object for us.
//      }
//    }
//    mCallbacks.finishBroadcast();
  }

  @Override
  public void onCompletion(MediaPlayer mp){
    SendCallbackMessage(2000,0);
//    final int N=mCallbacks.beginBroadcast();
//    for(int i=0;i<N;i++){
//      try{
//        mCallbacks.getBroadcastItem(i).valueChanged(2000,0);
//      }catch(RemoteException e){
//        // The RemoteCallbackList will take care of removing
//        // the dead object for us.
//      }
//    }
//    mCallbacks.finishBroadcast();
    notifier.cancel(1);
    //SciFriMediaService.this.stopSelf();

  }

  @Override
  public void onPrepared(MediaPlayer mp){
    
  }
}
