package com.integration.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class PaymentController {

    @GetMapping
    public String welcome(){
        return "Welcome guys";
    }


    @PostMapping("/pay")
    public ResponseEntity<Map<String, ?>> processPayment(@RequestBody Payment payment){

        Response res;

        /* Extract Info from object */
        String amount = String.valueOf(payment.getAmount());
        String phoneNumber = payment.getPhoneNumber();
        String shortCode = "174379";

        try {

            res = pay(phoneNumber, amount, shortCode);
            return ResponseEntity.ok(Map.of("Success", res));

        } catch (IOException exception){
            return ResponseEntity.ok(Map.of("An error occurred", exception.getMessage()));
        }
    }

    private Response pay(String phoneNumber,
                     String amount,
                     String shortCode) throws IOException {

        OkHttpClient client = new OkHttpClient();

        String timeStamp = new SimpleDateFormat("YYYYMMDDHHmmss").format(new Timestamp(System.currentTimeMillis()));

        String pwdFields = "174379bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919" + timeStamp;

        byte[] pwdBytes = pwdFields.getBytes(StandardCharsets.UTF_8);

        String password = Base64.getEncoder().encodeToString(pwdBytes);

        Map<String, String> payLoad = new HashMap<>();
        payLoad.put("BusinessShortCode", shortCode);
        payLoad.put("Password", password);
        payLoad.put("Timestamp", timeStamp);
        payLoad.put("TransactionType", "CustomerPayBillOnline");
        payLoad.put("Amount", amount);
        payLoad.put("PartyA", phoneNumber);
        payLoad.put("PartyB", "174379");
        payLoad.put("PhoneNumber", "254790838747");
        payLoad.put("CallBackURL", "https://miramsolutions.co.ke");
        payLoad.put("AccountReference", "12345");
        payLoad.put("TransactionDesc", "Payment of X");

        JSONObject object = new JSONObject(payLoad);
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        okhttp3.RequestBody body = okhttp3.RequestBody.create(object.toString(), mediaType);

        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer VM8uDjO3z6KiMG0v7KtsPjJGDjhb")
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.body().string());

        return response;
    }
}
