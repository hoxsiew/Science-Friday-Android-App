package com.hoxnet.sciencefriday;

//import com.example.android.apis.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
//import android.widget.Toast;

public class SciFriPlayer extends Activity{
  //implements SurfaceHolder.Callback {
  private static final String TAG = "SciFriMediaPlayer";
  private static final int TMRMSG=1002;
  //private static final int DIMMSG=1003;
  private static final int MSGINITFINISH=1004;
  //private MediaPlayer mMediaPlayer=null;
  //private SurfaceView mPreview;
  //private SurfaceHolder holder;
  private Bundle extras;
  private static final String MEDIA = "media";
  private static final int LOCAL_AUDIO = 1;
  private ImageButton playButton;
  private ImageView imView01;
  private SeekBar prog=null;
  private boolean isPlaying=false,isPaused=false;
  private boolean isAudio=false;
  private String PodCastUrl=new String();
  private String PodCastTitle=new String();
  private int PodCastLength=0;
  private int PodCastLenkb=0;
  private int mTick=0;
  //private PowerManager.WakeLock mWakeLock;
  private LinearLayout DimableControls;
  private boolean mTracking=false;
  //private KeyguardLock lock;
  private Thread myRefreshThread = null;
  private TextView tv,cur,all; 
  private FrameLayout mFrameLayout;
  //private ActivityManager rsi=(ActivityManager)getSystemService(ACTIVITY_SERVICE);
  /**
   * 
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    setContentView(R.layout.player_overlay);
    extras = getIntent().getExtras();
    PodCastUrl = extras.getString("url");
    PodCastTitle = extras.getString("title");
    PodCastLength = extras.getInt("length",0);
    PodCastLenkb = extras.getInt("lenkb",0);
    isAudio=extras.getInt("media",0)==1;

    //bind to our media service (creating it if necessary)
    getApplicationContext().bindService(new Intent(IMediaService.class.getName()),mConnection,Context.BIND_AUTO_CREATE);
    getApplicationContext().bindService(new Intent(IMediaSec.class.getName()),mSecondaryConnection,Context.BIND_AUTO_CREATE);
    
    //PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    //mWakeLock = pm.newWakeLock(isAudio?PowerManager.SCREEN_DIM_WAKE_LOCK:PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
    
    tv=(TextView)findViewById(R.id.text_kb_streamed);
    tv.setText(PodCastTitle);
    initControls();
    //myViewUpdateHandler.sendMessageDelayed(myViewUpdateHandler.obtainMessage(MSGINITFINISH),1000);
    myRefreshThread = new Thread(new secondCountDownRunner());
    myRefreshThread.start();
    
  }

//  @Override
//  protected void onDestroy(){
//    super.onDestroy();
//  }
//  
//  @Override
//  protected void onPause(){
//    super.onPause();
//    try{
//      mSecondaryService.ResetPlayer();
//    }catch(RemoteException e){
//      e.printStackTrace();
//    }
//    finish();
//  }

  
//  @Override
//  protected void onDestroy(){
//    super.onDestroy();
//  }
//
//  @Override
//  protected void onRestart(){
//   //bindService(new Intent(IMediaService.class.getName()),mConnection,Context.BIND_AUTO_CREATE);
//   //bindService(new Intent(IMediaSec.class.getName()),mSecondaryConnection,Context.BIND_AUTO_CREATE);
//   super.onRestart();
//  }
//
//  @Override
//  protected void onStop(){
//    //unbindService(mConnection);
//    //unbindService(mSecondaryConnection);
//    super.onStop();
//  }

  Handler myViewUpdateHandler = new Handler() {
    /** Gets called on every message that is received */
    
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MSGINITFINISH:
          //OnCreate is done with its setup
          try{
            if(!mSecondaryService.getIsPlaying()){
              mSecondaryService.SetMediaSourcePath(PodCastUrl,PodCastTitle);
              mSecondaryService.StartPlayer();
            }else{
              //myViewUpdateHandler.obtainMessage(766)
//              String s=mSecondaryService.GetCurrentPath();
//              String x=PodCastUrl;
//              boolean bb=x!=s;
//              boolean cc=x==s;
//              boolean dd=x.compareTo(s)==0;
              if(mSecondaryService.GetCurrentPath().compareTo(PodCastUrl)!=0){
                mSecondaryService.ResetPlayer();
                mSecondaryService.SetMediaSourcePath(PodCastUrl,PodCastTitle);
                mSecondaryService.StartPlayer();
              }
            }
            isPlaying=true;            
            isPaused=mSecondaryService.getIsPaused();
            PodCastTitle=mSecondaryService.GetCurrentDesc();
            playButton.setImageResource(isPaused?R.drawable.button_play:R.drawable.button_pause);
            //initControls();
          }catch(Exception e){
            Log.d(TAG, "error: " + e.getMessage(), e);
            finish();
          }
          break;
        case TMRMSG:    
          //timer tick (every 500 ms)
          if(isPlaying){
            try{
              int a=mSecondaryService.getDuration();
              if(a==0){
                break;
              }            
              all.setText(TimeFromMS(a));
              int b=mSecondaryService.getCurrentPosition();
              cur.setText(TimeFromMS(b));
              int pct=(b*100)/a;
              prog.setProgress(pct);
            }catch(Exception e){
            }
          }          
          if(extras.getInt(MEDIA)!=LOCAL_AUDIO){
            mTick++;
            if(mTick>=8){ //4 seconds
              mTick=0;
              DimableControls.setVisibility(View.INVISIBLE);
            }
          }
          break;
        case 2000:
          //player finished
          try{
            mSecondaryService.ResetPlayer();
          }catch(RemoteException e){
            e.printStackTrace();
          }
          try{
            getApplicationContext().unbindService(mConnection);
            getApplicationContext().unbindService(mSecondaryConnection);
          }catch(Exception e){
            e.printStackTrace();
          }
          finish();
//          isPlaying=false;
//          prog.setProgress(0);
//          cur.setText(TimeFromMS(0));
//          isPaused=true;
//          playButton.setImageResource(R.drawable.button_play);
//          try{
//            mSecondaryService.ResetPlayer();
//          }catch(RemoteException e){
//            e.printStackTrace();
//          }
          break;
        case 2001:
          //Buffering progress update
          prog.setSecondaryProgress(msg.arg1);
          break;
        case 2002:
          //pause or resume based on phone state
          if(msg.arg1==0){
            isPaused=false;
            playButton.setImageResource(R.drawable.button_pause);
          }else{
            isPaused=true;
            playButton.setImageResource(R.drawable.button_play);
          }
          break;
        case 2007:  //error: exception thrown
          //TODO: display error message.
          //int err=msg.arg1;
          break;
      }
      super.handleMessage(msg);
    }
  };
  
  class secondCountDownRunner implements Runnable {
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
          Message m = new Message();
          m.what = TMRMSG;
          myViewUpdateHandler.sendMessage(m);
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          Log.d(TAG, "error: " + e.getMessage(), e);
          Thread.currentThread().interrupt();
        }
      }
    }
  } 
  
  private void initControls() {
    //imView01=(ImageView) findViewById(R.id.ImageView01);
    //imView01.setImageResource(R.drawable.sflogo_player);
    //LinearLayout ll3=(LinearLayout) findViewById(R.id.LinearLayout03);
    //ll3.setVisibility(View.INVISIBLE);
    //Button05  //power button
    ImageButton pow=(ImageButton)findViewById(R.id.Button05);
    if(pow!=null){
      pow.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v){
          try{
            mSecondaryService.ResetPlayer();
          }catch(RemoteException e){
            e.printStackTrace();
          }
          try{
            getApplicationContext().unbindService(mConnection);
            getApplicationContext().unbindService(mSecondaryConnection);
          }catch(Exception e){
            e.printStackTrace();
          }
          finish();
        }
      });
    }
    //mPreview = (SurfaceView) findViewById(R.id.SurfaceView01);
    mFrameLayout=(FrameLayout) findViewById(R.id.FrameLayout01);
    
    //mPreview = new SurfaceView(getBaseContext());//
    //holder = mPreview.getHolder();
    //holder.addCallback(this);
    
    mFrameLayout.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        mTick=0;
        DimableControls.setVisibility(View.VISIBLE);        
     }
    });
    //holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    cur=(TextView)findViewById(R.id.TextView01);
    all=(TextView)findViewById(R.id.TextView02);
    prog=(SeekBar)findViewById(R.id.SeekBar01);//progress_bar);
    prog.setMax(100);
    prog.setBackgroundColor(0);
    prog.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      boolean test=false;
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        if(mTracking){
//          return;
//        }
        if(fromUser){          
          try{
            if(mSecondaryService.getIsPlaying()){
              int a=mSecondaryService.getDuration();
              if(a==0){
                return;
              }
              //int b=mSecondaryService.getCurrentPosition();
              //int pct=(b*100)/a;
              //prog.setProgress(pct);       
              int where=(int)((float)a*((float)progress/100.0f));
              mSecondaryService.SeekTo(where);
            }
          }catch(Exception e){
            Log.d(TAG, "error: " + e.getMessage(), e);
          }
        }
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        //mTracking=true;
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        //mTracking=false;
      }
    });
    
    DimableControls=(LinearLayout)findViewById(R.id.dimable);
    playButton = (ImageButton) findViewById(R.id.button_play);
    playButton.setEnabled(true); 
    playButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        try{
          if(isPlaying){
            if(isPaused){
              //resume play
              mSecondaryService.PausePlayer(false);
              isPaused=false;
              playButton.setImageResource(R.drawable.button_pause);
            }else{
              //pause play
              mSecondaryService.PausePlayer(true);
              isPaused=true;
              playButton.setImageResource(R.drawable.button_play);
            }
          }else{
            myViewUpdateHandler.sendMessage(myViewUpdateHandler.obtainMessage(MSGINITFINISH));
          }
        }catch(Exception e){
          Log.d(TAG,e.getMessage());
        }
      }
    });
  }

//
//  public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
//    Log.d(TAG, "surfaceChanged called");
//
//  }
//
//  public void surfaceDestroyed(SurfaceHolder surfaceholder) {
//    Log.d(TAG, "surfaceDestroyed called");
//  }
//
//  public void surfaceCreated(SurfaceHolder holder) {
//  }
  
  private String TimeFromMS(int ms){
    String s=new String();
    int hr,mn,sc;

    int t=ms/1000;
    if(t>3600){
      hr=t/3600;
      mn=t%3600;
      sc=mn%60;
      s=String.format("%d:%02d:%02d",hr,mn/60,sc);
    }else if (t>60){
      mn=t/60;
      sc=t%60;
      s=String.format("%d:%02d",mn,sc);
    }else{              
      s=String.format("0:%02d",t);
    }
    return s;
  }
  
   //Interface stuff below.
  
  /** The primary interface we will be calling on the service. */
  IMediaService mService=null;
  /** Another interface we use on the service. */
  IMediaSec mSecondaryService=null;


  private ServiceConnection mConnection=new ServiceConnection(){
    public void onServiceConnected(ComponentName className,IBinder service){
      // This is called when the connection with the service has been
      // established, giving us the service object we can use to
      // interact with the service. We are communicating with our
      // service through an IDL interface, so get a client-side
      // representation of that from the raw service object.
      mService=IMediaService.Stub.asInterface(service);
      try{
        mService.registerCallback(mCallback);
      }catch(RemoteException e){
        // In this case the service has crashed before we could even
        // do anything with it; we can count on soon being
        // disconnected (and then reconnected if it can be restarted)
        // so there is no need to do anything here.
      }
    }

    public void onServiceDisconnected(ComponentName className){
      // This is called when the connection with the service has been
      // unexpectedly disconnected -- that is, its process crashed.
      mService=null;
    }
  };

  /**
   * Class for interacting with the secondary interface of the service.
   */
  private ServiceConnection mSecondaryConnection=new ServiceConnection(){
    public void onServiceConnected(ComponentName className,IBinder service){
      // Connecting to a secondary interface is the same as any
      // other interface.
      mSecondaryService=IMediaSec.Stub.asInterface(service);
      
      myViewUpdateHandler.sendMessage(myViewUpdateHandler.obtainMessage(MSGINITFINISH));
      
      //notify();
    }

    public void onServiceDisconnected(ComponentName className){
      mSecondaryService=null;
    }
  };

  private IMediaServiceCallback mCallback=new IMediaServiceCallback.Stub(){
    public void valueChanged(int type,int value){
      myViewUpdateHandler.sendMessage(myViewUpdateHandler.obtainMessage(type,value,0));
    }
  };

  
}
