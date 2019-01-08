package led_strip_control.batalov.ivan.ledstripcontrol.modules;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ivan on 6/10/16.
 */
public class CustomListViewAdapter extends BaseAdapter{

    ArrayList<LinearLayout> items;

    public void setViews(ArrayList<LinearLayout> items){
        this.items = items;
    }

    @Override
    public int getCount() {
        if(items != null){
            return items.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(items != null){
            return items.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(items != null){
            return items.get(position);
        }
        return null;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    /**
     *
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId1() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }
}
