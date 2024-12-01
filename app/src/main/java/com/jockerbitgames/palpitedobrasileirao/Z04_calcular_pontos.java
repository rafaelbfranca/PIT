package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.appcompat.app.AppCompatActivity;

public class Z04_calcular_pontos extends AppCompatActivity implements ComponentCallbacks2 {

    private File dirFiles;
    private String ano,origem;
    private String hoje;
    private String[] valoresPontuacoes;
    private ArrayList<String> listaPontuacoes;
    private ArrayList<String> listaSeries;
    private final String[] palpiteTimeCampeao = {"Time", "Pts", "SG"};
    private final String[] palpiteLibertadores = {"01", "02", "03", "04", "05", "06"};
    private final String[] palpiteSulamericana = {"01", "02", "03", "04", "05", "06"};
    private final String[] palpiteAcesso = {"01", "02", "03", "04"};
    private final String[] palpiteRebaixado = {"01", "02", "03", "04"};
    private final String[] palpiteTimeLanterna = {"Time", "Pts", "SG"};
    private final String[] tabelaTimeCampeao = {"Time", "Pts", "SG"};
    private final String[] tabelaLibertadores = {"01", "02", "03", "04", "05", "06", "07", "08", "09"};
    private final String[] tabelaSulamericana = {"01", "02", "03", "04", "05", "06"};
    private final String[] tabelaAcesso = {"01", "02", "03", "04"};
    private final String[] tabelaRebaixado = {"01", "02", "03", "04"};
    private final String[] tabelaTimeLanterna = {"Time", "Pts", "SG"};
    private List<String> listaTabelas;
    private AtomicBoolean erro;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_calcular_pontos);
        erro = new AtomicBoolean(false);
        dirFiles = getDir("files",MODE_PRIVATE);
        recebeDados();
        ballAnimation();
        registraData();
        getDadosComuns();
        resgataArrays();
        calculaPontos();
    }

    private void recebeDados(){
        Intent calcularPontos = getIntent();
        origem = calcularPontos.getStringExtra("origem");
    }

    private void ballAnimation (){
        ImageView imgView = findViewById(R.id.animation_ball);
        AnimationDrawable ball_Animation = (AnimationDrawable) imgView.getBackground();
        ball_Animation.start();
    }

    private void registraData(){
        Date momento = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        hoje = formatter.format(momento);
        setDadosComuns(hoje);
    }

    private void setDadosComuns(String valor){
        //Dados que serão lidos por outras classes.
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        SharedPreferences.Editor editor = dadosComuns.edit();
        editor.putString("atualizadoEm", valor);
        editor.apply();
    }

    private void getDadosComuns(){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        ano = getDadosComuns.getString("ano","--");
    }

    private void resgataArrays(){
        SharedPreferences referencias = getSharedPreferences("referencias",MODE_PRIVATE);
        Map<String,?> referenciasKeys = referencias.getAll();
        listaPontuacoes = new ArrayList<>();
        listaSeries = new ArrayList<>();
        for (Map.Entry<String, ?> entry : referenciasKeys.entrySet()){
            switch (entry.getValue().toString()){
                case "pontuacoes":
                    listaPontuacoes.add(entry.getKey());
                    break;
                case "series_array2":
                    listaSeries.add(entry.getKey());
                    break;
            }
        }
        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
        valoresPontuacoes = new String[listaPontuacoes.size()];
        listaTabelas = new ArrayList<>();
        for (String serie:listaSeries) {
            int totalFases = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + serie + ano + "TotalFases", "1")));
            for (int k = 1; k <= totalFases; k++) {
                int totalGruposNaFase = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + serie + ano + "F" + k + "TotalGruposNaFase", "1")));
                for (int j = 1; j <=totalGruposNaFase; j++) {
                    listaTabelas.add("tabela" + serie + "F" + k + "G" + j + ano);
                }
            }
        }
    }

    private void calculaPontos(){
        try {
            for (String serieEscolhida:listaSeries) {
                Arrays.fill(valoresPontuacoes, "0");
                //Calcular pontos dos jogos - Placares, Vencedores, Empates.
                SharedPreferences resMandante = getSharedPreferences("resultadomandante" + serieEscolhida + ano, MODE_PRIVATE);
                SharedPreferences pontuacoes = getSharedPreferences("pontuacoes" + ano, MODE_PRIVATE);
                SharedPreferences resVisitante = getSharedPreferences("resultadovisitante" + serieEscolhida + ano, MODE_PRIVATE);
                SharedPreferences meusPalpites = getSharedPreferences("meusPalpites" + serieEscolhida + ano, MODE_PRIVATE);
                SharedPreferences meusPontos = getSharedPreferences("meusPontos" + ano, MODE_PRIVATE);
                SharedPreferences.Editor editorMeusPontos = meusPontos.edit();
                if (!meusPontos.contains("arquivo")) {
                    editorMeusPontos.putString("arquivo", "criado");
                }
                if (!meusPontos.contains("pontuacaoPlacar" + serieEscolhida)) {
                    editorMeusPontos.putString("pontuacaoPlacar" + serieEscolhida, "0");
                    editorMeusPontos.putString("pontuacaoVencedor" + serieEscolhida, "0");
                    editorMeusPontos.putString("pontuacaoEmpate" + serieEscolhida, "0");
                    editorMeusPontos.putString("quantidadePlacar" + serieEscolhida, "0");
                    editorMeusPontos.putString("quantidadeVencedor" + serieEscolhida, "0");
                    editorMeusPontos.putString("quantidadeEmpate" + serieEscolhida, "0");
                }
                Map<String, ?> resultadoMandante = resMandante.getAll();
                for (Map.Entry<String, ?> entry : resultadoMandante.entrySet()) {
                    String chaveResultadoMandante = String.valueOf(entry.getKey());
                    String chaveMeusPontos = chaveResultadoMandante.replace("resultadomandante", ano).replace("resultado", "pontos");
                    if (!meusPontos.contains(chaveMeusPontos)){
                        editorMeusPontos.putString(chaveMeusPontos, "--");
                    }
                }
                editorMeusPontos.apply();

                int pontuacaoPlacar = 0;
                int pontuacaoEmpate = 0;
                int pontuacaoVencedor = 0;
                int quantidadePlacar = 0;
                int quantidadeEmpate = 0;
                int quantidadeVencedor = 0;

                Map<String, ?> meusPontosMap = meusPontos.getAll();
                for (Map.Entry<String, ?> entry : meusPontosMap.entrySet()){
                    if (entry.getValue().equals("--")) {
                        String chaveMeusPontosMap = String.valueOf(entry.getKey());
                        String chaveResultadoMandante = chaveMeusPontosMap.replace(ano, "resultadomandante").replace("pontos", "resultado");
                        String valorResultadoMandante = resMandante.getString(chaveResultadoMandante, "--");
                        if (!Objects.equals(valorResultadoMandante, "--")) {
                            String chaveResultadoVisitante = chaveResultadoMandante.replace("resultadomandante", "resultadovisitante");
                            String valorResultadoVisitante = resVisitante.getString(chaveResultadoVisitante, "v");
                            if (!Objects.equals(valorResultadoVisitante, "--")) {
                                String chavePalpiteMandante = chaveResultadoMandante.replace("resultado", "palpite");
                                String valorPalpiteMandante = meusPalpites.getString(chavePalpiteMandante, "-1");
                                String chavePalpiteVisitante = chavePalpiteMandante.replace("mandante", "visitante");
                                String valorPalpiteVisitante = meusPalpites.getString(chavePalpiteVisitante, "-1");
                                if ((!Objects.equals(valorPalpiteMandante, "--")) && (!Objects.equals(valorPalpiteVisitante, "--"))) {
                                    if ((Integer.parseInt(valorPalpiteMandante) >= 0)) {
                                        if (Integer.parseInt(valorPalpiteVisitante) >= 0) {
                                            int pontosNesteJogo = 0;
                                            if ((valorPalpiteMandante.equals(valorResultadoMandante)) && (valorPalpiteVisitante.equals(valorResultadoVisitante))) {//Placar e resultado certos.
                                                pontosNesteJogo = Integer.parseInt(Objects.requireNonNull(pontuacoes.getString("pontuacaoPlacar", "0")));
                                                pontuacaoPlacar += pontosNesteJogo;
                                                quantidadePlacar++;
                                                if (valorPalpiteMandante.equals(valorPalpiteVisitante)) {
                                                    pontosNesteJogo += Integer.parseInt(Objects.requireNonNull(pontuacoes.getString("pontuacaoEmpate", "0")));
                                                    pontuacaoEmpate += Integer.parseInt(Objects.requireNonNull(pontuacoes.getString("pontuacaoEmpate", "0")));
                                                    quantidadeEmpate++;
                                                } else {
                                                    pontosNesteJogo += Integer.parseInt(Objects.requireNonNull(pontuacoes.getString("pontuacaoVencedor", "0")));
                                                    pontuacaoVencedor += Integer.parseInt(Objects.requireNonNull(pontuacoes.getString("pontuacaoVencedor", "0")));
                                                    quantidadeVencedor++;
                                                }
                                            } else {//Apenas o resultado certo.
                                                if ((Integer.parseInt(valorPalpiteMandante) > Integer.parseInt(valorPalpiteVisitante)) && (Integer.parseInt(Objects.requireNonNull(valorResultadoMandante)) > Integer.parseInt(Objects.requireNonNull(valorResultadoVisitante)))) {
                                                    pontosNesteJogo = Integer.parseInt(Objects.requireNonNull(pontuacoes.getString("pontuacaoVencedor", "0")));
                                                    pontuacaoVencedor += pontosNesteJogo;
                                                    quantidadeVencedor++;
                                                } else if ((Integer.parseInt(valorPalpiteMandante) < Integer.parseInt(valorPalpiteVisitante)) && (Integer.parseInt(Objects.requireNonNull(valorResultadoMandante)) < Integer.parseInt(Objects.requireNonNull(valorResultadoVisitante)))) {
                                                    pontosNesteJogo = Integer.parseInt(Objects.requireNonNull(pontuacoes.getString("pontuacaoVencedor", "0")));
                                                    pontuacaoVencedor += pontosNesteJogo;
                                                    quantidadeVencedor++;
                                                } else if ((Integer.parseInt(valorPalpiteMandante) == Integer.parseInt(valorPalpiteVisitante)) && (Integer.parseInt(Objects.requireNonNull(valorResultadoMandante)) == Integer.parseInt(Objects.requireNonNull(valorResultadoVisitante)))) {
                                                    pontosNesteJogo = Integer.parseInt(Objects.requireNonNull(pontuacoes.getString("pontuacaoEmpate", "0")));
                                                    pontuacaoEmpate += pontosNesteJogo;
                                                    quantidadeEmpate++;
                                                }
                                            }
                                            editorMeusPontos.putString(chaveMeusPontosMap, String.valueOf(pontosNesteJogo));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                int valorPontos;
                if(pontuacaoPlacar > 0){
                    valorPontos = Integer.parseInt(Objects.requireNonNull(meusPontos.getString("pontuacaoPlacar" + serieEscolhida, "0")));
                    valorPontos += pontuacaoPlacar;
                    editorMeusPontos.putString("pontuacaoPlacar" + serieEscolhida, String.valueOf(valorPontos));
                }
                if (pontuacaoVencedor > 0) {
                    valorPontos = Integer.parseInt(Objects.requireNonNull(meusPontos.getString("pontuacaoVencedor" + serieEscolhida, "0")));
                    valorPontos += pontuacaoVencedor;
                    editorMeusPontos.putString("pontuacaoVencedor" + serieEscolhida, String.valueOf(valorPontos));
                }
                if (pontuacaoEmpate > 0) {
                    valorPontos = Integer.parseInt(Objects.requireNonNull(meusPontos.getString("pontuacaoEmpate" + serieEscolhida, "0")));
                    valorPontos += pontuacaoEmpate;
                    editorMeusPontos.putString("pontuacaoEmpate" + serieEscolhida, String.valueOf(valorPontos));
                }
                if (quantidadePlacar > 0) {
                    valorPontos = Integer.parseInt(Objects.requireNonNull(meusPontos.getString("quantidadePlacar" + serieEscolhida, "0")));
                    valorPontos += quantidadePlacar;
                    editorMeusPontos.putString("quantidadePlacar" + serieEscolhida, String.valueOf(valorPontos));
                }
                if (quantidadeVencedor > 0) {
                    valorPontos = Integer.parseInt(Objects.requireNonNull(meusPontos.getString("quantidadeVencedor" + serieEscolhida, "0")));
                    valorPontos += quantidadeVencedor;
                    editorMeusPontos.putString("quantidadeVencedor" + serieEscolhida, String.valueOf(valorPontos));
                }
                if (quantidadeEmpate > 0) {
                    valorPontos = Integer.parseInt(Objects.requireNonNull(meusPontos.getString("quantidadeEmpate" + serieEscolhida, "0")));
                    valorPontos += quantidadeEmpate;
                    editorMeusPontos.putString("quantidadeEmpate" + serieEscolhida, String.valueOf(valorPontos));
                }
                editorMeusPontos.apply();

                //Ler palpites das classificações dos times.
                SharedPreferences lerPalpites = getSharedPreferences("meusPalpites" + serieEscolhida + ano, MODE_PRIVATE);
                palpiteTimeCampeao[0] = lerPalpites.getString("palpiteTimeCampeao" + serieEscolhida, "p");
                palpiteTimeCampeao[1] = lerPalpites.getString("palpitePontosTimeCampeao" + serieEscolhida, "p");
                palpiteTimeCampeao[2] = lerPalpites.getString("palpiteSaldoTimeCampeao" + serieEscolhida, "p");
                if (serieEscolhida.equals("SerieA")) {
                    palpiteLibertadores[0] = palpiteTimeCampeao[0];
                    palpiteLibertadores[1] = lerPalpites.getString("palpiteLibertadores01" + serieEscolhida, "p");
                    palpiteLibertadores[2] = lerPalpites.getString("palpiteLibertadores02" + serieEscolhida, "p");
                    palpiteLibertadores[3] = lerPalpites.getString("palpiteLibertadores03" + serieEscolhida, "p");
                    palpiteLibertadores[4] = lerPalpites.getString("palpiteLibertadores04" + serieEscolhida, "p");
                    palpiteLibertadores[5] = lerPalpites.getString("palpiteLibertadores05" + serieEscolhida, "p");
                    palpiteSulamericana[0] = lerPalpites.getString("palpiteSulamericana01" + serieEscolhida, "p");
                    palpiteSulamericana[1] = lerPalpites.getString("palpiteSulamericana02" + serieEscolhida, "p");
                    palpiteSulamericana[2] = lerPalpites.getString("palpiteSulamericana03" + serieEscolhida, "p");
                    palpiteSulamericana[3] = lerPalpites.getString("palpiteSulamericana04" + serieEscolhida, "p");
                    palpiteSulamericana[4] = lerPalpites.getString("palpiteSulamericana05" + serieEscolhida, "p");
                    palpiteSulamericana[5] = lerPalpites.getString("palpiteSulamericana06" + serieEscolhida, "p");
                }
                else {
                    palpiteAcesso[0] = palpiteTimeCampeao[0];
                    palpiteAcesso[1] = lerPalpites.getString("palpiteAcesso01" + serieEscolhida, "p");
                    palpiteAcesso[2] = lerPalpites.getString("palpiteAcesso02" + serieEscolhida, "p");
                    palpiteAcesso[3] = lerPalpites.getString("palpiteAcesso03" + serieEscolhida, "p");
                }
                palpiteTimeLanterna[0] = lerPalpites.getString("palpiteTimeLanterna" + serieEscolhida, "p");
                palpiteTimeLanterna[1] = lerPalpites.getString("palpitePontosTimeLanterna" + serieEscolhida, "p");
                palpiteTimeLanterna[2] = lerPalpites.getString("palpiteSaldoTimeLanterna" + serieEscolhida, "p");
                if (!serieEscolhida.equals("SerieD")) {
                    palpiteRebaixado[0] = lerPalpites.getString("palpiteRebaixado01" + serieEscolhida, "p");
                    palpiteRebaixado[1] = lerPalpites.getString("palpiteRebaixado02" + serieEscolhida, "p");
                    palpiteRebaixado[2] = lerPalpites.getString("palpiteRebaixado03" + serieEscolhida, "p");
                    palpiteRebaixado[3] = palpiteTimeLanterna[0];
                }

                //Ler tabelas
                int acessoCD = 0;
                int rebaixaC = 1;
                for (String tabela:listaTabelas) {
                    if (tabela.contains(serieEscolhida)){
                        SharedPreferences lerTabela = getSharedPreferences(tabela,MODE_PRIVATE);
                        switch (serieEscolhida) {
                            case "SerieA":
                                Map<String, ?> allEntriesSerieA = lerTabela.getAll();
                                int lib = 1;
                                int sula = 0;
                                int rebaixaA = 1;
                                for (Map.Entry<String, ?> entry : allEntriesSerieA.entrySet()) {
                                    if (entry.getValue().equals("TimeCampeao")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaTimeCampeao[0] = lerTabela.getString(key, "t");
                                        tabelaLibertadores[0] = tabelaTimeCampeao[0];
                                        key = key.replace("Time", "Pts");
                                        tabelaTimeCampeao[1] = lerTabela.getString(key, "t");
                                        key = key.replace("Pts", "SG");
                                        tabelaTimeCampeao[2] = lerTabela.getString(key, "t");
                                    }
                                    if (entry.getValue().equals("Libertadores")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaLibertadores[lib] = lerTabela.getString(key, "t");
                                        lib++;
                                    }
                                    if (entry.getValue().equals("Sulamericana")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaSulamericana[sula] = lerTabela.getString(key, "t");
                                        sula++;
                                    }
                                    if (entry.getValue().equals("Rebaixado")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaRebaixado[rebaixaA] = lerTabela.getString(key, "t");
                                        rebaixaA++;
                                    }
                                    if (entry.getValue().equals("TimeLanterna")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaTimeLanterna[0] = lerTabela.getString(key, "t");
                                        tabelaRebaixado[0] = tabelaTimeLanterna[0];
                                        key = key.replace("Time", "Pts");
                                        tabelaTimeLanterna[1] = lerTabela.getString(key, "t");
                                        key = key.replace("Pts", "SG");
                                        tabelaTimeLanterna[2] = lerTabela.getString(key, "t");
                                    }
                                }
                                break;

                            case "SerieB":
                                Map<String, ?> allEntriesSerieB = lerTabela.getAll();
                                int acessoB = 1;
                                int rebaixaB = 1;
                                for (Map.Entry<String, ?> entry : allEntriesSerieB.entrySet()) {
                                    if (entry.getValue().equals("TimeCampeao")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaTimeCampeao[0] = lerTabela.getString(key, "t");
                                        tabelaAcesso[0] = tabelaTimeCampeao[0];
                                        key = key.replace("Time", "Pts");
                                        tabelaTimeCampeao[1] = lerTabela.getString(key, "t");
                                        key = key.replace("Pts", "SG");
                                        tabelaTimeCampeao[2] = lerTabela.getString(key, "t");
                                    }
                                    if (entry.getValue().equals("Acesso")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaAcesso[acessoB] = lerTabela.getString(key, "t");
                                        acessoB++;
                                    }
                                    if (entry.getValue().equals("Rebaixado")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaRebaixado[rebaixaB] = lerTabela.getString(key, "t");
                                        rebaixaB++;
                                    }
                                    if (entry.getValue().equals("TimeLanterna")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaTimeLanterna[0] = lerTabela.getString(key, "t");
                                        tabelaRebaixado[0] = tabelaTimeLanterna[0];
                                        key = key.replace("Time", "Pts");
                                        tabelaTimeLanterna[1] = lerTabela.getString(key, "t");
                                        key = key.replace("Pts", "SG");
                                        tabelaTimeLanterna[2] = lerTabela.getString(key, "t");
                                    }
                                }
                                break;

                            case "SerieC":
                                Map<String, ?> allEntriesSerieC = lerTabela.getAll();
                                for (Map.Entry<String, ?> entry : allEntriesSerieC.entrySet()) {
                                    if (entry.getValue().equals("TimeCampeao")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaTimeCampeao[0] = lerTabela.getString(key, "t");
                                        //tabelaAcesso[0] = tabelaTimeCampeao[0];
                                        key = key.replace("Time", "Pts");
                                        tabelaTimeCampeao[1] = lerTabela.getString(key, "t");
                                        key = key.replace("Pts", "SG");
                                        tabelaTimeCampeao[2] = lerTabela.getString(key, "t");
                                    }
                                    if (entry.getValue().equals("Acesso")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaAcesso[acessoCD] = lerTabela.getString(key, "t");
                                        acessoCD++;
                                    }
                                    if (entry.getValue().equals("Rebaixado")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaRebaixado[rebaixaC] = lerTabela.getString(key, "t");
                                        rebaixaC++;
                                    }
                                    if (entry.getValue().equals("TimeLanterna")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaTimeLanterna[0] = lerTabela.getString(key, "t");
                                        tabelaRebaixado[0] = tabelaTimeLanterna[0];
                                        key = key.replace("Time", "Pts");
                                        tabelaTimeLanterna[1] = lerTabela.getString(key, "t");
                                        key = key.replace("Pts", "SG");
                                        tabelaTimeLanterna[2] = lerTabela.getString(key, "t");
                                    }
                                }
                                break;

                            case "SerieD":
                                Map<String, ?> allEntriesSerieD = lerTabela.getAll();
                                for (Map.Entry<String, ?> entry : allEntriesSerieD.entrySet()) {
                                    if (entry.getValue().equals("TimeCampeao")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaTimeCampeao[0] = lerTabela.getString(key, "t");
                                        key = key.replace("Time", "Pts");
                                        tabelaTimeCampeao[1] = lerTabela.getString(key, "t");
                                        key = key.replace("Pts", "SG");
                                        tabelaTimeCampeao[2] = lerTabela.getString(key, "t");
                                    }
                                    if (entry.getValue().equals("Acesso")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaAcesso[acessoCD] = lerTabela.getString(key, "t");
                                        acessoCD++;
                                    }
                                    if (entry.getValue().equals("TimeLanterna")) {
                                        String key = entry.getKey();
                                        key = key.replace("Qualificador", "Time");
                                        tabelaTimeLanterna[0] = lerTabela.getString(key, "t");
                                        key = key.replace("Time", "Pts");
                                        tabelaTimeLanterna[1] = lerTabela.getString(key, "t");
                                        key = key.replace("Pts", "SG");
                                        tabelaTimeLanterna[2] = lerTabela.getString(key, "t");
                                    }
                                }
                                break;
                        }
                    }
                }

                //Comparar leituras e computar os pontos
                SharedPreferences arquivoPontuacoes = getSharedPreferences("pontuacoes" + ano, MODE_PRIVATE);
                //Time Campeão
                if (palpiteTimeCampeao[0].equals(tabelaTimeCampeao[0])) {
                    int position = listaPontuacoes.indexOf("quantidadeTimeCampeao");
                    valoresPontuacoes[position] = "1";
                    position = listaPontuacoes.indexOf("pontuacaoTimeCampeao");
                    valoresPontuacoes[position] = arquivoPontuacoes.getString("pontuacaoTimeCampeao", "0");
                }
                if (palpiteTimeCampeao[1].equals(tabelaTimeCampeao[1])) {
                    int position = listaPontuacoes.indexOf("quantidadePtsCampeao");
                    valoresPontuacoes[position] = "1";
                    position = listaPontuacoes.indexOf("pontuacaoPtsCampeao");
                    valoresPontuacoes[position] = arquivoPontuacoes.getString("pontuacaoPtsCampeao", "0");
                }
                if (palpiteTimeCampeao[2].equals(tabelaTimeCampeao[2])) {
                    int position = listaPontuacoes.indexOf("quantidadeSgCampeao");
                    valoresPontuacoes[position] = "1";
                    position = listaPontuacoes.indexOf("pontuacaoSgCampeao");
                    valoresPontuacoes[position] = arquivoPontuacoes.getString("pontuacaoSgCampeao", "0");
                }
                //Time Lanterna
                if (palpiteTimeLanterna[0].equals(tabelaTimeLanterna[0])) {
                    int position = listaPontuacoes.indexOf("quantidadeTimeLanterna");
                    valoresPontuacoes[position] = "1";
                    position = listaPontuacoes.indexOf("pontuacaoTimeLanterna");
                    valoresPontuacoes[position] = arquivoPontuacoes.getString("pontuacaoTimeLanterna", "0");
                }
                if (palpiteTimeLanterna[1].equals(tabelaTimeLanterna[1])) {
                    int position = listaPontuacoes.indexOf("quantidadePtsLanterna");
                    valoresPontuacoes[position] = "1";
                    position = listaPontuacoes.indexOf("pontuacaoPtsLanterna");
                    valoresPontuacoes[position] = arquivoPontuacoes.getString("pontuacaoPtsLanterna", "0");
                }
                if (palpiteTimeLanterna[2].equals(tabelaTimeLanterna[2])) {
                    int position = listaPontuacoes.indexOf("quantidadeSgLanterna");
                    valoresPontuacoes[position] = "1";
                    position = listaPontuacoes.indexOf("pontuacaoSgLanterna");
                    valoresPontuacoes[position] = arquivoPontuacoes.getString("pontuacaoSgLanterna", "0");
                }
                //Libertadores e Sulamericana
                if (serieEscolhida.equals("SerieA")) {
                    //Libertadores
                    int lib = 0;
                    for (String palpiteTime : palpiteLibertadores) {
                        for (String tabelaTime : tabelaLibertadores) {
                            if (palpiteTime.equals(tabelaTime)) {
                                lib++;
                            }
                        }
                    }
                    int pontos = Integer.parseInt(Objects.requireNonNull(arquivoPontuacoes.getString("pontuacaoLibertadores", "0")));
                    int position = listaPontuacoes.indexOf("quantidadeLibertadores");
                    valoresPontuacoes[position] = String.valueOf(lib);
                    position = listaPontuacoes.indexOf("pontuacaoLibertadores");
                    valoresPontuacoes[position] = String.valueOf(lib * pontos);
                    //Sulamericana
                    int sula = 0;
                    for (String palpiteTime : palpiteSulamericana) {
                        for (String tabelaTime : tabelaSulamericana) {
                            if (palpiteTime.equals(tabelaTime)) {
                                sula++;
                            }
                        }
                    }
                    pontos = Integer.parseInt(Objects.requireNonNull(arquivoPontuacoes.getString("pontuacaoSulamericana", "0")));
                    position = listaPontuacoes.indexOf("quantidadeSulamericana");
                    valoresPontuacoes[position] = String.valueOf(sula);
                    position = listaPontuacoes.indexOf("pontuacaoSulamericana");
                    valoresPontuacoes[position] = String.valueOf(sula * pontos);
                }
                //Acesso
                else {
                    int acesso = 0;
                    for (String palpiteTime : palpiteAcesso) {
                        for (String tabelaTime : tabelaAcesso) {
                            if (palpiteTime.equals(tabelaTime)) {
                                acesso++;
                            }
                        }
                    }
                    int pontos = Integer.parseInt(Objects.requireNonNull(arquivoPontuacoes.getString("pontuacaoAcesso", "0")));
                    int position = listaPontuacoes.indexOf("quantidadeAcesso");
                    valoresPontuacoes[position] = String.valueOf(acesso);
                    position = listaPontuacoes.indexOf("pontuacaoAcesso");
                    valoresPontuacoes[position] = String.valueOf(acesso * pontos);
                }
                //Rebaixados
                if (!serieEscolhida.equals("SerieD")) {
                    int rebaixa = 0;
                    for (String palpiteTime : palpiteRebaixado) {
                        for (String tabelaTime : tabelaRebaixado) {
                            if (palpiteTime.equals(tabelaTime)) {
                                rebaixa++;
                            }
                        }
                    }
                    int pontos = Integer.parseInt(Objects.requireNonNull(arquivoPontuacoes.getString("pontuacaoRebaixados", "0")));
                    int position = listaPontuacoes.indexOf("quantidadeRebaixados");
                    valoresPontuacoes[position] = String.valueOf(rebaixa);
                    position = listaPontuacoes.indexOf("pontuacaoRebaixados");
                    valoresPontuacoes[position] = String.valueOf(rebaixa * pontos);
                }

                //Resgate de outros dados já salvos no arquivo meusPontos apenas para completar o vetor valoresPontuacoes.
                int position = listaPontuacoes.indexOf("pontuacaoPlacar");
                valoresPontuacoes[position] = meusPontos.getString("pontuacaoPlacar" + serieEscolhida, "0");
                position = listaPontuacoes.indexOf("quantidadePlacar");
                valoresPontuacoes[position] = meusPontos.getString("quantidadePlacar" + serieEscolhida, "0");
                position = listaPontuacoes.indexOf("pontuacaoVencedor");
                valoresPontuacoes[position] = meusPontos.getString("pontuacaoVencedor" + serieEscolhida, "0");
                position = listaPontuacoes.indexOf("quantidadeVencedor");
                valoresPontuacoes[position] = meusPontos.getString("quantidadeVencedor" + serieEscolhida, "0");
                position = listaPontuacoes.indexOf("pontuacaoEmpate");
                valoresPontuacoes[position] = meusPontos.getString("pontuacaoEmpate" + serieEscolhida, "0");
                position = listaPontuacoes.indexOf("quantidadeEmpate");
                valoresPontuacoes[position] = meusPontos.getString("quantidadeEmpate" + serieEscolhida, "0");

                //Atualizar arquivo meusPontos
                //SharedPreferences.Editor editorMeusPontos = meusPontos.edit();
                for (String chave : listaPontuacoes) {
                    int p = listaPontuacoes.indexOf(chave);
                    String valor = valoresPontuacoes[p];
                    editorMeusPontos.putString(chave+serieEscolhida, valor);
                }
                editorMeusPontos.apply();
            }
            total();
        } catch (Exception e) {
            erro.set(true);
            chamarOrigem();
        }
    }

    private void total(){
        String arquivoMeusPontos = "meusPontos" + ano;
        SharedPreferences meusDados = getSharedPreferences("meusDados", MODE_PRIVATE);
        SharedPreferences.Editor editorMeusDados = meusDados.edit();
        SharedPreferences meusPontos = getSharedPreferences(arquivoMeusPontos, MODE_PRIVATE);
        SharedPreferences.Editor editorMeusPontos = meusPontos.edit();
        String meusPontosBackup = "meusPontos"+ano+"Backup"+meusDados.getString("meuNumero","--");
        Map<String,?> allEntries = meusPontos.getAll();
        try {
            for (String serie : listaSeries) {
                int totalPontos = 0;
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    if (entry.getKey().contains("pontuacao")){
                        if (entry.getKey().contains(serie)){
                            String value = (String) entry.getValue();//Lê cada valor relativo a cada key.
                            totalPontos += Integer.parseInt(value);
                        }
                    }
                }
                editorMeusPontos.putString("totalPontos" + serie, String.valueOf(totalPontos));//Escreve cada par "key,value" no destino.
                editorMeusDados.putString("totalPontos" + serie + ano, String.valueOf(totalPontos));//Escreve cada par "key,value" no destino.
                editorMeusDados.putString("atualizadoEm", hoje);//Escreve cada par "key,value" no destino.
            }
            editorMeusPontos.apply();
            editorMeusDados.apply();
        } catch (Exception e) {
            erro.set(true);
            throw new RuntimeException(e);
        }

        //Criar o arquivo meusPontosBackup no diretório app_files
        try {
            Properties properties = new Properties();
            for(Map.Entry<String,?> entry : allEntries.entrySet()){
                properties.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }
            File getFile = new File(dirFiles, meusPontosBackup + ".xml");
            FileOutputStream outputStream = new FileOutputStream(getFile);
            properties.storeToXML(outputStream, "meusPontosBackup");
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            erro.set(true);
            e.printStackTrace();
        }

        //Subir arquivos de backup
        try {
            Runnable r = Z_runnable_subir_dados.newInstance(meusPontosBackup,"meusPontosBackup",ano,dirFiles);
            new Thread(r).start();
        } catch (Exception e) {
            erro.set(true);
            throw new RuntimeException(e);
        }
        try {
            Runnable r = Z_runnable_subir_dados.newInstance(criarArquivoUsuario(),"usuario",ano,dirFiles);
            new Thread(r).start();
        } catch (Exception e) {
            erro.set(true);
            throw new RuntimeException(e);
        }

        chamarOrigem();
    }

    private String criarArquivoUsuario(){
        SharedPreferences preferences = getSharedPreferences("meusDados", Context.MODE_PRIVATE);
        String nomeArquivoUsuario = preferences.getString("meuNumero","SemNumero");
        Map<String,?> allEntries = preferences.getAll();
        Properties properties = new Properties();
        for(Map.Entry<String,?> entry : allEntries.entrySet()){
            properties.setProperty(entry.getKey(), entry.getValue().toString());
        }
        try {
            File getFile = new File(dirFiles, nomeArquivoUsuario + ".xml");
            FileOutputStream outputStream = new FileOutputStream(getFile);
            properties.storeToXML(outputStream, "usuario");
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nomeArquivoUsuario;
    }

    private void chamarOrigem(){
        if (erro.get()){
            showMesage();
        }
        switch (origem){
            case "A_tela_inicial":
                Intent telaInicial = new Intent(this, A_tela_inicial.class);
                startActivity(telaInicial);
                finish();
                break;
            case "G_resultados_dos_jogos":
                Intent resultadosJogos = new Intent(this, G_resultados_dos_jogos.class);
                startActivity(resultadosJogos);
                finish();
                break;
            case "G_tabela_classificacao":
                Intent tabelaClassificacao = new Intent(this, G_tabela_classificacao.class);
                startActivity(tabelaClassificacao);
                finish();
                break;
            default:
                Intent menuInicial = new Intent(this, A_menu_inicial.class);
                startActivity(menuInicial);
                finish();
        }
    }

    private void showMesage(){
        Toast.makeText(this, "Ocorreu um erro ao calcular seus pontos.", Toast.LENGTH_SHORT).show();
    }

    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}
