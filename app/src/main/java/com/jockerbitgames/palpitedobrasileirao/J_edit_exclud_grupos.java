package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import androidx.appcompat.app.AppCompatActivity;

public class J_edit_exclud_grupos extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private String ano;
    private ImageButton buttonExcluir;
    private List<String> lista_grupos_spinner;
    private List<String> lista_grupos_arquivo;
    private Boolean toqueNaTela = false;
    private boolean statusAdmin = false;
    private String arquivoGrupo;
    private String nomeGrupo;
    private String meuNumero;
    private File dirFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.j_edit_exclud_grupos);
        dirFiles = getDir("files",MODE_PRIVATE);
        buttonExcluir = findViewById(R.id.excluir_grupo);
        ano = getDadosComuns();
        lista_grupos_spinner = new ArrayList<>();
        lista_grupos_spinner.add("Não há grupos.");
        lista_grupos_arquivo = new ArrayList<>();
        lista_grupos_arquivo.add("Não há grupos.");
        getListas();
        implementarSpinners();
        verificaStatusAdmin();
        atualizaCampos();
    }

    private String getDadosComuns(){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        return getDadosComuns.getString("ano","--");
    }

    private void getListas(){
        SharedPreferences getListas = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        String chaveGrupo = "grupo"+ano;
        int contGrupos = 0;
        Map<String,?> allEntries = getListas.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains(chaveGrupo)) {
                lista_grupos_arquivo.add(entry.getKey());
                SharedPreferences grupo = getSharedPreferences(entry.getKey(),MODE_PRIVATE);
                lista_grupos_spinner.add(grupo.getString("nomeDoGrupo","--"));
                contGrupos++;
                lista_grupos_arquivo.remove("Não há grupos.");
                lista_grupos_spinner.remove("Não há grupos.");
            }
        }
        if (contGrupos == 0){
            Toast.makeText(this, "Não há grupos criados para o ano de "+ano+".", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void atualizaCampos(){
        TextView anoDoCampeonato = findViewById(R.id.ano_do_campeonato);
        anoDoCampeonato.setText(ano);

        if (!statusAdmin) {
            buttonExcluir.setVisibility(View.GONE);
        }
        else{
            buttonExcluir.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void implementarSpinners(){
        //Implementa o dropdown de escolha da série.
        Spinner dropdown_grupo_spinner = findViewById(R.id.dropdown_grupo_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lista_grupos_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_grupo_spinner.setAdapter(adapter);
        dropdown_grupo_spinner.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_grupo_spinner.setOnItemSelectedListener(this);

        arquivoGrupo = lista_grupos_arquivo.get(0);
        nomeGrupo = (String) dropdown_grupo_spinner.getItemAtPosition(0);
        //*********************(Fim do escopo do comentário)
    }

    //Executa algo a partir da seleção nos spinners.
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(toqueNaTela) {
            toqueNaTela = false;
            arquivoGrupo = lista_grupos_arquivo.get(position);
            nomeGrupo = lista_grupos_spinner.get(position);
            verificaStatusAdmin();
            atualizaCampos();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    //*********************(Fim do escopo do comentário)**********************

    private void verificaStatusAdmin(){
        SharedPreferences meusDados = getSharedPreferences("meusDados", MODE_PRIVATE);
        meuNumero = meusDados.getString("meuNumero", "--");
        SharedPreferences grupoAdmin = getSharedPreferences(arquivoGrupo, MODE_PRIVATE);
        String meuStatus = "convidado";
        Map<String,?> allEntries = grupoAdmin.getAll();
        for (Map.Entry<String,?> entry:allEntries.entrySet()) {
            if (entry.getValue().equals(meuNumero)){
                String chaveStatus = entry.getKey().replace("meuNumero","status");
                meuStatus = grupoAdmin.getString(chaveStatus,"convidado");
            }
        }
        statusAdmin = Objects.equals(meuStatus, "administrador");
    }

    public void editarGrupo(@SuppressWarnings("unused") View view) {
        chamarVibrate();
        if (statusAdmin) {
            Toast.makeText(this, "Você pode editar qualquer informação deste grupo, menos o nome.", Toast.LENGTH_LONG).show();
            Intent editGrupo = new Intent(this,K_editar_grupos.class);
            editGrupo.putExtra("arquivoGrupo",arquivoGrupo);
            startActivity(editGrupo);
        }
        else{
            Toast.makeText(this, "Você só pode convidar novos participantes para este grupo.", Toast.LENGTH_LONG).show();
            Intent criarConvite = new Intent(this,I_criar_convite.class);
            criarConvite.putExtra("arquivoGrupo",arquivoGrupo);
            startActivity(criarConvite);
        }
        finish();
    }

    public void excluirGrupo(@SuppressWarnings("unused") View view) {
        chamarVibrate();
        if (statusAdmin) {
            new AlertDialog.Builder(this)
                    .setTitle("Exluir grupo " + nomeGrupo)
                    .setMessage(getString(R.string.confirmacaoExcluirGrupo) + " " + nomeGrupo + "?!")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        //Limpar meus dados do arquivoGrupo.
                        //Transferir o status administrador para outro membro, se existir.
                        //Subir arquivoGrupo atualizado sem meus dados.
                        transferirDadosContato();

                        SharedPreferences clearFile = getSharedPreferences(arquivoGrupo, MODE_PRIVATE);
                        SharedPreferences.Editor clearFileEditor = clearFile.edit();
                        Map<String, ?> allEntries = clearFile.getAll();
                        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                            clearFileEditor.remove(entry.getKey());
                        }
                        clearFileEditor.apply();
                        SharedPreferences deleteFile = getSharedPreferences("dadosComuns", MODE_PRIVATE);
                        SharedPreferences.Editor deleteFileEditor = deleteFile.edit();
                        deleteFileEditor.remove(arquivoGrupo);
                        deleteFileEditor.apply();
                        int position = lista_grupos_arquivo.indexOf(arquivoGrupo);
                        lista_grupos_arquivo.remove(arquivoGrupo);
                        lista_grupos_spinner.remove(position);
                        if (lista_grupos_spinner.size()==0){
                            lista_grupos_arquivo.add("Não há grupos.");
                            lista_grupos_spinner.add("Não há grupos.");
                            finish();
                        }
                        implementarSpinners();
                        verificaStatusAdmin();
                        atualizaCampos();
                        backupMeusGrupos();
                    })
                    .setNegativeButton("Não", null)
                    .show();
        }
    }

    private void transferirDadosContato() {
        int numMembers = 0;
        int meuContato = 0;
        SharedPreferences editFile = getSharedPreferences(arquivoGrupo, MODE_PRIVATE);
        SharedPreferences.Editor clearFileEditor = editFile.edit();
        Map<String, ?> allEntries = editFile.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains("meuNumero")) {
                numMembers++;
            }
            if (entry.getValue().equals(meuNumero)) {
                meuContato = Integer.parseInt(entry.getKey().replace("meuNumeroContato", ""));
            }
        }

        if (numMembers > 1) {
            //Transferir o status administrador para outro membro e reordenar os dados dos contatos para poder excluir o último.
            if (meuContato < numMembers) {
                clearFileEditor.putString("statusContato" + (meuContato + 1), "administrador");
                clearFileEditor.apply();
                int k = meuContato;
                while (k < numMembers) {
                    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                        if (entry.getKey().contains("Contato" + k)) {
                            String nextKey = entry.getKey().replace("Contato" + k, "Contato" + (k + 1));
                            String nextValue = editFile.getString(nextKey, "--");
                            clearFileEditor.putString(entry.getKey(), nextValue);
                        }
                    }
                    k++;
                }
            } else {
                clearFileEditor.putString("statusContato" + (meuContato - 1), "administrador");
            }
            clearFileEditor.apply();
        }
        excluirUltimoContato(numMembers);
    }

    private void excluirUltimoContato(int numMembers) {
        //Excluir o último contato do grupo e salvar. Criar a properties para salvar no dirFiles.
        SharedPreferences clearFile = getSharedPreferences(arquivoGrupo, MODE_PRIVATE);
        SharedPreferences.Editor clearFileEditor = clearFile.edit();
        Map<String, ?> allEntries = clearFile.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains("Contato" + numMembers)) {
                clearFileEditor.remove(entry.getKey());
            }
        }
        clearFileEditor.apply();
        subirGrupo();
    }

    private void subirGrupo(){
        try {
            //Passar o arquivoGrupo atualizado para o dirFiles e então subir.
            final File groupFile = new File(dirFiles, arquivoGrupo + ".xml");
            FileOutputStream fos = new FileOutputStream(groupFile);
            Properties properties = new Properties();
            SharedPreferences uploadFile = getSharedPreferences(arquivoGrupo, MODE_PRIVATE);
            Map<String, ?> allEntries = uploadFile.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                properties.setProperty(entry.getKey(), (String) entry.getValue());
            }
            properties.storeToXML(fos,null);
            fos.flush();
            fos.close();
            Runnable r = Z_runnable_subir_dados.newInstance(arquivoGrupo,"grupo",ano,dirFiles);
            new Thread(r).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void backupMeusGrupos(){
        String meusGruposBackup = "meusGrupos"+ano+"Backup"+meuNumero;
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns",MODE_PRIVATE);
        Map<String,?> allEntries = dadosComuns.getAll();
        Properties properties = new Properties();
        properties.setProperty("arquivo", "criado");
        for(Map.Entry<String,?> entry : allEntries.entrySet()){
            if (entry.getKey().startsWith("grupo")){
                properties.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        try {
            //File dirFiles = getDir("files",MODE_PRIVATE);
            File getFile = new File(dirFiles, meusGruposBackup+".xml");
            FileOutputStream outputStream = new FileOutputStream(getFile);
            properties.storeToXML(outputStream, "meusGruposBackup");
            outputStream.flush();
            outputStream.close();
            try {
                Runnable r = Z_runnable_subir_dados.newInstance(meusGruposBackup,"meusGruposBackup",ano,dirFiles);
                new Thread(r).start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_j_edit_exclud_grupos, menu);
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
                instruction.putExtra("tela","J_edit_exclud_grupos");
                startActivity(instruction);
                break;
            case 1:
                finish();
        }
        return true;
    }
}
