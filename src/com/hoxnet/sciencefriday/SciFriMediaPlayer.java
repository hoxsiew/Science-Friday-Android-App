package com.hoxnet.sciencefriday;

//import com.example.android.apis.R;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
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
import android.widget.Toast;

public class SciFriMediaPlayer extends Activity implements
    OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener,
    OnVideoSizeChangedListener, SurfaceHolder.Callback {
  IMediaService mMediaService = null;
  private static final String TAG = "SciFriMediaPlayer";
  private static final int TMRMSG=1002;
  private static final int DIMMSG=1003;
  private int mVideoWidth;
  private int mVideoHeight;
  private MediaPlayer mMediaPlayer=null;
  private SurfaceView mPreview;
  private SurfaceHolder holder;
  private Bundle extras;
  private static final String MEDIA = "media";
  private static final int LOCAL_AUDIO = 1;
  private static final int STREAM_AUDIO = 2;
  private static final int RESOURCES_AUDIO = 3;
  private static final int LOCAL_VIDEO = 4;
  private static final int STREAM_VIDEO = 5;
  private boolean mIsVideoSizeKnown = false;
  private boolean mIsVideoReadyToBePlayed = false;
  private ImageButton playButton;
  private TextView textStreamed;
  private SeekBar prog=null;
  private boolean isPlaying;
  private boolean isAudio=false;
  private String PodCastUrl=new String();
  private String PodCastTitle=new String();
  private int PodCastLength=0;
  private int PodCastLenkb=0;
  private int mTick=0;
  private PowerManager.WakeLock mWakeLock;
  private LinearLayout DimableControls;
  //private KeyguardLock lock;
  private Thread myRefreshThread = null;
  private TextView tv,cur,all; 
  private FrameLayout mFrameLayout;
  /**
   * 
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    setContentView(R.layout.player_1);
    mPreview = (SurfaceView) findViewById(R.id.SurfaceView01);
    mFrameLayout=(FrameLayout) findViewById(R.id.FrameLayout01);
    
    //mPreview = new SurfaceView(getBaseContext());//
    holder = mPreview.getHolder();
    holder.addCallback(this);
    
    mFrameLayout.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        mTick=0;
        DimableControls.setVisibility(View.VISIBLE);        
     }
    });
    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    extras = getIntent().getExtras();
        //KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE); 
        //lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE); 

    PodCastUrl = extras.getString("url");
    PodCastTitle = extras.getString("title");
    PodCastLength = extras.getInt("length",0);
    PodCastLenkb = extras.getInt("lenkb",0);
    isAudio=extras.getInt("media",0)==1;
    
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    mWakeLock = pm.newWakeLock(isAudio?PowerManager.SCREEN_DIM_WAKE_LOCK:PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
    
    tv=(TextView)findViewById(R.id.text_kb_streamed);
    tv.setText(PodCastTitle);
    initControls();
    myRefreshThread = new Thread(new secondCountDownRunner());
    myRefreshThread.start();
    
  }
  
//  @Override
//  public Object onRetainNonConfigurationInstance() {
//    if(mMediaPlayer.isPlaying()){
//          mMediaPlayer.setOnBufferingUpdateListener(null);
//          mMediaPlayer.setOnCompletionListener(null);
//          mMediaPlayer.setOnPreparedListener(null);
//          mMediaPlayer.setOnVideoSizeChangedListener(null);
//      return mMediaPlayer;
//    }
//    return null;
//  }

  
  @Override
  public void onStop(){
    super.onStop();
    releaseMediaPlayer();
    doCleanUp();
    if(mWakeLock.isHeld()){
      mWakeLock.release();
    }
  }
  
  Handler myViewUpdateHandler = new Handler() {
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case TMRMSG:
          if(prog!=null&&mMediaPlayer!=null){
            int a=mMediaPlayer.getDuration();
            if(a==0){
              break;
            }            
            all.setText(TimeFromMS(a));
            int b=mMediaPlayer.getCurrentPosition();
            cur.setText(TimeFromMS(b));
            int pct=(b*100)/a;
            prog.setProgress(pct);
          }
          if(extras.getInt(MEDIA)!=LOCAL_AUDIO){
            mTick++;
            if(mTick>=8){ //4 seconds
              mTick=0;
              DimableControls.setVisibility(View.INVISIBLE);
            }
          }
          break;
      }
      super.handleMessage(msg);
    }
  };
  
  class secondCountDownRunner implements Runnable {
    // send a message every 2 seconds to update the actual progress 
    // of the media player.
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
    cur=(TextView)findViewById(R.id.TextView01);
    all=(TextView)findViewById(R.id.TextView02);
    ImageView img=(ImageView)findViewById(R.id.ImageView01);
    img.setVisibility(View.GONE);
    prog=(SeekBar)findViewById(R.id.SeekBar01);//progress_bar);
    prog.setMax(100);
    prog.setBackgroundColor(0);
    prog.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
          if(mMediaPlayer.isPlaying()){
            int a=mMediaPlayer.getDuration();
            if(a==0){
              return;
            }
            //int b=mMediaPlayer.getCurrentPosition();
            //int pct=(b*100)/a;
            //prog.setProgress(pct);       
            int where=(int)((float)a*((float)progress/100.0f));
            mMediaPlayer.seekTo(where);
          }
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        
      }
    });
    
    DimableControls=(LinearLayout)findViewById(R.id.dimable);
    playButton = (ImageButton) findViewById(R.id.button_play);
    playButton.setEnabled(true); 
    playButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        if(mMediaPlayer.isPlaying()){
          mMediaPlayer.pause();
          playButton.setImageResource(R.drawable.button_play);
        }else{
          mMediaPlayer.start();
          playButton.setImageResource(R.drawable.button_pause);
        }
     }
    });
  }

  private void playVideo(Integer Media) {
    doCleanUp();
    try {
      switch (Media) {
      case LOCAL_AUDIO:
        mPreview.setBackgroundResource(R.drawable.sflogo);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(PodCastUrl);
        mMediaPlayer.prepare();
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        //mMediaPlayer.setOnVideoSizeChangedListener(this);
        
    int w=mPreview.getWidth();
    int h=mPreview.getHeight();
    if(h==0){
      return;
    }
    int tempw,temph;
    LinearLayout.LayoutParams lp;
      //set width as a function of height and aspect ratio
    //tempw=(560*h)/1000;
      tempw=w/2;
      temph=h/2;//(w*1000)/56;
      //set height as a function of width and aspect ratio
      lp=new LinearLayout.LayoutParams(tempw,temph);
      lp.leftMargin=(w-tempw)/2;
      lp.topMargin=(h-temph)/2;
      mPreview.setLayoutParams(lp);
        
        
        mMediaPlayer.start();
        mWakeLock.acquire();
        return;
      case LOCAL_VIDEO:
      case STREAM_VIDEO:
        break;

      }

      // Create a new media player and set the listeners
      mWakeLock.acquire();
      mMediaPlayer = new MediaPlayer();
      //http://www.podtrac.com/pts/redirect.mp4?http://traffic.libsyn.com/sciencefriday/pumpkinphysics-102210.mp4
      //mMediaPlayer.setDataSource("http://c1.libsyn.com/media/18801/pumpkinphysics-102210.mp4?nvb=20101025015704&nva=20101026020704&sid=c75f480f10d3b638d5115ae29ac3da66&l_sid=18801&l_eid=&l_mid=2193174&t=0dd97c893cdb7a164da49");
      mMediaPlayer.setDataSource(walkUrlRedirects(PodCastUrl));
      mMediaPlayer.setDisplay(holder);
      mMediaPlayer.prepare();
      mMediaPlayer.setOnBufferingUpdateListener(this);
      mMediaPlayer.setOnCompletionListener(this);
      mMediaPlayer.setOnPreparedListener(this);
      mMediaPlayer.setOnVideoSizeChangedListener(this);
      mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    } catch (Exception e) {
      Log.e(TAG, "error: " + e.getMessage(), e);
    }
  }
  
  protected String walkUrlRedirects(String url){
    HttpClient client=new DefaultHttpClient();
    HttpContext context = new BasicHttpContext();
    HttpHead request=new HttpHead(url);
    request.setHeader("User-Agent","hoxnet.sciencefriday");
    try{
      client.execute(request,context);
      RequestWrapper requestWrapper = (RequestWrapper) context.getAttribute(ExecutionContext.HTTP_REQUEST);
      HttpUriRequest urirequest = (HttpUriRequest)requestWrapper.getOriginal();
      return urirequest.getURI().toString();      
    }catch(Exception e){
      Log.e(TAG, "error: " + e.getMessage(), e);
    }
    return "";
  }
  

  public void onBufferingUpdate(MediaPlayer arg0, int percent) {
    prog.setSecondaryProgress(percent);
  }

  public void onCompletion(MediaPlayer arg0) {
    if(mWakeLock.isHeld()){
      mWakeLock.release();
    }
    finish();
  }

  public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    Log.v(TAG, "onVideoSizeChanged called");
    if (width == 0 || height == 0) {
      Log
          .e(TAG, "invalid video width(" + width + ") or height(" + height
              + ")");
      return;
    }
    mIsVideoSizeKnown = true;
    mVideoWidth = width;
    mVideoHeight = height;
    if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
      startVideoPlayback();
    }
  }

  public void onPrepared(MediaPlayer mediaplayer) {
    Log.d(TAG, "onPrepared called");
    mIsVideoReadyToBePlayed = true;
    if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
      startVideoPlayback();
    }
  }

  public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
    Log.d(TAG, "surfaceChanged called");

  }

  public void surfaceDestroyed(SurfaceHolder surfaceholder) {
    Log.d(TAG, "surfaceDestroyed called");
  }

  public void surfaceCreated(SurfaceHolder holder) {
    Log.d(TAG, "surfaceCreated called");
    playVideo(extras.getInt(MEDIA));

  }

  @Override
  protected void onPause() {
    super.onPause();    
    //releaseMediaPlayer();
    //doCleanUp();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    releaseMediaPlayer();
    doCleanUp();
    if(mWakeLock.isHeld()){
      mWakeLock.release();
    }
  }

  private void releaseMediaPlayer() {
    if (mMediaPlayer != null) {
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }

  private void doCleanUp() {
    mVideoWidth = 0;
    mVideoHeight = 0;
    mIsVideoReadyToBePlayed = false;
    mIsVideoSizeKnown = false;
  }

  private void startVideoPlayback() {
    if(mVideoHeight==0){
      return;
    }
    int vidaspect=(mVideoWidth*1000)/mVideoHeight;
    int w=mPreview.getWidth();
    int h=mPreview.getHeight();
    if(h==0){
      return;
    }
    int winaspect=(w*1000)/h;
    int tempw,temph;
    LinearLayout.LayoutParams lp;
    if(winaspect>vidaspect){
      //set width as a function of height and aspect ratio
      tempw=(vidaspect*h)/1000;
      temph=h;
      lp=new LinearLayout.LayoutParams(tempw,temph);
      lp.leftMargin=(w-tempw)/2;
      mPreview.setLayoutParams(lp);
    }else{
      tempw=w;
      temph=(w*1000)/vidaspect;
      //set height as a function of width and aspect ratio
      lp=new LinearLayout.LayoutParams(tempw,temph);
      lp.topMargin=(h-temph)/2;
      mPreview.setLayoutParams(lp);
    }
//      lp=new LinearLayout.LayoutParams(w,h);
//      mPreview.setLayoutParams(lp);
      //mPreview.setZOrderOnTop(true);
    holder.setFixedSize(mVideoWidth,mVideoHeight);
    mMediaPlayer.start();
  }
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

  
}
