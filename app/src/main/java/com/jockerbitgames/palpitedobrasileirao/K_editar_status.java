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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import androidx.appcompat.app.AppCompatActivity;

public class K_editar_status extends AppCompatActivity {

    private String arquivoGrupo, ano;
    private ArrayList<String> lista_palpiteiros;
    private ListView listView;
    private String[] statusMarcado;
    private boolean[] checkBoxStateAdm;
    private boolean[] checkBoxStateCon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.k_editar_status);
        listView = findViewById(R.id.listViewStatus);
        lista_palpiteiros = new ArrayList<>();
        recebeDados();
        resgataArrays();
        atualizaCampos();
        setAdapterArray(arquivoGrupo);
    }

    private void recebeDados(){
        Intent editStatus = getIntent();
        arquivoGrupo = editStatus.getStringExtra("arquivoGrupo");
        ano = editStatus.getStringExtra("ano");
    }

    private void resgataArrays(){
        SharedPreferences grupoEscolhido = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
        Map<String,?> allEntries = grupoEscolhido.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains("status")){
                lista_palpiteiros.add(entry.getKey());
            }
        }
        checkBoxStateAdm = new boolean[lista_palpiteiros.size()];
        checkBoxStateCon = new boolean[lista_palpiteiros.size()];
        statusMarcado = new String[lista_palpiteiros.size()];
        for (int i = 0; i<lista_palpiteiros.size(); i++){
            statusMarcado[i] = grupoEscolhido.getString(lista_palpiteiros.get(i),"administrador");
            if (Objects.equals(statusMarcado[i], "administrador")){
                checkBoxStateAdm[i] = true;
                checkBoxStateCon[i] = false;
            }
            else{
                checkBoxStateAdm[i] = false;
                checkBoxStateCon[i] = true;
            }
        }
    }

    private void atualizaCampos(){
        SharedPreferences arquivoDoGrupo = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
        TextView nomeDoGrupo = findViewById(R.id.nome_do_grupo_status);
        nomeDoGrupo.setText(arquivoDoGrupo.getString("nomeDoGrupo","--"));
    }

    public void buttonSalvarStatus(@SuppressWarnings("unused") View view) {
        chamarVibrate();
        SharedPreferences editarGrupo = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
        SharedPreferences.Editor editarGrupoEditor = editarGrupo.edit();
        for (int i = 0; i<statusMarcado.length; i++){
            editarGrupoEditor.putString(lista_palpiteiros.get(i),statusMarcado[i]);
        }
        editarGrupoEditor.apply();
        subirGrupo();
        Toast.makeText(this, "Alterações salvas.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void subirGrupo(){
        try {
            //Passar o arquivoGrupo atualizado para o dirFiles e então subir.
            File dirFiles = getDir("files",MODE_PRIVATE);
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

    private void setAdapterArray(String arquivoGrupo){
        StatusAdapter adapter;
        ArrayList<StatusModel> contatos;
        contatos = new ArrayList<>();
        SharedPreferences grupoEscolhido = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
       //Busca de todos os atributos.
        int tamanho = lista_palpiteiros.size();
        for (String dado:lista_palpiteiros){
            CheckBox checkBoxAdm;
            CheckBox checkBoxConv;
            dado = dado.replace("status","apelido");
            String apelido = grupoEscolhido.getString(dado,"--");
            checkBoxAdm = new CheckBox(this);
            checkBoxConv = new CheckBox(this);
            //Criação do objeto.
            StatusModel contato = new StatusModel(checkBoxAdm, checkBoxConv, apelido);
            //Carregamento de todos os atributos.
            contato.setCheckBoxAdmin(checkBoxAdm);
            contato.setCheckBoxConvidado(checkBoxConv);
            contato.setApelido(apelido);
            contatos.add(contato);
            tamanho--;
            if (tamanho == 0){
                break;
            }
        }
        //Transferência da lista de objetos para o adapter.
        adapter = new StatusAdapter(this,contatos);
        //Inicialização da listview com o adapter.
        listView.setAdapter(adapter);
    }

    static class StatusModel {

        CheckBox checkBoxAdmin,checkBoxConvidado;
        String apelido;

        StatusModel(CheckBox checkBoxAdmin, CheckBox checkBoxConvidado, String apelido){
            this.checkBoxAdmin = checkBoxAdmin;
            this.checkBoxConvidado = checkBoxConvidado;
            this.apelido = apelido;
        }

        void setCheckBoxAdmin(CheckBox checkBoxAdmin) {
            this.checkBoxAdmin = checkBoxAdmin;
        }

        void setCheckBoxConvidado(CheckBox checkBoxConvidado) {
            this.checkBoxConvidado = checkBoxConvidado;
        }

        void setApelido(String apelido) {
            this.apelido = apelido;
        }
    }

    class StatusAdapter extends BaseAdapter {

        private final Context context;
        private final ArrayList<StatusModel> contatos;

        StatusAdapter(Context context, ArrayList<StatusModel> contatos) {
            this.context = context;
            this.contatos = contatos;
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
            StatusModel contato = contatos.get(position);
            final ViewHolder holder;
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.k_editar_status_list_view,parent,false);
                holder = new ViewHolder();
                holder.checkbox_admin = convertView.findViewById(R.id.checkbox_admin);
                holder.checkbox_convidado = convertView.findViewById(R.id.checkbox_convidado);
                holder.cApelido = convertView.findViewById(R.id.cApelido);
                convertView.setTag(holder);
            }
            else{
                holder = (ViewHolder) convertView.getTag();
            }

            //Comandos para recuperar o status das checkbosxes quando a imagem sumir da tela por causa do scroll.
            if (checkBoxStateAdm[position]){
                holder.checkbox_admin.setChecked(checkBoxStateAdm[position]);
            }
            else{
                holder.checkbox_admin.setChecked(contato.checkBoxAdmin.isChecked());
            }

            if (checkBoxStateCon[position]){
                holder.checkbox_convidado.setChecked(checkBoxStateCon[position]);
            }
            else{
                holder.checkbox_convidado.setChecked(contato.checkBoxConvidado.isChecked());
            }
            //***********************************Até aqui*******************************************************

            holder.cApelido.setText(contato.apelido);
            holder.checkbox_admin.setOnClickListener(v -> {
            boolean checked = ((CheckBox) v).isChecked();
                if (checked){
                    statusMarcado[position] = "administrador";
                    holder.checkbox_convidado.setChecked(false);
                    checkBoxStateAdm[position] = true;//Salvamento para recuperar o status depois do scroll.
                    checkBoxStateCon[position] = false;//Salvamento para recuperar o status depois do scroll.
                }
                else{
                    statusMarcado[position] = "convidado";
                    holder.checkbox_convidado.setChecked(true);
                    checkBoxStateAdm[position] = false;//Salvamento para recuperar o status depois do scroll.
                    checkBoxStateCon[position] = true;//Salvamento para recuperar o status depois do scroll.
                }
            });
            
            holder.checkbox_convidado.setOnClickListener(v -> {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked){
                    statusMarcado[position] = "convidado";
                    holder.checkbox_admin.setChecked(false);
                    checkBoxStateAdm[position] = false;
                    checkBoxStateCon[position] = true;
                }
                else{
                    statusMarcado[position] = "administrador";
                    holder.checkbox_admin.setChecked(true);
                    checkBoxStateAdm[position] = true;
                    checkBoxStateCon[position] = false;
                }
            });

            return convertView;
        }

        class ViewHolder {
            CheckBox checkbox_admin;
            CheckBox checkbox_convidado;
            TextView cApelido;
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
