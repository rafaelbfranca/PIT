package com.jockerbitgames.palpitedobrasileirao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class F_exibir_outras_infos extends AppCompatActivity {

    private String arquivoGrupo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.f_exibir_outras_infos);
        recebeDados();
        atualizaCampos();
    }

    private void recebeDados(){
        Intent showOutrasInfos = getIntent();
        arquivoGrupo = showOutrasInfos.getStringExtra("arquivoGrupo");
    }

    private void atualizaCampos(){
        SharedPreferences arquivoDoGrupo = getSharedPreferences(arquivoGrupo,MODE_PRIVATE);
        CheckBox serieA = findViewById(R.id.checkboxSerieA);
        serieA.setChecked(Boolean.parseBoolean(arquivoDoGrupo.getString("checkboxSerieA","--")));
        CheckBox serieB = findViewById(R.id.checkboxSerieB);
        serieB.setChecked(Boolean.parseBoolean(arquivoDoGrupo.getString("checkboxSerieB","--")));
        CheckBox serieC = findViewById(R.id.checkboxSerieC);
        serieC.setChecked(Boolean.parseBoolean(arquivoDoGrupo.getString("checkboxSerieC","--")));
        CheckBox serieD = findViewById(R.id.checkboxSerieD);
        serieD.setChecked(Boolean.parseBoolean(arquivoDoGrupo.getString("checkboxSerieD","--")));
        TextView premioCombinado = findViewById(R.id.premio_combinado);
        premioCombinado.setText(arquivoDoGrupo.getString("premioCombinado","--"));
    }

    public void fixaCheckbox(@SuppressWarnings("unused") View view){
        atualizaCampos();
    }

}
