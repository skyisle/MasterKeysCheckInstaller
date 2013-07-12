package com.alanjeon.blueboxcheck;

import android.net.Uri;
import android.os.AsyncTask;

import java.io.FileNotFoundException;

/**
 * Created by skyisle on 13. 7. 12..
 */
public class BlueBoxCheckTask extends AsyncTask<Uri, Void, Boolean> {
    public interface OnCheckResult {

        public void OnCheckResult(boolean passed);
    }

    private OnCheckResult listener;

    public BlueBoxCheckTask(OnCheckResult listener) {
        super();
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Uri... params) {
        CheckBlueBoxBug checker = new CheckBlueBoxBug();
        try {
            return checker.hasVulnerability(params[0]);
        } catch (FileNotFoundException e) {
        }
        return Boolean.FALSE;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        listener.OnCheckResult(aBoolean);

    }

    public void onStop() {
        if (getStatus() != AsyncTask.Status.FINISHED) {
            cancel(true);
        }
    }
}
