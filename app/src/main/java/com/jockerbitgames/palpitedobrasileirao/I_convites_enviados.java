package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;

public class I_convites_enviados extends AppCompatActivity {

    private ListView listView;
    private ArrayList<ConviteModel> convites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.i_convites_enviados);
        convites = new ArrayList<>();
        listView = findViewById(R.id.listViewConvitesEnviados);
        setAdapterArray();
    }

    private void setAdapterArray(){
        //Busca de todos os atributos.
        SharedPreferences convitesEnviados = getSharedPreferences("convitesEnviados",MODE_PRIVATE);
        Map<String, ?> allEntries = convitesEnviados.getAll();
        for (Map.Entry<String, ?> entry:allEntries.entrySet()) {
            if (entry.getKey().contains("nomeConvidado")) {
                String conviteKey = entry.getKey();
                String nomeConvidado = convitesEnviados.getString(conviteKey, "--");
                String dataConvite = convitesEnviados.getString(conviteKey.replace("nomeConvidado", "dataConvite"), "--");
                String nomeDoGrupo = convitesEnviados.getString(conviteKey.replace("nomeConvidado", "nomeDoGrupo"), "--");
                String idConvite = conviteKey.replace("nomeConvidado", "");
                //Criação do objeto.
                ConviteModel convite = new ConviteModel(nomeConvidado,dataConvite,nomeDoGrupo,idConvite);
                //Carregamento de todos os atributos.
                convite.setNomeConvidado(nomeConvidado);
                convite.setDataConvite(dataConvite);
                convite.setNomeDoGrupo(nomeDoGrupo);
                convite.setIdConvite(idConvite);
                convites.add(convite);
            }
            openListView();
        }
    }

    private void openListView(){
        //Transferência da lista de objetos para o adapter.
        ConviteAdapter adapter = new ConviteAdapter(this, convites);
        //Inicialização da listview com o adapter.
        listView.setAdapter(adapter);
    }

    private void alertDialog(String nomeConvidado, String dataConvite, final String nomeDoGrupo, final int position){
        new AlertDialog.Builder(this)
                .setTitle("CONVITE ENVIADO")
                .setMessage("\n\n"+"Você convidou "+nomeConvidado+" para participar do grupo "+nomeDoGrupo+" em "+dataConvite+".\n\n"+
                        "Apagar este registro NÃO cancela o convite enviado!\n"+
                        "Manter este registro também não prolonga o tempo de vida do convite.\n"+
                        "Ele serve apenas para o seu controle, para ajudá-lo a não enviar convites repetidos sem necessidade.\n"+
                        "Se for necessário, você poderá editar o grupo e reenviar um convite independente deste registro ter sido apagado ou não.\n\n"+
                        "Você quer apagar o registro?\n\n")
                .setPositiveButton("SIM", (dialog, which) -> {
                    SharedPreferences convitesEnviados = getSharedPreferences("convitesEnviados",MODE_PRIVATE);
                    SharedPreferences.Editor convitesEnviadosEditor = convitesEnviados.edit();
                    convitesEnviadosEditor.remove(convites.get(position).idConvite + "nomeConvidado");
                    convitesEnviadosEditor.remove(convites.get(position).idConvite + "dataConvite");
                    convitesEnviadosEditor.remove(convites.get(position).idConvite + "nomeDoGrupo");
                    convitesEnviadosEditor.apply();
                    convites.remove(position);
                    openListView();
                })
                .setNegativeButton("NÃO", null)
                .show();
    }

    class ConviteModel {

        String nomeConvidado,dataConvite,nomeDoGrupo,idConvite;
        int position;

        ConviteModel(String nomeConvidado, String dataConvite, String nomeDoGrupo, String idConvite){
            this.nomeConvidado = nomeConvidado;
            this.dataConvite = dataConvite;
            this.nomeDoGrupo = nomeDoGrupo;
            this.idConvite = idConvite;
        }

        void apagarConvite(){
            alertDialog(nomeConvidado,dataConvite,nomeDoGrupo,position);
        }

        void setNomeConvidado(String nomeConvidado) {
            this.nomeConvidado = nomeConvidado;
        }

        void setDataConvite(String dataConvite) {
            this.dataConvite = dataConvite;
        }

        void setNomeDoGrupo(String nomeDoGrupo) {
            this.nomeDoGrupo = nomeDoGrupo;
        }

        void setIdConvite(String idConvite) {
            this.idConvite = idConvite;
        }
    }

    class ConviteAdapter extends BaseAdapter {

        private final Context context;
        private final ArrayList<ConviteModel> convitesAdapter;

        ConviteAdapter(Context context, ArrayList<ConviteModel> convites) {
            this.context = context;
            this.convitesAdapter = convites;
        }

        @Override
        public int getCount() {
            return convitesAdapter.size();
        }

        @Override
        public Object getItem(int position) {
            return convitesAdapter.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ConviteModel convite = convitesAdapter.get(position);
            final I_convites_enviados.ConviteAdapter.ViewHolder holder;
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.i_convites_enviados_list_view,parent,false);
                holder = new ViewHolder();
                holder.destinatarioConviteEnviado = convertView.findViewById(R.id.destinatarioConviteEnviado);
                holder.dataConviteEnviado = convertView.findViewById(R.id.dataConviteEnviado);
                holder.grupoConviteEnviado = convertView.findViewById(R.id.grupoConviteEnviado);
                holder.buttonApagarConviteEnviado = convertView.findViewById(R.id.buttonApagarConviteEnviado);
                convertView.setTag(holder);
            }
            else{
                holder = (I_convites_enviados.ConviteAdapter.ViewHolder) convertView.getTag();
            }

            String nome = "Para: "+convite.nomeConvidado;
            String data = "Data: "+convite.dataConvite;
            String grupo = "Grupo: "+convite.nomeDoGrupo;
            holder.destinatarioConviteEnviado.setText(nome);
            holder.dataConviteEnviado.setText(data);
            holder.grupoConviteEnviado.setText(grupo);
            holder.buttonApagarConviteEnviado.setOnClickListener(v -> {
                convite.position = position;
                convite.apagarConvite();
            });

            return convertView;
        }

        class ViewHolder {
            TextView destinatarioConviteEnviado;
            TextView dataConviteEnviado;
            TextView grupoConviteEnviado;
            Button buttonApagarConviteEnviado;
        }
    }
}
