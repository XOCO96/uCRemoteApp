package com.xoco96.ucremoteapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import android.graphics.Color;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AnalografActivity extends AppCompatActivity {


    LineChartView lineChartView;
    String[] axisData = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
    int[] yAxisData = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    //List yAxisValues = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analograf);

        //_--------------------------------------------
        String nombre = "prueba";

        try {
            File tarjetaSD = Environment.getDataDirectory();
            File rutaarchivo = new File(tarjetaSD.getPath(), nombre);
            InputStreamReader abrirArvhivo = new InputStreamReader(openFileInput(nombre));

            BufferedReader leerArchivo = new BufferedReader(abrirArvhivo);
            String linea = leerArchivo.readLine();
            //String contenidoCompleto = "";
            int i = 0;
            while (linea != null){
                yAxisData[i] = Integer.parseInt(linea);
                //contenidoCompleto = contenidoCompleto + linea + "\n";
                linea = leerArchivo.readLine();
                i++;
            }
            leerArchivo.close();
            abrirArvhivo.close();
            //edtx_contenido.setText(contenidoCompleto);
        }catch (IOException e){
            Toast.makeText(this,"Error al leer el arvhivo", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


        //---------------------------------------------

        lineChartView = findViewById(R.id.chart);

        List yAxisValues = new ArrayList();
        List axisValues = new ArrayList();


        Line line = new Line(yAxisValues).setColor(Color.parseColor("#9C27B0"));

        for (int i = 0; i < axisData.length; i++) {
            axisValues.add(i, new AxisValue(i).setLabel(axisData[i]));
        }

        /*for (int i = 0; i < yAxisData.length; i++) {
            yAxisValues.add(new PointValue(i, yAxisData[i]));
        }*/
        for (int i = 0; i < yAxisData.length; i++) {
            yAxisValues.add(new PointValue(i, yAxisData[i]));
            //yAxisValues.set(i, new PointValue(i, yAxisData[i]));
        }

        List lines = new ArrayList();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        Axis axis = new Axis();
        axis.setValues(axisValues);
        axis.setTextSize(16);
        axis.setTextColor(Color.parseColor("#03A9F4"));
        data.setAxisXBottom(axis);

        Axis yAxis = new Axis();
        yAxis.setName("Sales in millions");
        yAxis.setTextColor(Color.parseColor("#03A9F4"));
        yAxis.setTextSize(16);
        data.setAxisYLeft(yAxis);

        lineChartView.setLineChartData(data);
        Viewport viewport = new Viewport(lineChartView.getMaximumViewport());
        viewport.top = 1100;
        lineChartView.setMaximumViewport(viewport);
        lineChartView.setCurrentViewport(viewport);

    }

    public void onClickBack(View view) {
        Intent backHome = new Intent(this,MainActivity.class);
        //startActivity(backHome);
        finish();
    }
}