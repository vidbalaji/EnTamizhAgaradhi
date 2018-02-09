package com.vidhyalearning.entamizhagaradhi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {
    EditText wordText;
    TextView meaningText;
    String meaningStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wordText = (EditText)findViewById(R.id.wordText);
        meaningText = (TextView)findViewById(R.id.meaningText);
        meaningText.setMovementMethod(new ScrollingMovementMethod());
        meaningText.setText("");
    }

    public void findMeaning(View view) {
        String searchStr = wordText.getText().toString();

        meaningStr="";

        meaningText.setText(meaningStr);
        String finalUrl = "https://od-api.oxforddictionaries.com:443/api/v1/entries/ta/" + searchStr;
        HttpGetRequest getRequest = new HttpGetRequest();
        //Perform the doInBackground method, passing in our url
        try {
            getRequest.execute(finalUrl).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if(meaningStr.isEmpty()){
            meaningStr="மன்னிக்கவும்!!எந்த அர்த்தமும் காணப்படவில்லை!!";
        }
        meaningText.setText(meaningStr);
    }
    public class HttpGetRequest extends AsyncTask<String, Void, String> {


        String result;
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... params) {
            String stringUrl = params[0];


            try{
                Log.d("dictionary",stringUrl);
                URL url = new URL(stringUrl);
                HttpURLConnection con = null;
                try {
                    con = (HttpURLConnection) url.openConnection();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    con.setRequestMethod("GET");
                } catch (ProtocolException e1) {
                    e1.printStackTrace();
                }
                con.setRequestProperty("Accept","application/json");
                con.setRequestProperty(     "app_id","xxx");
                con.setRequestProperty(    "app_key", "xxxxxx");

                int status = 0;
                try {

                    status = con.getResponseCode();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                switch(status) {

                    case HttpURLConnection.HTTP_GONE:
                        // The timestamp is expired.
                        throw new AffiliateAPIException("URL expired");

                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        // The API Token or the Tracking ID is invalid.
                        throw new AffiliateAPIException("API Token or Affiliate Tracking ID invalid.");

                    case HttpURLConnection.HTTP_FORBIDDEN:
                        // Tampered URL, i.e., there is a signature mismatch.
                        // The URL contents are modified from the originally returned value.
                        throw new AffiliateAPIException("Tampered URL - The URL contents are modified from the originally returned value");

                    case HttpURLConnection.HTTP_OK:
                        String response = convertStreamToString(con.getInputStream());
                        parseUsingDOM(response);
                        break;
                    default:
                        throw new AffiliateAPIException("Connection error with the Affiliate API service: HTTP/" + status);
                }


            }  catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AffiliateAPIException affiliateAPIException) {
                affiliateAPIException.printStackTrace();
            }

            return result;

        }
        private String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void parseUsingDOM(String response) throws IOException {

            String sno, amtPledged, blurb, author, country, currency, endTime, location, percentageFunded;
            String numBackers, state, title, type, url;
/*{
  "metadata": {
    "provider": "Oxford University Press"
  },
  "results": [
    {
      "id": "%E0%AE%9A%E0%AF%86%E0%AE%B5%E0%AE%BF",
      "language": "ta",
      "lexicalEntries": [
        {
          "entries": [
            {
              "homographNumber": "000",
              "senses": [
                {
                  "definitions": [
                    "காது"
                  ],
                  "examples": [
                    {
                      "text": "யானைச் செவி"
                    },
                    {
                      "text": "செவியைக் கிழிப்பதுபோல ஒரு சத்தம்"
                    }
                  ],
                  "id": "ide62c27b4-82c1-4ee0-8325-413f065bb7e7"
                }
              ]
            }
          ],
          "language": "ta",
          "lexicalCategory": "Noun",
          "text": "செவி"
        }
      ],
      "type": "headword",
      "word": "செவி"
    }
  ]
}*/
            try {
                JSONObject object = new JSONObject(response);
                JSONArray listArray = object.getJSONArray("results");
                if(listArray.length()>0) {
                    JSONObject resultObject = (JSONObject) listArray.get(0);
                    JSONArray resArray = resultObject.getJSONArray("lexicalEntries");
                    if(resArray.length()>0) {
                        JSONObject lexicalObject = (JSONObject) resArray.get(0);
                        JSONArray lexArray = lexicalObject.getJSONArray("entries");
                        if(lexArray.length()>0) {
                            JSONObject entryObject = (JSONObject) lexArray.get(0);
                            JSONArray entryArray = entryObject.getJSONArray("senses");
                            if(entryArray.length()>0) {
                                for (int i=0;i<entryArray.length();i++) {
                                    JSONObject senseObject = (JSONObject) entryArray.get(i);
                                    JSONArray defArray = senseObject.getJSONArray("definitions");

                                    String defineString = defArray.get(0).toString();
                                    String len = String.valueOf(i+1);
                                    meaningStr =  meaningStr + "♦ " + defineString + "\n";
                                    JSONArray exampleArray = senseObject.getJSONArray("examples");
                                    for (int j=0;j<exampleArray.length();j++) {
                                        JSONObject textObj  = (JSONObject) exampleArray.get(j);
                                        String exampleString = textObj.getString("text");
                                        if(j==0)
                                            meaningStr = meaningStr+ "எடுத்துக்காட்டு:" +"\n";
                                        meaningStr = meaningStr +  "\t"+ "○ " + exampleString + "\n";
                                    }
                                    meaningStr = meaningStr +"\n";
                                }
                            }
                        }
                        }
                    }
                } catch (JSONException e1) {
                e1.printStackTrace();
            }
            Log.d("Dict", "Above to fetch Array" + response);



        }





        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }


    }
}
