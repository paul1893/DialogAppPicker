package com.thefrenchtouch.pbdialogapppicker.adapters.mothers;

/*
 * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


        import java.util.ArrayList;
        import java.util.List;

        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.TextView;

        import com.thefrenchtouch.pbdialogapppicker.IIconListAdapterItem;
        import com.thefrenchtouch.pbdialogapppicker.objects.AppItem;

public class IconListAdapter extends ArrayAdapter<AppItem>
        implements BaseListAdapterFilter.IBaseListAdapterFilterable<AppItem> {
    private Context mContext;
    private List<AppItem> mData = null;
    private List<AppItem> mFilteredData = null;
    private android.widget.Filter mFilter;

    public IconListAdapter(Context context, List<AppItem> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);

        mContext = context;
        mData = new ArrayList<AppItem>(objects);
        mFilteredData = new ArrayList<AppItem>(objects);
    }

    static class ViewHolder {
        TextView text;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if(row == null) {
            LayoutInflater inflater =
                    (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

            holder = new ViewHolder();
            holder.text = (TextView) row.findViewById(android.R.id.text1);
            holder.text.setCompoundDrawablePadding(10);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        IIconListAdapterItem item = mFilteredData.get(position);

        holder.text.setText(item.getText());
        holder.text.setCompoundDrawablesWithIntrinsicBounds(
                item.getIconLeft(), null, item.getIconRight(), null);

        return row;
    }

    @Override
    public android.widget.Filter getFilter() {
        if(mFilter == null)
            mFilter = new BaseListAdapterFilter<AppItem>(this);

        return mFilter;
    }

    @Override
    public List<AppItem> getOriginalData() {
        return mData;
    }

    @Override
    public List<AppItem> getFilteredData() {
        return mFilteredData;
    }

    @Override
    public void onFilterPublishResults(List<AppItem> results) {
        mFilteredData = results;
        clear();
        for (int i = 0; i < mFilteredData.size(); i++)
        {
            AppItem item = mFilteredData.get(i);
            add(item);
        }
    }
}