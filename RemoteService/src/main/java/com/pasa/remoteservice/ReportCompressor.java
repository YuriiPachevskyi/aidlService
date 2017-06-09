package com.pasa.remoteservice;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ReportCompressor {
    private static final String TAG = "ReportCompressor";

    public void compress(String path) {
        File file = new File(path);

        if (file.isDirectory() && file.listFiles().length == 0) {
            Log.e(TAG, "Failed to compress empty directory");
        }
        else {
            compress(file);
        }
    }

    private void compress(File file) {
        try {
            OutputStream fos = new FileOutputStream(cutFileExtension(file.getPath()) + ".zip");
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));

            if (file.isDirectory()) {
                compressDirectory(file.getName(), file, zos);
            }
            else {
                compressFile(file.getName(), file, zos);
            }

            zos.flush();
            zos.close();
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compressDirectory(String rootDir, File folder, ZipOutputStream zos) {
        for (File file: folder.listFiles()) {
            if (file.isDirectory()) {
                compressDirectory(rootDir, file, zos);
                continue;
            }

            compressFile(file.getPath().substring(file.getPath().indexOf(rootDir)), file, zos);
        }
    }

    private void compressFile(String entryPath, File file, ZipOutputStream zos) {
        byte[] buffer = new byte[2048];

        try {
            FileInputStream fis = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(entryPath);

            zos.putNextEntry(zipEntry);

            for (int length; (length = fis.read(buffer)) != -1; zos.write(buffer, 0, length));

            fis.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String cutFileExtension(String path) {
        int index = path.lastIndexOf(".");

        return index == -1 ? path : path.substring(0, index);
    }
}

