package com.flipkart.chatheads;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.flipkart.chatheads.config.ChatHeadConfig;
import com.flipkart.chatheads.interfaces.ChatHeadManager;
import com.flipkart.chatheads.utils.ChatHeadUtils;
import com.flipkart.chatheads.utils.SpringConfigsHolder;

import java.io.Serializable;

@SuppressLint("AppCompatCustomView")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ChatHead extends ImageView implements SpringListener {

    private String key;

    public final int CLOSE_ATTRACTION_THRESHOLD = ChatHeadUtils.dpToPx(getContext(), 110);
    private final int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    private final float DELTA = ChatHeadUtils.dpToPx(getContext(), 10);
    private ChatHeadManager manager;
    private SpringSystem springSystem;
    private State state;
    private float downX = -1;
    private float downY = -1;
    private VelocityTracker velocityTracker;
    private boolean isDragging;
    private float downTranslationX;
    private float downTranslationY;
    private int unreadCount = 0;
    private SpringListener xPositionListener;
    private SpringListener yPositionListener;
    private Spring scaleSpring;
    private Spring xPositionSpring;
    private Spring yPositionSpring;
    private Bundle extras;
    private ImageView imageView;
    private boolean isHero;

    private float initialTouchX;
    private float initialTouchY;
    private final int CLICK_DISTANCE_THRESHOLD = 15;

    public ChatHead(Context context) {
        super(context);
        throw new IllegalArgumentException("This constructor cannot be used");
    }

    public ChatHead(Context context, AttributeSet attrs) {
        super(context, attrs);
        throw new IllegalArgumentException("This constructor cannot be used");
    }

    public ChatHead(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        throw new IllegalArgumentException("This constructor cannot be used");
    }

    public ChatHead(String key, ChatHeadManager manager, SpringSystem springsHolder, Context context) {
        super(context);
        this.key = key;
        this.manager = manager;
        this.springSystem = springsHolder;
        init();
    }

    private void init() {
        xPositionListener = new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                manager.getWindowManagerContainer().setViewX(ChatHead.this, (int) spring.getCurrentValue());
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
                manager.getWindowManagerContainer().setViewY(ChatHead.this, (int) spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                super.onSpringAtRest(spring);
            }
        };
        yPositionSpring = springSystem.createSpring();
        yPositionSpring.addListener(yPositionListener);
        yPositionSpring.addListener(this);

        scaleSpring = springSystem.createSpring();
        scaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                double currentValue = spring.getCurrentValue();
                setScaleX((float) currentValue);
                setScaleY((float) currentValue);
            }
        });
        scaleSpring.setCurrentValue(ChatHeadConfig.inactiveSize).setAtRest();
    }

    public boolean isHero() {
        return isHero;
    }

    public void setHero(boolean hero) {
        isHero = hero;
    }

//    public void hide() {
//        setVisibility(GONE);
//    }
//
//    public void show() {
//        setVisibility(VISIBLE);
//    }
//
//    public boolean isHidden() {
//        return getVisibility() == GONE;
//    }

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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getKey() {
        return key;
    }

    public SpringListener getHorizontalPositionListener() {
        return xPositionListener;
    }

    public SpringListener getVerticalPositionListener() {
        return yPositionListener;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);
        if (xPositionSpring == null || yPositionSpring == null)
            return false;
        //Chathead view will set the correct active springs on touch
        Spring activeHorizontalSpring = xPositionSpring;
        Spring activeVerticalSpring = yPositionSpring;

        int action = event.getAction();
        float rawX = event.getRawX();
        float rawY = event.getRawY();
        float offsetX = rawX - downX;
        float offsetY = rawY - downY;
        boolean showCloseButton = false;
        event.offsetLocation(manager.getWindowManagerContainer().getViewX(this), manager.getWindowManagerContainer().getViewY(this));
        if (action == MotionEvent.ACTION_DOWN) {
            initialTouchX = event.getRawX();
            initialTouchY = event.getRawY();
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
            // scaleSpring.setEndValue(1.0f);
            activeHorizontalSpring.setAtRest();
            activeVerticalSpring.setAtRest();
            velocityTracker.addMovement(event);
        }
        else if (action == MotionEvent.ACTION_MOVE) {
            //  if (Math.hypot(offsetX, offsetY) > touchSlop) {
            isDragging = true;
            // }
            velocityTracker.addMovement(event);
        }
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.DRAGGING);
            activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.DRAGGING);
            isDragging = false;
            //scaleSpring.setEndValue(isHero ? 1.0f : ChatHeadConfig.inactiveSize);
            int xVelocity = (int) velocityTracker.getXVelocity();
            int yVelocity = (int) velocityTracker.getYVelocity();
            velocityTracker.recycle();
            velocityTracker = null;
            double distance = Math.hypot(offsetX, offsetY);// ChatHeadUtils.getDistance(initialTouchX, event.getRawX(), initialTouchY, event.getRawY());
            boolean wasDragging = distance > CLICK_DISTANCE_THRESHOLD;
            if (!wasDragging && xPositionSpring != null && yPositionSpring != null) {
                manager.getActiveArrangement().handleChatHeadTouchUp(this, xVelocity, yVelocity, activeHorizontalSpring, activeVerticalSpring, wasDragging);
            }
        }

        return true;
    }


    public void onRemove() {
        xPositionSpring.setAtRest();
        xPositionSpring.removeAllListeners();
        xPositionSpring.destroy();
        xPositionSpring = null;
        yPositionSpring.setAtRest();
        yPositionSpring.removeAllListeners();
        yPositionSpring.destroy();
        yPositionSpring = null;
        scaleSpring.setAtRest();
        scaleSpring.removeAllListeners();
        scaleSpring.destroy();
        scaleSpring = null;
    }

    public void setImageDrawable(Drawable chatHeadDrawable) {
        super.setImageDrawable(chatHeadDrawable);
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        if (xPositionSpring != null && yPositionSpring != null) {
            Spring activeHorizontalSpring = xPositionSpring;
            Spring activeVerticalSpring = yPositionSpring;
            if (spring != activeHorizontalSpring && spring != activeVerticalSpring)
                return;
            int totalVelocity = (int) Math.hypot(activeHorizontalSpring.getVelocity(), activeVerticalSpring.getVelocity());
            if (manager.getActiveArrangement() != null) {
                // manager.getActiveArrangement().onSpringUpdate(this, isDragging, manager.getMaxWidth(), manager.getMaxHeight(), spring, activeHorizontalSpring, activeVerticalSpring, totalVelocity);
            }
        }
    }

    @Override
    public void onSpringAtRest(Spring spring) {

    }

    @Override
    public void onSpringActivate(Spring spring) {

    }

    @Override
    public void onSpringEndStateChange(Spring spring) {

    }

    public void setInactive(boolean inactive) {
        scaleSpring.setEndValue(inactive ? ChatHeadConfig.inactiveSize : 1.0f);
    }

    public enum State {
        FREE, CAPTURED
    }
}

