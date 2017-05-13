package com.technolive.droidconbigdata;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
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

import cz.msebera.android.httpclient.Header;

/**
 * Created by Sourov00 on 06-05-17.
 */

public class AnalyticsActivity extends AppCompatActivity {

    String my_access_token = "";

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

        final PieChart mPieChart = (PieChart) findViewById(R.id.piechart);
        Button generate_analysis = (Button) findViewById(R.id.generate_analysis);

        generate_analysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String first = first_page.getText().toString();
                String second = second_page.getText().toString();
                String third = third_page.getText().toString();

                final String[] first_like = {""};
                final String[] second_like = {""};
                final String[] third_like = {""};

                AsyncHttpClient client = new AsyncHttpClient();
                client.get("https://graph.facebook.com/" + first + "?fields=fan_count&access_token=" + my_access_token, new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        // called before request is started
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        first_like[0] = new String(responseBody);
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


                client.get("https://graph.facebook.com/" + second + "?fields=fan_count&access_token=" + my_access_token, new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        // called before request is started
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        second_like[0] = new String(responseBody);
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


                client.get("https://graph.facebook.com/" + third + "?fields=fan_count&access_token=" + my_access_token, new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        // called before request is started
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        third_like[0] = new String(responseBody);

                        int final1 = 0, final2 = 0, final3 = 0;
                        try {
                            JSONObject data1 = new JSONObject(first_like[0]);
                            JSONObject data2 = new JSONObject(second_like[0]);
                            JSONObject data3 = new JSONObject(third_like[0]);

                            final1 = Integer.parseInt(data1.getString("fan_count"));
                            final2 = Integer.parseInt(data2.getString("fan_count"));
                            final3 = Integer.parseInt(data3.getString("fan_count"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        mPieChart.addPieSlice(new PieModel("First Page", final1, Color.parseColor("#FE6DA8")));
                        mPieChart.addPieSlice(new PieModel("Second Page", final2, Color.parseColor("#56B7F1")));
                        mPieChart.addPieSlice(new PieModel("Third Page", final3, Color.parseColor("#CDA67F")));

                        mPieChart.startAnimation();
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
