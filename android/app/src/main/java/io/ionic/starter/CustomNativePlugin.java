package io.ionic.starter;

import android.content.Context;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import static androidx.core.content.ContextCompat.getSystemService;

@NativePlugin()
public class CustomNativePlugin extends Plugin {
  private static final int    REQUEST_ENABLEBLUETOOTH = 1;
  private BluetoothAdapter mBluetoothAdapter;
  @PluginMethod()
  public void customCall(PluginCall call) {
    String message = call.getString("message");
    // More code here...
    call.success();
  }

  @PluginMethod()
  public void customFunction(PluginCall call) {
    // More code here...
    call.resolve();
  }
  @PluginMethod()
  public void bleInit(PluginCall call) {
    Context context=getContext();
    if(getActivity().getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE ) ){
      Toast.makeText(context,R.string.ble_is_not_supported,Toast.LENGTH_SHORT).show();
      call.reject("ble is not supported");
    }
    BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService( Context.BLUETOOTH_SERVICE );
    mBluetoothAdapter = bluetoothManager.getAdapter();
    if( null == mBluetoothAdapter )
    {    // Android端末がBluetoothをサポートしていない
      Toast.makeText( context, R.string.bluetooth_is_not_supported, Toast.LENGTH_SHORT ).show();
      call.reject("ble is not supported");
    }else{
      Toast.makeText( context, R.string.ble_is_support, Toast.LENGTH_SHORT ).show();
    }
    call.resolve();
  }
}