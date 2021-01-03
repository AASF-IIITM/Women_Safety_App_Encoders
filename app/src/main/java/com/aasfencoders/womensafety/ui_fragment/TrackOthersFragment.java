package com.aasfencoders.womensafety.ui_fragment;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.aasfencoders.womensafety.MapsActivity;
import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.adapter.ConnectionCursorAdapter;
import com.aasfencoders.womensafety.data.DataContract;

// Track Other Fragment of the application, where we get the list of the contacts who are currently in danger.
public class TrackOthersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private View view;
    private ListView listView;
    private ConnectionCursorAdapter mCursorAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trackothers, container, false);
        if (getContext() != null) {
            mCursorAdapter = new ConnectionCursorAdapter(getContext(), null);
        }
        ColorDrawable cd = new ColorDrawable(0xFF3F51B5);
        if (getActivity() != null) {
            Window window = getActivity().getWindow();
            window.setStatusBarColor(Color.parseColor("#2e3b84"));
            ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(cd);
        }

        // set the view and the adapter
        view = (View) root.findViewById(R.id.empty_connection_view);
        listView = (ListView) root.findViewById(R.id.listOfInvitedConnections);
        listView.setEmptyView(view);
        listView.setAdapter(mCursorAdapter);

        // when we click on some connection from the Adapter List,
        // we need to open [MapsActivity.java] with the selected contact URI.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), MapsActivity.class);
                Uri currentToDoUri = ContentUris.withAppendedId(DataContract.DataEntry.CONTENT_URI, l);
                intent.setData(currentToDoUri);
                intent.putExtra("to_map", 0);
                startActivity(intent);
            }
        });

        if (getActivity() != null) {
            getActivity().getSupportLoaderManager().initLoader(1, null, this);
        }
        return root;
    }

    // Query the database to find some matched contacts whose STATUS = 1, i.e, they are currently in danger
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                DataContract.DataEntry._ID,
                DataContract.DataEntry.COLUMN_NAME,
                DataContract.DataEntry.COLUMN_PHONE,
                DataContract.DataEntry.COLUMN_STAMP};


        if (getContext() != null) {
            String selection = DataContract.DataEntry.COLUMN_STATUS + " =? ";
            String[] selectionArgs = new String[]{getString(R.string.one)};
            return new CursorLoader(getContext(), DataContract.DataEntry.CONTENT_URI, projection, selection, selectionArgs, null);
        } else {
            return null;
        }
    }

    // Once the query is completed, we populate the adapter with the dangered connection details.
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    // to reset the adapter
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
