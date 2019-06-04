package hr.tvz.android.podcastclient;

import android.media.Image;
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

import java.lang.reflect.Array;
import java.util.ArrayList;

import hr.tvz.android.podcastclient.Model.Result;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.SearchResultsViewHolder> {

    private ArrayList<Result> mDataSet;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onBookmarkClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class SearchResultsViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView mImageView;
        public TextView mTextView;
        public TextView mTextView2;
        public ImageView mBookmark;

        public SearchResultsViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.searchResultsImageView);
            mTextView = itemView.findViewById(R.id.searchResultsTextViewTop);
            mTextView2 = itemView.findViewById(R.id.searchResultsTextViewBottom);
            mBookmark = itemView.findViewById(R.id.image_bookmark);

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

            mBookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onBookmarkClick(position);
                        }
                    }
                }
            });
        }
    }

    public SearchResultsAdapter(ArrayList<Result> dataSet) {
        mDataSet = dataSet;
    }

    @NonNull
    @Override
    public SearchResultsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_search_results, viewGroup, false);
        SearchResultsViewHolder vh = new SearchResultsViewHolder(v, mListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultsViewHolder searchResultsViewHolder, int i) {
        Result currentItem = mDataSet.get(i);

        ArrayList<Result> fromBase = (ArrayList<Result>) SQLite.select().from(Result.class).queryList();

        //fresco
        Uri uri = Uri.parse(currentItem.getThumbnail());
        searchResultsViewHolder.mImageView.setImageURI(uri);
        searchResultsViewHolder.mTextView.setText(currentItem.getTitle_original());
        searchResultsViewHolder.mTextView2.setText(currentItem.getDescription_original());
        if (currentItem.mBookmarked) {
            searchResultsViewHolder.mBookmark.setImageResource(R.drawable.ic_bookmarked);
        } else {
            searchResultsViewHolder.mBookmark.setImageResource(R.drawable.ic_not_bookmarked);
        }

        for(Result res : fromBase){
            if(res.getId().equals(currentItem.getId())){
                currentItem.mBookmarked=true;
                searchResultsViewHolder.mBookmark.setImageResource(R.drawable.ic_bookmarked);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
