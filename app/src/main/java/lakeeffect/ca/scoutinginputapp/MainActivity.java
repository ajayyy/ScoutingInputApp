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
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
        final BluetoothDevice[] devices = pairedDevices.toArray(new BluetoothDevice[0]);
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
                                    BluetoothSocket bluetoothsocket = null;
                                    try {
                                        bluetoothsocket = devices[which].createRfcommSocketToServiceRecord(UUID.fromString("6ba6afdc-6a0a-4b1d-a2bf-f71ac108b636"));
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
                                    String data = "";
                                    Log.d("HELLO", "KJLDJJLADS");
                                    while(out != null && in != null && bluetoothsocket.isConnected()){
                                        Log.d("HELLO", "");
                                        try {

                                            byte[] bytes = new byte[1000000];
                                            int amount = in.read(bytes);
                                            if(amount>0)  bytes = Arrays.copyOfRange(bytes, 0, amount);//puts data into bytes and cuts bytes
                                            else continue;
                                            String message = new String(bytes, Charset.forName("UTF-8"));
                                            if (bytes.length > 0){
                                                parse(data + message);
                                                out.write("done".getBytes(Charset.forName("UTF-8")));
                                                data = "";
                                            }else{
                                                data += message;
                                            }
                                            final byte[] bytes2 = bytes;
                                            Log.d("HELLO", "KJLDJJLADS" + bytes);

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                    }
                                    Log.d("HELLO", "KJLDJJLADS sssssss");
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

    public void parse(String data){

        File sdCard = Environment.getExternalStorageDirectory();

        File file = new File(sdCard.getPath() + "/ScoutingData/" + data.split(":")[0] + ".txt");

        data = data.split(":")[1];

        try {
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream f = new FileOutputStream(file, true);

            OutputStreamWriter out = new OutputStreamWriter(f);

            out.write(data);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Saved",
                            Toast.LENGTH_LONG).show();
                }
            });

            out.close();
            f.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
