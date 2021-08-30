package com.example.stripepaymentapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PayActivity extends AppCompatActivity {

    private static final String BACKEND_URL = "https://vast-chamber-94682.herokuapp.com/";
    private OkHttpClient httpClient = new OkHttpClient();
    private String paymentIntentClientSecret;
    private Stripe stripe;

    public TextView et_amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        et_amount = findViewById(R.id.tv_amount);
        et_amount.setText(getIntent().getStringExtra("amount"));

        //Configure the SDK with your Stripe publishable key so it can make requests to Stripe
        stripe = new Stripe(getApplicationContext(), Objects.requireNonNull("pk_test_51JGC4LIRBnw4Xdu233hZIEkrsHAiFcdsJv5vIExP7pqE6TdhgjxdodTyWlREKxNGQmnOc4qKcEzdf2VC245JuVKn00XfmYsfBp"));
        startCheckout();
    }

    private void startCheckout() {
        //Create a PaymentIntent by calling the server's endpoint.
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");

        double amount = Double.parseDouble(et_amount.getText().toString())*100;

        Map<String, Object> payMap = new HashMap<>();
        Map<String, Object> itemMap = new HashMap<>();
        List<Map<String, Object>> itemList = new ArrayList<>();
        payMap.put("currency", "mxn");
        itemMap.put("id", "photo_subscription");
        itemMap.put("amount", amount);
        itemList.add(itemMap);
        payMap.put("items", itemList);
        String json = new Gson().toJson(payMap);

        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.MINUTES) //connect timeout
                .writeTimeout(5, TimeUnit.MINUTES) //write timeout
                .readTimeout(5, TimeUnit.MINUTES); //read timeout

        httpClient = builder.build();
        httpClient.newCall(request).enqueue(new PayCallback(this));

        //Hook up the pay button to the card widget and stripe instance
        Button payButton = findViewById(R.id.payButton);
        payButton.setOnClickListener((View view) -> {
            try{
                CardInputWidget cardInputWidget = (CardInputWidget)findViewById(R.id.cardInputWidget);
                PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();

                if (params != null) {
                    payButton.setText(R.string.btn_espera);
                    payButton.setClickable(false);
                    ConfirmPaymentIntentParams confirmParams =
                            ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams
                                    (params, paymentIntentClientSecret);
                    stripe.confirmPayment(this, confirmParams);
                }
            }
            catch (Exception ex){
                Toast.makeText(getApplicationContext(), "Error: " + ex.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayAlert(@NonNull String title, @Nullable String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(title).setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", (dialog, which) -> {
            Intent intent = new Intent(getApplicationContext(), AmountActivity.class);
            startActivity(intent);
            finish();
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
    }

    private void onPaymentSuccess(@NonNull final Response response) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> responseMap = gson.fromJson(Objects.requireNonNull(response.body()).string(), type);
        paymentIntentClientSecret = responseMap.get("clientSecret");
    }

    private static final class PayCallback implements Callback {
        @NonNull private final WeakReference<PayActivity> activityRef;

        PayCallback(@NonNull PayActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            final PayActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(() ->
                    Toast.makeText(activity, "Error: " + e.toString(), Toast.LENGTH_LONG).show()
            );
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
            final PayActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            if (!response.isSuccessful()) {
                activity.runOnUiThread(() ->
                        Toast.makeText(activity, "Error: " + response.toString(), Toast.LENGTH_LONG).show()
                );
            } else {
                activity.onPaymentSuccess(response);
            }
        }
    }

    private static final class PaymentResultCallback implements ApiResultCallback<PaymentIntentResult> {
        @NonNull private final WeakReference<PayActivity> activityRef;

        PaymentResultCallback(@NonNull PayActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final PayActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                activity.displayAlert("Transacción realizada","La transacción se realizó correctamente");
            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                activity.displayAlert("Error", "Error al intentar realizar la transacción");
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            final PayActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            activity.displayAlert("Error", e.toString());
        }
    }
}