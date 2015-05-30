package jp.thotta.android.wifipasswordreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by thotta on 15/05/09.
 */

public class TrimmingRectangleView extends View {
    class Position {
        public float x;
        public float y;
    }

    private static final String TAG = "TrimmingRectangleView";
    private Paint paint;
    private Paint paintBitmap;
    private Position start = new Position();
    private Position end = new Position();
    private Bitmap originalBitmap;
    private Bitmap currentBitmap;
    private FrameLayout parentLayout;
    private boolean isTouching = false;

    public TrimmingRectangleView(Context context) {
        super(context);
        init();
    }

    public TrimmingRectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrimmingRectangleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void initialize(FrameLayout parentLayout, Bitmap originalBitmap) {
        this.parentLayout = parentLayout;
        this.originalBitmap = originalBitmap;
        this.currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, false);
    }

    public void backToOriginalBitmap() {
        this.currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, false);
        invalidate();
    }

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    private void updateViewLayout(Canvas canvas) {
        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
        int bmW = 0; // = currentBitmap.getWidth();
        int bmH = 0; // = currentBitmap.getHeight();
        if(currentBitmap != null) {
            bmW = currentBitmap.getWidth();
            bmH = currentBitmap.getHeight();
        }
        float plW = 0.0f;
        float plH = 0.0f;
        if(parentLayout != null) {
            plW = parentLayout.getWidth();
            plH = parentLayout.getHeight();
        }
        if(bmW > bmH) {
            layoutParams.width = (int)plW;
            layoutParams.height = (int)(plW * ((double)bmH / bmW));
        } else {
            layoutParams.height = (int)plH;
            layoutParams.width = (int)(plH * ((double)bmW / bmH));
        }
        this.setLayoutParams(layoutParams);
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setAlpha(128);
        paint.setAntiAlias(true);

        paintBitmap = new Paint();
        paintBitmap.setFilterBitmap(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        updateViewLayout(canvas);
        if(currentBitmap != null) {
            Rect src = new Rect(0, 0, currentBitmap.getWidth(), currentBitmap.getHeight());
            Rect dst = new Rect(0, 0, this.getWidth(), this.getHeight());
            canvas.drawBitmap(currentBitmap, src, dst, paintBitmap);
        }
        if(isTouching) {
            canvas.drawRect(makeRectangle(), paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.v(TAG, "onActionDown in TrimmingRectangleView");
                isTouching = true;
                start.x = event.getX();
                start.y = event.getY();
                end.x = event.getX();
                end.y = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                isTouching = true;
                end.x = event.getX();
                end.y = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                Log.v(TAG, "onActionUp in TrimmingRectangleView");
                isTouching = false;
                trimCurrentBitmap();
                break;
        }
        invalidate();
        return true;
    }

    private RectF makeRectangle() {
        RectF rectF = new RectF();
        rectF.left = start.x < end.x ? start.x : end.x;
        rectF.right = start.x > end.x ? start.x : end.x;
        rectF.top = start.y < end.y ? start.y : end.y;
        rectF.bottom = start.y > end.y ? start.y : end.y;

        rectF.left = rectF.left > 0.0 ? rectF.left : 0.0f;
        rectF.right = rectF.right > 0.0 ? rectF.right : 0.0f;
        rectF.top = rectF.top > 0.0 ? rectF.top : 0.0f;
        rectF.bottom = rectF.bottom > 0.0 ? rectF.bottom : 0.0f;

        int w = this.getWidth();
        int h = this.getHeight();
        rectF.left = rectF.left < w ? rectF.left : w;
        rectF.right = rectF.right < w ? rectF.right : w;
        rectF.top = rectF.top < h ? rectF.top : h;
        rectF.bottom = rectF.bottom < h ? rectF.bottom : h;
        return rectF;
    }

    private Rect makeBitmapRectangle() {
        Rect bmRect = new Rect();
        RectF viewRect = makeRectangle();
        bmRect.left = (int)(viewRect.left * ((double)currentBitmap.getWidth() / this.getWidth()));
        bmRect.right = (int)(viewRect.right * ((double)currentBitmap.getWidth() / this.getWidth()));
        bmRect.top = (int)(viewRect.top * ((double)currentBitmap.getHeight() / this.getHeight()));
        bmRect.bottom = (int)(viewRect.bottom * ((double)currentBitmap.getHeight() / this.getHeight()));
        return bmRect;
    }

    private void trimCurrentBitmap() {
        Rect r = makeBitmapRectangle();
        if(r.width() > 0 && r.height() > 0) {
            currentBitmap = Bitmap.createBitmap(currentBitmap, r.left, r.top, r.width(), r.height());
        } else {
            Log.v(TAG, "トリミングの面積が0です。");
        }
    }
}
