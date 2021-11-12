package com.example.loggerv4;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

//Classe geral para o serviço utilizando os sensores
public class SensorService extends Service implements SensorEventListener {
    //Definindo as váriaveis gerais

    public List<Worker> threads = new ArrayList<Worker>();
    private Context ctx;
    private SensorManager sensorManager;
    //Váriaveis dos sensores, crie mais se necessário
    public float valorProx;
    public float valorPres;

    //Funções para chamar a classe (Fundamentais)
    public SensorService(Context applicationContext) {
        super();
        ctx = applicationContext;
    }
    public SensorService() {
    }

    //Métodos básicos do service (Fundamentais)
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //Método do service: executa apenas uma vez ao iniciar a aplicação
    @Override
    public void onCreate() {
        super.onCreate();
    }
    //Método do service: executa toda vez que o service for chamado
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Condição para começar a thread secundária
        if (threads.size() <1) {
            Worker w = new Worker(startId);
            w.start();
            threads.add(w);
        }
        //Função para registrar quais sensores queremos "analisar", pode acrescentar mais basta copiar
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), 3);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), 3);

        return (super.onStartCommand(intent,flags,startId));
    }
    //Método do service: o que faz quando o serviço é encerrado
    @Override
    public void onDestroy() {
        super.onDestroy();
        //Método para encerrar as threads
        for(int i = 0, tam = threads.size(); i < tam;i++){
            threads.get(i).ativo = false;
        }

    }
    //Requisição para parar o serviço
    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }
    //Método para leitura do sensor (Ler README)
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Atribuindo valores as váriaveis de leitura (Crie mais se necessário)
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            valorPres = event.values[0];
        }
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            valorProx = event.values[0];
        }
    }
    //Método para leitura do sensor (Fundamental)
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    //Criação de uma classe de thread (Ler README)
    class Worker extends Thread{
        int startId;
        public boolean ativo = true;
        public Worker(int startId){
            this.startId = startId;
        }
        public void run(){

            while (ativo){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("Coleta", "Pressão: "+valorPres);
                Log.d("Coleta", "Proximidade: "+valorProx);
            }
            stopSelf(startId);
        }
    }
}
