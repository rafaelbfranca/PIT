package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class Z03_atualizar_dados extends AppCompatActivity implements ComponentCallbacks2 {

    private String anoAtual,origem;
    private String ano;
    private ArrayList<String> downloadFiles;
    private StorageReference arquivosDoAppRef;
    private File dirFiles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_atualizar_dados);
        dirFiles = getDir("files",MODE_PRIVATE);
        recebeDados();
        ballAnimation();
        registraData();
        getDadosComuns();
        limparDadosComuns();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        arquivosDoAppRef = storageRef.child("arquivosDoApp");
        resgataArrays();
    }

    private void recebeDados(){
        Intent atualizarDados = getIntent();
        origem = atualizarDados.getStringExtra("origem");
    }

    private void ballAnimation (){
        ImageView imgView = findViewById(R.id.animation_ball);
        AnimationDrawable ball_Animation = (AnimationDrawable) imgView.getBackground();
        ball_Animation.start();
    }

    private void registraData(){
        Date momento = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        String hoje = formatter.format(momento);
        setDadosComuns("atualizadoEm", hoje);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy");
        anoAtual = formatter2.format(momento);
    }

    private void setDadosComuns(String chave, String valor){
        //Dados que serão lidos por outras classes.
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        SharedPreferences.Editor editor = dadosComuns.edit();
        editor.putString(chave, valor);
        editor.apply();
    }

    private void getDadosComuns(){
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        ano = dadosComuns.getString("ano","--");
        if (Objects.equals(ano, "--")){
            ano = anoAtual;
            setDadosComuns("ano", ano);
        }
    }

    private void limparDadosComuns(){
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns",MODE_PRIVATE);
        SharedPreferences.Editor dadosComunsEditor = dadosComuns.edit();
        dadosComunsEditor.remove("E_meus_grupos");
        dadosComunsEditor.apply();
    }

    private void resgataArrays(){
        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
        SharedPreferences referencias = getSharedPreferences("referencias",MODE_PRIVATE);
        Map<String,?> referenciasKeys = referencias.getAll();

        ArrayList<String> listaSeries = new ArrayList<>();
        for (Map.Entry<String, ?> entry : referenciasKeys.entrySet()){
            if (entry.getValue().equals("series_array2")){
                listaSeries.add(entry.getKey());
            }
        }

        downloadFiles = new ArrayList<>();
        for (Map.Entry<String, ?> entry : referenciasKeys.entrySet()){
            if (entry.getValue().equals("downloadFiles")){
                for (String serie : listaSeries) {
                    downloadFiles.add(entry.getKey() + serie + ano);
                }
            }
        }
        for (String serie : listaSeries) {
            int totalFases = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + serie + ano + "TotalFases", "1")));
            for (int k = 1; k <= totalFases; k++) {
                int totalGruposNaFase = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + serie + ano + "F" + k + "TotalGruposNaFase", "1")));
                for (int j = 1; j <=totalGruposNaFase; j++) {
                    downloadFiles.add("tabela" + serie + "F" + k + "G" + j + ano);
                }
            }
        }
        baixarDownloadFiles();
    }

    private void baixarDownloadFiles(){
        int cont = 0;
        for (final String file:downloadFiles) {
            cont++;
            StorageReference fileRef = arquivosDoAppRef.child(file + ".xml");
            final File downloadLocalFile = new File(dirFiles, file +".xml");
            final int finalCont = cont;
            fileRef.getFile(downloadLocalFile).addOnSuccessListener(taskSnapshot -> {
                try {
                    FileInputStream fis = new FileInputStream(downloadLocalFile);
                    Properties properties = new Properties();
                    properties.loadFromXML(fis);
                    fis.close();
                    Enumeration<Object> enuKeys = properties.keys();
                    SharedPreferences downloadFile = getSharedPreferences(file,MODE_PRIVATE);
                    SharedPreferences.Editor downloadFileEditor = downloadFile.edit();
                    while (enuKeys.hasMoreElements()) {
                        String key = (String) enuKeys.nextElement();
                        downloadFileEditor.putString(key, properties.getProperty(key));
                    }
                    downloadFileEditor.apply();
                    //noinspection ResultOfMethodCallIgnored
                    downloadLocalFile.delete();
                    if (finalCont == downloadFiles.size()){
                        chamarCalcularPontos();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (finalCont == downloadFiles.size()){
                        chamarCalcularPontos();
                    }
                }
            }).addOnFailureListener(exception -> {
                if (finalCont == downloadFiles.size()){
                    chamarCalcularPontos();
                }
            });
        }
    }

    private void chamarCalcularPontos(){
        Intent calcularPontos = new Intent(this, Z04_calcular_pontos.class);
        calcularPontos.putExtra("origem",origem);
        startActivity(calcularPontos);
        finish();
    }

    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}
