package co.gladminds.bajajcvl.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import co.gladminds.bajajcvl.interphace.OnConnectedListener;
import co.gladminds.bajajcvl.interphace.OnResponseListener;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpRequestUpload {
	
	private final int RETRY_COUNT = 5;
	private int intial = 0;
	public Dialog dialog;
	public static Context mContext = null;
	
	private OnResponseListener onResponseListener;
	private boolean showDialog = true;

	public synchronized void httpPost(final Activity act, final Request request) {
		mContext = act;
		URLConstants.context = act;
		
		NetworkConnection network = new NetworkConnection();
		network.setOnConnectedListener(new OnConnectedListener() {
			@Override
			public void httpConnect() {
				if(URLConstants.isNetworkAvailable()){
					new GetData(act, request).execute();
				}else {
					Toast.makeText(act, "Please Try Again...", Toast.LENGTH_SHORT).show();
				}

			}
		});
		network.isOnline(act);
	}

	public synchronized void httpPost(final Activity act, final Request request, boolean showDialog) {

		this.showDialog = showDialog;
		NetworkConnection network = new NetworkConnection();
		network.setOnConnectedListener(new OnConnectedListener() {
			@Override
			public void httpConnect() {
				new GetData(act, request).execute();
			}
		});
		network.isOnline(act);
	}

	class GetData extends AsyncTask<String, Void, String> {
		
		Activity act;
		Request request;

		ProgressDialog pd;

		public GetData(Activity act, Request request) {
			// TODO Auto-generated constructor stub
			this.act = act;
			this.request = request;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (showDialog) {
				/*pd = new ProgressDialog(act);
				pd.setCanceledOnTouchOutside(false);
				pd.setMessage("Loading...");
				pd.show();*/
				/*dialog = new Dialog(act);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.custom);
				ImageView image = (ImageView) dialog.findViewById(R.id.image);
				GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(image);
				Glide.with(act).load(R.drawable.progress3).into(imageViewTarget);
				dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
				dialog.show();*/
			}
		}

		@Override
		protected String doInBackground(String... params) {
			
			String result = "";
			try {
				intial++;
				OkHttpClient client = new OkHttpClient();
				Response response = client.newCall(request).execute();
				result = response.body().string();
			} catch (Exception e) {
				//FirebaseCrash.report(new Exception(e.toString()));
				e.printStackTrace();
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.e("TAG", "res-"+result);
			try {
				if (result.length() <= 0 && intial < RETRY_COUNT) {
					new GetData(act, request).execute();
				} else {
					if (onResponseListener != null) 
						onResponseListener.onResponse(result);
				}
			} catch (Exception e) {
				//FirebaseCrash.report(new Exception(e.toString()));
				e.printStackTrace();
				// TODO: handle exception
				if (intial < RETRY_COUNT) {
					new GetData(act, request).execute();
				}
			} finally {
				if (pd != null)
					pd.dismiss();
				///dialog.dismiss();
			}
		}
	}
	
	public void setOnResponseListener(OnResponseListener onResponseListener) {
		
		this.onResponseListener = onResponseListener;

	}
}
