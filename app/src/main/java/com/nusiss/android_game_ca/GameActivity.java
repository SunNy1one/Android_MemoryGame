package com.nusiss.android_game_ca;

import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nusiss.android_game_ca.animators.CardAnimator;
import com.nusiss.android_game_ca.game.GameCard;
import com.nusiss.android_game_ca.game.MemoryGame;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class GameActivity extends AppCompatActivity implements MemoryGame.ActionListener {

    private Map<Integer, Integer> cardPairIds = new HashMap<>();
    MemoryGame memoryGame;
    private CardAnimator cardAnimator;

    private Button returnBtn;
    private Button startBtn;
    private Button pauseBtn;
    private Button resetBtn;
    private Handler handler;

    private TextView record;
    private TextView tvUser;
    private TextView scoreBar;
    private TextView txtTime;
//    private Map<String, Integer> recordMap = new HashMap<>();
    private boolean isGaming = false;
    private String currentUser;

    private String[] urls;
    private MediaPlayer victorySound;

    @Override
    public void onGainScore(int score) {
        scoreBar.setText("Matched: "+score+" of 6");
        if(score == 6){
            startBtn.setEnabled(true);
            pauseBtn.setEnabled(false);
            isGaming = false;
            scoreBar.setText("Matched: 6 of 6.");
            if(handler != null){
                handler.removeCallbacksAndMessages(null);
            }
//            recordMap.put(currentUser, memoryGame.getSecondsElapsed());
//            //找出赢家，用时短的获胜
//            //Find the winner. The one with the shortest time wins.
//            String winUser = "";
//            int minValue = Integer.MAX_VALUE;
//
//            StringBuilder stringBuilder = new StringBuilder();
//            for (Map.Entry<String, Integer> entry : recordMap.entrySet()) {
//                if (entry.getValue() <= minValue) {
//                    minValue = entry.getValue();
//                    winUser = entry.getKey();
//                }
//                stringBuilder.append("Player：").append(entry.getKey()).append(getFormatTime(entry.getValue()))
//                        .append(",");
//            }
//            String result = stringBuilder.toString();
//            if (result.endsWith(",")) {
//                result = result.substring(0, result.length() - 1);
//            }
            record.setText("Player " + currentUser + " won in " + memoryGame.getSecondsElapsed() + " secs.");

            onGameWin();
            pauseBtn.setEnabled(false);
        }
    }

    public void onGameWin() {
        if (victorySound != null) {
            victorySound.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        MapCards();
        SetupAnimators();

        victorySound = MediaPlayer.create(this, R.raw.victory_sound);

        memoryGame = new MemoryGame(
                this,
                BuildGameCards(cardAnimator.getScale()),  // initialized 12 game cards
                (int)(LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(2)) % 1000), // seed for randomizer
                this // status bar view
        );
        memoryGame.SetupCardClickListener(cardAnimator);
        startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(this::clickStartBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        pauseBtn.setOnClickListener(this::clickPauseBtn);
        resetBtn = findViewById(R.id.resetBtn);
        resetBtn.setOnClickListener(this::clickResetBtn);
        pauseBtn.setOnClickListener(this::clickPauseBtn);
        returnBtn = findViewById(R.id.returnBtn);
        returnBtn.setOnClickListener(this::clickReturnBtn);
        record = findViewById(R.id.record);
        tvUser = findViewById(R.id.currentUser);
        scoreBar = findViewById(R.id.scoreBar);
        txtTime = findViewById(R.id.timeElapsedText);
    }


    @Override
    protected void onStart() {
        super.onStart();
        memoryGame.SetupGame();
        if(this != null){
            Intent callerIntent = getIntent();
            String url0 = callerIntent.getStringExtra("url_0");
            String url1 = callerIntent.getStringExtra("url_1");
            String url2 = callerIntent.getStringExtra("url_2");
            String url3 = callerIntent.getStringExtra("url_3");
            String url4 = callerIntent.getStringExtra("url_4");
            String url5 = callerIntent.getStringExtra("url_5");
            urls = new String[]{url0, url1, url2, url3, url4, url5};
            memoryGame.BindImagesToCard(this, urls);

        }

    }

    // Builds 12 game cards
    private List<GameCard> BuildGameCards(float scale){
        List<GameCard> gameCards = new ArrayList<>();
        cardPairIds.forEach((Integer k, Integer v) -> {
            TextView cardFront = findViewById(k);
            ImageButton cardBack = findViewById(v);
            GameCard card = new GameCard(cardFront.getId(), cardFront, cardBack);
            card.setCameraDistance(scale);
            gameCards.add(card);
        });
        return gameCards;
    }

    // Set up the initial 12 images mapping
    private void MapCards(){
        cardPairIds.put(R.id.card1Front, R.id.card1Back);
        cardPairIds.put(R.id.card2Front, R.id.card2Back);
        cardPairIds.put(R.id.card3Front, R.id.card3Back);
        cardPairIds.put(R.id.card4Front, R.id.card4Back);
        cardPairIds.put(R.id.card5Front, R.id.card5Back);
        cardPairIds.put(R.id.card6Front, R.id.card6Back);
        cardPairIds.put(R.id.card7Front, R.id.card7Back);
        cardPairIds.put(R.id.card8Front, R.id.card8Back);
        cardPairIds.put(R.id.card9Front, R.id.card9Back);
        cardPairIds.put(R.id.card10Front, R.id.card10Back);
        cardPairIds.put(R.id.card11Front, R.id.card11Back);
        cardPairIds.put(R.id.card12Front, R.id.card12Back);
    }

    private void SetupAnimators(){
        float scale = getApplicationContext().getResources().getDisplayMetrics().density;

        AnimatorSet frontAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.front_animator);
        AnimatorSet backAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.back_animator);

        cardAnimator = new CardAnimator(scale, frontAnimator, backAnimator);
    }

    private void clickReturnBtn(View view){
        finish();
    }

    private void clickStartBtn(View view){
        // enable stop button
        if (isGaming) {
            startBtn.setEnabled(false);
            startTimer();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Please enter the player's name");

            final EditText input = new EditText(this);
            builder.setView(input);

            // Setting up the dialogue box buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    currentUser = input.getText().toString();
//                    recordMap.put(currentUser, 0);
                    isGaming = true;
                    startBtn.setEnabled(false);
                    tvUser.setText("Current Player：" + currentUser);
                    scoreBar.setText("Matched: 0 of 6.");
                    txtTime.setText("Elapsed " + memoryGame.getMinuteElapsed() + ":" + memoryGame.getSecondsPortionOfElapsed());
                    memoryGame.SetupGame();
                    memoryGame.setGameStarted(true);
                    memoryGame.BindImagesToCard(GameActivity.this, urls);
                    startTimer();
                    resetBtn.setEnabled(true);
                    pauseBtn.setEnabled(true);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void clickResetBtn(View view){
        memoryGame.SetupGame();
        memoryGame.BindImagesToCard(this, urls);
        memoryGame.setGameStarted(true);
        scoreBar.setText("Matched: 0 of 6.");
        startTimer();
        txtTime.setText("Elapsed " + memoryGame.getMinuteElapsed() + ":" + memoryGame.getSecondsPortionOfElapsed());
        pauseBtn.setEnabled(true);
        startBtn.setEnabled(false);
    }

    private void clickPauseBtn(View view){
        if(handler != null){
            handler.removeCallbacksAndMessages(null);
        }
        if(pauseBtn.getText().toString().equals("Pause")){
            memoryGame.setGameStarted(false);
            pauseBtn.setText("Resume");
        } else {
            startTimer();
            memoryGame.setGameStarted(true);
            pauseBtn.setText("Pause");
        }

    }
    private void startTimer() {
        final TextView txtTime = findViewById(R.id.timeElapsedText);
        if(handler == null){
            handler = new Handler();
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                memoryGame.lapsedOneSecond();
                txtTime.setText("Elapsed " + memoryGame.getMinuteElapsed() + ":" + memoryGame.getSecondsPortionOfElapsed());
                handler.postDelayed(this, 1000);
            }
        });
    }
    public String getFormatTime(int seconds){
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        // Formatted output
        String formattedTime;
        if (minutes > 0) {
            formattedTime = String.format("%d:%02d", minutes, remainingSeconds);
        } else {
            formattedTime = String.format("%d", remainingSeconds);
        }
        return formattedTime;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (memoryGame != null) {
            memoryGame.release();  // Release MediaPlayer resources
        }
        if (victorySound != null) {
            victorySound.release(); // 释放胜利音效资源
            victorySound = null;
        }
    }
}