package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import androidx.appcompat.app.AppCompatActivity;

public class I_convites_recebidos extends AppCompatActivity {

    private String ano;
    private String meuNumero;
    private String conviteFile;
    private String meusGruposBackup;
    private ListView listView;
    private StorageReference gruposRef;
    private StorageReference convitesRef;
    private StorageReference fileRef;
    private ArrayList<ConviteModel> convites;
    private File dirFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.i_convites_recebidos);
        dirFiles = getDir("files",MODE_PRIVATE);
        SharedPreferences getMeuNumero = getSharedPreferences("meusDados",MODE_PRIVATE);
        meuNumero = getMeuNumero.getString("meuNumero","--");
        listView = findViewById(R.id.listViewConvitesRecebidos);
        getDadosComuns();
        atualizaCampos();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference anoRef = storageRef.child("ano" + ano);
        gruposRef = anoRef.child("grupos"+ano);
        convitesRef = anoRef.child("convites"+ano);
        convites = new ArrayList<>();
        setAdapterArray();
    }

    private void getDadosComuns(){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        ano = getDadosComuns.getString("ano","--");
    }

    private void atualizaCampos(){
        TextView anoCampeonato = findViewById(R.id.ano_do_campeonato_convites_recebidos);
        anoCampeonato.setText(ano);
    }

    private void setAdapterArray(){
        //Baixa o arquivo de convites do firebase.
        if (meuNumero.length()>=9){
            conviteFile = "convites" + meuNumero.substring(meuNumero.length()-9);
        }else {
            conviteFile = "convites" + meuNumero;
        }
        fileRef = convitesRef.child(conviteFile + ".xml");
        final File conviteLocalFile = new File(dirFiles, conviteFile + ".xml");
        fileRef.getFile(conviteLocalFile).addOnSuccessListener(taskSnapshot -> {
            try {
                int numConvites = 0;
                FileInputStream fis = new FileInputStream(conviteLocalFile);
                Properties properties = new Properties();
                properties.loadFromXML(fis);
                fis.close();
                //Busca de todos os atributos.
                Enumeration<Object> enuKeys = properties.keys();//enuKeys recebe as chaves do xml salvo em properties.
                while (enuKeys.hasMoreElements()) {
                    String keyConvite = (String) enuKeys.nextElement();
                    if (keyConvite.contains("nomeConvidante")) {
                        numConvites++;
                        String convidante = properties.getProperty(keyConvite);
                        keyConvite = keyConvite.replace("nomeConvidante", "finalNumeroConvidante");
                        String finalNumeroConvidante = properties.getProperty(keyConvite);
                        keyConvite = keyConvite.replace("finalNumeroConvidante", "dataConvite");
                        String dataConvite = properties.getProperty(keyConvite);
                        keyConvite = keyConvite.replace("dataConvite", "nomeDoGrupo");
                        String nomeDoGrupo = properties.getProperty(keyConvite);
                        keyConvite = keyConvite.replace("nomeDoGrupo", "serieA");
                        String serieA = properties.getProperty(keyConvite);
                        keyConvite = keyConvite.replace("serieA", "serieB");
                        String serieB = properties.getProperty(keyConvite);
                        keyConvite = keyConvite.replace("serieB", "serieC");
                        String serieC = properties.getProperty(keyConvite);
                        keyConvite = keyConvite.replace("serieC", "serieD");
                        String serieD = properties.getProperty(keyConvite);
                        keyConvite = keyConvite.replace("serieD", "premioCombinado");
                        String premioCombinado = properties.getProperty(keyConvite);
                        keyConvite = keyConvite.replace("premioCombinado", "arquivoGrupo");
                        String arquivoGrupo = properties.getProperty(keyConvite);
                        keyConvite = keyConvite.replace("arquivoGrupo", "conviteRef");
                        String conviteRef = properties.getProperty(keyConvite);
                        //Criação do objeto.
                        ConviteModel convite = new ConviteModel(convidante, finalNumeroConvidante, dataConvite, nomeDoGrupo, serieA, serieB, serieC, serieD, premioCombinado, arquivoGrupo, conviteRef);
                        //Carregamento de todos os atributos.
                        convite.setConvidante(convidante);
                        convite.setFinalNumeroConvidante(finalNumeroConvidante);
                        convite.setDataConvite(dataConvite);
                        convite.setNomeDoGrupo(nomeDoGrupo);
                        convite.setSerieA(serieA);
                        convite.setSerieB(serieB);
                        convite.setSerieC(serieC);
                        convite.setSerieD(serieD);
                        convite.setPremioCombinado(premioCombinado);
                        convite.setArquivoGrupo(arquivoGrupo);
                        convite.setConviteRef(conviteRef);
                        convites.add(convite);
                    }
                }
                if (numConvites == 0) {
                    Toast.makeText(I_convites_recebidos.this, "Você não tem convites pendentes.", Toast.LENGTH_LONG).show();
                    //noinspection ResultOfMethodCallIgnored
                    conviteLocalFile.delete();
                    voltar();
                }
                else{
                    openListView();
                }
            }
            catch(IOException e){
                Toast.makeText(I_convites_recebidos.this, "Não foi possível verificar os convites.", Toast.LENGTH_LONG).show();
                //noinspection ResultOfMethodCallIgnored
                conviteLocalFile.delete();
                voltar();
            }
        }).addOnFailureListener(exception -> {
            Toast.makeText(I_convites_recebidos.this, "Parece que você ainda não recebeu convites.", Toast.LENGTH_LONG).show();
            //noinspection ResultOfMethodCallIgnored
            conviteLocalFile.delete();
            voltar();
        });
    }

    private void openListView(){
        //Transferência da lista de objetos para o adapter.
        ConviteAdapter adapter = new ConviteAdapter(this, convites);
        //Inicialização da listview com o adapter.
        listView.setAdapter(adapter);
    }

    class ConviteModel {

        String convidante,finalNumeroConvidante,dataConvite,nomeDoGrupo,serieA,serieB,serieC,serieD,premioCombinado,arquivoGrupo,conviteRef;
        int position;

        ConviteModel(String convidante, String finalNumeroConvidante, String dataConvite, String nomeDoGrupo, String serieA, String serieB, String serieC, String serieD, String premioCombinado, String arquivoGrupo, String conviteRef){
            this.convidante = convidante;
            this.finalNumeroConvidante = finalNumeroConvidante;
            this.dataConvite = dataConvite;
            this.nomeDoGrupo = nomeDoGrupo;
            this.serieA = serieA;
            this.serieB = serieB;
            this.serieC = serieC;
            this.serieD = serieD;
            this.premioCombinado = premioCombinado;
            this.arquivoGrupo = arquivoGrupo;
            this.conviteRef = conviteRef;
        }

        void exibirConvite(){
            alertDialog(convidante,finalNumeroConvidante,dataConvite,nomeDoGrupo,serieA,serieB,serieC,serieD,premioCombinado,arquivoGrupo,conviteRef,position);
        }

        void setConvidante(String convidante) {
            this.convidante = convidante;
        }

        void setFinalNumeroConvidante(String finalNumeroConvidante){this.finalNumeroConvidante = finalNumeroConvidante;}

        void setDataConvite(String dataConvite) {
            this.dataConvite = dataConvite;
        }

        void setNomeDoGrupo(String nomeDoGrupo) {
            this.nomeDoGrupo = nomeDoGrupo;
        }

        void setSerieA(String serieA) {
            this.serieA = serieA;
        }

        void setSerieB(String serieB) {
            this.serieB = serieB;
        }

        void setSerieC(String serieC) {
            this.serieC = serieC;
        }

        void setSerieD(String serieD) {
            this.serieD = serieD;
        }

        void setPremioCombinado(String premioCombinado) {
            this.premioCombinado = premioCombinado;
        }

        void setArquivoGrupo(String arquivoGrupo) {
            this.arquivoGrupo = arquivoGrupo;
        }

        void setConviteRef(String conviteRef) {
            this.conviteRef = conviteRef;
        }
    }

    class ConviteAdapter extends BaseAdapter {

        private final Context context;
        private final ArrayList<ConviteModel> convites;

        ConviteAdapter(Context context, ArrayList<ConviteModel> convites) {
            this.context = context;
            this.convites = convites;
        }

        @Override
        public int getCount() {
            return convites.size();
        }

        @Override
        public Object getItem(int position) {
            return convites.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ConviteModel convite = convites.get(position);
            final I_convites_recebidos.ConviteAdapter.ViewHolder holder;
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.i_convites_recebidos_list_view,parent,false);
                holder = new ViewHolder();
                holder.dataConvite = convertView.findViewById(R.id.dataConvite);
                holder.convidante = convertView.findViewById(R.id.convidante);
                holder.verConviteButton = convertView.findViewById(R.id.verConviteButton);
                convertView.setTag(holder);
            }
            else{
                holder = (I_convites_recebidos.ConviteAdapter.ViewHolder) convertView.getTag();
            }

            holder.dataConvite.setText(convite.dataConvite);
            holder.convidante.setText(convite.convidante);
            holder.verConviteButton.setOnClickListener(v -> {
                convite.position = position;
                convite.exibirConvite();
            });

            return convertView;
        }

        class ViewHolder {
            TextView dataConvite;
            TextView convidante;
            Button verConviteButton;
        }
    }

    private void alertDialog(String convidante, String finalNumeroConvidante, String dataConvite, final String nomeDoGrupo, String serieA, String serieB, String serieC, String serieD, String premioCombinado, final String arquivoGrupo, final String conviteRef, final int position){
        String[] listaSeries = {serieA,serieB,serieC,serieD};
        String[] listaLetras = {"A -","B -","C -","D -"};
        StringBuilder series = new StringBuilder();
        for (int i=0; i<listaSeries.length; i++) {
            if (listaSeries[i].equals("true")){
                series.append(listaLetras[i]);
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("CONVITE")
                .setMessage("\n\n"+convidante+", "+finalNumeroConvidante+", convidou você em "+dataConvite+" para participar do grupo "+nomeDoGrupo+"!\n"+
                        "Este grupo vai palpitar sobre as séries do campeonato brasileiro indicadas abaixo:\n\n"+
                        "Masculino - "+series+"\n\n"+
                        "O prêmio combinado para o palpiteiro vencedor é:\n"+premioCombinado+"\n\n"+
                        "Você aceita participar do grupo?\n\n")
                .setPositiveButton("Aceitar", (dialog, which) -> {
                    //Incluir o nome do arquivo no dadosComuns
                    SharedPreferences dadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
                    SharedPreferences.Editor dadosComunsEditor = dadosComuns.edit();
                    dadosComunsEditor.remove("E_meus_grupos");
                    if (Objects.equals(dadosComuns.getString(arquivoGrupo, "--"), nomeDoGrupo)) {
                        Toast.makeText(I_convites_recebidos.this, "Você já aceitou um convite para este grupo.", Toast.LENGTH_LONG).show();
                        apagarConvite(conviteRef, position);
                    } else {
                        Toast.makeText(I_convites_recebidos.this, "ACEITO", Toast.LENGTH_SHORT).show();
                        dadosComunsEditor.putString(arquivoGrupo, nomeDoGrupo);
                        dadosComunsEditor.apply();
                        //Baixar, atualizar e subir o arquivo do grupo com o meuNumero e status depois do último contato.
                        baixarGrupos(arquivoGrupo);
                        //Apagar convite.
                        apagarConvite(conviteRef, position);
                        backupMeusGrupos();
                    }
                })
                .setNegativeButton("Recusar", (dialog, which) -> {
                    Toast.makeText(I_convites_recebidos.this, "RECUSADO", Toast.LENGTH_SHORT).show();
                    //Apagar convite.
                    apagarConvite(conviteRef, position);
                })
                .show();
    }

    private void baixarGrupos(final String arquivoGrupo){
        fileRef = gruposRef.child(arquivoGrupo+".xml");
        final File groupLocalFile = new File(dirFiles, arquivoGrupo+".xml");
        fileRef.getFile(groupLocalFile).addOnSuccessListener(taskSnapshot -> {
            //Atualiza o arquivo do grupo com o meuNumero depois do último contato.
            try {
                Properties propertiesGroup = new Properties();
                FileInputStream fis = new FileInputStream(groupLocalFile);
                propertiesGroup.loadFromXML(fis);
                fis.close();
                Enumeration<Object> enuKeys = propertiesGroup.keys();
                int numPalpiteiros = 0;
                while (enuKeys.hasMoreElements()) {
                    String keyGroup = (String) enuKeys.nextElement();//Lê cada chave.
                    if (keyGroup.contains("meuNumero")){
                        numPalpiteiros++;
                    }
                }
                propertiesGroup.setProperty("meuNumeroContato"+(numPalpiteiros+1),meuNumero);
                propertiesGroup.setProperty("statusContato"+(numPalpiteiros+1),"convidado");
                propertiesGroup.setProperty("colocacaoContato"+(numPalpiteiros+1), String.valueOf((numPalpiteiros+1)));
                FileOutputStream fos = new FileOutputStream(groupLocalFile);
                propertiesGroup.storeToXML(fos,null);
                fos.flush();
                fos.close();
                try {
                    Runnable r = Z_runnable_subir_dados.newInstance(arquivoGrupo,"grupo",ano,dirFiles);
                    new Thread(r).start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).addOnFailureListener(exception -> {

        });
    }

    private void apagarConvite(String conviteRef, int position){
        final File conviteLocalFile = new File(dirFiles, conviteFile+".xml");
        try {
            Properties propertiesConvite = new Properties();
            FileInputStream fis = new FileInputStream(conviteLocalFile);
            propertiesConvite.loadFromXML(fis);
            fis.close();
            Enumeration<Object> enuKeys = propertiesConvite.keys();
            while (enuKeys.hasMoreElements()) {
                String keyConvite = (String) enuKeys.nextElement();//Lê cada chave.
                if (keyConvite.contains(conviteRef)){
                    propertiesConvite.remove(keyConvite);
                }
            }
            FileOutputStream fos = new FileOutputStream(conviteLocalFile);
            propertiesConvite.storeToXML(fos,null);
            fos.flush();
            fos.close();
            convites.remove(position);
            //Atualizar a tela
            openListView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void backupMeusGrupos(){
        SharedPreferences meusDados = getSharedPreferences("meusDados",MODE_PRIVATE);
        meusGruposBackup = "meusGrupos"+ano+"Backup"+meusDados.getString("meuNumero","--");
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns",MODE_PRIVATE);
        Map<String,?> allEntries = dadosComuns.getAll();
        Properties properties = new Properties();
        properties.setProperty("arquivo", "criado");
        for(Map.Entry<String,?> entry : allEntries.entrySet()){
            if (entry.getKey().contains("grupo")){
                properties.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        try {
            File getFile = new File(dirFiles, meusGruposBackup+".xml");
            FileOutputStream outputStream = new FileOutputStream(getFile);
            properties.storeToXML(outputStream, "meusGruposBackup");
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void voltar(){
        Intent meusGrupos = new Intent(this,E_meus_grupos.class);
        startActivity(meusGrupos);
        finish();
    }

    public void onBackPressed(){
        File getFile = new File(dirFiles, meusGruposBackup + ".xml");
        if (getFile.exists()) {
            try {
                Runnable r = Z_runnable_subir_dados.newInstance(meusGruposBackup,"meusGruposBackup",ano,dirFiles);
                new Thread(r).start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        final File conviteLocalFile = new File(dirFiles, conviteFile + ".xml");
        if (conviteLocalFile.exists()) {
            try {
                Runnable r = Z_runnable_subir_dados.newInstance(conviteFile,"convite",ano,dirFiles);
                new Thread(r).start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        voltar();
    }
}
