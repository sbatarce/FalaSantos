package com.pms.falasantos.Comunicacoes;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.pms.falasantos.Globais;
/**
 * Created by w0513263 on 26/07/17.
 */

public class firebaseToken extends FirebaseInstanceIdService
	{
	/**
	 * Método chamado se o token do app no device mudar.
	 */
	// [START refresh_token]
	@Override
	public void onTokenRefresh()
		{
		// Get updated InstanceID token.
		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		Log.i( Globais.apptag, "Refreshed token: " + refreshedToken );
		
		// If you want to send messages to this application instance or
		// manage this apps subscriptions on the server side, send the
		// Instance ID token to your app server.
		registraToken( refreshedToken );
		}
	// [END refresh_token]
	
	/**
	 * envia o token ao servidor de mensagens
	 * @param token novo token
	 */
	private void registraToken( String token )
		{
		// TODO: Implement this method to send token to your app server.
		}
	}