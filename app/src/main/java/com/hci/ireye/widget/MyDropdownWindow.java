package com.hci.ireye.widget;

import androidx.annotation.Nullable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hci.ireye.R;

/**
 * related:
 * java: this class
 * layout: layout_my_dropdown_window
 * attrs: MyDropDownWindow
 * colors: colorGrey
 * colors: colorLightGrey
 * styles: HorizontalDivider
 */
public class MyDropdownWindow extends LinearLayout {

    private Context mContext = null;
    private View mWindowView = null;
    private String title = null;
    private Drawable imgRes;

    private View mVDividerBelow;
    private FrameLayout mFlWindowFrame;
    private TextView mTvTitle;
    private ImageView mIvArrow;
    private RelativeLayout mRlBar;
    private ImageView mImg;

    private int mWindowFrameId;
    private int maxWindowHeight;

    private boolean windowShowing;

    private ValueAnimator animator;
    private final int hideWindowDuration = 300;
    private final int showWindowDuration = 300;

    public MyDropdownWindow(Context context) {
        super(context);
        initLayout();
    }


    public MyDropdownWindow(Context context, String title, View view, Drawable img) {
        super(context);
        mContext = context;
        this.title = title;
        mWindowView = view;
        imgRes = img;

        initLayout();
    }

    public MyDropdownWindow(Context context, String title, int id, Drawable img) {
        super(context);
        mContext = context;
        this.title = title;
        mWindowView = LayoutInflater.from(mContext).inflate(id, null);
        imgRes = img;

        initLayout();
    }

    public MyDropdownWindow(Context context, String title, Drawable img) {
        super(context);
        mContext = context;
        this.title = title;
        imgRes = img;
        mWindowView = null;
        initLayout();

    }

    public MyDropdownWindow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MyDropdownWindow);

        //get view from view attribute by default; if failed, find view from viewFromRes attribute; if failed again, set to null.
        mWindowView = null;
        if (array.getString(R.styleable.MyDropdownWindow_view) != null) {
            try {
                mWindowView = Class.forName(array.getString(R.styleable.MyDropdownWindow_view)).asSubclass(View.class).getConstructor(Context.class).newInstance(mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mWindowView == null && array.getResourceId(R.styleable.MyDropdownWindow_viewFromRes, 0) != 0) {
            mWindowView = LayoutInflater.from(mContext).inflate(array.getResourceId(R.styleable.MyDropdownWindow_viewFromRes, 0), null);
        }

        title = array.getString(R.styleable.MyDropdownWindow_title);

        imgRes = array.getDrawable(R.styleable.MyDropdownWindow_img);

        initLayout();

        array.recycle();
    }

    //set view at run time
    public void setWindowView(int id) {
        setWindowView(LayoutInflater.from(mContext).inflate(id, null));
    }

    //set view at run time
    public void setWindowView(View view) {
        if (animator != null && animator.isRunning()) {
            return;
        }

        mWindowView = view;
        if (view != null) {
            mIvArrow.setVisibility(VISIBLE);
        } else {
            mIvArrow.setVisibility(GONE);
            return;
        }

        mFlWindowFrame.removeAllViews();
        mFlWindowFrame.addView(mWindowView);
        addListenersToWindow();
    }

    private void addListenersToWindow() {
        //adjust maxWindowHeight when View itself changes its size
        if (mWindowView != null) {
            mWindowView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    //height change caused by animation does not affect maxWindowHeight
                    if (animator != null && !animator.isRunning()) {
                        maxWindowHeight = bottom - top;
                        mFlWindowFrame.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, maxWindowHeight));
                    }
                }
            });
        }

        //adjust maxWindowHeight when view is replaced
        if (mWindowView != null) {
            int widthSpec = MeasureSpec.makeMeasureSpec(mTvTitle.getWidth(), MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            mWindowView.measure(widthSpec, heightSpec);
            maxWindowHeight = mWindowView.getMeasuredHeight();
            mFlWindowFrame.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, maxWindowHeight));
        }
    }

    public View getWindowView() {
        return mWindowView;
    }

    public void setImg(Drawable img) {
        imgRes = img;
        if(imgRes != null) {
            mImg.setVisibility(VISIBLE);
            mImg.setImageDrawable(imgRes);
        } else {
            mImg.setImageDrawable(null);
            mImg.setVisibility(GONE);
        }
    }

    public void setTitle(String title) {
        this.title = title;
        if (this.title != null) {
            mTvTitle.setText(this.title);
        }
    }

    public String getTitle() {
        return title;
    }

    public Drawable getImg() {
        return imgRes;
    }

    private void initLayout() {

        //inflate layout to widget
        LayoutInflater.from(mContext).inflate(R.layout.layout_my_dropdown_window, this);

        mTvTitle = findViewById(R.id.tv_my_dropdown_window_title);
        mVDividerBelow = findViewById(R.id.v_my_dropdown_window_below);
        mIvArrow = findViewById(R.id.iv_my_dropdown_window_arrow);
        mFlWindowFrame = findViewById(R.id.fl_my_dropdown_window_window);
        mRlBar = findViewById(R.id.rl_my_dropdowm_window_bar);
        mImg = findViewById(R.id.iv_my_dropdown_window_img);


        //generate a unique id for the FrameLayout where our Fragment is put into.
        //without this step, id conflict will arise when two or more of MyDropdownWindow widget are put into the same activity's layout
        //resulting in FragmentTransaction unexpectedly putting Fragments into another MyDropdownWindow with the same id.
//        mWindowFrameId = View.generateViewId();
//        mFlWindowFrame.setId(mWindowFrameId);
        //no need? 6/21

        if(mWindowView != null) {
            mFlWindowFrame.addView(mWindowView);
            addListenersToWindow();
        }


        windowShowing = mFlWindowFrame.getVisibility() == VISIBLE;

        if(title != null) {
            mTvTitle.setText(title);
        }

        if(imgRes != null) {
            mImg.setImageDrawable(imgRes);
        } else mImg.setVisibility(GONE);

        if (mWindowView == null) {
            mIvArrow.setVisibility(GONE);
        }

        mRlBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWindowView != null) {
                    if(windowShowing) {
                        hideWindow();
                    } else {
                        showWindow();
                    }
                }
            }
        });
//        Log.d("我", "initLayout: "+(mWindowView.getParent()==null?"null":"not null"));


        getViewTreeObserver().addOnScrollChangedListener(scrollListener);
        getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }

    private void hideWindow() {
        windowShowing = false;
        mVDividerBelow.setVisibility(GONE);
        mIvArrow.setImageResource(R.drawable.ic_down_arrow);

        if(animator != null && animator.isRunning()) {
            animator.cancel();
        }

        animator = ValueAnimator.ofInt(mFlWindowFrame.getHeight(), 0);
        animator.setDuration(hideWindowDuration);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int newHeight = (int)animation.getAnimatedValue();
                mFlWindowFrame.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, newHeight));

                //correct the visibility manually
                //purpose: to keep mFlWindowFrame hidden when updating its height when setting a new Fragment.
                //see setWindowView()
                //warning: cannot be put into onAnimationEnd() as this Animation may not end normally,
                //eg. showWindow() is called before animation ends.
                //in that case, mFlWindowFrame may be shown again at the end of this animation

                if(newHeight == 0) {
                    mFlWindowFrame.setVisibility(GONE);
                }
            }
        });

        //set fragment non-clickable during animation (optional step)
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                setTreeClickable(mWindowView, false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setTreeClickable(mWindowView, true);
            }
        });

        animator.start();
    }

    private void showWindow() {
        windowShowing = true;
        mVDividerBelow.setVisibility(VISIBLE);
        mIvArrow.setImageResource(R.drawable.ic_up_arrow);

        //adjust maxWindowHeight when window is to be shown (in case Fragment changes its size when hidden / statically assigned Fragment is yet to get measured maxWindowHeight)
        int widthSpec = MeasureSpec.makeMeasureSpec(mTvTitle.getWidth(), MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        mFlWindowFrame.measure(widthSpec, heightSpec);
        maxWindowHeight = mFlWindowFrame.getMeasuredHeight();

        if(animator != null && animator.isRunning()) {
            animator.cancel();
        }

        //getHeight() should return 0 in GONE visibility?? but actually does not. Don't know why. Fixed by adding a visibility check.
        animator = ValueAnimator.ofInt(mFlWindowFrame.getVisibility() == GONE ? 0 : mFlWindowFrame.getHeight(), maxWindowHeight);
        animator.setDuration(showWindowDuration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int newHeight = (int)animation.getAnimatedValue();
                mFlWindowFrame.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, newHeight));
            }
        });

        //set view non-clickable during animation (optional step)
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                setTreeClickable(mWindowView, false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setTreeClickable(mWindowView, true);
            }
        });

        mFlWindowFrame.setVisibility(VISIBLE);

        animator.start();
    }

    //set mWindowView with all its children (non-)clickable.
    private void setTreeClickable(View parent, boolean clickable) {
        if(parent != null) {
            parent.setClickable(clickable);
            if(parent instanceof ViewGroup) {
                for(int i = 0; i < ((ViewGroup)parent).getChildCount(); ++i) {
                    setTreeClickable(((ViewGroup)parent).getChildAt(i), clickable);

                }
            }
        }
    }

    //used in OnGlobalLayoutListener and OnScrollChangedListener when bar's position needs to change.
    private void adjustBarPosition() {
        int totalHeight = getHeight();
        int barHeight = mRlBar.getHeight();
        int maxTranslate = totalHeight - barHeight;

        Rect barVisibleRect = new Rect();
        Rect totalVisibleRect = new Rect();
        getGlobalVisibleRect(totalVisibleRect);

        int[] windowPos = new int[2];
        getLocationInWindow(windowPos);

        int translate = 0;
        mRlBar.getGlobalVisibleRect(barVisibleRect);
        if (totalVisibleRect.height() < totalHeight && windowPos[1] < totalVisibleRect.top) {
            translate = totalVisibleRect.top - windowPos[1];
            translate = Math.min(translate, maxTranslate);
        }
        mRlBar.setTranslationY(translate);
    }

    ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            adjustBarPosition();
        }
    };


    ViewTreeObserver.OnScrollChangedListener scrollListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            adjustBarPosition();
        }
    };

}