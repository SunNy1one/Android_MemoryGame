package com.nusiss.android_game_ca.animators;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Handler;
import android.view.View;

import com.nusiss.android_game_ca.R;

public class CardAnimator {

    private float scale;
    private AnimatorSet frontCardAnimator;

    private AnimatorSet backCardAnimator;

    public CardAnimator(float scale, AnimatorSet frontAnimator, AnimatorSet backAnimator){
        this.scale = scale;
        this.frontCardAnimator = frontAnimator;
        this.backCardAnimator = backAnimator;

    }

    public float getScale() {
        return scale;
    }

    public AnimatorSet getFrontCardAnimator() {
        return frontCardAnimator;
    }

    public AnimatorSet getBackCardAnimator() {
        return backCardAnimator;
    }

    public AnimatorSet getSecondFrontCardAnimator() {
        return frontCardAnimator.clone();
    }

    public AnimatorSet getSecondBackCardAnimator() {
        return backCardAnimator.clone();
    }

    public void flipCard(View frontView, View backView, long delay, FlipAnimationEndCallBack callback){
        if(frontCardAnimator.isRunning() || backCardAnimator.isRunning()){
            AnimatorSet secondFrontAnimator = getSecondFrontCardAnimator();
            AnimatorSet secondBackAnimator = getSecondBackCardAnimator();
            frontView.postDelayed(() -> {
                secondFrontAnimator.setTarget(frontView);
                secondFrontAnimator.start();
            }, delay);
            backView.postDelayed(() -> {
                secondBackAnimator.setTarget(backView);
                secondBackAnimator.start();
            }, delay);
        } else {
            frontView.postDelayed(() -> {
                frontCardAnimator.setTarget(frontView);
                frontCardAnimator.start();
            }, delay);
            backView.postDelayed(() -> {
                backCardAnimator.setTarget(backView);
                backCardAnimator.start();
            }, delay);
        }
        if(callback != null){
            new Handler().postDelayed(() -> {
                callback.callback();
            }, delay);
        }
    }

    public void flipSecondCard(View frontView, View backView, long delay, FlipAnimationEndCallBack callback){
        AnimatorSet secondFrontAnimator = getSecondFrontCardAnimator();
        AnimatorSet secondBackAnimator = getSecondBackCardAnimator();
        frontView.postDelayed(() -> {
            secondFrontAnimator.setTarget(frontView);
            secondFrontAnimator.start();
        }, delay);
        backView.postDelayed(() -> {
            secondBackAnimator.setTarget(backView);
            secondBackAnimator.start();
        }, delay);
        if(callback != null){
            new Handler().postDelayed(() -> {
                callback.callback();
            }, delay);
        }
    }

    public interface FlipAnimationEndCallBack {
        void callback();
    }
}
