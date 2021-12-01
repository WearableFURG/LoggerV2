package com.example.loggerv4;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.loggerv4.databinding.ActivityMainBinding;

public class MainActivity extends Activity {
    //Definindo aqui algumas váriaveis para começar nosso serviço em background
    Intent mServiceIntent;
    private SensorService mSensorService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermissions(new String[]{Manifest.permission.BODY_SENSORS, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        //Comando básico para atribuir nosso activity_main.xml como o padrão da interface
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        //Chamando e iniciando nosso serviço em background
        mSensorService = new SensorService(this);
        mServiceIntent = new Intent(this, mSensorService.getClass());
        startService(mServiceIntent);
    }
}