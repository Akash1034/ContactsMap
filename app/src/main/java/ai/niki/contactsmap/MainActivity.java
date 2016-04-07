package ai.niki.contactsmap;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    JSONArray contacts;
    JSONArray jsonArray;

    String name;
    String email;
    String phone;
    String officephone;
    String latitude;
    String longitude;
    private GoogleMap googleMap;
    ArrayList<LatLng> latLngs=new ArrayList<>();
    private MarkerOptions options = new MarkerOptions();
    ArrayList<String> names=new ArrayList<>();
    ArrayList<String> phones=new ArrayList<>();
    ArrayList<String> names1=new ArrayList<>();
    ArrayList<String> phones1=new ArrayList<>();
    ArrayList<String> emails1=new ArrayList<>();
    String listnames[];
    String listphones[];
    String listemails[];
    TabHost tabHost;
    ListView listView;
    ProgressBar spinner;
    ProgressBar progressBar;
    int progressStatus = 0;
    TextView textView;
    Handler handler = new Handler();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView=(ListView)findViewById(R.id.contact_list);
        tabHost=(TabHost)findViewById(R.id.tabHost);


        tabHost.setup();

        TabHost.TabSpec spec=tabHost.newTabSpec("All Contacts");
        spec.setContent(R.id.linearLayout);
        spec.setIndicator("All Contacts");
        tabHost.addTab(spec);

        spec=tabHost.newTabSpec("Contact Map");
        spec.setContent(R.id.linearLayout2);
        spec.setIndicator("Contact Map");
        tabHost.addTab(spec);
        // we will using AsyncTask during parsing
        new AsyncTaskParseJson().execute();
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        textView = (TextView) findViewById(R.id.textView1);
        // Start long running operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;
                    // Update the progress bar and display the
                    //current value in the text view
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                            textView.setText(progressStatus+"/"+progressBar.getMax());
                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        //Just to display the progress slowly
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();




    }

    // you can make this class as another java file so it will be separated from your main activity.
    public class AsyncTaskParseJson extends AsyncTask<String, String, String> {

        final String TAG = "AsyncTaskParseJson.java";

        // set your json string url here
        String yourJsonStringUrl = "http://private-b08d8d-nikitest.apiary-mock.com/contacts";

        // contacts JSONArray
        JSONArray dataJsonArr = null;

        @Override
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... arg0) {


            try {

                // instantiate our json parser
                JsonParser jParser = new JsonParser();
                jsonArray = jParser.getJSONFromUrl(yourJsonStringUrl);
                contacts = new JSONArray(jsonArray.getJSONObject(0).getString("contacts").toString());
                Log.i("testing", contacts.getJSONObject(1).getString("name").toString());


            } catch (Exception e) {
                Log.i("testingerror","error");
                e.printStackTrace();
            }
            saveContact(contacts);

            /*try {
                // Loading map
                initilizeMap();

            } catch (Exception e) {
                e.printStackTrace();
            }*/

            readcontacts();

            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {
            progressBar.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            Log.i("CHECK", latLngs.toString());
            int i=0;
            for (LatLng point : latLngs) {
                options.position(point);
                options.title(names.get(i));
                options.snippet(phones.get(i).toString());
                googleMap.addMarker(options);
                i++;
            }
            listnames=new String[names1.size()];
            listnames=names1.toArray(listnames);

            listphones=new String[phones1.size()];
            listphones=phones1.toArray(listphones);

            listemails=new String[emails1.size()];
            listemails=emails1.toArray(listemails);

            String details[]=new String[listnames.length];
            Arrays.fill(details," ");
            for(i=0;i<listnames.length;i++)
            {
                listnames[i]=listnames[i].concat(listphones[i]);
                details[i]=details[i].concat(listnames[i]);
            }


            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    MainActivity.this,
                    android.R.layout.simple_list_item_1,listnames
            );

            listView.setAdapter(arrayAdapter);
//            spinner.setVisibility(View.GONE);


        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap(latLngs);
    }


    public void saveContact(JSONArray array) {

        for (int i = 0; i < array.length(); i++) {
            try {
                name = array.getJSONObject(i).optString("name").toString();
                email = array.getJSONObject(i).optString("email").toString();
                phone = array.getJSONObject(i).optString("phone").toString();
                officephone = array.getJSONObject(i).optString("officePhone").toString();
                latitude = array.getJSONObject(i).optString("latitude").toString();
                longitude = array.getJSONObject(i).optString("longitude").toString();
                Log.i("NAME", name + " " + email + " " + phone + " " + officephone + " " + latitude + " " + longitude);
            } catch (JSONException e) {
                officephone = null;

                e.printStackTrace();
            }

            latLngs.add(new LatLng(Float.parseFloat(latitude), Float.parseFloat(longitude)));
            names.add(name);
            phones.add((phone));
            Log.i("LAT",latLngs.toString());
            ArrayList<ContentProviderOperation> contentProviderOperation = new ArrayList<ContentProviderOperation>();

            contentProviderOperation.add(ContentProviderOperation
                    .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            if (name != null) {
                contentProviderOperation.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                                ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                name).build());
            }

            if (phone != null) {
                contentProviderOperation.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                                ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                                phone)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());
            }

            if (officephone != null) {
                contentProviderOperation.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                                ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                                officephone)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                        .build());
            }

            if (email != null) {
                contentProviderOperation.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                                ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA,
                                email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                                ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                        .build());
            }

            try {
                getContentResolver()
                        .applyBatch(ContactsContract.AUTHORITY, contentProviderOperation);
                Log.i("CONTACT", "yes");
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("CONTACTERROR", e.toString());
                //show exception in toast
                Toast.makeText(MainActivity.this, "Exception: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }


        }
        try {
            // Loading map
            initilizeMap(latLngs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initilizeMap(ArrayList<LatLng> latLngs1) {

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();

            }
        }


    }

    public void readcontacts(){

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);


        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    names1.add(name);
                  //  Log.i("NAME",name);
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phones1.add(phone);
                //        Log.i("PHONE",phone);
                    }
                    pCur.close();

                    Cursor emailCur = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (emailCur.moveToNext()) {
                        // This would allow you get several email addresses
                        // if the email addresses were stored in an array
                        String email = emailCur.getString(
                                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        String emailType = emailCur.getString(
                                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                        emails1.add(email);
                       Log.i("EMAIL",email);
                    }
                    emailCur.close();
                }
            }
        }
    }
}
