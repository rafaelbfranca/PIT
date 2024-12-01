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

public class H_meus_palpites_serie_D extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private AdView adView;
    private final String selectedSerie = "SerieD";
    private String pontoDeCorte;
    private String ano;
    private String meusPalpitesBackup;
    private List<String> listTimes;
    private List<String> listSeries;
    private Boolean toqueNaTela = false;
    private Spinner dropdown_series;
    private Spinner timeCampeao;
    private Spinner timeLanterna;
    private Spinner acesso01;
    private Spinner acesso02;
    private Spinner acesso03;
    private Properties properties;
    private boolean redeDisponivel;
    private File dirFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.h_meus_palpites_serie_d);
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
        adView = findViewById(R.id.adsPalpitesSerieD);
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

    private void setPontoDeCorte(){
        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
        int totalRodadasNaFase = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + selectedSerie + ano + "F1" + "TotalRodadasNaFase", "1")));
        pontoDeCorte = "F1G1R"+(totalRodadasNaFase/2+1)+"J1";
        Log.v("pontoDeCorte","Ponto de corte: "+pontoDeCorte);
    }

    private void getDadosComuns(){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        ano = getDadosComuns.getString("ano","--");
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

        //Implementa o dropdown serie_b01.
        acesso01 = findViewById(R.id.dropdown_acesso01);
        ArrayAdapter<String> acesso01Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        acesso01Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        acesso01.setAdapter(acesso01Adapter);
        acesso01.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        acesso01.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown acesso02.
        acesso02 = findViewById(R.id.dropdown_acesso02);
        ArrayAdapter<String> acesso02Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        acesso02Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        acesso02.setAdapter(acesso02Adapter);
        acesso02.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        acesso02.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown acesso03.
        acesso03 = findViewById(R.id.dropdown_acesso03);
        ArrayAdapter<String> acesso03Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listTimes);
        acesso03Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        acesso03.setAdapter(acesso03Adapter);
        acesso03.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        acesso03.setOnItemSelectedListener(this);
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
        String serie = "Série D";
        dropdown_series.setSelection(listSeries.indexOf(serie));
        //Atualiza o time campeão.
        String time_campeao = prefs.getString("palpiteTimeCampeaoSerieD","--");
        p = listTimes.indexOf(time_campeao);
        timeCampeao.setSelection(p);
        //Atualiza pontos do time campeão.
        String pontos_time_campeao = prefs.getString("palpitePontosTimeCampeaoSerieD","--");
        EditText pontosTimeCampeao = findViewById(R.id.edit_pontos_campeao);
        pontosTimeCampeao.setText(pontos_time_campeao);
        //Atualiza saldo do time campeão.
        String saldo_time_campeao = prefs.getString("palpiteSaldoTimeCampeaoSerieD","--");
        EditText saldoTimeCampeao = findViewById(R.id.edit_saldo_campeao);
        saldoTimeCampeao.setText(saldo_time_campeao);
        //Atualiza o time acesso01.
        String acesso_01 = prefs.getString("palpiteAcesso01SerieD","--");
        p = listTimes.indexOf(acesso_01);
        acesso01.setSelection(p);
        //Atualiza o time acesso02.
        String acesso_02 = prefs.getString("palpiteAcesso02SerieD","--");
        p = listTimes.indexOf(acesso_02);
        acesso02.setSelection(p);
        //Atualiza o time acesso03.
        String acesso_03 = prefs.getString("palpiteAcesso03SerieD","--");
        p = listTimes.indexOf(acesso_03);
        acesso03.setSelection(p);
        //Atualiza o time timeLanterna.
        String time_lanterna = prefs.getString("palpiteTimeLanternaSerieD","--");
        p = listTimes.indexOf(time_lanterna);
        timeLanterna.setSelection(p);
        //Atualiza pontos do time lanterna.
        String pontos_time_lanterna = prefs.getString("palpitePontosTimeLanternaSerieD","--");
        EditText pontosTimeLanterna = findViewById(R.id.edit_pontos_lanterna);
        pontosTimeLanterna.setText(pontos_time_lanterna);
        //Atualiza saldo do time lanterna.
        String saldo_time_lanterna = prefs.getString("palpiteSaldoTimeLanternaSerieD","--");
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
            if ((parent.getId())==R.id.dropdown_series) {
                switch (position) {
                    case 0:
                        serie = new Intent(this, H_meus_palpites_serie_A.class);
                        startActivity(serie);
                        finish();
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
                        break;
                }
            }
            else {
                if (configuracoesAutomaticas()) {
                    if (palpitePermitido()) {
                        List<Object> ids = new ArrayList<>();
                        ids.add(R.id.dropdown_time_campeao);
                        ids.add(R.id.dropdown_acesso01);
                        ids.add(R.id.dropdown_acesso02);
                        ids.add(R.id.dropdown_acesso03);
                        ids.add(R.id.dropdown_time_lanterna);

                        switch (ids.indexOf(parent.getId())) {//Identifica qual spinner está sendo selecionado.
                            case 0:
                                int selectedPosition = timeCampeao.getSelectedItemPosition();
                                chave = "palpiteTimeCampeao" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 1:
                                selectedPosition = acesso01.getSelectedItemPosition();
                                chave = "palpiteAcesso01" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 2:
                                selectedPosition = acesso02.getSelectedItemPosition();
                                chave = "palpiteAcesso02" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 3:
                                selectedPosition = acesso03.getSelectedItemPosition();
                                chave = "palpiteAcesso03" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;
                            case 4:
                                selectedPosition = timeLanterna.getSelectedItemPosition();
                                chave = "palpiteTimeLanterna" + selectedSerie;
                                valor = listTimes.get(selectedPosition);
                                salvarPalpite(chave, valor);
                                break;

                        }
                    }
                    else {
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
        Intent palpitarJogos = new Intent(this, M_palpitar_jogos_serie_D.class);
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
