package com.pasa.remoteclient.servicebindingtool;

import android.app.Service;

public interface IServiceBindingListener <S extends Service> {
    void onServiceBound(S service);
    void onServiceUnbound();
}
