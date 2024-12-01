package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import androidx.appcompat.app.AppCompatActivity;

public class I_criar_grupos extends AppCompatActivity {

    private ArrayList<String> listaSeries;
    private String ano;
    private String arquivoGrupo;
    private String hoje;
    private int checkboxSeries = 0;
    private EditText nomeDoGrupo;
    private EditText premioCombinado;
    private File dirFiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.i_criar_grupos);
        dirFiles = getDir("files",MODE_PRIVATE);
        registraData();
        resgataArrays();
        ano = getDadosComuns("ano");
        TextView anoCampeonato = findViewById(R.id.ano_do_campeonato);
        anoCampeonato.setText(ano);
        nomeDoGrupo = findViewById(R.id.nome_do_grupo);
        premioCombinado = findViewById(R.id.premio_combinado);
        ImageButton btnEscalarSelecao = findViewById(R.id.btnEscalarSelecao);
        btnEscalarSelecao.setOnClickListener(this::callContactList);
    }

    private void registraData(){
        Date momento = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        hoje = formatter.format(momento);
        setDadosComuns("atualizadoEm",hoje);
    }

    private void resgataArrays() {
        listaSeries = new ArrayList<>();
        SharedPreferences referencias = getSharedPreferences("referencias",MODE_PRIVATE);
        Map<String,?> referenciasKeys = referencias.getAll();
        for(Map.Entry<String,?> entry : referenciasKeys.entrySet()){
            if (entry.getValue().equals("series_array2")) {
                listaSeries.add(String.valueOf(entry.getKey()));
            }
        }
    }

    private String getDadosComuns(String chave){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        return getDadosComuns.getString(chave,"--");
    }

    private void setDadosComuns(String chave, String valor){
        //Dados que serão lidos por outras classes.
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        SharedPreferences.Editor editor = dadosComuns.edit();
        editor.putString(chave, valor);
        editor.apply();
    }

//**********Selecionar participantes**********
    public void callContactList(@SuppressWarnings("unused") View view) {
        chamarVibrate();
        if (verificaCadastro()) {
            if (verificaCamposPreenchidos()) {
                if (criarGrupo()) {
                    atualizarColocacao(arquivoGrupo, String.valueOf(nomeDoGrupo.getText()));
                }
            }
        }
    }

    private boolean verificaCadastro(){
        SharedPreferences meusDados = getSharedPreferences("meusDados",MODE_PRIVATE);
        String[] dadosCadastro = new String[]{"palpiteiro","timeDoCoracao","meuNumero","meuEmail"};
        boolean cadastroCompleto = true;
        for (String dado:dadosCadastro) {
            if (Objects.equals(meusDados.getString(dado, "--"), "--")){
                cadastroCompleto = false;
            }
        }
        if (!cadastroCompleto){
            Toast.makeText(this, getString(R.string.aviso_cadastro_incompleto), Toast.LENGTH_SHORT).show();
            Intent cadastro = new Intent(this,C_cadastro.class);
            startActivity(cadastro);
        }
        return cadastroCompleto;
    }

//**********Verificar campos preenchidos**********
    private boolean verificaCamposPreenchidos(){
    boolean camposPreenchidos = true;
        if (nomeDoGrupo.getText().toString().equals("")){
            Toast.makeText(this, "Escreva o nome do grupo.", Toast.LENGTH_SHORT).show();
            camposPreenchidos = false;
        }
        for (String serie:listaSeries) {
            String chave = "checkbox" + serie;
            @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(chave, "id", getPackageName());
            CheckBox chkBox = findViewById(resId);
            if(chkBox.isChecked()){
                checkboxSeries++;
            }
        }
        if (checkboxSeries == 0){
            Toast.makeText(this, "Escolha as séries.", Toast.LENGTH_SHORT).show();
            camposPreenchidos = false;
        }
        else{
            checkboxSeries = 0;
        }
        if (premioCombinado.getText().toString().equals("")){
            Toast.makeText(this, "Escreva o prêmio combinado.", Toast.LENGTH_SHORT).show();
            camposPreenchidos = false;
        }
        return camposPreenchidos;
    }

    private String verificaCaracteres(String nomeDoGrupo){
        int[] opcoes_de_a = {97,192,193,194,195,196,197,198,224,225,226,227,228,229,230}; // trocar por 97.
        int[] opcoes_de_e = {101,200,201,202,203,232,233,234,235,274,275,278,279,280,281}; // trocar por 101.
        int[] opcoes_de_i = {105,161,204,205,206,207,236,237,238,239,298,299,302,303}; // trocar por 105.
        int[] opcoes_de_o = {111,210,211,212,213,214,216,242,243,244,245,246,248,332,333,338,339}; // trocar por 111.
        int[] opcoes_de_u = {117,217,218,219,220,249,250,251,252,362,363}; // trocar por 117.
        int[] opcoes_de_c = {99,199,231,262,263,268,269}; // trocar por 99.
        int[] opcoes_de_n = {110,209,241}; // trocar por 110.
        int[] opcoes_de_y = {121,221,253,255}; // trocar por 121.
        int[][] opcoes_de_letras = {opcoes_de_a,opcoes_de_e,opcoes_de_i,opcoes_de_o,opcoes_de_u,opcoes_de_c,opcoes_de_n,opcoes_de_y};
        StringBuilder nomeLimpoDoGrupo = new StringBuilder();
        nomeLimpoDoGrupo.append(nomeDoGrupo);
        for (int k = nomeLimpoDoGrupo.length()-1; k>=0; k--){
            int c = nomeLimpoDoGrupo.charAt(k);
            for (int[] opcoes_de_letra : opcoes_de_letras) {
                for (int j = 1; j < opcoes_de_letra.length; j++) {
                    if (c == opcoes_de_letra[j]) {
                        nomeLimpoDoGrupo.setCharAt(k, (char) opcoes_de_letra[0]);
                        c = nomeLimpoDoGrupo.charAt(k);
                    }
                }
            }
            //48 ~ 57 = intervalo números
            //65 ~ 90 = intervalo letras maiúsculas
            //97 ~ 122 = intervalo letras minúsculas
            if (!((c>=48)&&(c<=57))&&!((c>=65)&&(c<=90))&&!((c>=97)&&(c<=122))){
                nomeLimpoDoGrupo.deleteCharAt(k);
            }
        }
        return String.valueOf(nomeLimpoDoGrupo);
    }

    private String criarIdGrupo(String nomeDoGrupo, String meuNumero){
        StringBuilder idGrupo = new StringBuilder();
        StringBuilder nomeGrupo = new StringBuilder();
        nomeGrupo.append(nomeDoGrupo);
        for (int k = 0; k<nomeGrupo.length(); k++){
            idGrupo.append((int)nomeGrupo.charAt(k));
        }
        idGrupo.append(meuNumero);
        return String.valueOf(idGrupo);
    }

    private boolean criarGrupo(){
        SharedPreferences meusDados = getSharedPreferences("meusDados",MODE_PRIVATE);
        String meuNumero = meusDados.getString("meuNumero","--");
        String nomeLimpoDoGrupo = verificaCaracteres(String.valueOf(nomeDoGrupo.getText()));
        String idGrupo = criarIdGrupo(String.valueOf(nomeDoGrupo.getText()), meuNumero);
        arquivoGrupo = "grupo" + ano + meuNumero + "_" + nomeLimpoDoGrupo;
        if (getDadosComuns(arquivoGrupo).equals(String.valueOf(nomeDoGrupo.getText()))){
            Toast.makeText(this, "Este grupo já existe.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            setDadosComuns(arquivoGrupo, String.valueOf(nomeDoGrupo.getText()));
            SharedPreferences novoGrupo = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
            SharedPreferences.Editor novoGrupoEditor = novoGrupo.edit();
            //O "Contato1" será o criador e administrador do grupo.
            novoGrupoEditor.putString("meuNomeContato1", meusDados.getString("palpiteiro","--"));
            novoGrupoEditor.putString("meuNumeroContato1", meusDados.getString("meuNumero","--"));
            novoGrupoEditor.putString("apelidoContato1", meusDados.getString("palpiteiro","--"));
            novoGrupoEditor.putString("timeDoCoracaoContato1", meusDados.getString("timeDoCoracao","--"));
            novoGrupoEditor.putString("colocacaoContato1", "1");
            novoGrupoEditor.putString("ptsTotalContato1", "0");
            novoGrupoEditor.putString("totalPontosSerieAContato1", meusDados.getString("totalPontosSerieA","0"));
            novoGrupoEditor.putString("totalPontosSerieBContato1", meusDados.getString("totalPontosSerieB","0"));
            novoGrupoEditor.putString("totalPontosSerieCContato1", meusDados.getString("totalPontosSerieC","0"));
            novoGrupoEditor.putString("totalPontosSerieDContato1", meusDados.getString("totalPontosSerieD","0"));
            novoGrupoEditor.putString("statusContato1", "administrador");
            novoGrupoEditor.putString("atualizadoEmContato1", hoje);
            novoGrupoEditor.putString("ano", ano);
            novoGrupoEditor.putString("nomeDoGrupo", String.valueOf(nomeDoGrupo.getText()));
            novoGrupoEditor.putString("idGrupo", idGrupo);
            for (String serie : listaSeries) {
                String chave = "checkbox" + serie;
                @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(chave, "id", getPackageName());
                CheckBox chkBox = findViewById(resId);
                if (chkBox.isChecked()) {
                    novoGrupoEditor.putString(chave, "true");
                } else {
                    novoGrupoEditor.putString(chave, "false");
                }
            }
            novoGrupoEditor.putString("premioCombinado", String.valueOf(premioCombinado.getText()));
            novoGrupoEditor.apply();
            return true;
        }
    }

    private void atualizarColocacao(String arquivoGrupo, String nomeDoGrupo){
        try {
            Intent atualizar_Colocacao = new Intent(this, Z_atualizar_colocacao.class);
            atualizar_Colocacao.putExtra("ano",ano);
            atualizar_Colocacao.putExtra("baixar_grupos","não");
            atualizar_Colocacao.putExtra("arquivoGrupo",arquivoGrupo);
            atualizar_Colocacao.putExtra("nomeDoGrupo",nomeDoGrupo);
            startActivity(atualizar_Colocacao);
        }catch (Exception e){
            Log.v("Error","Error: "+e);
        }finally {
            backupMeusGrupos();
            chamarCriarConvite();
        }
    }

    private void backupMeusGrupos(){
        SharedPreferences meusDados = getSharedPreferences("meusDados",MODE_PRIVATE);
        String meusGruposBackup = "meusGrupos"+ano+"Backup"+meusDados.getString("meuNumero","--");
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

    private void chamarCriarConvite(){
        Intent criarConvite = new Intent(this, I_criar_convite.class);
        criarConvite.putExtra("arquivoGrupo", arquivoGrupo);
        startActivity(criarConvite);
        finish();
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

    //**********MENU**********
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_i_criar_grupos, menu);
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
                instruction.putExtra("tela","I_criar_grupos");
                startActivity(instruction);
                break;
            case 1:
                finish();
        }
        return true;
    }
}
