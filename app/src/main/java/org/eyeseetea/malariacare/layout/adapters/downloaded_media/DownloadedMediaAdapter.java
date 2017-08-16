package org.eyeseetea.malariacare.layout.adapters.downloaded_media;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.MediaDB;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.sdk.common.FileUtils;
import java.util.List;

public class DownloadedMediaAdapter extends
        RecyclerView.Adapter<DownloadedMediaAdapter.MediaViewHolder> {
    List<MediaDB> items;
    Context context;
    LayoutInflater lInflater;
    protected Integer rowLayout;

    public DownloadedMediaAdapter(List<MediaDB> items, Context context) {
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.rowLayout = R.layout.download_media_row;
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.download_media_row, parent, false);

        return new MediaViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(MediaViewHolder holder, int position) {
        MediaDB media = items.get(position);
        if (media.getFilename() != null) {
            holder.fileName.setText(FileUtils.removePathFromName(media.getFilename()));
        }
        if (media.getFilename() != null) {
            holder.size.setText(FileUtils.getSizeInMB(media.getFilename(), context));
        }
        if (media.isPicture()) {
            holder.icon.setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.ic_image_black_18dp));
        }
        if (media.isVideo()) {
            holder.icon.setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.ic_movie_black_18dp));
        }
    }

    private String removePathFromName(String filename) {
        return filename.substring(filename.lastIndexOf("/") + 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getItemCount() {
        return items.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final CustomTextView fileName;
        private final CustomTextView size;

        public MediaViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            fileName = (CustomTextView) itemView.findViewById(R.id.filename);
            size = (CustomTextView) itemView.findViewById(R.id.size);
        }
    }
}
