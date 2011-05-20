/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\vcprojects\\Android\\workspace\\SciFri\\src\\com\\hoxnet\\sciencefriday\\IMediaServiceCallback.aidl
 */
package com.hoxnet.sciencefriday;
public interface IMediaServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.hoxnet.sciencefriday.IMediaServiceCallback
{
private static final java.lang.String DESCRIPTOR = "com.hoxnet.sciencefriday.IMediaServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.hoxnet.sciencefriday.IMediaServiceCallback interface,
 * generating a proxy if needed.
 */
public static com.hoxnet.sciencefriday.IMediaServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.hoxnet.sciencefriday.IMediaServiceCallback))) {
return ((com.hoxnet.sciencefriday.IMediaServiceCallback)iin);
}
return new com.hoxnet.sciencefriday.IMediaServiceCallback.Stub.Proxy(obj);
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
case TRANSACTION_valueChanged:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
this.valueChanged(_arg0, _arg1);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.hoxnet.sciencefriday.IMediaServiceCallback
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
/**
     * Called when the service has a new value for you.
     */
public void valueChanged(int type, int value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(type);
_data.writeInt(value);
mRemote.transact(Stub.TRANSACTION_valueChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_valueChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
/**
     * Called when the service has a new value for you.
     */
public void valueChanged(int type, int value) throws android.os.RemoteException;
}
