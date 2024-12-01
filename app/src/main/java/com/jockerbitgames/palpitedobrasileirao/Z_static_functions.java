package com.jockerbitgames.palpitedobrasileirao;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.annotation.IntRange;
import androidx.appcompat.app.AppCompatActivity;

class Z_static_functions extends AppCompatActivity {

   @IntRange(from = 0, to = 3)
   public static int verificandoRede(Context context) {
      int result = 0; // Returns connection type. 0: none; 1: mobile data; 2: wifi
      ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cm != null) {
         NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
         if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
               result = 2;
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
               result = 1;
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
               result = 3;
            }
         }
      } else {
         if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
               // connected to the internet
               if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                  result = 2;
               } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                  result = 1;
               } else if (activeNetwork.getType() == ConnectivityManager.TYPE_VPN) {
                  result = 3;
               }
            }
         }
      }
      return result;
   }

}
