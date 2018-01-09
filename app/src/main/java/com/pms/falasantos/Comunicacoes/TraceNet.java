package com.pms.falasantos.Comunicacoes;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.pms.falasantos.Globais;
/**
 * Created by w0513263 on 22/09/17.
 */

public class TraceNet implements NetworkStateReceiver.NetworkStateReceiverListener
	{
	private NetworkStateReceiver networkStateReceiver;
	Context ctx;
	
	public TraceNet( Context ctx )
		{
		this.ctx = ctx;
		networkStateReceiver = new NetworkStateReceiver();
		networkStateReceiver.addListener(this);
		IntentFilter ifil = new IntentFilter( android.net.ConnectivityManager.CONNECTIVITY_ACTION);
		ifil.addAction( WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION );
		ctx.registerReceiver(networkStateReceiver, ifil );
		}
	@Override
	public void networkAvailable() {
	Log.i( Globais.apptag, "Conectou" );
    // TODO: conectou
	}
	
	@Override
	public void networkUnavailable() {
	Log.i( Globais.apptag, "Desconectou");
     // TODO: desconectou
	}	}
