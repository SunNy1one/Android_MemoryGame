package com.nusiss.android_game_ca.game;

import android.app.Activity;
import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.media.MediaPlayer;
import android.util.Log;

import android.widget.TextView;

import com.nusiss.android_game_ca.R;
import com.nusiss.android_game_ca.animators.CardAnimator;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;


public class MemoryGame {

    private Random rand;
    private List<GameCard> gameCards;
    private int currentScore;
    public int flippedCardId = 0;

    private int isSecondCardFlipped = 0;
    public int secondsElapsed = 0;

    private boolean isGameStarted = false;
    private TextView scoreBar;
    private ActionListener mActionListener;
    private CardAnimator cardAnimator;
    private Context context;
    private MediaPlayer successSound;
    private MediaPlayer failureSound;
    private MediaPlayer victorySound;


    public interface ActionListener{
        void onGainScore(int score);
        void onGameWin();
    }

    public MemoryGame(Context context, List<GameCard> gameCards, int seed, ActionListener actionListener)  {
        this.context = context;
        this.gameCards = gameCards;
        this.rand = new Random(seed);
        this.currentScore = 0;
        this.flippedCardId = 0;
        this.mActionListener = actionListener;
        initializeSounds();
    }

    private void initializeSounds() {
        successSound = MediaPlayer.create(context, R.raw.success_sound);
        failureSound = MediaPlayer.create(context, R.raw.failure_sound);
        victorySound = MediaPlayer.create(context, R.raw.victory_sound);
    }

    private void Initialize(){
        int[] imageIndices = {0,1,2,3,4,5};
        for(GameCard card : gameCards){
            int randomImage;
            do {
                randomImage = rand.nextInt(6);
            } while (paired(imageIndices[randomImage]));
            card.setCardImageIndex(imageIndices[randomImage]);
        }
    }

    private boolean paired(int imageIndex){
        long countCards = gameCards
                .stream()
                .filter(gc -> gc.getCardImageIndex() == imageIndex)
                .count();
        return countCards >= 2;
    }

    public void SetupGame(){
        Reset();
        Initialize();
    }

    private void Reset(){
        currentScore = 0;
        flippedCardId = 0;
        secondsElapsed = 0;
        for(GameCard card : gameCards){
            if(card.isFlipped){
                card.unflip();
            }
            if (card.getCardImageIndex() != -1) {
                card.cardFront.setRotationY(0);
                card.cardFront.setAlpha(1.0f);
                card.cardBack.setRotationY(0);
                card.cardBack.setAlpha(0.0f);
                card.setCardImageIndex(-1);
            }
        }
    }

    public boolean gameCheck(int card1Id, int card2Id){
        GameCard card1 = findGameCardById(card1Id);
        GameCard card2 = findGameCardById(card2Id);
        boolean isMatch = card1.getCardImageIndex() == card2.getCardImageIndex();

        if (isMatch) {
            playSuccessSound();
        } else {
            playFailureSound();
        }

        return isMatch;
    }

    public void SetupCardClickListener(CardAnimator animator){
        for(GameCard card : gameCards){
            card.setGame(this);
            card.setCardClickListener(animator);
        }
    }

    public int getFlippedCardId(){
        return this.flippedCardId;
    }

    public void setFlippedCardId(int flippedCardId){
        this.flippedCardId = flippedCardId;
    }

    public void gainScore(){
        currentScore++;
        mActionListener.onGainScore(currentScore);
    }

    public GameCard findGameCardById(int id){
        return gameCards
                .stream()
                .filter(gc -> gc.getId() == id)
                .findFirst()
                .get();
    }

    public void BindImagesToCard(Activity context, String[] urls){
        for(GameCard card : gameCards){
            new Thread(() -> {
                try {
                    Bitmap bm = BitmapFactory.decodeFile(urls[card.getCardImageIndex()]);
                    card.bindImage(context, bm);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public int getSecondsElapsed() {
        return secondsElapsed;
    }

    public void setSecondsElapsed(int secondsElapsed) {
        this.secondsElapsed = secondsElapsed;
    }

    public int getMinuteElapsed(){
        return (getSecondsElapsed() - (getSecondsElapsed() % 60)) / 60;
    }

    public String getSecondsPortionOfElapsed(){
        int sec = getSecondsElapsed() % 60;
        if(sec < 10){
            return "0" + sec;
        }
        return "" + sec;
    }

    public void lapsedOneSecond(){
        secondsElapsed++;
    }


    private final String ua = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36 OPR/38.0.2220.41";

    private void playSuccessSound() {
        if (successSound != null) {
            successSound.start();
        }
    }

    private void playFailureSound() {
        if (failureSound != null) {
            failureSound.start();
        }
    }

    private void playVictorySound() {
        if (victorySound != null) {
            victorySound.start();
        }
    }

    public void release() {
        if (successSound != null) {
            successSound.release();
            successSound = null;
        }
        if (failureSound != null) {
            failureSound.release();
            failureSound = null;
        }
        if (victorySound != null) {
            victorySound.release();
            victorySound = null;
        }
    }

    public static final int SecondCardIsNotFlipped = 0;
    public static final int SecondCardIsFlipped = 1;
    public void setIsSecondCardFlipped(int val){
        if(val == 0 || val == 1){
            isSecondCardFlipped = val;
        }
    }

    public int getIsSecondCardFlipped(){
        return isSecondCardFlipped;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        isGameStarted = gameStarted;
    }
}
