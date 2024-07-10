package com.nusiss.android_game_ca.game;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nusiss.android_game_ca.animators.CardAnimator;


public class GameCard {
    private int id;
    private int imageIndex;
    TextView cardFront;

    ImageButton cardBack;

    boolean isFlipped;

    MemoryGame game;

    public GameCard(int id, TextView cardFront, ImageButton cardBack){
        this.id = id;
        this.isFlipped = false;
        this.cardFront = cardFront;
        this.cardBack = cardBack;
        this.imageIndex = -1;
    }

    public void setCameraDistance(float scale){
        this.cardFront.setCameraDistance(8000 * scale);
        this.cardBack.setCameraDistance(8000 * scale);
    }

    public void setCardClickListener(CardAnimator animator){
        cardBack.setOnClickListener(view -> {
            if(!game.isGameStarted()){
                Toast.makeText(cardBack.getContext(), "Please start the game first.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(isFlipped){
                return;
            } else {
                int flippedCardId = game.getFlippedCardId();
                if(flippedCardId == 0){
                    animator.flipCard(cardFront, cardBack, 0L, null);
                    isFlipped = true;
                    game.setFlippedCardId(this.id);
                    return;
                } else if(game.getIsSecondCardFlipped() == MemoryGame.SecondCardIsNotFlipped ){
                    game.setIsSecondCardFlipped(MemoryGame.SecondCardIsFlipped);
                    isFlipped = true;
                    animator.flipCard(cardFront, cardBack, 0L, null);
                    if(game.gameCheck(flippedCardId, this.id)){
                        game.gainScore();
                        new Handler().postDelayed(() -> {
                            game.setIsSecondCardFlipped(MemoryGame.SecondCardIsNotFlipped);
                            game.setFlippedCardId(0);
                        }, 900);
                    } else {
                        GameCard flippedCard = game.findGameCardById(flippedCardId);
                        animator.flipCard(cardBack, cardFront, 2500L, () -> {
                            game.setIsSecondCardFlipped(MemoryGame.SecondCardIsNotFlipped);
                            game.setFlippedCardId(0);
                        });
                        animator.flipSecondCard(flippedCard.cardBack, flippedCard.cardFront, 2500L, null);
                        this.unflip();
                        flippedCard.unflip();
                    }

                }
            }
        });
    }

    public void setGame(MemoryGame game){
        this.game = game;
    }

    public int getId(){
        return id;
    }

    public void unflip(){
        isFlipped = false;
    }

    public int getCardImageIndex(){
        return imageIndex;
    }

    public void setCardImageIndex(int index){
        this.imageIndex = index;
    }

    public void bindImage (Activity context, Bitmap bitmap){
        context.runOnUiThread(() -> {
            cardBack.setImageBitmap(bitmap);
        });
    }

}
