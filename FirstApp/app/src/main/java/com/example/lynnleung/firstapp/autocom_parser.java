package com.example.lynnleung.firstapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lynnleung on 2017/11/24.
 */

public class autocom_parser {

    static InputStream in;
    static JSONArray jsonArr;

    public autocom_parser(){}
    public JSONArray getautoData(String url) throws Exception{
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

}
