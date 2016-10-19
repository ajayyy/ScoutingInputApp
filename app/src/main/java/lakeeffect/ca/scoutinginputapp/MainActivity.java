package lakeeffect.ca.scoutinginputapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import lakeeffect.ca.scoutinginputapp.R;

public class MainActivity extends AppCompatActivity {

    TextView connected;
    int connectedDevices = 1;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        parse("2708,2,0,0,0,0,0,0,1,0,0,1,1,1,0,HELLO", this);

        final BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, 0);

        Set<BluetoothDevice> pairedDevices = ba.getBondedDevices();
        final BluetoothDevice[] devices = (BluetoothDevice[]) pairedDevices.toArray(new BluetoothDevice[0]);
        String[] names = new String[devices.length];
        if (pairedDevices.size() > 0) {
            for (int i=0;i<devices.length;i++) {
                names[i] = devices[i].getName();
            }
            new AlertDialog.Builder(this)
                    .setTitle("Which device should I connect to?")
                    .setMultiChoiceItems(names, null, new OnMultiChoiceClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, final int which, boolean isChecked) {
                            dialog.dismiss();
                            thread = new Thread(){
                                public void run(){
                                    OutputStream out = null;
                                    InputStream in = null;
                                    try {
                                        BluetoothSocket bluetoothsocket = devices[which].createRfcommSocketToServiceRecord(UUID.fromString("6ba6afdc-6a0a-4b1d-a2bf-f71ac108b636"));
                                        bluetoothsocket.connect();
                                        out = bluetoothsocket.getOutputStream();
                                        in = bluetoothsocket.getInputStream();
                                        //CONNECTED!!
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "connected!",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    while(out != null && in != null){
                                        try {

                                            Toast.makeText(MainActivity.this, "1",
                                                    Toast.LENGTH_LONG).show();
                                            byte[] bytes = new byte[1000000];
                                            Toast.makeText(MainActivity.this, "2",
                                                    Toast.LENGTH_LONG).show();
                                            int amount = in.read(bytes);
                                            if(amount>0)  bytes = Arrays.copyOfRange(bytes, 0, in.read(bytes) - 1);//puts data into bytes and cuts bytes
                                            if (bytes.length > 0){
                                                parse(String.valueOf(bytes), MainActivity.this);
                                            } else{
                                                Toast.makeText(MainActivity.this, "EMPTY",
                                                        Toast.LENGTH_LONG).show();
                                            }

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Connection Ended",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            };
                            thread.start();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "No devices paired...",
                            Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
    }

    public void parse(String in, Context context){

        final String data[] = in.split(",");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(MainActivity.this, "PARSE",
//                        Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, data[0],
                        Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, data[1],
                        Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, data[2],
                        Toast.LENGTH_LONG).show();
            }
        });
//
//        try {
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(Environment.getExternalStorageDirectory().getAbsolutePath()+""+data[0]+""+data[1]+".txt", Context.MODE_PRIVATE));
//            outputStreamWriter.write(in);
//            outputStreamWriter.close();
//        }
//        catch (IOException e) {
//            Log.e("Exception", "File write failed: " + e.toString());
//        }
    }
}
