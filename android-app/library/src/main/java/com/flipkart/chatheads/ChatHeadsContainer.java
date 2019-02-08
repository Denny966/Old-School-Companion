package com.flipkart.chatheads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.flipkart.chatheads.interfaces.ChatHeadManager;
import com.flipkart.chatheads.utils.SpringConfigsHolder;

public class ChatHeadsContainer extends LinearLayout implements SpringListener {
    private Context context;
    private final int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    private ChatHeadManager manager;
    private SpringSystem springSystem;
    private State state;
    private float downX = -1;
    private float downY = -1;
    private VelocityTracker velocityTracker;
    private boolean isDragging;
    private float downTranslationX;
    private float downTranslationY;
    private SpringListener xPositionListener;
    private SpringListener yPositionListener;
    private Spring xPositionSpring;
    private Spring yPositionSpring;
    private Bundle extras;
    private HorizontalScrollView scrollView;
    private LinearLayout chatHeadsHolder;
    private int alignmentMargin;
    private boolean alignLeft;

    public ChatHeadsContainer(ChatHeadManager manager, SpringSystem springsHolder, Context context) {
        super(context);
        this.context = context;
        this.manager = manager;
        this.springSystem = springsHolder;
        this.alignmentMargin = manager.getFloatingViewPreferences().getAlignmentMargin();
        this.alignLeft = manager.getFloatingViewPreferences().alignFloatingViewsLeft();
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        scrollView = (HorizontalScrollView) inflate(context, R.layout.chatheads_container, null);
        chatHeadsHolder = scrollView.findViewById(R.id.chatheads_holder);
        addView(scrollView);

        xPositionListener = new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                manager.getWindowManagerContainer().setViewX(ChatHeadsContainer.this, (int) spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                super.onSpringAtRest(spring);
            }
        };
        xPositionSpring = springSystem.createSpring();
        xPositionSpring.addListener(xPositionListener);
        xPositionSpring.addListener(this);

        yPositionListener = new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                manager.getWindowManagerContainer().setViewY(ChatHeadsContainer.this, (int) spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                super.onSpringAtRest(spring);
            }
        };
        yPositionSpring = springSystem.createSpring();
        yPositionSpring.addListener(yPositionListener);
        yPositionSpring.addListener(this);
    }

    public void addChatHead(View view, ViewGroup.LayoutParams params) {
        chatHeadsHolder.addView(view, params);
    }

    public void removeChatHead(View view) {
        chatHeadsHolder.removeView(view);
    }

    public void hide() {
        setVisibility(GONE);
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public boolean isHidden() {
        return getVisibility() == GONE;
    }

    public Bundle getExtras() {
        return extras;
    }

    public void setExtras(Bundle extras) {
        this.extras = extras;
    }

    public Spring getHorizontalSpring() {
        return xPositionSpring;
    }

    public Spring getVerticalSpring() {
        return yPositionSpring;
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        if (xPositionSpring != null && yPositionSpring != null) {
            Spring activeHorizontalSpring = xPositionSpring;
            Spring activeVerticalSpring = yPositionSpring;
            if (spring != activeHorizontalSpring && spring != activeVerticalSpring)
                return;
            int totalVelocity = (int) Math.hypot(activeHorizontalSpring.getVelocity(), activeVerticalSpring.getVelocity());
            if (manager.getActiveArrangement() != null)
                manager.getActiveArrangement().onSpringUpdate(this, isDragging, manager.getMaxWidth(), manager.getMaxHeight(), spring, activeHorizontalSpring, activeVerticalSpring, totalVelocity);
        }
    }

    @Override
    public void onSpringAtRest(Spring spring) {
        if (manager.getListener() != null)
            manager.getListener().onChatHeadAnimateEnd(this);
    }

    @Override
    public void onSpringActivate(Spring spring) {
        if (manager.getListener() != null)
            manager.getListener().onChatHeadAnimateStart(this);
    }

    @Override
    public void onSpringEndStateChange(Spring spring) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (xPositionSpring == null || yPositionSpring == null)
            return true;

        //Chathead view will set the correct active springs on touch
        Spring activeHorizontalSpring = xPositionSpring;
        Spring activeVerticalSpring = yPositionSpring;

        int action = event.getAction();
        float rawX = event.getRawX();
        float rawY = event.getRawY();
        float offsetX = rawX - downX;
        float offsetY = rawY - downY;

        event.offsetLocation(manager.getWindowManagerContainer().getViewX(this), manager.getWindowManagerContainer().getViewY(this));
        if (action == MotionEvent.ACTION_DOWN) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            else {
                velocityTracker.clear();
            }
            activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            setState(State.FREE);
            downX = rawX;
            downY = rawY;
            downTranslationX = (float) activeHorizontalSpring.getCurrentValue();
            downTranslationY = (float) activeVerticalSpring.getCurrentValue();

            activeHorizontalSpring.setAtRest();
            activeVerticalSpring.setAtRest();
            velocityTracker.addMovement(event);
        }
        else if (action == MotionEvent.ACTION_MOVE) {
            if (Math.hypot(offsetX, offsetY) > touchSlop) {
                isDragging = true;
            }
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.addMovement(event);
            if (isDragging) {
                if (manager.getActiveArrangement().canDrag(this)) {
                    setState(State.FREE);
                    activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.DRAGGING);
                    activeVerticalSpring.setSpringConfig(SpringConfigsHolder.DRAGGING);
                    activeHorizontalSpring.setCurrentValue(downTranslationX + offsetX);
                    activeVerticalSpring.setCurrentValue(downTranslationY + offsetY);
                    velocityTracker.computeCurrentVelocity(1000);
                }
            }
        }
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            boolean wasDragging = isDragging;
            activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.DRAGGING);
            activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.DRAGGING);
            isDragging = false;
            int xVelocity = (int) velocityTracker.getXVelocity();
            int yVelocity = (int) velocityTracker.getYVelocity();
            velocityTracker.recycle();
            velocityTracker = null;
            if (xPositionSpring != null && yPositionSpring != null) {
                manager.getActiveArrangement().handleTouchUp(this, xVelocity, yVelocity, activeHorizontalSpring, activeVerticalSpring, wasDragging);
            }
        }

        return true;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void reduceWidth() {
        setPadding(0, getPaddingTop(), 0, getPaddingBottom());
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = manager.getConfig().getInitialHeadWidth();
        setLayoutParams(params);
    }

    public void restoreWidth() {
        setPadding(alignLeft ? alignmentMargin : getPaddingLeft(), getPaddingTop(), !alignLeft ? alignmentMargin : getPaddingRight(), getPaddingBottom());
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        setLayoutParams(params);
    }

    public void scrollTo(final ChatHead currentChatHead) {
        scrollView.post(new Runnable() {

            @Override
            public void run() {
                int scrollTo = 0;
                final int count = chatHeadsHolder.getChildCount();
                for (int i = 0; i < count; i++) {
                    final View child = chatHeadsHolder.getChildAt(i);
                    if (child != currentChatHead) {
                        scrollTo += manager.getConfig().getInitialHeadWidth();
                    }
                    else {
                        break;
                    }
                }
                scrollView.scrollTo(scrollTo, 0);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        onTouchEvent(ev);
        return false;
    }

    public enum State {
        FREE, CAPTURED
    }
}
