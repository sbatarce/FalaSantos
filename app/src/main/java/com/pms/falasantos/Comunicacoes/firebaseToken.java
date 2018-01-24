package com.pms.falasantos.Comunicacoes;
import android.app.ProgressDialog;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.pms.falasantos.Atividades.AlvosActivity;
import com.pms.falasantos.Globais;
import com.pms.falasantos.RespostaConfig;

import java.net.URLEncoder;
/**
 * Created by w0513263 on 26/07/17.
 *
 *
 */

public class firebaseToken extends FirebaseInstanceIdService implements RespostaConfig
	{
	RequestHttp            req   = null;
	private ProgressDialog progress;
	/**
	 * Método chamado se o token do app no device mudar.
	 */
	@Override
	public void onTokenRefresh()
		{
		// Obtem o token novo
		String token = FirebaseInstanceId.getInstance().getToken();
		Log.i( Globais.apptag, "Refreshed token: " + token );
		if( Globais.config.iddis != -1 )
			registraToken( token );     //  manda o token pro servidor
		}
	/**
	 * envia o token ao servidor de mensagens
	 * @param token novo token
	 */
	private void registraToken( String token )
		{
		req = new RequestHttp( this );
		String url = Globais.dominio + "partes/funcoes.php?func=caddispositivo";
		url += "&iddes="+ URLEncoder.encode( ""+Globais.config.iddes );
		url += "&serie="+ URLEncoder.encode( Globais.config.nuserie );
		url += "&token="+ URLEncoder.encode( token );
		req.delegate = this;
		req.execute( url );
		}
	@Override
	public void Resposta( String resposta )
		{
		Log.i( Globais.apptag, "Resposta: " + resposta );
		}
	}