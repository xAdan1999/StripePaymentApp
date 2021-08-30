package com.example.stripepaymentapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

public class AmountActivity extends AppCompatActivity {

    Button btn_500;
    Button btn_1000;
    Button btn_5000;
    Button btn_10000;

    EditText et_otraCantidad;
    Button btn_procesar;

    String amount = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount);

        if(isConnected(this)){
            showCustomDialog();
        }

        btn_500 = findViewById(R.id.btn_500);
        btn_1000 = findViewById(R.id.btn_1000);
        btn_5000 = findViewById(R.id.btn_5000);
        btn_10000 = findViewById(R.id.btn_10000);

        et_otraCantidad = findViewById(R.id.tf_otraCantidad);
        btn_procesar = findViewById(R.id.btn_procesar);

        loadAnimations();

        btn_500.setOnClickListener(v -> {
            if(isConnected(this)){
                showCustomDialog();
            }else{
                amount = "500";
                Intent intent = new Intent(getApplicationContext(), PayActivity.class);
                intent.putExtra("amount", amount);
                startActivity(intent);
                Clean();
            }
        });

        btn_1000.setOnClickListener(v -> {
            if(isConnected(this)){
                showCustomDialog();
            }else{
                amount = "1000";
                Intent intent = new Intent(getApplicationContext(), PayActivity.class);
                intent.putExtra("amount", amount);
                startActivity(intent);
                Clean();
            }
        });

        btn_5000.setOnClickListener(v -> {
            if(isConnected(this)){
                showCustomDialog();
            }else{
                amount = "5000";
                Intent intent = new Intent(getApplicationContext(), PayActivity.class);
                intent.putExtra("amount", amount);
                startActivity(intent);
                Clean();
            }
        });

        btn_10000.setOnClickListener(v -> {
            if(isConnected(this)){
                showCustomDialog();
            }else{
                amount = "10000";
                Intent intent = new Intent(getApplicationContext(), PayActivity.class);
                intent.putExtra("amount", amount);
                startActivity(intent);
                Clean();
            }
        });

        btn_procesar.setOnClickListener(v -> {
            if(isConnected(this)){
                showCustomDialog();
            }else{
                amount = et_otraCantidad.getText().toString();
                if("".equals(amount)){
                    et_otraCantidad.setError("Ingresa la cantidad");
                    et_otraCantidad.requestFocus();
                    return;
                }
                else{
                    double newAmount = Double.parseDouble(amount);
                    if(newAmount < 10 ){
                        et_otraCantidad.setError("La cantidad mínima para realizar una transacción es de $10");
                        et_otraCantidad.requestFocus();
                        return;
                    }
                }
                Intent intent = new Intent(getApplicationContext(), PayActivity.class);
                intent.putExtra("amount", amount);
                startActivity(intent);
                et_otraCantidad.setText("");
            }
        });
    }

    private void loadAnimations(){
        btn_500.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animation));
        btn_1000.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animation));
        btn_5000.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animation));
        btn_10000.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animation));
    }

    private void Clean(){
        if (!(et_otraCantidad.getText().toString()).isEmpty()) {
            et_otraCantidad.setText("");
        }
    }

    private boolean isConnected(AmountActivity amountActivity){
        ConnectivityManager connectivityManager = (ConnectivityManager) amountActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return (wifi == null || !wifi.isConnected()) && (mobile == null || !mobile.isConnected());
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AmountActivity.this);
        builder.setMessage("Conectate a internet para comenzar a realizar pagos").setCancelable(false).setPositiveButton("Conectar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==event.KEYCODE_BACK){
            AlertDialog.Builder builder = new AlertDialog.Builder(AmountActivity.this);
            builder.setMessage("¿Deseas salir de la aplicacion?");
            builder.setCancelable(false);
            builder.setPositiveButton("SI", (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            builder.setNegativeButton("CANCELAR", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.create().show();
        }
        return super.onKeyDown(keyCode, event);
    }
}