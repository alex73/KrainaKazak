package org.alex73.android.dzietkam.playbook;

import org.alex73.android.dzietkam.Logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ImageFragment extends Fragment {
    private final Logger log = new Logger(getClass());

    private PlayBookActivity parent;
    private int drawedWidth, drawedHeight;

    /**
     * Need to create through Bundle, because constructor myst be without params
     */
    public static ImageFragment newInstance(int index) {
        Bundle args = new Bundle();
        args.putInt("index", index);

        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        parent = (PlayBookActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final int index = getIndex();
        log.e("Create view for index " + index);

//        ImageView view = new ImageView(getContext()) {
//            @Override
//            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//                if (right - left != drawedWidth || bottom - top != drawedHeight) {
//                    drawedWidth = right - left;
//                    drawedHeight = bottom - top;
//                    try {
//                        setImageBitmap(parent.pl.loadPicture(index + 1, drawedWidth, drawedHeight));
//                    } catch (IOException ex) {// TODO
//                    }
//                }
//                super.onLayout(changed, left, top, right, bottom);
//            }
//        };
//        drawedWidth = container.getWidth();
//        drawedHeight = container.getHeight();
//        if (drawedWidth == 0 || drawedHeight == 0) {
//            retrieveScreenSize();
//        }
//        try {
//            view.setImageBitmap(parent.pl.loadPicture(index + 1, drawedWidth, drawedHeight));
//        } catch (IOException ex) {// TODO
//        }
        ImageViewBookPage view=new ImageViewBookPage(getContext());
        view.setContent(parent.pl, index+1);
        view.setOnClickListener(parent.mOnClickListener);
        return view;
    }

    private int getIndex() {
        return getArguments().getInt("index");
    }

    @SuppressLint("NewApi")
    private void retrieveScreenSize() {
        Display d = parent.getWindowManager().getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            d.getSize(size);
            drawedWidth = size.x;
            drawedHeight = size.y;
        } else {
            drawedWidth = d.getWidth();
            drawedHeight = d.getHeight();
        }
    }
}
