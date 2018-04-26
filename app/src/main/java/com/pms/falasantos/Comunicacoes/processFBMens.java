package com.pms.falasantos.Comunicacoes;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
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
	Context     ctx = Globais.ctx;
	RequestHttp req = null;
	private ProgressDialog progress;
	private boolean flprog = false;
	List<Integer> lsmsgs = null;          //  lista de ids de mensagens respondidas
	
	String msgid = "";                    //  lista de ids de mensagens atualizadas
	
	String lsrec = "";        //  lista de IDs recebidos
	String lslei = "";        //  lista de IDs lidos
	String lsres = "";        //  lista de IDs respondidos
	String lsdel = "";        //  lista de IDs deletados
	
	private enum State
		{
			nenhum,
			obtmens,
			atuamsas,
			atuaresp,
			atuauatua
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
		//
		req = new RequestHttp( ctx );
		state = State.obtmens;
		String url = Globais.dominio + "/services/SRV_OBTERMENS.php?";
		url += "iddis=" + URLEncoder.encode( "" + Globais.config.iddis );
		req.delegate = this;
		req.execute( url );
		return true;
		}
	
	//  atualiza todos os MSA(s) das mensagens pendentes de atualização
	public void atuaMSAs()
		{
		String msaid = "";
		String uatua = "";
		String receb = "";
		String leitu = "";
		String respo = "";
		String delet = "";
		
		String sql = "select msg_id, msg_msaid, msg_dtuatua, msg_dtreceb, msg_dtleitu, msg_dtresp, msg_dtdelete " +
			" from mensagens " +
			" where msg_dtreceb > msg_dtuatua OR " +
			"   msg_dtleitu > msg_dtuatua OR " +
			"   msg_dtresp > msg_dtuatua OR " +
			"   msg_dtdelete > msg_dtuatua";
		
		String lsmsg = "";
		Cursor cur = Globais.db.rawQuery( sql, null );
		while( cur.moveToNext() )
			{
			msgid = cur.getString( cur.getColumnIndex( "msg_id" ) );
			msaid = cur.getString( cur.getColumnIndex( "msg_msaid" ) );
			uatua = cur.getString( cur.getColumnIndex( "msg_dtuatua" ) );
			receb = cur.getString( cur.getColumnIndex( "msg_dtreceb" ) );
			leitu = cur.getString( cur.getColumnIndex( "msg_dtleitu" ) );
			respo = cur.getString( cur.getColumnIndex( "msg_dtresp" ) );
			delet = cur.getString( cur.getColumnIndex( "msg_dtdelete" ) );
			
			if( lsmsg.length() > 0 )
				lsmsg += ",";
			lsmsg += msgid;
			
			if( receb.compareTo( uatua ) > 0 )
				{
				if( !lsrec.equals( "" ) )
					lsrec += ",";
				lsrec += msaid;
				}
			if( leitu.compareTo( uatua ) > 0 )
				{
				if( !lslei.equals( "" ) )
					lslei += ",";
				lslei += msaid;
				}
			if( respo.compareTo( uatua ) > 0 )
				{
				if( !lsres.equals( "" ) )
					lsres += ",";
				lsres += msaid;
				}
			if( delet.compareTo( uatua ) > 0 )
				{
				if( !lsdel.equals( "" ) )
					lsdel += ",";
				lsdel += msaid;
				}
			}
		cur.close();
		
		if( lsdel.equals( "" ) && lslei.equals( "" ) &&
			lsrec.equals( "" ) && lsres.equals( "" ) )
			{
			return;
			}
		String body = "[";
		if( !lsdel.equals( "" ) )
			{
			if( !body.equals( "[" ) )
				body += ",";
			body += "{\"lista\": \"" + lsdel + "\"," +
				"\"cmp\": \"D\"," +
				"\"datahora\": \"" + delet + "\"}";
			}
		if( !lslei.equals( "" ) )
			{
			if( !body.equals( "[" ) )
				body += ",";
			body += "{\"lista\": \"" + lslei + "\"," +
				"\"cmp\": \"L\"," +
				"\"datahora\": \"" + leitu + "\"}";
			}
		if( !lsrec.equals( "" ) )
			{
			if( !body.equals( "[" ) )
				body += ",";
			body += "{\"lista\": \"" + lsrec + "\"," +
				"\"cmp\": \"R\"," +
				"\"datahora\": \"" + receb + "\"}";
			}
		if( !lsres.equals( "" ) )
			{
			if( !body.equals( "[" ) )
				body += ",";
			body += "{\"lista\": \"" + lsres + "\"," +
				"\"cmp\": \"E\"," +
				"\"datahora\": \"" + respo + "\"}";
			}
		body += "]";
		state = State.atuamsas;
		req = new RequestHttp( ctx );
		req.setBody( body );
		String url = Globais.dominio + "/services/SRV_ATUAMSAS.php";
		req.delegate = this;
		req.execute( url );
		return;
		}
	
	public void atuauatua()
		{
		String lsin = "";
		if( !lsdel.equals( "" ) )
			lsin += lsin;
		if( !lslei.equals( "" ) )
			{
			if( !lsin.equals( "" ) )
				lsin += ",";
			lsin += lslei;
			}
		if( !lsrec.equals( "" ) )
			{
			if( !lsin.equals( "" ) )
				lsin += ",";
			lsin += lsrec;
			}
		if( !lsres.equals( "" ) )
			{
			if( !lsin.equals( "" ) )
				lsin += ",";
			lsin += lsres;
			}
		String where = "msg_msaid in(" + lsin + ")";
		ContentValues cv = new ContentValues( 5 );
		cv.put( "msg_dtuatua", Globais.agoraDB() );
		int qtd = Globais.db.update( "mensagens", cv, where, null );
		return;
		}
	
	//  monta todas as respostas necessárias e manda ao servidor
	private String montaResposta()
		{
		lsmsgs = new ArrayList<>();
		JSONArray jres = new JSONArray();
		JSONObject corpo = null;
		JSONArray opts = null;
		try
			{
			//  seleciona todas as mensagens com respostas ainda não enviadas
			String sql = "SELECT msg.msg_id, msg.msg_dtresp, msg.msg_msaid, " +
				"cor.cor_idcor, cor.cor_ticorpo, cor.cor_resposta, opt.opt_idopt " +
				"FROM        mensagens msg " +
				"INNER JOIN  corpo cor ON " +
				"            cor.msg_id=msg.msg_id AND " +
				"            cor.cor_stresposta = 1 " +
				"LEFT JOIN   opcoes opt ON " +
				"            opt.cor_id=cor.cor_id AND " +
				"            opt.opt_flchecked=1 " +
				"WHERE  msg.msg_dtresp<>'' " +
				"  AND  (msg.msg_dtuatua='' OR msg.msg_dtresp > msg.msg_dtuatua)" +
				"  ORDER BY msg.msg_id";
			
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
				int corid = cur.getInt( cur.getColumnIndex( "cor_idcor" ) );
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
						int optid = cur.getInt( cur.getColumnIndex( "opt_idopt" ) );
						opts.put( optid );
						break;
					}
				}
			if( msgant == -1 )
				return null;
			//if( corpo != null )
			//	jres.put( corpo );
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
	//
	public boolean mandaRespostas()
		{
		if( Globais.fldebug )
			{
			
			}
		if( Globais.db == null )
			return false;
		if( !Globais.isConnected() )
			return false;
		String jresp = montaResposta();
		if( jresp == null )
			{
			atuaMSAs();
			return true;
			}
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
		String lista = "";
		if( flprog )
			{
			progress.dismiss();
			flprog = false;
			}
		int qtd;
		JSONArray dados;
		JSONObject junid;
		JSONObject jobj;
		ContentValues cv;
		//  verifica a parte comum a todas as respostas
		try
			{
			jobj = new JSONObject( resposta );
			if( jobj.has( "erro" ) )
				{
				String erro = jobj.getString( "erro" );
				if( erro.contains( "01017" ) )
					Globais.Alerta( ctx, "Acesso negado", "SSHD e/ou senha não corretos" );
				else
					{
					Globais.Alerta( ctx, "Por favor, tente mais tarde! (6)",
					                "O acesso aos dados apresentou um problema.\n" +
						                "Pode estar passando por dificuldades no momento.\n" +
					                  resposta );
					Log.d( Globais.apptag, "Erro do servidor(6): " + erro );
					}
				return;
				}
			if( !jobj.has( "status" ) )
				{
				Globais.Alerta( ctx, "Por favor, tente mais tarde! (7)",
				                "O acesso aos dados apresentou um problema.\n" +
					                "Pode estar passando por dificuldades no momento.\n" +
					                resposta );
				return;
				}
			if( !jobj.getString( "status" ).equals( "OK" ) && !jobj.getString( "status" ).equals( "ok" ) )
				{
				String status = jobj.getString( "status" );
				Globais.Alerta( ctx, "Por favor, tente mais tarde! (8)",
				                "O acesso aos dados apresentou um problema.\n" +
					                "Pode estar passando por dificuldades no momento.\n" +
					                resposta );
				return;
				}
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, jexc.getMessage() );
			return;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, exc.getMessage() );
			return;
			}
		//  processamento de cada estado
		switch( state )
			{
			case obtmens:
				try
					{
					if( !jobj.has( "quantidade" ) )
						{
						Globais.Alerta( ctx, "Por favor, tente mais tarde! (9)",
						                "O acesso aos dados apresentou um problema.\n" +
							                "Pode estar passando por dificuldades no momento.\n" );
						return;
						}
					int qtmsg = jobj.getInt( "quantidade" );
					if( qtmsg < 1 )
						return;
					if( !jobj.has( "mensagens" ) )
						{
						Globais.Alerta( ctx, "Por favor, tente mais tarde! (10)",
						                "O acesso aos dados apresentou um problema.\n" +
							                "Pode estar passando por dificuldades no momento.\n" );
						return;
						}
					JSONArray mensgs = jobj.getJSONArray( "mensagens" );
					
					for( int ixmsg = 0; ixmsg < qtmsg; ixmsg++ )
						{
						JSONObject mensg = mensgs.getJSONObject( ixmsg );
						int idalv = mensg.getInt( "idalv" );
						int idmsa = mensg.getInt( "idmsa" );
						int idmsg = mensg.getInt( "idmsg" );
						String titulo = mensg.getString( "titulo" );
						String sshd = mensg.getString( "sshd" );
						String remet = mensg.getString( "remetente" );
						String notif = mensg.getString( "dtnoti" );
						boolean flconf = false;
						if( mensg.has( "confiden" ) )
							flconf = mensg.getString( "confiden" ).equals( "1" );
						if( lista.length() > 0 )
							lista += ",";
						lista += "" + idmsa;
						//  verifica repetição do MSA
						Cursor cur = Globais.db.rawQuery(
							"select msg_id from mensagens where msg_msaid=" + idmsa, null );
						if( cur.moveToFirst() )
							{
							cur.close();
							continue;
							}
						cur.close();
						//
						long ixrem = Globais.ixRemet( sshd, idalv, remet );
						//  insere a nova mensagem
						//  afazer => precisa iniciar uma transação aqui?
						Globais.db.beginTransaction();
						cv = new ContentValues( 15 );
						cv.put( "rem_id", ixrem );
						cv.put( "msg_msaid", idmsa );
						cv.put( "msg_titulo", titulo );
						cv.put( "msg_dtnotif", notif );
						cv.put( "msg_dtreceb", Globais.agoraDB() );
						cv.put( "msg_dtleitu", "" );
						cv.put( "msg_dtresp", "" );
						cv.put( "msg_dtuatua", "" );
						cv.put( "msg_dtdelete", "" );
						if( flconf )
							cv.put( "msg_confiden", "1" );
						idmsg = (int) Globais.db.insertOrThrow( "mensagens", null, cv );
						//  obtem os eventuais corpos da mensagem
						if( mensg.has( "corpos" ) )
							{
							JSONArray corpos = mensg.getJSONArray( "corpos" );
							int qtcor = corpos.length();
							for( int ixcor = 0; ixcor < qtcor; ixcor++ )
								{
								JSONObject corpo = corpos.getJSONObject( ixcor );
								long idcor = corpo.getInt( "idcor" );
								int idtic = corpo.getInt( "idtic" );
								String codigo = corpo.getString( "corpo" );
								String texto = corpo.getString( "texto" );
								int respo = corpo.getInt( "resposta" );
								int obrig = corpo.getInt( "obrigatoria" );
								//  insere o corpo na mensagem
								cv = new ContentValues( 10 );
								cv.put( "cor_idcor", idcor );
								cv.put( "msg_id", idmsg );
								cv.put( "cor_ticorpo", idtic );
								cv.put( "cor_corpo", codigo );
								cv.put( "cor_texto", texto );
								cv.put( "cor_stresposta", respo );
								cv.put( "cor_stobrigatoria", obrig );
								idcor = Globais.db.insertOrThrow( "corpo", null, cv );
								//  verifica opções do corpo
								if( corpo.has( "opcoes" ) )
									{
									JSONArray opcoes = corpo.getJSONArray( "opcoes" );
									int qtopt = opcoes.length();
									for( int ixopt = 0; ixopt < qtopt; ixopt++ )
										{
										JSONObject opcao = opcoes.getJSONObject( ixopt );
										int idopt = opcao.getInt( "idopt" );
										codigo = opcao.getString( "codigo" );
										texto = opcao.getString( "texto" );
										//  insere a opção
										cv = new ContentValues( 10 );
										cv.put( "opt_idopt", idopt );
										cv.put( "cor_id", idcor );
										cv.put( "opt_codigo", codigo );
										cv.put( "opt_texto", texto );
										cv.put( "opt_flchecked", 0 );
										Globais.db.insertOrThrow( "opcoes", null, cv );
										}     //  for opcoes
									}       //  corpo tem opcoes
								}         //  for corpos
							}           //  mensagem tem corpos
						Globais.db.setTransactionSuccessful();
						Globais.db.endTransaction();
						}             //  for mensagens
					//  atualiza os MSA's
					Globais.comNovidades();
					mandaRespostas();
					break;
					}
				catch( JSONException jexc )
					{
					Log.i( "chamada", jexc.getMessage() );
					if( Globais.db.inTransaction() )
						Globais.db.endTransaction();
					}
				catch( Exception exc )
					{
					Log.i( "chamada", exc.getMessage() );
					if( Globais.db.inTransaction() )
						Globais.db.endTransaction();
					}
				
				//  atualizou os msas com RECEBIDO
			case atuamsas:
				atuauatua();
				break;
			//  atualiza respostas
			case atuaresp:
				String in = "";
				for( int msgid : lsmsgs )
					{
					if( in.equals( "" ) )
						in += msgid;
					else
						in += ", " + msgid;
					}
				atuaMSAs();
				break;
			}
		}
	
	public boolean setMens( RemoteMessage mens )
		{
		return true;
		}
	}
