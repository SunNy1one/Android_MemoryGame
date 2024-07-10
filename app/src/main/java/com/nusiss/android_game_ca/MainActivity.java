package com.nusiss.android_game_ca;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.nusiss.android_game_ca.adapters.GridAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ProgressBar progressBar;
    TextView progressText;
    Button startGameButton;
    private GridAdapter adapter;

    private TextView selectedCountTextView;
    private ArrayList<String> selectedUrls = new ArrayList<>();

    private List<String> urls = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupButtonsAndAdapter();
    }

    private void setupButtonsAndAdapter(){
        progressBar = findViewById(R.id.determinateBar);
        progressText = findViewById(R.id.progressText);
        progressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        adapter = new GridAdapter(this, urls);

        Button fetchButton = findViewById(R.id.fetchButton);
        fetchButton.setOnClickListener(this);

        startGameButton = findViewById(R.id.startGameButton);
        startGameButton.setVisibility(View.GONE); // Hide initially
        startGameButton.setOnClickListener(this);

        GridView gridView = findViewById(R.id.downloadedImgs);
        if(gridView != null){
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this::onItemClick);
        }
    }

    @Override
    public void onClick(View view){
        int id = view.getId();
        if (id == R.id.fetchButton) {
            TextInputEditText searchInput = findViewById(R.id.searchurl);
            String url = searchInput.getText().toString();
            tryFetch(url);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else if(id == R.id.startGameButton){
            if(selectedUrls.size() < 6){
                Toast.makeText(this, "Require 6 images to start game.", Toast.LENGTH_LONG).show();
                return;
            }
            Intent gameActivityIntent = new Intent(this, GameActivity.class);
            gameActivityIntent.putExtra("url_0", selectedUrls.get(0));
            gameActivityIntent.putExtra("url_1", selectedUrls.get(1));
            gameActivityIntent.putExtra("url_2", selectedUrls.get(2));
            gameActivityIntent.putExtra("url_3", selectedUrls.get(3));
            gameActivityIntent.putExtra("url_4", selectedUrls.get(4));
            gameActivityIntent.putExtra("url_5", selectedUrls.get(5));
            startActivity(gameActivityIntent);
        } else {
            view.setBackgroundColor(Color.parseColor("#ffcceecc"));
        }
    }

    public void onItemClick(AdapterView adapterView, View view, int pos, long id){
        ImageView imgView = (ImageView) view;
        String clickedUrl = urls.get(pos);
        if(selectedUrls.contains(clickedUrl)){
            selectedUrls.remove(clickedUrl);
            view.setBackgroundColor(Color.TRANSPARENT);
            imgView.clearColorFilter();
            updateSelectedCountTextView();
        } else if(selectedUrls.size() < 6){
            selectedUrls.add(clickedUrl);
            view.setBackgroundColor(Color.parseColor("#ffcceecc"));
            imgView.setColorFilter(Color.argb(150, 255, 255, 0));
            updateSelectedCountTextView();
        } else {
            Toast.makeText(this, "You have already selected 6 images.", Toast.LENGTH_SHORT).show();
        }
        // Update start game button visibility
        updateStartGameButtonVisibility();
    }

    private void tryFetch(String url){
        // Clear previous Urls
        urls.clear();
        selectedUrls.clear();
        // Reset UI elements for new fetch
        showProgressBar();

        // Start to download images and push to the adapter
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    Document doc = Jsoup.connect("https://stocksnap.io").get();
                    Elements links = doc.select("img[src]");
                    for (int i = 0; i < links.size(); i++) {
                        Element link = links.get(i);
                        String imgUrl = link.attr("src");
                        if(imgUrl.startsWith("https://cdn.") && imgUrl.endsWith("jpg")){
                            int finalI = i;

                            URL url = new URL(imgUrl);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestProperty("user-agent", ua);
                            InputStream inputStream = connection.getInputStream();
                            File imgFile = new File(getFilesDir(), "img" + i + ".jpg");
                            writeToFile(inputStream, imgFile);

                            runOnUiThread(() -> {
                                adapter.setUrls(urls);
                                adapter.notifyDataSetChanged();

                                int progress = Math.min(finalI * 100 / links.size(), 100);
                                progressBar.setProgress(progress);
                                progressText.setText("Downloading " + finalI + " of " + links.size() + " images");
                            });
                            inputStream.close();
                            connection.disconnect();
                        }
                    }
                    // Hide progress bar and show download complete text
                    runOnUiThread(() -> {
                        hideProgressBar();
                        adapter.setUrls(urls);
                        adapter.notifyDataSetChanged();
                        // Hide download complete text and inform user to select images
                        showSelectionInstructions();
                        // Check and refresh game start button visibility
                        updateStartGameButtonVisibility();
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run(){
                                showSelectedCountTextView();
                            }
                        }, 2000);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateStartGameButtonVisibility(){
        if (selectedUrls.size() == 6) {
            startGameButton.setVisibility(View.VISIBLE);
        } else {
            startGameButton.setVisibility(View.GONE);
        }
    }

    private void showProgressBar(){
        // Reset UI elements for new fetch
        updateStartGameButtonVisibility();
        progressText.setVisibility(View.VISIBLE);
        progressText.setText("Downloading...");
        progressBar.setVisibility(View.VISIBLE); // Show progress bar
        progressBar.setProgress(0); // Reset progress bar
        adapter.setUrls(urls);
        adapter.notifyDataSetChanged();
    }

    private void hideProgressBar(){
        progressBar.setVisibility(View.GONE); // Hide progress bar
        progressBar.setProgress(100);
        progressText.setText("Download complete");
    }

    private void showSelectionInstructions(){
        progressText.postDelayed(() -> {
            progressText.setVisibility(View.GONE);
            progressText.postDelayed(() -> {
                Toast.makeText(MainActivity.this, "Select exactly 6 images to start game", Toast.LENGTH_LONG).show();
            }, 500);
        }, 1000);
    }

    private void writeToFile(InputStream in, File file){
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while((bytesRead = in.read(buffer)) != -1){
                out.write(buffer, 0, bytesRead);
            }
            urls.add(file.getAbsolutePath());
            out.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private final String ua = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36 OPR/38.0.2220.41";

    private void showSelectedCountTextView() {
        if (selectedCountTextView == null) {
            selectedCountTextView = findViewById(R.id.selectedCountTextView);
        }
        selectedCountTextView.setVisibility(View.VISIBLE);

        // Update selected count TextView initially
        updateSelectedCountTextView();
    }

    private void updateSelectedCountTextView() {
        if (selectedCountTextView == null) {
            return; // TextView not initialized yet
        }

        int selectedCount = selectedUrls.size();
        int totalImages = 6; // Total number of images required for the game
        String countText = selectedCount + " of " + totalImages + " selected";
        selectedCountTextView.setText(countText);
    }

}