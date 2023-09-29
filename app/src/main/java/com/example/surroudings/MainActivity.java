package com.example.surroudings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onSwitchBusca(View view){
        setContentView(R.layout.tela_busca);
    }

    public void onSwitchPrincipal(View view){
        setContentView(R.layout.activity_main);
    }
}