package org.alex73.android.dzietkam.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alex73.android.dzietkam.CatalogLoader;
import org.alex73.android.dzietkam.Logger;
import org.alex73.android.dzietkam.R;
import org.alex73.android.dzietkam.catalog.Item;
import org.alex73.android.dzietkam.download.DownloadService;
import org.alex73.android.dzietkam.download.DownloadStarter;
import org.alex73.android.dzietkam.play.audio.PlayAudioActivity;
import org.alex73.android.dzietkam.play.audio.service.PlayServiceHelper;
import org.alex73.android.dzietkam.play.text.PlayTextActivity;
import org.alex73.android.dzietkam.play.youtube.PlayYoutubeActivity;
import org.alex73.android.dzietkam.playbook.PlayBookActivity;
import org.alex73.android.dzietkam.widgets.DividerItemDecoration;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ItemsListActivity extends AppCompatActivity {
    private final Logger log = new Logger(getClass());

    AnalyticsApplication application;

    private String itemPath;
    private Item item;

    RecyclerView list;
    private String iconsPrefix;
    private int iconsCount;

    @SuppressLint("NewApi")
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (Build.VERSION.SDK_INT >= 11)
            return super.onCreateView(parent, name, context, attrs);
        return null;
    }

    @Override
    public void onCreate(Bundle icicle) {
        log.v(">> onCreate");
        super.onCreate(icicle);
        setContentView(R.layout.items_list);

        application = (AnalyticsApplication) getApplication();

        itemPath = getIntent().getData().getPath();

        if (itemPath.startsWith("/kalychanki/")) {
            iconsPrefix = "item_kalychanka_";
            iconsCount = 16;
        }else {
            iconsPrefix = "item_";
            iconsCount = 15;
        }

        item = application.catalog.getItem(itemPath);

        initList();

        if (item.cover != null) {
            ((TextView) findViewById(R.id.album_name)).setText(item.title);
            ((TextView) findViewById(R.id.album_details)).setText(item.description);
            int coverId = ItemsListActivity.this.getResources().getIdentifier(item.cover, "drawable",
                    getPackageName());
            ((ImageView) findViewById(R.id.album_cover)).setImageResource(coverId);
            findViewById(R.id.album_download).setOnClickListener(mDownloadAll);
        } else {
            // альбом без загалоўка
            findViewById(R.id.album_header).setVisibility(View.GONE);
            findViewById(R.id.album_separator).setVisibility(View.GONE);
        }

        setAlbumDownload();

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_FINISH_DOWNLOAD);
        bManager.registerReceiver(downloadFinished, intentFilter);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        PlayServiceHelper.finish(this);

        if (list.getAdapter()==null) {
            list.setAdapter(new RVAdapter(item.items));
        }
        list.getAdapter().notifyDataSetChanged();

        list.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                RecyclerView.LayoutManager lm = list.getLayoutManager();
//TODO check
                /*if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        int p = lm.getPosition(v);
                        Item a = items.get(p);
                        swipeLeft(a);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        int p = lm.getPosition(v);
                        Item a = items.get(p);
                        swipeRight(a);
                        return true;
                    }
                }*/

                return false;
            }
        });
    }

    void setAlbumDownload() {
        boolean allDownloaded = CatalogLoader.isItemDownloaded(item.file);
        for (Item it : item.items) {
            if (!CatalogLoader.isItemDownloaded(it.file)) {
                allDownloaded = false;
                break;
            }
        }
        findViewById(R.id.album_download).setVisibility(allDownloaded ? View.GONE : View.VISIBLE);
    }

    private BroadcastReceiver downloadFinished = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setAlbumDownload();
            list.getAdapter().notifyDataSetChanged();
        }
    };

    public void recyclerViewListClicked(View v, int position) {
        Item it = item.items.get(position);
            if (!CatalogLoader.isItemDownloaded(it.file)) {
                List<Item> items = new ArrayList<>();
                items.add(it);
                DownloadStarter.start(ItemsListActivity.this, getApplicationContext(), application, items);
                return;
            }
        startPlay(it);
    }

    OnClickListener mDownloadAll = new OnClickListener() {
        @Override
        public void onClick(View v) {
            List<Item> items = new ArrayList<>();
            if (item.file != null) {
                items.add(item);
            } else {
                for (Item it : item.items) {
                    if (CatalogLoader.isItemDownloaded(it.file)) {
                        continue;
                    }
                    items.add(it);
                }
            }
            if (!items.isEmpty()) {
                DownloadStarter.start(ItemsListActivity.this, getApplicationContext(), application, items);
            }
        }
    };

    void startPlay(final Item item) {
        Intent intent;
        if (item.type==null) {
            intent = new Intent(ItemsListActivity.this, ItemsListActivity.class);
            intent.setData(Uri.parse(itemPath+'/'+item.id));
            startActivity(intent);
            return;
        }
        switch (item.type) {
        case "book":
            intent = new Intent(ItemsListActivity.this, PlayBookActivity.class);
            intent.setData(Uri.parse(itemPath+'/'+item.id));
            startActivity(intent);
            break;
        case "audio":
            PlayServiceHelper.startPlay(ItemsListActivity.this, item);
            intent = new Intent(ItemsListActivity.this, PlayAudioActivity.class);
            startActivity(intent);
            break;
        case "youtube":
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            boolean ask = sharedPref.getBoolean("pref_ask_wifi",true);
            if (ask && DownloadService.isNotWiFi(this)) {
                new AlertDialog.Builder(this).setTitle(R.string.download_youtube_nowifi_title)
                        .setMessage(R.string.download_youtube_nowifi_message)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent(ItemsListActivity.this, PlayYoutubeActivity.class);
                                intent.setData(Uri.parse(itemPath+'/'+item.id));
                                startActivity(intent);
                            }
                        }).setNegativeButton(R.string.no, null).show();
            } else {
                intent = new Intent(ItemsListActivity.this, PlayYoutubeActivity.class);
                intent.setData(Uri.parse(itemPath+'/'+item.id));
                startActivity(intent);
            }
            break;
        case "text":
            intent = new Intent(ItemsListActivity.this, PlayTextActivity.class);
            intent.setData(Uri.parse(itemPath+'/'+item.id));
            startActivity(intent);
            break;
        }
    }

    void initList() {
        list = (RecyclerView) findViewById(R.id.list);
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.addItemDecoration(new DividerItemDecoration(this));
        list.setItemAnimator(new DefaultItemAnimator());

        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(list);
    }

    public class RVAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        List<Item> items;
        private boolean focused;

        RVAdapter(List<Item> items) {
            this.items = items;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_card, viewGroup,
                    false);
            ItemViewHolder pvh = new ItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(ItemViewHolder itemViewHolder, int i) {
            itemViewHolder.title.setText(items.get(i).title);
            itemViewHolder.details.setText(items.get(i).description);
// TODO download mark itemViewHolder.downloadMark.setVisibility(View.GONE);

            int coverId = 0;
            String cover = items.get(i).cover;
            if (cover != null) {
                coverId = ItemsListActivity.this.getResources().getIdentifier(cover, "drawable",
                        getPackageName());
            }
            if (coverId == 0) {
                coverId = ItemsListActivity.this.getResources().getIdentifier(iconsPrefix + (i % iconsCount + 1),
                        "drawable", getPackageName());
            }
            itemViewHolder.cover.setImageResource(coverId);

            final Item item = items.get(i);
            itemViewHolder.downloadMark
                    .setVisibility(CatalogLoader.isItemDownloaded(item.file) ? View.GONE : View.VISIBLE);
            itemViewHolder.viewedMark.setVisibility(CatalogLoader.isItemViewed(ItemsListActivity.this, item) ? View.VISIBLE : View.GONE);
            itemViewHolder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    view.setBackgroundColor(b ? 0x20000000 : 0x00000000);
                }
            });
            if (!focused) {
                focused=true;
                itemViewHolder.itemView.requestFocus();
            }
        }


//        @Override
//        public void onViewRecycled(ItemViewHolder holder) {
//            log.e("detach view "+holder.title.getText());
//            if (holder.cover.getDrawable() instanceof BitmapDrawable) {
//                ((BitmapDrawable) holder.cover.getDrawable()).getBitmap().recycle();
//                holder.cover.setImageDrawable(new ColorDrawable());
//            }
//            if (holder.downloadMark.getDrawable() instanceof BitmapDrawable) {
//                ((BitmapDrawable) holder.downloadMark.getDrawable()).getBitmap().recycle();
//                holder.downloadMark.setImageDrawable(new ColorDrawable());
//            }
//            super.onViewRecycled(holder);
//        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView details;
        ImageView cover;
        ImageView downloadMark;
        ImageView viewedMark;

        ItemViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_name);
            details = (TextView) itemView.findViewById(R.id.item_details);
            cover = (ImageView) itemView.findViewById(R.id.item_cover);
            downloadMark = (ImageView) itemView.findViewById(R.id.item_download_mark);
            viewedMark = (ImageView) itemView.findViewById(R.id.item_viewed_mark);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getLayoutPosition();
                    recyclerViewListClicked(v, pos);
                }
            });
            itemView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    RecyclerView.LayoutManager lm = list.getLayoutManager();
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        int pos = getLayoutPosition();
                        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            swipeLeft(item.items.get(pos));
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            swipeRight(item.items.get(pos));
                            return true;
                        }
                    }
                    return false;
                }
            });

            // TODO below - from album
            title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    view.setBackgroundColor(b ? 0x20000000 : 0x00000000);
                }
            });
            ImageView viewedMark = (ImageView) itemView.findViewById(R.id.item_viewed_mark);
            viewedMark.setVisibility(View.GONE);
        }
    }


    ItemTouchHelper.Callback touchCallback = new ItemTouchHelper.Callback() {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT);
        }

        @Override
        public boolean onMove(RecyclerView arg0, RecyclerView.ViewHolder arg1, RecyclerView.ViewHolder arg2) {
            return false;
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX,
                                float dY, int actionState, boolean isCurrentlyActive) {
            if(isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                float x = (dX > 0 ? itemView.getLeft() : itemView.getRight()) + dX / 2;
                float y = itemView.getTop() + itemView.getHeight() * 4f / 6;
                TextPaint paint = new TextPaint();
                paint.setColor(Color.RED);
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(itemView.getHeight() / 5);
                paint.setTextAlign(Paint.Align.CENTER);
                // рухаецца
                if (dX<0) {
                    //to left
                    CharSequence cc = TextUtils.ellipsize("Непрагледжанае", paint, Math.abs(dX), TextUtils.TruncateAt.END);
                    c.drawText(cc.toString(), x, y, paint);
                }else {
                    // to right
                    CharSequence cc = TextUtils.ellipsize("Выдаліць ?", paint, Math.abs(dX), TextUtils.TruncateAt.END);
                    c.drawText(cc.toString(), x, y, paint);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }else {
                // зроблена
                super.onChildDraw(c, recyclerView, viewHolder, 0, dY, actionState, isCurrentlyActive);
            }
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int p = viewHolder.getAdapterPosition();
            Item sitem=item.items.get(p);
            switch(direction) {
                case ItemTouchHelper.RIGHT:
                    swipeRight(sitem);
                    break;
                case ItemTouchHelper.LEFT:
                    swipeLeft(sitem);
                    break;
            }
        }
    };

    void swipeRight(Item item ) {
        File f = CatalogLoader.getItemDownloaded(item);
        if (f!=null) {
            f.delete();
        }
        if (item.items!=null) {
            for (Item it : item.items) {
                f = CatalogLoader.getItemDownloaded(it);
                if (f != null) {
                    f.delete();
                }
            }
        }
        Toast.makeText(ItemsListActivity.this, "Выдалена", Toast.LENGTH_SHORT).show();
        list.getAdapter().notifyDataSetChanged();
    }
    void swipeLeft(Item item) {
        if (item!=null) {
            CatalogLoader.setItemViewed(ItemsListActivity.this, item, false);
        } else {
            // TODO from album
            for (Item it : item.items) {
                CatalogLoader.setItemViewed(ItemsListActivity.this, it, false);
            }
        }
        Toast.makeText(ItemsListActivity.this, "Прыбралі памету прагляду", Toast.LENGTH_SHORT).show();
        list.getAdapter().notifyDataSetChanged();
    }
}
