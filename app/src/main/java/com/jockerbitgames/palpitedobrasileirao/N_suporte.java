package com.jockerbitgames.palpitedobrasileirao;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

import androidx.appcompat.app.AppCompatActivity;

public class N_suporte extends AppCompatActivity {

    private String hoje;
    private String hojeCode;
    private File dirFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_suporte);
        dirFiles = getDir("files",MODE_PRIVATE);
        registraData();
        String texto = getString(R.string.suporte_orientacao_texto01);
        TextView orientacao = findViewById(R.id.suporteOrientacao);
        orientacao.setText(texto);
    }

    private void registraData(){
        Date momento = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        hoje = formatter.format(momento);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter2 = new SimpleDateFormat("yyyyMMddHHmmss");
        hojeCode = formatter2.format(momento);
    }

    public void enviarSuporte(@SuppressWarnings("unused") View view){
        chamarVibrate();
        String manufacturer = Build.MANUFACTURER;
        String brand = Build.BRAND;
        String product = Build.PRODUCT;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        String versionRelease = Build.VERSION.RELEASE;

        EditText suporteTexto = findViewById(R.id.suporteTexto);
        String texto = String.valueOf(suporteTexto.getText());

        SharedPreferences meusDados = getSharedPreferences("meusDados",MODE_PRIVATE);
        SharedPreferences.Editor editor = meusDados.edit();
        editor.putString("manufacturer",manufacturer);
        editor.putString("brand",brand);
        editor.putString("product",product);
        editor.putString("model",model);
        editor.putString("osVersion", String.valueOf(version));
        editor.putString("osVersionRelease",versionRelease);
        editor.apply();

        String meuNumero = meusDados.getString("meuNumero","--");
        Properties properties = new Properties();
        properties.setProperty("manufacturer",manufacturer);
        properties.setProperty("brand",brand);
        properties.setProperty("product",product);
        properties.setProperty("model",model);
        properties.setProperty("osVersion", String.valueOf(version));
        properties.setProperty("osVersionRelease",versionRelease);
        properties.setProperty("palpiteiro",meusDados.getString("palpiteiro","--"));
        properties.setProperty("meuEmail",meusDados.getString("meuEmail","--"));
        properties.setProperty("meuNumero",meuNumero);
        properties.setProperty("data",hoje);
        properties.setProperty("texto",texto);

        String arquivoContato = "suporte"+meuNumero+hojeCode;

        try {
            //File getFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), arquivoContato+".xml"); //Cria o arquivo na memória externa.
            File getFile = new File(dirFiles, arquivoContato+".xml");
            FileOutputStream outputStream = new FileOutputStream(getFile);
            properties.storeToXML(outputStream,"suporte");
            outputStream.flush();
            outputStream.close();
            //subirDados(arquivoContato);
            try {
                Runnable r = Z_runnable_subir_dados.newInstance(arquivoContato,"suporte","--",dirFiles);
                new Thread(r).start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
