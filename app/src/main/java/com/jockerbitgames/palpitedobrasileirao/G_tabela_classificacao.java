package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

public class G_tabela_classificacao extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private AdView adView;
    private String serieEscolhida,ano;
    private String abrirTabela;
    private Spinner dropdown_serieTabela_spinner;
    private Spinner dropdown_anoTabela_spinner;
    private Spinner dropdown_faseTabela_spinner;
    private Spinner dropdown_grupoTabela_spinner;
    private boolean toqueNaTela = false;
    private ListView listViewTabela;
    private boolean redeDisponivel;
    private String[] lista_ano_spinner;
    private String[] lista_serie_spinner;
    private String[] lista_serie_escolhida;
    private String[] lista_fases_spinner;
    private String[] grupo;
    private String[][]lista_grupos_spinner;
    View headerView, footerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.g_tabela_classificacao);
        loadAds();
        ano = getDadosComuns();
        redeDisponivel = Z_static_functions.verificandoRede(this) != 0;
        listViewTabela = findViewById(R.id.listViewTabela);
        grupo = new String[]{};
        resgataArrays();
        implementaSpinner(lista_serie_spinner,lista_ano_spinner,lista_fases_spinner,lista_grupos_spinner[0]);
        setAdapterArray(abrirTabela);
    }

    //-----Início Bloco Exibição de Anúncios-----
    private void loadAds(){
        adView = findViewById(R.id.adsClassificacao);
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

    private void setDadosComuns(@SuppressWarnings("SameParameterValue") String chave, String valor){
        //Dados que serão lidos por outras classes.
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        SharedPreferences.Editor editor = dadosComuns.edit();
        editor.putString(chave, valor);
        editor.apply();
    }

    private void resgataArrays(){
        Resources res = getResources();
        lista_serie_spinner = res.getStringArray(R.array.series_array);
        lista_serie_escolhida = new String[lista_serie_spinner.length];
        for (int pos = 0; pos < lista_serie_spinner.length; pos++) {
            lista_serie_escolhida[pos] = lista_serie_spinner[pos].replace("Série ", "Serie");
        }
        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
        int i = 0;
        while(!Objects.equals(dadosCampeonatos.getString("dadosCampeonatoAno" + i,"--"), "--")){
            i++;
        }
        lista_ano_spinner = new String[i];
        for (int j = 0; j<i; j++){
            lista_ano_spinner[j] = dadosCampeonatos.getString("dadosCampeonatoAno" + j,"--");
        }
        serieEscolhida = lista_serie_escolhida[0];
        if (ano.equals("--")){
            ano = lista_ano_spinner[0];
            setDadosComuns("ano",lista_ano_spinner[0]);
        }
        resgataFasesGrupos(lista_serie_escolhida[0],ano);
    }

    private void resgataFasesGrupos(String serieEsc, String anoCamp){
        SharedPreferences dadosCampeonatos = getSharedPreferences("dadosCampeonatos",MODE_PRIVATE);
        int totalFases = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + serieEsc + anoCamp + "TotalFases", "1")));
        lista_fases_spinner = new String[totalFases];
        lista_grupos_spinner = new String[totalFases][];
        int[] totalGruposNaFase = new int[totalFases];
        for (int i = 0; i< totalFases; i++){
            totalGruposNaFase[i] = Integer.parseInt(Objects.requireNonNull(dadosCampeonatos.getString("dadosCampeonato" + serieEsc + anoCamp + "F" + (i + 1) + "TotalGruposNaFase", "1")));
            lista_grupos_spinner[i] = new String[totalGruposNaFase[i]];
            if (totalFases >1){
                if (i+1 == totalFases){
                    lista_fases_spinner[i] = "FINAL";
                }
                else{
                    lista_fases_spinner[i] = (i+1)+"ª FASE";
                }
                for (int j = 0; j< totalGruposNaFase[i]; j++){
                    lista_grupos_spinner[i][j] = "GRUPO "+(j+1);
                }
            }
            else{
                lista_fases_spinner[i] = "FASE ÚNICA";
                int j = 0;
                if (totalGruposNaFase[i]>1){
                    for (; j< totalGruposNaFase[i]; j++){
                        lista_grupos_spinner[i][j] = "GRUPO "+(j+1);
                    }
                }
                else{
                    lista_grupos_spinner[i][j] = "GRUPO ÚNICO";
                }
            }
        }
        abrirTabela = "tabela"+serieEsc+"F1"+"G1"+anoCamp;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void implementaSpinner(String[] serie, String[] anoList, String[] fase, String[] grupo){
        //Implementa o dropdown de escolha da serie.
        dropdown_serieTabela_spinner = findViewById(R.id.dropdown_serieTabela_spinner);
        ArrayAdapter<String> adapterSerie = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, serie);
        adapterSerie.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_serieTabela_spinner.setAdapter(adapterSerie);
        dropdown_serieTabela_spinner.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_serieTabela_spinner.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown de escolha do ano.
        dropdown_anoTabela_spinner = findViewById(R.id.dropdown_anoTabela_spinner);
        ArrayAdapter<String> adapterAno = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, anoList);
        adapterAno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_anoTabela_spinner.setAdapter(adapterAno);
        dropdown_anoTabela_spinner.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_anoTabela_spinner.setOnItemSelectedListener(this);
        if (!ano.equals("--")) {
            int pos = adapterAno.getPosition(ano);
            dropdown_anoTabela_spinner.setSelection(pos);
        }
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown de escolha da fase.
        dropdown_faseTabela_spinner = findViewById(R.id.dropdown_faseTabela_spinner);
        ArrayAdapter<String> adapterFase = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fase);
        adapterFase.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_faseTabela_spinner.setAdapter(adapterFase);
        dropdown_faseTabela_spinner.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_faseTabela_spinner.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)

        //Implementa o dropdown de escolha do grupo.
        dropdown_grupoTabela_spinner = findViewById(R.id.dropdown_grupoTabela_spinner);
        ArrayAdapter<String> adapterGrupo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, grupo);
        adapterGrupo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_grupoTabela_spinner.setAdapter(adapterGrupo);
        dropdown_grupoTabela_spinner.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                toqueNaTela=true;
            }
            return false;
        });
        dropdown_grupoTabela_spinner.setOnItemSelectedListener(this);
        //*********************(Fim do escopo do comentário)
    }

    //Executa algo a partir da seleção nos spinners.
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(toqueNaTela) {
            toqueNaTela = false;
            int sPosition = 0;
            int aPosition = 0;
            int fPosition = 0;
            int gPosition = 0;

            List<Object> ids = new ArrayList<>();
            ids.add(R.id.dropdown_serieTabela_spinner);
            ids.add(R.id.dropdown_anoTabela_spinner);
            ids.add(R.id.dropdown_faseTabela_spinner);
            ids.add(R.id.dropdown_grupoTabela_spinner);

            switch (ids.indexOf(parent.getId())) {
                case 0:
                    sPosition = position;
                    aPosition = dropdown_anoTabela_spinner.getSelectedItemPosition();
                    resgataFasesGrupos(lista_serie_escolhida[sPosition],lista_ano_spinner[aPosition]);
                    grupo = lista_grupos_spinner[fPosition];
                    break;
                case 1:
                    sPosition = dropdown_serieTabela_spinner.getSelectedItemPosition();
                    aPosition = position;
                    setDadosComuns("ano",lista_ano_spinner[aPosition]);
                    resgataFasesGrupos(lista_serie_escolhida[sPosition],lista_ano_spinner[aPosition]);
                    grupo = lista_grupos_spinner[fPosition];
                    break;
                case 2:
                    sPosition = dropdown_serieTabela_spinner.getSelectedItemPosition();
                    aPosition = dropdown_anoTabela_spinner.getSelectedItemPosition();
                    fPosition = position;
                    grupo = lista_grupos_spinner[position];
                    break;
                case 3:
                    sPosition = dropdown_serieTabela_spinner.getSelectedItemPosition();
                    aPosition = dropdown_anoTabela_spinner.getSelectedItemPosition();
                    fPosition = dropdown_faseTabela_spinner.getSelectedItemPosition();
                    grupo = lista_grupos_spinner[fPosition];
                    gPosition = position;
                    break;
            }
            implementaSpinner(lista_serie_spinner,lista_ano_spinner,lista_fases_spinner,grupo);
            dropdown_serieTabela_spinner.setSelection(sPosition);
            dropdown_anoTabela_spinner.setSelection(aPosition);
            dropdown_faseTabela_spinner.setSelection(fPosition);
            dropdown_grupoTabela_spinner.setSelection(gPosition);
            abrirTabela = "tabela"+lista_serie_escolhida[sPosition]+"F"+(fPosition+1)+"G"+(gPosition+1)+lista_ano_spinner[aPosition];
            serieEscolhida = lista_serie_escolhida[sPosition];
            listViewTabela.removeHeaderView(headerView);
            listViewTabela.removeFooterView(footerView);
            setAdapterArray(abrirTabela);
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {}
    //*********************(Fim do escopo do comentário)**********************

    private void includeHeaderView(){
        headerView = getLayoutInflater().inflate(R.layout.g_tabela_classificacao_list_view_header,listViewTabela,false);
        TextView penaltisHeader = headerView.findViewById(R.id.penaltisTimeHeader);
        if (abrirTabela.contains("F1")){
            penaltisHeader.setVisibility(View.GONE);
        }
        else{
            penaltisHeader.setVisibility(View.VISIBLE);
        }
        listViewTabela.addHeaderView(headerView);
    }

    private void includeFooterView(){

        footerView = getLayoutInflater().inflate(R.layout.g_tabela_classificacao_list_view_footer,listViewTabela,false);
        TextView libertadores = footerView.findViewById(R.id.corLibertadores);
        TextView acesso = footerView.findViewById(R.id.corAcesso);
        TextView sulamericana = footerView.findViewById(R.id.corSulamericana);
        TextView rebaixado = footerView.findViewById(R.id.corRebaixados);

        switch (serieEscolhida){
            case "SerieA":
                acesso.setVisibility(View.GONE);
                break;
            case "SerieB":
            case "SerieC":
                libertadores.setVisibility(View.GONE);
                sulamericana.setVisibility(View.GONE);
                break;
            case "SerieD":
                libertadores.setVisibility(View.GONE);
                sulamericana.setVisibility(View.GONE);
                rebaixado.setVisibility(View.GONE);
                break;
        }
        listViewTabela.addFooterView(footerView);
    }

    private void setAdapterArray(String tabelaEscolhida){
        includeHeaderView();
        includeFooterView();
        ArrayList<TabelaModel> tabelas = new ArrayList<>();
        List<String> lista_times = new ArrayList<>();
        SharedPreferences lerTabela = getSharedPreferences(tabelaEscolhida,MODE_PRIVATE);
        Map<String, ?> allEntries = lerTabela.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if ((entry.getKey().contains("Pos"))){
                lista_times.add(entry.getKey());
            }
        }
        //Busca de todos os atributos.
        for (int i = 1; i <= lista_times.size(); i++){
            for (String posKey : lista_times) {
                if (Objects.equals(lerTabela.getString(posKey,"--"),(String.valueOf(i)))){
                    String posicao = lerTabela.getString(posKey,"--");
                    String time = lerTabela.getString(posKey.replace("Pos","Time"),"--");
                    String pontosGanhos = lerTabela.getString(posKey.replace("Pos","Pts"),"--");
                    String jogos = lerTabela.getString(posKey.replace("Pos","J"),"--");
                    String vitorias = lerTabela.getString(posKey.replace("Pos","V"),"--");
                    String empates = lerTabela.getString(posKey.replace("Pos","E"),"--");
                    String derrotas = lerTabela.getString(posKey.replace("Pos","D"),"--");
                    String golsPro = lerTabela.getString(posKey.replace("Pos","GP"),"--");
                    String golsContra = lerTabela.getString(posKey.replace("Pos","GC"),"--");
                    String saldoGols = lerTabela.getString(posKey.replace("Pos","SG"),"--");
                    String cAmarelos = lerTabela.getString(posKey.replace("Pos","CA"),"--");
                    String cVermelhos = lerTabela.getString(posKey.replace("Pos","CV"),"--");
                    String qualificador = lerTabela.getString(posKey.replace("Pos","Qualificador"),"--");
                    String penaltis = lerTabela.getString(posKey.replace("Pos","Penaltis"),"--");

                    //Criação do objeto.
                    TabelaModel tabela = new TabelaModel(posicao, time, pontosGanhos, jogos, vitorias, empates, derrotas, golsPro, golsContra, saldoGols, cAmarelos, cVermelhos, qualificador, penaltis);
                    //Carregamento de todos os atributos.
                    tabela.setPosicao(posicao);
                    tabela.setTime(time);
                    tabela.setPontosGanhos(pontosGanhos);
                    tabela.setJogos(jogos);
                    tabela.setVitorias(vitorias);
                    tabela.setEmpates(empates);
                    tabela.setDerrotas(derrotas);
                    tabela.setGolsPro(golsPro);
                    tabela.setGolsContra(golsContra);
                    tabela.setSaldoGols(saldoGols);
                    tabela.setcAmarelos(cAmarelos);
                    tabela.setcVermelhos(cVermelhos);
                    tabela.setQualificador(qualificador);
                    tabela.setPenaltis(penaltis);
                    //Inclusão do objeto carregado na lista de objetos.
                    tabelas.add(tabela);
                    break;
                }
            }
        }
        //Transferência da lista de objetos para o adapter.
        TabelaAdapter adapter = new TabelaAdapter(this, tabelas);
        //Inicialização da listview com o adapter.
        listViewTabela.setAdapter(adapter);
    }

    private static class TabelaModel{
        String posicao,time,pontosGanhos,jogos,vitorias,empates,derrotas,golsPro,golsContra,saldoGols,cAmarelos,cVermelhos,qualificador,penaltis;

        TabelaModel(String posicao,String time,String pontosGanhos,String jogos,String vitorias,String empates,String derrotas,String golsPro,String golsContra,String saldoGols,String cAmarelos,String cVermelhos,String qualificador,String penaltis){
            this.posicao = posicao;
            this.time = time;
            this.pontosGanhos = pontosGanhos;
            this.jogos = jogos;
            this.vitorias = vitorias;
            this.empates = empates;
            this.derrotas = derrotas;
            this.golsPro = golsPro;
            this.golsContra = golsContra;
            this.saldoGols = saldoGols;
            this.cAmarelos = cAmarelos;
            this.cVermelhos = cVermelhos;
            this.qualificador = qualificador;
            this.penaltis = penaltis;
        }

        void setPosicao(String posicao) {
            this.posicao = posicao;
        }

        void setTime(String time) {
            this.time = time;
        }

        void setPontosGanhos(String pontosGanhos) {
            this.pontosGanhos = pontosGanhos;
        }

        void setJogos(String jogos) {
            this.jogos = jogos;
        }

        void setVitorias(String vitorias) {
            this.vitorias = vitorias;
        }

        void setEmpates(String empates) {
            this.empates = empates;
        }

        void setDerrotas(String derrotas) {
            this.derrotas = derrotas;
        }

        void setGolsPro(String golsPro) {
            this.golsPro = golsPro;
        }

        void setGolsContra(String golsContra) {
            this.golsContra = golsContra;
        }

        void setSaldoGols(String saldoGols) {
            this.saldoGols = saldoGols;
        }

        void setcAmarelos(String cAmarelos) {
            this.cAmarelos = cAmarelos;
        }

        void setcVermelhos(String cVermelhos) {
            this.cVermelhos = cVermelhos;
        }

        void setQualificador(String qualificador) {
            this.qualificador = qualificador;
        }

        void setPenaltis(String penaltis) {
            this.penaltis = penaltis;
        }
    }

    private class TabelaAdapter extends BaseAdapter{
        private final Context context;
        private final ArrayList<TabelaModel> tabelas;

        TabelaAdapter(Context context, ArrayList<TabelaModel> tabelas) {
            this.context = context;
            this.tabelas = tabelas;
        }

        @Override
        public int getCount() {
            return tabelas.size();
        }

        @Override
        public Object getItem(int position) {
            return tabelas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TabelaModel tabela = tabelas.get(position);
            ViewHolder holder;
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.g_tabela_classificacao_list_view_item,parent,false);
                holder = new ViewHolder();
                holder.linhaTabela = convertView.findViewById(R.id.linhaTabela);
                holder.posicaoTime = convertView.findViewById(R.id.posicaoTimeHeader);
                holder.nomeTime = convertView.findViewById(R.id.nomeTimeHeader);
                holder.pontosTime = convertView.findViewById(R.id.pontosTimeHeader);
                holder.jogosTime = convertView.findViewById(R.id.jogosTimeHeader);
                holder.vitoriasTime = convertView.findViewById(R.id.vitoriasTimeHeader);
                holder.empatesTime = convertView.findViewById(R.id.empatesTimeHeader);
                holder.derrotasTime = convertView.findViewById(R.id.derrotasTimeHeader);
                holder.golsProTime = convertView.findViewById(R.id.golsProTimeHeader);
                holder.golsContraTime = convertView.findViewById(R.id.golsContraTimeHeader);
                holder.saldoGolsTime = convertView.findViewById(R.id.saldoGolsTimeHeader);
                holder.cAmarelosTime = convertView.findViewById(R.id.cAmarelosTimeHeader);
                holder.cVermelhosTime = convertView.findViewById(R.id.cVermelhosTimeHeader);
                holder.penaltisTime = convertView.findViewById(R.id.penaltisTimeHeader);
                convertView.setTag(holder);
            }
            else{
                holder = (ViewHolder) convertView.getTag();
            }

            holder.posicaoTime.setText(tabela.posicao);
            holder.nomeTime.setText(tabela.time);
            holder.pontosTime.setText(tabela.pontosGanhos);
            holder.jogosTime.setText(tabela.jogos);
            holder.vitoriasTime.setText(tabela.vitorias);
            holder.empatesTime.setText(tabela.empates);
            holder.derrotasTime.setText(tabela.derrotas);
            holder.golsProTime.setText(tabela.golsPro);
            holder.golsContraTime.setText(tabela.golsContra);
            holder.saldoGolsTime.setText(tabela.saldoGols);
            holder.cAmarelosTime.setText(tabela.cAmarelos);
            holder.cVermelhosTime.setText(tabela.cVermelhos);
            if (abrirTabela.contains("F1")){
                holder.penaltisTime.setVisibility(View.GONE);
            }
            else {
                holder.penaltisTime.setText(tabela.penaltis);
            }

            switch (tabela.qualificador){
                case "TimeCampeao":
                    holder.linhaTabela.setBackgroundColor(0xFF2196F3);
                    break;
                case "Libertadores":
                case "Acesso":
                    holder.linhaTabela.setBackgroundColor(0xFFC8E6C9);
                    break;
                case "Sulamericana":
                    holder.linhaTabela.setBackgroundColor(0xFFFFF59D);
                    break;
                case "Rebaixado":
                    holder.linhaTabela.setBackgroundColor(0xFFFFA726);
                    break;
                case "TimeLanterna":
                    holder.linhaTabela.setBackgroundColor(0xFFFF1744);
                    break;
                default:
                    holder.linhaTabela.setBackgroundColor(0xFFFFFFFF); //Casos que não se encaixam acima.
            }

            return convertView;
        }

        class ViewHolder {
            TableRow linhaTabela;
            TextView posicaoTime,
                    nomeTime,
                    pontosTime,
                    jogosTime,
                    vitoriasTime,
                    empatesTime,
                    derrotasTime,
                    golsProTime,
                    golsContraTime,
                    saldoGolsTime,
                    cAmarelosTime,
                    cVermelhosTime,
                    penaltisTime;
        }
    }

    public void onBackPressed(){
        Intent menuInicial = new Intent(this, A_menu_inicial.class);
        startActivity(menuInicial);
        finish();
    }

    //***********************MENU***********************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_g_tabela_classificacao, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // return true so that the menu pop up is opened
        List<Object> ids = new ArrayList<>();
        ids.add(R.id.instrucoes);
        ids.add(R.id.atualizar_dados);
        ids.add(R.id.voltar);

        switch (ids.indexOf(item.getItemId())) {
            case 0:
                Intent instruction = new Intent(this,B_instrucoes.class);
                instruction.putExtra("tela","G_tabela_classificacao");
                startActivity(instruction);
                break;
            case 1:
                if (redeDisponivel) {
                    Intent atualizarDados = new Intent(this, Z03_atualizar_dados.class);
                    atualizarDados.putExtra("origem","G_tabela_classificacao");
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
