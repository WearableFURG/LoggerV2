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
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.Nullable;
import android.provider.Settings.Secure;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

// Imports Volley (Web Service)
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

//Classe geral para o serviço utilizando os sensores
public class SensorService extends Service implements SensorEventListener {
    //Definindo as váriaveis gerais

    public List<Worker> threads = new ArrayList<Worker>();
    private Context ctx;
    private SensorManager sensorManager;

    // Variável ID android
    String androidID;

    // Criação global do objeto JSON
    JSONObject tSensores = new JSONObject();

    //Váriaveis dos sensores, crie mais se necessário
    public float valorPres;
    public float valorLux;
    public float valorBPM;

    public float valorSPO2;
    public float valorECG;
    public float valorPPG;

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

    public List<Float> listBPM = new ArrayList<Float>();
    public List<Float> listPPG = new ArrayList<Float>();
    public List<Float> listECG = new ArrayList<Float>();
    public List<Float> listSPO2 = new ArrayList<Float>();

    public List<String> listID = new ArrayList();
    public List<String> listTime = new ArrayList();

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
        // Adquirindo ID android
        androidID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        // Coloca o valor do ID apenas uma vez no objeto JSON
        JSONArray arID = new JSONArray();
        listID.add(androidID);
        arID.put(listID);
        try {
            tSensores.put("ID_smartwatch",arID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Método POST
    public void webPost(JSONObject postData){
        String postUrl = "https://reqres.in/api/users";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, postUrl, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    //Método GET
    public void webGet(){
        String url = "https://reqres.in/api/users/2";
        List<String> jsonResponses = new ArrayList<>();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    for(int i = 0; i < jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        // Printa o objeto JSON
                        System.out.println(jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);
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
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE), 3);

        //sensorManager.registerListener(this, sensorManager.getDefaultSensor(ecg), 3);
        //sensorManager.registerListener(this, sensorManager.getDefaultSensor(ppg), 3);
        //sensorManager.registerListener(this, sensorManager.getDefaultSensor(spo2), 3);
        return START_STICKY;
    }
    //Método do service: o que faz quando o serviço é encerrado
    @Override
    public void onDestroy() {
        super.onDestroy();
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
            valorECG = 0;
            valorPPG = 0;
            valorSPO2 = 0;
        }
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            valorBPM = event.values[0];
        }
        /*
        if (event.sensor.getType() == Sensor.ecg) {
            valorEGC = (int) event.values[0];
        }
        if (event.sensor.getType() == Sensor.ppg) {
            valorPPG = (int) event.values[0];
        }
        if (event.sensor.getType() == Sensor.spo2) {
            valorSPO2 = (int) event.values[0];
        }
        */
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
        int count = 0;
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

                JSONArray arBPM = new JSONArray();
                JSONArray arECG = new JSONArray();
                JSONArray arPPG = new JSONArray();
                JSONArray arSPO2 = new JSONArray();

                JSONArray arAceZ = new JSONArray();
                JSONArray arAceX = new JSONArray();
                JSONArray arAceY = new JSONArray();

                JSONArray arGirZ = new JSONArray();
                JSONArray arGirX = new JSONArray();
                JSONArray arGirY = new JSONArray();
                JSONArray arTime = new JSONArray();

                listLux.add(valorLux);
                listPres.add(valorPres);

                listBPM.add(valorBPM);
                listPPG.add(valorPPG);
                listECG.add(valorECG);
                listSPO2.add(valorSPO2);

                listAceZ.add(valorAceZ);
                listAceX.add(valorAceX);
                listAceY.add(valorAceY);

                listGirZ.add(valorGirZ);
                listGirX.add(valorGirX);
                listGirY.add(valorGirY);

                listTime.add(Calendar.getInstance().getTime().toString());

                //Objeto JSON que irá unir as listas

                try {
                    Thread.sleep(1000); //Aqui é feito o controle do tempo em que será armazenado no json os dados do sensor
                    Log.d("TAG", "run: "+count);
                    // Checkpoint para fazer o POST via web
                    if(count % 15 == 0){
                        // Chama o método POST com o objeto JSON
                        webPost(tSensores);
                    }
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    //Coloca os arrays normais dentro do Array JSON
                    arPres.put(listPres);
                    arLux.put(listLux);

                    arBPM.put(listBPM);
                    arPPG.put(listPPG);
                    arSPO2.put(listSPO2);
                    arECG.put(listECG);

                    arAceZ.put(listAceZ);
                    arAceX.put(listAceX);
                    arAceY.put(listAceY);

                    arGirZ.put(listGirZ);
                    arGirX.put(listGirX);
                    arGirY.put(listGirY);

                    arTime.put(listTime);

                    //Coloca os arrays JSON dentro dos objetos
                    tSensores.put("Tempo",arTime);
                    tSensores.put("Pressao",arPres);
                    tSensores.put("Lux",arLux);

                    tSensores.put("BPM",arBPM);
                    tSensores.put("ECG",arECG);
                    tSensores.put("PPG",arPPG);
                    tSensores.put("SPO2",arSPO2);

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
                    File root = new File("/storage/","emulated/0/Documents");
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