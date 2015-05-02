package com.kekiel.test.btlelist;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    private BluetoothAdapter mBluetoothAdapter = null;
    private boolean mScanning = false;
    private Handler mHandler = new Handler();
    private ListAdapter mLeDeviceListAdapter;
    private static final long SCAN_TIMEOUT = 5000;

    static class ViewHolder {
        public TextView text;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init BT
        final BluetoothManager BluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = BluetoothManager.getAdapter();

        mLeDeviceListAdapter = new ListAdapter();
        ListView listView = (ListView) this.findViewById(R.id.deviceList);
        listView.setAdapter(mLeDeviceListAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setScanState(boolean value) {
        mScanning = value;
        setProgressBarIndeterminateVisibility(value);
        ((Button) this.findViewById(R.id.scanButton)).setText(value ? "Stop" : "Scan");
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // scan for SCAN_TIMEOUT
            mHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            setScanState(false);
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        }
                    }, SCAN_TIMEOUT );
            setScanState(true);
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();
            // link loss service UUID
            UUID[] uuids = new UUID[1];
            uuids[0] = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
            mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
        } else {
            setScanState(false);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    public void onScan(View view){
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return;
        }
        scanLeDevice(!mScanning);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    }
            );
        }
    };


    private class ListAdapter extends BaseAdapter {

        private ArrayList<BluetoothDevice> mLeDevices;

        public ListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return mLeDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = MainActivity.this.getLayoutInflater().inflate(R.layout.list_row, null);
                viewHolder = new ViewHolder();
                viewHolder.text = (TextView) convertView.findViewById(R.id.textView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.text.setText(deviceName);
            } else {
                viewHolder.text.setText("unknown");
            }
            return convertView;
        }
    }
}
