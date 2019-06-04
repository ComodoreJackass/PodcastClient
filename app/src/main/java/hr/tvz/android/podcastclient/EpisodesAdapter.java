package hr.tvz.android.podcastclient;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;

import hr.tvz.android.podcastclient.Model.Episode;
import hr.tvz.android.podcastclient.Model.Result;

public class EpisodesAdapter extends RecyclerView.Adapter<EpisodesAdapter.EpisodesViewHolder> {
    private ArrayList<Episode> mDataSet;
    private EpisodesAdapter.OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onPlay(int position);
    }

    public void setOnItemClickListener(EpisodesAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class EpisodesViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView mImageView;
        public TextView mTextView;
        public TextView mTextView2;
        public ImageView mPlay;

        public EpisodesViewHolder(@NonNull View itemView, final EpisodesAdapter.OnItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.image_episodes);
            mTextView = itemView.findViewById(R.id.text_top_episodes);
            mTextView2 = itemView.findViewById(R.id.text_bottom_episodes);
            mPlay = itemView.findViewById(R.id.image_play);

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

            mPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onPlay(position);
                        }
                    }
                }
            });
        }
    }

    public EpisodesAdapter(ArrayList<Episode> dataSet) {
        mDataSet = dataSet;
    }

    @NonNull
    @Override
    public EpisodesAdapter.EpisodesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_episodes, viewGroup, false);
        EpisodesAdapter.EpisodesViewHolder vh = new EpisodesAdapter.EpisodesViewHolder(v, mListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodesAdapter.EpisodesViewHolder episodesViewHolder, int i) {
        Episode currentItem = mDataSet.get(i);

        ArrayList<Episode> fromBase = (ArrayList<Episode>) SQLite.select().from(Episode.class).queryList();

        //fresco
        Uri uri = Uri.parse(currentItem.getThumbnail());
        episodesViewHolder.mImageView.setImageURI(uri);
        episodesViewHolder.mTextView.setText(currentItem.getTitle());
        episodesViewHolder.mTextView2.setText(currentItem.getDescription());
        episodesViewHolder.mPlay.setImageResource(R.drawable.ic_play);

        if (currentItem.mPlaying) {
            episodesViewHolder.mPlay.setImageResource(R.drawable.ic_stop);
        } else {
            episodesViewHolder.mPlay.setImageResource(R.drawable.ic_play);
        }

        for(Episode res : fromBase){
            if(res.getId().equals(currentItem.getId())){
                currentItem.mPlaying=true;
                episodesViewHolder.mPlay.setImageResource(R.drawable.ic_stop);
            }
        }

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
