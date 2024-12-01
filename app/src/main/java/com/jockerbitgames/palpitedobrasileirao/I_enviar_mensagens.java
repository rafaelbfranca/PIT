package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

public class I_enviar_mensagens extends AppCompatActivity {

    private String mensagem;
    private String url_instalar;
    private ArrayList<String> contatosMarcados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.i_enviar_mensagens);
        contatosMarcados = new ArrayList<>();
        recebeDados();
        resgataArrays();
        ImageButton inviteBtnSms = findViewById(R.id.invite_button_sms);
        ImageButton inviteBtnZap = findViewById(R.id.invite_button_zap);

        inviteBtnSms.setOnClickListener(view -> {
            chamarVibrate();
            //Para o SMS, o total de caracteres da mensagem com link não pode ultrapassar 70.
            mensagem =  getString(R.string.mensagemConviteSms)+"\n"+url_instalar;
            sendSmsByManager();
        });

        inviteBtnZap.setOnClickListener(view -> {
            chamarVibrate();
            mensagem =  getString(R.string.mensagemConviteZap) + "\n" + url_instalar;
            sendTextMsgOnZap();
        });
    }

    private void recebeDados(){
        Intent enviarMensagens = getIntent();
        contatosMarcados = enviarMensagens.getStringArrayListExtra("listaContatosMensagens");
    }

    private void resgataArrays(){
        SharedPreferences referencias = getSharedPreferences("referencias",MODE_PRIVATE);
        url_instalar = referencias.getString("instaladorDoApp","--");
    }

    //**********Enviar convite por SMS**********
    private void sendSmsByManager() {
        if (Z_static_functions.verificandoRede(this) != 0) {
            if (contatosMarcados.size() > 0) {
                for (String numero : contatosMarcados) {
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + numero));
                    smsIntent.putExtra("sms_body", mensagem);
                    startActivity(smsIntent);
                }
                finish();
            }
        }
    }

    //**********Enviar convite por Zap**********
    private void sendTextMsgOnZap() {
        if (Z_static_functions.verificandoRede(this) != 0) {
            if (contatosMarcados.size() > 0) {
                PackageManager packageManager = this.getPackageManager();
                Intent zapIntent = new Intent(Intent.ACTION_VIEW);
                try {
                    for (String numero:contatosMarcados) {
                        String url = "https://api.whatsapp.com/send?phone=" + numero + "&text=" + URLEncoder.encode(mensagem, "UTF-8");
                        zapIntent.setPackage("com.whatsapp");
                        zapIntent.setData(Uri.parse(url));
                        if (zapIntent.resolveActivity(packageManager) != null) {
                            this.startActivity(zapIntent);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
        inflater.inflate(R.menu.menu_i_enviar_mensagens, menu);
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
                instruction.putExtra("tela","I_enviar_mensagens");
                startActivity(instruction);
                break;
            case 1:
                finish();
        }
        return true;
    }
}
