package com.pms.falasantos;
import com.google.firebase.messaging.RemoteMessage;
/**
 * Created by w0513263 on 21/08/17.
 */

public class processFBMens implements RespostaConfig
	{
	RequestHttp req = null;
	@Override
	public void Resposta( String resposta )
		{
		}
	
	public boolean setMens( RemoteMessage mens )
		{
		return true;
		}
	}
