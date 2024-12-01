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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

public class A_tela_inicial extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private AdView adView;
    private String[] listaAnoDoCampeonato;
    private ArrayList<String> listaSeries;
    private Date momento;
    private String ano;
    private String hoje;
    private Boolean toqueNaTela = false;
    private String botao;
    private String serieEscolhida;
    private boolean redeDisponivel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_tela_inicial);

        //verificandoRede();
        redeDisponivel = Z_static_functions.verificandoRede(this) != 0;
        if (redeDisponivel){
            loadAds();
        }
        registraData();
        ano = getDadosComuns();
        resgataArrays();
        implementaSpinners();
        atualizaCampos();
        avisoConvites();
    }

    //-----Início Bloco Exibição de Anúncios-----
    private void loadAds(){
        adView = findViewById(R.id.adsTelaInicial);
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

    private void executarBotao(){
        switch (botao){
            case "detalhadoSerieA":
            case "detalhadoSerieB":
            case "detalhadoSerieC":
            case "detalhadoSerieD":
                telaSerie(serieEscolhida);
                break;
            case "meusGrupos":
                if (redeDisponivel){
                    chamarMeusGrupos();
                }
                else{
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setMessage("Rede indisponível.\n" +
                                    "As informações dos grupos poderão estar desatualizadas.\n" +
                                    "Não será possível criar, editar nem excluir grupos.\n" +
                                    "Também não será possível aceitar ou recusar convites.")
                            .setPositiveButton("OK", (dialog, which) -> chamarMeusGrupos())
                            .show();
                }
                break;
            case "meusPalpites":
                if (redeDisponivel){
                    chamarMeusPalpites();
                }
                else{
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setMessage("Rede indisponível.\n" +
                                    "O aplicativo salvará, mas não fará backup dos seus palpites.")
                            .setPositiveButton("OK", (dialog, which) -> chamarMeusPalpites())
                            .show();
                }
        }
    }

    private void registraData(){
        momento = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        hoje = formatter.format(momento);
        setDadosComuns("atualizadoEm",hoje);
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

    @SuppressLint("ClickableViewAccessibility")
    private void implementaSpinners() {
        //Implementa o dropdown de escolha do ano.
        Spinner dropdown_ano = findViewById(R.id.dropdown_ano_spinner);
        ArrayAdapter<String> adapter_ano = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaAnoDoCampeonato);
        adapter_ano.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_ano.setAdapter(adapter_ano);
        dropdown_ano.setOnTouchListener((View v, MotionEvent event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                toqueNaTela = true;
            }
            return false;
        });
        dropdown_ano.setOnItemSelectedListener(this);
        if (!ano.equals("--")) {
            int pos = adapter_ano.getPosition(ano);
            dropdown_ano.setSelection(pos);
        }
    }

    private void setDadosComuns(String chave, String valor){
        //Dados que serão lidos por outras classes.
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        SharedPreferences.Editor editor = dadosComuns.edit();
        editor.putString(chave, valor);
        editor.apply();
    }

    private String getDadosComuns(){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        return getDadosComuns.getString("ano","--");
    }

    private void atualizaCampos(){
        //Atualiza campos totais.
        SharedPreferences meusPontos = getSharedPreferences("meusPontos"+ano, MODE_PRIVATE);
        for (String serie:listaSeries) {
            String total = meusPontos.getString("totalPontos"+serie,"--");
            @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier("total_pontos_"+serie, "id", getPackageName());
            TextView atualizaCampo = findViewById(resId);
            atualizaCampo.setText(total);
        }
    }

    private void avisoConvites(){
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns",MODE_PRIVATE);
        boolean convitesPendentes = Boolean.parseBoolean(dadosComuns.getString("convitesPendentes", "false"));
        if (convitesPendentes) {
            String registroAviso = dadosComuns.getString("avisoConvites", "--");
            if (!Objects.equals(registroAviso, "--")) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("DDD");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MMM-yyyy");
                int dataHoje;
                int dataRegistroAviso;
                try {
                    dataHoje = Integer.parseInt(formatter.format(momento));
                    dataRegistroAviso = Integer.parseInt(formatter.format(Objects.requireNonNull(formatter2.parse(registroAviso))));
                    if (dataHoje > dataRegistroAviso) {
                        alertaConvitesPendentes();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                alertaConvitesPendentes();
            }
            SharedPreferences.Editor dadosComunsEditor = dadosComuns.edit();
            dadosComunsEditor.putString("avisoConvites", hoje);
            dadosComunsEditor.apply();
        }
    }

    private void alertaConvitesPendentes(){
        new AlertDialog.Builder(this)
                .setTitle("CONVITES")
                .setMessage("\n\nExistem convites pendentes\n"+
                        "Quero verificá-los ...\n\n")
                .setPositiveButton("Agora", (dialog, which) -> chamarConvitesRecebidos())
                .setNegativeButton("Depois", null)
                .show();
    }

    private void chamarConvitesRecebidos(){
        Intent convitesRecebidos = new Intent(this,I_convites_recebidos.class);
        startActivity(convitesRecebidos);
        finish();
    }

    public void buttonDetalhado(View view){
        chamarVibrate();
        botao = (String) view.getContentDescription();
        switch (botao) {
            case "detalhadoSerieA":
                serieEscolhida = "A";
                break;
            case "detalhadoSerieB":
                serieEscolhida = "B";
                break;
            case "detalhadoSerieC":
                serieEscolhida = "C";
                break;
            case "detalhadoSerieD":
                serieEscolhida = "D";
                break;
        }
        executarBotao();
    }

    private void telaSerie(String serieEscolhida){
        Intent serie = new Intent(this,D_serie.class);
        serie.putExtra("serieEscolhida", serieEscolhida);
        startActivity(serie);
        finish();
    }

    public void buttonMeusGrupos(@SuppressWarnings("unused") View view){
        chamarVibrate();
        botao = "meusGrupos";
        executarBotao();
    }

    private void chamarMeusGrupos(){
        Intent meusGrupos = new Intent(this, E_meus_grupos.class);
        startActivity(meusGrupos);
        finish();
    }

    public void buttonMeusPalpites(@SuppressWarnings("unused") View view){
        chamarVibrate();
        botao = "meusPalpites";
        executarBotao();
    }

    private void chamarMeusPalpites(){
        Intent meusPalpites = new Intent(this,H_meus_palpites_serie_A.class);
        startActivity(meusPalpites);
        finish();
    }

    //Executa algo a partir das seleções nos spinners.
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(toqueNaTela) {
            toqueNaTela = false;
            ano = listaAnoDoCampeonato[position];
            setDadosComuns("ano",ano);
            atualizaCampos();
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {}

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
        Intent menuInicial = new Intent(this, A_menu_inicial.class);
        startActivity(menuInicial);
        finish();
    }

    //**********************MENU**********************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_a_tela_inicial, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        List<Object> ids = new ArrayList<>();
        ids.add(R.id.instrucoes);
        ids.add(R.id.atualizar_dados);
        ids.add(R.id.voltar);

        switch (ids.indexOf(item.getItemId())) {
            case 0:
                Intent instrucoes = new Intent(this, B_instrucoes.class);
                instrucoes.putExtra("tela","A_tela_inicial");
                startActivity(instrucoes);
                break;
            case 1:
                if (redeDisponivel) {
                    Intent atualizarDados = new Intent(this, Z03_atualizar_dados.class);
                    atualizarDados.putExtra("origem","A_tela_inicial");
                    startActivity(atualizarDados);
                    finish();
                } else {
                    Toast.makeText(this, "Rede indisponível.", Toast.LENGTH_LONG).show();
                }
                break;
            case 2:
                Intent menuInicial = new Intent(this, A_menu_inicial.class);
                startActivity(menuInicial);
                finish();
        }
        return true;
    }
}
