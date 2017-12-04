package jani.suoranta.android.jyunioni;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * CategoryAdapter is a FragmentPagerAdapter that can provide the layout for
 * each list item based on a data source which is a list of Event objects.
 *
 * NOTE:
 * This class is my own implementation from the original code of Google.
 * The original code can be found in this course material:
 * https://www.udacity.com/course/android-basics-nanodegree-by-google--nd803
 *
 * @author Jani Suoranta 25.11.2017
 */
class CategoryAdapter extends FragmentPagerAdapter {


    /**
     * Context of the app
     */
    private Context mContext;

    /**
     * Create a new {@link CategoryAdapter} object.
     *
     * @param context is the context of the app
     * @param fm      is the fragment manager that will keep each fragment's state in the adapter across swipes.
     */
    CategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    /**
     * Return the Fragment that should be displayed for the given page number.
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            default:
                return new EventsFragment();
            case 1:
                return new ShoutboxFragment();
        }
    }

    /**
     * Return the total number of pages.
     */
    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            default:
                return mContext.getString(R.string.category_events);
            case 1:
                return mContext.getString(R.string.category_shoutbox);

        }
    }


}