package com.khmer.fm.adnroid_recordd.record;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;


import com.khmer.fm.adnroid_recordd.R;
import com.khmer.fm.adnroid_recordd.eventbus.EditTextEventbus;

import org.greenrobot.eventbus.EventBus;

public class EditTextActivity extends AppCompatActivity {
    private EditText etTest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        String text = getIntent().getStringExtra("EditText");
        etTest = findViewById(R.id.et_test);
        etTest.setText(text);
    }

    public void CompleteText(View view) {
        String text = etTest.getText().toString();
        EditTextEventbus editTextEventbus = new EditTextEventbus(text);
        EventBus.getDefault().post(editTextEventbus);
        finish();
    }

    public void toBack(View view) {
        finish();
    }
}
