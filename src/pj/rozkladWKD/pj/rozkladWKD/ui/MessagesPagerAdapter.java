package pj.rozkladWKD.pj.rozkladWKD.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import pj.rozkladWKD.Prefs;
import pj.rozkladWKD.R;

/**
 * Created with IntelliJ IDEA.
 * User: paweljaneczek
 * Date: 31.05.2012
 * Time: 12:42
 * To change this template use File | Settings | File Templates.
 */
public class MessagesPagerAdapter extends FragmentPagerAdapter {
    public static final String MESSAGE_EVENTS = "EVS";

    public static final String MESSAGE_WKD = "WKD";

    public static final String MESSAGE_APP = "APP";


    public static final String[] messageTypes = {MESSAGE_EVENTS, MESSAGE_WKD, MESSAGE_APP};


    public MessagesFragment[] fragments = new MessagesFragment[3];

    private Context context;

    public MessagesPagerAdapter( Context context, FragmentManager fm)
    {
        super(fm);
        this.context = context;

        fragments[0] = new MessagesFragment(messageTypes[0]);
        fragments[1] = new MessagesFragment(messageTypes[1]);
        fragments[2] = new MessagesFragment(messageTypes[2]);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        final String type = messageTypes[position];
        int messages = Prefs.getNotificationMessageNumber(context, type);
        if(type.equals(MESSAGE_EVENTS)) {
            return context.getString(R.string.title_events) + ((messages >0) ? (" (" + messages + ")") : "");
        } else if(type.equals(MESSAGE_WKD)) {
            return context.getString(R.string.title_wkd) + ((messages >0) ? (" (" + messages + ")") : "");
        } else if(type.equals(MESSAGE_APP)) {
            return context.getString(R.string.title_app) + ((messages >0) ? (" (" + messages + ")") : "");
        }
        return null;
    }


    @Override
    public int getCount()
    {
        return 3;
    }

    @Override
    public Fragment getItem(int i) {
        return fragments[i];
    }

    @Override
    public Object instantiateItem( ViewGroup view, int position )
    {
        return super.instantiateItem(view, position);
    }

}
