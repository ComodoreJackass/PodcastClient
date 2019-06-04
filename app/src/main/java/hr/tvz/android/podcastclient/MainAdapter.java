package hr.tvz.android.podcastclient;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import hr.tvz.android.podcastclient.Model.Result;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {
    private ArrayList<Result> mDataSet;
    private MainAdapter.OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(MainAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView mImageView;

        public MainViewHolder(@NonNull View itemView, final MainAdapter.OnItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.image_main);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public MainAdapter(ArrayList<Result> dataSet) {
        mDataSet = dataSet;
    }

    @NonNull
    @Override
    public MainAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_activity_main, viewGroup, false);
        MainAdapter.MainViewHolder vh = new MainAdapter.MainViewHolder(v, mListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.MainViewHolder mainViewHolder, int i) {
        Result currentItem = mDataSet.get(i);

        //fresco
        Uri uri = Uri.parse(currentItem.getThumbnail());
        mainViewHolder.mImageView.setImageURI(uri);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
