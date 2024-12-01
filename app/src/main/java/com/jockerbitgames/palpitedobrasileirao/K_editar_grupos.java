package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class K_editar_grupos extends AppCompatActivity {
    private ArrayList<String> listaSeries;
    private String ano;
    private String arquivoGrupo;
    private TextView nomeDoGrupo;
    private EditText premioCombinado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.k_editar_grupos);
        recebeDados();
        resgataArrays();
        ano = getDadosComuns();
        nomeDoGrupo = findViewById(R.id.nome_do_grupo);
        premioCombinado = findViewById(R.id.premio_combinado);
        atualizaCampos();
    }

    private void recebeDados(){
        Intent editGrupo = getIntent();
        arquivoGrupo = editGrupo.getStringExtra("arquivoGrupo");
    }

    private void resgataArrays() {
        listaSeries = new ArrayList<>();
        SharedPreferences referencias = getSharedPreferences("referencias",MODE_PRIVATE);
        Map<String,?> referenciasKeys = referencias.getAll();
        for(Map.Entry<String,?> entry : referenciasKeys.entrySet()){
            if (entry.getValue().equals("series_array2")) {
                listaSeries.add(String.valueOf(entry.getKey()));
            }
        }
    }

    private String getDadosComuns(){
        SharedPreferences getDadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        return getDadosComuns.getString("ano","--");
    }

    private void atualizaCampos(){
        TextView anoDoCampeonato = findViewById(R.id.ano_do_campeonato);
        anoDoCampeonato.setText(ano);
        SharedPreferences arquivoDoGrupo = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
        nomeDoGrupo.setText(arquivoDoGrupo.getString("nomeDoGrupo","--"));
        CheckBox serieA = findViewById(R.id.checkboxSerieA);
        serieA.setChecked(Boolean.parseBoolean(arquivoDoGrupo.getString("checkboxSerieA","--")));
        CheckBox serieB = findViewById(R.id.checkboxSerieB);
        serieB.setChecked(Boolean.parseBoolean(arquivoDoGrupo.getString("checkboxSerieB","--")));
        CheckBox serieC = findViewById(R.id.checkboxSerieC);
        serieC.setChecked(Boolean.parseBoolean(arquivoDoGrupo.getString("checkboxSerieC","--")));
        CheckBox serieD = findViewById(R.id.checkboxSerieD);
        serieD.setChecked(Boolean.parseBoolean(arquivoDoGrupo.getString("checkboxSerieD","--")));
        premioCombinado.setText(arquivoDoGrupo.getString("premioCombinado","--"));
    }

    public void buttonSalvarEditados(@SuppressWarnings("unused") View view) {
        chamarVibrate();
        SharedPreferences editarGrupo = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
        SharedPreferences.Editor editarGrupoEditor = editarGrupo.edit();
        for (String serie : listaSeries) {
            String chave = "checkbox" + serie;
            @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(chave, "id", getPackageName());
            CheckBox chkBox = findViewById(resId);
            if (chkBox.isChecked()) {
                editarGrupoEditor.putString(chave, "true");
            } else {
                editarGrupoEditor.putString(chave, "false");
            }
        }
        editarGrupoEditor.putString("premioCombinado", String.valueOf(premioCombinado.getText()));
        editarGrupoEditor.apply();
        Toast.makeText(this, "Alterações salvas.", Toast.LENGTH_SHORT).show();
    }

    public void buttonIncluirPalpiteiro(@SuppressWarnings("unused") View view) {
        chamarVibrate();
        new AlertDialog.Builder(this)
            .setTitle("ATENÇÃO")
            .setMessage("\n\n" + "Se você editou alguma informação nesta tela, não esqueça de salvar!" + "\n\n" + "Precisa salvar?!")
            .setPositiveButton("Sim", null)
            .setNegativeButton("Não", (dialog, which) -> {
                criarConvite();
                finish();
            })
            .show();
    }

    public void buttonExcluirPalpiteiro(@SuppressWarnings("unused") View view) {
        chamarVibrate();
        new AlertDialog.Builder(this)
            .setTitle("ATENÇÃO")
            .setMessage("\n\n" + "Se você editou alguma informação nesta tela, não esqueça de salvar!" + "\n\n" + "Precisa salvar?!")
            .setPositiveButton("Sim", null)
            .setNegativeButton("Não", (dialog, which) -> {
                excluirPalpiteiro();
                finish();
            })
            .show();
    }

    private void criarConvite(){
        Intent criarConvite = new Intent(this, I_criar_convite.class);
        criarConvite.putExtra("arquivoGrupo", arquivoGrupo);
        startActivity(criarConvite);
    }

    private void excluirPalpiteiro(){
        Intent excluirPalpiteiro = new Intent(this, L_excluir_palpiteiros.class);
        excluirPalpiteiro.putExtra("arquivoGrupo", arquivoGrupo);
        excluirPalpiteiro.putExtra("ano", ano);
        startActivity(excluirPalpiteiro);
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

    //**********Menu**********
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_k_editar_grupos, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // return true so that the menu pop up is opened
        List<Object> ids = new ArrayList<>();
        ids.add(R.id.instrucoes);
        ids.add(R.id.status);
        ids.add(R.id.voltar);

        switch (ids.indexOf(item.getItemId())) {
            case 0:
                Intent instruction = new Intent(this,B_instrucoes.class);
                instruction.putExtra("tela","K_editar_grupos");
                startActivity(instruction);
                break;
            case 1:
                Intent editStatus = new Intent(this,K_editar_status.class);
                editStatus.putExtra("arquivoGrupo",arquivoGrupo);
                editStatus.putExtra("ano",ano);
                startActivity(editStatus);
                finish();
                break;
            case 2:
                Intent editExclud = new Intent(this,J_edit_exclud_grupos.class);
                startActivity(editExclud);
                finish();
        }
        return true;
    }
}
