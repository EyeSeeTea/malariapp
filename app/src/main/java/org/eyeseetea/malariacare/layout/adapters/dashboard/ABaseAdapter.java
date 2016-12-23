package org.eyeseetea.malariacare.layout.adapters.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by idelcano on 23/12/2016.
 */

public class ABaseAdapter extends BaseAdapter {
    /**
     * List of surveys to show
     */
    public List<?> items;

    /**
     * Reference to inflater (micro optimization)
     */
    protected LayoutInflater lInflater;

    /**
     * Context required to resolve strings
     */
    protected Context context;

    /**
     * The layout of the header
     */
    protected Integer headerLayout;

    /**
     * The layout of the footer
     */
    protected Integer footerLayout;

    /**
     * The layout of the record itself
     */
    protected Integer recordLayout;

    public String title;

    public ABaseAdapter(Context context) {
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(List items) {
        this.items = (List<Object>) items;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getHeaderLayout() {
        return this.headerLayout;
    }

    public Integer getFooterLayout() {
        return footerLayout;
    }

    public Integer getRecordLayout() {
        return this.recordLayout;
    }

    public Context getContext() {
        return this.context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
