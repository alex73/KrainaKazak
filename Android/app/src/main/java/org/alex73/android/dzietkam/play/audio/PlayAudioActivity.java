package org.alex73.android.dzietkam.play.audio;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.alex73.android.dzietkam.R;
import org.alex73.android.dzietkam.catalog.Item;
import org.alex73.android.dzietkam.play.audio.service.PlayServiceHelper;
import org.alex73.android.dzietkam.play.audio.service.PlayStatus;
import org.alex73.android.dzietkam.ui.AnalyticsApplication;

public class PlayAudioActivity extends AppCompatActivity {
    TextView textControl;
    TextView textItemTitle;

    private Handler tickHandler = new Handler();
    private Runnable tickRunnable;
    private long tickDelay;

    SetterProgressBar sbSeekSetter;
    SetterProgressBar sbSoundLevelSetter;
    SetterTimeText elapsedSetter, durationSetter;
    SetterText nameSetter, albumSetter, descriptionSetter;
    SetterBook textSetter;
    SetterImage coverSetter;
    SetterImageButton moveBackwardSetter, playSetter, pauseSetter, continueSetter, playListSetter, moveForwardSetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ThemeHelper.setTheme(this, R.style.Playback);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_audio);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        elapsedSetter = new SetterTimeText((TextView) findViewById(R.id.elapsed));
        durationSetter = new SetterTimeText((TextView) findViewById(R.id.duration));
        sbSeekSetter = new SetterProgressBar((SeekBar) findViewById(R.id.seek_bar));
        sbSeekSetter.widget.setOnSeekBarChangeListener(seekChanged);

        sbSoundLevelSetter = new SetterProgressBar((SeekBar) findViewById(R.id.sound_level_bar));
        sbSoundLevelSetter.widget.setOnSeekBarChangeListener(soundLevelChanged);

        nameSetter = new SetterText((TextView) findViewById(R.id.name));
        albumSetter = new SetterText((TextView) findViewById(R.id.album));
        descriptionSetter = new SetterText((TextView) findViewById(R.id.description));

        coverSetter = new SetterImage((ImageView) findViewById(R.id.cover_view));
        textControl = (TextView) findViewById(R.id.text_view);
        textItemTitle = (TextView) findViewById(R.id.name);
        textSetter = new SetterBook((TextView) findViewById(R.id.text_view), findViewById(R.id.text_scroll_view), findViewById(R.id.cover_view), (ImageView) findViewById(R.id.imgText));
        textSetter.textPanel.setVisibility(View.GONE);
        textSetter.switcher.setOnClickListener(textSwitch);


        playSetter = new SetterImageButton((ImageButton) findViewById(R.id.btnPlay));
        playSetter.widget.setOnClickListener(playClickListsner);
        pauseSetter = new SetterImageButton((ImageButton) findViewById(R.id.btnPause));
        pauseSetter.widget.setOnClickListener(pauseClickListsner);
        continueSetter = new SetterImageButton((ImageButton) findViewById(R.id.btnContinue));
        continueSetter.widget.setOnClickListener(contnueClickListener);
        playListSetter = new SetterImageButton((ImageButton) findViewById(R.id.btnPlayList));
        playListSetter.widget.setOnClickListener(playlistClickListsner);
        moveBackwardSetter = new SetterImageButton((ImageButton) findViewById(R.id.btnMoveBackward));
        moveBackwardSetter.widget.setOnClickListener(moveBackwardClickListsner);
        moveForwardSetter = new SetterImageButton((ImageButton) findViewById(R.id.btnMoveForward));
        moveForwardSetter.widget.setOnClickListener(moveForwardClickListsner);

        for (int id : new int[]{R.id.btnPlay, R.id.btnPause, R.id.btnContinue, R.id.btnPlayList, R.id.btnMoveBackward, R.id.btnMoveForward, R.id.imgText}) {
            findViewById(id).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    view.setBackgroundColor(b ? 0x20000000 : 0x00000000);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        tickDelay = 50;
        tickHandler.postDelayed(new Runnable() {
            public void run() {
                PlayStatus s = PlayServiceHelper.getStatus();
                tickDelay = 200;
                showInfo(s);
                tickRunnable = this;
                tickHandler.postDelayed(tickRunnable, tickDelay);
            }
        }, tickDelay);
    }

    @Override
    protected void onStop() {
        tickHandler.removeCallbacks(tickRunnable);

        super.onStop();
    }

    protected void showInfo(PlayStatus status) {
        if (status == null) {
            return;
        }
        if (status.item!=null && status.item.parent!=null) {
            albumSetter.set(status.item.parent.title);
            nameSetter.set(status.item.title);
            descriptionSetter.set(status.item.description);
        }

        coverSetter.set(status.cover);
        textSetter.set(status.text);

        moveBackwardSetter.set(true, status.backwardEnabled);
        playSetter.set(status.paused, true);
        pauseSetter.set(!status.paused, true);
        continueSetter.set(true, status.playContinue);
        playListSetter.set(true, status.playList);
        moveForwardSetter.set(true, status.forwardEnabled);

        textSetter.set(status.text);

        int pos = status.getPosition();
        elapsedSetter.set(pos / 1000);
        durationSetter.set(status.duration / 1000);
        sbSeekSetter.set(pos / 1000, status.duration / 1000);

        sbSoundLevelSetter.set(Math.round(status.volume * 100), 100);
    }

    OnSeekBarChangeListener seekChanged = new OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                PlayServiceHelper.seekTo(PlayAudioActivity.this, progress * 1000);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
    OnSeekBarChangeListener soundLevelChanged = new OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                float v = progress / 100.0f;
                PlayServiceHelper.setVolume(PlayAudioActivity.this, v);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    OnClickListener textSwitch = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (textSetter.textPanel.getVisibility() == View.VISIBLE) {
                textSetter.textPanel.setVisibility(View.GONE);
                textSetter.imagePanel.setVisibility(View.VISIBLE);
                textSetter.switcher.setImageResource(R.drawable.audio_read);
            } else {
                textSetter.imagePanel.setVisibility(View.GONE);
                textSetter.textPanel.setVisibility(View.VISIBLE);
                textSetter.switcher.setImageResource(R.drawable.audio_picture);
            }
        }
    };

    OnClickListener playClickListsner = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlayServiceHelper.resume(PlayAudioActivity.this);
        }
    };
    OnClickListener pauseClickListsner = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlayServiceHelper.pause(PlayAudioActivity.this);
        }
    };
    OnClickListener contnueClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlayStatus s = PlayServiceHelper.getStatus();
            s.playContinue = !continueSetter.enabled;
        }
    };
    OnClickListener playlistClickListsner = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlayStatus s = PlayServiceHelper.getStatus();
            s.playList = !playListSetter.enabled;
        }
    };
    OnClickListener moveBackwardClickListsner = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlayStatus s = PlayServiceHelper.getStatus();
            if (s.backwardEnabled) {
                PlayServiceHelper.playBackward(PlayAudioActivity.this);
            }
        }
    };
    OnClickListener moveForwardClickListsner = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlayStatus s = PlayServiceHelper.getStatus();
            if (s.forwardEnabled) {
                PlayServiceHelper.playForward(PlayAudioActivity.this);
            }
        }
    };

    private static class SetterProgressBar {
        private final SeekBar widget;
        private int v, vmax;

        SetterProgressBar(SeekBar widget) {
            this.widget = widget;
            set(0, 100);

        }

        public void set(int value, int maxvalue) {
            if (v == value && vmax == maxvalue) {
                return;
            }
            vmax = maxvalue;
            v = value;
            widget.setMax(vmax);
            widget.setProgress(v);
        }
    }

    private static class SetterText {
        private final TextView widget;
        private String v;

        SetterText(TextView widget) {
            this.widget = widget;
            set("");
        }

        public void set(String value) {
            if (value == null && v == null) {
                return;
            }
            if (value != null && v != null && value.equals(v)) {
                return;
            }
            v = value;
            widget.setText(v != null ? v : "");
            widget.setVisibility(v != null ? View.VISIBLE : View.GONE);
        }
    }

    private static class SetterTimeText {
        private final StringBuilder mTimeBuilder = new StringBuilder();
        private final TextView widget;
        private int v;

        SetterTimeText(TextView widget) {
            this.widget = widget;
            set(0);
        }

        public void set(int value) {
            if (value == v) {
                return;
            }
            v = value;
            String text = DateUtils.formatElapsedTime(mTimeBuilder, v);
            widget.setText(text);
        }
    }

    private static class SetterBook {
        private final TextView textOutput;
        private final View textPanel;
        private final View imagePanel;
        private final ImageView switcher;
        private String v;

        SetterBook(TextView textOutput, View textPanel, View imagePanel, ImageView switcher) {
            this.textOutput = textOutput;
            this.textPanel = textPanel;
            this.imagePanel = imagePanel;
            this.switcher = switcher;
            set("");
            set(null);
        }

        public void set(String value) {
            if (value == null && v == null) {
                return;
            }
            if (value != null && v != null && value.equals(v)) {
                return;
            }
            v = value;
            if (v != null) {
                textOutput.setText(v);
                switcher.setVisibility(View.VISIBLE);
            } else {
                textOutput.setText("");
                textPanel.setVisibility(View.GONE);
                imagePanel.setVisibility(View.VISIBLE);
                switcher.setVisibility(View.GONE);
            }
        }
    }

    private static class SetterImageButton {
        private final ImageButton widget;
        private boolean visible, enabled;

        SetterImageButton(ImageButton widget) {
            this.widget = widget;
            set(true, true);
        }

        public void set(boolean visible, boolean enabled) {
            if (visible == this.visible && enabled == this.enabled) {
                return;
            }
            this.visible = visible;
            this.enabled = enabled;
            widget.setVisibility(visible ? View.VISIBLE : View.GONE);
            widget.getDrawable().setAlpha(enabled ? 255 : 128);
            widget.invalidate();
        }
    }

    private static class SetterImage {
        private final ImageView widget;
        private Object bitmap;

        SetterImage(ImageView widget) {
            this.widget = widget;
            set(new ColorDrawable());
        }

        public void set(Object b) {
            if (bitmap == b) {
                return;
            }
            bitmap = b;
            if (bitmap instanceof Bitmap) {
                widget.setImageBitmap((Bitmap) bitmap);
            } else {
                widget.setImageDrawable((Drawable) bitmap);
            }
        }
    }
}
