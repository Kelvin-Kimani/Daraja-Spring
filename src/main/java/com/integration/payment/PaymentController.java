package com.integration.payment;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.springframework.http.HttpStatus;
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
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
public class PaymentController {

    @PostMapping("/callback")
    public ResponseEntity<PaymentCallBack> getCallback(PaymentCallBack callBack){
        return ResponseEntity.ok(callBack);
    }

    @PostMapping("/pay")
    public ResponseEntity<Map<String, Response>> processPayment(@RequestBody Payment payment){

        Response res = null;

        /* Extract Info from object */
        String customerName = payment.getNames();
        String amount = String.valueOf(payment.getAmount());
        String phoneNumber = payment.getPhoneNumber();
        String shortCode = "174379";


        try {

            res = pay(phoneNumber, amount, shortCode);

        } catch (IOException exception){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(Map.of("An error occurred", res));
        }

        return ResponseEntity.ok(Map.of("Success", res));
    }

    private Response pay(String phoneNumber,
                     String amount,
                     String shortCode) throws IOException {

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");

        String timeStamp = new SimpleDateFormat("YYYYMMDDHHmmss").format(new Timestamp(System.currentTimeMillis()));
        byte[] timeStampByte = timeStamp.getBytes(StandardCharsets.UTF_8);
        String password = Base64.getEncoder().encodeToString(timeStampByte);


        String payload =  "{" +
                "'BusinessShortCode' : " + shortCode +
                "'Password' : " + password +
                "'Timestamp':" + timeStamp +
                "'Amount': " + amount +
                "'PartyA':" + phoneNumber +
                "'PartyB': 174379," +
                "'PhoneNumber': 254790838747," +
                "'CallBackURL': 'https://datasyde.co.ke'," +
                "'AccountReference': 'CompanyXLTD'," +
                "'TransactionDesc': 'Payment of X'"+
                "}";

        com.squareup.okhttp.RequestBody body = com.squareup.okhttp.RequestBody.create(mediaType, payload);

        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer wiZRj2z7GUp3vVLTmsi68OtUtFRE")
                .build();

        return client.newCall(request).execute();
    }
}
