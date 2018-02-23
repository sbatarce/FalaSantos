package com.pms.falasantos.Comunicacoes;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.pms.falasantos.Globais;

import java.util.HashSet;
import java.util.Set;

import javax.microedition.khronos.opengles.GL;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLING;
import static android.net.wifi.WifiManager.WIFI_STATE_UNKNOWN;
/**
 * Created by w0513263 on 22/09/17.
 */

public class NetworkStateReceiver extends BroadcastReceiver
	{
	
	protected Set<NetworkStateReceiverListener> listeners;
	protected Boolean                           connected = false;
	protected Boolean                           wifi = false;
	
	public NetworkStateReceiver()
		{
		listeners = new HashSet<NetworkStateReceiverListener>();
		connected = false;
		}
	
	public void onReceive( Context context, Intent intent )
		{
		if( intent == null || intent.getExtras() == null )
			return;
		
		ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(
			Context.CONNECTIVITY_SERVICE );
		NetworkInfo netInfo = connMan.getActiveNetworkInfo();
		if( netInfo == null )
			{
			connected = false;
			wifi = false;
			}
		else
			{
			WifiManager wifiManager = (WifiManager) context.getSystemService(
				Context.WIFI_SERVICE );
			int wifiState = wifiManager.getWifiState();
			switch( wifiState )
				{
				case WIFI_STATE_DISABLED:
					Log.i( Globais.apptag, "WIFI state: WIFI_STATE_DISABLED" );
					wifi = false;
					break;
				case WIFI_STATE_DISABLING:
					Log.i( Globais.apptag, "WIFI state: WIFI_STATE_DISABLING" );
					wifi = false;
					break;
				case WIFI_STATE_ENABLED:
					Log.i( Globais.apptag, "WIFI state: WIFI_STATE_ENABLED" );
					wifi = true;
					break;
				case WIFI_STATE_ENABLING:
					Log.i( Globais.apptag, "WIFI state: WIFI_STATE_ENABLING" );
					wifi = false;
					break;
				case WIFI_STATE_UNKNOWN:
					Log.i( Globais.apptag, "WIFI state: WIFI_STATE_UNKNOWN" );
					wifi = false;
					break;
				default:
					Log.i( Globais.apptag, "WIFI state: sabe-se lá" );
					wifi = false;
					break;
				}
			
			switch( netInfo.getState() )
				{
				case CONNECTING:
					Log.i( Globais.apptag, "NET State: " + netInfo.getState().toString() );
					break;
				case CONNECTED:
					Log.i( Globais.apptag, "NET State: " + netInfo.getState().toString() );
					break;
				case SUSPENDED:
					Log.i( Globais.apptag, "NET State: " + netInfo.getState().toString() );
					break;
				case DISCONNECTING:
					Log.i( Globais.apptag, "NET State: " + netInfo.getState().toString() );
					break;
				case DISCONNECTED:
					Log.i( Globais.apptag, "NET State: " + netInfo.getState().toString() );
					break;
				case UNKNOWN:
					Log.i( Globais.apptag, "NET State: " + netInfo.getState().toString() );
					break;
				}
			
			if( netInfo.getState() == NetworkInfo.State.CONNECTED )
				{
				connected = true;
				}
			else if( intent.getBooleanExtra( ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE ) )
				{
				connected = false;
				}
			}
		Globais.setWifi( wifi );
		Globais.setConnected( connected );
		
		notifyStateToAll();
		}
	
	private void notifyStateToAll()
		{
		for( NetworkStateReceiverListener listener : listeners )
			notifyState( listener );
		}
	
	private void notifyState( NetworkStateReceiverListener listener )
		{
		if( connected == null || listener == null )
			return;
		
		if( connected == true || wifi == true  )
			listener.networkAvailable();
		else
			listener.networkUnavailable();
		}
	
	public void addListener( NetworkStateReceiverListener l )
		{
		listeners.add( l );
		notifyState( l );
		}
	
	public void removeListener( NetworkStateReceiverListener l )
		{
		listeners.remove( l );
		}
	
	public interface NetworkStateReceiverListener
		{
		public void networkAvailable();
		public void networkUnavailable();
		}
	}