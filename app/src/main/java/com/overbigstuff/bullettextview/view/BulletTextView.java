package com.overbigstuff.bullettextview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.overbigstuff.bullettextview.R;

import java.lang.ref.WeakReference;

public class BulletTextView extends TextView {
    private static final float TITLE_RELATIVE_SIZE = 1.1f;
    private float mTitleRelativeSize = TITLE_RELATIVE_SIZE;
    private int mCurrentLineNumber = 0;

    SpannableStringBuilder mStringBuilder = new SpannableStringBuilder();
    private final int mBulletRes = R.drawable.bullet;

    public BulletTextView(Context context) {
        super(context);
    }

    public BulletTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BulletTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BulletTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BulletTextView,
                0, 0);
        try {
            mTitleRelativeSize = a.getFloat(R.styleable.BulletTextView_titleSizeMultiplier, 1);
        } finally {
            a.recycle();
        }
    }

    public void addLine(String line) {
        if (mCurrentLineNumber != 0) {
            mStringBuilder.append("\n");
        }
        int bulletIndex = mStringBuilder.length();
        mStringBuilder.append("  ").append(line);
        CenteredImageSpan centeredImageSpan = new CenteredImageSpan(getContext(), mBulletRes, this);
        centeredImageSpan.setLineNumber(mCurrentLineNumber);
        mStringBuilder.setSpan(centeredImageSpan, bulletIndex, bulletIndex + 1, 0);
        mCurrentLineNumber++;
    }

    public void addTitleLine(String line) {
        addTitleLine(line, -1);
    }

    public void addTitleLine(String line, float relativeSize) {
        if (mCurrentLineNumber != 0) {
            mStringBuilder.append("\n");
        }
        int startIndex = mStringBuilder.length();
        mStringBuilder.append(line);
        mStringBuilder.setSpan(new RelativeSizeSpan(relativeSize > 0 ? relativeSize : mTitleRelativeSize), startIndex, mStringBuilder.length(), 0);
        mCurrentLineNumber++;

    }

    public void buildBulletList() {
        setText(mStringBuilder);
        mStringBuilder.clear();
        mStringBuilder.clearSpans();
    }

    class CenteredImageSpan extends ImageSpan {
        private WeakReference<Drawable> mDrawableRef;
        private TextView mTextView;
        private int mLineNumber;
        private Rect mTempRect = new Rect();

        public CenteredImageSpan(Context context, final int drawableRes, TextView textView) {
            super(context, drawableRes);
            this.mTextView = textView;
        }

        public void setLineNumber(int lineNumber) {
            mLineNumber = lineNumber;
        }

        @Override
        public int getSize(Paint paint, CharSequence text,
                           int start, int end,
                           Paint.FontMetricsInt fm) {
            Drawable d = getCachedDrawable();
            Rect rect = d.getBounds();

            if (fm != null) {
                Paint.FontMetricsInt pfm = paint.getFontMetricsInt();
                // keep it the same as paint's fm
                fm.ascent = pfm.ascent;
                fm.descent = pfm.descent;
                fm.top = pfm.top;
                fm.bottom = pfm.bottom;
            }

            return rect.right;
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text,
                         int start, int end, float x,
                         int top, int y, int bottom, @NonNull Paint paint) {
            Drawable b = getCachedDrawable();
            canvas.save();

            int drawableHeight = b.getIntrinsicHeight();
            int fontAscent = paint.getFontMetricsInt().ascent;
            int fontDescent = paint.getFontMetricsInt().descent;

            mTextView.getLineBounds(mLineNumber, mTempRect);
            int lineHeight = mTempRect.bottom - mTempRect.top;
            int transY = (int) (bottom - lineHeight - fontAscent - (mLineNumber == 0 ? fontDescent / 2f : fontDescent) + drawableHeight / 2f - b.getBounds().bottom);

            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }

        // Redefined locally because it is a private member from DynamicDrawableSpan
        private Drawable getCachedDrawable() {
            WeakReference<Drawable> wr = mDrawableRef;
            Drawable d = null;

            if (wr != null)
                d = wr.get();

            if (d == null) {
                d = getDrawable();
                mDrawableRef = new WeakReference<>(d);
            }

            return d;
        }
    }

}