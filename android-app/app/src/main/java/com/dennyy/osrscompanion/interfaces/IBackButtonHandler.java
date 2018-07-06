package com.dennyy.osrscompanion.interfaces;

public class IBackButtonHandler {
    public interface BackButtonHandlerInterface {
        void addBackClickListener(OnBackClickListener onBackClickListener);

        void removeBackClickListener(OnBackClickListener onBackClickListener);
    }

    public interface OnBackClickListener {
        boolean onBackClick();
    }
}
