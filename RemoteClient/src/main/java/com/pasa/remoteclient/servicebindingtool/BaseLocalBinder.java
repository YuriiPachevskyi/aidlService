package com.pasa.remoteclient.servicebindingtool;

import android.app.Service;
import android.os.Binder;

public class BaseLocalBinder<S extends Service> extends Binder {
    private final S mService;

    public BaseLocalBinder(S service) {
        mService = service;
    }

    public S getService() {
        return mService;
    }
}
