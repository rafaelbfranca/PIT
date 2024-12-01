package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import androidx.appcompat.app.AppCompatActivity;

public class I_criar_convite extends AppCompatActivity {

    private ArrayList<ContactModel> contatosSelecionados;
    private String ano;
    private String hoje;
    private String arquivoGrupo;
    private ArrayList<ContatoMarcado> contatosMarcados;
    private ArrayList<ContatoMarcado> contatosExistentes;
    private StorageReference convitesRef;
    private File dirFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.i_criar_convite);
        dirFiles = getDir("files",MODE_PRIVATE);
        contatosSelecionados = new ArrayList<>();
        contatosMarcados = new ArrayList<>();
        contatosExistentes = new ArrayList<>();
        recebeDados();
        getDadosComuns();
        contaPalpiteiros();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference anoRef = storageRef.child("ano" + ano);
        convitesRef = anoRef.child("convites"+ano);
        SearchView buscaContato = findViewById(R.id.filtroContato);
        ImageButton enviarBtn = findViewById(R.id.enviar_button);

        buscaContato.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                setAdapterArray(newText.toLowerCase());
                return false;
            }
        });

        enviarBtn.setOnClickListener(view -> {
            chamarVibrate();
            enviarConvite();
        });

        setAdapterArray(null);
    }

    private void setAdapterArray(String filtro){
        //**********Listar contatos**********
        ContactAdapter adapter;
        ListView listView = findViewById(R.id.listView);

        for (int k = contatosSelecionados.size()-1; k>=0; k--){
            if (!contatosSelecionados.get(k).checkBox.isChecked()){
                contatosSelecionados.remove(k);
            }
        }

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
        assert phones != null;
        while (phones.moveToNext())
        {
            CheckBox chkbx = new CheckBox(this);
            @SuppressLint("Range") String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            @SuppressLint("Range") String pNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            pNumber = pNumber.replace(" ","").replace("-","");
            boolean adicionar = true;
            for (int k = contatosSelecionados.size()-1; k>=0; k--) {
                if (contatosSelecionados.get(k).name.equals(name)&&contatosSelecionados.get(k).number.equals(pNumber)){
                    adicionar = false;
                    break;
                }
            }
            if (adicionar) {
                ContactModel contactModel = new ContactModel(chkbx, name, pNumber);
                contactModel.checkBox.setChecked(chkbx.isChecked());
                contactModel.setName(name);
                contactModel.setNumber(pNumber);
                if (filtro != null) {
                    if (name.toLowerCase().contains(filtro)) {
                        contatosSelecionados.add(contactModel);
                    }
                } else {
                    contatosSelecionados.add(contactModel);
                }
            }
        }
        phones.close();
        for (int i=0; i<contatosSelecionados.size(); i++){
            ContactModel contato1 = contatosSelecionados.get(i);
            String numero1 = contato1.number;
            for (int k=i+1; k<contatosSelecionados.size(); k++){
                ContactModel contato2 = contatosSelecionados.get(k);
                String numero2 = contato2.number;
                if (numero1.equals(numero2) || numero1.contains(numero2)){
                    contatosSelecionados.remove(k);
                    k--;
                }
                else if(numero2.contains(numero1)){
                    contatosSelecionados.set(k-1,contato2);
                    contatosSelecionados.remove(k);
                    k--;
                }
            }
        }
        adapter = new ContactAdapter(this,contatosSelecionados);
        listView.setAdapter(adapter);
    }

    private void recebeDados(){
        Intent criarConvite = getIntent();
        arquivoGrupo = criarConvite.getStringExtra("arquivoGrupo");
    }

    private void getDadosComuns(){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        ano = getDadosComuns.getString("ano","--");
        hoje = getDadosComuns.getString("atualizadoEm","--");
    }

    private void contaPalpiteiros(){
        SharedPreferences arquivo = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
        Map<String,?> allEntries = arquivo.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if(entry.getKey().contains("colocacao")){
                String nomeContato = arquivo.getString(entry.getKey().replace("colocacao","meuNome"),"--");
                String numeroContato = arquivo.getString(entry.getKey().replace("colocacao","meuNumero"),"--");
                ContatoMarcado contatoExistente = new ContatoMarcado(nomeContato, numeroContato);
                contatosExistentes.add(contatoExistente);
            }
        }
    }

    //**********Aviso marcar contatos**********
    private void avisoMarcarContatos(){
        Toast.makeText(this, "Selecione os participantes.", Toast.LENGTH_SHORT).show();
    }

    //**********Enviar convites para o Firebase**********
    private void enviarConvite() {
        if (contatosMarcados.size() > 0){
            filtrarConvites();
            verificarConvites();
            registrarConvite();
            enviarMensagem();
        }
        else{
            avisoMarcarContatos();
        }
    }

    private void filtrarConvites(){
        for (ContatoMarcado valor:contatosExistentes) {
            int repetido = -1;
            String existenteNome = valor.nome;
            String existenteNumero = valor.numero;
            for (int i = 0; i < contatosMarcados.size(); i++) {
                String marcadoNome = contatosMarcados.get(i).nome;
                String marcadoNumero = contatosMarcados.get(i).numero;
                if ((marcadoNome.equals(existenteNome))||(marcadoNumero.equals(existenteNumero))) {
                    repetido = i;
                    break;
                }
            }
            if (repetido != -1){
                contatosMarcados.remove(contatosMarcados.get(repetido));
            }
        }
    }

    private void verificarConvites(){
        if (contatosMarcados.size()>0) {
            for (ContatoMarcado valor : contatosMarcados) {

                String numeroContato;
                if (valor.numero.length()>=9){
                    numeroContato = valor.numero.substring(valor.numero.length()-9);
                }else {
                    numeroContato = valor.numero;
                }

                String conviteFile = "convites" + numeroContato;
                int position = contatosMarcados.indexOf(valor);
                getConviteFile(conviteFile,position);
            }
        }
    }

    private void getConviteFile(final String file, final int position){
        SharedPreferences arqvGrupo = getSharedPreferences(arquivoGrupo, MODE_PRIVATE);
        final String idGrupo = arqvGrupo.getString("idGrupo","--");
        final String nomeDoGrupo = arqvGrupo.getString("nomeDoGrupo", "--");
        final String serieA = arqvGrupo.getString("checkboxSerieA", "--");
        final String serieB = arqvGrupo.getString("checkboxSerieB", "--");
        final String serieC = arqvGrupo.getString("checkboxSerieC", "--");
        final String serieD = arqvGrupo.getString("checkboxSerieD", "--");
        final String premioCombinado = arqvGrupo.getString("premioCombinado", "--");
        SharedPreferences meusDados = getSharedPreferences("meusDados",MODE_PRIVATE);
        String meuPais = meusDados.getString("meuPais","--");
        String meuDDD = meusDados.getString("meuDDD","--");
        final String meuNumero = meusDados.getString("meuNumero","--");
        StringBuilder finalNumero = new StringBuilder();
        if (meuNumero.length() > 4) {
            finalNumero.append(meuPais).append("(").append(meuDDD).append(")").append("*****-");
            for (int i = meuNumero.length() - 4; i < meuNumero.length(); i++) {
                finalNumero.append(meuNumero.charAt(i));
            }
        }
        else{
            finalNumero.append("--");
        }
        final String finalMeuNumero = String.valueOf(finalNumero);
        final String meuNome = meusDados.getString("palpiteiro","--");
        StorageReference fileRef = convitesRef.child(file + ".xml");
        final File conviteLocalFile = new File(dirFiles, file + ".xml");
        fileRef.getFile(conviteLocalFile).addOnSuccessListener(taskSnapshot -> {
            //Incluir convite e subir.
            try {
                FileInputStream fis = new FileInputStream(conviteLocalFile);
                Properties properties = new Properties();
                properties.loadFromXML(fis);
                fis.close();
                Enumeration<Object> enuKeys = properties.keys();//enuKeys recebe as chaves do xml salvo em properties.
                boolean enviarConvite = true;
                while (enuKeys.hasMoreElements()) {
                    String key = (String) enuKeys.nextElement();//Lê cada chave.
                    if (key.contains(idGrupo)) {
                        enviarConvite = false;
                        contatosMarcados.remove(position);
                        break;
                    }
                }
                if (enviarConvite){
                    properties.setProperty(idGrupo + meuNumero + "nomeConvidante", meuNome);
                    properties.setProperty(idGrupo + meuNumero + "finalNumeroConvidante", finalMeuNumero);
                    properties.setProperty(idGrupo + meuNumero + "dataConvite", hoje);
                    properties.setProperty(idGrupo + meuNumero + "nomeDoGrupo", nomeDoGrupo);
                    properties.setProperty(idGrupo + meuNumero + "serieA", serieA);
                    properties.setProperty(idGrupo + meuNumero + "serieB", serieB);
                    properties.setProperty(idGrupo + meuNumero + "serieC", serieC);
                    properties.setProperty(idGrupo + meuNumero + "serieD", serieD);
                    properties.setProperty(idGrupo + meuNumero + "premioCombinado", premioCombinado);
                    properties.setProperty(idGrupo + meuNumero + "arquivoGrupo", arquivoGrupo);
                    properties.setProperty(idGrupo + meuNumero + "conviteRef", idGrupo + meuNumero);
                    FileOutputStream fos = new FileOutputStream(conviteLocalFile);
                    properties.storeToXML(fos, "Convites");
                    fos.flush();
                    fos.close();
                    try {
                        Runnable r = Z_runnable_subir_dados.newInstance(file, "convite", ano,dirFiles);
                        new Thread(r).start();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                else{
                    //noinspection ResultOfMethodCallIgnored
                    conviteLocalFile.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
                //noinspection ResultOfMethodCallIgnored
                conviteLocalFile.delete();
                Toast.makeText(I_criar_convite.this, "Falha ao preparar o convite para"+ contatosMarcados.get(position).nome +". Tente novamente.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(exception -> {
        //Criar convite e subir.
        try {
            FileOutputStream fos = new FileOutputStream(conviteLocalFile);
            Properties properties = new Properties();
            properties.setProperty(idGrupo + meuNumero + "nomeConvidante", meuNome);
            properties.setProperty(idGrupo + meuNumero + "finalNumeroConvidante", finalMeuNumero);
            properties.setProperty(idGrupo + meuNumero + "dataConvite", hoje);
            properties.setProperty(idGrupo + meuNumero + "nomeDoGrupo", nomeDoGrupo);
            properties.setProperty(idGrupo + meuNumero + "serieA", serieA);
            properties.setProperty(idGrupo + meuNumero + "serieB", serieB);
            properties.setProperty(idGrupo + meuNumero + "serieC", serieC);
            properties.setProperty(idGrupo + meuNumero + "serieD", serieD);
            properties.setProperty(idGrupo + meuNumero + "premioCombinado", premioCombinado);
            properties.setProperty(idGrupo + meuNumero + "arquivoGrupo", arquivoGrupo);
            properties.setProperty(idGrupo + meuNumero + "conviteRef", idGrupo + meuNumero);
            properties.storeToXML(fos, null);
            fos.flush();
            fos.close();
            try {
                Runnable r = Z_runnable_subir_dados.newInstance(file,"convite",ano,dirFiles);
                new Thread(r).start();
            } catch (Exception e) {
                Toast.makeText(I_criar_convite.this, "Falha ao subir o convite para"+ contatosMarcados.get(position).nome +". Tente novamente.", Toast.LENGTH_LONG).show();
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //noinspection ResultOfMethodCallIgnored
            conviteLocalFile.delete();
            Toast.makeText(I_criar_convite.this, "Falha ao preparar o convite para"+ contatosMarcados.get(position).nome +". Tente novamente.", Toast.LENGTH_LONG).show();
        }
        });
    }

    private void registrarConvite(){
        if (contatosMarcados.size()>0) {
            SharedPreferences meusDados = getSharedPreferences("meusDados", MODE_PRIVATE);
            final String meuNumero = meusDados.getString("meuNumero", "--");
            SharedPreferences arqvGrupo = getSharedPreferences(arquivoGrupo, MODE_PRIVATE);
            final String idGrupo = arqvGrupo.getString("idGrupo", "--");
            final String nomeDoGrupo = arqvGrupo.getString("nomeDoGrupo", "--");
            SharedPreferences convitesEnviados = getSharedPreferences("convitesEnviados", MODE_PRIVATE);
            SharedPreferences.Editor convitesEnviadosEditor = convitesEnviados.edit();
            for (ContatoMarcado valor : contatosMarcados) {
                String nomeConvidado = valor.nome;
                String numeroConvidado = valor.numero;
                convitesEnviadosEditor.putString(idGrupo + meuNumero + numeroConvidado + "nomeConvidado", nomeConvidado);
                convitesEnviadosEditor.putString(idGrupo + meuNumero + numeroConvidado + "nomeDoGrupo", nomeDoGrupo);
                convitesEnviadosEditor.putString(idGrupo + meuNumero + numeroConvidado + "dataConvite", hoje);
            }
            convitesEnviadosEditor.apply();
        }
    }

    private void enviarMensagem(){
        if (contatosMarcados.size() > 0){
            ArrayList<String> listaContatosMensagens = new ArrayList<>();
            for (ContatoMarcado valor:contatosMarcados) {
                listaContatosMensagens.add(valor.numero);
            }
            Intent enviarMensagens = new Intent(this,I_enviar_mensagens.class);
            enviarMensagens.putStringArrayListExtra("listaContatosMensagens", listaContatosMensagens);
            startActivity(enviarMensagens);
        }
        finish();
    }

    static class ContatoMarcado{
        final String nome;
        final String numero;

        ContatoMarcado(String nome, String numero){
            this.nome = nome;
            this.numero = numero;
        }
    }

    //**********Mostrar lista de contatos**********
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
            final ContactModel contato = contatos.get(position);
            final ViewHolder holder;
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.i_criar_convite_list_view,parent,false);
                holder = new ViewHolder();
                holder.checkBox = convertView.findViewById(R.id.checkbox);
                holder.cName = convertView.findViewById(R.id.cName);
                holder.cNumber = convertView.findViewById(R.id.cNumber);
                convertView.setTag(holder);
            }
            else{
                holder = (ViewHolder) convertView.getTag();
            }

            if (checkBoxState[position]){
                holder.checkBox.setChecked(checkBoxState[position]);
                contato.checkBox.setChecked(checkBoxState[position]);
            }
            else{
                holder.checkBox.setChecked(contato.checkBox.isChecked());
                contato.checkBox.setChecked(contato.checkBox.isChecked());
            }

            holder.cName.setText(contato.name);
            holder.cNumber.setText(contato.number);
            holder.checkBox.setOnClickListener(v -> {
                boolean checked = ((CheckBox) v).isChecked();
                String numeroContato = ((String) holder.cNumber.getText()).replace("+","").replace(" ","").replace("-","");
                StringBuilder num = new StringBuilder(numeroContato);
                //Retira sequências de zeros no início do número.
                while ((num.charAt(0)<49)||((num.charAt(0)>57))){ // 49 ao 57
                    num.delete(0,1);
                }
                numeroContato = String.valueOf(num);
                String nomeContato = (String) holder.cName.getText();
                ContatoMarcado contatoMarcado = new ContatoMarcado(nomeContato, numeroContato);
                if (checked){
                    checkBoxState[position] = true;
                    contato.checkBox.setChecked(checkBoxState[position]);
                    contatosMarcados.add(contatoMarcado);
                    contatosSelecionados.add(contato);
                    setAdapterArray(null);
                }
                else {
                    checkBoxState[position] = false;
                    contato.checkBox.setChecked(checkBoxState[position]);
                    int i = contatosMarcados.size();
                    for (i -= 1; i >= 0; i--) {
                        if (contatoMarcado.nome.equals(contatosMarcados.get(i).nome)) {
                            if (contatoMarcado.numero.equals(contatosMarcados.get(i).numero)) {
                                contatosMarcados.remove(i);
                                break;
                            }
                        }
                    }
                    for (int k = contatosSelecionados.size() - 1; k >= 0; k--) {
                        if (contatosSelecionados.get(k).name.equals(contato.name) && contatosSelecionados.get(k).number.equals(contato.number)) {
                            contatosSelecionados.remove(k);
                            setAdapterArray(null);
                            break;
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

    //**********MENU**********
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_i_criar_convite, menu);
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
                instruction.putExtra("tela","I_criar_convite");
                startActivity(instruction);
                break;
            case 1:
                finish();
        }
        return true;
    }
}
