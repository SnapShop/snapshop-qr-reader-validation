package com.mycompany.androidqrcodeexample;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AndroidQrCodeExample extends Activity  {

    private final String BASE_URL = "https://snap-shop.herokuapp.com";
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private TextView message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the main content layout of the Activity
        setContentView(R.layout.activity_main);

        message = (TextView) findViewById(R.id.message);
    }

    public void scanBar(View v) {
        ValidatePayment task = new ValidatePayment("ch_15pR6GCm4h5tM32oTqutLjSf");
        task.execute();
    }

    //product qr code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(AndroidQrCodeExample.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");

                ValidatePayment task = new ValidatePayment(contents);
                task.execute();
            }
        }
    }

    private class ValidatePayment extends AsyncTask<Void, Void, String> {
        private String chargeid;

        public ValidatePayment(String chargeid) {
            this.chargeid = chargeid;
        }
        @Override
        protected String doInBackground(Void... params) {
            System.out.println("ValidatePayment.doInBackground()");
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(BASE_URL + "/validate");

            try {
                List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                urlParameters.add(new BasicNameValuePair("chargeid", chargeid));
                httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

                HttpResponse response = httpClient.execute(httpPost); // sends to server
                System.out.println("Status line: " + response.getStatusLine());

                System.out.println("JSON => " + response.getStatusLine().getStatusCode());

                String json_string = EntityUtils.toString(response.getEntity());
                JSONObject json = new JSONObject(json_string);

                String paid = json.getString("paid");

                if (paid.equals("true")) {
                    System.out.println("PAYMENT SUCCESSFUL! \n Thank you!");

                } else {
                    System.out.println("PAYMENT UNSUCCESSFUL!");
                }

                return paid;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println("ValidatePayment.onPostExecute()");
            if (result.equals("true")) {
                message.setText("PAYMENT SUCCESSFUL! \n Thank you!");
            } else {
                message.setText("PAYMENT UNSUCCESSFUL!");
            }
        }
    }
}