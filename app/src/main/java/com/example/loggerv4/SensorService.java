package com.example.loggerv4;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//Classe geral para o serviço utilizando os sensores
public class SensorService extends Service implements SensorEventListener {
    //Definindo as váriaveis gerais

    public List<Worker> threads = new ArrayList<Worker>();
    private Context ctx;
    private SensorManager sensorManager;
    //Váriaveis dos sensores, crie mais se necessário
    public float valorPres;
    public float valorLux;

    public float valorGirZ;
    public float valorGirX;
    public float valorGirY;

    public float valorAceZ;
    public float valorAceX;
    public float valorAceY;

    public List<Float> listAceZ = new ArrayList<Float>();
    public List<Float> listAceX = new ArrayList<Float>();
    public List<Float> listAceY = new ArrayList<Float>();

    public List<Float> listGirZ = new ArrayList<Float>();
    public List<Float> listGirX = new ArrayList<Float>();
    public List<Float> listGirY = new ArrayList<Float>();

    public List<Float> listPres = new ArrayList<Float>();
    public List<Float> listLux = new ArrayList<Float>();

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
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), 3);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 3);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 3);

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
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            valorLux = event.values[0];
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            valorAceZ = event.values[0];
            valorAceX = event.values[1];
            valorAceY = event.values[2];
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            valorGirZ = event.values[0];
            valorGirX = event.values[1];
            valorGirY = event.values[2];
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
            //Loop da nossa thread
            while (ativo){
                //Aqui começa a preparação do JSON

                //Criação dos arrays de JSON onde os dados dos sensores serão armazenados
                JSONArray arPres = new JSONArray();
                JSONArray arLux = new JSONArray();

                JSONArray arAceZ = new JSONArray();
                JSONArray arAceX = new JSONArray();
                JSONArray arAceY = new JSONArray();

                JSONArray arGirZ = new JSONArray();
                JSONArray arGirX = new JSONArray();
                JSONArray arGirY = new JSONArray();

                listLux.add(valorLux);
                listPres.add(valorPres);

                listAceZ.add(valorAceZ);
                listAceX.add(valorAceX);
                listAceY.add(valorAceY);

                listGirZ.add(valorGirZ);
                listGirX.add(valorGirX);
                listGirY.add(valorGirY);

                //Objeto JSON que irá unir as listas
                JSONObject tSensores = new JSONObject();

                try {
                    Thread.sleep(1000); //Aqui é feito o controle do tempo em que será armazenado no json os dados do sensor
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    //Coloca os arrays normais dentro do Array JSON
                    arPres.put(listPres);
                    arLux.put(listLux);

                    arAceZ.put(listAceZ);
                    arAceX.put(listAceX);
                    arAceY.put(listAceY);

                    arGirZ.put(listGirZ);
                    arGirX.put(listGirX);
                    arGirY.put(listGirY);

                    //Coloca os arrays JSON dentro dos objetos
                    tSensores.put("Pressao",arPres);
                    tSensores.put("Lux",arLux);

                    tSensores.put("AcelerometroZ",arAceZ);
                    tSensores.put("AcelerometroX",arAceX);
                    tSensores.put("AcelerometroY",arAceY);

                    tSensores.put("GiroscopioZ",arGirZ);
                    tSensores.put("GiroscopioX",arGirX);
                    tSensores.put("GiroscopioY",arGirY);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Criação do arquivo JSON
                try {
                    //Definindo o local em que o arquivo será colocado
                    //O emulated/0[..] diz respeito ao endereço do WearOS no emulador
                    //Logo, basta colocar o diretório real ao usar no smarthwatch
                    File root = new File(Environment.getStorageDirectory(),"emulated/0/Documents");
                    if (!root.exists()){
                        root.mkdir();
                    }
                    //Criando o arquivo em si
                    File filepath = new File(root,"logger.json");

                    //Inserindo o objeto JSON dentro do arquivo
                    FileWriter writer = new FileWriter(filepath);
                    writer.append(tSensores.toString());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            stopSelf(startId);
        }
    }
}
