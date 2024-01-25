package com.example.sapstracker.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.sapstracker.Call_View_Activity;
import com.example.sapstracker.R;

import java.util.HashSet;


public class ContactFragment extends Fragment {

    private static final int READ_CONTACTS_REQUEST_CODE = 1;

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private HashSet<String> uniqueContacts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        listView = view.findViewById(R.id.contactListView);
        adapter = new CustomContactAdapter(requireContext(), R.layout.custom_contact_item);
        listView.setAdapter(adapter);
        uniqueContacts = new HashSet<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestContactsPermission();
        } else {
            loadContactList();
        }
        return view;
    }

    private void requestContactsPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            loadContactList();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_CONTACTS_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadContactList();
        }
    }

    private void loadContactList() {
        ContentResolver contentResolver = requireContext().getContentResolver();

        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    if (uniqueContacts.add(contactName)) {
                        adapter.add(contactName);
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }
    private class CustomContactAdapter extends ArrayAdapter<String> {

        private int resource;

        public CustomContactAdapter(Context context, int resource) {
            super(context, resource);
            this.resource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(resource, parent, false);
            }

            String contactName = getItem(position);

            if (contactName != null) {
                TextView contactNameTextView = convertView.findViewById(R.id.contactName);
                ImageButton phone = convertView.findViewById(R.id.btn_call);
                phone.setOnClickListener(v -> {

                    startActivity(new Intent(requireContext(), Call_View_Activity.class));
                });
                contactNameTextView.setText(contactName);
            }

            return convertView;
        }
    }
}

