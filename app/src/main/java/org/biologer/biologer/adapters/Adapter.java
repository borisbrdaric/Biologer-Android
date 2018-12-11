package org.biologer.biologer.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.biologer.biologer.App;
import org.biologer.biologer.R;
import org.biologer.biologer.model.Entry;
import org.biologer.biologer.model.Stage;
import org.biologer.biologer.model.StageDao;
import org.biologer.biologer.model.Taxon;
import org.biologer.biologer.model.TaxonDao;
import org.greenrobot.greendao.query.QueryBuilder;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.biologer.biologer.R.id.slika;

/**
 * Created by brjovanovic on 2/24/2018.
 */

public class Adapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Entry> mList;
    public String koristiSliku;

    public Adapter(Context mContext, ArrayList<Entry> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    public void addAll(List<Entry> list, boolean clean) {
        if (clean)
            mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Entry getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewHolder viewHolder = new ViewHolder();


        if (convertView == null) {

            convertView = inflater.inflate(R.layout.list_item, parent, false);

            viewHolder.taxon = (TextView) convertView.findViewById(R.id.taxon);
            viewHolder.stage = (TextView) convertView.findViewById(R.id.stage);
//            viewHolder.entryId = (TextView) convertView.findViewById(R.id.entryId);
            viewHolder.slika = (ImageView) convertView.findViewById(slika);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        long j = getItem(position).getTaxon();
        Taxon t = App.get().getDaoSession().getTaxonDao().load(j);
        if (t != null)
            viewHolder.taxon.setText(t.getName());

        if (getItem(position).getStage() != null) {
            long i = getItem(position).getStage();
            //Stage s = (Stage) App.get().getDaoSession().getStageDao().load(i);
            Stage s = App.get().getDaoSession().getStageDao().queryBuilder().where(StageDao.Properties.StageId.eq(i)).limit(1).unique();
            viewHolder.stage.setText(s.getName());
        } else {
            viewHolder.stage.setText("");
        }

//        long id = getItem(position).getId();
//        String entryId = String.valueOf(id);
//        viewHolder.entryId.setText(entryId);

        if (getItem(position).getSlika1() != null) {
            koristiSliku = getItem(position).getSlika1();
        } else {
            if (getItem(position).getSlika2() != null) {
                koristiSliku = getItem(position).getSlika2();
            } else {
                if (getItem(position).getSlika3() != null) {
                    koristiSliku = getItem(position).getSlika3();
                } else {
                    koristiSliku = "";
                }
            }
        }
        if (koristiSliku != null && koristiSliku.trim().length() > 0) {
            Uri myUri = Uri.parse(koristiSliku);
            //viewHolder.slika.setImageURI(myUri);
            Glide.with(convertView)
                    .load(koristiSliku)
                    .into(viewHolder.slika);
        } else {
            viewHolder.slika.setImageResource(R.mipmap.ic_kornjaca_kocka);
        }
        return convertView;
    }

    private class ViewHolder {
        TextView taxon;
        TextView stage;
        //        TextView entryId;
        ImageView slika;
    }

}
