/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\vcprojects\\Android\\workspace\\SciFri\\src\\com\\hoxnet\\sciencefriday\\IMediaSec.aidl
 */
package com.hoxnet.sciencefriday;
//Methods that can be called by processes that have bound to the media service. 

public interface IMediaSec extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.hoxnet.sciencefriday.IMediaSec
{
private static final java.lang.String DESCRIPTOR = "com.hoxnet.sciencefriday.IMediaSec";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.hoxnet.sciencefriday.IMediaSec interface,
 * generating a proxy if needed.
 */
public static com.hoxnet.sciencefriday.IMediaSec asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.hoxnet.sciencefriday.IMediaSec))) {
return ((com.hoxnet.sciencefriday.IMediaSec)iin);
}
return new com.hoxnet.sciencefriday.IMediaSec.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getDuration:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getDuration();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getCurrentPosition:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getCurrentPosition();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getBufferedPosition:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getBufferedPosition();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getIntegerValue:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getIntegerValue();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getIsPlaying:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getIsPlaying();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getIsPaused:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getIsPaused();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_StartPlayer:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.StartPlayer();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_GetCurrentPath:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.GetCurrentPath();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_GetCurrentDesc:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.GetCurrentDesc();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_PausePlayer:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.PausePlayer(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_SetMediaSourcePath:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.SetMediaSourcePath(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_ResetPlayer:
{
data.enforceInterface(DESCRIPTOR);
this.ResetPlayer();
reply.writeNoException();
return true;
}
case TRANSACTION_SeekTo:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.SeekTo(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.hoxnet.sciencefriday.IMediaSec
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public int getDuration() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getDuration, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int getCurrentPosition() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCurrentPosition, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int getBufferedPosition() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getBufferedPosition, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int getIntegerValue() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getIntegerValue, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean getIsPlaying() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getIsPlaying, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean getIsPaused() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getIsPaused, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean StartPlayer() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_StartPlayer, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.lang.String GetCurrentPath() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_GetCurrentPath, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.lang.String GetCurrentDesc() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_GetCurrentDesc, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void PausePlayer(boolean pause) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((pause)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_PausePlayer, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void SetMediaSourcePath(java.lang.String s, java.lang.String d) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(s);
_data.writeString(d);
mRemote.transact(Stub.TRANSACTION_SetMediaSourcePath, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void ResetPlayer() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_ResetPlayer, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void SeekTo(int pos_ms) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(pos_ms);
mRemote.transact(Stub.TRANSACTION_SeekTo, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getDuration = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getCurrentPosition = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getBufferedPosition = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getIntegerValue = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getIsPlaying = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getIsPaused = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_StartPlayer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_GetCurrentPath = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_GetCurrentDesc = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_PausePlayer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_SetMediaSourcePath = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_ResetPlayer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_SeekTo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
}
public int getDuration() throws android.os.RemoteException;
public int getCurrentPosition() throws android.os.RemoteException;
public int getBufferedPosition() throws android.os.RemoteException;
public int getIntegerValue() throws android.os.RemoteException;
public boolean getIsPlaying() throws android.os.RemoteException;
public boolean getIsPaused() throws android.os.RemoteException;
public boolean StartPlayer() throws android.os.RemoteException;
public java.lang.String GetCurrentPath() throws android.os.RemoteException;
public java.lang.String GetCurrentDesc() throws android.os.RemoteException;
public void PausePlayer(boolean pause) throws android.os.RemoteException;
public void SetMediaSourcePath(java.lang.String s, java.lang.String d) throws android.os.RemoteException;
public void ResetPlayer() throws android.os.RemoteException;
public void SeekTo(int pos_ms) throws android.os.RemoteException;
}
