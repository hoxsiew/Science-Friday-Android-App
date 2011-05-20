package com.hoxnet.sciencefriday;

oneway interface IMediaServiceCallback {
    /**
     * Called when the service has a new value for you.
     */
    void valueChanged(int type,int value); 
}
