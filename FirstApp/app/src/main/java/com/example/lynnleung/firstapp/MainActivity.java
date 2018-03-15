package com.example.lynnleung.firstapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Switch;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.facebook.FacebookSdk;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public class at_format{
        String at_symbol;
        String at_info;
    }
    //fav list sp
    public Context mContext=this;
    public SharedPreferences fav_sp;/*= getSharedPreferences("favlist",Context.MODE_PRIVATE)*/
    public SharedPreferences.Editor editor;
    public ArrayList<HashMap<String, String>> favMap = new ArrayList<HashMap<String, String>>();
    public HashSet<String> symSet;
    public ProgressBar fav_bar;
    public ListView favListView;
    public SimpleAdapter favAdapter;
    ArrayList<at_format> atc_list;
    AutoCompleteTextView atc_con;
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        favListView = findViewById(R.id.fav_con);
        registerForContextMenu(favListView);
        fav_bar = findViewById(R.id.favbar);
        //shared preference-favlist
        fav_sp = getSharedPreferences("favlist",MODE_PRIVATE);
        editor = fav_sp.edit();
        symSet = (HashSet<String>) fav_sp.getStringSet("symbol",new HashSet<String>());
        createFavlist(symSet);


        //autocomplete
        atc_con=findViewById(R.id.autocomplete);
        /*atc_con.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                String auto_Url;
                String input;
                if (atc_con.isPerformingCompletion()) {
                    return;
                }
                Log.i("oTC: CharSequence", s.toString());
                    input=s.toString();
                    auto_Url="http://csci571-hw8php.us-west-1.elasticbeanstalk.com/index.php/?input="+input;
                    new getAutocomplete().execute(auto_Url);


            }

            @Override
            public void afterTextChanged(Editable s){}

        });
    }

    private class getAutocomplete extends AsyncTask<String, Void, JSONArray> {
        JSONArray test=null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONArray doInBackground(String... urls) {
            try {
                return getautoData(urls[0]);
            } catch (Exception e) {
                return test;
            }
            *//*autocom_parser autoParser = new autocom_parser();
            JSONArray autoArr=null;
            try {
                autoArr = autoParser.getautoData(urls[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return autoArr;*//*
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            //atc_list.clear();
            for (int i = 0; i < 5; i++) {
                at_format data = new at_format();
                try {
                    data.at_symbol = ((JSONObject) result.get(i)).get("Symbol").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    data.at_info = ((JSONObject) result.get(i)).get("Name").toString() + "(" + ((JSONObject) result.get(i)).get("Exchange").toString() + ")";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                atc_list.add(data);
            }
            //auto list element
            final String[] autoItem = new String[atc_list.size()];
            for (int j = 0; j < atc_list.size(); j++) {
                at_format temp = atc_list.get(j);
                autoItem[j] = temp.at_symbol + "-" + temp.at_info;
            }
            //set adapter
            ArrayAdapter<String> autoaAapter;
            autoaAapter = new ArrayAdapter<String>(MainActivity.this,R.layout.auto_complete,autoItem);
            autoaAapter.notifyDataSetChanged();
            atc_con.setThreshold(2);
            atc_con.setAdapter(autoaAapter);
            atc_con.setThreshold(2);
            autoaAapter.notifyDataSetChanged();
            atc_con.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                        long arg3) {
                    // TODO Auto-generated method stub
                    String[] data2 = autoItem[pos].split("-");
                    atc_con.setText(data2[0]);
                    *//*searchOnClick(search);*//*
                }
            });
        }*/


        //refresh btn
        ImageButton refreshButton = (ImageButton)findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fav_bar.setVisibility(View.VISIBLE);
                createFavlist(symSet);
            }
        });

        //clear btn
        Button clearBtn = findViewById(R.id.clear);
        clearBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                atc_con.setText("");
            }
        });
    }

    private JSONArray getautoData(String url) throws Exception{
        InputStream in=null;
        JSONArray jsonArr=null;
        try {
            URL acurl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) acurl.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("fail to request url");
            }
            in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            String ac_str = "";
            while ((line = reader.readLine()) != null) {
                ac_str += line;
            }
            in.close();
            try{
                jsonArr=new JSONArray(ac_str);
            }catch (JSONException e){
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
            return jsonArr;
        } finally {
            if(in!=null) {
                in.close();

            }
        }
    }
    public void createFavlist(Set<String> set){
        favMap.clear();
            getFavitem ft = new getFavitem();
            ft.execute(set);
    }

    public void autoRefresh(final View view) {

        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Switch autoToggle = (Switch)view;
                if(autoToggle != null && autoToggle.isChecked()) {
                    fav_bar.setVisibility(View.VISIBLE);
                    createFavlist(symSet);
                    handler.postDelayed(this, 10000);
                }
            }
        };

        handler.postDelayed(runnable, 100);
    }

    public void getQoute(View view) {
        if (atc_con.getText().toString() == "" || atc_con.getText().toString().trim().length() == 0){
            Toast.makeText(MainActivity.this, "Please enter a stock name or a symbol",
                    Toast.LENGTH_SHORT).show();
        }

        //post symbol
        else{
            Intent intent = new Intent(this, DisplayStockResult.class);
            String symbol = atc_con.getText().toString();
            intent.putExtra(EXTRA_MESSAGE, symbol);
            startActivity(intent);
        }

    }

    private class getFavitem extends AsyncTask<Set<String>, Void, JSONArray> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONArray doInBackground(Set<String>... sets) {
            Set<String> stockSymbol = sets[0];
            JSONArray jsonArray = new JSONArray();
            Iterator iterator = stockSymbol.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                String item = (String) iterator.next();
                String currUrl = "http://csci571-hw8php.us-west-1.elasticbeanstalk.com/index.php/?symbol=" + item;
                URL surl = null;
                InputStream in;
                JSONObject stcArr = new JSONObject();
                try {
                    surl = new URL(currUrl);
                    HttpURLConnection conn = (HttpURLConnection) surl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    if (conn.getResponseCode() != 200) {
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
                    /*return stcArr;*/
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    jsonArray.put(i,stcArr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                i++;

                /*return stcArr;*/
            }
            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);
            fav_bar.setVisibility(View.VISIBLE);


            int length = jsonArray.length();
            for(int i = 0; i < length; i++) {
                float ch = 0;
                try {
                    JSONObject stcArr = jsonArray.getJSONObject(i);
                    JSONObject meta = stcArr.getJSONObject("Meta Data");
                    String sym = meta.getString("2. Symbol");
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
                    ch = c1 - c2;
                    float pre = (ch / c2) * 100;
                    close1 = df.format(c1);
                    String change = df.format(ch);
                    String precent = df.format(pre);

                    //draw fav map
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Symbol", sym);
                    map.put("Price", close1);
                    map.put("Change", change + "(" + precent + "%)");
                    favMap.add(map);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            favAdapter = new SimpleAdapter(getBaseContext(), favMap, R.layout.fav_list,
                    new String[]{"Symbol", "Price", "Change"}, new int[]{R.id.fav_symbol, R.id.fav_price, R.id.fav_change});

            //put stock detail into proper tab
           /* ListView favListView = findViewById(R.id.fav_con);*/
            favListView.setAdapter(favAdapter);

            //click fav item

            favListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> adapterView, View rowView,
                                        int position, long id){
                    TextView symbol = (TextView)rowView.findViewById(R.id.fav_symbol);
                    String quoteSym = symbol.getText().toString();
                    Intent intent = new Intent(MainActivity.this, DisplayStockResult.class);
                    intent.putExtra(EXTRA_MESSAGE, quoteSym);
                    startActivity(intent);
                }
            });
            fav_bar.setVisibility(View.GONE);

        }
    }

    //longclick

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Remove from Favorites");
        menu.add(0, 0, 0, "No");
        menu.add(0, 1, 0, "Yes");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos =  info.position;
        switch (item.getItemId()) {
            case 0:
                //nothing
                return super.onContextItemSelected(item);
            case 1:
                //delete
                HashMap temp = favMap.get(pos);
                symSet.remove(temp.get("Symbol"));
                editor.clear();
                editor.putStringSet("symbol",symSet);
                editor.commit();
                favMap.remove(pos);
                favAdapter.notifyDataSetChanged();


                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}


