package com.jockerbitgames.palpitedobrasileirao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Z01_confirma_verificacao_email extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //verificandoRede();
        if(Z_static_functions.verificandoRede(this) != 0){
            confirmacaoDeEmail();
        }else{
            Toast.makeText(this, "Rede indisponível.", Toast.LENGTH_LONG).show();
            chamarMenuInicial();
        }
    }

    private void confirmacaoDeEmail(){
        auth = FirebaseAuth.getInstance();
        SharedPreferences meusDados = getSharedPreferences("meusDados",MODE_PRIVATE);
        SharedPreferences.Editor meusDadosEditor = meusDados.edit();
        if (Objects.equals(meusDados.getString("arquivo", "--"), "--")){
            meusDadosEditor.putString("arquivo","criado");
        }

        if (!Objects.equals(meusDados.getString("meuNumero", "--"), "--")){
            final String meuEmail = meusDados.getString("meuEmail", "--");
            final String meuNumero = meusDados.getString("meuNumero", "--");
            auth.signInWithEmailAndPassword(Objects.requireNonNull(meuEmail), Objects.requireNonNull(meuNumero)).addOnCompleteListener(Z01_confirma_verificacao_email.this, task -> {

            });
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null){
            Objects.requireNonNull(user).reload();
            if (Objects.requireNonNull(user).isEmailVerified()){
                meusDadosEditor.putString("emailVerificado","true");
                chamarBaixarDadosFirebase();
            }
            else{
                meusDadosEditor.putString("emailVerificado","false");
                new AlertDialog.Builder(this)
                        .setMessage("Seu cadastro ainda não foi confirmado.\n"+
                                    "É necessário clicar no link do e-mail de verificação para confirmar o seu cadastro e depois reabrir o app. O sistema pode levar alguns minutos para reconhecer a confirmação.\n\n"+
                                    "Você precisa receber outro e-mail de verificação?.\n\n")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification();
                            Toast.makeText(Z01_confirma_verificacao_email.this, "E-mail enviado.", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton("Não", (dialog, which) -> finish())
                        .show();
            }
        }
        else{
            meusDadosEditor.putString("emailVerificado","false");
            abrirCadastro();
        }
        meusDadosEditor.apply();
    }

    private void chamarMenuInicial(){
        Intent menuInicial = new Intent(this, A_menu_inicial.class);
        startActivity(menuInicial);
        finish();
    }

    private void abrirCadastro(){
        new AlertDialog.Builder(this)
                .setMessage("ESCLARECIMENTOS\n\n" +
                        "Seus dados de cadastro não serão compartilhados com ninguém e servirão apenas para o correto funcionamento do app.\n\n" +
                        "Os dados que o app solicita no seu cadastro são necessários para as seguintes situações:\n" +
                        "1ª - (número e e-mail) Para você ter acesso seguro aos arquivos do app;\n" +
                        "2ª - (apelido e número) Para seus contatos reconhecerem você quando você enviar convites para eles;\n" +
                        "3ª - (número) Para que os convites dos seus contatos cheguem até você;\n" +
                        "4ª - (e-mail) Para  você receber respostas quando contatar o suporte do app.\n\n" +
                        "Seja muito bem vindo e divirta-se!")
                .setPositiveButton("Continuar", (dialog, which) -> {
                    Intent cadastro = new Intent(Z01_confirma_verificacao_email.this,C_cadastro.class);
                    startActivity(cadastro);
                    finishAffinity();
                })
                .setNegativeButton("Sair", (dialog, which) -> finishAffinity())
                .show();
    }

    private void chamarBaixarDadosFirebase(){
        Intent baixarDadosFirebase = new Intent(this, Z02_baixar_dados_firebase.class);
        startActivity(baixarDadosFirebase);
        finish();
    }

}
