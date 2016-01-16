package me.jimm.popularmovies2;

import android.os.Bundle;

// https://github.com/google/guava/wiki/CachesExplained
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import me.jimm.popularmovies2.presenters.BasePresenter;

/**
 * Created by jimmaior on 1/2/16.
 * https://github.com/google/guava/wiki/CachesExplained
 */
public class PresenterManager {
;
    private static final String KEY_PRESENTER_ID = "presenter_id";
    private final long CACHE_MAX_SIZE = 10;
    private final long CACHE_EXPIRY_VALUE = 30;
    private static PresenterManager mInstance;
    private final AtomicLong mCurrentId;
    private Cache<Long, BasePresenter<?,?>> mPresenterCache;


    private PresenterManager(){
        mCurrentId = new AtomicLong();
        mPresenterCache = CacheBuilder.newBuilder()
                .maximumSize(CACHE_MAX_SIZE)
                .expireAfterWrite(CACHE_EXPIRY_VALUE, TimeUnit.SECONDS)
                .build();
    }

    public static PresenterManager getInstance() {
        if (mInstance != null) {
            // do nothing
        }
        else {
            mInstance = new PresenterManager();
        }
        return mInstance;
    }

    public void savePresenter(BasePresenter<?,?> presenter, Bundle state){
        long presenterId = mCurrentId.incrementAndGet();
        mPresenterCache.put(presenterId, presenter);
        state.putLong(KEY_PRESENTER_ID, presenterId);
    }

    public <Presenter extends BasePresenter<?,?>> Presenter getPresenter(Bundle savedState){
        Long presenterId = savedState.getLong(KEY_PRESENTER_ID);
        Presenter presenter = (Presenter) mPresenterCache.getIfPresent(presenterId);
        mPresenterCache.invalidate(presenterId);
        return presenter;
    }

    public <Presenter extends BasePresenter<?,?>> Presenter restorePresenter(Bundle savedInstanceState) {
        long presenterId = savedInstanceState.getLong(KEY_PRESENTER_ID);
        Presenter presenter = (Presenter) mPresenterCache.getIfPresent(presenterId);
        mPresenterCache.invalidate(presenterId);
        return presenter;
    }

}
