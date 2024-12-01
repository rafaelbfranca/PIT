package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

public class L_excluir_palpiteiros extends AppCompatActivity {

    private String ano;
    private String arquivoGrupo;
    private String nomeGrupo;
    private List<String> lista_palpiteiros;
    private ListView listView;
    private ArrayList<ContatoMarcado> contatosMarcados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.l_excluir_palpiteiros);
        listView = findViewById(R.id.listViewExcluir);
        lista_palpiteiros = new ArrayList<>();
        contatosMarcados = new ArrayList<>();
        recebeDados();
        atualizaCampos();
        setAdapterArray(arquivoGrupo);
    }

    private void recebeDados(){
        Intent excluirPalpiteiro = getIntent();
        ano = excluirPalpiteiro.getStringExtra("ano");
        arquivoGrupo = excluirPalpiteiro.getStringExtra("arquivoGrupo");
        SharedPreferences grupo = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
        nomeGrupo = grupo.getString("nomeDoGrupo","--");
    }

    private void atualizaCampos(){
        TextView anoCampeonato = findViewById(R.id.ano_do_campeonato);
        anoCampeonato.setText(ano);
        TextView nomeDoGrupo = findViewById(R.id.nome_do_grupo);
        nomeDoGrupo.setText(nomeGrupo);
    }

    private void setAdapterArray(String arquivoGrupo){
        ContactAdapter adapter;
        ArrayList<ContactModel> contatos;
        contatos = new ArrayList<>();

        SharedPreferences grupoEscolhido = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
        Map<String,?> allEntries = grupoEscolhido.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains("apelido")){
                lista_palpiteiros.add(entry.getKey());
            }
        }
        //Busca de todos os atributos.
        int tamanho = lista_palpiteiros.size();
        for (String dado:lista_palpiteiros){
            CheckBox checkBox = new CheckBox(this);
            checkBox.setChecked(false);
            String apelido = grupoEscolhido.getString(dado,"--");
            dado = dado.replace("apelido","meuNumero");
            String numero = grupoEscolhido.getString(dado,"--");

            //Criação do objeto.
            ContactModel contato = new ContactModel(checkBox, apelido, numero);
            //Carregamento de todos os atributos.
            contato.checkBox.setChecked(checkBox.isChecked());
            contato.setName(apelido);
            contato.setNumber(numero);
            contatos.add(contato);
            tamanho--;
            if (tamanho == 0){
                break;
            }
        }
        //Transferência da lista de objetos para o adapter.
        adapter = new ContactAdapter(this,contatos);
        //Inicialização da listview com o adapter.
        listView.setAdapter(adapter);
    }

    public void buttonExcluirParticipante(@SuppressWarnings("unused") View view){
        chamarVibrate();
        if (contatosMarcados.size()>0){
            SharedPreferences excluirParticipante = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
            SharedPreferences.Editor excluirParticipanteEditor = excluirParticipante.edit();
            Map<String,?> allEntries = excluirParticipante.getAll();
            for (int i = 0; i<contatosMarcados.size(); i++) {
                String numero = contatosMarcados.get(i).numero;
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    if (entry.getValue().equals(numero)){
                        String chave = entry.getKey();
                        excluirParticipanteEditor.remove(chave);
                        chave = chave.replace("meuNumero","meuNome");
                        excluirParticipanteEditor.remove(chave);
                        chave = chave.replace("meuNome","apelido");
                        excluirParticipanteEditor.remove(chave);
                        chave = chave.replace("apelido","timeDoCoracao");
                        excluirParticipanteEditor.remove(chave);
                        chave = chave.replace("timeDoCoracao","colocacao");
                        excluirParticipanteEditor.remove(chave);
                        chave = chave.replace("colocacao","ptsTotal");
                        excluirParticipanteEditor.remove(chave);
                        chave = chave.replace("ptsTotal","totalPontosSerieA");
                        excluirParticipanteEditor.remove(chave);
                        chave = chave.replace("totalPontosSerieA","totalPontosSerieB");
                        excluirParticipanteEditor.remove(chave);
                        chave = chave.replace("totalPontosSerieB","totalPontosSerieC");
                        excluirParticipanteEditor.remove(chave);
                        chave = chave.replace("totalPontosSerieC","totalPontosSerieD");
                        excluirParticipanteEditor.remove(chave);
                        chave = chave.replace("totalPontosSerieD","status");
                        excluirParticipanteEditor.remove(chave);
                        chave = chave.replace("status","atualizadoEm");
                        excluirParticipanteEditor.remove(chave);
                    }
                }
                Toast.makeText(this, "Palpiteiro(a) "+contatosMarcados.get(i).apelido+" excluído(a).", Toast.LENGTH_SHORT).show();
            }
            excluirParticipanteEditor.apply();
            atualizarColocacao();
        }
        else {
            Toast.makeText(this, "Nenhum(a) palpiteiro(a) foi excluído(a).", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void atualizarColocacao(){
        Intent atualizar_Colocacao = new Intent(this, Z_atualizar_colocacao.class);
        atualizar_Colocacao.putExtra("ano",ano);
        atualizar_Colocacao.putExtra("baixar_grupos","não");
        atualizar_Colocacao.putExtra("arquivoGrupo",arquivoGrupo);
        atualizar_Colocacao.putExtra("nomeDoGrupo",nomeGrupo);
        startActivity(atualizar_Colocacao);
    }

    static class ContatoMarcado{
        final String nome;
        final String numero;
        final String apelido;

        ContatoMarcado(String nome, String numero, String apelido){
            this.nome = nome;
            this.numero = numero;
            this.apelido = apelido;
        }
    }

    static class ContactModel {

        final CheckBox checkBox;
        String name, number;

        ContactModel(CheckBox checkBox, String name, String number){
            this.checkBox = checkBox;
            this.name = name;
            this.number = number;
        }

        void setName(String name) {
            this.name = name;
        }

        void setNumber(String number) {
            this.number = number;
        }
    }

    class ContactAdapter extends BaseAdapter {

        private final Context context;
        private final ArrayList<ContactModel> contatos;
        private final boolean[] checkBoxState;

        ContactAdapter(Context context, ArrayList<ContactModel> contatos) {
            this.context = context;
            this.contatos = contatos;
            checkBoxState = new boolean[contatos.size()];
        }

        @Override
        public int getCount() {
            return contatos.size();
        }

        @Override
        public Object getItem(int position) {
            return contatos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ContactModel contato = contatos.get(position);
            final L_excluir_palpiteiros.ContactAdapter.ViewHolder holder;
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.l_excluir_palpiteiros_list_view,parent,false);
                holder = new ViewHolder();
                holder.checkBox = convertView.findViewById(R.id.checkbox);
                holder.cName = convertView.findViewById(R.id.cName);
                holder.cNumber = convertView.findViewById(R.id.cNumber);
                convertView.setTag(holder);
            }
            else{
                holder = (L_excluir_palpiteiros.ContactAdapter.ViewHolder) convertView.getTag();
            }

            if (checkBoxState[position]){
                holder.checkBox.setChecked(checkBoxState[position]);
            }
            else{
                holder.checkBox.setChecked(contato.checkBox.isChecked());
            }

            holder.cName.setText(contato.name);
            holder.cNumber.setText(contato.number);
            holder.checkBox.setOnClickListener(v -> {
                boolean checked = ((CheckBox) v).isChecked();
                String numeroContato = ((String) holder.cNumber.getText()).replace("+","").replace(" ","").replace("-","");
                StringBuilder num = new StringBuilder(numeroContato);
                for(int i=0; i<3; i++){//Retirar 0 das três primeiras posições do meuNumero.
                    if(num.subSequence(i,i+1).equals("0")){
                        num.delete(i,i+1);
                    }
                }
                numeroContato = String.valueOf(num);
                String nomeContato = (String) holder.cName.getText();
                ContatoMarcado contatoMarcado = new ContatoMarcado(nomeContato, numeroContato, nomeContato);
                if (checked){
                    checkBoxState[position] = true;
                    contatosMarcados.add(contatoMarcado);
                }
                else{
                    checkBoxState[position] = false;
                    int i = contatosMarcados.size();
                    for (i-=1; i>=0; i--) {
                        if (contatoMarcado.nome.equals(contatosMarcados.get(i).nome)){
                            if (contatoMarcado.numero.equals(contatosMarcados.get(i).numero)) {
                                contatosMarcados.remove(i);
                                break;
                            }
                        }
                    }
                }
            });

            return convertView;
        }

        class ViewHolder {
            CheckBox checkBox;
            TextView cName;
            TextView cNumber;
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

}
