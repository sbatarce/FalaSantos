package com.pms.falasantos.Atividades;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pms.falasantos.Adaptadores.ElsMensAdapter;
import com.pms.falasantos.Comunicacoes.RequestHttp;
import com.pms.falasantos.Globais;
import com.pms.falasantos.Outras.clMensagem;
import com.pms.falasantos.R;
import com.pms.falasantos.RespostaConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MensagensActivity extends AppCompatActivity implements RespostaConfig
	{
	RequestHttp req = null;
	private ProgressDialog progress;

	String idreme;
	String noreme;
	String noalvo;
	
	private long delay = 5000;
	
	//ArrayList<HashMap<String, Object>> oslist = new ArrayList<HashMap<String, Object>>();
	ArrayList<String> idmsgar = new ArrayList<>();
	List<String> lstitulo;
	List<clMensagem> lsmens;
	ArrayList<String> lsgru;
	
	ElsMensAdapter     msgadapter;
	ExpandableListView expview;
	HashMap<String, List<clMensagem>> lista = new HashMap<>();
	
	private int posData = -1;
	private int posMens = -1;
	private int msaid = -1;
	private int msgid = -1;
	private int remid = -1;
	
	int ixnld = -1;               //  índice da primeira mensagem não lida
	
	final Handler          hdl      = new Handler();
	private final Runnable atualiza = new Runnable()
		{
		public void run()
			{
			if( Globais.temNovidades() )
				{
				obterMensg();
				Globais.semNovidades();
				}
			else
				hdl.postDelayed( atualiza, delay );
			}
		};
	
	private boolean flback;
	@Override
	public void onBackPressed()
		{
		super.onBackPressed();
		flback = true;
		}
	
	@Override
	protected void onPause()
		{
		super.onPause();
		if( !flback )
			Globais.atividade = Globais.Atividade.nenhuma;
		flback = false;
		hdl.removeCallbacks( atualiza );
		}
	@Override
	protected void onResume()
		{
		super.onResume();
		Globais.atividade = Globais.Atividade.Mensagens;
		Globais.setContext( this );
		Globais.setSemUso();
		obterMensg();
		}
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu )
		{
		/*
		Globais.setMenuPadrao( this, menu );
		getMenuInflater().inflate( R.menu.menu_main, menu );
		*/
		return true;
		}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item )
		{
		int id = item.getItemId();
		
		Globais.prcMenuItem( this, id );
		return super.onOptionsItemSelected( item );
		}
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_mensagens );
		Globais.atividade = Globais.Atividade.Mensagens;
		Globais.setContext( this );
		//
		Intent mensg = getIntent();
		idreme = mensg.getStringExtra( "idReme" );
		noreme = mensg.getStringExtra( "noReme" );
		noalvo = mensg.getStringExtra( "noAlvo" );
		//
		getSupportActionBar().setDisplayOptions( ActionBar.DISPLAY_SHOW_CUSTOM );
		getSupportActionBar().setDisplayShowCustomEnabled( true );
		getSupportActionBar().setCustomView( R.layout.actbar );
		//
		((TextView) findViewById( R.id.txMsgPerfil )).setText( "Alvo: " + noalvo );
		((TextView) findViewById( R.id.txMsgOrig )).setText( "Remetente: " + noreme );
		expview = (ExpandableListView) findViewById( R.id.exlvMensag );
		//
		idmsgar.clear();
		//
		ixnld = -1;
		setupList();
		obterMensg();
		Globais.setSemUso();
		}
	
	private void remoMens()
		{
		Cursor cur = null;
		String sql = "select msg_msaid, rem_id from mensagens where msg_id=" + msgid;
		try
			{
			cur = Globais.db.rawQuery( sql, null );
			if( !cur.moveToFirst() )
				{
				sql = "Os dados do FalaSantos estão corrompidos.\n" +
					"Por favor, desinstale-o e instale novamente para corrigir.";
				Globais.Alerta( this, "Erro grave", sql );
				cur.close();
				return;
				}
			msaid = cur.getInt( cur.getColumnIndex( "msg_msaid" ) );
			remid = cur.getInt( cur.getColumnIndex( "rem_id" ) );
			
			progress = ProgressDialog.show( this, "Por favor, aguarde...", "Removendo mensagem...",
			                                true );
			req = new RequestHttp( this );
			String body = "[{ \"lista\": " + msaid + ", \"cmp\": \"D\", \"datahora\": \"" +
				Globais.agoraDB() + "\" }]";
			req.setBody( body );
			String url = Globais.dominio + "/services/SRV_ATUAMSAS.php";
			req.delegate = this;
			req.execute( url );
			}
		catch( Exception e )
			{
			Log.i( Globais.apptag, "remoMens: " + e.getMessage() );
			if( cur != null )
				cur.close();
			sql = "Os dados do FalaSantos foram corrompidos.\n" +
				"Por favor, desinstale-o e instale novamente para corrigir.";
			Globais.Alerta( this, "Erro grave!!", sql );
			return;
			}
		}

	private void setupList()
		{
		final android.app.AlertDialog.Builder confDelete =
			new android.app.AlertDialog.Builder( MensagensActivity.this );
		confDelete.setTitle( "Atenção! Isto não poderá ser desfeito" );
		confDelete.setCancelable( false );
		confDelete.setNegativeButton( "Não, manter", new DialogInterface.OnClickListener()
			{
			@Override
			public void onClick( DialogInterface dialogInterface, int i )
				{
				}
			} );
		confDelete.setPositiveButton( "Sim, remover", new DialogInterface.OnClickListener()
			{
			@Override
			public void onClick( DialogInterface dialogInterface, int i )
				{
				clMensagem clmsg = (clMensagem) msgadapter.getChild( posData, posMens );
				msgid = Integer.parseInt( clmsg.idmens );
				remoMens(  );
				}
			} );
		
		expview.setOnChildClickListener( new ExpandableListView.OnChildClickListener()
			{
			@Override
			public boolean onChildClick( ExpandableListView parent, View v, int groupPosition, int childPosition, long id )
				{
				clMensagem clm = (clMensagem) msgadapter.getChild( groupPosition, childPosition );
				if( clm.daleitu == null )
					{
					String sql = "UPDATE mensagens SET msg_dtLeitu='" + Globais.agoraDB() +
						"' WHERE msg_id=" + clm.idmens;
					try
						{
						Globais.db.execSQL( sql );
						}
					catch( Exception e )
						{
						Log.i( Globais.apptag, "onGroupExpand UPDATE: " + e.getMessage() );
						}
					}
				Intent mens = new Intent( MensagensActivity.this, MensagemActivity.class );
				mens.putExtra( "idmens", "" + clm.idmens );
				mens.putExtra( "titulo", clm.titulo );
				mens.putExtra( "dtresp", clm.daresp );
				startActivity( mens );
				return true;
				}
			} );
		
		expview.setOnItemLongClickListener( new ExpandableListView.OnItemLongClickListener()
			{
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int ixpos, long id)
				{
				posMens = ExpandableListView.getPackedPositionChild( id );
				posData = ExpandableListView.getPackedPositionGroup( id );
				if( ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD )
					{
					if( posData >= 0 && posMens >= 0 )
						{
						clMensagem clm = (clMensagem) msgadapter.getChild( posData, posMens );
						String msg;
						msg = "";
						if( clm.daleitu == null )
							msg += "Esta mensagem ainda não foi lida.\n";
						if( clm.flresp && clm.daresp == null )
							msg += "O remetente solicita uma resposta.\n";
						msg += "Deseja remover permanentemente a mensagem?";
						confDelete.setMessage( msg );
						confDelete.show();
						}
					}
				return true;
				}
			});
		
		}
	//  verifica se a mensagem tem algum corpo com resposta
	//  seta o flresp do clMensagem
	private boolean temResp(clMensagem clm )
		{
		boolean ret = true;
		String sql = "select count(1) as respostas from corpo where msg_id=?" +
									" and cor_stresposta = 1";
		String[] parms = new String[] {""+clm.idmens};
		Cursor c = Globais.db.rawQuery( sql, parms );
		if( c.moveToFirst() )
			{
			int qtd = c.getInt( c.getColumnIndex( "respostas" ) );
			if( qtd > 0 )
				clm.setFlresp( true );
			else
				clm.setFlresp( false );
			}
		else
			ret = false;
		if( c != null )
			c.close();
		return ret;
		}
	
	private boolean obterMensg()
		{
		Cursor curmsg = null;
		lsmens = new ArrayList<>();
		lsgru = new ArrayList<>();
		lstitulo = new ArrayList<>();
		ArrayList<Boolean> lido = new ArrayList<>();      //  todas as mensagens lidas
		Boolean fllida = false;                           //  indica mensagem ja lida
		int qtold = lista.size();
		lista = new HashMap<>();
		idmsgar.clear();
		//  obtem as mensagens do destinatário em ordem decrescente de data
		String sql =
			"select msg.msg_id, msg_msaid, msg_titulo, msg_dtreceb, " +
				"     case  " +
				"       when msg.msg_dtleitu is null then '' " +
				"       else msg.msg_dtleitu " +
				"       end  as msg_dtleitu, " +
				"     case  " +
				"       when msg.msg_dtresp is null then '' " +
				"       else msg.msg_dtresp " +
				"       end  as msg_dtresp, " +
				"     cor_texto, cor.cor_id " +
				"  from  mensagens msg " +
				"  left join corpo cor on " +
				"            cor.msg_id=msg.msg_id " +
				" where      msg.rem_id=" + idreme +
				" ORDER BY msg_dtreceb desc, cor.cor_id";
		String dtrec = "";
		String dtlei = "";
		String dtres = "";
		int idmsg;

		String dtrecant = "-";
		String titulo = null;
		String texto;
		int idmsgant = -1;
		String corpo = "";
		try
			{
			curmsg = Globais.db.rawQuery( sql, null );
			int ixlista = 0;
			while( curmsg.moveToNext() )
				{
				idmsg = curmsg.getInt( curmsg.getColumnIndex( "msg_id" ) );
				if( idmsg != idmsgant )
					{
					if( idmsgant != -1)
						{
						clMensagem clmens = new clMensagem
							(
							titulo,
							Globais.toHumanDt( dtrec ),
							Globais.toHumanDt( dtlei ),
							Globais.toHumanDt( dtres ),
							corpo,
							""+idmsgant
							);
						temResp( clmens );
						if( clmens.getFlresp() )
							clmens.mensagem = "toque para abrir a mensagem";
						lsmens.add( clmens );
						idmsgar.add( ""+idmsgant );
						ixlista++;
						}
					idmsgant = idmsg;
					corpo = "";
					}
				dtrec = curmsg.getString( curmsg.getColumnIndex( "msg_dtreceb" ) );
				dtlei = curmsg.getString( curmsg.getColumnIndex( "msg_dtleitu" ) );
				dtres = curmsg.getString( curmsg.getColumnIndex( "msg_dtresp" ) );
				titulo = curmsg.getString( curmsg.getColumnIndex( "msg_titulo" ) );
				texto = curmsg.getString( curmsg.getColumnIndex( "cor_texto" ) );
				if( corpo.length() > 0 )
					corpo += "\n";
				corpo += texto;
				if( !dtrec.substring( 0, 8 ).equals( dtrecant ) )
					{
					if( lsmens.size() > 0 )
						{
						lista.put( Globais.toHumanDt( dtrecant ), lsmens );
						lstitulo.add( Globais.toHumanDt( dtrecant ) );
						lido.add( fllida );
						lsmens = new ArrayList<>();
						}
					dtrecant = dtrec.substring( 0, 8 );
					fllida = false;
					}
				if( dtlei != null )
					fllida = true;
				if( ixnld < 0 && fllida )
					ixnld = ixlista;
				}
			if( idmsgant != -1)
				{
				clMensagem clmens = new clMensagem
					(
					titulo,
					Globais.toHumanDt( dtrec ),
					Globais.toHumanDt( dtlei ),
					Globais.toHumanDt( dtres ),
					corpo,
					""+idmsgant
					);
				temResp( clmens );
				if( clmens.getFlresp() )
					clmens.mensagem = "toque para abrir a mensagem";
				lsmens.add( clmens );
				idmsgar.add( ""+idmsgant );
				ixlista++;
				}
			if( ixnld < 0 )
				ixnld = ixlista - 1;
			}
		catch( Exception e )
			{
			Log.i( Globais.apptag, "obterMensg: " + e.getMessage() );
			if( curmsg != null )
				curmsg.close();
			hdl.postDelayed( atualiza, delay );
			Globais.semNovidades();
			return false;
			}
		if( lsmens.size() > 0 )
			{
			lista.put( Globais.toHumanDt( dtrecant ), lsmens );
			lstitulo.add( Globais.toHumanDt( dtrecant ) );
			lido.add( fllida );
			}
		msgadapter = new ElsMensAdapter( this, lstitulo, lista );
		expview.setAdapter( msgadapter );
		for( int ix = 0; ix < lido.size(); ix++ )
			{
			if( !lido.get( ix ) )
				expview.expandGroup( ix );
			}
		Globais.semNovidades();
		hdl.postDelayed( atualiza, delay );
		return true;
		}
	@Override
	public void Resposta( String resposta )
		{
		progress.dismiss();
		
		try
			{
			JSONObject jobj = new JSONObject( resposta );
			if( jobj.has( "erro" ) )
				{
				String erro = jobj.getString( "erro" );
				if( erro.contains( "01017" ) )
					{
					Globais.Alerta( this, "Acesso negado", "SSHD e/ou senha não corretos" );
					return;
					}
				if( erro.contains( "exclusiv" ) )
					{
					Globais.Alerta( this, "Duplicado", "Já há este alvo neste aparelho." );
					return;
					}
				Globais.Alerta( this, "Resposta validando Alvo com erro:", erro );
				return;
				}
			if( !jobj.has( "status" ) )
				{
				Globais.Alerta( this, "Resposta do servidor com erro (15)",
				                "Resposta sem indicativo de estado" );
				return;
				}
			if( !jobj.getString( "status" ).equals( "OK" ) && !jobj.getString( "status" ).equals( "ok" ) )
				{
				String descr = jobj.getString( "descr" );
				Globais.Alerta( this, "Resposta do servidor com erro (16)", descr );
				return;
				}
			//  remove a mensagem do banco interno
			//  opcoes
			String query =
				"delete from opcoes " +
					"   where opt_id in  " +
					"      (SELECT opt.opt_id " +
					"         FROM         corpo cor " +
					"         inner join   opcoes opt on opt.cor_id=cor.cor_id " +
					"         where cor.msg_id=" + msgid + " )";
			try
				{
				Globais.db.execSQL( query );
				}
			catch( Exception e )
				{
				Log.i( Globais.apptag, "Removendo opcoes: " + e.getMessage() );
				return;
				}
			//  corpos
			query = "delete from corpo where msg_id=" + msgid;
			try
				{
				Globais.db.execSQL( query );
				}
			catch( Exception e )
				{
				Log.i( Globais.apptag, "Removendo corpos: " + e.getMessage() );
				return;
				}
			//  mensagens
			query = "delete from mensagens where msg_id=" + msgid;
			try
				{
				Globais.db.execSQL( query );
				}
			catch( Exception e )
				{
				Log.i( Globais.apptag, "Removendo mensagens: " + e.getMessage() );
				return;
				}
			Cursor cur = null;
			query = "select count(1) as qtd from mensagens where rem_id=" + remid;
			try
				{
				cur = Globais.db.rawQuery( query, null );
				if( !cur.moveToFirst() )
					{
					query = "Os dados do FalaSantos estão corrompidos.\n" +
						"Por favor, desinstale-o e instale novamente para corrigir.";
					Globais.Alerta( this, "Erro grave", query );
					cur.close();
					return;
					}
				int qtd = cur.getInt( cur.getColumnIndex( "qtd" ) );
				cur.close();
				if( qtd < 1 )
					{
					query = "delete from remetentes where rem_id=" + remid;
					Globais.db.execSQL( query );
					}
				}
			catch( Exception e )
				{
				Log.i( Globais.apptag, "Removendo remetentes: " + e.getMessage() );
				return;
				}
			//  refaz o display
			obterMensg();
			}
		catch( JSONException jexc )
			{
			Globais.Alerta( this, "Por favor, tente mais tarde.", "Falhou acesso ao servidor." );
			Log.i( "chamada", jexc.getMessage() );
			}
		catch( Exception exc )
			{
			Toast.makeText( getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG ).show();
			Log.i( "chamada", exc.getMessage() );
			}
		}
	}
