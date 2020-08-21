package io.ionic.starter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import android.os.Handler;
import java.util.HashMap;
import java.util.UUID;

@NativePlugin()
public class CustomNativePlugin extends Plugin {
  private static final int REQUEST_ENABLE_BT = 1;
  private BluetoothAdapter bluetoothAdapter;
  @PluginMethod()
  public void init(PluginCall call) {
    Context context = getContext();
    if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      Toast.makeText(context, R.string.ble_is_not_supported, Toast.LENGTH_SHORT).show();
      call.reject("ble is not supported");
    }else {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            saveCall(call);
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(call, enableBtIntent, REQUEST_ENABLE_BT);
        }
        call.resolve();
    }
  }
  @Override
  protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
    super.handleOnActivityResult(requestCode, resultCode, data);
    PluginCall savedCall = getSavedCall();
    if (savedCall == null) {
      return;
    }
    if (requestCode == REQUEST_ENABLE_BT) {
      savedCall.resolve();
    } else {
      savedCall.reject("unknown ActivityResult");
    }
  }
  private HashMap<String,String> devices=new HashMap<String,String>();
  private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
    @Override
    public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord){
    getActivity().runOnUiThread(new Runnable() {
    @Override
      public void run() {
          String address=bluetoothDevice.getAddress();
          String name=bluetoothDevice.getName();
          name=name==null?"名無し":name;
          if(devices.get(address)==null) {
              devices.put(address,name);
              JSObject device = new JSObject();
              device.put(address, name);
              notifyListeners("scan", device);
              Log.d("scan", name+" device lookup");
          }
      }
  });
      }
  };

  @PluginMethod()
  public void scan(PluginCall call) {
      if (bluetoothAdapter == null) {
          this.init(call);
      } else {
          if (bluetoothAdapter.isEnabled()) {
              new Handler().postDelayed(new Runnable() {
                  @Override
                  public void run() {
                      bluetoothAdapter.stopLeScan(leScanCallback);
                      Log.d("scan", "scan stop success");
                  }
              }, 5000);
              bluetoothAdapter.startLeScan(leScanCallback);
          } else {
              this.init(call);
          }
      }
      call.resolve();
  }

    private BluetoothGatt bluetoothGatt;
    private int connectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public final static String ACTION_GATT_CONNECTED ="com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED ="com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED ="com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE ="com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA ="com.example.bluetooth.le.EXTRA_DATA";
    //public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    private static final UUID UUID_SERVICE_PRIVATE         = UUID.fromString("d5875408-fa51-4763-a75d-7d33cecebc31");//( "FF6B1160-8FE6-11E7-ABC4-CEC278B6B50A" );
    private static final UUID UUID_CHARACTERISTIC_RX = UUID.fromString("a4f01d8c-a037-43b6-9050-1876a8c23584");//( "FF6B1426-8FE6-11E7-ABC4-CEC278B6B50A" );
    private static final UUID UUID_CHARACTERISTIC_TX = UUID.fromString("a4f01d8c-a037-43b6-9050-1876a8c23584");//( "FF6B1548-8FE6-11E7-ABC4-CEC278B6B50A" );
    private static final UUID UUID_NOTIFY = UUID.fromString( "00002902-0000-1000-8000-00805f9b34fb" );
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                bluetoothGatt.discoverServices();
                Log.d("gatt", "Connected to GATT server.");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                Log.d("gat", "Disconnected from GATT server.");
                gatt.connect();
            }
        }
        @Override
        public void onServicesDiscovered( BluetoothGatt gatt, int status ){
            if( BluetoothGatt.GATT_SUCCESS != status ){
                return;
            }
            // 発見されたサービスのループ
            for( BluetoothGattService service : gatt.getServices() ){
                if( ( null == service ) || ( null == service.getUuid() ) ) {// サービスごとに個別の処理
                    Log.d("gatt","service uuid");
                    continue;
                }
                if( UUID_SERVICE_PRIVATE.equals( service.getUuid() ) ){    // プライベートサービス
                    BluetoothGattCharacteristic characteristic=service.getCharacteristic(UUID_CHARACTERISTIC_TX);
                    gatt.setCharacteristicNotification(characteristic,true);
                    BluetoothGattDescriptor descriptor=characteristic.getDescriptor(UUID_NOTIFY);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                    Log.d("gatt","private service");
                }
            }
        }
        @Override
        public void onCharacteristicChanged( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic ){
            if(UUID_CHARACTERISTIC_TX.equals(characteristic.getUuid())){
                final String val=characteristic.getStringValue(0);
                Log.d("caracteristic","notify value:" + val);
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("caracteristic","write");
        }
    };
    @PluginMethod()
    public void gatt(PluginCall call) {
        String mac = call.getString("mac");
        if (mac.equals("")||bluetoothGatt!=null) {
            Log.d("gatt","gatt fail");
            call.resolve();
        }
        BluetoothDevice device=bluetoothAdapter.getRemoteDevice(mac);
        bluetoothGatt = device.connectGatt(getContext(), false, gattCallback);
        call.resolve();
    }
    @PluginMethod()
    public void write(PluginCall call) {
        String val = call.getString("val");
        BluetoothGattCharacteristic characteristic=bluetoothGatt.getService(UUID_SERVICE_PRIVATE).getCharacteristic(UUID_CHARACTERISTIC_RX);
        characteristic.setValue(val);
        bluetoothGatt.writeCharacteristic(characteristic);
        call.resolve();
    }
}