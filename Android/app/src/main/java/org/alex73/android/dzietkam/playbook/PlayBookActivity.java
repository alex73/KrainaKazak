package org.alex73.android.dzietkam.playbook;

import java.io.File;
import java.io.IOException;

import org.alex73.android.dzietkam.CatalogLoader;
import org.alex73.android.dzietkam.Logger;
import org.alex73.android.dzietkam.R;
import org.alex73.android.dzietkam.catalog.Catalog;
import org.alex73.android.dzietkam.catalog.Item;
import org.alex73.android.dzietkam.ui.AnalyticsApplication;
import org.alex73.android.dzietkam.util.EmptyFragmentStatePagerAdapter;
import org.alex73.android.dzietkam.util.PackFileWrapper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class PlayBookActivity extends AppCompatActivity {// FragmentActivity {
    private final Logger log = new Logger(getClass());

    AnalyticsApplication application;

    private String itemPath;
    private Item item;

    ViewPager flipper;
    View buttons;
    RadioButton rbManual;

    MediaPlayer mMediaPlayer;

    boolean playerPaused;
    boolean manualMode;
    int pagesCount;

    BookLoader pl;
    PackFileWrapper packFile;
    PackFileWrapper.FileObjectDataSource packFileDataSource;

    @SuppressLint("NewApi")
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (Build.VERSION.SDK_INT >= 11)
            return super.onCreateView(parent, name, context, attrs);
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.v(">> onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_book);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideNavigationBar();

        application = (AnalyticsApplication) getApplication();
        itemPath = getIntent().getData().getPath();
        item = application.catalog.getItem(itemPath);

        application.analytics().showScreen(item);

        File f = CatalogLoader.getItemDownloaded(item);
        try {
            packFile = new PackFileWrapper(f);
        } catch (Exception ex) {
            log.e("Error read pack file", ex);
        }
        pl = new BookLoader(packFile);

        buttons = findViewById(R.id.pauseLayer);
        buttons.setVisibility(View.INVISIBLE);
        buttons.setOnTouchListener(mDisableUnderlyingTouch);

        findViewById(R.id.buttonPlayPause).setOnClickListener(mStartClickListener);
        findViewById(R.id.buttonClose).setOnClickListener(mExitClickListener);
        rbManual = (RadioButton) findViewById(R.id.rbManual);

        flipper = (ViewPager) findViewById(R.id.flipper);
       
        flipper.addOnPageChangeListener(pageChangeListener);

        flipper.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                    mOnClickListener.onClick(null);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        log.v(">> onStart");
        super.onStart();
        askQuestion(this, packFile.getSettings().getString("question"),
                packFile.getSettings().getString("answer"), new Runnable() {
                    @Override
                    public void run() {
                        setupPlayer();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                flipper.setAdapter(new PlayPagesAdapter(getSupportFragmentManager()));
                                pageChangeListener.onPageSelected(flipper.getCurrentItem());
                            }
                        });

                        CatalogLoader.setItemViewed(PlayBookActivity.this, item, true);
                    }
                });
        log.v("<< onStart");
    }

    @Override
    protected void onStop() {
        log.v(">> onStop");

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (packFileDataSource != null) {
            packFileDataSource.close();
            packFileDataSource = null;
        }
        flipper.setAdapter(new EmptyFragmentStatePagerAdapter(getSupportFragmentManager()));
        System.gc();
        super.onStop();
        log.v("<< onStop");
    }

    /**
     * Ask question before show book.
     */
    private void askQuestion(final Context context, final String question, final String answer,
            final Runnable run) {
        if (question == null || answer == null) {
            run.run();
            return;
        }
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.ask_question_dialog, null);
        ((TextView)dialoglayout.findViewById(R.id.askQuestion)).setText(question);

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setView(dialoglayout);
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText input = (EditText) ((AlertDialog) dialog).findViewById(R.id.askAnswer);
                Editable value = input.getText();
                String out = value.toString();
                if (out.equals(answer)) {
                    run.run();
                } else {
                    finish();
                }
            }
        });
        alert.show();
    }

    @SuppressLint("NewApi")
    private void hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= 11) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    protected void onPause() {
        log.v(">> onPause");
        super.onPause();

        if (mMediaPlayer != null && !playerPaused) {
            mMediaPlayer.pause();
        }
        log.v("<< onPause");
    }

    protected void onResume() {
        log.v(">> onResume");
        super.onResume();
        if (mMediaPlayer != null && mMediaPlayer.getCurrentPosition() > 0)
            if (!playerPaused) {
                mMediaPlayer.start();
            }

        log.v("<< onResume");
    }

    void setupPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.reset();
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);

        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // pause
            if (playerPaused) {

            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                mMediaPlayer.pause();
                playerPaused = true;
                buttons.startAnimation(AnimationUtils.loadAnimation(PlayBookActivity.this, R.anim.show));
                flipper.startAnimation(AnimationUtils.loadAnimation(PlayBookActivity.this, R.anim.half_hide));
                buttons.setVisibility(View.VISIBLE);
                flipper.setEnabled(false);
                flipper.setFocusable(false);
                for (int id : new int[]{R.id.buttonPlayPause, R.id.buttonClose}) {
                    findViewById(id).setFocusable(true);
                    findViewById(id).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View view, boolean b) {
                            view.setBackgroundColor(b ? 0x80808080 : 0x00000000);
                        }
                    });
                }
                findViewById(R.id.buttonPlayPause).requestFocus();
            }
        }
    };
    OnClickListener mStartClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            hideNavigationBar();
            boolean wasManualMode = manualMode;
            manualMode = rbManual.isChecked();
            if (!manualMode) {
                if (wasManualMode) {
                    // auto play just enabled - need to play audio
                    pageChangeListener.onPageSelected(flipper.getCurrentItem());
                } else {
                    // continue audio play
                    mMediaPlayer.start();
                }
            }
            playerPaused = false;
            flipper.setEnabled(true);
            flipper.setFocusable(true);
            buttons.startAnimation(AnimationUtils.loadAnimation(PlayBookActivity.this, R.anim.hide));
            flipper.startAnimation(AnimationUtils.loadAnimation(PlayBookActivity.this, R.anim.half_show));
            buttons.setVisibility(View.INVISIBLE);
        }
    };
    OnClickListener mExitClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
    OnTouchListener mDisableUnderlyingTouch = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };

    MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mMediaPlayer.start();
        }
    };

    MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            log.v("MediaPlayer.onError " + what + " " + extra);
            return true;
        }
    };

    MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            log.v(">> onCompletion");
            mMediaPlayer.reset();
            if (flipper.getCurrentItem() + 1 >= pagesCount) {
                PlayBookActivity.this.finish();
            }
            flipper.setCurrentItem(flipper.getCurrentItem() + 1);
            hideNavigationBar();
            log.v("<< onCompletion");
        }
    };

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int index) {
            log.v(">> onPageSelected " + index);
            mMediaPlayer.reset();
            if (packFileDataSource != null) {
                packFileDataSource.close();
            }
            playerPaused = false;
            if (!manualMode) {
                try {
                    packFileDataSource = pl.getAudioFile(index + 1);
                    if (packFileDataSource != null) {
                        packFileDataSource.apply(mMediaPlayer);
                        mMediaPlayer.prepareAsync();
                    }
                } catch (IOException ex) {
                }
                hideNavigationBar();
            }
        }
    };

    private class PlayPagesAdapter extends FragmentStatePagerAdapter {
        public PlayPagesAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {
            return ImageFragment.newInstance(index);
        }

        @Override
        public int getCount() {
            pagesCount = pl.getPagesCount();
            return pagesCount;
        }
    }
}
