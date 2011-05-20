package com.hoxnet.sciencefriday;

//Methods that can be called by processes that have bound to the media service. 

interface IMediaSec {
  int getDuration();
  int getCurrentPosition();
  int getBufferedPosition();
  int getIntegerValue();
  boolean getIsPlaying();
  boolean getIsPaused();
  boolean StartPlayer();
  String GetCurrentPath();
  String GetCurrentDesc();
  void PausePlayer(boolean pause);
  void SetMediaSourcePath(String s,String d);
  void ResetPlayer();
  void SeekTo(int pos_ms);
}
