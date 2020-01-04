package com.aasfencoders.womensafety;


import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aasfencoders.womensafety.Class.ContactNameClass;
import com.aasfencoders.womensafety.adapter.ContactAdapter;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        protected void onPostExecute(final ArrayList<ContactNameClass> contactList) {
            super.onPostExecute(contactList);
            progressBar.setVisibility(View.GONE);
            loadingtextview.setVisibility(View.GONE);
            ContactAdapter contactAdapter = new ContactAdapter(getBaseContext(), contactList);
            contactListView.setAdapter(contactAdapter);
            contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int position,
                                        long id) {
                    phone_number_display(contactList.get(position).getId() , contactList.get(position).getName());
                }
            });
        }

    }

    private void phone_number_display(String id, String name){

        ArrayList<String> items = new ArrayList<>();

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, ContactsContract.Contacts._ID + " = ?",
                new String[]{id}, null);
        cur.moveToFirst();
        if (cur.getInt(cur.getColumnIndex(
                ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
            Cursor pCur = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{id}, null);
            while (pCur.moveToNext()) {
                String phoneNo = pCur.getString(pCur.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneNo.replaceAll(" ","");

                boolean flagCommon = false;

                for (String item : items)
                {
                    if (item.contains(phoneNo))
                        flagCommon = true;
                }
                if(!flagCommon){
                    items.add(phoneNo);
                }

            }
            pCur.close();


            new LovelyChoiceDialog(this, R.style.TintTheme)
                    .setTopColorRes(R.color.dialogColour)
                    .setTitle(name)
                    .setIcon(R.drawable.ic_contact_phone_white_24dp)
                    .setItemsMultiChoice(items, new LovelyChoiceDialog.OnItemsSelectedListener<String>() {
                        @Override
                        public void onItemsSelected(List<Integer> positions, List<String> items) {
                            if(items.isEmpty()){
                                Toast.makeText(getBaseContext(),R.string.noContactSelected , Toast.LENGTH_SHORT).show();
                            }else{
                                // update database choosing from list
                            }
                        }
                    })
                    .setConfirmButtonText(R.string.confirm)
                    .show();



        }else {
            Toast.makeText(getBaseContext(),R.string.noPhoneNumberPresent , Toast.LENGTH_SHORT).show();
            cur.close();
        }
    }

}
