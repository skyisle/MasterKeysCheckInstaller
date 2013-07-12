package com.alanjeon.blueboxcheck;

import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by skyisle on 13. 7. 12..
 */
public class CheckBlueBoxBug {

    public boolean hasVulnerability(Uri uri) throws FileNotFoundException {
        boolean hasDuplicatedEntry = false;

        final File sourceFile = new File(uri.getPath());

        InputStream theFile = new FileInputStream(sourceFile);
        ZipInputStream stream = new ZipInputStream(theFile);

        HashMap<String, ZipEntry> entryHashMap = new HashMap<String, ZipEntry>();
        try {
            // now iterate through each item in the stream. The get next
            // entry call will return a ZipEntry for each file in the
            // stream
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {

                // bug 8219321
                if (entryHashMap.get(entry.getName()) != null) {
                    hasDuplicatedEntry = true;
                    break;
                }

                entryHashMap.put(entry.getName(), entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // we must always close the zip file.
            closeQuietly(stream);
        }

        return hasDuplicatedEntry;
    }

    private void closeQuietly(ZipInputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public boolean isPatchedDevice() {
        boolean patched = false;
        try {
            patched = testDuplicateEntries();
        } catch (IOException e) {
        }

        return patched;
    }


    // copied from http://goo.gl/sGmMa
    private static void replaceBytes(byte[] original, byte[] replacement, byte[] buffer) {
        // Gotcha here: original and replacement must be the same length
        //assertEquals(original.length, replacement.length);

        boolean found;
        for (int i = 0; i < buffer.length - original.length; i++) {
            found = false;
            if (buffer[i] == original[0]) {
                found = true;
                for (int j = 0; j < original.length; j++) {
                    if (buffer[i + j] != original[j]) {
                        found = false;
                        break;
                    }
                }
            }
            if (found) {
                for (int j = 0; j < original.length; j++) {
                    buffer[i + j] = replacement[j];
                }
            }
        }
    }

    /**
     * Make sure we don't fail silently for duplicate entries.
     * b/8219321
     */
    public boolean testDuplicateEntries() throws IOException {
        String entryName = "test_file_name1";
        String tmpName = "test_file_name2";

        // create the template data
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(bytesOut);
        ZipEntry ze1 = new ZipEntry(tmpName);
        out.putNextEntry(ze1);
        out.closeEntry();
        ZipEntry ze2 = new ZipEntry(entryName);
        out.putNextEntry(ze2);
        out.closeEntry();
        out.close();

        // replace the bytes we don't like
        byte[] buf = bytesOut.toByteArray();
        replaceBytes(tmpName.getBytes(), entryName.getBytes(), buf);

        // write the result to a file
        File badZip = File.createTempFile("badzip", "zip");
        badZip.deleteOnExit();
        FileOutputStream outstream = new FileOutputStream(badZip);
        outstream.write(buf);
        outstream.close();

        // see if we can still handle it
        boolean pass;
        try {
            ZipFile bad = new ZipFile(badZip);
            pass = false;
        } catch (ZipException expected) {
            pass = true;
        }

        return pass;
    }
}
