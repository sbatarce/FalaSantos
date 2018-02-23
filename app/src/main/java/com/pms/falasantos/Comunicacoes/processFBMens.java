package com.pms.falasantos.Comunicacoes;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
import java.util.ArrayList;
import java.util.List;
/**
 * Created by w0513263 on 21/08/17.
 */

public class processFBMens implements RespostaConfig
	{
	Context ctx = Globais.ctx;
	RequestHttp req = null;
	private ProgressDialog progress;
	private boolean flprog = false;
	List<Integer> lsmsgs = null;          //  lista de ids de mensagens respondidas
	
	private enum State
		{
		nenhum,
		obtmens,
		atuamsa,
		atuaresp
		}
	
	private State state = State.nenhum;
	
	public processFBMens( Context ctx )
		{
		this.ctx = ctx;
		}
	
	public boolean obterMens( boolean prog )
		{
		if( !Globais.isConnected() )
			return false;
		if( prog )
			{
			progress = ProgressDialog.show( ctx, "Por favor, espere...",
			                                "Obtendo as últimas mensagens...",
			                                true );
			flprog = true;
			}
		else
			flprog = false;
		
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
	
	private String montaResposta()
		{
		lsmsgs = new ArrayList<>();
		JSONArray jres = new JSONArray(  );
		JSONObject corpo = null;
		JSONArray opts = null;
		try
			{
			String sql = "SELECT msg.msg_id, msg.msg_dtresp, msg.msg_msaid, " +
				"cor.cor_id, cor.cor_ticorpo, cor.cor_resposta, opt.opt_id " +
				"FROM        mensagens msg " +
				"INNER JOIN  corpo cor ON " +
				"            cor.msg_id=msg.msg_id AND " +
				"            cor.cor_stresposta = 1 " +
				"LEFT JOIN   opcoes opt ON " +
				"            opt.cor_id=cor.cor_id AND " +
				"            opt.opt_flchecked=1 " +
				"WHERE  msg.msg_dtresp is not null AND msg.msg_dtresp<>''" +
				"  AND  (msg.msg_dtretorno is null OR msg.msg_dtretorno='')";

			Cursor cur = Globais.db.rawQuery( sql, null );
			int msgant = -1;
			int corant = -1;
			int ticor = -1;
			
			while( cur.moveToNext() )
				{
				int msgid = cur.getInt( cur.getColumnIndex( "msg_id" ) );
				if( msgant != msgid )
					{
					lsmsgs.add( msgid );
					msgant = msgid;
					}
				int corid = cur.getInt( cur.getColumnIndex( "cor_id" ) );
				if( corant != corid )
					{
					corant = corid;
					corpo = new JSONObject();
					jres.put( corpo );
					int msaid = cur.getInt( cur.getColumnIndex( "msg_msaid" ) );
					ticor = cur.getInt( cur.getColumnIndex( "cor_ticorpo" ) );
					String data = cur.getString( cur.getColumnIndex( "msg_dtresp" ) );
					corpo.put( "msaid", msaid );
					corpo.put( "corid", corid );
					corpo.put( "data", data );
					corpo.put( "tipo", ticor );
					opts = null;
					}
				switch( ticor )
					{
					case 1:
					case 2:
					case 3:
						String resp = cur.getString( cur.getColumnIndex( "cor_resposta" ) );
						if( resp == null )
							resp = "";
						corpo.put( "texto", resp );
						break;
					case 4:
					case 5:
					case 6:
						if( opts == null )
							{
							opts = new JSONArray();
							corpo.put( "opcoes", opts );
							}
						int optid = cur.getInt( cur.getColumnIndex( "opt_id" ) );
						opts.put( optid );
						break;
					}
				}
			if( msgant == -1 )
				return null;
			if( corpo != null )
				jres.put( corpo );
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return null;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return null;
			}
		return jres.toString();
		}
	
	public boolean mandaRespostas()
		{
		String jresp = montaResposta();
		if( jresp == null )
			return true;
		
		if( !Globais.isConnected() )
			return false;
		state = State.atuaresp;
		req = new RequestHttp( ctx );
		String url = Globais.dominio + "/services/SRV_RecRespostas.php";
		req.setBody( jresp );
		req.delegate = this;
		req.execute( url );
		return true;
		}
	
	@Override
	public void Resposta( String resposta )
		{
		if( flprog )
			{
			progress.dismiss();
			flprog = false;
			}
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
						cv.put( "msg_id", idmsg );
						cv.put( "rem_id", ixrem );
						cv.put( "msg_msaid", idmsa );
						cv.put( "msg_titulo", titulo );
						cv.put( "msg_dtreceb", Globais.agoraDB() );
						cv.put( "msg_dtleitu", "" );
						cv.put( "msg_dtresp", "" );
						cv.put( "msg_dtretorno", "" );
						cv.put( "msg_dtdelete", "" );
						cv.put( "msg_flatua", 1 );
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
					Globais.comNovidades();
					break;
				
				case atuamsa:
					break;
					
				case atuaresp:
					String in = "";
					for( int msgid: lsmsgs )
						{
						if( in.equals( "" ) )
							in += msgid;
						else
							in += ", " + msgid;
						}
					String sql = "UPDATE mensagens SET msg_dtretorno='" + Globais.agoraDB() +
						"' WHERE msg_id IN( " + in + " )";
					Globais.db.execSQL( sql );
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
