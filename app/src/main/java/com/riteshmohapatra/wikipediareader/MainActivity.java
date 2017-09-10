package com.riteshmohapatra.wikipediareader;

import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RequestQueue queue;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.queue = Volley.newRequestQueue(MainActivity.this);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                // noop
            }
        });
        tts.setLanguage(Locale.getDefault());

        setContentView(R.layout.activity_main);

        ImageButton searchBtn = (ImageButton) findViewById(R.id.searchBtn);
        final EditText searchBox = (EditText) findViewById(R.id.editText);
        final TextView textView = (TextView) findViewById(R.id.textView);
        final ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar);
        final View textViewer = findViewById(R.id.textViewer);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.volume);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBox.clearFocus();
                textViewer.requestFocus();
                String url = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles=" + encodeURIComponent(searchBox.getText().toString().trim());

                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                progress.setVisibility(View.INVISIBLE);
                                try {
                                    JSONObject pages = response.getJSONObject("query")
                                            .getJSONObject("pages");
                                    String firstPage = pages.keys().next();
                                    String text = pages.getJSONObject(firstPage).getString("extract");
                                    textViewer.setVisibility(View.VISIBLE);
                                    textView.setText(text);
                                } catch (JSONException ex) {
                                    Toast.makeText(MainActivity.this,"Error in parsing response", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progress.setVisibility(View.INVISIBLE);
                                // todo: view image
                                Toast.makeText(MainActivity.this,"Error in getting response", Toast.LENGTH_SHORT).show();

                            }
                        });


                // Add the request to the RequestQueue.
                textViewer.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
                MainActivity.this.queue.add(jsObjRequest);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tts.isSpeaking()) {
                    tts.stop();
                    fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_play_arrow));
                } else {
                    fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_stop));
                    tts.speak(textView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.tts.stop();
        this.queue.stop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.queue.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.tts.shutdown();
    }

    private static String encodeURIComponent(String s)
    {
        String result = null;

        try
        {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        }

        // This exception should never occur.
        catch (UnsupportedEncodingException e)
        {
            result = s;
        }

        return result;
    }

}
