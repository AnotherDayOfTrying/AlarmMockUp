package com.ijfh.alarmmockup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NewUserActivity extends AppCompatActivity {
    public final static String USER_NAME = "com.ijfh.universalalarm.newuser";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_user_activity);


        final EditText userName = findViewById(R.id.new_user_name);
        Button setName = findViewById(R.id.new_user_set_button);

        setName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = userName.getText().toString();
                Intent replyIntent = new Intent();
                replyIntent.putExtra(USER_NAME, name);
                setResult(RESULT_OK, replyIntent);
                finish();
            }
        });
    }
}
