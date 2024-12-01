package com.jockerbitgames.palpitedobrasileirao;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.appcompat.app.AppCompatActivity;

public class Z_atualizar_colocacao extends AppCompatActivity {
    private String arquivoGrupo;
    private String nomeDoGrupo;
    private String ano;
    private String baixar_grupos;
    private int tamanhoGrupo;
    private int userRead;
    private int userWrite;
    private ArrayList<PalpiteiroModel> palpiteiros;
    private ArrayList<PalpiteiroModel> classificados;
    private StorageReference usuariosRef,gruposRef,fileRef;
    private File dirFiles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_atualizar_colocacao);
        recebeDados();
        dirFiles = getDir("files",MODE_PRIVATE);
        ballAnimation();
        palpiteiros = new ArrayList<>();
        classificados = new ArrayList<>();
        atualizaCampos();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        usuariosRef = storageRef.child("usuarios");
        StorageReference anoRef = storageRef.child("ano" + ano);
        gruposRef = anoRef.child("grupos"+ano);
        atualizar();
    }

    private void recebeDados(){
        Intent atualizar_Colocacao = getIntent();
        ano = atualizar_Colocacao.getStringExtra("ano");
        baixar_grupos = atualizar_Colocacao.getStringExtra("baixar_grupos");
        arquivoGrupo = atualizar_Colocacao.getStringExtra("arquivoGrupo");
        nomeDoGrupo = atualizar_Colocacao.getStringExtra("nomeDoGrupo");
    }

    private void ballAnimation (){
        ImageView imgView = findViewById(R.id.animation_ball2);
        AnimationDrawable ball_Animation = (AnimationDrawable) imgView.getBackground();
        ball_Animation.start();
    }

    private void atualizaCampos(){
        TextView atualizaGrupo = findViewById(R.id.atualizaGrupo);
        String texto = "Grupo: " + nomeDoGrupo;
        atualizaGrupo.setText(texto);
    }

    private void atualizar(){
        tamanhoGrupo = 0;
        if (baixar_grupos.equals("não")){
            copiaArquivoGrupo(arquivoGrupo);
        }
        else{
            baixarGrupos();
        }
    }

    private void baixarGrupos(){
        SharedPreferences meusDados = getSharedPreferences("meusDados",MODE_PRIVATE);
        final String meuNumero = meusDados.getString("meuNumero","--");
        fileRef = gruposRef.child(arquivoGrupo+".xml");
        File groupLocalFile = new File(dirFiles, arquivoGrupo + ".xml");
        fileRef.getFile(groupLocalFile).addOnSuccessListener(taskSnapshot -> {
            try {
                boolean estouNoGrupo = false;
                FileInputStream fileInput = new FileInputStream(groupLocalFile);//Crio o leitor do arquivo.
                Properties propertiesGroup = new Properties();//Crio um objeto para receber o conteúdo do arquivo.
                propertiesGroup.loadFromXML(fileInput);//Transfiro para o objeto o conteúdo lido do arquivo.
                fileInput.close();//Fecho o leitor do arquivo.
                Enumeration<Object> enuKeys = propertiesGroup.keys();//enuKeys recebe as chaves do xml salvo em propertiesGroup.
                while (enuKeys.hasMoreElements()) {
                    String keyGroup = (String) enuKeys.nextElement();//Lê cada chave.
                    if (propertiesGroup.getProperty(keyGroup).equals(meuNumero)){
                        estouNoGrupo = true;
                        baixarUsuarios();
                        break;
                    }
                }
                if (!estouNoGrupo){
                    excluirGrupo();
                }
            } catch (IOException e) {
                fileToDelete(arquivoGrupo);
                finish();
            }
        }).addOnFailureListener(exception -> {
            fileToDelete(arquivoGrupo);
            finish();
        });
    }

    private void copiaArquivoGrupo(String arquivoGrupo){
        SharedPreferences preferences= getSharedPreferences(arquivoGrupo, Context.MODE_PRIVATE);
        Map<String,?> allEntries = preferences.getAll();
        Properties properties = new Properties();
        for(Map.Entry<String,?> entry : allEntries.entrySet()){
            properties.setProperty(entry.getKey(), entry.getValue().toString());
        }
        try {
            File getFile = new File(dirFiles, arquivoGrupo + ".xml");
            FileOutputStream outputStream = new FileOutputStream(getFile);
            properties.storeToXML(outputStream, null);
            outputStream.flush();
            outputStream.close();
            baixarUsuarios();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void baixarUsuarios(){
        userRead = 0;
        userWrite = 0;
        File groupLocalFile = new File(dirFiles, arquivoGrupo + ".xml");
        try {
            FileInputStream fileInput = new FileInputStream(groupLocalFile);//Crio o leitor do arquivo.
            Properties propertiesGroup = new Properties();//Crio um objeto para receber o conteúdo do arquivo.
            propertiesGroup.loadFromXML(fileInput);//Transfiro para o objeto o conteúdo lido do arquivo.
            fileInput.close();//Fecho o leitor do arquivo.
            Enumeration<Object> enuKeysGroup = propertiesGroup.keys();//enuKeys recebe as chaves do xml salvo em propertiesGroup.
            AtomicBoolean falha = new AtomicBoolean(false);

            while (enuKeysGroup.hasMoreElements()) {
                String keyGroup = (String) enuKeysGroup.nextElement();//Lê cada chave.
                if (keyGroup.contains("meuNumero")){
                    userRead++;
                    final String usuarioFileName = propertiesGroup.getProperty(keyGroup);//Lê cada valor relativo a cada keyGroup.
                    final String contato = keyGroup.replace("meuNumero","");
                    fileRef = usuariosRef.child(usuarioFileName+".xml");
                    File userLocalFile = new File(dirFiles, usuarioFileName + ".xml");
                    fileRef.getFile(userLocalFile).addOnSuccessListener(taskSnapshot -> {
                        try {
                            FileInputStream fisUser = new FileInputStream(userLocalFile);//Crio o leitor do arquivo.
                            Properties propertiesUser = new Properties();//Crio um objeto para receber o conteúdo do arquivo.
                            propertiesUser.loadFromXML(fisUser);//Transfiro para o objeto o conteúdo lido do arquivo.
                            fisUser.close();//Fecho o leitor do arquivo.
                            Enumeration<Object> enuKeysUser = propertiesUser.keys();//enuKeys recebe as chaves do xml salvo em propertiesUser.
                            while (enuKeysUser.hasMoreElements()) {
                                String keyUser = (String) enuKeysUser.nextElement();//Lê cada chave.
                                String valueUser = propertiesUser.getProperty(keyUser);//Lê cada valor relativo a cada keyUser.
                                if ((!keyUser.equals("arquivo"))&&(!keyUser.equals("emailVerificado"))){
                                    if (keyUser.equals("palpiteiro")){
                                        propertiesGroup.setProperty("meuNome"+contato, valueUser);
                                        propertiesGroup.setProperty("apelido"+contato, valueUser);
                                    }
                                    else if (keyUser.contains("totalPontosSerie") && keyUser.contains(ano)){
                                        propertiesGroup.setProperty(keyUser.replace(ano,contato), valueUser);
                                    }
                                    else if (!keyUser.contains("totalPontosSerie")){
                                        propertiesGroup.setProperty(keyUser+contato, valueUser);
                                    }
                                }
                            }
                            userWrite++;
                            if (userWrite == userRead){
                                FileOutputStream fos = new FileOutputStream(groupLocalFile);
                                propertiesGroup.storeToXML(fos,null);
                                fos.flush();
                                fos.close();
                                lerArquivoGrupo();
                            }
                            fileToDelete(usuarioFileName);
                        } catch (IOException e) {
                            falha.set(true);
                        }
                    }).addOnFailureListener(exception -> falha.set(true));
                }
            }
            if (falha.get()){
                Toast.makeText(Z_atualizar_colocacao.this, "Não foi possível atualizar todos os dados desse grupo.", Toast.LENGTH_SHORT).show();
            }
            finish();
        } catch (IOException e) {
            Toast.makeText(Z_atualizar_colocacao.this, "Não foi possível baixar os dados desse grupo.", Toast.LENGTH_SHORT).show();
            fileToDelete(arquivoGrupo);
            finish();
        }
    }

    private void lerArquivoGrupo(){
        String[]seriesEscolhidas = new String[4];
        File groupLocalFile = new File(dirFiles, arquivoGrupo + ".xml");
        try {
            FileInputStream fileInput = new FileInputStream(groupLocalFile);
            Properties propertiesGroup = new Properties();//Crio um objeto para receber o conteúdo do arquivo.
            propertiesGroup.loadFromXML(fileInput);//Transfiro para o objeto o conteúdo lido do arquivo.
            fileInput.close();//Fecho o leitor do arquivo.
            Enumeration<Object> enuKeys = propertiesGroup.keys();//enuKeys recebe as chaves do xml salvo em propertiesGroup.
            seriesEscolhidas[0] = propertiesGroup.getProperty("checkboxSerieA", "false");
            seriesEscolhidas[1] = propertiesGroup.getProperty("checkboxSerieB", "false");
            seriesEscolhidas[2] = propertiesGroup.getProperty("checkboxSerieC", "false");
            seriesEscolhidas[3] = propertiesGroup.getProperty("checkboxSerieD", "false");
            while (enuKeys.hasMoreElements()) {
                String keyGroup = (String) enuKeys.nextElement();//Lê cada chave.
                if (keyGroup.contains("colocacao")){
                    String colocacao = propertiesGroup.getProperty(keyGroup, "--");
                    keyGroup = keyGroup.replace("colocacao", "meuNome");
                    String nome = propertiesGroup.getProperty(keyGroup, "--");
                    keyGroup = keyGroup.replace("meuNome", "apelido");
                    String apelido = propertiesGroup.getProperty(keyGroup, "--");
                    keyGroup = keyGroup.replace("apelido", "meuNumero");
                    String numero = propertiesGroup.getProperty(keyGroup, "--");
                    keyGroup = keyGroup.replace("meuNumero", "timeDoCoracao");
                    String time = propertiesGroup.getProperty(keyGroup, "--");
                    keyGroup = keyGroup.replace("timeDoCoracao", "status");
                    String status = propertiesGroup.getProperty(keyGroup, "convidado");
                    keyGroup = keyGroup.replace("status", "totalPontosSerieA");
                    String ptsSerieA = propertiesGroup.getProperty(keyGroup, "0");
                    if (seriesEscolhidas[0] != null && seriesEscolhidas[0].equals("false")) {
                        ptsSerieA = "0";
                    }
                    keyGroup = keyGroup.replace("totalPontosSerieA", "totalPontosSerieB");
                    String ptsSerieB = propertiesGroup.getProperty(keyGroup, "0");
                    if (seriesEscolhidas[1] != null && seriesEscolhidas[1].equals("false")) {
                        ptsSerieB = "0";
                    }
                    keyGroup = keyGroup.replace("totalPontosSerieB", "totalPontosSerieC");
                    String ptsSerieC = propertiesGroup.getProperty(keyGroup, "0");
                    if (seriesEscolhidas[2] != null && seriesEscolhidas[2].equals("false")) {
                        ptsSerieC = "0";
                    }
                    keyGroup = keyGroup.replace("totalPontosSerieC", "totalPontosSerieD");
                    String ptsSerieD = propertiesGroup.getProperty(keyGroup, "0");
                    if (seriesEscolhidas[3] != null && seriesEscolhidas[3].equals("false")) {
                        ptsSerieD = "0";
                    }
                    keyGroup = keyGroup.replace("totalPontosSerieD" + ano, "atualizadoEm");
                    String atualizadoEm = propertiesGroup.getProperty(keyGroup, "0");
                    String ptsTotal = String.valueOf(Integer.parseInt(ptsSerieA != null ? ptsSerieA : "0") +
                            Integer.parseInt(ptsSerieB != null ? ptsSerieB : "0") +
                            Integer.parseInt(ptsSerieC != null ? ptsSerieC : "0") +
                            Integer.parseInt(ptsSerieD != null ? ptsSerieD : "0"));
                    PalpiteiroModel palpiteiro = new PalpiteiroModel(colocacao, nome, apelido, numero, time, ptsTotal, ptsSerieA, ptsSerieB, ptsSerieC, ptsSerieD, status, atualizadoEm);
                    palpiteiros.add(palpiteiro);
                    tamanhoGrupo++;
                    if (tamanhoGrupo == userRead){
                        classificaPalpiteiros();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void classificaPalpiteiros(){
        int position;
        int t = tamanhoGrupo - 1;
        while (t>=0){
            int ptsTotala = Integer.parseInt(palpiteiros.get(t).ptsTotal);
            position = t;
            int k=t-1;
            while (k>=0){
                int ptsTotalb = Integer.parseInt(palpiteiros.get(k).ptsTotal);
                if (ptsTotalb == ptsTotala){
                    int ptsSerieAa = Integer.parseInt(palpiteiros.get(position).ptsSerieA);
                    int ptsSerieAb = Integer.parseInt(palpiteiros.get(k).ptsSerieA);
                    if (ptsSerieAb > ptsSerieAa) {
                        position = k;
                    }
                    if(ptsSerieAb == ptsSerieAa){
                        int ptsSerieBa = Integer.parseInt(palpiteiros.get(position).ptsSerieB);
                        int ptsSerieBb = Integer.parseInt(palpiteiros.get(k).ptsSerieB);
                        if (ptsSerieBb > ptsSerieBa){
                            position = k;
                        }
                        if (ptsSerieBb == ptsSerieBa){
                            int ptsSerieCa = Integer.parseInt(palpiteiros.get(position).ptsSerieC);
                            int ptsSerieCb = Integer.parseInt(palpiteiros.get(k).ptsSerieC);
                            if (ptsSerieCb > ptsSerieCa){
                                position = k;
                            }
                            if (ptsSerieCb == ptsSerieCa){
                                int ptsSerieDa = Integer.parseInt(palpiteiros.get(position).ptsSerieD);
                                int ptsSerieDb = Integer.parseInt(palpiteiros.get(k).ptsSerieD);
                                if (ptsSerieDb >= ptsSerieDa){
                                    position = k;
                                }
                            }
                        }
                    }
                }
                if (ptsTotalb>ptsTotala){
                    ptsTotala = ptsTotalb;
                    position = k;
                }
                k--;
            }
            classificados.add(palpiteiros.get(position));
            palpiteiros.remove(position);
            t--;
        }
        if (palpiteiros.size() == 0){
            organizaColocacao();
        }
    }

    private void organizaColocacao(){
        for (int i = 0; i<classificados.size(); i++){
            classificados.get(i).colocacao = String.valueOf(i+1);
        }
        atualizaArquivoGrupo();
    }

    private void atualizaArquivoGrupo(){
        File groupLocalFile = new File(dirFiles, arquivoGrupo + ".xml");
        try {
            FileInputStream fileInput = new FileInputStream(groupLocalFile);
            Properties propertiesGroup = new Properties();//Crio um objeto para receber o conteúdo do arquivo.
            propertiesGroup.loadFromXML(fileInput);//Transfiro para o objeto o conteúdo lido do arquivo.
            fileInput.close();//Fecho o leitor do arquivo.
            Enumeration<Object> enuKeys = propertiesGroup.keys();//enuKeys recebe as chaves do xml salvo em propertiesGroup.
            while (enuKeys.hasMoreElements()) {
                String keyGroup = (String) enuKeys.nextElement();//Lê cada chave.
                if (keyGroup.contains("Contato")) {
                    propertiesGroup.remove(keyGroup);
                }
            }
            int tamanho = classificados.size();
            int contaAdimin = 0;
            for (int i = tamanho; i>0; i--){
                propertiesGroup.setProperty("colocacaoContato"+ (i),classificados.get(i-1).colocacao);
                propertiesGroup.setProperty("meuNomeContato"+ (i),classificados.get(i-1).nome);
                propertiesGroup.setProperty("apelidoContato"+ (i),classificados.get(i-1).apelido);
                propertiesGroup.setProperty("meuNumeroContato"+ (i),classificados.get(i-1).numero);
                propertiesGroup.setProperty("timeDoCoracaoContato"+ (i),classificados.get(i-1).time);
                propertiesGroup.setProperty("ptsTotalContato"+ (i),classificados.get(i-1).ptsTotal);
                propertiesGroup.setProperty("totalPontosSerieAContato"+ (i),classificados.get(i-1).ptsSerieA);
                propertiesGroup.setProperty("totalPontosSerieBContato"+ (i),classificados.get(i-1).ptsSerieB);
                propertiesGroup.setProperty("totalPontosSerieCContato"+ (i),classificados.get(i-1).ptsSerieC);
                propertiesGroup.setProperty("totalPontosSerieDContato"+ (i),classificados.get(i-1).ptsSerieD);
                propertiesGroup.setProperty("statusContato"+ (i),classificados.get(i-1).status);
                propertiesGroup.setProperty("atualizadoEmContato"+ (i),classificados.get(i-1).atualizadoEm);
                if (classificados.get(i - 1).status.equals("administrador")){
                    contaAdimin++;
                }
                classificados.remove(i-1);
            }
            if (contaAdimin == 0){
                propertiesGroup.setProperty("statusContato1","administrador");
            }
            if (classificados.size() == 0){
                FileOutputStream fos = new FileOutputStream(groupLocalFile);
                propertiesGroup.storeToXML(fos,null);
                fos.close();
                transferirGrupos(arquivoGrupo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class PalpiteiroModel{
        String colocacao;
        final String nome;
        final String apelido;
        final String numero;
        final String time;
        final String ptsTotal;
        final String ptsSerieA;
        final String ptsSerieB;
        final String ptsSerieC;
        final String ptsSerieD;
        final String status;
        final String atualizadoEm;

        PalpiteiroModel(String colocacao, String nome, String apelido, String numero, String time, String ptsTotal, String ptsSerieA, String ptsSerieB, String ptsSerieC, String ptsSerieD, String status, String atualizadoEm){
            this.colocacao = colocacao;
            this.nome = nome;
            this.apelido = apelido;
            this.numero = numero;
            this.time = time;
            this.ptsTotal = ptsTotal;
            this.ptsSerieA = ptsSerieA;
            this.ptsSerieB = ptsSerieB;
            this.ptsSerieC = ptsSerieC;
            this.ptsSerieD = ptsSerieD;
            this.status = status;
            this.atualizadoEm = atualizadoEm;
        }
    }

    private void fileToDelete(String file){
        File fileDelete = new File(dirFiles, file + ".xml");//Encontra o arquivo desejado.
        //noinspection ResultOfMethodCallIgnored
        fileDelete.delete();
    }

    private void transferirGrupos(String nomeArquivoGrupo){
        File groupLocalFile = new File(dirFiles, nomeArquivoGrupo + ".xml");
        SharedPreferences arquivo_grupo = this.getSharedPreferences(nomeArquivoGrupo, Context.MODE_PRIVATE);
        SharedPreferences.Editor arquivo_grupo_editor = arquivo_grupo.edit();
        Map<String, ?> allEntries = arquivo_grupo.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains("Contato")) {
                arquivo_grupo_editor.remove(entry.getKey());
            }
        }
        try {
            FileInputStream fileInput = new FileInputStream(groupLocalFile);//Crio o leitor do arquivo.
            Properties properties = new Properties();//Crio um objeto para receber o conteúdo do arquivo.
            properties.loadFromXML(fileInput);//Transfiro para o objeto o conteúdo lido do arquivo.
            fileInput.close();//Fecho o leitor do arquivo.
            Enumeration<Object> enuKeys = properties.keys();//enuKeys recebe as chaves do xml salvo em properties.
            while (enuKeys.hasMoreElements()) {
                String key = (String) enuKeys.nextElement();//Lê cada chave.
                String value = properties.getProperty(key);//Lê cada valor relativo a cada key.
                arquivo_grupo_editor.putString(key, value);
            }
            arquivo_grupo_editor.apply();
            //subirDados(arquivoGrupo);
            try {
                Runnable r = Z_runnable_subir_dados.newInstance(arquivoGrupo,"grupo",ano,dirFiles);
                new Thread(r).start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void excluirGrupo(){
        //exclui arquivo da memoria local.
        fileToDelete(arquivoGrupo);
        //exclui grupo dos dados comuns.
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns",MODE_PRIVATE);
        SharedPreferences.Editor dadosComunsEditor = dadosComuns.edit();
        dadosComunsEditor.remove(arquivoGrupo);
        dadosComunsEditor.apply();
        //limpa arquivo do grupo.
        SharedPreferences grupo = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
        SharedPreferences.Editor grupoEditor = grupo.edit();
        Map<String,?> allEntries = grupo.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            grupoEditor.remove(entry.getKey());
        }
        grupoEditor.apply();
        //Fecha a activity.
        finish();
    }
}

