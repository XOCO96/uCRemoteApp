package com.xoco96.ucremoteapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";

    UsbDevice device;
    UsbManager usbManager;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;

    int ig = 0;

    //EditText TextEntrada;
    //Button BtnIniciar, BtnEnviar, BtnDetener, BtnBorrar;
    ImageButton BtnIniciar, BtnDetener, BtnBorrar, BtnDigital, BtnAnalog, BtnTimer, BtnPuerto;
    TextView TxVwPantalla;
    boolean grafView = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        usbManager = (UsbManager)getSystemService(this.USB_SERVICE);
        //TextEntrada = (EditText)findViewById(R.id.editTextInput);
        BtnIniciar = (ImageButton)findViewById(R.id.iB_usbon);
        BtnDetener = (ImageButton) findViewById(R.id.iB_usboff);
        BtnBorrar = (ImageButton) findViewById(R.id.iB_borrador);
        BtnDigital = (ImageButton) findViewById(R.id.iB_digital);
        BtnAnalog = (ImageButton) findViewById(R.id.iB_analog);
        BtnTimer = (ImageButton) findViewById(R.id.iB_timer);
        BtnPuerto = (ImageButton) findViewById(R.id.iB_puert);
        TxVwPantalla = (TextView)findViewById(R.id.tV_display);
        TxVwPantalla.setMovementMethod(new ScrollingMovementMethod());

        setUiEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver,filter);
    }// final de onCreate


    public void onClickStart(View view) {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()){
            boolean keep = true;
            for (Map.Entry<String, UsbDevice>entry:usbDevices.entrySet()){
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if(deviceVID == 6790 || deviceVID == 1659)// Arduino Vendor ID
                //if(deviceVID == 0x2341)// Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this,0, new Intent(ACTION_USB_PERMISSION),0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                }else{
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }
    }// Termina onclickStart


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // Broadcast Receiver to automatically
        // start and stop the serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)){
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted){
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device,connection);
                    if (serialPort != null){
                        if (serialPort.open()){ // Set serial conection parameters
                            setUiEnabled(true); // Enable Buttons in UI
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallBack);
                            tvAppend(TxVwPantalla, "\nSerial connection Opened!\n");
                            Toast.makeText(context, "Serial connection Opened!", Toast.LENGTH_SHORT).show();
                        }else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                            Toast.makeText(context, "PORT NOT OPEN", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Log.d("SERIAL", "PORT IS NULL");
                        Toast.makeText(context, "PORT IS NULL", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Log.d("SERIAL", "PERM NOT GRANTED");
                    Toast.makeText(context, "PERM NOT GRANTED", Toast.LENGTH_SHORT).show();
                }
            }else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                onClickStart(BtnIniciar);
            }else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
                onClickStop(BtnDetener);
            }
        };
    };


    UsbSerialInterface.UsbReadCallback mCallBack = new UsbSerialInterface.UsbReadCallback(){
        // DEfining CallBack which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0){
            String data = null;
            //int ig = 0;
            try {
                //int i= 0;
                //Toast.makeText(getApplicationContext(),"Try de mcallback", Toast.LENGTH_SHORT).show(); //causa error en la ejecucion este toasd
                data = new String(arg0, "UTF-8");
                data.concat("\n");
                tvAppend(TxVwPantalla, data);
                if (grafView){
                    ig++;
                }

            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            if (grafView & ig==10){ //falta habilitar que se ejecute a los 10 datos
            //if (grafView ){ //falta habilitar que se ejecute a los 10 datos
                grafView = false;
                ig=0;
                guardarDatos();
            }
        }
    };


    private void tvAppend(TextView tv, CharSequence text){
        final TextView ftv = tv;
        final CharSequence ftext = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }


    public void onClickStop(View view) {
        setUiEnabled(false);
        serialPort.close();
        tvAppend(TxVwPantalla,"\nSerial Connection Closed! \n");
    }


    public void onClickClear(View view) {
        TxVwPantalla.setText(" ");
    }


    public void setUiEnabled(boolean bool){
        BtnIniciar.setEnabled(!bool);
        //BtnEnviar.setEnabled(bool);
        BtnDetener.setEnabled(bool);
        BtnDigital.setEnabled(bool);
        BtnAnalog.setEnabled(bool);
        BtnTimer.setEnabled(bool);
        BtnPuerto.setEnabled(bool);
        TxVwPantalla.setEnabled(bool);

    }

    public void onClicklDigital(View view) {
        String string ="D";
        serialPort.write(string.getBytes());
        tvAppend(TxVwPantalla, "\nFunción Digital habilitada. \n");

        grafView = false;
    }

    public void onClickTimer(View view) {
        String string ="R";
        serialPort.write(string.getBytes());
        tvAppend(TxVwPantalla, "\nFunción de Timer habilitada. \n");

        grafView = false;
    }

    public void onClickPuerto(View view) {
        String string ="P";
        serialPort.write(string.getBytes());
        tvAppend(TxVwPantalla, "\nInterrupción por Puerto habilitada. \n");

        grafView = false;
    }

    public void onClickAnalog(View view) {
        String string ="A";
        tvAppend(TxVwPantalla, "\nFunción Analógica habilitada. \n");
        TxVwPantalla.setText("");
        serialPort.write(string.getBytes());
        //Intent actGrafica = new Intent(this,AnalografActivity.class);
        //startActivity(actGrafica);
        grafView = true;
        ig=0;
    }


    public void guardarDatos(){

        String nombre = "prueba";
        String contenido = TxVwPantalla.getText().toString();

        try {
            File tarjetaSD = Environment.getDataDirectory();
            //Toast.makeText(getApplicationContext(),tarjetaSD.getPath(), Toast.LENGTH_SHORT).show();
            File rutaarchivo = new File(tarjetaSD.getPath(), nombre);

            OutputStreamWriter crearArchivo = new OutputStreamWriter(openFileOutput(nombre, Activity.MODE_PRIVATE));

            crearArchivo.write(contenido);
            crearArchivo.flush();
            crearArchivo.close();

            //Toast.makeText(this,"Guardado correctamente", Toast.LENGTH_SHORT).show();
            //edtx_nombre.setText("");
            //TxVwPantalla.setText("Guardado correctamente");
            tvAppend(TxVwPantalla, "\nGuardado correctamente. \n");

            Graficar();
            //Intent actGrafica = new Intent(this,AnalografActivity.class);
            //startActivity(actGrafica);
        }catch (IOException e){
            //Toast.makeText(this,"No se pudo guardar", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }


    public void Graficar(){
        Intent actGrafica = new Intent(this,AnalografActivity.class);
        startActivity(actGrafica);
        //Intent bhnj = new Intent(getApplicationContext(),AnalografActivity.class);
    }

}