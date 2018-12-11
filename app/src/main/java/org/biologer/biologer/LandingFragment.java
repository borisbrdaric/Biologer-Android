package org.biologer.biologer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.biologer.biologer.adapters.Adapter;
import org.biologer.biologer.bus.DeleteEntryFromList;
import org.biologer.biologer.model.Entry;
import org.biologer.biologer.model.Taxon;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class LandingFragment extends Fragment {

    public static final int REQ_CODE_NEW_ENTRY = 1001;
    private ListView list_entries;
    private Adapter adapter;
    private ArrayList<Entry> entries;
    private SwipeMenuListView listView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_landing, container, false);
        entries = (ArrayList<Entry>) App.get().getDaoSession().getEntryDao().loadAll();
        if (entries == null) {
            entries = new ArrayList<>();
        }
        adapter = new Adapter(getActivity().getApplicationContext(), entries);

        listView = rootView.findViewById(R.id.list_entries);
        listView.setAdapter(adapter);

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getActivity().getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xFF,
                        0xC2, 0xB3)));
                deleteItem.setWidth(200);
                deleteItem.setIcon(R.drawable.ic_delete);
                menu.addMenuItem(deleteItem);
            }
        };
// set creator
        listView.setMenuCreator(creator);

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // delete button
                        App.get().getDaoSession().getEntryDao().deleteByKey(adapter.getItem(position).getId());
                        entries = (ArrayList<Entry>) App.get().getDaoSession().getEntryDao().loadAll();
                        if (entries == null) {
                            entries = new ArrayList<>();
                        }
                        adapter = new Adapter(getActivity().getApplicationContext(), entries);
                        listView.setAdapter(adapter);
                        break;

                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Entry entry = adapter.getItem(position);
                long l = entry.getId();
                String m = String.valueOf(l);
                //Toast.makeText(getActivity().getApplicationContext(), m, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity().getApplicationContext(), DetailActivity.class);
                //Entry entry = adapter.getItem(position);
                intent.putExtra("extra_obj", m);
                intent.putExtra("ID_nalaza", l);
                startActivity(intent);
            }
        });

        FloatingActionButton fbtn_add = (FloatingActionButton) rootView.findViewById(R.id.fbtn_add);
        fbtn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EntryActivity.class);
                //startActivity(intent);
                startActivityForResult(intent, REQ_CODE_NEW_ENTRY);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
       EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeleteEntryFromList deleteEntryFromList) {
        adapter.addAll(App.get().getDaoSession().getEntryDao().loadAll(), true);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        entries = (ArrayList<Entry>) App.get().getDaoSession().getEntryDao().loadAll();
    }

    public void updateData() {
        entries = (ArrayList<Entry>) App.get().getDaoSession().getEntryDao().loadAll();
        if (entries == null) {
            entries = new ArrayList<>();
        }
        adapter.addAll(entries, true);
    }
}
