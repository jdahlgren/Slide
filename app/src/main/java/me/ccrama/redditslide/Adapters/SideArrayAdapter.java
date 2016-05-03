package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SantitizeField;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;


/**
 * Created by ccrama on 8/17/2015.
 */
public class SideArrayAdapter extends ArrayAdapter<String> {
    private final List<String> objects;
    private Filter filter;
    public ArrayList<String> baseItems;
    public ArrayList<String> fitems;
    public ListView parentL;
    public boolean openInSubView = true;

    public SideArrayAdapter(Context context, ArrayList<String> objects, ArrayList<String> allSubreddits, ListView view) {
        super(context, 0, objects);
        this.objects = new ArrayList<>(allSubreddits);
        filter = new SubFilter();
        fitems = new ArrayList<>(objects);
        baseItems = new ArrayList<>(objects);
        parentL = view;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public Filter getFilter() {

        if (filter == null) {
            filter = new SubFilter();
        }
        return filter;
    }

    int height;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (position < fitems.size()) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.subforsublist, parent, false);

            final TextView t =
                    ((TextView) convertView.findViewById(R.id.name));
            t.setText(fitems.get(position));

            if (height == 0) {
                final View finalConvertView = convertView;
                convertView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        height = finalConvertView.getHeight();
                        finalConvertView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }

            final String subreddit = fitems.get(position).contains("+") ? fitems.get(position) : SantitizeField.sanitizeString(fitems.get(position).replace(getContext().getString(R.string.search_goto) + " ", ""));
            convertView.findViewById(R.id.color).setBackgroundResource(R.drawable.circle);
            convertView.findViewById(R.id.color).getBackground().setColorFilter(Palette.getColor(subreddit), PorterDuff.Mode.MULTIPLY);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (fitems.get(position).startsWith(getContext().getString(R.string.search_goto) + " ") || !((MainActivity) getContext()).usedArray.contains(fitems.get(position))) {
                        Intent inte = new Intent(getContext(), SubredditView.class);
                        inte.putExtra(SubredditView.EXTRA_SUBREDDIT, subreddit);
                        ((Activity) getContext()).startActivityForResult(inte, 4);
                    } else {
                        if (((MainActivity) getContext()).commentPager && ((MainActivity) getContext()).adapter instanceof MainActivity.OverviewPagerAdapterComment) {
                            ((MainActivity) getContext()).openingComments = null;
                            ((MainActivity) getContext()).toOpenComments = -1;
                            ((MainActivity.OverviewPagerAdapterComment) ((MainActivity) getContext()).adapter).size = (((MainActivity) getContext()).usedArray.size() + 1);
                            ((MainActivity) getContext()).adapter.notifyDataSetChanged();
                            ((MainActivity) getContext()).doPageSelectedComments(((MainActivity) getContext()).usedArray.indexOf(fitems.get(position)));
                        }
                        ((MainActivity) getContext()).pager.setCurrentItem(((MainActivity) getContext()).usedArray.indexOf(fitems.get(position)));
                    }

                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    ((MainActivity) getContext()).drawerLayout.closeDrawers();

                }
            });
        } else {
            if ((fitems.size() * height) < parentL.getHeight() && (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_DRAWER)) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spacer, parent, false);
                ViewGroup.LayoutParams params = convertView.findViewById(R.id.height).getLayoutParams();
                params.height = (parentL.getHeight() - (getCount() - 1) * height);
                convertView.setLayoutParams(params);
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spacer, parent, false);
                ViewGroup.LayoutParams params = convertView.findViewById(R.id.height).getLayoutParams();
                params.height = 0;
                convertView.setLayoutParams(params);
            }
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return fitems.size() + 1;
    }

    private class SubFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            String prefix = constraint.toString().toLowerCase();

            if (prefix == null || prefix.length() == 0) {
                ArrayList<String> list = new ArrayList<>(baseItems);
                results.values = list;
                results.count = list.size();
            } else {
                openInSubView = true;
                final ArrayList<String> list = new ArrayList<>(objects);
                final ArrayList<String> nlist = new ArrayList<>();

                for (String sub : list) {
                    if (sub.contains(prefix))
                        nlist.add(sub);
                    if (sub.equals(prefix))
                        openInSubView = false;
                }
                if (openInSubView) {
                    nlist.add(getContext().getString(R.string.search_goto) + " " + prefix);
                }

                results.values = nlist;
                results.count = nlist.size();
            }
            return results;
        }


        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            fitems = (ArrayList<String>) results.values;
            notifyDataSetChanged();
            clear();
            if (fitems != null) {
                int count = fitems.size();
                for (int i = 0; i < count; i++) {
                    add(fitems.get(i));
                    notifyDataSetInvalidated();
                }
            }
        }
    }
}