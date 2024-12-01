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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import androidx.appcompat.app.AppCompatActivity;

public class M_palpitar_jogos_serie_D extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private AdView adView;
    private String selectedSerie,fase,grupo,rodada,jogo,ano,dataJogo,meusPalpitesBackup;
    private String[] list_fase;
    private String[][] list_grupo,list_rodada,list_jogo;
    private ArrayList<String> downloadFiles,list_palpites,fileName;
    private Spinner dropdown_fase,dropdown_grupo,dropdown_rodada,dropdown_jogo;
    private Boolean toqueNaTela = false;
    private boolean dataPassada,redeDisponivel;
    private Properties properties;
    private File dirFiles;
    EditText palpitemandante,palpitevisitante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m_palpitar_jogos_serie_d);
        loadAds();
        dirFiles = getDir("files",MODE_PRIVATE);
        redeDisponivel = Z_static_functions.verificandoRede(this) != 0;
        properties = new Properties();
        recebeDados();
        getDadosComuns();
        resgataArrays();
        setListas();
        implementaSpinners();
        atualizaCampos();
        verificaData();
        setOnClickViews();
    }

    //-----Início Bloco Exibição de Anúncios-----
    private void loadAds(){
        adView = findViewById(R.id.adsPalpitarJogosSerieD);
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

    private void recebeDados(){
        Intent palpitarJogos = getIntent();
        Bundle extras = palpitarJogos.getExtras();
        if (extras!=null){
            selectedSerie = palpitarJogos.getStringExtra("passarSerie");
            rodada = palpitarJogos.getStringExtra("passarRodada");
            jogo = palpitarJogos.getStringExtra("passarJogo");
            fase = palpitarJogos.getStringExtra("passarFase");
            grupo = palpitarJogos.getStringExtra("passarGrupo");
        }
        else{
            selectedSerie = "SerieD";
            rodada = "R1";
            jogo = "J1";
            fase = "F1";
            grupo = "G1";
        }
    }

    private void getDadosComuns(){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        ano = getDadosComuns.getString("ano","--");
    }

    private void resgataArrays(){
        SharedPreferences referencias = getSharedPreferences("referencias",MODE_PRIVATE);
        Map<String,?> referenciasKeys = referencias.getAll();
        downloadFiles = new ArrayList<>();
        fileName = new ArrayList<>();
        list_palpites = new ArrayList<>();
        for (Map.Entry<String, ?> entry : referenciasKeys.entrySet()){
            switch(entry.getValue().toString()){
                case "downloadFiles":
                    fileName.add(entry.getKey());
                    downloadFiles.add(entry.getKey() + selectedSerie + ano);
                    break;
                case "campos_palpites_editaveis":
                    list_palpites.add(entry.getKey());
                    break;
            }
        }

        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
        int i = 0;
        while(!Objects.equals(dadosCampeonatos.getString("dadosCampeonatoAno" + i,"--"), "--")){
            i++;
        }
    }

    private void setListas(){
        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
        int totalFases = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + selectedSerie + ano + "TotalFases", "1")));
        list_fase = new String[totalFases];
        list_grupo = new String[totalFases][];
        list_rodada = new String[totalFases][];
        list_jogo = new String[totalFases][];
        for (int i = 0; i < totalFases; i++) {
            int totalGruposNaFase = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + selectedSerie + ano + "F" + (i+1) + "TotalGruposNaFase", "1")));
            int totalRodadasNaFase = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + selectedSerie + ano + "F" + (i+1) + "TotalRodadasNaFase", "1")));
            int totalJogosNaRodada = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + selectedSerie + ano + "F" + (i+1) + "TotalJogosNaRodada", "1")));
            list_grupo[i] = new String[totalGruposNaFase];
            list_rodada[i] = new String[totalRodadasNaFase];
            list_jogo[i] = new String[totalJogosNaRodada];
            if (totalFases>1){
                if (i+1 == totalFases){
                    list_fase[i] = "FINAL";
                }
                else{
                    list_fase[i] = (i+1)+"ª FASE";
                }
            }
            else{
                list_fase[i] = "FASE ÚNICA";
            }
            for (int j = 0; j < totalGruposNaFase; j++){
                list_grupo[i][j] = "GRUPO "+(j+1);
            }
            for (int j = 0; j < totalRodadasNaFase; j++){
                list_rodada[i][j] = "RODADA "+(j+1);
            }
            for (int j = 0; j < totalJogosNaRodada; j++){
                list_jogo[i][j] = "JOGO "+(j+1);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void implementaSpinners(){
        int f = Integer.parseInt(fase.replace("F","")) - 1;
        String[] listaGrupo, listaRodada, listaJogo;
        listaGrupo = new String[list_grupo[f].length];
        listaRodada = new String[list_rodada[f].length];
        listaJogo = new String[list_jogo[f].length];

        System.arraycopy(list_grupo[f], 0, listaGrupo, 0, list_grupo[f].length);
        System.arraycopy(list_rodada[f], 0, listaRodada, 0, list_rodada[f].length);
        System.arraycopy(list_jogo[f], 0, listaJogo, 0, list_jogo[f].length);

        //Implementa o dropdown de escolha da fase.
        dropdown_fase = findViewById(R.id.dropdown_fase_spinner);
        ArrayAdapter<String> adapter_fase = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list_fase);
        adapter_fase.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_fase.setAdapter(adapter_fase);
        dropdown_fase.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_fase.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown de escolha do grupo.
        dropdown_grupo = findViewById(R.id.dropdown_grupo_spinner);
        ArrayAdapter<String> adapter_grupo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaGrupo);
        adapter_grupo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_grupo.setAdapter(adapter_grupo);
        dropdown_grupo.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_grupo.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown de escolha da rodada.
        dropdown_rodada = findViewById(R.id.dropdown_rodada_spinner);
        ArrayAdapter<String> adapter_rodada = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaRodada);
        adapter_rodada.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_rodada.setAdapter(adapter_rodada);
        dropdown_rodada.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_rodada.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown de escolha do jogo.
        dropdown_jogo = findViewById(R.id.dropdown_jogo_spinner);
        ArrayAdapter<String> adapter_jogo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaJogo);
        adapter_jogo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_jogo.setAdapter(adapter_jogo);
        dropdown_jogo.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_jogo.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        dropdown_fase.setSelection(Integer.parseInt(fase.replace("F","")) - 1);
        dropdown_grupo.setSelection(Integer.parseInt(grupo.replace("G", "")) - 1);
        dropdown_rodada.setSelection(Integer.parseInt(rodada.replace("R","")) - 1);
        dropdown_jogo.setSelection(Integer.parseInt(jogo.replace("J","")) - 1);
    }

    private void atualizaCampos(){
        //Atualiza campos resultados.
        dataJogo = "Data";
        String valor;
        for (String downloadFile : downloadFiles) {
            SharedPreferences prefs = getSharedPreferences(downloadFile, MODE_PRIVATE);
            if(!prefs.contains("arquivo")){
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("arquivo", "criado");
                editor.apply();
            }
            for (String file : fileName) {
                if (!Objects.equals(file, "times") && Objects.equals(downloadFile,file+selectedSerie+ano)){
                    valor = prefs.getString("resultado" + selectedSerie + fase + grupo + rodada + jogo + file, "Data");
                    @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(file, "id", getPackageName());
                    TextView atualizaCampo = findViewById(resId);
                    atualizaCampo.setText(valor);
                    if (file.equals("data")){
                        dataJogo = valor;
                    }
                }
            }
        }

        //Atualiza campos palpites.
        SharedPreferences palpites = getSharedPreferences("meusPalpites" + selectedSerie + ano, MODE_PRIVATE);
        if(!palpites.contains("arquivo")){
            SharedPreferences.Editor editor = palpites.edit();
            editor.putString("arquivo", "criado");
            editor.apply();
        }
        for (String palpite:list_palpites) {
            valor = palpites.getString("palpite" + selectedSerie + fase + grupo + rodada + jogo + palpite,"--");
            @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(palpite, "id", getPackageName());
            TextView atualizaCampo = findViewById(resId);
            atualizaCampo.setText(valor);
        }

        SharedPreferences meusDados = getSharedPreferences("meusDados",MODE_PRIVATE);
        meusPalpitesBackup = "meusPalpites"+selectedSerie+ano+"Backup"+meusDados.getString("meuNumero","--");
        Map<String,?> allEntries = palpites.getAll();
        for(Map.Entry<String,?> entry : allEntries.entrySet()){
            properties.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
        }

        //Atualiza campo de pontos.
        SharedPreferences meusPontos = getSharedPreferences("meusPontos" + ano, MODE_PRIVATE);
        String chavePontosNesteJogo = "pontos"+selectedSerie+fase+grupo+rodada+jogo+ano;
        String valorPontosNesteJogo = meusPontos.getString(chavePontosNesteJogo,"--");
        TextView pontosNesteJogo = findViewById(R.id.valorPontosNesteJogo);
        pontosNesteJogo.setText(valorPontosNesteJogo);
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

    private void verificaData(){
        dataPassada = false;
        EditText palpiteMandante = findViewById(R.id.palpitemandante);
        EditText palpiteVisitante = findViewById(R.id.palpitevisitante);
        if (configuracoesAutomaticas()) {
            int dataDoJogo = 0;
            Date momento = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterDDD = new SimpleDateFormat("DDD");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterData = new SimpleDateFormat("EEE, dd/MM/yyyy");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterAno = new SimpleDateFormat("yyyy");
            int dataHoje = Integer.parseInt(formatterDDD.format(momento));

            if (dataJogo.equals("Data")) {
                dataDoJogo = dataHoje + 1;
            }
            else {
                try {
                    Date data = formatterData.parse(dataJogo);
                    int anoHoje = Integer.parseInt(formatterAno.format(momento));
                    int anoJogo = Integer.parseInt(formatterAno.format(Objects.requireNonNull(data)));
                    if (anoJogo < anoHoje){
                        dataPassada = true;
                    }
                    if (anoJogo == anoHoje){
                        dataDoJogo = Integer.parseInt(formatterDDD.format(Objects.requireNonNull(data)));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (dataHoje >= dataDoJogo){
                dataPassada = true;
            }
        }
        else{
            dataPassada = true;
        }

        if (dataPassada){
            palpiteMandante.setInputType(0);//"0" desabilita a edição, "1" habilita texto geral, "2" habilita número inteiro.
            palpiteVisitante.setInputType(0);//"0" desabilita a edição, "1" habilita texto geral, "2" habilita número inteiro.
        }
        else {
            palpiteMandante.setInputType(2);//"0" desabilita a edição, "1" habilita texto geral, "2" habilita número inteiro.
            palpiteVisitante.setInputType(2);//"0" desabilita a edição, "1" habilita texto geral, "2" habilita número inteiro.
        }
    }

    private void salvarPalpite(){
        SharedPreferences prefs = getSharedPreferences("meusPalpites"+selectedSerie+ano, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String descriptionMandante = (String) palpitemandante.getContentDescription();
        String chaveMandante = "palpite"+selectedSerie+fase+grupo+rodada+jogo+descriptionMandante;
        String valorMandante = String.valueOf(palpitemandante.getText());
        editor.putString(chaveMandante, valorMandante);

        String descriptionVisitante = (String) palpitevisitante.getContentDescription();
        String chaveVisitante = "palpite"+selectedSerie+fase+grupo+rodada+jogo+descriptionVisitante;
        String valorVisitante = String.valueOf(palpitevisitante.getText());
        editor.putString(chaveVisitante, valorVisitante);

        Toast.makeText(this, R.string.palpiteSalvo, Toast.LENGTH_SHORT).show();
        editor.apply();
        atualizaCampos();
        toqueNaTela = false;
        
        properties.setProperty(chaveMandante, valorMandante);
        properties.setProperty(chaveVisitante, valorVisitante);
        try {
            File getFile = new File(dirFiles, meusPalpitesBackup+".xml");
            FileOutputStream outputStream = new FileOutputStream(getFile);
            properties.storeToXML(outputStream, "meusPalpitesBackup");
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private void setOnClickViews(){
        palpitemandante = findViewById(R.id.palpitemandante);
        palpitevisitante = findViewById(R.id.palpitevisitante);
        palpitemandante.setOnClickListener(this::setInputData);
        palpitevisitante.setOnClickListener(this::setInputData);
    }

    //Executa algo a partir da seleção nos EditTexts.
    public void setInputData(View v){
        EditText view = (EditText) v;
        if (configuracoesAutomaticas()) {
            if (dataPassada) {
                Toast.makeText(this, R.string.bloqueio_palpites_jogos, Toast.LENGTH_SHORT).show();
            } else {
                salvarPalpite();
            }
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0); //hide keyboard
        }
        else{
            Toast.makeText(this, "Para editar os palpites, configure data, hora e fuso horário do aparelho para automáticos.", Toast.LENGTH_LONG).show();
        }
    }

    //Executa algo a partir das seleções nos spinners.
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(toqueNaTela) {
            List<Object> ids = new ArrayList<>();
            ids.add(R.id.dropdown_fase_spinner);
            ids.add(R.id.dropdown_grupo_spinner);
            ids.add(R.id.dropdown_rodada_spinner);
            ids.add(R.id.dropdown_jogo_spinner);

            switch (ids.indexOf(parent.getId())) {
                case 0:
                    fase = "F"+(position+1);
                    grupo = "G1";
                    rodada = "R1";
                    jogo = "J1";
                    dropdown_fase.setSelection(position);
                    dropdown_grupo.setSelection(0);
                    dropdown_rodada.setSelection(0);
                    dropdown_jogo.setSelection(0);
                    toqueNaTela = false;
                    implementaSpinners();
                    break;
                case 1:
                    grupo = "G"+(position+1);
                    rodada = "R1";
                    jogo = "J1";
                    dropdown_rodada.setSelection(0);
                    dropdown_jogo.setSelection(0);
                    toqueNaTela = false;
                    break;
                case 2:
                    rodada = "R"+(position+1);
                    jogo = "J1";
                    dropdown_jogo.setSelection(0);
                    toqueNaTela = false;
                    break;
                case 3:
                    jogo = "J"+(position+1);
                    toqueNaTela = false;
                    break;
            }
            atualizaCampos();
            verificaData();
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {}

    public void buttonResultadosJogos(View view) {
        chamarVibrate();
        chamarResultadosJogos();
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

    private void chamarResultadosJogos(){
        Intent resultadosJogos = new Intent(this, G_resultados_dos_jogos.class);
        resultadosJogos.putExtra("serie",selectedSerie);
        resultadosJogos.putExtra("fase",fase);
        resultadosJogos.putExtra("grupo",grupo);
        resultadosJogos.putExtra("rodada",Integer.parseInt(rodada.replace("R","")) - 1);
        startActivity(resultadosJogos);
        finish();
    }

    @Override
    public void onBackPressed(){
        if (redeDisponivel) {
            File getFile = new File(dirFiles, meusPalpitesBackup+".xml");
            if (getFile.exists()) {
                try {
                    Runnable r = Z_runnable_subir_dados.newInstance(meusPalpitesBackup,"meusPalpitesBackup",ano,dirFiles);
                    new Thread(r).start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Intent meusPalpites = new Intent(this,H_meus_palpites_serie_D.class);
        startActivity(meusPalpites);
        finish();
    }

    //*********************MENU**********************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_m_jogos, menu);
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
                instruction.putExtra("tela","M_palpitar_jogos");
                startActivity(instruction);
                break;
            case 1:
                onBackPressed();
        }
        return true;
    }
}
