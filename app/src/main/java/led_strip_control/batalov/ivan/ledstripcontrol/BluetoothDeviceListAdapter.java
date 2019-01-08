package led_strip_control.batalov.ivan.ledstripcontrol;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ivan on 2/29/16.
 */
public class BluetoothDeviceListAdapter extends BaseAdapter {
    public ArrayList<Integer> ids = new ArrayList<Integer>();
    public ArrayList<View> items = new ArrayList<View>();
    public ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return items.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void add(BluetoothDevice device, Context context){
        if(!devices.contains(device)) {
            String name;
            String mac = null;
            if(device.getName() != null) {
                name = device.getName();
                mac = device.getAddress();
            } else{
                name = device.getAddress();
            }
            LayoutInflater inflater = LayoutInflater.from(context);
            View item = inflater.inflate(R.layout.bluetooth_device_list_item, null);

            TextView nameView = (TextView) item.findViewById(R.id.bt_list_item_name);
            nameView.setText(name);
            TextView macAddressView = (TextView) item.findViewById(R.id.bt_list_item_mac);
            macAddressView.setText(mac);

            int id = generateViewId1();
            item.setId(id);
            ids.add(id);
            items.add(item);
            devices.add(device);
            notifyDataSetChanged();
            System.out.println("device item added");
        }
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
