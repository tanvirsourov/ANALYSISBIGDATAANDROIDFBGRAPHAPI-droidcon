package com.technolive.droidconbigdata;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Sourov00 on 06-05-17.
 */

public class AnalyticsActivity extends AppCompatActivity {

    String my_access_token = "";
    ArrayList<JSONObject> page_likes;
    ArrayList<String> fb_pages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        my_access_token = getIntent().getStringExtra("my_access_token");


        final EditText first_page = (EditText) findViewById(R.id.first_page);
        final EditText second_page = (EditText) findViewById(R.id.second_page);
        final EditText third_page = (EditText) findViewById(R.id.third_page);

        Button generate_analysis = (Button) findViewById(R.id.generate_analysis);

        generate_analysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fb_pages = new ArrayList<>();
                fb_pages.add(first_page.getText().toString());
                fb_pages.add(second_page.getText().toString());
                fb_pages.add(third_page.getText().toString());

                page_likes = new ArrayList<>();

                AsyncHttpClient client = new AsyncHttpClient();

                for (int i = 0; i < fb_pages.size(); i++) {
                    final int finalI = i;
                    if (finalI == 0) {
                        page_likes.clear();
                    }
                    client.get("https://graph.facebook.com/" + fb_pages.get(i) + "?fields=fan_count&access_token=" + my_access_token, new AsyncHttpResponseHandler() {
                        @Override
                        public void onStart() {
                            // called before request is started
                        }
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            try {
                                page_likes.add(new JSONObject(new String(responseBody)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (finalI == 2) {
                                        PieChart mPieChart = (PieChart) findViewById(R.id.piechart);
                                        mPieChart.clearChart();
                                        Random rnd = new Random();
                                        for (int j = 0; j < fb_pages.size(); j++) {
                                            try {
                                                mPieChart.addPieSlice(new PieModel("Page: " + (j + 1), Integer.parseInt(page_likes.get(j).getString("fan_count")), Color.parseColor("#" + (rnd.nextInt(999999 - 100001) + 100001))));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        mPieChart.startAnimation();
                                        fb_pages.clear();
                                    }
                                }
                            }, 3000);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        }

                        @Override
                        public void onRetry(int retryNo) {
                            // called when request is retried
                        }
                    });
                }

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
