package com.pasa.remoteclient.servicebindingtool;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import java.util.ArrayList;
import java.util.List;

public class ServiceBindingHelper<S extends Service> {
    private final List<IServiceBindingListener<S>> mListeners = new ArrayList<>();
    private final Context mContext;
    private final Class<? extends S> mConcreteClass;
    private S mService;
    private boolean mIsBound = false;
    private boolean mIsConnected = false;

    public ServiceBindingHelper(@NonNull Context context, @NonNull Class<? extends S> concreteClass) {
        mContext = context;
        mConcreteClass = concreteClass;
    }

    @UiThread
    public void addListener(@NonNull IServiceBindingListener<S> listener) {
        if(mIsConnected) {
            listener.onServiceBound(mService);
        }
        mListeners.add(listener);
    }

    @UiThread
    public void removeListener(@NonNull IServiceBindingListener<S> listener) {
        mListeners.remove(listener);
    }

    @UiThread
    public void bind() {
        if (!mIsBound) {
            mIsBound = true;
            Intent bindIntent = new Intent(mContext, mConcreteClass);
            mContext.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @UiThread
    public void unbind() {
        if (mIsBound) {
            mContext.unbindService(mServiceConnection);
            mIsBound = false;
            if(mIsConnected) {
                mIsConnected = false;
                notifyServiceUnbound();
            }
        }
    }

    @UiThread
    public S getService() {
        return mIsConnected ? mService : null;
    }

    @UiThread
    public boolean isServiceBound() {
        return mIsConnected;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        @SuppressWarnings("unchecked")
        public void onServiceConnected(ComponentName className, IBinder binder) {
            BaseLocalBinder<S> concreteBinder = (BaseLocalBinder<S>) binder;
            mService = concreteBinder.getService();
            mIsConnected = true;
            notifyServiceBound();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mIsConnected = false;
            notifyServiceUnbound();
        }
    };

    private void notifyServiceBound() {
        for (IServiceBindingListener<S> listener : getListenersCopy()) {
            listener.onServiceBound(mService);
        }
    }

    private void notifyServiceUnbound() {
        for (IServiceBindingListener<S> listener : getListenersCopy()) {
            listener.onServiceUnbound();
        }
    }

    private List<IServiceBindingListener<S>> getListenersCopy() {
        return new ArrayList<>(mListeners);
    }
}