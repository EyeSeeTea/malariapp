package org.eyeseetea.malariacare.layout.adapters.downloaded_media;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.Media;
import org.eyeseetea.malariacare.utils.FileIOUtils;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

public class DownloadedMediaAdapter extends
        RecyclerView.Adapter<DownloadedMediaAdapter.MediaViewHolder> {
    List<Media> items;
    Context context;
    LayoutInflater lInflater;
    protected Integer rowLayout;

    public DownloadedMediaAdapter(List<Media> items, Context context) {
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
        Media media = items.get(position);
        if (media.getFilename() != null) {
            holder.fileName.setText(removePathFromName(media.getFilename()));
        }
        if (media.getFilename() != null) {
            holder.size.setText(getSizeInMB(media.getFilename()));
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

    private String getSizeInMB(String filename) {
        String size = "0";
        try {
            File file = new File(filename);
            double fileSizeInBytes;
            if (file.exists()) {
                fileSizeInBytes = file.length();
            } else {
                fileSizeInBytes = FileIOUtils.getAssetFileDescriptorFromRaw(filename).getLength();
            }
            // Get length of file in bytes
            // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
            double fileSizeInKB = fileSizeInBytes / 1024.0;
            // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
            double fileSizeInMB = fileSizeInKB / 1024.0;
            if (fileSizeInKB < 1024.0) {
                size = fixDecimals(fileSizeInMB, "0.000");
            } else {
                size = fixDecimals(fileSizeInMB, "#.0");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return size + " MB";
    }

    private String fixDecimals(double fileSizeInMB, String format) {
        DecimalFormat df = new DecimalFormat(format);
        return df.format(fileSizeInMB);
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
