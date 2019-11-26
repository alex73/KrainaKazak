package org.alex73.android.dzietkam.play.youtube;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.alex73.android.dzietkam.CatalogLoader;
import org.alex73.android.dzietkam.R;
import org.alex73.android.dzietkam.catalog.Item;
import org.alex73.android.dzietkam.ui.AnalyticsApplication;


public class PlayYoutubeActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {
    public static final String YOUTUBE_API_KEY_ENCODED = "QUl6YVN5Q3JHbS1KSE56cnJxSHMxZWdNTU4wVFA2WmpfLXBPa3ZZ";
    private static final int RECOVERY_REQUEST = 1;

    private String itemPath;
    private Item item;
    private YouTubePlayerView youTubeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_youtube);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();

        itemPath = getIntent().getData().getPath();
        item = application.catalog.getItem(itemPath);

        application.showScreen(item);

        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        String key = new String(Base64.decode(YOUTUBE_API_KEY_ENCODED, 0));
        youTubeView.initialize(key, this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            player.setFullscreen(true);
            player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                public void onLoading() {
                }

                public void onLoaded(String var1) {
                }

                public void onAdStarted() {
                }

                public void onVideoStarted() {
                }

                public void onVideoEnded() {
                    PlayYoutubeActivity.this.finish();
                }

                public void onError(YouTubePlayer.ErrorReason var1) {
                }
            });
            player.loadVideo(item.id);
            CatalogLoader.setItemViewed(this, item, true);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
        String error = "Памылка доступу да YouTube: " + errorReason.toString();
        for (int i = 0; i < 3; i++) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        }
        PlayYoutubeActivity.this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            String key = new String(Base64.decode(YOUTUBE_API_KEY_ENCODED, 0));
            youTubeView.initialize(key, this);
        }
    }
}
