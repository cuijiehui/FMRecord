package com.khmer.fm.fmdemotomodel;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.khmer.fm.adnroid_recordd.record.RecordActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toRecord(View view) {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.putExtra("language",1.0);
        startActivity(intent);
    }
}
