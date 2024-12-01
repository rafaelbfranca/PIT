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
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class C_cadastro extends AppCompatActivity {

    private String hoje;
    private String ano;
    private String palpiteiro;
    private String timeDoCoracao;
    private String codigoPais;
    private String dddDoCel;
    private String meuNumero;
    private String meuEmail;
    private String nAntigo;
    private EditText cadastroPalpiteiro;
    private EditText cadastroTime;
    private EditText cadastroPais;
    private EditText cadastroDdd;
    private EditText cadastroNumero;
    private EditText cadastroEmail;
    private View dadosCadastroView;
    private View verificaErroView;
    private View numeroNovoView;
    private View codigoPaisDdd;
    private ImageButton saveBtn;
    private SharedPreferences prefs;
    private FirebaseAuth auth;
    private CheckBox checkbox01;
    private CheckBox checkbox02;
    private CheckBox checkbox03;
    private boolean redeDisponivel;
    private boolean alterarCadastro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_cadastro);
        redeDisponivel = Z_static_functions.verificandoRede(this) != 0;
        setDadosComuns("arquivo","criado");
        registraData();
        nAntigo = "--";
        prefs = getSharedPreferences("meusDados", MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        dadosCadastroView = findViewById(R.id.dadosCadastroScroll);
        verificaErroView = findViewById(R.id.verificaErroScroll);
        checkbox01 = findViewById(R.id.verificaErroCheckbox01);
        checkbox02 = findViewById(R.id.verificaErroCheckbox02);
        checkbox03 = findViewById(R.id.verificaErroCheckbox03);
        numeroNovoView = findViewById(R.id.numeroNovoScroll);
        cadastroPalpiteiro = findViewById(R.id.palpiteiro);
        cadastroTime = findViewById(R.id.time_do_coracao);
        codigoPaisDdd = findViewById(R.id.codigos_pais_ddd);
        cadastroPais = findViewById(R.id.codigo_do_pais);
        cadastroDdd = findViewById(R.id.ddd_do_cel);
        cadastroNumero = findViewById(R.id.meu_numero);
        cadastroEmail = findViewById(R.id.meu_email);
        saveBtn = findViewById(R.id.saveButton);
        recebeDados();
        atualizaCampos();
    }

    private void recebeDados(){
        Intent alterar = getIntent();
        alterarCadastro = alterar.getBooleanExtra("alterarCadastro",false);
    }

    private void registraData(){
        Date momento = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterHoje = new SimpleDateFormat("dd-MMM-yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterAno = new SimpleDateFormat("yyyy");
        hoje = formatterHoje.format(momento);
        ano = formatterAno.format(momento);
        setDadosComuns("atualizadoEm",hoje);
    }

    private void setDadosComuns(String chave, String valor){
        //Dados que serão lidos por outras classes.
        SharedPreferences dadosComuns = getSharedPreferences("dadosComuns", MODE_PRIVATE);
        SharedPreferences.Editor editor = dadosComuns.edit();
        editor.putString(chave, valor);
        editor.apply();
    }

    private void atualizaCampos(){
        verificaErroView.setVisibility(GONE);
        numeroNovoView.setVisibility(GONE);
        palpiteiro = prefs.getString("palpiteiro", "null");
        if(!Objects.equals(palpiteiro, "null") && !alterarCadastro){
            int editar = 0;
            codigoPaisDdd.setVisibility(GONE);
            saveBtn.setVisibility(GONE);
            cadastroPalpiteiro.setText(palpiteiro);
            cadastroPalpiteiro.setInputType(editar);//"0" desabilita a edição e "1" habilita.
            cadastroTime.setText(prefs.getString("timeDoCoracao", "null"));
            cadastroTime.setInputType(editar);//"0" desabilita a edição e "1" habilita.
            cadastroNumero.setText(prefs.getString("meuNumero", "null"));
            cadastroNumero.setInputType(editar);//"0" desabilita a edição e "1" habilita.
            cadastroEmail.setText(prefs.getString("meuEmail", "null"));
            cadastroEmail.setInputType(editar);//"0" desabilita a edição e "1" habilita.
        }
        else{
            if (alterarCadastro){
                cadastroPalpiteiro.setText(palpiteiro);
                cadastroTime.setText(prefs.getString("timeDoCoracao", "null"));
                codigoPais = prefs.getString("meuPais", "null");
                cadastroPais.setText(codigoPais);
                dddDoCel = prefs.getString("meuDDD", "null");
                cadastroDdd.setText(dddDoCel);
                String codigos = limpaNumero(codigoPais) + limpaNumero(dddDoCel);
                StringBuilder celular = new StringBuilder();
                celular.append(prefs.getString("meuNumero", "null"));
                for (int i = codigos.length() - 1; i>=0; i--){
                    celular.deleteCharAt(i);
                }
                cadastroNumero.setText(celular.toString());
                cadastroEmail.setText(prefs.getString("meuEmail", "null"));
            }
            cadastroPalpiteiro.requestFocus();
            setDadosComuns("ano",ano);
        }
    }

    public void buttonSalvar(@SuppressWarnings("unused") View view){
        chamarVibrate();
        if (redeDisponivel) {
            //**********************LÊ TEXTO DIGITADO**********************
            palpiteiro = cadastroPalpiteiro.getText().toString();
            timeDoCoracao = cadastroTime.getText().toString();
            codigoPais = cadastroPais.getText().toString();
            dddDoCel = cadastroDdd.getText().toString();
            String numeroDoCel = cadastroNumero.getText().toString();
            meuNumero = codigoPais + dddDoCel + numeroDoCel;
            meuEmail = cadastroEmail.getText().toString();
            //*********************(Fim do escopo do comentário)**********************

            //**********************VERIFICA CAMPOS SEM PREENCHIMENTO**********************
            String[] listaCampos = new String[]{palpiteiro, timeDoCoracao, codigoPais, dddDoCel, numeroDoCel, meuEmail};
            int campos = 0;
            for (String dado : listaCampos) {
                if (dado.isEmpty()) {
                    campos++;
                }
            }
            if (campos > 0) {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            } else {
                //Confirmar o número do telefone antes de salvar.
                String texto1 = getString(R.string.confirmacaoCadastroNumeroTexto1);
                String texto2 = getString(R.string.confirmacaoCadastroNumeroTexto2);
                String texto3 = getString(R.string.confirmacaoCadastroNumeroTexto3);
                String pergunta = getString(R.string.confirmacaoCadastroNumeroPergunta);
                new AlertDialog.Builder(this)
                        .setMessage(texto1 + "\n\n" + meuNumero + "\n\n" + texto2 + "\n\n" + texto3 + "\n\n" + meuEmail + "\n\n" + pergunta)
                        .setPositiveButton("Sim", (dialog, which) -> {
                            meuNumero = limpaNumero(String.valueOf(cadastroPais.getText())) + limpaNumero(String.valueOf(cadastroDdd.getText())) + limpaNumero(String.valueOf(cadastroNumero.getText()));
                            registerUser(meuEmail, meuNumero);
                        })
                        .setNegativeButton("Não", null)
                        .show();
            }
            //*********************(Fim do escopo do comentário)**********************
        }
        else{
            Toast.makeText(this, "Rede indisponível", Toast.LENGTH_SHORT).show();
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

    private void registerUser(String email, String password){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(C_cadastro.this, task -> {
            if (task.isSuccessful()) {
                Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification();
                salvarCadastro();
            } else {
                Toast.makeText(C_cadastro.this, "Cadastro falhou.", Toast.LENGTH_LONG).show();
                verificarErroDeCadastro();
            }
        });
    }

    private void salvarCadastro() {
        //**********************CRIA/ABRE ARQUIVO XML E SALVA TEXTO DIGITADO**********************
        String manufacturer = Build.MANUFACTURER;
        String brand = Build.BRAND;
        String product = Build.PRODUCT;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        String versionRelease = Build.VERSION.RELEASE;

        SharedPreferences prefs = getSharedPreferences("meusDados", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("palpiteiro", palpiteiro);
        editor.putString("timeDoCoracao", timeDoCoracao);
        if (!nAntigo.equals("--")){
            editor.putString("numeroAntigo", nAntigo);
        }

        editor.putString("manufacturer",manufacturer);
        editor.putString("brand",brand);
        editor.putString("product",product);
        editor.putString("model",model);
        editor.putString("osVersion", String.valueOf(version));
        editor.putString("osVersionRelease",versionRelease);

        editor.putString("meuPais", codigoPais);
        editor.putString("meuDDD", dddDoCel);
        editor.putString("meuNumero", meuNumero);
        editor.putString("meuEmail", meuEmail);
        editor.putString("totalPontosSerieA","0");
        editor.putString("totalPontosSerieB","0");
        editor.putString("totalPontosSerieC","0");
        editor.putString("totalPontosSerieD","0");
        editor.putString("atualizadoEm", hoje);
        editor.apply();
        Toast.makeText(C_cadastro.this, "Cadastro salvo.", Toast.LENGTH_LONG).show();
        restartApp();
        //*********************(Fim do escopo do comentário)**********************
    }

    private void verificarErroDeCadastro(){
        resetAllCheckbox();
        dadosCadastroView.setVisibility(GONE);
        verificaErroView.setVisibility(VISIBLE);

        checkbox01.setOnClickListener(v -> alerta01());
        checkbox02.setOnClickListener(v -> auth.signInWithEmailAndPassword(meuEmail,meuNumero).addOnCompleteListener(C_cadastro.this, task -> {
            if (task.isSuccessful()) {
                salvarCadastro();
            } else {
                Toast.makeText(C_cadastro.this, "O número não é o mesmo.", Toast.LENGTH_SHORT).show();
                dadosCadastroView.setVisibility(VISIBLE);
                verificaErroView.setVisibility(GONE);
            }
        }));
        checkbox03.setOnClickListener(v -> {
            verificaErroView.setVisibility(GONE);
            numeroNovoView.setVisibility(VISIBLE);
            EditText paisAntigo = findViewById(R.id.codigo_do_pais_antigo);
            paisAntigo.requestFocus();
        });
    }

    public void buttonAtualizar(@SuppressWarnings("unused") View view){
        chamarVibrate();
        EditText paisAntigo = findViewById(R.id.codigo_do_pais_antigo);
        EditText dddAntigo = findViewById(R.id.ddd_do_cel_antigo);
        EditText numAntigo = findViewById(R.id.numeroAntigoEdit);
        nAntigo = limpaNumero(String.valueOf(paisAntigo.getText())) + limpaNumero(String.valueOf(dddAntigo.getText())) + limpaNumero(String.valueOf(numAntigo.getText()));
        EditText paisNovo = findViewById(R.id.codigo_do_pais_novo);
        EditText dddNovo = findViewById(R.id.ddd_do_cel_novo);
        EditText numNovo = findViewById(R.id.numeroNovoEdit);
        final String nNovo = limpaNumero(String.valueOf(paisNovo.getText())) + limpaNumero(String.valueOf(dddNovo.getText())) + limpaNumero(String.valueOf(numNovo.getText()));
        if (nNovo.equals(meuNumero)) {
            if (!nAntigo.equals("")) {
                auth.signInWithEmailAndPassword(meuEmail, nAntigo).addOnCompleteListener(C_cadastro.this, task -> {
                    if (task.isSuccessful()) {
                        alerta03(nAntigo, nNovo);
                    }
                });
            } else {
                Toast.makeText(this, "Informe os dois números.", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(this, "O número novo não confere.", Toast.LENGTH_LONG).show();
            dadosCadastroView.setVisibility(VISIBLE);
            numeroNovoView.setVisibility(GONE);
        }
    }

    private String limpaNumero(String numero){
        StringBuilder num = new StringBuilder(numero);
        //Exclui qualquer caracter que não seja número.
        for (int i = num.length()-1; i>=0; i--){
            if ((num.charAt(i)<48)||((num.charAt(i)>57))){
                num.delete(i,i+1);
            }
        }

        //Exclui todos os zeros à esquerda.
        while ((num.charAt(0)<49)||((num.charAt(0)>57))){ // 49 ao 57
            num.delete(0,1);
        }

        numero = String.valueOf(num);
        return numero;
    }

    private void resetAllCheckbox(){
        CheckBox checkbox01 = findViewById(R.id.verificaErroCheckbox01);
        CheckBox checkbox02 = findViewById(R.id.verificaErroCheckbox02);
        CheckBox checkbox03 = findViewById(R.id.verificaErroCheckbox03);
        checkbox01.setChecked(false);
        checkbox02.setChecked(false);
        checkbox03.setChecked(false);
    }

    private void alerta01(){
        new AlertDialog.Builder(this)
                .setMessage("Você pode estar tentando por engano utilizar um e-mail já cadastrado.\n\n" +
                        meuEmail + "\n\n" +
                        "O e-mail está correto?\n\n")
                .setPositiveButton("Correto", (dialog, which) -> alerta02())
                .setNegativeButton("Corrigir", (dialog, which) -> {
                    dadosCadastroView.setVisibility(VISIBLE);
                    verificaErroView.setVisibility(GONE);
                })
                .show();
    }

    private void alerta02(){
        new AlertDialog.Builder(this)
                .setMessage("Se você já recebeu um e-mail de confirmação, delete o e-mail SEM fazer a confirmação," +
                        " aguarde 24h e refaça o seu cadastro.\n" +
                        "Outra pessoa pode ter utilizado o seu e-mail por engano para solicitar cadastro," +
                        " mas não se preocupe, se a confirmação não for feita em 24h a solicitação será excluída" +
                        " e você poderá fazer o seu cadastro normalmente.\n" +
                        "Se não quiser esperar, você também pode tentar se cadastrar com um outro e-mail que você utilize.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    private void alerta03(final String nAntigo, final String nNovo){
        new AlertDialog.Builder(this)
                .setMessage("Você está alterando o seu cadastro do número "+nAntigo+" para o número "+nNovo+".")
                .setPositiveButton("OK", (dialog, which) -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        user.updatePassword(nNovo);
                        salvarCadastro();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dadosCadastroView.setVisibility(VISIBLE);
                    verificaErroView.setVisibility(GONE);
                    numeroNovoView.setVisibility(GONE);
                })
                .show();
    }

    private void restartApp(){
        Intent restart = new Intent(this, Z01_confirma_verificacao_email.class);
        finishAffinity();
        startActivity(restart);
    }

    //**********************MENU**********************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_c_cadastro, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // return true so that the menu pop up is opened
        List<Object> ids = new ArrayList<>();
        ids.add(R.id.instrucoes);
        ids.add(R.id.sair);

        switch (ids.indexOf(item.getItemId())) {
            case 0:
                Intent instruction = new Intent(this,B_instrucoes.class);
                instruction.putExtra("tela","C_cadastro");
                startActivity(instruction);
                break;
            case 1:
                finish();
        }
        return true;
    }
    //*********************(Fim do escopo do comentário)**********************
}
