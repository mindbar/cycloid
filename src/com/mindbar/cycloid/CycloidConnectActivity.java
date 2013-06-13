package com.mindbar.cycloid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mindbar.cycloid.helpers.CyclopusMsg;
import com.mindbar.cycloid.pojo.CyclopusStatus;

import java.io.*;
import java.util.UUID;

public class CycloidConnectActivity extends Activity {
    private static final String TAG = "cycloid";
    private static final int REQUEST_ENABLE_BT = 1;
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // MAC-address of Arduino Bluetooth module
    private static String address = "20:13:02:25:10:89";
    final int RECIEVE_MESSAGE = 1;        // Статус для Handler
    TextView txtSpeed, txtCadence, txtStatus, txtOdo, txtDist, txtMaxSpeed;
    LinearLayout layoutCadence;
    Handler h;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private BufferedReader mBufferedReader = null;
    private StringBuilder sb = new StringBuilder();
    private ConnectedThread mConnectedThread;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        txtSpeed = (TextView) findViewById(R.id.txtSpeed);
        txtMaxSpeed = (TextView) findViewById(R.id.txtMaxSpeed);
        txtCadence = (TextView) findViewById(R.id.txtCadence);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtOdo = (TextView) findViewById(R.id.txtOdometer);
        txtDist = (TextView) findViewById(R.id.txtDistance);
        layoutCadence = (LinearLayout) findViewById(R.id.layoutCadence);
        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:
                        String arduinoMsg = (String) msg.obj;
                        sb.delete(0, sb.length());
                        Log.i(TAG, arduinoMsg);
                        CyclopusStatus cs = CyclopusMsg.parseMessage(arduinoMsg);
                        if (cs == null) return;
                        txtStatus.setText(arduinoMsg);

                        txtSpeed.setText(Float.toString(cs.getSpeed()));
                        txtMaxSpeed.setText(Float.toString(cs.getMaxSpeed()));
                        txtCadence.setText(Integer.toString(cs.getCadence()));

                        txtOdo.setText(Float.toString(cs.getOdometer()));
                        txtDist.setText(Float.toString(cs.getTotalDistance()));

                        if (cs.getCadence() < 60) layoutCadence.setBackgroundColor(Color.RED);
                        else layoutCadence.setBackgroundColor(Color.GREEN);

                        break;
                }
            }


        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // local Bluetooth adapter
        checkBTState();
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume - Reconnect");

        // wakelock
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);


        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "Connection ready");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure " + e2.getMessage());
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "Creating Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "In onPause()...");

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket. " + e2.getMessage());
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not supported");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth ON");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private BufferedReader br = null;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                br = new BufferedReader(new InputStreamReader(tmpIn));
            } catch (IOException ignored) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    if (br.ready()) {
                        h.obtainMessage(RECIEVE_MESSAGE, br.readLine()).sendToTarget();
                    }
                    Thread.sleep(100);
                } catch (IOException e) {
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "Message to send: " + message);
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "Error sending message: " + e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
