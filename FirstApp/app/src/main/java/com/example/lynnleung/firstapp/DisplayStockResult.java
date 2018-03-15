package com.example.lynnleung.firstapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

public class DisplayStockResult extends AppCompatActivity {

    public String symbol = "";
    public String getSym() {
        return symbol;
    }
    //sp
    public SharedPreferences fav_sp;
    public SharedPreferences.Editor editor;
    public ArrayList<HashMap<String, String>> favMap = new ArrayList<HashMap<String, String>>();
    public HashSet<String> symSet;
    boolean check;
    public boolean Check() {
        return check;
    }


    CallbackManager callbackManager;
    ShareDialog shareDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_stock_result);

        //sp
        fav_sp = getSharedPreferences("favlist",MODE_PRIVATE);
        editor = fav_sp.edit();
        symSet = (HashSet<String>) fav_sp.getStringSet("symbol",new HashSet<String>());

        //get symbol
        Intent intent = getIntent();
        symbol = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        //check fav
        check=fav_check();
        //back main page
        getSupportActionBar().setTitle(symbol);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //viewpager instance
        ViewPager pager = (ViewPager) findViewById(R.id.info_con);
        //tab instance
        TabLayout host = findViewById(R.id.tabHost);
        //Create all tabs
        Bundle bundle = new Bundle();
        bundle.putString("data", symbol);
        CurFragment f = new CurFragment();
        f.setArguments(bundle);
        PagerAdapter pagerAda = new PagerAdapter(getSupportFragmentManager());
        pagerAda.addFragments(f, "CURRENT");
        pagerAda.addFragments(new HisCFragment(), "HISTORICAL");
        pagerAda.addFragments(new NewsFragment(), "NEWS");
        pager.setAdapter(pagerAda);
        host.setupWithViewPager(pager);
        initFacebook();




    }

/*    @Override
    protected void onStart(Bundle savedInstanceState) {}*/
    private void initFacebook() {
        //fb
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        // this part is optional
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {

            @Override
            public void onSuccess(Sharer.Result result) {
                Toast.makeText(DisplayStockResult.this, "Share successfully!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {
                Toast.makeText(DisplayStockResult.this, "Share cancel!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(DisplayStockResult.this, "Share error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,    Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }

    public boolean fav_check(){
        if(symSet.contains(symbol)){
            return true;
        }
        else {
            return false;
        }

    }

    public void fav_func (View view){
        Button fav_btn = (Button) view;
        if (fav_btn.getTag().toString().equals("empty"))
        {
            fav_btn.setBackgroundResource(R.drawable.filled);
            fav_btn.setTag("filled");
            symSet.add(symbol);
            editor.putStringSet("symbol",symSet);
            editor.commit();

        } else if (fav_btn.getTag().toString().equals("filled")) {
            fav_btn.setBackgroundResource(R.drawable.empty);
            fav_btn.setTag("empty");
            symSet.remove(symbol);
            editor.clear();
            editor.putStringSet("symbol",symSet);
            editor.commit();
        }

    }

    public void fb_func (View view){
        /*Toast.makeText(DisplayStockResult.this, "FB",
                Toast.LENGTH_SHORT).show();*/
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            /*shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {


                @Override
                public void onSuccess(Sharer.Result result) {
                    Toast.makeText(FacebookActivity.this, "You shared this post", Toast.LENGTH_SHORT).show();
                    Log.d("DEBUG", "SHARE SUCCESS");
                    finish();
                }

                @Override
                public void onCancel() {
                    Toast.makeText(FacebookActivity.this, "Post Cancelled", Toast.LENGTH_SHORT).show();
                    Log.d("DEBUG", "SHARE CANCELLED");
                    finish();
                }

                @Override
                public void onError(FacebookException exception) {
                    Toast.makeText(FacebookActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("DEBUG", "Share: " + exception.getMessage());
                    exception.printStackTrace();
                    finish();
                }
            });*/

            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse("http://cs-server.usc.edu:12623/Price.html?sym="+symbol))
                    .build();
            shareDialog.show(linkContent);


        } else {
            Log.i("ERROR", "cannot show share");
        }

    }

    public class PagerAdapter extends FragmentPagerAdapter {
        List<String> title_arr = new ArrayList<>();
        List<Fragment> fragment_arr = new ArrayList<>();


        public void addFragments(Fragment fragment, String title) {
            this.fragment_arr.add(fragment);
            this.title_arr.add(title);
        }

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {

            return fragment_arr.get(position);
        }

        @Override
        public int getCount() {

            return fragment_arr.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return title_arr.get(position);
        }
    }

    @SuppressLint("ValidFragment")
    public static class CurFragment extends Fragment {

        public CurFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View stockView = inflater.inflate(R.layout.stock_table, container, false);
            String s = getArguments().getString("data");
            CurrentStock cs = new CurrentStock();
            cs.execute(s);
            PriceChart2 p = new PriceChart2();
            p.execute(s);
            return stockView;
        }

        //create current stock table
        private class CurrentStock extends AsyncTask<String, Integer, JSONObject> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            /*<ProgressBar
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />*/
            }


            @Override
            protected JSONObject doInBackground(String... strings) {
                String stockSymbol = strings[0];
                String currUrl = "http://csci571-hw8php.us-west-1.elasticbeanstalk.com/index.php/?symbol=" + stockSymbol;
                URL surl = null;
                InputStream in;
                JSONObject stcArr = null;
                TextView stock_e = getActivity().findViewById(R.id.stock_e);
                ProgressBar stock_bar = getActivity().findViewById(R.id.stockbar);
                try {
                    surl = new URL(currUrl);
                    HttpURLConnection conn = (HttpURLConnection) surl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    if (conn.getResponseCode() != 200) {
                        stock_bar.setVisibility(View.GONE);
                        stock_e.setVisibility(View.VISIBLE);
                        throw new RuntimeException("fail to request url");
                    }
                    in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    String stc_str = "";
                    while ((line = reader.readLine()) != null) {
                        stc_str += line;
                    }
                    in.close();
                    stcArr = new JSONObject(stc_str);
                    return stcArr;
                } catch (ProtocolException e) {
                    stock_bar.setVisibility(View.GONE);
                    stock_e.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    stock_bar.setVisibility(View.GONE);
                    stock_e.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    stock_bar.setVisibility(View.GONE);
                    stock_e.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                }
                return stcArr;
            }

            @Override
            protected void onPostExecute(JSONObject stcArr) {
                super.onPostExecute(stcArr);
                TextView stock_e = getActivity().findViewById(R.id.stock_e);
                ProgressBar stock_bar = getActivity().findViewById(R.id.stockbar);
                stock_e.setVisibility(View.GONE);
                TextView temp;
                ImageView image;
                View div;
                if(stcArr==null){
                    stock_bar.setVisibility(View.GONE);
                    stock_e.setVisibility(View.VISIBLE);
                }
                try {
                    JSONObject meta = stcArr.getJSONObject("Meta Data");
                    String date = meta.getString("3. Last Refreshed");
                    JSONObject data_list = stcArr.getJSONObject("Time Series (Daily)");
                    int k = 0;
                    String[] key = new String[2];
                    Iterator<String> iter = data_list.keys();
                    while (iter.hasNext()) {
                        if (k == 2) {
                            break;
                        }
                        key[k] = iter.next();
                        k++;
                    }
                    JSONObject currDay = data_list.getJSONObject(key[0]);
                    JSONObject preDay = data_list.getJSONObject(key[1]);
                    DecimalFormat df = new DecimalFormat("0.00");
                    String close1 = currDay.getString("4. close");
                    float c1 = Float.parseFloat(close1);
                    String close2 = preDay.getString("4. close");
                    float c2 = Float.parseFloat(preDay.getString("4. close"));
                    String open = currDay.getString("1. open");
                    float o = Float.parseFloat(currDay.getString("1. open"));
                    String low = currDay.getString("1. open");
                    float l = Float.parseFloat(currDay.getString("3. low"));
                    String high = currDay.getString("1. open");
                    float h = Float.parseFloat(currDay.getString("2. high"));
                    String volume = currDay.getString("1. open");
                    float ch = c1 - c2;
                    float pre = (ch / c2) * 100;
                    close1 = df.format(c1);
                    close2 = df.format(c2);
                    open = df.format(o);
                    low = df.format(l);
                    high = df.format(h);
                    String change = df.format(ch);
                    String precent = df.format(pre);

                    //fill col name
                    temp = getActivity().findViewById(R.id.col_name1);
                    temp.setText("Stock Symbol");
                    temp = getActivity().findViewById(R.id.col_name2);
                    temp.setText("Last Price");
                    temp = getActivity().findViewById(R.id.col_name3);
                    temp.setText("Change");
                    temp = getActivity().findViewById(R.id.col_name4);
                    temp.setText("Timestamp");
                    temp = getActivity().findViewById(R.id.col_name5);
                    temp.setText("Open");
                    temp = getActivity().findViewById(R.id.col_name6);
                    temp.setText("Close");
                    temp = getActivity().findViewById(R.id.col_name7);
                    temp.setText("Day's Range");
                    temp = getActivity().findViewById(R.id.col_name8);
                    temp.setText("Volume");

                    //fill col data
                    temp = getActivity().findViewById(R.id.col_data1);
                    temp.setText(((DisplayStockResult) getActivity()).getSym());
                    temp = getActivity().findViewById(R.id.col_data2);
                    temp.setText(close1);
                    temp = getActivity().findViewById(R.id.col_data3);
                    temp.setText(change + "(" + precent + "%)");
                    image = getActivity().findViewById(R.id.icon3);
                    if (ch >= 0) {
                        image.setImageResource(R.drawable.up);
                    } else {
                        image.setImageResource(R.drawable.down);
                    }
                    temp = getActivity().findViewById(R.id.col_data4);
                    if (date.length() > 10) {
                        temp.setText(date + " EDT");
                    } else {
                        temp.setText(date + " 16:00:00 EDT");
                    }
                    temp = getActivity().findViewById(R.id.col_data5);
                    temp.setText(open);
                    temp = getActivity().findViewById(R.id.col_data6);
                    if (date.length() > 10) {
                        temp.setText(close2);
                    } else {
                        temp.setText(close1);
                    }
                    temp = getActivity().findViewById(R.id.col_data7);
                    temp.setText(low + " - " + high);
                    temp = getActivity().findViewById(R.id.col_data8);
                    temp.setText(volume);

                    //draw divider
                    div = getActivity().findViewById(R.id.d1);
                    div.setVisibility(View.VISIBLE);
                    div = getActivity().findViewById(R.id.d2);
                    div.setVisibility(View.VISIBLE);
                    div = getActivity().findViewById(R.id.d3);
                    div.setVisibility(View.VISIBLE);
                    div = getActivity().findViewById(R.id.d4);
                    div.setVisibility(View.VISIBLE);
                    div = getActivity().findViewById(R.id.d5);
                    div.setVisibility(View.VISIBLE);
                    div = getActivity().findViewById(R.id.d6);
                    div.setVisibility(View.VISIBLE);
                    div = getActivity().findViewById(R.id.d7);
                    div.setVisibility(View.VISIBLE);
                    div = getActivity().findViewById(R.id.d8);
                    div.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    stock_bar.setVisibility(View.GONE);
                    stock_e.setVisibility(View.VISIBLE);
                }

                //Spineer
                Spinner chart_spin=getActivity().findViewById(R.id.charts);
                final String select=chart_spin.getSelectedItem().toString();

                chart_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    Button change_btn = getActivity().findViewById(R.id.change_btn);
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        final String select = parent.getItemAtPosition(position).toString();
                        change_btn.setEnabled(true);
                        change_btn.setOnClickListener(new View.OnClickListener() {
                            String temp = select;

                            @Override
                            public void onClick(View v) {
                                String sym = ((DisplayStockResult) getActivity()).getSym();
                                /*Toast.makeText(getActivity(), "Please enter a stock name or a symbol",
                                        Toast.LENGTH_SHORT).show();*/

                                class PriceChart extends AsyncTask<String, Integer, String> {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();

                                    }


                                    @Override
                                    protected String doInBackground(String... strings) {
                                        String sym = strings[0];
                                        return sym;
                                    }

                                    @Override
                                    protected void onPostExecute(String sym) {
                                        super.onPostExecute(sym);
                                        WebView webView = getActivity().findViewById(R.id.Ind_con);
                                        webView.getSettings().setJavaScriptEnabled(true);
                                        String priceurl = "http://cs-server.usc.edu:12623/Price.html?sym="+sym;
                                        webView.loadUrl(priceurl);
                                    }

                                }
                                class SMA extends AsyncTask<String, Integer, String> {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                /*<ProgressBar
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" />*/
                                    }


                                    @Override
                                    protected String doInBackground(String... strings) {
                                        String sym = strings[0];
                                        return sym;
                                    }

                                    @Override
                                    protected void onPostExecute(String sym) {
                                        super.onPostExecute(sym);
                                        WebView webView = getActivity().findViewById(R.id.Ind_con);
                                        webView.getSettings().setJavaScriptEnabled(true);
                                        String priceurl = "http://cs-server.usc.edu:12623/SMA.html?sym="+sym;
                                        webView.loadUrl(priceurl);
                                    }

                                }
                                class EMA extends AsyncTask<String, Integer, String> {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                /*<ProgressBar
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" />*/
                                    }


                                    @Override
                                    protected String doInBackground(String... strings) {
                                        String sym = strings[0];
                                        return sym;
                                    }

                                    @Override
                                    protected void onPostExecute(String sym) {
                                        super.onPostExecute(sym);
                                        WebView webView = getActivity().findViewById(R.id.Ind_con);
                                        webView.getSettings().setJavaScriptEnabled(true);
                                        String priceurl = "http://cs-server.usc.edu:12623/EMA.html?sym="+sym;
                                        webView.loadUrl(priceurl);
                                    }

                                }
                                class RSI extends AsyncTask<String, Integer, String> {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                /*<ProgressBar
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" />*/
                                    }


                                    @Override
                                    protected String doInBackground(String... strings) {
                                        String sym = strings[0];
                                        return sym;
                                    }

                                    @Override
                                    protected void onPostExecute(String sym) {
                                        super.onPostExecute(sym);
                                        WebView webView = getActivity().findViewById(R.id.Ind_con);
                                        webView.getSettings().setJavaScriptEnabled(true);
                                        String priceurl = "http://cs-server.usc.edu:12623/RSI.html?sym="+sym;
                                        webView.loadUrl(priceurl);
                                    }

                                }
                                class ADX extends AsyncTask<String, Integer, String> {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                /*<ProgressBar
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" />*/
                                    }


                                    @Override
                                    protected String doInBackground(String... strings) {
                                        String sym = strings[0];
                                        return sym;
                                    }

                                    @Override
                                    protected void onPostExecute(String sym) {
                                        super.onPostExecute(sym);
                                        WebView webView = getActivity().findViewById(R.id.Ind_con);
                                        webView.getSettings().setJavaScriptEnabled(true);
                                        String priceurl = "http://cs-server.usc.edu:12623/ADX.html?sym="+sym;
                                        webView.loadUrl(priceurl);
                                    }

                                }
                                class CGI extends AsyncTask<String, Integer, String> {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                /*<ProgressBar
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" />*/
                                    }


                                    @Override
                                    protected String doInBackground(String... strings) {
                                        String sym = strings[0];
                                        return sym;
                                    }

                                    @Override
                                    protected void onPostExecute(String sym) {
                                        super.onPostExecute(sym);
                                        WebView webView = getActivity().findViewById(R.id.Ind_con);
                                        webView.getSettings().setJavaScriptEnabled(true);
                                        String priceurl = "http://cs-server.usc.edu:12623/CGI.html?sym="+sym;
                                        webView.loadUrl(priceurl);
                                    }

                                }
                                class MACD extends AsyncTask<String, Integer, String> {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                /*<ProgressBar
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" />*/
                                    }


                                    @Override
                                    protected String doInBackground(String... strings) {
                                        String sym = strings[0];
                                        return sym;
                                    }

                                    @Override
                                    protected void onPostExecute(String sym) {
                                        super.onPostExecute(sym);
                                        WebView webView = getActivity().findViewById(R.id.Ind_con);
                                        webView.getSettings().setJavaScriptEnabled(true);
                                        String priceurl = "http://cs-server.usc.edu:12623/MACD.html?sym="+sym;
                                        webView.loadUrl(priceurl);
                                    }

                                }
                                class STOCH extends AsyncTask<String, Integer, String> {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                /*<ProgressBar
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" />*/
                                    }


                                    @Override
                                    protected String doInBackground(String... strings) {
                                        String sym = strings[0];
                                        return sym;
                                    }

                                    @Override
                                    protected void onPostExecute(String sym) {
                                        super.onPostExecute(sym);
                                        WebView webView = getActivity().findViewById(R.id.Ind_con);
                                        webView.getSettings().setJavaScriptEnabled(true);
                                        String priceurl = "http://cs-server.usc.edu:12623/STOCH.html?sym="+sym;
                                        webView.loadUrl(priceurl);
                                    }

                                }
                                class BBANDS extends AsyncTask<String, Integer, String> {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                /*<ProgressBar
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" />*/
                                    }


                                    @Override
                                    protected String doInBackground(String... strings) {
                                        String sym = strings[0];
                                        return sym;
                                    }

                                    @Override
                                    protected void onPostExecute(String sym) {
                                        super.onPostExecute(sym);
                                        WebView webView = getActivity().findViewById(R.id.Ind_con);
                                        webView.getSettings().setJavaScriptEnabled(true);
                                        String priceurl = "http://cs-server.usc.edu:12623/BBANDS.html?sym="+sym;
                                        webView.loadUrl(priceurl);
                                    }

                                }

                                if (temp.equals("SMA")) {
                                    SMA sma2 = new SMA();
                                    sma2.execute(sym);
                                } else if (temp.equals("EMA")) {
                                    EMA ema = new EMA();
                                    ema.execute(sym);
                                } else if (temp.equals("CGI")) {
                                    CGI cgi = new CGI();
                                    cgi.execute(sym);
                                } else if (temp.equals("MACD")) {
                                    MACD macd = new MACD();
                                    macd.execute(sym);
                                } else if (temp.equals("RSI")) {
                                    RSI rsi = new RSI();
                                    rsi.execute(sym);
                                } else if (temp.equals("ADX")) {
                                    ADX adx = new ADX();
                                    adx.execute(sym);
                                } else if (temp.equals("BBANDS")) {
                                    BBANDS bb = new BBANDS();
                                    bb.execute(sym);
                                } else if (temp.equals("STOCH")) {
                                    STOCH stoch = new STOCH();
                                    stoch.execute(sym);
                                } else if (temp.equals("Price")) {
                                    PriceChart p2 = new PriceChart();
                                    p2.execute(sym);

                                }
                                change_btn.setEnabled(false);

                            }

                        });
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                Button fav_btn = getActivity().findViewById(R.id.fav_btn);
                boolean check = ((DisplayStockResult) getActivity()).fav_check();
                if (check){
                    fav_btn.setBackgroundResource(R.drawable.filled);
                    fav_btn.setTag("filled");
                }else {
                    fav_btn.setBackgroundResource(R.drawable.empty);
                    fav_btn.setTag("empty");
                }
                stock_bar.setVisibility(View.GONE);
            }

        }

        private class PriceChart2 extends AsyncTask<String, Integer, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            /*<ProgressBar
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />*/
            }


            @Override
            protected String doInBackground(String... strings) {
                String sym = strings[0];
                return sym;
            }

            @Override
            protected void onPostExecute(String sym) {
                super.onPostExecute(sym);
                WebView webView = getActivity().findViewById(R.id.Ind_con);
                webView.getSettings().setJavaScriptEnabled(true);
                String priceurl = "http://cs-server.usc.edu:12623/Price.html?sym="+sym;
                webView.loadUrl(priceurl);
            }

        }

    }

    @SuppressLint("ValidFragment")
    public static class HisCFragment extends Fragment {

        public HisCFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View hchartView = inflater.inflate(R.layout.his_chart, container, false);
            String s = ((DisplayStockResult) getActivity()).getSym();
            HisChart hchart = new HisChart();
            hchart.execute(s);
            return hchartView;
        }

        //create his chart
        private class HisChart extends AsyncTask<String, Integer, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }


            @Override
            protected String doInBackground(String... strings) {
                String sym = strings[0];
                return sym;
            }

            @Override
            protected void onPostExecute(String sym) {
                super.onPostExecute(sym);
                TextView his_e = getActivity().findViewById(R.id.his_e);
                ProgressBar his_bar = getActivity().findViewById(R.id.hisbar);
                    WebView webView = getActivity().findViewById(R.id.his_con);
                    webView.getSettings().setJavaScriptEnabled(true);
                    String hisurl = "http://cs-server.usc.edu:12623/hischart.html?sym="+sym;
                    webView.loadUrl(hisurl);
                    his_bar.setVisibility(View.GONE);
            }

            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
                TextView his_e = getActivity().findViewById(R.id.his_e);
                ProgressBar his_bar = getActivity().findViewById(R.id.hisbar);
                his_bar.setVisibility(View.GONE);
                his_e.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), "Your Internet Connection May not be active Or " + error , Toast.LENGTH_LONG).show();
            }


            }

        }

    @SuppressLint("ValidFragment")
    public static class NewsFragment extends Fragment {
        public NewsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View newsView = inflater.inflate(R.layout.news_layout, container, false);
            String s = ((DisplayStockResult) getActivity()).getSym();
            NewsList news = new NewsList();
            news.execute(s);
            return newsView;
        }

        private class NewsList extends AsyncTask<String, Integer, JSONArray> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected JSONArray doInBackground(String... strings) {
                String newsSymbol = strings[0];
                String Url = "http://csci571-hw8php.us-west-1.elasticbeanstalk.com/index.php/?news=" + newsSymbol;
                URL newsUrl = null;
                InputStream in2;
                JSONArray newsArr = null;
                TextView news_e = getActivity().findViewById(R.id.news_e);
                ProgressBar news_bar = getActivity().findViewById(R.id.newsbar);
                try {
                    newsUrl = new URL(Url);
                    HttpURLConnection conn2 = (HttpURLConnection) newsUrl.openConnection();
                    conn2.setRequestMethod("GET");
                    conn2.setDoInput(true);
                    if (conn2.getResponseCode() != 200) {
                        news_bar.setVisibility(View.GONE);
                        news_e.setVisibility(View.VISIBLE);
                        throw new RuntimeException("fail to request url");
                    }
                    in2 = conn2.getInputStream();
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(in2));
                    String line2;
                    String news_str = "";
                    while ((line2 = reader2.readLine()) != null) {
                        news_str += line2;
                    }
                    in2.close();
                    newsArr = new JSONArray(news_str);
                    return newsArr;
                } catch (ProtocolException e) {
                    news_bar.setVisibility(View.GONE);
                    news_e.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    news_bar.setVisibility(View.GONE);
                    news_e.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                } catch (IOException e) {
                    news_bar.setVisibility(View.GONE);
                    news_e.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                } catch (JSONException e) {
                    news_bar.setVisibility(View.GONE);
                    news_e.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                }
                return newsArr;
            }
            @Override
            public void onPostExecute(JSONArray newsJson) {
                List<HashMap<String, String>> newsList = new ArrayList<HashMap<String, String>>();
                final ArrayList<String> nUrl = new ArrayList<String>();
                ArrayList<String> nTitle = new ArrayList<String>();
                ArrayList<String> nAuthor = new ArrayList<String>();
                ArrayList<String> nDate = new ArrayList<String>();
                TextView news_e = getActivity().findViewById(R.id.news_e);
                ProgressBar news_bar = getActivity().findViewById(R.id.newsbar);

                try {
                    for (int i = 0; i < 5; i++) {
                        JSONArray obj = newsJson.getJSONArray(i);
                        nUrl.add(obj.getString(1));
                        nTitle.add(obj.getString(0));
                        nAuthor.add(obj.getString(2));
                        nDate.add(obj.getString(3));
                        //news map
                        HashMap<String, String> newsMap = new HashMap<String, String>();
                        newsMap.put("newsTitle", nTitle.get(i));
                        newsMap.put("newsAuthor", "Author: " + nAuthor.get(i));
                        newsMap.put("newsDate", "Date: " + nDate.get(i));
                        newsList.add(newsMap);
                    }
                } catch (JSONException e) {
                    news_bar.setVisibility(View.GONE);
                    news_e.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                }
                SimpleAdapter newsAdapter = new SimpleAdapter(getContext(), newsList, R.layout.news_list,
                        new String[]{"newsTitle", "newsAuthor", "newsDate"}, new int[]{R.id.n_title, R.id.n_author, R.id.n_date});

                //put stock detail into proper tab
                ListView newsListView = getActivity().findViewById(R.id.newsFeed);
                newsListView.setAdapter(newsAdapter);

                //click news

                newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View rowView,
                    int position, long id){
                        Uri uri;
                        uri = Uri.parse(nUrl.get(position));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);


                    }
                });
                news_bar.setVisibility(View.GONE);

            }
        }
    }
}
