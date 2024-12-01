package com.jockerbitgames.palpitedobrasileirao;

import android.net.Uri;
import android.widget.Toast;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import androidx.appcompat.app.AppCompatActivity;

public class Z_runnable_subir_dados extends AppCompatActivity implements Runnable {
   //Declaração de variáveis globais tratadas no construtor
   static String arquivoDadosFirebase,caso,ano;
   static File dirFiles;
   //---------------------------------

   //Declaração de outras variáveis globais
   private StorageReference convitesRef;
   private StorageReference gruposRef;
   private StorageReference meusGruposBackupRef;
   private StorageReference meusPalpitesBackupRef;
   private StorageReference meusPontosBackupRef;
   private StorageReference usuariosRef;
   private StorageReference suporteRef;
   //---------------------------------

   public Z_runnable_subir_dados(){
      // Required empty public constructor
   }

   public static Z_runnable_subir_dados newInstance(String file, String casoSubir, String year, File dir) {
      Z_runnable_subir_dados zRunnableSubirDados = new Z_runnable_subir_dados();
      arquivoDadosFirebase = file;
      caso = casoSubir;
      ano = year;
      dirFiles = dir;
      return zRunnableSubirDados;
   }

   @Override
   public void run() {
      try {
         FirebaseStorage storage = FirebaseStorage.getInstance();
         StorageReference storageRef = storage.getReference();
         usuariosRef = storageRef.child("usuarios");
         suporteRef = storageRef.child("suporte");
         StorageReference anoRef = storageRef.child("ano" + ano);
         gruposRef = anoRef.child("grupos"+ano);
         meusGruposBackupRef = anoRef.child("meusGruposBackup"+ano);
         meusPalpitesBackupRef = anoRef.child("meusPalpitesBackup"+ano);
         meusPontosBackupRef = anoRef.child("meusPontosBackup"+ano);
         convitesRef = anoRef.child("convites"+ano);
         subirDados(arquivoDadosFirebase,caso);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private void subirDados(final String subirArquivo, final String caso){

      StorageReference fileRef;
      File file = new File(dirFiles, subirArquivo + ".xml");
      Uri uri = Uri.fromFile(file);
      UploadTask uploadTask = null;
      switch(caso) {
         case "grupo":
            fileRef = gruposRef.child(subirArquivo + ".xml");
            uploadTask = fileRef.putFile(uri);
            break;
         case "usuario":
         case "update":
            fileRef = usuariosRef.child(subirArquivo + ".xml");
            uploadTask = fileRef.putFile(uri);
            break;
         case "convite":
            fileRef = convitesRef.child(subirArquivo + ".xml");
            uploadTask = fileRef.putFile(uri);
            break;
         case "suporte":
            fileRef = suporteRef.child(subirArquivo + ".xml");
            uploadTask = fileRef.putFile(uri);
            break;
         case "meusGruposBackup":
            fileRef = meusGruposBackupRef.child(subirArquivo + ".xml");
            uploadTask = fileRef.putFile(uri);
            break;
         case "meusPalpitesBackup":
            fileRef = meusPalpitesBackupRef.child(subirArquivo + ".xml");
            uploadTask = fileRef.putFile(uri);
            break;
         case "meusPontosBackup":
            fileRef = meusPontosBackupRef.child(subirArquivo + ".xml");
            uploadTask = fileRef.putFile(uri);
            break;
      }
      if (uploadTask != null) {
         uploadTask.addOnFailureListener(exception -> {
            if (caso.equals("suporte")){
               Toast.makeText(Z_runnable_subir_dados.this, "A mensagem não foi enviada. Tente novamente depois.", Toast.LENGTH_SHORT).show();
            }
            if (caso.equals("convite")){
               Toast.makeText(Z_runnable_subir_dados.this, "O convite não foi enviado. Tente novamente depois.", Toast.LENGTH_SHORT).show();
            }
         }).addOnSuccessListener(taskSnapshot -> {
            if (caso.equals("suporte")){
               Toast.makeText(Z_runnable_subir_dados.this, "Sua mensagem foi enviada para o suporte. Se for necessário, a resposta será enviada para o seu e-mail.", Toast.LENGTH_LONG).show();
            }
         });
      }
      //noinspection ResultOfMethodCallIgnored
      file.delete();
   }
}
