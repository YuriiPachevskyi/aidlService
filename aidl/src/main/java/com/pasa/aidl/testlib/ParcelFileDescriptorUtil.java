package com.pasa.aidl.testlib;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ParcelFileDescriptorUtil {

    public static ParcelFileDescriptor pipeTo(OutputStream outputStream) throws IOException {
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readSide = pipe[0];
        ParcelFileDescriptor writeSide = pipe[1];

        new TransferThread(new ParcelFileDescriptor.AutoCloseInputStream(readSide), outputStream).start();

        return writeSide;
    }

    static class TransferThread extends Thread {
        final InputStream mIn;
        final OutputStream mOut;

        TransferThread(InputStream in, OutputStream out) {
            super("ParcelFileDescriptor Transfer Thread");
            mIn = in;
            mOut = out;
            setDaemon(true);
        }

        @Override
        public void run() {
            byte[] buf = new byte[1024];
            int len;

            try {
                while ((len = mIn.read(buf)) > 0) {
                    mOut.write(buf, 0, len);
                }
                mOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    mIn.close();
                } catch (IOException e) {
                }
                try {
                    mOut.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
