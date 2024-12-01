package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import androidx.appcompat.app.AppCompatActivity;

public class H_meus_palpites_serie_A extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private AdView adView;
    private final String selectedSerie = "SerieA";
    private String pontoDeCorte;
    private String ano;
    private String meusPalpitesBackup;
    private List<String> listTimes;
    private List<String> listSeries;
    private Boolean toqueNaTela = false;
    private Spinner dropdown_series;
    private Spinner timeCampeao;
    private Spinner timeLanterna;
    private Spinner rebaixado01;
    private Spinner rebaixado02;
    private Spinner rebaixado03;
    private Spinner libertadores01;
    private Spinner libertadores02;
    private Spinner libertadores03;
    private Spinner libertadores04;
    private Spinner libertadores05;
    private Spinner sulamericana01;
    private Spinner sulamericana02;
    private Spinner sulamericana03;
    private Spinner sulamericana04;
    private Spinner sulamericana05;
    private Spinner sulamericana06;
    private Properties properties;
    private boolean redeDisponivel;
    private File dirFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.h_meus_palpites_serie_a);
        loadAds();
        dirFiles = getDir("files",MODE_PRIVATE);
        redeDisponivel = Z_static_functions.verificandoRede(this) != 0;
        properties = new Properties();
        getDadosComuns();
        setPontoDeCorte();
        resgataArrays();
        implementaSpinners();
        atualizaCampos();
        setOnClickViews();
    }

    //-----Início Bloco Exibição de Anúncios-----
    private void loadAds(){
        adView = findViewById(R.id.adsPalpitesSerieA);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onPause() {
        if (adView != null){
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (adView != null){
            adView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (adView != null){
            adView.destroy();
        }
        super.onDestroy();
    }
//-----Fim Bloco Exibição de Anúncios-----

    private void getDadosComuns(){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        ano = getDadosComuns.getString("ano","--");
    }

    private void setPontoDeCorte(){
        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
        int totalRodadasNaFase = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + selectedSerie + ano + "F1" + "TotalRodadasNaFase", "1")));
        pontoDeCorte = "R"+(totalRodadasNaFase/2+1)+"J1";
        Log.v("pontoDeCorte","Ponto de corte: "+pontoDeCorte);
    }

    private void resgataArrays(){
        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
        int i = 0;
        while(!Objects.equals(dadosCampeonatos.getString("dadosCampeonatoAno" + i,"--"), "--")){
            i++;
        }
        listSeries = Arrays.asList(getResources().getStringArray(R.array.series_array));
        listTimes = new ArrayList<>();
        SharedPreferences times = getSharedPreferences("times"+selectedSerie+ano,MODE_PRIVATE);
        Map<String,?> allEntries = times.getAll();
        Properties properties = new Properties();
        int numTimes = 0;
        for(Map.Entry<String,?> entry : allEntries.entrySet()){
            if (entry.getKey().contains("time")) {
                properties.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
                numTimes++;
            }
        }
        for (int k = 0; k<numTimes; k++){
            listTimes.add(properties.getProperty("time"+k));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void implementaSpinners(){
        //Implementa o dropdown de escolha da série.
        dropdown_series = findViewById(R.id.dropdown_series);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listSeries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_series.setAdapter(adapter);
        dropdown_series.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_series.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown de escolha do time campeão.
        timeCampeao = findViewById(R.id.dropdown_time_campeao);
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeCampeao.setAdapter(timeAdapter);
        timeCampeao.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        timeCampeao.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown libertadores01.
        libertadores01 = findViewById(R.id.dropdown_libertadores01);
        ArrayAdapter<String> libertadores01Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        libertadores01Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        libertadores01.setAdapter(libertadores01Adapter);
        libertadores01.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        libertadores01.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown libertadores02.
        libertadores02 = findViewById(R.id.dropdown_libertadores02);
        ArrayAdapter<String> libertadores02Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        libertadores02Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        libertadores02.setAdapter(libertadores02Adapter);
        libertadores02.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        libertadores02.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown libertadores03.
        libertadores03 = findViewById(R.id.dropdown_libertadores03);
        ArrayAdapter<String> libertadores03Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        libertadores03Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        libertadores03.setAdapter(libertadores03Adapter);
        libertadores03.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        libertadores03.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown libertadores04.
        libertadores04 = findViewById(R.id.dropdown_libertadores04);
        ArrayAdapter<String> libertadores04Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        libertadores04Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        libertadores04.setAdapter(libertadores04Adapter);
        libertadores04.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        libertadores04.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown libertadores05.
        libertadores05 = findViewById(R.id.dropdown_libertadores05);
        ArrayAdapter<String> libertadores05Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        libertadores05Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        libertadores05.setAdapter(libertadores05Adapter);
        libertadores05.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        libertadores05.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown sulamericana01.
        sulamericana01 = findViewById(R.id.dropdown_sulamericana01);
        ArrayAdapter<String> sulamericana01Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        sulamericana01Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sulamericana01.setAdapter(sulamericana01Adapter);
        sulamericana01.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        sulamericana01.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown sulamericana02.
        sulamericana02 = findViewById(R.id.dropdown_sulamericana02);
        ArrayAdapter<String> sulamericana02Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        sulamericana02Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sulamericana02.setAdapter(sulamericana02Adapter);
        sulamericana02.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        sulamericana02.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown sulamericana03.
        sulamericana03 = findViewById(R.id.dropdown_sulamericana03);
        ArrayAdapter<String> sulamericana03Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        sulamericana03Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sulamericana03.setAdapter(sulamericana03Adapter);
        sulamericana03.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        sulamericana03.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown sulamericana04.
        sulamericana04 = findViewById(R.id.dropdown_sulamericana04);
        ArrayAdapter<String> sulamericana04Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        sulamericana04Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sulamericana04.setAdapter(sulamericana04Adapter);
        sulamericana04.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        sulamericana04.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown sulamericana05.
        sulamericana05 = findViewById(R.id.dropdown_sulamericana05);
        ArrayAdapter<String> sulamericana05Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        sulamericana05Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sulamericana05.setAdapter(sulamericana05Adapter);
        sulamericana05.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        sulamericana05.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown sulamericana06.
        sulamericana06 = findViewById(R.id.dropdown_sulamericana06);
        ArrayAdapter<String> sulamericana06Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        sulamericana06Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sulamericana06.setAdapter(sulamericana06Adapter);
        sulamericana06.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        sulamericana06.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown rebaixado01.
        rebaixado01 = findViewById(R.id.dropdown_rebaixado01);
        ArrayAdapter<String> rebaixado01Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        rebaixado01Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rebaixado01.setAdapter(rebaixado01Adapter);
        rebaixado01.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        rebaixado01.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown rebaixado02.
        rebaixado02 = findViewById(R.id.dropdown_rebaixado02);
        ArrayAdapter<String> rebaixado02Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        rebaixado02Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rebaixado02.setAdapter(rebaixado02Adapter);
        rebaixado02.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        rebaixado02.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown rebaixado03.
        rebaixado03 = findViewById(R.id.dropdown_rebaixado03);
        ArrayAdapter<String> rebaixado03Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        rebaixado03Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rebaixado03.setAdapter(rebaixado03Adapter);
        rebaixado03.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        rebaixado03.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown time lanterna.
        timeLanterna = findViewById(R.id.dropdown_time_lanterna);
        ArrayAdapter<String> timeLanternaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        timeLanternaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeLanterna.setAdapter(timeLanternaAdapter);
        timeLanterna.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        timeLanterna.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)
    }

    private void atualizaCampos(){
        int p;
        SharedPreferences prefs = getSharedPreferences("meusPalpites"+selectedSerie+ano, MODE_PRIVATE);
        if(!prefs.contains("arquivo")){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("arquivo", "criado");
            editor.apply();
        }
        //Atualiza a seleção da série.
        String serie = "Série A";
        dropdown_series.setSelection(listSeries.indexOf(serie));
        //Atualiza o time campeão.
        String time_campeao = prefs.getString("palpiteTimeCampeaoSerieA","--");
        p = listTimes.indexOf(time_campeao);
        timeCampeao.setSelection(p);
        //Atualiza pontos do time campeão.
        String pontos_time_campeao = prefs.getString("palpitePontosTimeCampeaoSerieA","--");
        EditText pontosTimeCampeao = findViewById(R.id.edit_pontos_campeao);
        pontosTimeCampeao.setText(pontos_time_campeao);
        //Atualiza saldo do time campeão.
        String saldo_time_campeao = prefs.getString("palpiteSaldoTimeCampeaoSerieA","--");
        EditText saldoTimeCampeao = findViewById(R.id.edit_saldo_campeao);
        saldoTimeCampeao.setText(saldo_time_campeao);
        //Atualiza o time libertadores01.
        String libertadores_01 = prefs.getString("palpiteLibertadores01SerieA","--");
        p = listTimes.indexOf(libertadores_01);
        libertadores01.setSelection(p);
        //Atualiza o time libertadores02.
        String libertadores_02 = prefs.getString("palpiteLibertadores02SerieA","--");
        p = listTimes.indexOf(libertadores_02);
        libertadores02.setSelection(p);
        //Atualiza o time libertadores03.
        String libertadores_03 = prefs.getString("palpiteLibertadores03SerieA","--");
        p = listTimes.indexOf(libertadores_03);
        libertadores03.setSelection(p);
        //Atualiza o time libertadores04.
        String libertadores_04 = prefs.getString("palpiteLibertadores04SerieA","--");
        p = listTimes.indexOf(libertadores_04);
        libertadores04.setSelection(p);
        //Atualiza o time libertadores05.
        String libertadores_05 = prefs.getString("palpiteLibertadores05SerieA","--");
        p = listTimes.indexOf(libertadores_05);
        libertadores05.setSelection(p);
        //Atualiza o time sulamericana01.
        String sulamericana_01 = prefs.getString("palpiteSulamericana01SerieA","--");
        p = listTimes.indexOf(sulamericana_01);
        sulamericana01.setSelection(p);
        //Atualiza o time sulamericana02.
        String sulamericana_02 = prefs.getString("palpiteSulamericana02SerieA","--");
        p = listTimes.indexOf(sulamericana_02);
        sulamericana02.setSelection(p);
        //Atualiza o time sulamericana03.
        String sulamericana_03 = prefs.getString("palpiteSulamericana03SerieA","--");
        p = listTimes.indexOf(sulamericana_03);
        sulamericana03.setSelection(p);
        //Atualiza o time sulamericana04.
        String sulamericana_04 = prefs.getString("palpiteSulamericana04SerieA","--");
        p = listTimes.indexOf(sulamericana_04);
        sulamericana04.setSelection(p);
        //Atualiza o time sulamericana05.
        String sulamericana_05 = prefs.getString("palpiteSulamericana05SerieA","--");
        p = listTimes.indexOf(sulamericana_05);
        sulamericana05.setSelection(p);
        //Atualiza o time sulamericana06.
        String sulamericana_06 = prefs.getString("palpiteSulamericana06SerieA","--");
        p = listTimes.indexOf(sulamericana_06);
        sulamericana06.setSelection(p);
        //Atualiza o time rebaixado01.
        String rebaixado_01 = prefs.getString("palpiteRebaixado01SerieA","--");
        p = listTimes.indexOf(rebaixado_01);
        rebaixado01.setSelection(p);
        //Atualiza o time rebaixado02.
        String rebaixado_02 = prefs.getString("palpiteRebaixado02SerieA","--");
        p = listTimes.indexOf(rebaixado_02);
        rebaixado02.setSelection(p);
        //Atualiza o time rebaixado03.
        String rebaixado_03 = prefs.getString("palpiteRebaixado03SerieA","--");
        p = listTimes.indexOf(rebaixado_03);
        rebaixado03.setSelection(p);
        //Atualiza o time timeLanterna.
        String time_lanterna = prefs.getString("palpiteTimeLanternaSerieA","--");
        p = listTimes.indexOf(time_lanterna);
        timeLanterna.setSelection(p);
        //Atualiza pontos do time lanterna.
        String pontos_time_lanterna = prefs.getString("palpitePontosTimeLanternaSerieA","--");
        EditText pontosTimeLanterna = findViewById(R.id.edit_pontos_lanterna);
        pontosTimeLanterna.setText(pontos_time_lanterna);
        //Atualiza saldo do time lanterna.
        String saldo_time_lanterna = prefs.getString("palpiteSaldoTimeLanternaSerieA","--");
        EditText saldoTimeLanterna = findViewById(R.id.edit_saldo_lanterna);
        saldoTimeLanterna.setText(saldo_time_lanterna);

        SharedPreferences meusDados = getSharedPreferences("meusDados",MODE_PRIVATE);
        meusPalpitesBackup = "meusPalpites"+selectedSerie+ano+"Backup"+meusDados.getString("meuNumero","--");
        Map<String,?> allEntries = prefs.getAll();
        for(Map.Entry<String,?> entry : allEntries.entrySet()){
            properties.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
        }

    }

    private boolean configuracoesAutomaticas(){
        boolean dataHoraAutomatico = false,fusoAutomatico = false;
        try {
            dataHoraAutomatico = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME) == 1;
            fusoAutomatico = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE) == 1;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return dataHoraAutomatico && fusoAutomatico;
    }

    private boolean palpitePermitido(){
        SharedPreferences lerData = getSharedPreferences("data" + selectedSerie + ano, MODE_PRIVATE);
        String dataLida = lerData.getString("resultado" + selectedSerie + pontoDeCorte + "data", "Data");
        boolean permitido = true;
        if (configuracoesAutomaticas()) {
            int dataDeCorte = 0;
            Date momento = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterDDD = new SimpleDateFormat("DDD");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterData = new SimpleDateFormat("EEE, dd/MM/yyyy");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterAno = new SimpleDateFormat("yyyy");
            int dataHoje = Integer.parseInt(formatterDDD.format(momento));

            if (dataLida.equals("Data")||dataLida.equals("data")||dataLida.equals("DATA")) {
                dataDeCorte = dataHoje + 1;
            }
            else {
                try {
                    Date data = formatterData.parse(dataLida);
                    int anoHoje = Integer.parseInt(formatterAno.format(momento));
                    int anoCorte = Integer.parseInt(formatterAno.format(Objects.requireNonNull(data)));
                    if (anoCorte < anoHoje){
                        permitido = false;
                    }
                    if (anoCorte == anoHoje){
                        dataDeCorte = Integer.parseInt(formatterDDD.format(Objects.requireNonNull(data)));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (dataHoje >= dataDeCorte){
                permitido = false;
            }
        }
        else{
            permitido = false;
        }
        return permitido;
    }

    private void salvarPalpite(String chave, String valor){
        SharedPreferences prefs = getSharedPreferences("meusPalpites"+selectedSerie+ano, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(chave, valor);
        Toast.makeText(this, R.string.palpiteSalvo, Toast.LENGTH_SHORT).show();
        editor.apply();
        toqueNaTela = false;
        
        properties.setProperty(chave, valor);
        try {
            File getFile = new File(dirFiles, meusPalpitesBackup + ".xml");
            FileOutputStream outputStream = new FileOutputStream(getFile);
            properties.storeToXML(outputStream, "meusPalpitesBackup");
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private void setOnClickViews(){
        EditText editPontosCampeao,editSaldoCampeao,editPontosLanterna,editSaldoLanterna;
        editPontosCampeao = findViewById(R.id.edit_pontos_campeao);
        editSaldoCampeao = findViewById(R.id.edit_saldo_campeao);
        editPontosLanterna = findViewById(R.id.edit_pontos_lanterna);
        editSaldoLanterna = findViewById(R.id.edit_saldo_lanterna);
        editPontosCampeao.setOnClickListener(this::setInputData);
        editSaldoCampeao.setOnClickListener(this::setInputData);
        editPontosLanterna.setOnClickListener(this::setInputData);
        editSaldoLanterna.setOnClickListener(this::setInputData);
    }

    //Executa algo a partir da seleção nos EditTexts.
    public void setInputData(View v){
        EditText view = (EditText) v;
        String description = (String) view.getContentDescription();
        String chave = description+selectedSerie;
        String valor = String.valueOf(view.getText());
        salvarPalpite(chave, valor);
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0); //hide keyboard
    }

    //Executa algo a partir da seleção nos spinners.
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Intent serie;
        String chave;
        String valor;
        if(toqueNaTela) {
            if ((parent.getId())==R.id.dropdown_series){
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        serie = new Intent(this, H_meus_palpites_serie_B.class);
                        startActivity(serie);
                        finish();
                        break;
                    case 2:
                        serie = new Intent(this, H_meus_palpites_serie_C.class);
                        startActivity(serie);
                        finish();
                        break;
                    case 3:
                        serie = new Intent(this, H_meus_palpites_serie_D.class);
                        startActivity(serie);
                        finish();
                        break;
                }
            }
            else {
                if (configuracoesAutomaticas()) {
                    if (palpitePermitido()) {
                        List<Object> ids = new ArrayList<>();
                        ids.add(R.id.dropdown_time_campeao);
                        ids.add(R.id.dropdown_libertadores01);
                        ids.add(R.id.dropdown_libertadores02);
                        ids.add(R.id.dropdown_libertadores03);
                        ids.add(R.id.dropdown_libertadores04);
                        ids.add(R.id.dropdown_libertadores05);
                        ids.add(R.id.dropdown_sulamericana01);
                        ids.add(R.id.dropdown_sulamericana02);
                        ids.add(R.id.dropdown_sulamericana03);
                        ids.add(R.id.dropdown_sulamericana04);
                        ids.add(R.id.dropdown_sulamericana05);
                        ids.add(R.id.dropdown_sulamericana06);
                        ids.add(R.id.dropdown_rebaixado01);
                        ids.add(R.id.dropdown_rebaixado02);
                        ids.add(R.id.dropdown_rebaixado03);
                        ids.add(R.id.dropdown_time_lanterna);

                        switch (ids.indexOf(parent.getId())) {//Identifica qual spinner está sendo selecionado.
                            case 0:
                                int selectedPosition = timeCampeao.getSelectedItemPosition();
                                chave = "palpiteTimeCampeao" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 1:
                                selectedPosition = libertadores01.getSelectedItemPosition();
                                chave = "palpiteLibertadores01" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 2:
                                selectedPosition = libertadores02.getSelectedItemPosition();
                                chave = "palpiteLibertadores02" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 3:
                                selectedPosition = libertadores03.getSelectedItemPosition();
                                chave = "palpiteLibertadores03" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 4:
                                selectedPosition = libertadores04.getSelectedItemPosition();
                                chave = "palpiteLibertadores04" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 5:
                                selectedPosition = libertadores05.getSelectedItemPosition();
                                chave = "palpiteLibertadores05" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 6:
                                selectedPosition = sulamericana01.getSelectedItemPosition();
                                chave = "palpiteSulamericana01" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 7:
                                selectedPosition = sulamericana02.getSelectedItemPosition();
                                chave = "palpiteSulamericana02" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 8:
                                selectedPosition = sulamericana03.getSelectedItemPosition();
                                chave = "palpiteSulamericana03" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 9:
                                selectedPosition = sulamericana04.getSelectedItemPosition();
                                chave = "palpiteSulamericana04" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 10:
                                selectedPosition = sulamericana05.getSelectedItemPosition();
                                chave = "palpiteSulamericana05" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 11:
                                selectedPosition = sulamericana06.getSelectedItemPosition();
                                chave = "palpiteSulamericana06" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 12:
                                selectedPosition = rebaixado01.getSelectedItemPosition();
                                chave = "palpiteRebaixado01" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 13:
                                selectedPosition = rebaixado02.getSelectedItemPosition();
                                chave = "palpiteRebaixado02" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 14:
                                selectedPosition = rebaixado03.getSelectedItemPosition();
                                chave = "palpiteRebaixado03" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 15:
                                selectedPosition = timeLanterna.getSelectedItemPosition();
                                chave = "palpiteTimeLanterna" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                        }
                    } else {
                        Toast.makeText(this, R.string.bloqueio_palpites_classificacoes, Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(this, "Para editar os palpites, configure data, hora e fuso horário do aparelho para automáticos.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {}
    //*********************(Fim do escopo do comentário)**********************

    public void buttonPalpitarJogos(@SuppressWarnings("unused") View view){
        chamarVibrate();
        salvarBackupPalpites();
        Intent palpitarJogos = new Intent(this, M_palpitar_jogos_serie_A.class);
        startActivity(palpitarJogos);
        finish();
    }

    private void chamarVibrate(){
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            Objects.requireNonNull(vibrator).vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        else {
            Objects.requireNonNull(vibrator).vibrate(20);
        }
    }

    private void salvarBackupPalpites(){
        if (redeDisponivel) {
            File getFile = new File(dirFiles, meusPalpitesBackup + ".xml");
            if (getFile.exists()) {
                try {
                    Runnable r = Z_runnable_subir_dados.newInstance(meusPalpitesBackup,"meusPalpitesBackup",ano,dirFiles);
                    new Thread(r).start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void onBackPressed(){
        salvarBackupPalpites();
        Intent inicial = new Intent(this,A_tela_inicial.class);
        startActivity(inicial);
        finish();
    }
    
    //*********************MENU**********************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_h_meus_palpites, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // return true so that the menu pop up is opened
        List<Object> ids = new ArrayList<>();
        ids.add(R.id.instrucoes);
        ids.add(R.id.voltar);

        switch (ids.indexOf(item.getItemId())) {
            case 0:
                Intent instruction = new Intent(this,B_instrucoes.class);
                instruction.putExtra("tela","H_meus_palpites");
                startActivity(instruction);
                break;
            case 1:
                onBackPressed();
        }
        return true;
    }
    //*********************(Fim do escopo do comentário)**********************
}
