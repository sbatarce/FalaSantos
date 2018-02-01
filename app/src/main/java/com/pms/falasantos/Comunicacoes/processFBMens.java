package com.pms.falasantos.Comunicacoes;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;
import com.pms.falasantos.Atividades.AddAlvoActivity;
import com.pms.falasantos.Atividades.SetupActivity;
import com.pms.falasantos.Globais;
import com.pms.falasantos.RespostaConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
/**
 * Created by w0513263 on 21/08/17.
 */

public class processFBMens implements RespostaConfig
	{
	Context ctx = Globais.ctx;
	RequestHttp req = null;
	private ProgressDialog progress;
	
	private enum State
		{
		nenhum,
		obtmens,
		atuamsa
		}
	
	private State state = State.nenhum;
	
	public processFBMens( Context ctx )
		{
		this.ctx = ctx;
		}
	
	public boolean obterMens()
		{
		if( !Globais.isConnected() )
			return false;
		state = State.obtmens;
		req = new RequestHttp( ctx );
		String url = Globais.dominio + "/services/SRV_OBTERMENS.php?";
		url += "iddis=" + URLEncoder.encode( ""+Globais.config.iddis );
		req.delegate = this;
		req.execute( url );
		return true;
		}
	
	public boolean atuaMSAs( String lista )
		{
		if( !Globais.isConnected() )
			return false;
		state = State.atuamsa;
		req = new RequestHttp( ctx );
		String url = Globais.dominio + "/partes/funcoes.php?func=atuamsa";
		url += "&lista=" + URLEncoder.encode( lista );
		url += "&cmp=" + URLEncoder.encode( "R" );
		url += "&data=" + URLEncoder.encode( Globais.agoraDB() );
		req.delegate = this;
		req.execute( url );
		return true;
		}
	
	@Override
	public void Resposta( String resposta )
		{
		int qtd;
		JSONArray dados;
		JSONObject junid;
		ContentValues cv;
		//  verifica a parte comum a todas as respostas
		try
			{
			JSONObject jobj = new JSONObject( resposta );
			if( jobj.has( "erro" ) )
				{
				String erro = jobj.getString( "erro" );
				if( erro.contains( "01017" ) )
					Globais.Alerta( ctx, "Acesso negado", "SSHD e/ou senha não corretos" );
				else
					Globais.Alerta( ctx, "Resposta do servidor com erro", erro );
				return;
				}
			if( !jobj.has( "status" ) )
				{
				Globais.Alerta( ctx, "Resposta do servidor com erro",
				                "Resposta sem indicativo de estado" );
				return;
				}
			if( !jobj.getString( "status" ).equals( "OK" ) && !jobj.getString( "status" ).equals( "ok" ) )
				{
				String status = jobj.getString( "status" );
				Globais.Alerta( ctx, "Resposta do servidor com erro", status );
				return;
				}
			//  processamento de cada estado
			switch( state )
				{
				case obtmens:
					String lista = "";
					if( !jobj.has( "quantidade" ) )
						{
						Globais.Alerta( ctx, "Resposta do servidor com erro",
						                "Resposta sem indicativo de quantidade de mensagens" );
						return;
						}
					int qtmsg = jobj.getInt( "quantidade" );
					if( qtmsg < 1 )
						return;
					if( !jobj.has( "mensagens") )
						{
						Globais.Alerta( ctx, "Resposta do servidor com erro",
						                "Resposta sem lista de mensagens" );
						return;
						}
					JSONArray mensgs = jobj.getJSONArray( "mensagens" );
					for( int ixmsg=0; ixmsg<qtmsg; ixmsg++ )
						{
						JSONObject mensg = mensgs.getJSONObject( ixmsg );
						int idalv = mensg.getInt( "idalv" );
						int idmsa = mensg.getInt( "idmsa" );
						int idmsg = mensg.getInt( "idmsg" );
						String titulo = mensg.getString( "titulo" );
						String sshd = mensg.getString( "sshd" );
						String remet = mensg.getString( "remetente" );
						if( lista.length() > 0 )
							lista += ",";
						lista += ""+idmsa;
						long ixrem = Globais.ixRemet( sshd, idalv, remet );
						//  insere a nova mensagem
						//  afazer => precisa iniciar uma transação aqui?
						cv = new ContentValues( 10 );
						cv.put("msg_id", idmsg );
						cv.put("rem_id", ixrem );
						cv.put("msg_msaid", idmsa );
						cv.put( "msg_titulo", titulo );
						cv.put("msg_dtreceb", Globais.agoraDB() );
						cv.put("msg_dtleitu", "" );
						cv.put("msg_dtresp", "" );
						cv.put("msg_dtretorno", "" );
						cv.put("msg_dtdelete", "" );
						cv.put("msg_flatua", 1 );
						long res = Globais.db.insert( "mensagens", null, cv );
						//  obtem os eventuais corpos da mensagem
						if( mensg.has( "corpos") )
							{
							JSONArray corpos = mensg.getJSONArray( "corpos" );
							int qtcor = corpos.length();
							for( int ixcor=0; ixcor<qtcor; ixcor++ )
								{
								JSONObject corpo = corpos.getJSONObject( ixcor );
								int idcor = corpo.getInt( "idcor" );
								int idtic = corpo.getInt( "idtic" );
								String codigo = corpo.getString( "corpo" );
								String texto = corpo.getString( "texto" );
								int respo = corpo.getInt( "resposta" );
								int obrig = corpo.getInt( "obrigatoria" );
								//  insere o corpo na mensagem
								cv = new ContentValues( 10 );
								cv.put( "cor_id", idcor );
								cv.put( "msg_id", idmsg );
								cv.put( "cor_ticorpo", idtic );
								cv.put( "cor_corpo", codigo );
								cv.put( "cor_texto", texto );
								cv.put( "cor_stresposta", respo );
								cv.put( "cor_stobrigatoria", obrig );
								cv.put( "cor_flatua", 0 );
								res = Globais.db.insert( "corpo", null, cv );
								//  verifica opções do corpo
								if( corpo.has("opcoes") )
									{
									JSONArray opcoes = corpo.getJSONArray( "opcoes" );
									int qtopt = opcoes.length();
									for( int ixopt=0; ixopt<qtopt; ixopt++ )
										{
										JSONObject opcao = opcoes.getJSONObject( ixopt );
										int idopt = opcao.getInt( "idopt" );
										codigo = opcao.getString( "codigo" );
										texto = opcao.getString( "texto" );
										//  insere a opção
										cv = new ContentValues( 10 );
										cv.put( "opt_id", idopt );
										cv.put( "cor_id", idcor );
										cv.put( "opt_codigo", codigo );
										cv.put( "opt_texto", texto );
										cv.put( "opt_flchecked", 0 );
										cv.put( "opt_flatua", 0 );
										res = Globais.db.insert( "opcoes", null, cv );
										}     //  for opcoes
									}       //  corpo tem opcoes
								}         //  for corpos
							}           //  mensagem tem corpos
						}             //  for mensagens
					//  atualiza os MSA's
					atuaMSAs( lista );
					break;
				
				case atuamsa:
					break;
				}
			}
		catch( JSONException jexc )
			{
			Log.i( "chamada", jexc.getMessage() );
			}
		catch( Exception exc )
			{
			Log.i( "chamada", exc.getMessage() );
			}
		}
	
	public boolean setMens( RemoteMessage mens )
		{
		return true;
		}
	}
