package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class F_exibir_grupos extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private AdView adView;
    private String ano;
    private List<String> lista_grupos_spinner;
    private List<String> lista_grupos_arquivo;
    private Boolean toqueNaTela = false;
    private ListView listView;
    private String arquivoGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.f_exibir_grupos);
        loadAds();
        ano = getDadosComuns();
        lista_grupos_arquivo = new ArrayList<>();
        lista_grupos_arquivo.add("Não há grupos.");
        lista_grupos_spinner = new ArrayList<>();
        lista_grupos_spinner.add("Não há grupos.");
        getListas();
        implementarSpinners();
        listView = findViewById(R.id.listView);
        includeHeaderView();
        setAdapterArray(arquivoGrupo);
    }

    //-----Início Bloco Exibição de Anúncios-----
    private void loadAds(){
        adView = findViewById(R.id.adsExibirGrupos);
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

    @SuppressLint("ClickableViewAccessibility")
    private void implementarSpinners(){
        //Implementa o dropdown de escolha do grupo.
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
        //*********************(Fim do escopo do comentário)
    }

    //Executa algo a partir da seleção nos spinners.
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(toqueNaTela) {
            toqueNaTela = false;
            arquivoGrupo = lista_grupos_arquivo.get(position);
                    setAdapterArray(arquivoGrupo);
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {}
    //*********************(Fim do escopo do comentário)**********************

    private void includeHeaderView(){
        View headerView = getLayoutInflater().inflate(R.layout.f_exibir_grupos_list_view_header,listView,false);
        listView.addHeaderView(headerView);
    }

    private void setAdapterArray(String arquivoGrupo){
        ArrayList<PalpiteiroModel> palpiteiros;
        palpiteiros = new ArrayList<>();

        List<String> lista_palpiteiros = new ArrayList<>();

        SharedPreferences grupoEscolhido = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
        Map<String,?> allEntries = grupoEscolhido.getAll();
        int numPalpiteiros = 0;
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains("Contato")){
                lista_palpiteiros.add(entry.getKey());
                if (entry.getKey().contains("meuNome")){
                    numPalpiteiros++;
                }
            }
        }
        //Busca de todos os atributos.
        for (int i=1; i<=numPalpiteiros; i++){
            for (String dado: lista_palpiteiros){
                if (dado.contains("colocacao")){
                    if (Objects.equals(grupoEscolhido.getString(dado,"--"),(String.valueOf(i)))){
                        String colocacao = grupoEscolhido.getString(dado,"--");
                        dado = dado.replace("colocacao","apelido");
                        String nome = grupoEscolhido.getString(dado,"--");
                        dado = dado.replace("apelido","timeDoCoracao");
                        String time = grupoEscolhido.getString(dado,"--");
                        dado = dado.replace("timeDoCoracao","ptsTotal");
                        String ptsTotal = grupoEscolhido.getString(dado,"0");
                        dado = dado.replace("ptsTotal","totalPontosSerieA");
                        String ptsSerieA = grupoEscolhido.getString(dado,"0");
                        dado = dado.replace("totalPontosSerieA","totalPontosSerieB");
                        String ptsSerieB = grupoEscolhido.getString(dado,"0");
                        dado = dado.replace("totalPontosSerieB","totalPontosSerieC");
                        String ptsSerieC = grupoEscolhido.getString(dado,"0");
                        dado = dado.replace("totalPontosSerieC","totalPontosSerieD");
                        String ptsSerieD = grupoEscolhido.getString(dado,"0");
                        dado = dado.replace("totalPontosSerieD","atualizadoEm");
                        String atualizadoEm = grupoEscolhido.getString(dado,"0");

                        //Criação do objeto.
                        PalpiteiroModel palpiteiro = new PalpiteiroModel(colocacao, nome, time, ptsTotal, ptsSerieA, ptsSerieB, ptsSerieC, ptsSerieD, atualizadoEm);
                        //Carregamento de todos os atributos.
                        palpiteiro.setColocacao(colocacao);
                        palpiteiro.setNome(nome);
                        palpiteiro.setTime(time);
                        palpiteiro.setPtsTotal(ptsTotal);
                        palpiteiro.setPtsSerieA(ptsSerieA);
                        palpiteiro.setPtsSerieB(ptsSerieB);
                        palpiteiro.setPtsSerieC(ptsSerieC);
                        palpiteiro.setPtsSerieD(ptsSerieD);
                        palpiteiro.setAtualizadoEm(atualizadoEm);
                        //Inclusão do objeto carregado na lista de objetos.
                        palpiteiros.add(palpiteiro);
                        break;
                    }
                }
            }
        }
        //Transferência da lista de objetos para o adapter.
        PalpiteiroAdapter adapter = new PalpiteiroAdapter(this, palpiteiros);
        //Inicialização da listview com o adapter.
        listView.setAdapter(adapter);
    }

    public void showOutrasInfos(@SuppressWarnings("unused") View view) {
        chamarVibrate();
        Intent showOutrasInfos = new Intent(this,F_exibir_outras_infos.class);
        showOutrasInfos.putExtra("arquivoGrupo",arquivoGrupo);
        showOutrasInfos.putExtra("ano",ano);
        startActivity(showOutrasInfos);
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

    static class PalpiteiroModel{
        String colocacao,nome,time,ptsTotal,ptsSerieA,ptsSerieB,ptsSerieC,ptsSerieD,atualizadoEm;

        PalpiteiroModel(String colocacao, String nome, String time, String ptsTotal, String ptsSerieA, String ptsSerieB, String ptsSerieC, String ptsSerieD, String atualizadoEm){
            this.colocacao = colocacao;
            this.nome = nome;
            this.time = time;
            this.ptsTotal = ptsTotal;
            this.ptsSerieA = ptsSerieA;
            this.ptsSerieB = ptsSerieB;
            this.ptsSerieC = ptsSerieC;
            this.ptsSerieD = ptsSerieD;
            this.atualizadoEm = atualizadoEm;
        }

        void setColocacao(String colocacao) {
            this.colocacao = colocacao;
        }

        void setNome(String nome) {
            this.nome = nome;
        }

        void setTime(String time) {
            this.time = time;
        }

        void setPtsTotal(String ptsTotal) {
            this.ptsTotal = ptsTotal;
        }

        void setPtsSerieA(String ptsSerieA) {
            this.ptsSerieA = ptsSerieA;
        }

        void setPtsSerieB(String ptsSerieB) {
            this.ptsSerieB = ptsSerieB;
        }

        void setPtsSerieC(String ptsSerieC) {
            this.ptsSerieC = ptsSerieC;
        }

        void setPtsSerieD(String ptsSerieD) {
            this.ptsSerieD = ptsSerieD;
        }

        void setAtualizadoEm(String atualizadoEm){
            this.atualizadoEm = atualizadoEm;
        }
    }

    static class PalpiteiroAdapter extends BaseAdapter {

        private final Context context;
        private final ArrayList<PalpiteiroModel> palpiteiros;

        PalpiteiroAdapter(Context context, ArrayList<PalpiteiroModel> palpiteiros) {
            this.context = context;
            this.palpiteiros = palpiteiros;
        }

        @Override
        public int getCount() {
            return palpiteiros.size();
        }

        @Override
        public Object getItem(int position) {
            return palpiteiros.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            PalpiteiroModel palpiteiro = palpiteiros.get(position);
            final ViewHolder holder;
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.f_exibir_grupos_list_view_item,parent,false);
                holder = new ViewHolder();
                holder.colocacaoPalpiteiro = convertView.findViewById(R.id.colocacaoPalpiteiro);
                holder.nomePalpiteiro = convertView.findViewById(R.id.nomePalpiteiro);
                holder.timePalpiteiro = convertView.findViewById(R.id.timePalpiteiro);
                holder.ptsTotalPalpiteiro = convertView.findViewById(R.id.ptsTotalPalpiteiro);
                holder.ptsSerieAPalpiteiro = convertView.findViewById(R.id.ptsSerieAPalpiteiro);
                holder.ptsSerieBPalpiteiro = convertView.findViewById(R.id.ptsSerieBPalpiteiro);
                holder.ptsSerieCPalpiteiro = convertView.findViewById(R.id.ptsSerieCPalpiteiro);
                holder.ptsSerieDPalpiteiro = convertView.findViewById(R.id.ptsSerieDPalpiteiro);
                holder.atualizadoEmPalpiteiro = convertView.findViewById(R.id.atualizadoEmPalpiteiro);
                convertView.setTag(holder);
            }
            else{
                holder = (ViewHolder) convertView.getTag();
            }

            holder.colocacaoPalpiteiro.setText(palpiteiro.colocacao);
            holder.nomePalpiteiro.setText(palpiteiro.nome);
            holder.timePalpiteiro.setText(palpiteiro.time);
            holder.ptsTotalPalpiteiro.setText(palpiteiro.ptsTotal);
            holder.ptsSerieAPalpiteiro.setText(palpiteiro.ptsSerieA);
            holder.ptsSerieBPalpiteiro.setText(palpiteiro.ptsSerieB);
            holder.ptsSerieCPalpiteiro.setText(palpiteiro.ptsSerieC);
            holder.ptsSerieDPalpiteiro.setText(palpiteiro.ptsSerieD);
            holder.atualizadoEmPalpiteiro.setText(palpiteiro.atualizadoEm);

            return convertView;
        }

        static class ViewHolder {
            TextView colocacaoPalpiteiro,
                    nomePalpiteiro,
                    timePalpiteiro,
                    ptsTotalPalpiteiro,
                    ptsSerieAPalpiteiro,
                    ptsSerieBPalpiteiro,
                    ptsSerieCPalpiteiro,
                    ptsSerieDPalpiteiro,
                    atualizadoEmPalpiteiro;
        }
    }

    //**********MENU**********
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_f_exibir_grupos, menu);
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
                instruction.putExtra("tela","F_exibir_grupos");
                startActivity(instruction);
                break;
            case 1:
                finish();
                break;
        }
        return true;
    }
}
