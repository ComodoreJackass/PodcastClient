package hr.tvz.android.podcastclient.Model;

import java.util.ArrayList;

public class Response {
    private int count;
    private ArrayList<Result> results;

    public int getCount() {
        return count;
    }

    public ArrayList<Result> getResults() {
        return results;
    }
}
