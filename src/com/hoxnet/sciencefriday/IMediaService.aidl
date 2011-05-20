package com.hoxnet.sciencefriday;

import com.hoxnet.sciencefriday.IMediaServiceCallback;

interface IMediaService {
    /**
     * Often you want to allow a service to call back to its clients.
     * This shows how to do so, by registering a callback interface with
     * the service.
     */
    void registerCallback(IMediaServiceCallback cb); 
    
    /**
     * Remove a previously registered callback interface.
     */
    void unregisterCallback(IMediaServiceCallback cb);
}
