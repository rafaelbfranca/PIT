package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import androidx.appcompat.app.AppCompatActivity;

public class A_menu_inicial extends AppCompatActivity {

    private boolean redeDisponivel;
    private String botao,avisoNovaVersao,avisoRedeIndisponivel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_menu_inicial);
        getDadosComuns();
        redeDisponivel = Z_static_functions.verificandoRede(this) != 0;
        if (!redeDisponivel && avisoRedeIndisponivel.equals("NOK")){
            exibirAvisoRedeIndisponivel();
        }
        exibirAvisoAtualizacao();
    }

    private void getDadosComuns(){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        avisoNovaVersao = getDadosComuns.getString("avisoNovaVersao","NOK");
        avisoRedeIndisponivel = getDadosComuns.getString("avisoRedeIndisponivel","NOK");
    }

    private void setDadosComuns(String chave){
        //Dados que serão lidos por outras classes.
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        SharedPreferences.Editor editor = dadosComuns.edit();
        editor.putString(chave, "OK");
        editor.apply();
    }

    public void buttonPalpitesGrupos(View view) {
        chamarVibrate();
        botao = (String) view.getContentDescription();
        executarBotao();
    }

    public void buttonResultadosJogos(View view) {
        chamarVibrate();
        botao = (String) view.getContentDescription();
        executarBotao();
    }

    public void buttonTabelaClassificacao(View view) {
        chamarVibrate();
        botao = (String) view.getContentDescription();
        executarBotao();
    }

    private void executarBotao(){
        switch (botao){
            case "Palpites e Grupos":
                chamarTelaInicial();
                break;
            case "Resultados dos Jogos":
                chamarResultadosJogos();
                break;
            case "Tabela de Classificação":
                chamarTabelaClassificacao();
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

    private void chamarTelaInicial(){
        Intent telaInicial = new Intent(this, A_tela_inicial.class);
        startActivity(telaInicial);
        finish();
    }

    private void chamarResultadosJogos(){
        Intent resultadosJogos = new Intent(this, G_resultados_dos_jogos.class);
        startActivity(resultadosJogos);
        finish();
    }

    private void chamarTabelaClassificacao(){
        Intent tabelaClassificacao = new Intent(this, G_tabela_classificacao.class);
        startActivity(tabelaClassificacao);
        finish();
    }

    private void exibirAvisoRedeIndisponivel(){
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("Rede indisponível.\n" +
                        "As informações exibidas poderão estar desatualizadas.\n" +
                        "Não será possível criar, editar nem excluir grupos.\n" +
                        "Também não será possível aceitar ou recusar convites.")
                .setPositiveButton("OK", (dialog, which) -> setDadosComuns("avisoRedeIndisponivel"))
                .show();
    }

    private void exibirAvisoAtualizacao(){
        if (avisoNovaVersao.equals("NOK")) {
            final SharedPreferences referencias = getSharedPreferences("referencias", MODE_PRIVATE);
            int newVersionCode = Integer.parseInt(Objects.requireNonNull(referencias.getString("versionCode", "1")));
            int actualVersionCode = newVersionCode;
            PackageManager manager = this.getPackageManager();
            PackageInfo info;
            try {
                info = manager.getPackageInfo(this.getPackageName(), 0);
                actualVersionCode = info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (newVersionCode > actualVersionCode){
                new AlertDialog.Builder(this)
                        .setMessage("A versão " + newVersionCode + " do Palpite do Brasileirão está disponível.\n" +
                                "Sua versão atual é: " + actualVersionCode + "\n" +
                                "Atualize assim que for possível")
                        .setPositiveButton("OK", (dialog, which) -> setDadosComuns("avisoNovaVersao"))
                        .show();
            }
        }
    }

    //**********************MENU**********************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_a_menu_inicial, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        List<Object> ids = new ArrayList<>();
        ids.add(R.id.instrucoes);
        ids.add(R.id.cadastro);
        ids.add(R.id.canalSuporte);
        ids.add(R.id.sair);

        switch (ids.indexOf(item.getItemId())) {
            case 0:
                Intent instrucoes = new Intent(this, B_instrucoes.class);
                instrucoes.putExtra("tela","A_menu_inicial");
                startActivity(instrucoes);
                break;
            case 1:
                Intent cadastro = new Intent(this, C_cadastro.class);
                startActivity(cadastro);
                break;
            case 2:
                if (redeDisponivel) {
                    Intent contato = new Intent(this, N_suporte.class);
                    startActivity(contato);
                } else {
                    Toast.makeText(this, "Rede indisponível.", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                finishAffinity();
        }
        return true;
    }

}
