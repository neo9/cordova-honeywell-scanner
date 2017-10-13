package com.lexitgroup.honeywell;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import android.content.Context;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.AidcManager.CreatedCallback;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReaderInfo;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.BarcodeReader.BarcodeListener;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.UnsupportedPropertyException;

import android.util.Log;
import android.widget.Toast;

public class Aidc extends CordovaPlugin implements BarcodeListener {

	private static String TAG = Aidc.class.getName();

	private BarcodeReader barcodeReader;
	private AidcManager manager;
  private String mConnectedScanner = null;

	private CallbackContext currentCallbackContext = null;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    // We get the aidc manager at the initialization of the plugin
    AidcManager.create(this.cordova.getActivity(), new CreatedCallback() {
      @Override
      public void onCreated(AidcManager aidcManager) {
        manager = aidcManager;

        if (manager != null) Log.d(TAG, "manager aidc init success");
        else Log.d(TAG, "manager aidc init failed");
      }
    });
  }

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    // This function take the name of the device to instantiate the barcodeReader
		if ("selectDevice".equals(action)) {
			Log.d(TAG, "select device");
			// We expose the current register context
			currentCallbackContext = callbackContext;
      String deviceName = (args.length() == 0 || args.getString(0).equals("null")) ? "default" : args.getString(0);

      if (manager == null) {
        callbackContext.error("No manager found please");
      } else if (deviceName != null && !deviceName.equals(mConnectedScanner)) {
        // We store the deviceName
        mConnectedScanner = deviceName;

        // We removing the old barcodeReader if exist
        if (barcodeReader != null) {
            barcodeReader.release();
            barcodeReader.close();
        }

				Log.d(TAG, "selected device" + deviceName);

        barcodeReader = (deviceName.equals("default")) ? manager.createBarcodeReader() : manager.createBarcodeReader(deviceName);

        if (barcodeReader != null) {
          // We are binding the current context for barcode events
          barcodeReader.addBarcodeListener(Aidc.this);
          callbackContext.success();
        } else {
          callbackContext.error("Failed to open barcode reader with name " + deviceName);
        }
			}

			return true;
		} else if ("claim".equals(action)) {
      // We claim the access to the device
			if (barcodeReader != null) {
				try {
					barcodeReader.claim();

					callbackContext.success();
				} catch (ScannerUnavailableException e) {
					e.printStackTrace();
					callbackContext.error("Unable to claim reader");
				}
			} else {
				callbackContext.error("Reader not open");
			}

			return true;
		} else if ("release".equals(action)) {
      // We release the barcodeReader if exist
			if (barcodeReader != null) {
				barcodeReader.release();
				callbackContext.success();
			} else {
				callbackContext.error("Reader not open");
			}

			return true;
		} else if ("register".equals(action)) {
      //We register the current callback context
      currentCallbackContext = callbackContext;

			PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			result.setKeepCallback(true);
			callbackContext.sendPluginResult(result);

			return true;
		} else if ("unregister".equals(action)) {
      // We unregister the current callback context
			currentCallbackContext = null;

			return true;
		} else if ("enableTrigger".equals(action)) {
      // Enable the device trigger (true by default)
			Log.d(TAG, "enableTrigger");

      if (barcodeReader == null) {
        callbackContext.error("no barcode reader initalized");
      } else {
        try {
				  barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
				  callbackContext.success();
			  } catch (UnsupportedPropertyException e) {
				  callbackContext.error(e.getMessage());
			  }
      }

			return true;
		} else if ("disableTrigger".equals(action)) {
      // Disable the device trigger
			Log.d(TAG, "disableTrigger");

      if (barcodeReader == null) {
        callbackContext.error("no barcode reader initalized");
      } else {
        try {
				  barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_DISABLE);
				  callbackContext.success();
			  } catch (UnsupportedPropertyException e) {
				  callbackContext.error(e.getMessage());
			  }

			  callbackContext.success();
      }

			return true;
		} else if ("listConnectedBarcodeDevices".equals(action)) {
      // Return the list of connected barcode Devices
			Log.d(TAG, "listConnectedBarcodeDevices");

      if (manager == null) {
        callbackContext.error("Unable to find aidc manager");
      } else {
				this.cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						List<BarcodeReaderInfo> list = manager.listConnectedBarcodeDevices();

						try {
							JSONObject obj = new JSONObject();
							JSONArray arrayDevices = new JSONArray();
							int index = 0;

							for (BarcodeReaderInfo value : list) {
								JSONObject objReader = new JSONObject();
								objReader.put("name", value.getFriendlyName());
								objReader.put("id", value.getName());

								arrayDevices.put(objReader);
								index++;
							}

							obj.put("devices", arrayDevices);
							callbackContext.success(obj);
						} catch (Exception x) {
							Log.e(TAG, x.getMessage());
							callbackContext.error(x.getMessage());
						}
					}
				});
      }

			return true;
		}

		return false;
	}

	@Override
	public void onDestroy() {
		if (barcodeReader != null) {
			// close BarcodeReader to clean up resources.
			barcodeReader.close();
			barcodeReader = null;
		}

		if (manager != null) {
			// close AidcManager to disconnect from the scanner service.
			// once closed, the object can no longer be used.
			manager.close();
		}

		super.onDestroy();
	}

	@Override
	public void onBarcodeEvent(BarcodeReadEvent arg0) {
		Log.d(TAG, arg0.getBarcodeData());

		if (currentCallbackContext != null) {
			try {
				JSONObject obj = new JSONObject();
				obj.put("success", true);
				obj.put("data", arg0.getBarcodeData());

				PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
				result.setKeepCallback(true);
				currentCallbackContext.sendPluginResult(result);
			} catch (Exception x) {
				Log.e(TAG, x.getMessage());
			}
		}
	}

	@Override
	public void onFailureEvent(BarcodeFailureEvent arg0) {
		Log.d(TAG, "No data");

		if (currentCallbackContext != null) {
			try {
				JSONObject obj = new JSONObject();
				obj.put("success", false);

				PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
				result.setKeepCallback(true);
				currentCallbackContext.sendPluginResult(result);
			} catch (Exception x) {
				Log.e(TAG, x.getMessage());
			}
		}
	}
}
