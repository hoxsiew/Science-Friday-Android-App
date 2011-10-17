package com.hoxnet.sciencefriday;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.view.Menu;
import android.view.MenuItem;

public class SciFriMain extends Activity{
  private ListView mListView;
  private ArrayList<PodCastItem> mPodCastItems;
  private boolean isAudio=true;
  private static String AudioURL="http://www.sciencefriday.com/audio/scifriaudio.xml";
  public int mNetworkStatus=0;  
  private static final String TAG = "SciFriMediaPlayer";
  private static String VideoURL="http://www.sciencefriday.com/video/scifrivideo.xml";
  private boolean mIsRelease=false;
  private TextView mTitleBar;
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    //bindService(new Intent(IMediaSec.class.getName()),mSecondaryConnection,Context.BIND_AUTO_CREATE);
//    SharedPreferences settings = getSharedPreferences(MyPreferences.PREFERENCE_NAME,0);
//    SharedPreferences.Editor prefEditor = settings.edit();
//    prefEditor.putString(MyPreferences.MPSTATE, MyPreferences.MPSTATE_VALUE_INIT);
//    prefEditor.putLong(MyPreferences.ACTIVE_PLAYLIST, -1);
//    prefEditor.putInt(MyPreferences.LOOP_STATE, MyPreferences.LOOP_STATE_VALUE_OFF);
//    prefEditor.putInt(MyPreferences.SHUFFLE_STATE, MyPreferences.SHUFFLE_STATE_VALUE_OFF);
//    prefEditor.commit();   
    mNetworkStatus=CheckNetworkStatus();
    if(mNetworkStatus==0){
      AlertDialog.Builder builder=new AlertDialog.Builder(this);
      builder.setMessage("This application requires network connectivity.  None detected.")
        .setTitle("No Tetworkl!").setIcon(android.R.drawable.ic_dialog_alert)
        .setCancelable(false)
        .setPositiveButton("OK",new DialogInterface.OnClickListener(){
          public void onClick(DialogInterface dialog,int id){
            finish();
          }
        }
      ).show();
    }else{
      mHandler.obtainMessage(1).sendToTarget();
    }
    
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu){
    if(isAudio){
      menu.getItem(0).setIcon(R.drawable.videocw);
      menu.getItem(0).setTitle("Video Podcasts");
      //menu.getItem(1).setEnabled(true).setIcon(R.drawable.videocw);
    }else{
      menu.getItem(0).setIcon(R.drawable.audiocw);
      menu.getItem(0).setTitle("Audio Podcasts");
      //menu.getItem(1).setEnabled(false).setIcon(R.drawable.videobw);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  class RetrievePodcastIndex implements Runnable {
    public void run() {
      //Comparator<PodCastItem> mycomp=new java.util.Comparator<PodCastItem>();
      //mPodCastItems.clear();
      mPodCastItems=GetPodLinks();
      java.util.Collections.sort(mPodCastItems);
      mHandler.obtainMessage(0).sendToTarget();
    }
  }  
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu){
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.xml.menu,menu);
    menu.getItem(0).setEnabled(true).setIcon(R.drawable.videocw);
    menu.getItem(1).setEnabled(true).setIcon(R.drawable.donate);
    
    return super.onCreateOptionsMenu(menu);
  }
  

  @Override
  public boolean onMenuItemSelected(int featureId,MenuItem item){
    int a=item.getItemId();
    switch(a){ // 2131099654
      case R.id.switch_podcast:
        // Audio
        if(!isAudio){
          isAudio=true;
          mListView.setAdapter(new MyWaitingAdapter(this));
          mTitleBar.setText(R.string.ATitle);
          Thread myRefreshThread = new Thread(new RetrievePodcastIndex());
          myRefreshThread.start();
        }else{
          isAudio=false;
          mListView.setAdapter(new MyWaitingAdapter(this));
          mTitleBar.setText(R.string.VTitle);
          Thread myRefreshThread = new Thread(new RetrievePodcastIndex());
          myRefreshThread.start();
        }
        return true;
      case R.id.donate:
        //Toast.makeText(this,"Donate",Toast.LENGTH_LONG).show();
        Intent it = new Intent("android.intent.action.VIEW",Uri.parse("http://www.sciencefriday.com/about/sponsor/"));
        startActivity(it);
        return true;
      case R.id.about:
        Intent itt = new Intent();
        itt.setClassName("com.hoxnet.sciencefriday","com.hoxnet.sciencefriday.AboutSciFri");
        startActivity(itt);
        return true;
    }
    return super.onMenuItemSelected(featureId,item);
  }

  Handler mHandler=new Handler(){
    public void handleMessage(Message msg){
      switch(msg.what){
        case 0:
          mListView.setAdapter(new MyListAdapter(getBaseContext()));
          ((BaseAdapter)((HeaderViewListAdapter)mListView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
          break;
        case 1:
          getApplicationContext().bindService(new Intent(IMediaSec.class.getName()),mSecondaryConnection,Context.BIND_AUTO_CREATE);
          initControls();
          Thread myRefreshThread=new Thread(new RetrievePodcastIndex());
          myRefreshThread.start();
          break;
      }
      super.handleMessage(msg);
    }
  };
 
  private class MyListAdapter extends BaseAdapter{
    // private LayoutInflater mInflater;
    private Context mContext;

    public MyListAdapter(Context context){
      mContext=context;
    }

    @Override
    public int getCount(){
      if(mPodCastItems!=null){
        return mPodCastItems.size();
      }
      return 0;
    }

    @Override
    public Object getItem(int arg0){
      return arg0;
    }

    @Override
    public long getItemId(int position){
      return position;
    }

    @Override
    public View getView(int position,View convertView,ViewGroup parent){
      PodCastItem pci;
      MyCustomView sv;
      if(convertView==null){
        sv=new MyCustomView(mContext);
      }else{
        sv=(MyCustomView)convertView;
      }
      pci=mPodCastItems.get(position);
      sv.setTitle(pci.title,pci.pubDate,pci.len);
      sv.setAsHeader(false);
      return sv;
    }
  }

  private class MyWaitingAdapter extends BaseAdapter{
    // private LayoutInflater mInflater;
    private Context mContext;

    public MyWaitingAdapter(Context context){
      mContext=context;
    }

    @Override
    public int getCount(){
      return 1;
    }

    @Override
    public Object getItem(int arg0){
      return arg0;
    }

    @Override
    public long getItemId(int position){
      return position;
    }

    @Override
    public View getView(int position,View convertView,ViewGroup parent){
      MyWaitingView sv;
      if(convertView==null){
        sv=new MyWaitingView(mContext);
      }else{
        sv=(MyWaitingView)convertView;
      }
      return sv;
    }
  }

  private void initControls(){
    mListView=(ListView)findViewById(R.id.ListView01);
    mTitleBar=(TextView)findViewById(R.id.TextView01);
    mTitleBar.setText(R.string.ATitle);
    mTitleBar.setGravity(android.view.Gravity.CENTER);
    LayoutInflater li=getLayoutInflater();
    View v1=li.inflate(R.layout.list_footer,null);
    TextView mFooter=(TextView)v1.findViewById(R.id.TextViewFooter);
    mFooter.setTextSize(11);
    mFooter.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
    if(mIsRelease==false){ //enable for final distribution
      Pattern p=Pattern.compile("\\bHoxnet Software\\b");
      android.text.util.Linkify.addLinks(mFooter,p,"http://droid.hoxnet.com");
    }else{
      android.text.util.Linkify.addLinks(mFooter,android.text.util.Linkify.WEB_URLS);
    }
    mListView.addFooterView(v1);    
    v1=li.inflate(R.layout.list_header,null);
    mListView.addHeaderView(v1);
    mListView.setAdapter(new MyWaitingAdapter(this));
    mListView.setOnItemClickListener(new OnItemClickListener(){
      public void onItemClick(AdapterView<?> v,View view,int i,long l){
        if(i==0){
          return;
        }
        //boolean bRes;
        //int last=mPodCastItems.size();
        //if(i>last){
          // footer. Link to web site
          // Intent it = new
          // Intent("android.intent.action.VIEW",Uri.parse("http://www.sciencefriday.com/"));
          // startActivity(it);
        //  return;
        //}
        PodCastItem pc=mPodCastItems.get(i-1);
         
//        
//        
        Intent it=new Intent();
        if(isAudio){
          it.setClassName("com.hoxnet.sciencefriday","com.hoxnet.sciencefriday.SciFriPlayer");
        }else{
          it.setClassName("com.hoxnet.sciencefriday","com.hoxnet.sciencefriday.SciFriMediaPlayer");
          boolean bplay=false;
          try{
            bplay=mSecondaryService.getIsPlaying();
            if(bplay){
              mSecondaryService.ResetPlayer();
            }
          }catch(RemoteException e){
            Log.e(TAG,e.getMessage());
            finish();
          }
        }
        //it.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //String s=pc.uri;
        it.putExtra("url",pc.uri);
        it.putExtra("title",pc.title);
        it.putExtra("pubDate",pc.pubDate);
        it.putExtra("length",pc.len_in_sec);
        it.putExtra("lenkb",pc.len_in_bytes/1024);
        it.putExtra("media",isAudio?1:5);
        startActivity(it);
        //startActivityIfNeeded(it,100);
        
      }
    });
  }

  private ArrayList<PodCastItem> GetPodLinks(){
    String s;
    mXmlHandler h=new mXmlHandler();

//    if(isAudio){
      s=getUrlContent(isAudio?AudioURL:VideoURL);
      s=s.replaceAll("itunes:","itunes_");
      try{
        android.util.Xml.parse(s,h);
      }catch(SAXException e){
        e.printStackTrace();
      }
      return h.Items;
//    }else{
//      s=getUrlContent(VideoURL);
//      s=s.replaceAll("itunes:","itunes_");
//      //h.isVideo=true;
//      try{
//        android.util.Xml.parse(s,h);
//      }catch(SAXException e){
//        e.printStackTrace();
//      }
//      return h.Items;
//    }
  }

  private class MyWaitingView extends LinearLayout{
    private LayoutInflater li=getLayoutInflater();
    private View v1=li.inflate(R.layout.waiting,null);
    //private PodView mPodView;
    
    public MyWaitingView(Context context){
      super(context);
      setOrientation(VERTICAL);
      //mPodView=new PodView(context);
      //addView(mPodView,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
      addView(v1,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
    }
  }
  
  private class MyCustomView extends LinearLayout{
    private LayoutInflater li=getLayoutInflater();
    private View v1=li.inflate(R.layout.pod_link,null);
    //private PodView mPodView;
    
    public MyCustomView(Context context){
      super(context);
      setOrientation(VERTICAL);
      //mPodView=new PodView(context);
      //addView(mPodView,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
      addView(v1,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
    }

    public void setTitle(String title,String pubDate,String lenSec){
      TextView tv1=(TextView)v1.findViewById(R.id.TextView01);
      TextView tv2=(TextView)v1.findViewById(R.id.TextView02);
      TextView tv3=(TextView)v1.findViewById(R.id.TextView03);
      tv2.setText(FormatDate(pubDate));
//      float sz=(float)tv2.getHeight();
      ImageView iv1=(ImageView)v1.findViewById(R.id.ImageView02);
      iv1.setImageResource(isAudio?R.drawable.audiocw:R.drawable.videocw);
      //iv1.setLayoutParams(new LayoutParams((int)(sz*2.8),(int)(sz*2.8)));
      //iv1.setPadding(10,0,0,(int)(sz/2));
      tv1.setText(title);
      tv3.setText("Length: "+lenSec);
      //mPodView.setPubDate(pubDate);
    }

    public void setAsHeader(boolean header){
      //mPodView.setState(header?0:1);
    }

    // public void setAsFooter(boolean footer) {
    // mPodView.setState(footer?2:1);
    // }

  }
  public String FormatDate(String text){
    //String ret=new String();
    String s[]=new String[6];
    StringTokenizer st = new StringTokenizer(text);
    for(int i=0;i<6;i++){
      if(st.hasMoreTokens()){
        s[i]=st.nextToken();
      }else{
        s[i]="";
      }
    }
    //return String.format("%s %s, %s %s",s[2],s[1],s[3],s[4]);
    return String.format("%s %s, %s",s[2],s[1],s[3]);
    //return ret;
  }

  protected String getUrlContent(String url){
    // Create client and set our specific user-agent string
    HttpClient client=new DefaultHttpClient();
    HttpGet request=new HttpGet(url);
    request.setHeader("User-Agent","hoxnet.sciencefriday");
    byte sBuffer[]=new byte[256];
    try{
      HttpResponse response=client.execute(request);

      // Check if server response is valid
      StatusLine status=response.getStatusLine();
      if(status.getStatusCode()!=200){
        return "";
      }
      // Pull content stream from response
      HttpEntity entity=response.getEntity();
      InputStream inputStream=entity.getContent();

      ByteArrayOutputStream content=new ByteArrayOutputStream();

      // Read response into a buffered stream
      int readBytes=0;

      while((readBytes=inputStream.read(sBuffer))!=-1){
        content.write(sBuffer,0,readBytes);
      }
      // Return result from buffered stream
      return new String(content.toByteArray());
    }catch(IOException e){
      Toast.makeText(null,e.getMessage(),Toast.LENGTH_LONG).show();
    }
    return "";
  }

//  @Override
//  protected void onStop(){
//    //unbindService(mSecondaryConnection);
//    //unbindService(mConnection);
//    super.onStop();
//  }


  private class mXmlHandler implements ContentHandler{

    public ArrayList<PodCastItem> Items=new ArrayList<PodCastItem>();
    //private boolean isVideo=false;
    private PodCastItem currentPodCastItem;
    private boolean ItemOpen=false,TitleOpen=false,itunes_len=false,PubDateOpen=false;
    SimpleDateFormat sdf=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");

    @Override
    public void startElement(String uri,String localName,String qName,Attributes atts) throws SAXException{
      if(localName.compareToIgnoreCase("item")==0){
        currentPodCastItem=new PodCastItem();
        ItemOpen=true;
        return;
      }
      if(localName.compareToIgnoreCase("enclosure")==0&&ItemOpen){
//        if(false){//isVideo){
//          String s=atts.getValue("url");
//          int i=s.indexOf('?')+1;
//          currentPodCastItem.uri=s.substring(i);
//          currentPodCastItem.len_in_bytes=Integer.valueOf(atts.getValue("length"));
//        }else{
          currentPodCastItem.uri=atts.getValue("url");
          currentPodCastItem.len_in_bytes=Integer.valueOf(atts.getValue("length"));
//        }
        return;
      }
      if(localName.compareToIgnoreCase("title")==0&&ItemOpen){
        TitleOpen=true;
        return;
      }
      if(localName.compareToIgnoreCase("pubDate")==0&&ItemOpen){
        PubDateOpen=true;
        return;
      }
      if(localName.compareToIgnoreCase("itunes_duration")==0&&ItemOpen){
        itunes_len=true;
        return;
      }
    }

    /*
     * <item> <title>Mark Twain And Science: It's Complicated</title>
     * <description><![CDATA[Did you know Mark Twain tried his hand at science
     * fiction? In the book <em>The Disappearing Spoon,</em> author Sam Kean
     * writes about Twain's prescient story "Sold to Satan." In the story,
     * Satan<E2><80><99>s problems stem, in part, from the fact that he is made
     * entirely of the newly discovered radioactive element
     * radium.]]></description> <pubDate>Fri, 01 Oct 2010 18:12:34
     * -0400</pubDate>
     * <link>http://www.npr.org/templates/story/story.php?storyId
     * =130268526&amp;ft=2&amp;f=510221</link>
     * <guid>http://podcastdownload.npr.org
     * /anon.npr-podcasts/podcast/510221/130276680/npr_130276680.mp3</guid>
     * <itunes:summary><![CDATA[Did you know Mark Twain tried his hand at
     * science fiction? In the book <em>The Disappearing Spoon,</em> author Sam
     * Kean writes about Twain's prescient story "Sold to Satan." In the story,
     * Satan<E2><80><99>s problems stem, in part, from the fact that he is made
     * entirely of the newly discovered radioactive element
     * radium.]]></itunes:summary> <itunes:keywords>NPR,National Public
     * Radio,Science Friday,Morning Edition,All Things Considered,Fresh
     * Air</itunes:keywords> <itunes:duration>4:35</itunes:duration>
     * <itunes:explicit>no</itunes:explicit> <enclosureurl=
     * "http://podcastdownload.npr.org/anon.npr-podcasts/podcast/510221/130276680/npr_130276680.mp3"
     * length="2234287" type="audio/mpeg"/> </item>
     */

    @Override
    public void endElement(String uri,String localName,String qName) throws SAXException{
      if(localName.compareToIgnoreCase("item")==0){
        Items.add(currentPodCastItem);
        ItemOpen=false;
        return;
      }
      if(localName.compareToIgnoreCase("title")==0&&ItemOpen){
        TitleOpen=false;
        return;
      }
      if(localName.compareToIgnoreCase("pubDate")==0&&ItemOpen){
        PubDateOpen=false;
        try{
          currentPodCastItem.SortDate=sdf.parse(currentPodCastItem.pubDate);
        }catch(ParseException e){
          e.printStackTrace();
          currentPodCastItem.SortDate=null;
        }      
        return;
      }
      if(localName.compareToIgnoreCase("itunes_duration")==0&&ItemOpen){
        itunes_len=false;
        //some podcasts list this as hh:mm:ss, others as xxx (sec).
        int i=0,j,count=0;
        while((j=currentPodCastItem.len.indexOf(':',i))!=-1){
          i=j+1;
          count++;
        }
        if(count==0){  //no colons in the string.
          currentPodCastItem.len_in_sec=Integer.valueOf(currentPodCastItem.len);
          currentPodCastItem.len=FormatTime(currentPodCastItem.len_in_sec);
        }else{        
          for(i=0;i<(2-count);i++){
            currentPodCastItem.len="0:"+currentPodCastItem.len;
          }
          int times[]=new int[3];
  
          StringTokenizer st=new StringTokenizer(currentPodCastItem.len,":");
          for(i=0;i<3;i++){
            if(st.hasMoreTokens()){
              times[i]=Integer.parseInt(st.nextToken());
            }else{
              times[i]=0;
            }
          }
          currentPodCastItem.len_in_sec=times[0]*3600+times[1]*60+times[2];
          currentPodCastItem.len=FormatTime(currentPodCastItem.len_in_sec);
        }
        return;
      }
    }

    private String FormatTime(int secs){
          String s;//=new String();
          int hr,mn,sc;

          if(secs>3600){
            hr=secs/3600;
            mn=secs%3600;
            sc=mn%60;
            s=String.format("%d:%02d:%02d",hr,mn/60,sc);
          }else if (secs>60){
            mn=secs/60;
            sc=secs%60;
            s=String.format("%d:%02d",mn,sc);
          }else{              
            s=String.format("0:%02d",secs);
          }
          return s;
    }
    
    @Override
    public void characters(char[] ch,int start,int length) throws SAXException{
      if(TitleOpen&&ItemOpen){
        if(currentPodCastItem.title==null){
          currentPodCastItem.title=String.copyValueOf(ch,start,length);
        }else{
          currentPodCastItem.title+=String.copyValueOf(ch,start,length);
        }
      }
      if(PubDateOpen&&ItemOpen){
        if(currentPodCastItem.pubDate==null){
          currentPodCastItem.pubDate=String.copyValueOf(ch,start,length);
        }else{
          currentPodCastItem.pubDate+=String.copyValueOf(ch,start,length);
        }
      }
      if(itunes_len&&ItemOpen){
        if(currentPodCastItem.len==null){
          currentPodCastItem.len=String.copyValueOf(ch,start,length);
        }else{
          currentPodCastItem.len+=String.copyValueOf(ch,start,length);
        }
      }
    }

    @Override
    public void endDocument() throws SAXException{
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException{
    }

    @Override
    public void ignorableWhitespace(char[] ch,int start,int length) throws SAXException{
    }

    @Override
    public void processingInstruction(String target,String data) throws SAXException{
    }

    @Override
    public void setDocumentLocator(Locator locator){
    }

    @Override
    public void skippedEntity(String name) throws SAXException{
    }

    @Override
    public void startDocument() throws SAXException{
    }

    @Override
    public void startPrefixMapping(String prefix,String uri) throws SAXException{
    }
  }
  private class PodCastItem implements Comparable<PodCastItem>{
    String uri;
    String title;
    String pubDate;
    String len;
    int len_in_bytes;
    int len_in_sec;
    Date SortDate;
    
    @Override
    public int compareTo(PodCastItem another){
      if(SortDate!=null&&another.SortDate!=null){
        return another.SortDate.compareTo(SortDate);
      }
      return 0;
    }
  }

//  
//  //Interface stuff below.
//  
//  /** The primary interface we will be calling on the service. */
//  IMediaService mService=null;
//  /** Another interface we use on the service. */
  IMediaSec mSecondaryService=null;
//  
//  
//  
//
//  private ServiceConnection mConnection=new ServiceConnection(){
//    public void onServiceConnected(ComponentName className,IBinder service){
//      // This is called when the connection with the service has been
//      // established, giving us the service object we can use to
//      // interact with the service. We are communicating with our
//      // service through an IDL interface, so get a client-side
//      // representation of that from the raw service object.
//      mService=IMediaService.Stub.asInterface(service);
//      try{
//        mService.registerCallback(mCallback);
//      }catch(RemoteException e){
//        // In this case the service has crashed before we could even
//        // do anything with it; we can count on soon being
//        // disconnected (and then reconnected if it can be restarted)
//        // so there is no need to do anything here.
//      }
//    }
//
//    public void onServiceDisconnected(ComponentName className){
//      // This is called when the connection with the service has been
//      // unexpectedly disconnected -- that is, its process crashed.
//      mService=null;
//    }
//  };
//
//  /**
//   * Class for interacting with the secondary interface of the service.
//   */
  private ServiceConnection mSecondaryConnection=new ServiceConnection(){
    public void onServiceConnected(ComponentName className,IBinder service){
      // Connecting to a secondary interface is the same as any
      // other interface.
      mSecondaryService=IMediaSec.Stub.asInterface(service);
    }

    public void onServiceDisconnected(ComponentName className){
      mSecondaryService=null;
    }
  };

  //
  // private IMediaServiceCallback mCallback=new IMediaServiceCallback.Stub(){
  // public void valueChanged(int type,int value){
  // mHandler.sendMessage(mHandler.obtainMessage(type,value,0));
  // }
  // };

  public class MyPreferences{
    public static final String PREFERENCE_NAME="MediaPlayerPrefs";
    public static final String MPSTATE="mpState";
    public static final String MPSTATE_VALUE_PLAYING="playing";
    public static final String MPSTATE_VALUE_PAUSED="paused";
    public static final String MPSTATE_VALUE_INIT="initial";
    public static final String MPSTATE_VALUE_STOPPED="stopped";
    public static final String ACTIVE_PLAYLIST="activePlaylist";
    public static final String CURRENT_SONG_IN_PLAYLIST="currentSongInPlaylist";
    public static final String CURRENT_SONG_ID="currentSongId";
    public static final String SHUFFLE_STATE="shuffleState";
    public static final String LOOP_STATE="loopState";
    public static final int SHUFFLE_STATE_VALUE_ON=1;
    public static final int SHUFFLE_STATE_VALUE_OFF=0;
    public static final int LOOP_STATE_VALUE_ON=1;
    public static final int LOOP_STATE_VALUE_OFF=0;

    MyPreferences(){
    };
  }

  int CheckNetworkStatus(){
    final ConnectivityManager connMgr=(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
    final android.net.NetworkInfo wifi=connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    final android.net.NetworkInfo mobile=connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    if(wifi.isAvailable()){
      if(wifi.getState()==State.CONNECTED){
        return 2;
      }
    }
    if(mobile.isAvailable()){
      if(mobile.getState()==State.CONNECTED){
        return 1;
      }
    }
    return 0;
  }

}

