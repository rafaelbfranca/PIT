package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;
import androidx.appcompat.app.AppCompatActivity;

public class Z02_baixar_dados_firebase extends AppCompatActivity implements ComponentCallbacks2 {
    private String ano;
    private String meuNumero;
    private StorageReference anoRef,usuariosRef,arquivosDoAppRef;
    private SharedPreferences dadosComuns;
    private File dirFiles;
    private boolean reinstalando;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_baixar_dados_firebase);
        dirFiles = getDir("files",MODE_PRIVATE);
        ballAnimation();
        getDadosComuns();
        SharedPreferences getMeuNumero = getSharedPreferences("meusDados",MODE_PRIVATE);
        meuNumero = getMeuNumero.getString("meuNumero","--");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        anoRef = storageRef.child("ano"+ano);
        usuariosRef = storageRef.child("usuarios");
        arquivosDoAppRef = storageRef.child("arquivosDoApp");
        baixarUpdateCadastro();
    }

    private void ballAnimation (){
        ImageView imgView = findViewById(R.id.animation_ball);
        AnimationDrawable ball_Animation = (AnimationDrawable) imgView.getBackground();
        ball_Animation.start();
    }

    private void getDadosComuns(){
        dadosComuns = getSharedPreferences("dadosComuns",MODE_PRIVATE);
        SharedPreferences.Editor dadosComunsEditor = dadosComuns.edit();
        dadosComunsEditor.putString("avisoNovaVersao","NOK");
        dadosComunsEditor.putString("avisoRedeIndisponivel","NOK");
        dadosComunsEditor.apply();
        reinstalando = Boolean.parseBoolean(dadosComuns.getString("reinstalando","true"));
        ano = dadosComuns.getString("ano","--");
    }

    private void setDadosComuns(String valor){
        //Dados que serão lidos por outras classes.
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        SharedPreferences.Editor editor = dadosComuns.edit();
        editor.putString("ano", valor);
        editor.apply();
    }

    private void baixarUpdateCadastro(){
        final SharedPreferences meusDados = getSharedPreferences("meusDados",MODE_PRIVATE);
        final String meuNumero = meusDados.getString("meuNumero","--");
        StorageReference fileRef = usuariosRef.child(meuNumero+".xml");
        final File usuarioLocalFile = new File(dirFiles, meuNumero+".xml");
        fileRef.getFile(usuarioLocalFile).addOnSuccessListener(taskSnapshot -> {
            try {
                FileInputStream fis = new FileInputStream(usuarioLocalFile);
                Properties properties = new Properties();
                properties.loadFromXML(fis);
                fis.close();
                String update = properties.getProperty("update");
                if (update != null && update.equals("true")){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        user.updatePassword(properties.getProperty("meuNumero"));
                        properties.setProperty("update", "false");
                        Enumeration<Object> enuKeys = properties.keys();
                        SharedPreferences.Editor meusDadosEditor = meusDados.edit();
                        while (enuKeys.hasMoreElements()) {
                            String key = (String) enuKeys.nextElement();
                            meusDadosEditor.putString(key, properties.getProperty(key));
                            properties.remove(key);
                        }
                        meusDadosEditor.apply();
                        FileOutputStream fos = new FileOutputStream(usuarioLocalFile);
                        properties.storeToXML(fos, "Update Done");
                        fos.flush();
                        fos.close();
                        try {
                            Runnable r = Z_runnable_subir_dados.newInstance(meuNumero,"update",ano,dirFiles);
                            new Thread(r).start();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                else{
                    //noinspection ResultOfMethodCallIgnored
                    usuarioLocalFile.delete();
                }
                baixarReferencias();
            } catch (IOException e) {
                //noinspection ResultOfMethodCallIgnored
                usuarioLocalFile.delete();
                baixarReferencias();
            }
        }).addOnFailureListener(exception -> baixarReferencias());
    }

    private void baixarReferencias(){
        StorageReference fileRef = arquivosDoAppRef.child("referencias.xml");
        final File referenciasLocalFile = new File(dirFiles, "referencias.xml");
        fileRef.getFile(referenciasLocalFile).addOnSuccessListener(taskSnapshot -> {
            try {
                FileInputStream fis = new FileInputStream(referenciasLocalFile);
                Properties properties = new Properties();
                properties.loadFromXML(fis);
                fis.close();
                Enumeration<Object> enuKeys = properties.keys();
                SharedPreferences referencias = getSharedPreferences("referencias",MODE_PRIVATE);
                SharedPreferences.Editor referenciasEditor = referencias.edit();
                while (enuKeys.hasMoreElements()) {
                    String key = (String) enuKeys.nextElement();
                    referenciasEditor.putString(key, properties.getProperty(key));
                }
                referenciasEditor.apply();
                //noinspection ResultOfMethodCallIgnored
                referenciasLocalFile.delete();
                baixarInstrucoes();
            } catch (IOException e) {
                e.printStackTrace();
                baixarInstrucoes();
            }
        }).addOnFailureListener(exception -> baixarInstrucoes());
    }

    private void baixarInstrucoes(){
        StorageReference fileRef = arquivosDoAppRef.child("instrucoes.xml");
        final File instrucoesLocalFile = new File(dirFiles, "instrucoes.xml");
        fileRef.getFile(instrucoesLocalFile).addOnSuccessListener(taskSnapshot -> {
            try {
                FileInputStream fis = new FileInputStream(instrucoesLocalFile);
                Properties properties = new Properties();
                properties.loadFromXML(fis);
                fis.close();
                Enumeration<Object> enuKeys = properties.keys();
                SharedPreferences instrucoes = getSharedPreferences("instrucoes",MODE_PRIVATE);
                SharedPreferences.Editor instrucoesEditor = instrucoes.edit();
                while (enuKeys.hasMoreElements()) {
                    String key = (String) enuKeys.nextElement();
                    instrucoesEditor.putString(key, properties.getProperty(key));
                }
                instrucoesEditor.apply();
                //noinspection ResultOfMethodCallIgnored
                instrucoesLocalFile.delete();
                baixarDadosCampeonatos();
            } catch (IOException e) {
                e.printStackTrace();
                baixarDadosCampeonatos();
            }
        }).addOnFailureListener(exception -> baixarDadosCampeonatos());
    }

    private void baixarDadosCampeonatos(){
        StorageReference fileRef = arquivosDoAppRef.child("dadosCampeonatos.xml");
        final File dadosCampeonatosLocalFile = new File(dirFiles, "dadosCampeonatos.xml");
        fileRef.getFile(dadosCampeonatosLocalFile).addOnSuccessListener(taskSnapshot -> {
            try {
                FileInputStream fis = new FileInputStream(dadosCampeonatosLocalFile);
                Properties properties = new Properties();
                properties.loadFromXML(fis);
                fis.close();
                Enumeration<Object> enuKeys = properties.keys();
                SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
                SharedPreferences.Editor dadosCampeonatosEditor = dadosCampeonatos.edit();
                while (enuKeys.hasMoreElements()) {
                    String key = (String) enuKeys.nextElement();
                    dadosCampeonatosEditor.putString(key, properties.getProperty(key));
                }
                dadosCampeonatosEditor.apply();
                //noinspection ResultOfMethodCallIgnored
                dadosCampeonatosLocalFile.delete();
                baixarPontuacoes();
            } catch (IOException e) {
                e.printStackTrace();
                baixarPontuacoes();
            }
        }).addOnFailureListener(exception -> baixarPontuacoes());
    }

    private void baixarPontuacoes(){
        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos", MODE_PRIVATE);
        int i = 0;
        while (!Objects.equals(dadosCampeonatos.getString("dadosCampeonatoAno" + i, "--"), "--")) {
            i++;
        }
        String[] listaAnoDoCampeonato = new String[i];
        for (int j = 0; j < i; j++) {
            listaAnoDoCampeonato[j] = dadosCampeonatos.getString("dadosCampeonatoAno" + j, "--");
        }
        for (final String anoCampeonato:listaAnoDoCampeonato) {
            StorageReference fileRef = arquivosDoAppRef.child("pontuacoes" + anoCampeonato + ".xml");
            final File pontuacoesLocalFile = new File(dirFiles, "pontuacoes" + anoCampeonato + ".xml");
            fileRef.getFile(pontuacoesLocalFile).addOnSuccessListener(taskSnapshot -> {
                try {
                    FileInputStream fis = new FileInputStream(pontuacoesLocalFile);
                    Properties properties = new Properties();
                    properties.loadFromXML(fis);
                    fis.close();
                    Enumeration<Object> enuKeys = properties.keys();
                    SharedPreferences pontuacoes = getSharedPreferences("pontuacoes" + anoCampeonato, MODE_PRIVATE);
                    SharedPreferences.Editor pontuacoesEditor = pontuacoes.edit();
                    while (enuKeys.hasMoreElements()) {
                        String key = (String) enuKeys.nextElement();
                        pontuacoesEditor.putString(key, properties.getProperty(key));
                    }
                    pontuacoesEditor.apply();
                    //noinspection ResultOfMethodCallIgnored
                    pontuacoesLocalFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).addOnFailureListener(exception -> {

            });
        }
        baixarBackups();
    }

    private void baixarBackups(){
        if (reinstalando){
            reinstalando = false;
            SharedPreferences.Editor dadosComunsEditor = dadosComuns.edit();
            dadosComunsEditor.putString("reinstalando","false");
            dadosComunsEditor.apply();
            registraAno();
            baixarConvites(false);
            chamarGetBackups();
        }
        else{
            baixarConvites(true);
        }
    }

    private void registraAno(){
        Date momento = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        ano = formatter.format(momento);
        setDadosComuns(ano);
    }

    private void baixarConvites(boolean atualizarDados){
        StorageReference convitesRef = anoRef.child("convites" + ano);

        String conviteFile;
        if (meuNumero.length()>=9){
            conviteFile = "convites" + meuNumero.substring(meuNumero.length()-9);
        }else {
            conviteFile = "convites" + meuNumero;
        }

        StorageReference fileRef = convitesRef.child(conviteFile + ".xml");
        final File conviteLocalFile = new File(dirFiles, conviteFile +".xml");
        fileRef.getFile(conviteLocalFile).addOnSuccessListener(taskSnapshot -> {
            try {
                FileInputStream fis = new FileInputStream(conviteLocalFile);
                Properties properties = new Properties();
                properties.loadFromXML(fis);
                fis.close();
                Enumeration<Object> enuKeys = properties.keys();
                SharedPreferences.Editor dadosComunsEditor = dadosComuns.edit();
                dadosComunsEditor.putString("convitesPendentes", "false");
                while (enuKeys.hasMoreElements()) {
                    String keyConvite = (String) enuKeys.nextElement();
                    if (keyConvite.contains("nomeConvidante")) {
                        dadosComunsEditor.putString("convitesPendentes", "true");
                        dadosComunsEditor.apply();
                        break;
                    }
                }
                dadosComunsEditor.apply();
                if (Objects.equals(dadosComuns.getString("convitesPendentes", "false"), "false")){
                    //noinspection ResultOfMethodCallIgnored
                    conviteLocalFile.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (atualizarDados){
                    chamarAtualizarDados();
                }
            }
        }).addOnFailureListener(exception -> {
            if (atualizarDados){
                chamarAtualizarDados();
            }
        });
    }

    private void chamarGetBackups(){
        Intent getBackups = new Intent(this, Z_get_backups.class);
        getBackups.putExtra("origem","Z02_baixar_dados_firebase");
        startActivity(getBackups);
        finish();
    }

    private void chamarAtualizarDados(){
        Intent atualizarDados = new Intent(this, Z03_atualizar_dados.class);
        atualizarDados.putExtra("origem","Z02_baixar_dados_firebase");
        startActivity(atualizarDados);
        finish();
    }

    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}
