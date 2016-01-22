package me.jimm.popularmovies2.models;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

/**
 * Created by jimmaior on 12/10/15.
 */
public class MovieServiceReceiver extends ResultReceiver {

    private static final String TAG = MovieServiceReceiver.class.getSimpleName();

    private Receiver mReceiver;

    public interface Receiver {
        public void onReceiveResponse(int resultCode, Bundle data) ;
    }
    public MovieServiceReceiver(Handler handler) {
        super(handler);
        Log.d(TAG, "MovieDbApiResponseReceiver");
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle data) {
        if (mReceiver != null) {
            mReceiver.onReceiveResponse(resultCode, data);
        }
    }
}

