package org.eyeseetea.malariacare.layout.adapters.sectionDetail;

import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class SectionDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static int SECTION_TYPE = 0;
    private static int ROW_TYPE = 1;

    protected abstract int getSectionsCount();

    protected abstract int getItemsCountInSection(int section);

    protected abstract RecyclerView.ViewHolder onCreateSectionViewHolder(ViewGroup parent);

    protected abstract RecyclerView.ViewHolder onCreateRowViewHolder(ViewGroup parent);

    protected abstract void onBindSectionViewHolder(
            RecyclerView.ViewHolder holder, int sectionPosition);

    protected abstract void onBindRowViewHolder(
            RecyclerView.ViewHolder holder, int sectionPosition,
            int rowPositionInSection);

    private List<Object> items =null;

    protected void refreshData() {
        items = null;
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            items = new ArrayList<>();

            int sectionsCount = getSectionsCount();

            for (int sectionPosition = 0;sectionPosition<sectionsCount;sectionPosition++){
                items.add("Section$sectionPosition");

                int itemsCountInSection = getItemsCountInSection(sectionPosition);

                for (int itemPosition = 0;itemPosition<itemsCountInSection;itemPosition++){
                    items.add(itemPosition);
                }
            }
        }

        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);

        if (item instanceof String){
            return SECTION_TYPE;
        }else {
            return ROW_TYPE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SECTION_TYPE) {
            return onCreateSectionViewHolder(parent);
        } else{
            return onCreateRowViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);
        int sectionPosition = getSectionPosition(position);
        int rowPositionInSection = getRowPositionInSection(position);

        if (viewType == SECTION_TYPE) {
            onBindSectionViewHolder(holder, sectionPosition);
        } else{
            onBindRowViewHolder(holder, sectionPosition, rowPositionInSection);
        }
    }

    private int getSectionPosition(int position) {
        int sectionPosition = -1;

        for (int i = 0;i<=position;i++){
            Object item = this.items.get(i);

            if (item instanceof String){
                sectionPosition += 1;
            }
        }

        return sectionPosition;
    }

    private int getRowPositionInSection(int position) {
        int rowPositionInSection = -1;

        for (int i = 0;i<=position;i++){
            Object item = this.items.get(i);

            if (item instanceof String){
                rowPositionInSection = -1;
            }else{
                rowPositionInSection += 1;
            }
        }

        return rowPositionInSection;
    }
}
