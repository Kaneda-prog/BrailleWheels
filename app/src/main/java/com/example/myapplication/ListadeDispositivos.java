package com.example.myapplication;
import java.util.Set;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListadeDispositivos extends ListActivity {

    private BluetoothAdapter meuBluetooth;
    static String EnderecoMAC = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> ArrayBluetooth = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        //obtem o bluetooth local do dispositivo
        meuBluetooth = BluetoothAdapter.getDefaultAdapter();

        //Pega os dispositivos paredaos
        Set<BluetoothDevice> dispositivospareados = meuBluetooth.getBondedDevices();

        //Se o tamanho dos dispositivos for maior que zero, serão adicionados os dispositivos na lista
        if (dispositivospareados.size() > 0) {
            for (BluetoothDevice bluetoothDevice : dispositivospareados) {
                String nome = bluetoothDevice.getName();
                String mac = bluetoothDevice.getAddress();

                ArrayBluetooth.add(nome + "\n" + mac);

            }
        }

        setListAdapter(ArrayBluetooth);
    }

    //Método de click na lista
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);

        //Obtém todos os dados do item que foi clicado
        String InfoGeral = ((TextView) v).getText().toString();

        //Retira o endereço MAC que são os ultimos 17 caracteres da informação
        String mac = InfoGeral.substring(InfoGeral.length() - 17);

        Intent retornaMac = new Intent();
        retornaMac.putExtra(EnderecoMAC, mac);

        //Atribui o resultado como OK e fecha a lista
        setResult(RESULT_OK, retornaMac);
        finish();
    }


}