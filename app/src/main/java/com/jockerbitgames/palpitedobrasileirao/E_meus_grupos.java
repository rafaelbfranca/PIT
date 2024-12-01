package com.jockerbitgames.palpitedobrasileirao;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class E_meus_grupos extends AppCompatActivity {

    private final String redeIndisponivel = "Rede indisponível.";
    private String ano;
    private boolean anoCorrente;
    private String[] listaAnoDoCampeonato;
    private boolean redeDisponivel;
    private static final int MY_PERMISSIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.e_meus_grupos);
        redeDisponivel = Z_static_functions.verificandoRede(this) != 0;
        resgataArrays();
        ano = getDadosComuns("ano");
        atualizaCampos();
        if (ano.equals(listaAnoDoCampeonato[0])) {
            anoCorrente = true;
            if (getDadosComuns("E_meus_grupos").equals("--")) {
                setDadosComuns();
                atualizaGrupos();
            }
        }else{
            anoCorrente = false;
        }
        verificaPermissoes();
    }

    private void verificaPermissoes(){
        if (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
            new AlertDialog.Builder(this)
                    .setMessage("Para enviar convites é necessário liberar acesso aos contatos.")
                    .setPositiveButton("Entendi", (dialog, which) -> ActivityCompat.requestPermissions(E_meus_grupos.this,new String[]{Manifest.permission.READ_CONTACTS},MY_PERMISSIONS))
                    .show();
        }
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
    }

    private String getDadosComuns(String chave){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        return getDadosComuns.getString(chave,"--");
    }

    private void setDadosComuns(){
        //Dados que serão lidos por outras classes.
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        SharedPreferences.Editor editor = dadosComuns.edit();
        editor.putString("E_meus_grupos", "aberta");
        editor.apply();
    }

    private void atualizaCampos(){
        TextView anoDoCampeonato = findViewById(R.id.ano_do_campeonato);
        anoDoCampeonato.setText(ano);
    }

    private void atualizaGrupos(){
        if (redeDisponivel) {
            SharedPreferences getGrupos = getSharedPreferences("dadosComuns", MODE_PRIVATE);
            String chaveGrupo = "grupo" + ano;
            Map<String, ?> allEntries = getGrupos.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().contains(chaveGrupo)) {
                    atualizarColocacao(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        else {
            Toast.makeText(this, redeIndisponivel, Toast.LENGTH_LONG).show();
        }
    }

    private void atualizarColocacao(String arquivoGrupo, String nomeDoGrupo){
        Intent atualizar_Colocacao = new Intent(this, Z_atualizar_colocacao.class);
        atualizar_Colocacao.putExtra("ano",ano);
        atualizar_Colocacao.putExtra("baixar_grupos","sim");
        atualizar_Colocacao.putExtra("arquivoGrupo",arquivoGrupo);
        atualizar_Colocacao.putExtra("nomeDoGrupo",nomeDoGrupo);
        startActivity(atualizar_Colocacao);
    }

    public void buttonExibirGrupo (@SuppressWarnings("unused") View view){
        chamarVibrate();
        Intent exibir = new Intent(this,F_exibir_grupos.class);
        startActivity(exibir);
    }

    public void buttonCriarGrupo (@SuppressWarnings("unused") View view){
        if (redeDisponivel && anoCorrente) {
            chamarVibrate();
            Intent escreverGrupo = new Intent(this, I_criar_grupos.class);
            startActivity(escreverGrupo);
        }
        else{
            showMesage();
        }
    }

    public void buttonEditarExcluirGrupo (@SuppressWarnings("unused") View view){
        if (redeDisponivel && anoCorrente) {
            chamarVibrate();
            Intent editar = new Intent(this, J_edit_exclud_grupos.class);
            startActivity(editar);
        }
        else{
            showMesage();
        }
    }

    private void showMesage(){
        if (!redeDisponivel){
            Toast.makeText(this, redeIndisponivel, Toast.LENGTH_LONG).show();
        }
        if (!anoCorrente){
            Toast.makeText(this, "Ação não permitida para campeonatos de anos anteriores.", Toast.LENGTH_LONG).show();
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

    public void onBackPressed(){
        Intent inicial = new Intent(this,A_tela_inicial.class);
        startActivity(inicial);
        finish();
    }

    //**********MENU**********
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_e_meus_grupos, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        List<Object> ids = new ArrayList<>();
        ids.add(R.id.instrucoes);
        ids.add(R.id.atualizarGrupos);
        ids.add(R.id.convitesRecebidos);
        ids.add(R.id.convitesEnviados);
        ids.add(R.id.voltar);

        switch (ids.indexOf(item.getItemId())) {
            case 0:
                Intent instruction = new Intent(this,B_instrucoes.class);
                instruction.putExtra("tela","E_meus_grupos");
                startActivity(instruction);
                break;
            case 1:
                atualizaGrupos();
                break;
            case 2:
                if (redeDisponivel) {
                    Intent convitesRecebidos = new Intent(this, I_convites_recebidos.class);
                    startActivity(convitesRecebidos);
                    finish();
                } else {
                    Toast.makeText(this, redeIndisponivel, Toast.LENGTH_LONG).show();
                }
                break;
            case 3:
                Intent convitesEnviados = new Intent(this,I_convites_enviados.class);
                startActivity(convitesEnviados);
                break;
            case 4:
                onBackPressed();
                break;
        }
        return true;
    }

}
