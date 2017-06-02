package com.pasa.remoteservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import com.pasa.aidl.testlib.IMainService;
import org.apache.commons.io.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainService extends Service {
    private static final String TAG = "MainService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: Received start command.");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: Received binding.");
        return mBinder;
    }

    private final IMainService.Stub mBinder = new IMainService.Stub() {
        @Override
        public void readInputFileDescriptor(ParcelFileDescriptor input) throws RemoteException {
            InputStream is = new ParcelFileDescriptor.AutoCloseInputStream(input);
            OutputStream os = new ByteArrayOutputStream();
            String inputResult = "uninitialized";

            try {
                int strLen = IOUtils.copy(is, os);
                inputResult = os.toString();

                Log.e(TAG, "read result strLen = " + strLen + " content = " + inputResult);

                is.close();
                os.close();
            }
            catch (IOException e) {
                Log.e(TAG, "Failed to read input " + e);
            }
            finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void writeOutputFileDescriptor(ParcelFileDescriptor output) throws RemoteException {
            InputStream is = new ByteArrayInputStream("Hello from service side!!!".getBytes());
            OutputStream os = new ParcelFileDescriptor.AutoCloseOutputStream(output);

            try {
                int count = IOUtils.copy(is, os);
                os.flush();

                Log.d(TAG, "wrote result strLen" + count);

                is.close();
                os.close();
            }
            catch (IOException e) {
                Log.e(TAG, "Failed to wrote result " + e);
            }
            finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void exit() throws RemoteException {
            Log.e(TAG, "exit: Received exit command.");
            stopSelf();
        }
    };
}
