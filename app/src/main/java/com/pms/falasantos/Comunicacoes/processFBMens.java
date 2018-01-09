package com.pms.falasantos.Comunicacoes;
import android.app.ProgressDialog;
import android.content.Context;

import com.google.firebase.messaging.RemoteMessage;
import com.pms.falasantos.RespostaConfig;
/**
 * Created by w0513263 on 21/08/17.
 */

public class processFBMens implements RespostaConfig
	{
	Context ctx;
	
	RequestHttp req = null;
	private ProgressDialog progress;
	
	private enum State
		{
		nenhum,
		obtmens,
		obtcorpos,
		obtopcoes
		}
	
	public processFBMens( Context ctx )
		{
		this.ctx = ctx;
		}
	
	public boolean sincro()
		{
		
		return false;
		}
	
	@Override
	public void Resposta( String resposta )
		{
		}
	
	public boolean setMens( RemoteMessage mens )
		{
		return true;
		}
	}
