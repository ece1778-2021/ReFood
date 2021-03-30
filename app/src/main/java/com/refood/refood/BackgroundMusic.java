package com.refood.refood;

import android.content.Context;
import android.media.MediaPlayer;

public class BackgroundMusic {

    // singleton instance
    private static BackgroundMusic sInstance;

    private Context mContext;
    private MediaPlayer mBGM;
    private MediaPlayer mGameBGM;
    private MediaPlayer mLast;

    private boolean mBGMEnabled;

    private BackgroundMusic(Context context) {
        mContext = context.getApplicationContext();


        mBGM = MediaPlayer.create(mContext, R.raw.refoodv1);
        mBGM.start();
        mBGM.setLooping(true);

        mGameBGM = MediaPlayer.create(mContext, R.raw.refoodv2);

        mLast = null;
        mBGMEnabled = true;
    }

    public static BackgroundMusic getInstance(Context context) {
        if (null == sInstance) {
            synchronized (BackgroundMusic.class) {
                sInstance = new BackgroundMusic(context);
            }
        }
        return sInstance;
    }

    public void switchBGM()
    {
        if (mBGM.isPlaying())
        {
            mBGM.pause();
            mGameBGM.start();
            mGameBGM.setLooping(true);
        }
        else if (mGameBGM.isPlaying())
        {
            mGameBGM.pause();
            mBGM.start();
            mBGM.setLooping(true);
        }
    }

    public void togglBGM()
    {
        mBGMEnabled = !mBGMEnabled;
        if (mBGMEnabled)
        {
            try
            {
                mBGM.prepare();
                mGameBGM.prepare();
            }
            catch (Exception e){}

            mBGM.start();
            mBGM.setLooping(true);
        }
        else
        {
            mBGM.stop();
            mGameBGM.stop();
        }
    }

    public boolean getBGMEnabled()
    {
        return mBGMEnabled;
    }

    public void pause()
    {
        mLast = null;
        if (mBGM.isPlaying())
        {
            mBGM.pause();
            mLast = mBGM;
        }
        else if (mGameBGM.isPlaying())
        {
            mGameBGM.pause();
            mLast = mGameBGM;
        }
    }

    public void start()
    {
        if (mLast != null)
        {
            mLast.start();
            mLast.setLooping(true);
        }
    }
}