package com.jockerbitgames.palpitedobrasileirao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import androidx.appcompat.app.AppCompatActivity;

public class Z_get_backups extends AppCompatActivity {

    private String origem;
    private List<String> backups;
    private List<String> arquivos;
    private List<String> tipos;
    private List<String> anos;
    private String[] listaAnoDoCampeonato;
    private ArrayList<String> listaSeries;
    private StorageReference storageRef;
    private StorageReference anoRef2;
    private StorageReference gruposRef;
    private File dirFiles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_get_backups);
        recebeDados();
        dirFiles = getDir("files",MODE_PRIVATE);
        ballAnimation();
        backups = new ArrayList<>();
        arquivos = new ArrayList<>();
        tipos = new ArrayList<>();
        anos = new ArrayList<>();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        resgataArrays();
        getBackup();
    }

    private void recebeDados(){
        Intent getBackups = getIntent();
        origem = getBackups.getStringExtra("origem");
    }

    private void ballAnimation (){
        ImageView imgView = findViewById(R.id.animation_ball);
        AnimationDrawable ball_Animation = (AnimationDrawable) imgView.getBackground();
        ball_Animation.start();
    }

    private void resgataArrays(){
        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
        int i = 0;
        while(!Objects.equals(dadosCampeonatos.getString("dadosCampeonatoAno" + i,"--"), "--")){
            i++;
        }
        listaAnoDoCampeonato = new String[i];
        for (int j = 0; j<i; j++){
            listaAnoDoCampeonato[j] = dadosCampeonatos.getString("dadosCampeonatoAno" + j,"--");
        }
        listaSeries = new ArrayList<>();
        SharedPreferences referencias = getSharedPreferences("referencias",MODE_PRIVATE);
        Map<String,?> referenciasKeys = referencias.getAll();
        for(Map.Entry<String,?> entry : referenciasKeys.entrySet()){
            if (entry.getValue().equals("series_array2")) {
                listaSeries.add(String.valueOf(entry.getKey()));
            }
        }
    }

    private void getBackup(){
        SharedPreferences meusDados = getSharedPreferences("meusDados",MODE_PRIVATE);
        String numeroAntigo = meusDados.getString("numeroAntigo","--");
        if (Objects.equals(numeroAntigo, "--")){
            numeroAntigo = meusDados.getString("meuNumero","--");
        }
        for (String anoBackup : listaAnoDoCampeonato) {
            //Criando referências para ler e escrever os backups dos meusGrupos de cada ano
            backups.add("meusGrupos" + anoBackup + "Backup" + numeroAntigo);
            arquivos.add("dadosComuns");
            tipos.add("meusGrupos");
            anos.add(anoBackup);
            //Criando referências para ler e escrever os backups dos meusPontos de cada ano
            backups.add("meusPontos" + anoBackup + "Backup" + numeroAntigo);
            arquivos.add("meusPontos" + anoBackup);
            tipos.add("meusPontos");
            anos.add(anoBackup);
            //Criando referências para ler e escrever os backups dos meusPalpites de cada série e ano
            for (String serieBackup : listaSeries) {
                backups.add("meusPalpites" + serieBackup + anoBackup + "Backup" + numeroAntigo);
                arquivos.add("meusPalpites" + serieBackup + anoBackup);
                tipos.add("meusPalpites");
                anos.add(anoBackup);
            }
        }
        int totalBackups = backups.size();
        try {
            for (int i=0; i<totalBackups; i++){
                StorageReference anoRef = storageRef.child("ano" + anos.get(i));
                StorageReference meuBackupRef = anoRef.child(tipos.get(i) + "Backup" + anos.get(i));
                StorageReference fileRef = meuBackupRef.child(backups.get(i) + ".xml");
                final File localFile = new File(dirFiles, backups.get(i) + ".xml");
                final int ii = i;
                fileRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                    try {
                        SharedPreferences sFile = getSharedPreferences(arquivos.get(ii), MODE_PRIVATE);
                        final SharedPreferences.Editor sFileEditor = sFile.edit();
                        FileInputStream fileInput = new FileInputStream(localFile);//Crio o leitor do arquivo.
                        Properties properties = new Properties();//Crio um objeto para receber o conteúdo do arquivo.
                        properties.loadFromXML(fileInput);//Transfiro para o objeto o conteúdo lido do arquivo.
                        fileInput.close();//Fecho o leitor do arquivo.
                        Enumeration<Object> enuKeys = properties.keys();//enuKeys recebe as chaves do xml salvo em propertiesGroup.
                        while (enuKeys.hasMoreElements()) {
                            final String key = (String) enuKeys.nextElement();//Lê cada chave.
                            String value = properties.getProperty(key);
                            sFileEditor.putString(key,value);
                            if (backups.get(ii).contains("meusGrupos")){
                                anoRef2 = storageRef.child("ano" + anos.get(ii));
                                gruposRef = anoRef2.child("grupos"+anos.get(ii));
                                if (key.contains("grupo")){
                                    StorageReference fileGrupoRef = gruposRef.child(key + ".xml");
                                    final File grupoLocalFile = new File(dirFiles, key + ".xml");
                                    fileGrupoRef.getFile(grupoLocalFile).addOnSuccessListener(taskSnapshot1 -> {
                                        try {
                                            SharedPreferences gFile = getSharedPreferences(key,MODE_PRIVATE);
                                            SharedPreferences.Editor gFileEditor = gFile.edit();
                                            FileInputStream gFileInput = new FileInputStream(grupoLocalFile);//Crio o leitor do arquivo.
                                            Properties gProperties = new Properties();//Crio um objeto para receber o conteúdo do arquivo.
                                            gProperties.loadFromXML(gFileInput);//Transfiro para o objeto o conteúdo lido do arquivo.
                                            gFileInput.close();//Fecho o leitor do arquivo.
                                            Enumeration<Object> enuKeys1 = gProperties.keys();//enuKeys recebe as chaves do xml salvo em propertiesGroup.
                                            while (enuKeys1.hasMoreElements()) {
                                                String key1 = (String) enuKeys1.nextElement();//Lê cada chave.
                                                String value1 = gProperties.getProperty(key1);
                                                gFileEditor.putString(key1, value1);
                                            }
                                            gFileEditor.apply();
                                            //noinspection ResultOfMethodCallIgnored
                                            grupoLocalFile.delete();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }).addOnFailureListener(exception -> {

                                    });
                                }
                            }
                        }
                        sFileEditor.apply();
                        //noinspection ResultOfMethodCallIgnored
                        localFile.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).addOnFailureListener(exception -> {

                });
            }
        }catch (Exception e){
            Log.v("Error","Error: "+e);
        }finally {
            chamarAtualizarDados();
        }
    }

    private void chamarAtualizarDados(){
        Intent atualizarDados = new Intent(this, Z03_atualizar_dados.class);
        atualizarDados.putExtra("origem",origem);
        startActivity(atualizarDados);
        finish();
    }
}
