package led_strip_control.batalov.ivan.ledstripcontrol;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConnectionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ConnectionFragment extends Fragment {

    private static final String TAG = "BT Connection fragment";

    public static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 123;

    public static final int ERASABLE_MESSAGE_LABEL = 1;
    public static final int NOT_ERASABLE_MESSAGE_LABEL = 0;

    public static final int EVENT_NO_BT = 0;
    public static final int EVENT_BT_DISABLED = 1;
    public static final int EVENT_BT_CONNECTED = 2;
    public static final int EVENT_BT_RECONNECTED = 3;
    public static final int EVENT_BT_RECONNECT_FAILED = 4;
    public static final int EVENT_BT_DISCONNECTED = 5;
    public static final int EVENT_MESSAGE_SENT = 6;
    public static final int EVENT_NO_RESPONSE = 7;
    public static final int EVENT_DELIVERY_FAILED = 8;
    public static final int REQUEST_ENABLE_BT = 43813; // some random number I came up with
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private OnFragmentInteractionListener mListener;
    private BluetoothSocket mSocket;
    View topView;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothManager mBluetoothManager;
    volatile boolean connectionRequestProcessed = true;

    public ConnectionFragment() {
        // start the thread for bluetooth connections
        connectionThread = new HandlerThread("Bluetooth connection thread");
        connectionThread.start();
        Looper connectionLooper = connectionThread.getLooper();
        connectionHandler = new ConnectionHandler(connectionLooper);

        // start the data transferring thread
        dataTransferThread = new HandlerThread("Data transferring thread");
        dataTransferThread.start();
        Looper dataTransferringLooper = dataTransferThread.getLooper();
        dataTransferHandler = new DataTransferHandler(dataTransferringLooper);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(topView == null) {
            topView = inflater.inflate(R.layout.fragment_connection, container, false);
        }
        if(mBluetoothAdapter != null){
            setUpDeviceList();
        }
        return topView;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println("broadcasted action received...");
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                System.out.println("found!");
                // Get the BluetoothDevice object from the Intent
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deviceListAdapter.add(device, getActivity());
                    }
                });
            } else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                startDiscoveryButton.setText("Discovering...");
            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                startDiscoveryButton.setText("Discover devices");
            }
        }
    };

    ListView deviceListView;
    BluetoothDeviceListAdapter deviceListAdapter;
    Button startDiscoveryButton;

    private void setUpDeviceList(){
        if(deviceListView == null) {
            deviceListView = (ListView) topView.findViewById(R.id.connection_device_list);
            deviceListAdapter = new BluetoothDeviceListAdapter();
            deviceListView.setAdapter(deviceListAdapter);
            deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int index = deviceListAdapter.ids.indexOf(view.getId());
                    if (index != -1 && connectionRequestProcessed) {
                        BluetoothDevice device = deviceListAdapter.devices.get(index);
                        if(mSocket != null && mSocket.isConnected()){
                            if(device.getAddress().equals(mSocket.getRemoteDevice().getAddress())){
                                // disconnect from current device
                                Message msg = connectionHandler.obtainMessage();
                                msg.arg1 = EVENT_BT_DISCONNECTED;
                                connectionHandler.sendMessage(msg);
                            } else{
                                // disconnect from current device and reconnect to a new device
                                Message msg1 = connectionHandler.obtainMessage();
                                msg1.arg1 = EVENT_BT_DISCONNECTED;
                                connectionHandler.sendMessage(msg1);

                                Message msg2 = connectionHandler.obtainMessage();
                                msg2.arg1 = EVENT_BT_CONNECTED;
                                Bundle msgData = new Bundle();
                                msgData.putString("mac", device.getAddress());
                                msg2.setData(msgData);
                                connectionHandler.sendMessage(msg2);
                            }
                        } else{
                            // connect to a selected device
                            Message msg = connectionHandler.obtainMessage();
                            msg.arg1 = EVENT_BT_CONNECTED;
                            Bundle msgData = new Bundle();
                            msgData.putString("mac", device.getAddress());
                            msg.setData(msgData);
                            connectionHandler.sendMessage(msg);
                            System.out.println("message sent...");
                        }
                    }
                }
            });
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                deviceListAdapter.add(device, getActivity());
            }
        }

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        startDiscoveryButton = (Button) topView.findViewById(R.id.connection_discover_button);
        startDiscoveryButton.getBackground().setColorFilter(getResources().getColor(R.color.palette_color_4_neutral), PorterDuff.Mode.MULTIPLY);
        startDiscoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                }

                int hasPermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
                if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("No coarse location permission is granted");
                    if(!mBluetoothAdapter.startDiscovery()){
                        Toast.makeText(getActivity(), "Error occurred. Try again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION_PERMISSIONS);
                }

            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(prefs.contains("default_device_mac_address") && (mSocket == null || !mSocket.isConnected())) {
            String mac = prefs.getString("default_device_mac_address", "");
            Message msg = connectionHandler.obtainMessage();
            msg.arg1 = EVENT_BT_CONNECTED;
            Bundle msgData = new Bundle();
            msgData.putString("mac", mac);
            msg.setData(msgData);
            connectionHandler.sendMessage(msg);
        }
    }

    boolean connecting = false;

    public void reconnect(){
        if(mSocket != null){
            connecting = true;
            // disconnect from current device and reconnect to a new device
            Message msg1 = connectionHandler.obtainMessage();
            msg1.arg1 = EVENT_BT_DISCONNECTED;
            connectionHandler.sendMessage(msg1);

            Message msg2 = connectionHandler.obtainMessage();
            msg2.arg1 = EVENT_BT_RECONNECTED;
            Bundle msgData = new Bundle();
            msgData.putString("mac", mSocket.getRemoteDevice().getAddress());
            msg2.setData(msgData);
            connectionHandler.sendMessage(msg2);
        } else{
            Toast.makeText(getActivity(), "Device is not connected", Toast.LENGTH_SHORT).show();
            Bundle data = new Bundle();
            data.putInt("eventID", EVENT_BT_RECONNECT_FAILED);
            mListener.onFragmentInteraction(data);
        }
    }

    private HandlerThread connectionThread;
    private ConnectionHandler connectionHandler;

    private class ConnectionHandler extends Handler {
        public ConnectionHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            connectionRequestProcessed = false;

            super.handleMessage(msg);
            final int eventID = msg.arg1;

            if(eventID == EVENT_BT_DISCONNECTED){
                if(mSocket != null) {
                    final TextView statusView = (TextView) deviceListAdapter.items
                            .get(deviceListAdapter.devices.indexOf(mSocket.getRemoteDevice()))
                            .findViewById(R.id.bt_list_item_status);
                    Activity activity = getActivity();
                    if(activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusView.setText("disconnecting...");
                                statusView.setVisibility(View.VISIBLE);
                                deviceListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    try {
                        System.out.println("closing socket...");
                        mSocket.close();
                    } catch (IOException closeException) {
                        closeException.printStackTrace();
                    }
                    if(activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusView.setText(null);
                                statusView.setVisibility(View.GONE);
                                deviceListAdapter.notifyDataSetChanged();
                                Bundle data = new Bundle();
                                data.putInt("eventID", eventID);
                                mListener.onFragmentInteraction(data);
                            }
                        });
                    }
                }
            } else {
                // event is connect or reconnect
                String mac = msg.getData().getString("mac", null);
                if (mac != null) {
                    connectionRequestProcessed = false;

                    BluetoothSocket tmp = null;
                    final BluetoothDevice mmDevice = mBluetoothAdapter.getRemoteDevice(mac);

                    if(deviceListAdapter.devices.indexOf(mmDevice) < 0){
                        connectionRequestProcessed = true;
                        System.out.println("saved bluetooth device is no longer paired");
                        return;
                    }

                    final TextView statusView = (TextView) deviceListAdapter.items
                            .get(deviceListAdapter.devices.indexOf(mmDevice))
                            .findViewById(R.id.bt_list_item_status);
                    Activity activity = getActivity();
                    if(activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusView.setText("connecting...");
                                statusView.setVisibility(View.VISIBLE);
                                deviceListAdapter.notifyDataSetChanged();
                                System.out.println("setting status...");
                            }
                        });
                    }

                    // Get a BluetoothSocket to connect with the given BluetoothDevice
                    try {
                        tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(tmp != null) {
                        final BluetoothSocket mmSocket = tmp;
                        mBluetoothAdapter.cancelDiscovery();

                        try {
                            // Connect the device through the socket. This will block
                            // until it succeeds or throws an exception
                            mmSocket.connect();
                            System.out.println("connected!");
                        } catch (IOException connectException) {
                            connecting = false;
                            connectException.printStackTrace();
                            try {
                                System.out.println("closing socket...");
                                mmSocket.close();
                            } catch (IOException closeException) {
                                closeException.printStackTrace();
                            }
                        }
                        System.out.println("Sync 1. About to lock...");
                        synchronized (ConnectionFragment.this) {
                            mSocket = mmSocket.isConnected() ? mmSocket : null;
                        }
                        System.out.println("Sync 1. Lock returned...");
                        // Do work to manage the connection (in a separate thread)
                        //manageConnectedSocket(mmSocket);
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (mmSocket.isConnected()) {
                                        statusView.setText("connected");
                                        //statusView.setVisibility(View.VISIBLE); // this line is not needed as long as I make this view visible when starting the connection process
                                        Bundle data = new Bundle();
                                        data.putInt("eventID", eventID);
                                        mListener.onFragmentInteraction(data);
                                    } else {
                                        statusView.setText(null);
                                        statusView.setVisibility(View.GONE);
                                        if (eventID == EVENT_BT_RECONNECTED) {
                                            Message msg = dataTransferHandler.obtainMessage();
                                            msg.arg1 = DataTransferHandler.CLEAR_MESSAGES;
                                            dataTransferHandler.sendMessage(msg);

                                            Bundle data = new Bundle();
                                            data.putInt("eventID", EVENT_BT_RECONNECT_FAILED);
                                            mListener.onFragmentInteraction(data);

                                        }
                                    }
                                    deviceListAdapter.notifyDataSetChanged();
                                    connecting = false;
                                }
                            });
                        }
                    }
                }
            }
            connectionRequestProcessed = true;
        }
    }

    public void sendData(Bundle data){
        int label;
        if(data.getBoolean("erasable", false)){
            label = ERASABLE_MESSAGE_LABEL;
            dataTransferHandler.removeMessages(ERASABLE_MESSAGE_LABEL);
        } else{
            label = NOT_ERASABLE_MESSAGE_LABEL;
        }
        Message msg = dataTransferHandler.obtainMessage(label);
        msg.arg1 = DataTransferHandler.SEND_MESSAGE;
        msg.setData(data);

        // need to do it so I don't get a huge queue of messages because they don't get processed fast enough
        dataTransferHandler.sendMessage(msg);

        System.out.println("Data is sent to the sending thread.");
    }

    DataTransferHandler dataTransferHandler;
    HandlerThread dataTransferThread;

    private class DataTransferHandler extends Handler{
        private static final int SEND_MESSAGE = 1;
        private static final int CLEAR_MESSAGES = 2;

        private final byte[] messageOpening = "<".getBytes();
        private final byte[] messageClosure = ">".getBytes();
        private final int maxResponseTime = 500;
        private ArrayList<Bundle> messageList = new ArrayList<>();
        private ArrayList<Integer> messageErasabilities = new ArrayList<Integer>();

        public DataTransferHandler(Looper looper){
            super(looper);
        }

        /**
         *
         * @param message - should contain data with keys "message"(string) and "delay_required"(boolean)
         */
        @Override
        public void handleMessage(Message message){

            if(message.arg1 == SEND_MESSAGE){
                if(message != null && message.getData() != null && message.getData().getString("message", null) != null) {
                    Bundle data = message.getData();
                    if(data.getBoolean("erasable", false)){
                        while(messageErasabilities.contains(ERASABLE_MESSAGE_LABEL)){
                            Integer i = messageErasabilities.indexOf(ERASABLE_MESSAGE_LABEL);
                            messageErasabilities.remove(i);
                            messageList.remove(i);
                        }
                    }
                    messageList.add(data);
                    messageErasabilities.add(data.getBoolean("erasable", false) ? ERASABLE_MESSAGE_LABEL: NOT_ERASABLE_MESSAGE_LABEL);
                }
                System.out.println("socket:" + (mSocket != null) + "; connected:" + mSocket.isConnected() + "; messages:" + messageList.size() + "; messageProperties: " + messageErasabilities.size());
                synchronized (ConnectionFragment.this) {
                    if (mSocket != null && mSocket.isConnected()) {
                        if (!messageList.isEmpty()) {
                            Bundle data = messageList.get(0);
                            String string = data.getString("message", null);
                            string = string.length() + " " + string; // adding string length for the purpose of checking the message integrity upon arrival
                            byte[] msgBuffer = string.getBytes();

                            Log.d(TAG, "...Send data: " + string + "...");

                            OutputStream outStream;
                            try {
                                outStream = mSocket.getOutputStream();
                                InputStream inputStream = mSocket.getInputStream();

                                if (data.getBoolean("wait_until_receiver_ready", false)) {
                                    boolean arduinoResponded = false;
                                    long timeMillis = System.currentTimeMillis();
                                    int messagesSent = 1;

                                    outStream.write(messageOpening);

                                    while (!arduinoResponded) {
                                        if (inputStream.available() > 0 && (char) inputStream.read() == '<') {
                                            arduinoResponded = true;
                                        } else if (System.currentTimeMillis() - timeMillis > maxResponseTime) {
                                            if(messagesSent >= 10){
                                                if (mListener != null) {
                                                    data.putInt("eventID", EVENT_NO_RESPONSE);
                                                    mListener.onFragmentInteraction(data);
                                                }
                                                return;
                                            } else {
                                                timeMillis = System.currentTimeMillis();
                                                outStream.write(messageOpening);
                                                messagesSent++;
                                            }
                                        }
                                    }
                                } else {
                                    outStream.write(messageOpening);
                                }

                                // empty the input buffer
                                while (inputStream.available() > 0) {
                                    inputStream.read();
                                }

                                outStream.write(msgBuffer);
                                outStream.write(messageClosure);
                                if (data.getBoolean("verify_successful_transfer", false)) {
                                    long timeMillis = System.currentTimeMillis();
                                    boolean arduinoResponded = false;
                                    boolean transferSucceeded = false;

                                    while (!arduinoResponded) {
                                        if (inputStream.available() > 0) {

                                            if ((char) inputStream.read() == '1') {
                                                arduinoResponded = true;
                                                transferSucceeded = true;
                                                if (mListener != null) {
                                                    data.putInt("eventID", EVENT_MESSAGE_SENT);
                                                    mListener.onFragmentInteraction(data);
                                                }
                                                messageList.remove(0);
                                                messageErasabilities.remove(0);
                                                System.out.println("data reception confirmed");
                                            } else if ((char) inputStream.read() == '0') {
                                                arduinoResponded = true;
                                                transferSucceeded = false;
                                                //System.out.println("data reception failed 1");
                                            } else {
                                                System.out.println("arduino responded: " + (char) inputStream.read());
                                            }
                                        } else if (System.currentTimeMillis() - timeMillis > maxResponseTime * 5) {
                                            arduinoResponded = true; // arduino didn't respond, but we assume that at this point it failed and we need to resend the data
                                            transferSucceeded = false;
                                            //System.out.println("data reception failed 2");
                                        }
                                    }

                                    if(!transferSucceeded && mListener != null){
                                        data.putInt("eventID", EVENT_DELIVERY_FAILED);
                                        mListener.onFragmentInteraction(data);
                                    }
                                } else {
                                    messageList.remove(0);
                                    messageErasabilities.remove(0);
                                    if (mListener != null) {
                                        data.putInt("eventID", EVENT_MESSAGE_SENT);
                                        mListener.onFragmentInteraction(data);
                                    }
                                }
                                while (inputStream.available() > 0) {
                                    inputStream.read();
                                }
                            } catch (IOException e) {
                                reconnect();
                                return;
                            }
                        }
                        if (!messageList.isEmpty() && !hasMessages(ERASABLE_MESSAGE_LABEL) && !hasMessages(NOT_ERASABLE_MESSAGE_LABEL)) {
                            System.out.println("retrying to send messages 1");
                            Message msg = obtainMessage();
                            msg.arg1 = SEND_MESSAGE;
                            dataTransferHandler.sendMessage(msg);
                            System.out.println("retrying to send messages 2");
                        }
                    } else if (!connecting) {
                        reconnect();
                    }
                }

            } else if(message.arg1 == CLEAR_MESSAGES){
                //removeMessages(ERASABLE_MESSAGE_LABEL);
                //removeMessages(NOT_ERASABLE_MESSAGE_LABEL);
                messageList.clear();
                messageErasabilities.clear();
            }


        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode != Activity.RESULT_OK){
                Bundle bundle = new Bundle();
                bundle.putInt("eventID", EVENT_BT_DISABLED);
                mListener.onFragmentInteraction(bundle);
                return;
            }

            // assume now that bluetooth is on
            setUpDeviceList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == REQUEST_COARSE_LOCATION_PERMISSIONS){
            System.out.println("permission response received");
            for(int i = 0; i < permissions.length; i++){
                if(android.Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[i])){
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        if (!mBluetoothAdapter.startDiscovery()) {
                            Toast.makeText(getActivity(), "Error occurred. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    } else if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                        Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
            }
            System.out.println("coarse location permission not found...");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if(Build.VERSION.SDK_INT >= 18){
            mBluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        } else {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (mBluetoothAdapter == null) {
            Bundle data = new Bundle();
            data.putInt("eventID", EVENT_NO_BT);
            mListener.onFragmentInteraction(data);
        } else if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else if(topView != null){
            setUpDeviceList();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;

        Thread thread1 = connectionThread;
        Thread thread2 = dataTransferThread;

        thread1.interrupt();
        thread2.interrupt();

        connectionThread = null;
        dataTransferThread = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Bundle data);
    }

    public BluetoothSocket getSocket() {
        return mSocket;
    }
}

