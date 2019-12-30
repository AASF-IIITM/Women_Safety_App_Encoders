package com.aasfencoders.womensafety;


import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aasfencoders.womensafety.Class.ContactNameClass;
import com.aasfencoders.womensafety.adapter.ContactAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.security.AccessController.getContext;

public class ContactActivity extends AppCompatActivity {

    private ListView contactListView;
    private ProgressBar progressBar;
    private TextView loadingtextview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        contactListView = findViewById(R.id.contactNameListView);
        progressBar = findViewById(R.id.progress);
        loadingtextview = findViewById(R.id.progressTextViewContacts);

        progressBar.setVisibility(View.VISIBLE);
        loadingtextview.setVisibility(View.VISIBLE);
        FetchContactAsyncTask task = new FetchContactAsyncTask();
        task.execute();
        getContactList();
    }

    private void getContactList() {

    }

    private class FetchContactAsyncTask extends AsyncTask<String, Integer, ArrayList<ContactNameClass>> {
        @Override
        protected ArrayList<ContactNameClass> doInBackground(String... args) {

            ArrayList<ContactNameClass> contactList = new ArrayList<ContactNameClass>();

            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

            if ((cur != null ? cur.getCount() : 0) > 0) {
                while (cur != null && cur.moveToNext()) {
                    String id = cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME));
                    contactList.add( new ContactNameClass(id,name));
                }
                Collections.sort(contactList, new Comparator<ContactNameClass>(){
                    @Override
                    public int compare(ContactNameClass obj1, ContactNameClass obj2) {
                        return obj1.getName().compareToIgnoreCase(obj2.getName());
                    }
                });
            }
            if (cur != null) {
                cur.close();
            }
            return contactList;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(ArrayList<ContactNameClass> contactList) {
            super.onPostExecute(contactList);
            progressBar.setVisibility(View.GONE);
            loadingtextview.setVisibility(View.GONE);
            ContactAdapter contactAdapter = new ContactAdapter(getBaseContext(), contactList);
            contactListView.setAdapter(contactAdapter);
        }

    }

}
