package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class D_serie extends AppCompatActivity {

    private AdView adView;
    private ArrayList<String> listaPontuacoes;
    private String ano;
    private String serieEscolhida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_serie);
        loadAds();
        getDadosComuns();
        resgataArrays();
        recebeDados();
        atualizaCampos();
    }

    //-----Início Bloco Exibição de Anúncios-----
    private void loadAds(){
        adView = findViewById(R.id.adsSerie);
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

    private void resgataArrays(){
        SharedPreferences referencias = getSharedPreferences("referencias",MODE_PRIVATE);
        Map<String,?> referenciasKeys = referencias.getAll();
        listaPontuacoes = new ArrayList<>();
        for (Map.Entry<String, ?> entry : referenciasKeys.entrySet()){
            if (entry.getValue().equals("pontuacoes")) {
                listaPontuacoes.add(entry.getKey());
            }
        }
    }

    private void recebeDados(){
        //*********************Recebe dados de outra tela(Intent)*********************
        Intent serie = getIntent();
        String letraSerie = serie.getStringExtra("serieEscolhida");
        String title = "Série " + letraSerie + " - " + ano;
        serieEscolhida = "Serie"+letraSerie;
        //*********************(Fim do escopo do comentário)************************

        //*********************Adiciona dados recebidos de outra tela(Intent)*********************
        TextView titulo = findViewById(R.id.series);
        titulo.setText(title);
        //*********************(Fim do escopo do comentário)************************

        //*********************Ajusta visibilidades*********************
        switch (Objects.requireNonNull(letraSerie)){
            case "A":
                String[] invisivelA = {"acesso","quantidadeAcesso","pontuacaoAcesso"};
                for (String setInvisible:invisivelA) {
                    @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(setInvisible, "id", getPackageName());
                    TextView text = findViewById(resId);
                    text.setVisibility(View.GONE);
                }
                break;
            case "B":
                String[] invisivelB = {"libertadores","quantidadeLibertadores","pontuacaoLibertadores","sulamericana","quantidadeSulamericana","pontuacaoSulamericana"};
                for (String setInvisible:invisivelB) {
                    @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(setInvisible, "id", getPackageName());
                    TextView text = findViewById(resId);
                    text.setVisibility(View.GONE);
                }
                break;
            case "C":
                String[] invisivelC = {"libertadores","quantidadeLibertadores","pontuacaoLibertadores","sulamericana","quantidadeSulamericana","pontuacaoSulamericana"};
                for (String setInvisible:invisivelC) {
                    @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(setInvisible, "id", getPackageName());
                    TextView text = findViewById(resId);
                    text.setVisibility(View.GONE);
                }
                break;
            case "D":
                String[] invisivelD = {"libertadores","quantidadeLibertadores","pontuacaoLibertadores","sulamericana","quantidadeSulamericana","pontuacaoSulamericana","rebaixados","quantidadeRebaixados","pontuacaoRebaixados"};
                for (String setInvisible:invisivelD) {
                    @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(setInvisible, "id", getPackageName());
                    TextView text = findViewById(resId);
                    text.setVisibility(View.GONE);
                }
                break;
        }
        //*********************(Fim do escopo do comentário)************************
    }

    private void atualizaCampos(){
        //Atualiza campo "Total Pontos".
        SharedPreferences meusPontos = getSharedPreferences("meusPontos"+ano, MODE_PRIVATE);
        String pontos = meusPontos.getString("totalPontos"+serieEscolhida,"--");
        String totalPontos = getString(R.string.total_pontos)+": " + pontos + getString(R.string.pts);
        TextView atualizaCampo = findViewById(R.id.total_seus_pontos);
        atualizaCampo.setText(totalPontos);

        //Atualiza campos detalhados.
        for (String chave:listaPontuacoes) {
            String valor = meusPontos.getString(chave+serieEscolhida, "--");
            @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(chave, "id", getPackageName());
            TextView atualiza = findViewById(resId);
            atualiza.setText(valor);
        }
    }

    @Override
    public void onBackPressed(){
        Intent inicial = new Intent(this,A_tela_inicial.class);
        startActivity(inicial);
        finish();
    }

    //*********************MENU*********************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_d_serie, menu);
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
                instruction.putExtra("tela","D_serie");
                startActivity(instruction);
                break;
            case 1:
                onBackPressed();
        }
        return true;
    }
}
