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
import java.util.Objects;

public class B_instrucoes extends AppCompatActivity {

    private AdView adView;
    private String tela;
    private View blocoInstrucoes;
    private View blocoCreditos;
    private TextView textoInstrucoes01;
    private TextView textoInstrucoes02;
    private TextView textoInstrucoes03;
    private TextView textoCreditos;
    private SharedPreferences instrucoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.b_instrucoes);
        loadAds();
        blocoInstrucoes = findViewById(R.id.blocoInstrucoes);
        blocoCreditos = findViewById(R.id.blocoCreditos);
        instrucoes = getSharedPreferences("instrucoes",MODE_PRIVATE);
        textoInstrucoes01 = findViewById(R.id.textoInstrucoes01);
        textoInstrucoes02 = findViewById(R.id.textoInstrucoes02);
        textoInstrucoes03 = findViewById(R.id.textoInstrucoes03);
        textoCreditos = findViewById(R.id.textoCreditos);
        recebeDados();
        exibirInstrucoes();
    }

//-----Início Bloco Exibição de Anúncios-----
    private void loadAds(){
        adView = findViewById(R.id.adsInstrucoes);
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
        Intent instrucoes = getIntent();
        tela = instrucoes.getStringExtra("tela") + "_";
    }

    private void exibirInstrucoes(){
        blocoInstrucoes.setVisibility(View.VISIBLE);
        blocoCreditos.setVisibility(View.GONE);
        String description01 = (String) textoInstrucoes01.getContentDescription();
        String description02 = (String) textoInstrucoes02.getContentDescription();
        String description03 = (String) textoInstrucoes03.getContentDescription();
        if (Objects.equals(instrucoes.getString(tela + description02, "Vídeo não disponível"), "Vídeo não disponível")){
            textoInstrucoes01.setVisibility(View.GONE);
            textoInstrucoes02.setVisibility(View.GONE);
        }
        else{
            textoInstrucoes01.setText(instrucoes.getString("B_instrucoes_" + description01, "--"));
            textoInstrucoes02.setText(instrucoes.getString(tela + description02, "--"));
        }
        textoInstrucoes03.setText(instrucoes.getString(tela + description03, "--"));
    }

    private void exibirCreditos(){
        blocoInstrucoes.setVisibility(View.GONE);
        blocoCreditos.setVisibility(View.VISIBLE);
        textoCreditos.setText(instrucoes.getString("textoCreditos", "--"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_b_instrucoes, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // return true so that the menu pop up is opened
        List<Object> ids = new ArrayList<>();
        ids.add(R.id.instrucoes);
        ids.add(R.id.creditos);
        ids.add(R.id.voltar);

        switch (ids.indexOf(item.getItemId())) {
            case 0:
                exibirInstrucoes();
                break;
            case 1:
                exibirCreditos();
                break;
            case 2:
                exit(item);
        }
        return true;
    }

    private void exit(@SuppressWarnings("unused") MenuItem item) {
        System.exit(0);
    }
}
