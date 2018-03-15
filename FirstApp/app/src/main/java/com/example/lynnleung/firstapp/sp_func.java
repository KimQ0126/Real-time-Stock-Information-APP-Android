package com.example.lynnleung.firstapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lynnleung on 2017/11/27.
 */

public class sp_func {
    private Context mContext;

    public sp_func() {
    }

    public sp_func(Context mContext) {
        this.mContext = mContext;
    }


    //save data
    public void save(String symbol, String lastP, String change, String changeP) {
        SharedPreferences sp = mContext.getSharedPreferences("favList", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("symbol", symbol);
        editor.putString("lastP", lastP);
        editor.putString("change", change);
        editor.putString("changeP", changeP);
        editor.commit();

    }

    //read data
    public Map<String, String> read() {
        Map<String, String> data = new HashMap<String, String>();
        SharedPreferences sp = mContext.getSharedPreferences("favList", Context.MODE_PRIVATE);
        data.put("symbol", sp.getString("symbol", ""));
        data.put("lastP", sp.getString("lastP", ""));
        data.put("change", sp.getString("change", ""));
        data.put("changeP", sp.getString("changeP", ""));
        return data;
    }
}
