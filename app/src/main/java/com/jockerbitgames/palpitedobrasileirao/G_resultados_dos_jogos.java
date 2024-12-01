package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

public class G_resultados_dos_jogos extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private AdView adView;
    private boolean redeDisponivel;
    private ListView listViewResultadosJogos;
    @SuppressWarnings("FieldCanBeLocal")
    private Spinner dropdown_serieResultado_spinner,dropdown_anoResultado_spinner,dropdown_faseResultado_spinner,dropdown_grupoResultado_spinner;
    private ArrayList<String> downloadFiles;
    private boolean toqueNaTela = false;
    private String[] lista_serie_spinner, lista_serie_escolhida, lista_ano_spinner, lista_fases_spinner, grupo_spinner;
    private String[][]lista_grupos_spinner;
    private String[] list_fase;
    private String [][] list_grupo, list_rodada, list_jogo;
    private int maxRodada;
    int setAdapterArrayFPosition,setAdapterArrayRodada;
    String setAdapterArrayResultadoEscolhido,setAdapterArrayPassarSerie,setAdapterArrayPassarFase,setAdapterArrayPassarGrupo,setAdapterArrayAno;
    TextView numRodada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.g_resultado_dos_jogos);
        loadAds();
        listViewResultadosJogos = findViewById(R.id.listViewResultadosJogos);
        numRodada = findViewById(R.id.numRodada);
        redeDisponivel = Z_static_functions.verificandoRede(this) != 0;
        recebeDados();
        resgataArrays();
        resgataListas(setAdapterArrayPassarSerie,setAdapterArrayAno);
        implementaSpinner(lista_serie_spinner,lista_ano_spinner,lista_fases_spinner,lista_grupos_spinner[0]);
        setAdapterArray(setAdapterArrayFPosition,setAdapterArrayResultadoEscolhido,setAdapterArrayPassarSerie,setAdapterArrayPassarFase,setAdapterArrayPassarGrupo,setAdapterArrayAno,setAdapterArrayRodada);
        if (!configuracoesAutomaticas()){
            Toast.makeText(G_resultados_dos_jogos.this, "Para editar os palpites, configure data, hora e fuso horário do aparelho para automáticos.", Toast.LENGTH_LONG).show();
        }
    }

    //-----Início Bloco Exibição de Anúncios-----
    private void loadAds(){
        adView = findViewById(R.id.adsResultadoJogos);
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

    private String getDadosComuns(){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        return getDadosComuns.getString("ano","--");
    }

    private void setDadosComuns(@SuppressWarnings("SameParameterValue") String chave, String valor){
        //Dados que serão lidos por outras classes.
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        SharedPreferences.Editor editor = dadosComuns.edit();
        editor.putString(chave, valor);
        editor.apply();
    }

    private void recebeDados(){
        setAdapterArrayAno = getDadosComuns();
        Intent resultadosJogos = getIntent();
        Bundle extras = resultadosJogos.getExtras();
        if (extras!=null){
            setAdapterArrayPassarSerie = resultadosJogos.getStringExtra("serie");
            setAdapterArrayPassarFase = resultadosJogos.getStringExtra("fase");
            setAdapterArrayPassarGrupo = resultadosJogos.getStringExtra("grupo");
            setAdapterArrayRodada = resultadosJogos.getIntExtra("rodada",-1);
            if (setAdapterArrayPassarSerie.equals("SerieC") || setAdapterArrayPassarSerie.equals("SerieD")){
                setAdapterArrayResultadoEscolhido = "resultado" + setAdapterArrayPassarSerie + setAdapterArrayPassarFase + setAdapterArrayPassarGrupo;
            }
            else{
                setAdapterArrayResultadoEscolhido = "resultado" + setAdapterArrayPassarSerie;
            }
        }
        else{
            setAdapterArrayPassarSerie = "SerieA";
            setAdapterArrayPassarFase = "F1";
            setAdapterArrayPassarGrupo = "G1";
            setAdapterArrayRodada = -1;
            setAdapterArrayResultadoEscolhido = "resultado" + setAdapterArrayPassarSerie;
        }
    }

    private void resgataArrays(){
        Resources res = getResources();
        lista_serie_spinner = res.getStringArray(R.array.series_array);
        lista_serie_escolhida = new String[lista_serie_spinner.length];
        for (int pos = 0; pos < lista_serie_spinner.length; pos++) {
            lista_serie_escolhida[pos] = lista_serie_spinner[pos].replace("Série ", "Serie");
        }

        SharedPreferences referencias = getSharedPreferences("referencias",MODE_PRIVATE);
        Map<String,?> referenciasKeys = referencias.getAll();
        downloadFiles = new ArrayList<>();
        for (Map.Entry<String, ?> entry : referenciasKeys.entrySet()){
            switch(entry.getValue().toString()){
                case "downloadFiles":
                case "campos_palpites_editaveis":
                    if(!entry.getKey().equals("times")) {
                        downloadFiles.add(entry.getKey());
                    }
                    break;
            }
        }

        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
        int i = 0;
        while(!Objects.equals(dadosCampeonatos.getString("dadosCampeonatoAno" + i,"--"), "--")){
            i++;
        }
        lista_ano_spinner = new String[i];
        for (int j = 0; j<i; j++){
            lista_ano_spinner[j] = dadosCampeonatos.getString("dadosCampeonatoAno" + j,"--");
        }
        if (setAdapterArrayAno.equals("--")){
            setAdapterArrayAno = lista_ano_spinner[0];
            setDadosComuns("ano",lista_ano_spinner[0]);
        }
    }

    private void resgataListas(String selectedSerie, String ano){
        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
        int totalFases = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + selectedSerie + ano + "TotalFases", "1")));
        lista_fases_spinner = new String[totalFases];
        lista_grupos_spinner = new String[totalFases][];
        int[] totalGruposNaFase = new int[totalFases];
        list_fase = new String[totalFases];
        list_grupo = new String[totalFases][];
        list_rodada = new String[totalFases][];
        list_jogo = new String[totalFases][];
        for (int i = 0; i< totalFases; i++){
            totalGruposNaFase[i] = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + selectedSerie + ano + "F" + (i + 1) + "TotalGruposNaFase", "1")));
            //Listas spinner
            lista_grupos_spinner[i] = new String[totalGruposNaFase[i]];
            if (totalFases >1){
                if (i+1 == totalFases){
                    lista_fases_spinner[i] = "FINAL";
                }
                else{
                    lista_fases_spinner[i] = (i+1)+"ª FASE";
                }
                for (int j = 0; j< totalGruposNaFase[i]; j++){
                    lista_grupos_spinner[i][j] = "GRUPO "+(j+1);
                }
            }
            else{
                lista_fases_spinner[i] = "FASE ÚNICA";
                int j = 0;
                if (totalGruposNaFase[i]>1){
                    for (; j< totalGruposNaFase[i]; j++){
                        lista_grupos_spinner[i][j] = "GRUPO "+(j+1);
                    }
                }
                else{
                    lista_grupos_spinner[i][j] = "GRUPO ÚNICO";
                }
            }
            //Outras listas
            list_grupo[i] = new String[totalGruposNaFase[i]];
            int totalRodadasNaFase = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + selectedSerie + ano + "F" + (i+1) + "TotalRodadasNaFase", "1")));
            list_rodada[i] = new String[totalRodadasNaFase];
            int totalJogosNaRodada = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + selectedSerie + ano + "F" + (i+1) + "TotalJogosNaRodada", "1")));
            list_jogo[i] = new String[totalJogosNaRodada];
            list_fase[i] = "F"+(i+1);
            for (int j = 0; j<totalGruposNaFase[i]; j++){
                list_grupo[i][j] = "G"+(j+1);
            }
            for (int j = 0; j<totalRodadasNaFase; j++){
                list_rodada[i][j] = "R"+(j+1);
            }
            for (int j = 0; j<totalJogosNaRodada; j++){
                list_jogo[i][j] = "J"+(j+1);
            }
        }
        for (int k = 0; k < list_fase.length; k++){
            if (list_fase[k].equals(setAdapterArrayPassarFase)){
                setAdapterArrayFPosition = k;
                break;
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void implementaSpinner(String[] serie, String[] ano, String[] fase, String[] grupo){
        //Implementa o dropdown de escolha da serie.
        dropdown_serieResultado_spinner = findViewById(R.id.dropdown_serieResultado_spinner);
        ArrayAdapter<String> adapterSerie = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, serie);
        adapterSerie.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_serieResultado_spinner.setAdapter(adapterSerie);
        dropdown_serieResultado_spinner.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_serieResultado_spinner.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown de escolha do ano.
        dropdown_anoResultado_spinner = findViewById(R.id.dropdown_anoResultado_spinner);
        ArrayAdapter<String> adapterAno = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ano);
        adapterAno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_anoResultado_spinner.setAdapter(adapterAno);
        dropdown_anoResultado_spinner.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_anoResultado_spinner.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown de escolha da fase.
        dropdown_faseResultado_spinner = findViewById(R.id.dropdown_faseResultado_spinner);
        ArrayAdapter<String> adapterFase = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fase);
        adapterFase.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_faseResultado_spinner.setAdapter(adapterFase);
        dropdown_faseResultado_spinner.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_faseResultado_spinner.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown de escolha do grupo.
        dropdown_grupoResultado_spinner = findViewById(R.id.dropdown_grupoResultado_spinner);
        ArrayAdapter<String> adapterGrupo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, grupo);
        adapterGrupo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_grupoResultado_spinner.setAdapter(adapterGrupo);
        dropdown_grupoResultado_spinner.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_grupoResultado_spinner.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        dropdown_serieResultado_spinner.setSelection(java.util.Arrays.asList(lista_serie_spinner).indexOf(setAdapterArrayPassarSerie.replace("Serie","Série ")));
        dropdown_anoResultado_spinner.setSelection(java.util.Arrays.asList(lista_ano_spinner).indexOf(setAdapterArrayAno));
        dropdown_faseResultado_spinner.setSelection(java.util.Arrays.asList(list_fase).indexOf(setAdapterArrayPassarFase));
        dropdown_grupoResultado_spinner.setSelection(java.util.Arrays.asList(list_grupo[java.util.Arrays.asList(list_fase).indexOf(setAdapterArrayPassarFase)]).indexOf(setAdapterArrayPassarGrupo));
    }

    //Executa algo a partir da seleção nos spinners.
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(toqueNaTela) {
            toqueNaTela = false;
            int sPosition = 0;
            int aPosition = 0;
            int fPosition = 0;
            int gPosition = 0;

            List<Object> ids = new ArrayList<>();
            ids.add(R.id.dropdown_serieResultado_spinner);
            ids.add(R.id.dropdown_anoResultado_spinner);
            ids.add(R.id.dropdown_faseResultado_spinner);
            ids.add(R.id.dropdown_grupoResultado_spinner);

            switch (ids.indexOf(parent.getId())) {
                case 0:
                    sPosition = position;
                    aPosition = dropdown_anoResultado_spinner.getSelectedItemPosition();
                    resgataListas(lista_serie_escolhida[sPosition],lista_ano_spinner[aPosition]);
                    grupo_spinner = lista_grupos_spinner[fPosition];
                    break;
                case 1:
                    sPosition = dropdown_serieResultado_spinner.getSelectedItemPosition();
                    aPosition = position;
                    setDadosComuns("ano",lista_ano_spinner[aPosition]);
                    resgataListas(lista_serie_escolhida[sPosition],lista_ano_spinner[aPosition]);
                    grupo_spinner = lista_grupos_spinner[fPosition];
                    break;
                case 2:
                    sPosition = dropdown_serieResultado_spinner.getSelectedItemPosition();
                    aPosition = dropdown_anoResultado_spinner.getSelectedItemPosition();
                    fPosition = position;
                    grupo_spinner = lista_grupos_spinner[position];
                    break;
                case 3:
                    sPosition = dropdown_serieResultado_spinner.getSelectedItemPosition();
                    aPosition = dropdown_anoResultado_spinner.getSelectedItemPosition();
                    fPosition = dropdown_faseResultado_spinner.getSelectedItemPosition();
                    grupo_spinner = lista_grupos_spinner[fPosition];
                    gPosition = position;
                    break;
            }
            setAdapterArrayFPosition = fPosition;
            if((!lista_serie_escolhida[sPosition].equals("SerieA")) && (!lista_serie_escolhida[sPosition].equals("SerieB"))){
                setAdapterArrayResultadoEscolhido = "resultado" + lista_serie_escolhida[sPosition] + list_fase[fPosition] + list_grupo[fPosition][gPosition];
            }
            else{
                setAdapterArrayResultadoEscolhido = "resultado" + lista_serie_escolhida[sPosition];
            }
            setAdapterArrayPassarSerie = lista_serie_escolhida[sPosition];
            setAdapterArrayPassarFase = list_fase[fPosition];
            setAdapterArrayPassarGrupo = list_grupo[fPosition][gPosition];
            setAdapterArrayAno = lista_ano_spinner[aPosition];
            setAdapterArrayRodada = -1;
            implementaSpinner(lista_serie_spinner,lista_ano_spinner,lista_fases_spinner,grupo_spinner);
            setAdapterArray(setAdapterArrayFPosition,setAdapterArrayResultadoEscolhido,setAdapterArrayPassarSerie,setAdapterArrayPassarFase,setAdapterArrayPassarGrupo,setAdapterArrayAno,setAdapterArrayRodada);
        }
    }

    public void escolherRodadaAbaixo(View view){
        int rodadaAtual = Integer.parseInt(String.valueOf(numRodada.getText()));
        if (rodadaAtual > 1){
            rodadaAtual--;
            numRodada.setText(String.valueOf(rodadaAtual));
        }
        setAdapterArrayRodada = rodadaAtual - 1;
        setAdapterArray(setAdapterArrayFPosition,setAdapterArrayResultadoEscolhido,setAdapterArrayPassarSerie,setAdapterArrayPassarFase,setAdapterArrayPassarGrupo,setAdapterArrayAno,setAdapterArrayRodada);
    }

    public void escolherRodadaAcima(View view){
        int rodadaAtual = Integer.parseInt(String.valueOf(numRodada.getText()));
        if (rodadaAtual<maxRodada){
            rodadaAtual++;
            numRodada.setText(String.valueOf(rodadaAtual));
        }
        setAdapterArrayRodada = rodadaAtual - 1;
        setAdapterArray(setAdapterArrayFPosition,setAdapterArrayResultadoEscolhido,setAdapterArrayPassarSerie,setAdapterArrayPassarFase,setAdapterArrayPassarGrupo,setAdapterArrayAno,setAdapterArrayRodada);
    }

    public void onNothingSelected(AdapterView<?> arg0) {}
    //*********************(Fim do escopo do comentário)**********************

    private void setAdapterArray(int fPosition, String resultadoEscolhido, String passarSerie, String passarFase, String passarGrupo, String ano, int rodadaInformada){
        ArrayList<ResultadoModel> resultados = new ArrayList<>();
        maxRodada = list_rodada[fPosition].length;
        int rPosition = list_rodada[fPosition].length - 1;
        int jPosition = list_jogo[fPosition].length - 1;
        SharedPreferences resultadomandante = getSharedPreferences("resultadomandante" + passarSerie + ano, MODE_PRIVATE);
        if (rodadaInformada == -1){
            loopRodada:
            for(int i = rPosition; i>=0; i--){
                for(int j = jPosition; j>=0; j--){
                    String resultadoProcurado = resultadoEscolhido+list_rodada[fPosition][i]+list_jogo[fPosition][j]+"resultadomandante";
                    if (!Objects.equals(resultadomandante.getString(resultadoProcurado, "--"), "--")){
                        rodadaInformada = i;
                        numRodada.setText(String.valueOf(rodadaInformada + 1));
                        break loopRodada;
                    }
                }
                if (i == 0){
                    rodadaInformada = 0;
                    numRodada.setText(String.valueOf(rodadaInformada + 1));
                }
            }
        }
        else{
            numRodada.setText(String.valueOf(rodadaInformada + 1));
        }
        //Busca de todos os atributos.
        resultadoEscolhido = resultadoEscolhido + list_rodada[fPosition][rodadaInformada];
        String palpiteEscolhido = resultadoEscolhido.replace("resultado", "palpite");
        String rodada = "Rodada " + (rodadaInformada + 1);
        String passarRodada = list_rodada[fPosition][rodadaInformada];
        String passarJogo;
        String jogo;
        String data = "";
        String local = "";
        String horario = "";
        String mandante = "";
        String visitante = "";
        String resultadoMandante = "";
        String resultadoVisitante = "";
        String palpiteMandante = "";
        String palpiteVisitante = "";

        for (int i = jPosition; i>=0; i--){
            passarJogo = list_jogo[fPosition][i];
            jogo = "Jogo " + (i + 1);
            for (String file:downloadFiles) {
                if (file.contains("palpite")){
                    SharedPreferences arquivo = getSharedPreferences("meusPalpites" + passarSerie + ano,MODE_PRIVATE);
                    switch (file){
                        case "palpitemandante":
                            palpiteMandante = arquivo.getString(palpiteEscolhido + passarJogo + file, "--");
                            break;
                        case "palpitevisitante":
                            palpiteVisitante = arquivo.getString(palpiteEscolhido + passarJogo + file, "--");
                            break;
                    }
                }
                else {
                    SharedPreferences arquivo = getSharedPreferences(file + passarSerie + ano, MODE_PRIVATE);
                    switch (file){
                        case "data":
                            data = arquivo.getString(resultadoEscolhido + passarJogo + file, "--");
                            break;
                        case "local":
                            local = arquivo.getString(resultadoEscolhido + passarJogo + file, "--");
                            break;
                        case "horario":
                            horario = arquivo.getString(resultadoEscolhido + passarJogo + file, "--");
                            break;
                        case "mandante":
                            mandante = arquivo.getString(resultadoEscolhido + passarJogo + file, "--");
                            break;
                        case "visitante":
                            visitante = arquivo.getString(resultadoEscolhido + passarJogo + file, "--");
                            break;
                        case "resultadomandante":
                            resultadoMandante = arquivo.getString(resultadoEscolhido + passarJogo + file, "--");
                            break;
                        case "resultadovisitante":
                            resultadoVisitante = arquivo.getString(resultadoEscolhido + passarJogo + file, "--");
                            break;
                    }
                }
            }
            //Criação do objeto.
            ResultadoModel resultado = new ResultadoModel(passarSerie, passarFase, passarGrupo, passarRodada, passarJogo, rodada, jogo, data, local, horario, mandante, visitante, resultadoMandante, resultadoVisitante, palpiteMandante, palpiteVisitante);
            //Carregamento de todos os atributos.
            resultado.setPassarSerie(passarSerie);
            resultado.setPassarFase(passarFase);
            resultado.setPassarGrupo(passarGrupo);
            resultado.setPassarRodada(passarRodada);
            resultado.setPassarJogo(passarJogo);
            resultado.setRodada(rodada);
            resultado.setJogo(jogo);
            resultado.setData(data);
            resultado.setLocal(local);
            resultado.setHorario(horario);
            resultado.setMandante(mandante);
            resultado.setVisitante(visitante);
            resultado.setResultadoMandante(resultadoMandante);
            resultado.setResultadoVisitante(resultadoVisitante);
            resultado.setPalpiteMandante(palpiteMandante);
            resultado.setPalpiteVisitante(palpiteVisitante);
            //Inclusão do objeto carregado na lista de objetos.
            resultados.add(resultado);
        }
        //Transferência da lista de objetos para o adapter.
        ResultadoAdapter adapter = new ResultadoAdapter(this, resultados);
        //Inicialização da listview com o adapter.
        listViewResultadosJogos.setAdapter(adapter);
    }

    private static class ResultadoModel{
        String passarSerie,passarFase,passarGrupo,passarRodada,passarJogo,rodada,jogo,data,local,horario,mandante,visitante,resultadoMandante,resultadoVisitante,palpiteMandante,palpiteVisitante;
        ResultadoModel(String passarSerie, String passarFase, String passarGrupo, String passarRodada, String passarJogo, String rodada, String jogo, String data, String local, String horario, String mandante, String visitante, String resultadoMandante, String resultadoVisitante, String palpiteMandante, String palpiteVisitante){
            this.passarSerie = passarSerie;
            this.passarFase = passarFase;
            this.passarGrupo = passarGrupo;
            this.passarRodada = passarRodada;
            this.passarJogo = passarJogo;
            this.rodada = rodada;
            this.jogo = jogo;
            this.data = data;
            this.local = local;
            this.horario = horario;
            this.mandante = mandante;
            this.visitante = visitante;
            this.resultadoMandante = resultadoMandante;
            this.resultadoVisitante = resultadoVisitante;
            this.palpiteMandante = palpiteMandante;
            this.palpiteVisitante = palpiteVisitante;
        }

        void setPassarSerie(String passarSerie) {
            this.passarSerie = passarSerie;
        }
        void setPassarFase(String passarFase) {
            this.passarFase = passarFase;
        }
        void setPassarGrupo(String passarGrupo) {
            this.passarGrupo = passarGrupo;
        }
        void setPassarRodada(String passarRodada) {
            this.passarRodada = passarRodada;
        }
        void setPassarJogo(String passarJogo) {
            this.passarJogo = passarJogo;
        }
        void setRodada(String rodada) {
            this.rodada = rodada;
        }
        void setJogo(String jogo) {
            this.jogo = jogo;
        }
        void setData(String data) {
            this.data = data;
        }
        void setLocal(String local) {
            this.local = local;
        }
        void setHorario(String horario) {
            this.horario = horario;
        }
        void setMandante(String mandante) {
            this.mandante = mandante;
        }
        void setVisitante(String visitante) {
            this.visitante = visitante;
        }
        void setResultadoMandante(String resultadoMandante) {
            this.resultadoMandante = resultadoMandante;
        }
        void setResultadoVisitante(String resultadoVisitante) {
            this.resultadoVisitante = resultadoVisitante;
        }
        void setPalpiteMandante(String palpiteMandante) {
            this.palpiteMandante = palpiteMandante;
        }
        void setPalpiteVisitante(String palpiteVisitante) {
            this.palpiteVisitante = palpiteVisitante;
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

    private class ResultadoAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<ResultadoModel> resultados;
        boolean dataPassada;

        ResultadoAdapter(Context context, ArrayList<ResultadoModel> resultados) {
            this.context = context;
            this.resultados = resultados;
        }

        private boolean isDataPassada(String dataJogo){
            dataPassada = false;
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
            return dataPassada;
        }

        @Override
        public int getCount() {
            return resultados.size();
        }

        @Override
        public Object getItem(int position) {
            return resultados.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ResultadoModel resultado = resultados.get(position);
            ViewHolder holder;
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.g_resultado_dos_jogos_list_view_item,parent,false);
                holder = new ViewHolder();
                holder.valorRodada = convertView.findViewById(R.id.valorRodada);
                holder.valorJogo = convertView.findViewById(R.id.valorJogo);
                holder.valorData = convertView.findViewById(R.id.valorData);
                holder.valorLocal = convertView.findViewById(R.id.valorLocal);
                holder.valorHorario = convertView.findViewById(R.id.valorHorario);
                holder.valorTimes = convertView.findViewById(R.id.valorTimes);
                holder.valorTimeMandante = convertView.findViewById(R.id.valorTimeMandante);
                holder.valorTimesX = convertView.findViewById(R.id.valorTimesX);
                holder.valorTimeVisitante = convertView.findViewById(R.id.valorTimeVisitante);
                holder.valorResultado = convertView.findViewById(R.id.valorResultado);
                holder.valorResultadoMandante = convertView.findViewById(R.id.valorResultadoMandante);
                holder.valorResultadoX = convertView.findViewById(R.id.valorResultadoX);
                holder.valorResultadoVisitante = convertView.findViewById(R.id.valorResultadoVisitante);
                holder.valorPalpite = convertView.findViewById(R.id.valorPalpite);
                holder.valorPalpiteMandante = convertView.findViewById(R.id.valorPalpiteMandante);
                holder.valorPalpiteX = convertView.findViewById(R.id.valorPalpiteX);
                holder.valorPalpiteVisitante = convertView.findViewById(R.id.valorPalpiteVisitante);
                holder.editarPalpite = convertView.findViewById(R.id.editarPalpite);
                holder.linhaPalpite = convertView.findViewById(R.id.linhaPalpite);
                holder.linhaEditarPalpite = convertView.findViewById(R.id.linhaEditarPalpite);
                holder.linhaPalpiteCongelado = convertView.findViewById(R.id.linhaPalpiteCongelado);
                convertView.setTag(holder);
            }
            else{
                holder = (ViewHolder) convertView.getTag();
            }

            holder.valorRodada.setText(resultado.rodada);
            holder.valorJogo.setText(resultado.jogo);
            holder.valorData.setText(resultado.data);
            holder.valorLocal.setText(resultado.local);
            holder.valorHorario.setText(resultado.horario);
            holder.valorTimeMandante.setText(resultado.mandante);
            holder.valorTimeVisitante.setText(resultado.visitante);
            holder.valorResultadoMandante.setText(resultado.resultadoMandante);
            holder.valorResultadoVisitante.setText(resultado.resultadoVisitante);
            holder.valorPalpiteMandante.setText(resultado.palpiteMandante);
            holder.valorPalpiteVisitante.setText(resultado.palpiteVisitante);

            //Verificar data e definir visibilidade dos RelativeLayouts
            if (isDataPassada(resultado.data)){
                holder.linhaEditarPalpite.setVisibility(View.GONE);
                holder.linhaPalpiteCongelado.setVisibility(View.VISIBLE);
                holder.linhaPalpite.setBackgroundColor(0xFF2196F3);
            }
            else{
                holder.linhaEditarPalpite.setVisibility(View.VISIBLE);
                holder.linhaPalpiteCongelado.setVisibility(View.GONE);
                //Verificar existência de palpite e setar cor da linha
                if (resultado.palpiteMandante.equals("--") || resultado.palpiteVisitante.equals("--")){
                    holder.linhaPalpite.setBackgroundColor(0xFFFFFF00);
                }
                if (!resultado.palpiteMandante.equals("--") && !resultado.palpiteVisitante.equals("--")){
                    holder.linhaPalpite.setBackgroundColor(0xFFCCFF90);
                }
            }

            final ResultadoModel passarResultado = resultado;
            holder.editarPalpite.setOnClickListener(view -> {
                Intent palpitarJogos = new Intent();
                switch (passarResultado.passarSerie){
                    case "SerieA":
                        palpitarJogos = new Intent(context,M_palpitar_jogos_serie_A.class);
                        break;
                    case "SerieB":
                        palpitarJogos = new Intent(context,M_palpitar_jogos_serie_B.class);
                        break;
                    case "SerieC":
                        palpitarJogos = new Intent(context,M_palpitar_jogos_serie_C.class);
                        break;
                    case "SerieD":
                        palpitarJogos = new Intent(context,M_palpitar_jogos_serie_D.class);
                        break;
                }
                palpitarJogos.putExtra("passarSerie",passarResultado.passarSerie);
                palpitarJogos.putExtra("passarFase",passarResultado.passarFase);
                palpitarJogos.putExtra("passarGrupo",passarResultado.passarGrupo);
                palpitarJogos.putExtra("passarRodada",passarResultado.passarRodada);
                palpitarJogos.putExtra("passarJogo",passarResultado.passarJogo);
                startActivity(palpitarJogos);
                finish();
            });

            return convertView;
        }

        class ViewHolder {
            TextView valorRodada,valorJogo,
                    valorData,valorLocal,valorHorario,
                    valorTimes,valorTimeMandante,valorTimesX,valorTimeVisitante,
                    valorResultado,valorResultadoMandante,valorResultadoX,valorResultadoVisitante,
                    valorPalpite,valorPalpiteMandante,valorPalpiteX,valorPalpiteVisitante,
                    editarPalpite;
            LinearLayout linhaPalpite;
            RelativeLayout linhaEditarPalpite,linhaPalpiteCongelado;
        }
    }

    public void onBackPressed(){
        Intent menuInicial = new Intent(this, A_menu_inicial.class);
        startActivity(menuInicial);
        finish();
    }

    //***********************MENU***********************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_g_tabela_classificacao, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // return true so that the menu pop up is opened
        List<Object> ids = new ArrayList<>();
        ids.add(R.id.instrucoes);
        ids.add(R.id.atualizar_dados);
        ids.add(R.id.voltar);

        switch (ids.indexOf(item.getItemId())) {
            case 0:
                Intent instruction = new Intent(this,B_instrucoes.class);
                instruction.putExtra("tela","G_resultados_dos_jogos");
                startActivity(instruction);
                break;
            case 1:
                if (redeDisponivel) {
                    Intent atualizarDados = new Intent(this, Z03_atualizar_dados.class);
                    atualizarDados.putExtra("origem","G_resultados_dos_jogos");
                    startActivity(atualizarDados);
                    finish();
                } else {
                    Toast.makeText(this, "Rede indisponível.", Toast.LENGTH_LONG).show();
                }
                break;
            case 2:
                Intent menuInicial = new Intent(this, A_menu_inicial.class);
                startActivity(menuInicial);
                finish();
        }
        return true;
    }
}
