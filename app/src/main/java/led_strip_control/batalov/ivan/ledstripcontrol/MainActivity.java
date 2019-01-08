package led_strip_control.batalov.ivan.ledstripcontrol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity
        implements ConnectionFragment.OnFragmentInteractionListener,
        LedSettingsFragment.OnFragmentInteractionListener,
        LedBinPicker.OnFragmentInteractionListener{

    private static final String TAG = "Bluetooth-Arduino app";

    ConnectionFragment connectionFragment;
    LedSettingsFragment ledSettingsFragment;

    boolean preventAllFragmentTransactions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            setContentView(R.layout.main_activity_layout);
            connectionFragment = new ConnectionFragment();
            ledSettingsFragment = new LedSettingsFragment();
            if(!preventAllFragmentTransactions) {
                getFragmentManager().beginTransaction()
                        .add(R.id.main_relative_layout, ledSettingsFragment, "LedSettingsFragment")
                        .add(R.id.main_relative_layout, connectionFragment, "ConnectionFragment")
                        .detach(connectionFragment)
                        .commit();
                getFragmentManager().beginTransaction()
                        .attach(connectionFragment)
                        .addToBackStack(null)
                        .commit();
            } else{
                finish();
            }
        }
    }

    @Override
    public void onFragmentInteraction(Bundle data) {
        int eventID = data.getInt("eventID", 0);

        if(eventID == ConnectionFragment.EVENT_BT_CONNECTED){
            if(!preventAllFragmentTransactions) {
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                    System.out.println("Back stack popped.");
                } else {
                    getFragmentManager().beginTransaction().detach(connectionFragment).commit();
                    System.out.println("Fragment detached.");
                }
            }
            System.out.println("device connected");

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            final String savedMAC = prefs.getString("default_device_mac_address", "");
            if(!connectionFragment.getSocket().getRemoteDevice().getAddress().equals(savedMAC)) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                dialogBuilder.setTitle("Save this device?");
                dialogBuilder.setMessage("Your device will be connected automatically next time");
                dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("default_device_mac_address", connectionFragment.getSocket().getRemoteDevice().getAddress());
                        editor.commit();
                    }
                });
                dialogBuilder.setNegativeButton("No", null);
                dialogBuilder.create().show();
            }

        } else if(eventID == ConnectionFragment.EVENT_NO_BT){
            Toast.makeText(this, "Bluetooth is required to run this app.", Toast.LENGTH_SHORT).show();
            finish();
            
        } else if(eventID == ConnectionFragment.EVENT_BT_DISABLED){
            Toast.makeText(this, "Bluetooth connection is required to run this app.", Toast.LENGTH_SHORT).show();
            finish();

        } else if(eventID == LedSettingsFragment.SEND_DATA){
            if(connectionFragment.getSocket() != null){
                connectionFragment.sendData(data);
            } else{
                System.out.println("Can't send data. Socket is null.");
            }
        } else if(eventID == ConnectionFragment.EVENT_BT_RECONNECTED){
            data.putInt("eventID", LedSettingsFragment.SEND_DATA);
            onFragmentInteraction(data);

        } else if(eventID == ConnectionFragment.EVENT_BT_RECONNECT_FAILED){
            if(!preventAllFragmentTransactions) {
                getFragmentManager().beginTransaction().attach(connectionFragment).addToBackStack(null).commit();
            }

        } else if(eventID == LedSettingsFragment.OPEN_BLUETOOTH_CONNECTIONS){
            if(!preventAllFragmentTransactions) {
                getFragmentManager().beginTransaction().attach(connectionFragment).addToBackStack(null).commit();
            }
        } else if(eventID == ConnectionFragment.EVENT_MESSAGE_SENT){
            String message = data.getString("message", null);
            String[] array = message.split(" ");
            try {
                if (Integer.parseInt(array[0]) == 3) {
                    final int noiseDuration = Integer.parseInt(array[1]);
                    final ProgressDialog progressDialog = new ProgressDialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setTitle("Listening to noise...");
                    progressDialog.setMax(100);
                    progressDialog.show();
                    (new Thread(new Runnable() {
                        @Override
                        public void run() {
                            long start = System.currentTimeMillis();
                            while ((System.currentTimeMillis() - start) / 1000 < noiseDuration) {
                                int progress = (int) ((double) ((System.currentTimeMillis() - start)) / noiseDuration / 10);
                                progressDialog.setProgress(Math.min(progress, 100));
                            }
                            progressDialog.dismiss();
                        }
                    })).start();
                }
            } catch (NumberFormatException e){
                // do nothing...
            }
        } else if(eventID == ConnectionFragment.EVENT_NO_RESPONSE){
            System.out.println("no response from arduino");
        } else if(eventID == ConnectionFragment.EVENT_DELIVERY_FAILED){
            System.out.println("delivery failed");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ConnectionFragment.REQUEST_ENABLE_BT){
            connectionFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == ConnectionFragment.REQUEST_COARSE_LOCATION_PERMISSIONS && connectionFragment != null){
            connectionFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onPause() {
        System.out.println("paused");
        preventAllFragmentTransactions = true;
        super.onPause();
    }

    @Override
    public void onResume(){
        System.out.println("resumed");
        preventAllFragmentTransactions = false;
        super.onResume();
    }

    @Override
    public void onDestroy(){
        if(connectionFragment != null && connectionFragment.getSocket() != null){
            try {
                System.out.println("closing socket...");
                connectionFragment.getSocket().close();
                System.out.println("closed!");
            } catch (IOException e) {
                System.out.println("IO Exception, not closed.");
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() > 0 && !preventAllFragmentTransactions){
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
