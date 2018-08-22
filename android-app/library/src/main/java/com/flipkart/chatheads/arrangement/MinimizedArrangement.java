package com.flipkart.chatheads.arrangement;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringChain;
import com.facebook.rebound.SpringListener;
import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.ChatHeads;
import com.flipkart.chatheads.ChatHeadsContainer;
import com.flipkart.chatheads.config.ChatHeadConfig;
import com.flipkart.chatheads.interfaces.ChatHeadManager;
import com.flipkart.chatheads.utils.ChatHeadUtils;
import com.flipkart.chatheads.utils.SpringConfigsHolder;

import java.util.List;

public class MinimizedArrangement extends ChatHeadArrangement {

    public static final String BUNDLE_HERO_INDEX_KEY = "hero_index";
    public static final String BUNDLE_HERO_RELATIVE_X_KEY = "hero_relative_x";
    public static final String BUNDLE_HERO_RELATIVE_Y_KEY = "hero_relative_y";
    private static int MAX_VELOCITY_FOR_IDLING;
    private static int MIN_VELOCITY_TO_POSITION_BACK;
    private float DELTA;
    private float currentDelta = 0;
    private int idleStateX = Integer.MIN_VALUE;
    private int idleStateY = Integer.MIN_VALUE;
    private int maxWidth;
    private int maxHeight;
    private boolean hasActivated = false;
    private ChatHeadManager manager;
    private SpringChain horizontalSpringChain;
    private SpringChain verticalSpringChain;
    private ChatHeadsContainer chatHeadsContainer;
    private ChatHead hero;
    private ChatHeads chatHeads;
    private double relativeXPosition = -1;
    private double relativeYPosition = -1;
    private Bundle extras;
    private SpringListener horizontalHeroListener = new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
            currentDelta = (float) (DELTA * (maxWidth / 2 - spring.getCurrentValue()) / (maxWidth / 2));
            if (horizontalSpringChain != null)
                horizontalSpringChain.getControlSpring().setCurrentValue(spring.getCurrentValue());
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            super.onSpringAtRest(spring);
            if (isTransitioning) {
                isTransitioning = false;
            }
        }
    };
    private SpringListener verticalHeroListener = new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (verticalSpringChain != null)
                verticalSpringChain.getControlSpring().setCurrentValue(spring.getCurrentValue());
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            super.onSpringAtRest(spring);
            if (isTransitioning) {
                isTransitioning = false;
            }
        }
    };
    private boolean isTransitioning;

    public MinimizedArrangement(ChatHeadManager manager) {
        this.manager = manager;
        DELTA = ChatHeadUtils.dpToPx(this.manager.getContext(), 5);
    }

    public void setIdleStateX(int idleStateX) {
        this.idleStateX = idleStateX;
    }

    public void setIdleStateY(int idleStateY) {
        this.idleStateY = idleStateY;
    }

    @Override
    public void setContainer(ChatHeadManager container) {
        this.manager = container;
    }

    @Override
    public void onActivate(ChatHeadManager chatHeadManager, Bundle extras, int maxWidth, int maxHeight, boolean animated) {

        isTransitioning = true;
        if (horizontalSpringChain != null || verticalSpringChain != null) {
            onDeactivate(maxWidth, maxHeight);
        }

        MIN_VELOCITY_TO_POSITION_BACK = ChatHeadUtils.dpToPx(chatHeadManager.getDisplayMetrics(), 600);
        MAX_VELOCITY_FOR_IDLING = 0;
        int heroIndex = 0;
        this.extras = extras;
        if (extras != null) {
            heroIndex = extras.getInt(BUNDLE_HERO_INDEX_KEY, -1);
            relativeXPosition = extras.getDouble(BUNDLE_HERO_RELATIVE_X_KEY, -1);
            relativeYPosition = extras.getDouble(BUNDLE_HERO_RELATIVE_Y_KEY, -1);
        }
        chatHeads = chatHeadManager.getChatHeads();
        hero = chatHeads.getByHeroIndex(heroIndex);
        manager.getChatHeadsContainer().reduceWidth();
        manager.getChatHeadsContainer().scrollTo(hero);
        int[] location = new int[2];
        hero.getLocationOnScreen(location);
        int moveToX = Math.max(0, location[0] - manager.getConfig().getInitialHeadWidth());
        manager.getChatHeadsContainer().getHorizontalSpring().setCurrentValue(moveToX);

        chatHeadsContainer = chatHeadManager.getChatHeadsContainer();
        horizontalSpringChain = SpringChain.create();
        verticalSpringChain = SpringChain.create();

        hero.setAlpha(manager.getFloatingViewPreferences().getInactiveAlpha());
        if (relativeXPosition == -1) {
            idleStateX = manager.getFloatingViewPreferences().startRightSide() ? maxWidth : chatHeadManager.getConfig().getInitialPosition().x;
        }
        else {
            idleStateX = (int) (relativeXPosition * maxWidth);
        }
        if (relativeYPosition == -1) {
            idleStateY = chatHeadManager.getConfig().getInitialPosition().y;
        }
        else {
            idleStateY = (int) (relativeYPosition * maxHeight);
        }

        idleStateX = stickToEdgeX(idleStateX, maxWidth);

        if (chatHeadsContainer != null && chatHeadsContainer.getHorizontalSpring() != null && chatHeadsContainer.getVerticalSpring() != null) {
            manager.getWindowManagerContainer().bringToFront(chatHeadsContainer);
            horizontalSpringChain.addSpring(new SimpleSpringListener() {
            });
            verticalSpringChain.addSpring(new SimpleSpringListener() {
            });
            horizontalSpringChain.setControlSpringIndex(0);
            verticalSpringChain.setControlSpringIndex(0);

            chatHeadsContainer.getHorizontalSpring().addListener(horizontalHeroListener);
            chatHeadsContainer.getVerticalSpring().addListener(verticalHeroListener);

            chatHeadsContainer.getHorizontalSpring().setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            if (chatHeadsContainer.getHorizontalSpring().getCurrentValue() == idleStateX) {
                //safety check so that spring animates correctly
                chatHeadsContainer.getHorizontalSpring().setCurrentValue(idleStateX - 1, true);
            }

            if (animated) {
                chatHeadsContainer.getHorizontalSpring().setEndValue(idleStateX);
            }
            else {
                chatHeadsContainer.getHorizontalSpring().setCurrentValue(idleStateX, true);
            }

            chatHeadsContainer.getVerticalSpring().setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            if (chatHeadsContainer.getVerticalSpring().getCurrentValue() == idleStateY) {
                //safety check so that spring animates correctly
                chatHeadsContainer.getVerticalSpring().setCurrentValue(idleStateY - 1, true);
            }
            if (animated) {
                chatHeadsContainer.getVerticalSpring().setEndValue(idleStateY);
            }
            else {
                chatHeadsContainer.getVerticalSpring().setCurrentValue(idleStateY, true);
            }
        }

        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;

        hasActivated = true;
    }

    private int stickToEdgeX(int currentX, int maxWidth) {
        int headWidth = this.manager.getConfig().getInitialHeadWidth();
        int headOffset = headWidth / 4;
        if (maxWidth - currentX < currentX) {
            // this means right edge is closer
            return maxWidth - headWidth + headOffset;
        }
        else {
            return -headOffset;
        }
    }

    @Override
    public void onChatHeadAdded(ChatHead chatHead, boolean animated) {
        if (chatHeadsContainer != null && chatHeadsContainer.getHorizontalSpring() != null && chatHeadsContainer.getVerticalSpring() != null) {
            chatHead.getHorizontalSpring().setCurrentValue(chatHeadsContainer.getHorizontalSpring().getCurrentValue() - currentDelta);
            chatHead.getVerticalSpring().setCurrentValue(chatHeadsContainer.getVerticalSpring().getCurrentValue());
        }

        onActivate(manager, getRetainBundle(), maxWidth, maxHeight, animated);
    }

    @Override
    public void onChatHeadRemoved(ChatHead removed) {
        manager.detachView(removed, manager.getContentView());
        manager.removeView(removed, manager.getContentView());
        if (removed == hero) {
            chatHeadsContainer = null;
        }

        onActivate(manager, null, maxWidth, maxHeight, true);
    }

    @Override
    public void selectChatHead(ChatHead chatHead) {
        //manager.toggleArrangement();
    }

    @Override
    public void onDeactivate(int maxWidth, int maxHeight) {
        hasActivated = false;
        if (chatHeadsContainer != null) {
            chatHeadsContainer.getHorizontalSpring().removeListener(horizontalHeroListener);
            chatHeadsContainer.getVerticalSpring().removeListener(verticalHeroListener);
        }
        if (horizontalSpringChain != null) {
            List<Spring> allSprings = horizontalSpringChain.getAllSprings();
            for (Spring spring : allSprings) {
                spring.destroy();
            }
        }
        if (verticalSpringChain != null) {
            List<Spring> allSprings = verticalSpringChain.getAllSprings();
            for (Spring spring : allSprings) {
                spring.destroy();
            }
        }

        horizontalSpringChain = null;
        verticalSpringChain = null;
    }


    @Override
    public boolean handleTouchUp(ChatHeadsContainer chatHeadsContainer, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {
        settleToClosest(chatHeadsContainer, xVelocity, yVelocity);

        if (!wasDragging) {
            boolean handled = manager.onItemSelected(chatHeadsContainer);
            if (!handled) {
                deactivate();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean handleChatHeadTouchUp(ChatHead chatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {
        // settleToClosest(chatHead, xVelocity, yVelocity);

        if (!wasDragging) {
            boolean handled = manager.onChatHeadSelected(chatHead);
            if (!handled) {
                deactivate();
                return false;
            }
        }
        return true;
    }

    private void settleToClosest(ChatHeadsContainer activeChatHead, int xVelocity, int yVelocity) {
        // snap to side
        Spring activeHorizontalSpring = activeChatHead.getHorizontalSpring();
        Spring activeVerticalSpring = activeChatHead.getVerticalSpring();
        if (activeChatHead.getState() == ChatHeadsContainer.State.FREE) {
            if (Math.abs(xVelocity) < ChatHeadUtils.dpToPx(manager.getDisplayMetrics(), 50)) {
                if (activeHorizontalSpring.getCurrentValue() < (maxWidth - activeHorizontalSpring.getCurrentValue())) {
                    xVelocity = -1;
                }
                else {
                    xVelocity = 1;
                }
            }
            if (xVelocity < 0) {
                int newVelocity = (int) (-activeHorizontalSpring.getCurrentValue() * SpringConfigsHolder.DRAGGING.friction);
                if (xVelocity > newVelocity)
                    xVelocity = (newVelocity);

            }
            else if (xVelocity > 0) {
                int newVelocity = (int) ((maxWidth - activeHorizontalSpring.getCurrentValue() - manager.getConfig().getHeadWidth()) * SpringConfigsHolder.DRAGGING.friction);
                if (newVelocity > xVelocity)
                    xVelocity = (newVelocity);
            }
        }
        if (Math.abs(xVelocity) <= 1) {
            // this is a hack. If both velocities are 0, onSprintUpdate is not called and the chat head remains whereever it is
            // so we give a a negligible velocity to artificially fire onSpringUpdate
            if (xVelocity < 0)
                xVelocity = -1;
            else
                xVelocity = 1;
        }

        if (yVelocity == 0)
            yVelocity = 1;

        activeHorizontalSpring.setVelocity(xVelocity);
        activeVerticalSpring.setVelocity(yVelocity);
    }

    private void deactivate() {
        Bundle bundle = getBundleWithHero();
        manager.setArrangement(MaximizedArrangement.class, bundle);
    }

    @NonNull
    private Bundle getBundleWithHero() {
        return getBundle(chatHeads.getHeroIndex(hero));
    }

    private Bundle getBundle(int heroIndex) {
        if (chatHeadsContainer != null && chatHeadsContainer.getHorizontalSpring() != null && chatHeadsContainer.getVerticalSpring() != null) {
            relativeXPosition = chatHeadsContainer.getHorizontalSpring().getCurrentValue() * 1.0 / maxWidth;
            relativeYPosition = chatHeadsContainer.getVerticalSpring().getCurrentValue() * 1.0 / maxHeight;
        }

        Bundle bundle = extras;
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putInt(MaximizedArrangement.BUNDLE_HERO_INDEX_KEY, heroIndex);
        bundle.putDouble(MinimizedArrangement.BUNDLE_HERO_RELATIVE_X_KEY, relativeXPosition);
        bundle.putDouble(MinimizedArrangement.BUNDLE_HERO_RELATIVE_Y_KEY, relativeYPosition);
        return bundle;
    }

    @Override
    public void onConfigChanged(ChatHeadConfig newConfig) {

    }

    @Override
    public Bundle getRetainBundle() {
        return getBundleWithHero();
    }

    @Override
    public boolean canDrag(ChatHeadsContainer chatHead) {
        return true; //all chat heads are draggable
    }

    @Override
    public void onSpringUpdate(ChatHeadsContainer chatHeadsContainer, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity) {
        /** This method does a bounds Check **/
        double xVelocity = activeHorizontalSpring.getVelocity();
        double yVelocity = activeVerticalSpring.getVelocity();
        if (!isDragging && Math.abs(totalVelocity) < MIN_VELOCITY_TO_POSITION_BACK) {

            if (Math.abs(totalVelocity) < MAX_VELOCITY_FOR_IDLING && chatHeadsContainer.getState() == ChatHeadsContainer.State.FREE && hasActivated) {
                setIdleStateX((int) activeHorizontalSpring.getCurrentValue());
                setIdleStateY((int) activeVerticalSpring.getCurrentValue());
            }
            if (spring == activeHorizontalSpring) {
                double xPosition = activeHorizontalSpring.getCurrentValue();
                int headWidth = manager.getConfig().getInitialHeadWidth();
                int headOffset = headWidth / 4;
                if (xPosition + headWidth > maxWidth - 5 && activeHorizontalSpring.getVelocity() > 0) {
                    //outside the right bound
                    //System.out.println("outside the right bound !! xPosition = " + xPosition);
                    int newPos = maxWidth - headWidth + headOffset;//manager.getConfig().getHeadWidth();
                    activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeHorizontalSpring.setEndValue(newPos);
                }
                else if (xPosition < 5 && activeHorizontalSpring.getVelocity() < 0) {
                    //outside the left bound
                    //System.out.println("outside the left bound !! xPosition = " + xPosition);
                    activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeHorizontalSpring.setEndValue(-headOffset);
                }
                else {
                    //within bound
                }
            }
            else if (spring == activeVerticalSpring) {
                double yPosition = activeVerticalSpring.getCurrentValue();
                if (yPosition + manager.getConfig().getHeadWidth() > maxHeight && activeVerticalSpring.getVelocity() > 0) {
                    //outside the bottom bound
                    //System.out.println("outside the bottom bound !! yPosition = " + yPosition);

                    activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeVerticalSpring.setEndValue(maxHeight - manager.getConfig().getInitialHeadWidth());
                }
                else if (yPosition < 0 && activeVerticalSpring.getVelocity() < 0) {
                    //outside the top bound
                    //System.out.println("outside the top bound !! yPosition = " + yPosition);

                    activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeVerticalSpring.setEndValue(0);
                }
                else {
                    //within bound
                }

            }
        }
    }

    @Override
    public void bringToFront(ChatHead chatHead) {
        Bundle b = getBundle(chatHeads.getHeroIndex(chatHead));
        onActivate(manager, b, manager.getMaxWidth(), manager.getMaxHeight(), true);
    }

    @Override
    public void onReloadFragment(ChatHead chatHead) {
        // nothing to do
    }
}
