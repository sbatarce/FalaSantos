package com.pms.falasantos.Atividades;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.pms.falasantos.Adaptadores.ElsAlvosAdapter;
import com.pms.falasantos.Comunicacoes.RequestHttp;
import com.pms.falasantos.Comunicacoes.TraceNet;
import com.pms.falasantos.Globais;
import com.pms.falasantos.Outras.clAlvs;
import com.pms.falasantos.Outras.clRems;
import com.pms.falasantos.R;
import com.pms.falasantos.RespostaConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RespostaConfig
	{
	ExpandableListView elsAlvos;
	ElsAlvosAdapter    expadapter;
	HashMap<clAlvs, List<clRems>> lista = new HashMap<>();    //  lista pronta da expandabler
	
	RequestHttp req = null;
	private ProgressDialog progress;
	private clAlvs clalvgl;             //  clalv global
	
	TraceNet trace;
	
	private long delay = 5000;
	private int posAlvo = -1;
	private int posReme = -1;
	private boolean flperm = false;     //  indica que o app está em processo de permissão
	
	final Handler hdl = new Handler();
	
	private final Runnable atualiza = new Runnable()
		{
		public void run()
			{
			try
				{
				if( Globais.flatua )
					{
					obterMensg();
					Globais.semNovidades();
					}
				else
					hdl.postDelayed( atualiza, delay );
					
				}
			catch( Exception e )
				{
				e.printStackTrace();
				}
			}
		};
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		Globais.atividade = Globais.Atividade.Main;
		Globais.setContext( this );
		
		//  prepara a lista de alvos
		elsAlvos = (ExpandableListView) findViewById( R.id.elsAlvos );
		setupList();
		//  verifica SDK version
		if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN )
			{
			String msg = "Desculpe, mas o FalaSantos não está preparado para rodar nesta versão de celular.";
			Globais.Alerta( this, "O FalaSantos se encerrará", msg );
			Globais.flfim = true;
			return;
			}
		//  verifica permissões
		int permite = ContextCompat.checkSelfPermission( this,
		                                                 android.Manifest.permission.READ_PHONE_STATE );
		if( permite != PackageManager.PERMISSION_GRANTED )
			{
			flperm = true;
			ActivityCompat.requestPermissions( MainActivity.this,
			                                   new String[]{ android.Manifest.permission.READ_PHONE_STATE },
			                                   1001 );
			/*
			permite = ContextCompat.checkSelfPermission( this,
			                                             android.Manifest.permission.READ_PHONE_STATE );
			if( permite != PackageManager.PERMISSION_GRANTED )
				finish();
			*/
			return;
			}
		//
		try
			{
			trace = new TraceNet( this );
			}
		catch( Exception exc )
			{
			}
		//  seta o action bar
		getSupportActionBar().setDisplayOptions( ActionBar.DISPLAY_SHOW_CUSTOM );
		getSupportActionBar().setDisplayShowCustomEnabled( true );
		getSupportActionBar().setCustomView( R.layout.actbar );
		//  verifica se é uma URL
		Bundle ext = getIntent().getExtras();
		/**
		 *
		 */
		if( ext != null )
			{
			if( ext.containsKey( "tipo" ) )
				{
				String tipo = ext.getString( "tipo" );
				Log.i( Globais.apptag, "Iniciando a partir de notificação tipo " + tipo );
				switch( tipo )
					{
					case "1":       //  notificação de mensagens
						break;
					case "2":       //  notificação de URL
						if( ext.containsKey( "url" ) )
							{
							Log.i( Globais.apptag, "tem data" );
							String url = ext.getString( "url" );
							Uri uri = Uri.parse( url );
							Intent intent = new Intent( Intent.ACTION_VIEW, uri );
							intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
							startActivity( intent );
							finishAffinity();
							return;
							}
						break;
					}
				}
			}
		}
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu )
		{
		Globais.setMenuPadrao( this, menu );
		getMenuInflater().inflate( R.menu.menu_main, menu );
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
	public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults )
		{
		switch( requestCode )
			{
			case 1001:
				if( grantResults.length < 1 )
					break;
				if( grantResults[0] == PackageManager.PERMISSION_GRANTED )
					flperm = false;
				else
					{
					String txmsg =
						"As permissões do aplicativo foram negadas e " +
							"não há como prosseguir sem as mesmas.\n" +
							"Essas permissões são necessárias para acessar o número de " +
							"série do telefone para que o sistema saiba a que " +
							"telefone as mensagens do FalaSantos se destinam.\n" +
							"O aplicativo não usará estas permissões para realizar qualquer " +
							"chamada ou enviar qualquer mensagem que não através da Internet, " +
							"restrito à conexão WI-FI se assim for solicitado por você.\n" +
							"O Aplicativo se encerrará quando você tocar o \"OK\" abaixo.\n" +
							"Para voltar a usá-lo você tem algumas opções:\n" +
							"1-Desinstale e reinstale o FalaSantos, ou\n" +
							"2-Vá em \"Gerenciar apps\", selecione o FalaSantos, " +
							"depois armazenamento e depois limpar dados, ou.\n" +
							"3-Vá em \"Gerenciar apps\", selecione o FalaSantos, " +
							"depois permissões e habilite as permissões apresentadas.\n" +
							"Após qualquer das opções acima, reexecute o FalaSantos " +
							"normalmente e autorize as permissões solicitadas.";
					final AlertDialog.Builder alert = new AlertDialog.Builder( this );
					alert.setTitle( "Permissões" );
					alert.setMessage( txmsg );
					alert.setCancelable( false );
					final EditText txsen = new EditText( this );
					alert.setView( txsen );
					alert.setPositiveButton( "Ok", new DialogInterface.OnClickListener()
						{
						public void onClick( DialogInterface dialog, int whichButton )
							{
							finishAffinity();
							}
						} );
					final AlertDialog dlg = alert.create();
					dlg.show();
					}
			}
		super.onRequestPermissionsResult( requestCode, permissions, grantResults );
		}
	@Override
	protected void onResume()
		{
		super.onResume();
		Globais.setContext( this );
		
		if( flperm )
			return;
		
		Globais.atividade = Globais.Atividade.Main;
		//
		if( Globais.flfim )
			{
			finishAffinity();
			Globais.flfim = false;
			return;
			}
		//  verifica SDK version
		if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN )
			{
			String msg = "Desculpe, mas o FalaSantos não está preparado para rodar nesta versão de celular.";
			Globais.Alerta( this, "O FalaSantos se encerrará", msg );
			Globais.flfim = true;
			return;
			}
		//  verifica permissões
		int permite = ContextCompat.checkSelfPermission( this,
		                                                 android.Manifest.permission.READ_PHONE_STATE );
		if( permite != PackageManager.PERMISSION_GRANTED )
			{
			ActivityCompat.requestPermissions( MainActivity.this,
			                                   new String[]{ android.Manifest.permission.READ_PHONE_STATE },
			                                   1001 );
			/*
			permite = ContextCompat.checkSelfPermission( this,
			                                             android.Manifest.permission.READ_PHONE_STATE );
			if( permite != PackageManager.PERMISSION_GRANTED )
				finish();
			*/
			return;
			}
		//
		if( !Globais.dbOK )
			{
			if( !Globais.AbreDB() )
				{
				Globais.RemoveDB();
				String msg = "O FalaSantos será encerrado.  " +
					"Por favor, reinicie-o para corrigir os problemas detectados.";
				Globais.Alerta( this, "Banco de dados corrompido", msg );
				Globais.flfim = true;
				return;
				}
			}
		//  verifica se fez setup
		if( Globais.fezSetup() )
			{
			Globais.pFBMens.obterMens(  );
			obterMensg();
			}
		else
			{
			Intent setup = new Intent( this, SetupActivity.class );
			startActivity( setup );
			return;
			}
		}
	
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
		hdl.removeCallbacks( atualiza );
		if( !flback )
			Globais.atividade =  Globais.Atividade.nenhuma;
		flback = false;
		}
	//
	private void remoAlvo( )
		{
		//
		if( !Globais.isConnected() )
			{
			Globais.Alerta( this, "Atenção",
			                "Seu celular deve estar conectado para realizar esta operação." );
			return;
			}
		progress = ProgressDialog.show( this, "Por favor, espere...", "Removendo o alvo...",
		                                true );
		req = new RequestHttp( this );
		String url = Globais.dominio + "/services/SRV_REMOALVO.php";
		url += "?id=" + URLEncoder.encode( ""+clalvgl.id );
		url += "&data=" + URLEncoder.encode( Globais.agoraDB() );
		req.delegate = this;
		req.execute( url );
		}
	//
	private void setupList()
		{
		final android.app.AlertDialog.Builder confDelete =
			new android.app.AlertDialog.Builder( MainActivity.this );
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
				clalvgl = (clAlvs) expadapter.getGroup( posAlvo );
				remoAlvo( );
				}
			} );
		elsAlvos.setOnChildClickListener( new ExpandableListView.OnChildClickListener()
			{
			@Override
			public boolean onChildClick( ExpandableListView expls, View view, int ixalv, int ixrem, long l )
				{
				clAlvs clalv = (clAlvs) expls.getExpandableListAdapter().getGroup( ixalv );
				clRems clrem = (clRems) expls.getExpandableListAdapter().getChild( ixalv, ixrem );
				//  abre as mensagens deste remetente neste grupo
				if( !clrem.rem.contains( "Não há mens" ))
					{
					Intent mens = new Intent( MainActivity.this, MensagensActivity.class );
					mens.putExtra( "idReme", "" + clrem.idrem );
					mens.putExtra( "noReme", clrem.rem );
					mens.putExtra( "noAlvo", clalv.alvo );
					startActivity( mens );
					}
				return true;
				}
			} );
		elsAlvos.setOnItemLongClickListener( new ExpandableListView.OnItemLongClickListener()
			{
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int ixalv, long id)
				{
				posReme = ExpandableListView.getPackedPositionChild( id );
				posAlvo = ExpandableListView.getPackedPositionGroup( id );
				if( ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP )
					{
					clAlvs clalv = (clAlvs) expadapter.getGroup( posAlvo );
					String msg;
					if( clalv.qtALer > 0 )
						msg = "Deseja remover permanentemente o alvo " + clalv.alvo + "?\n" +
							"Há " + clalv.qtALer + " mensagens que ainda não foram lidas.";
					else
						msg = "Deseja remover permanentemente o alvo " + clalv.alvo + "?";
					confDelete.setMessage( msg );
					confDelete.show();
					}
				return true;
				}
			});
		elsAlvos.setOnGroupExpandListener( new ExpandableListView.OnGroupExpandListener()
			{
			@Override
			public void onGroupExpand( int ixgr )
				{
				Log.i( Globais.apptag, "onGroupExpand i=" + ixgr );
				View view = null;
				ViewGroup gview = null;
				int qtch = expadapter.getChildrenCount( ixgr );
				}
			} );
		}
	
	private boolean obterMensg()
		{
		Cursor c = null;
		List<clRems> lsrems = new ArrayList<>();         //  lista de remetentes
		List<clAlvs> lsalvos = new ArrayList<>();
		lista = new HashMap<>();
		String sql =
			"select are.are_nome as noarea, alv.alv_nome as noalvo, alv.alv_id, rem.rem_id as idrem, rem.rem_nopessoa as norem,  " +
				"  case  " +
				"   when msg.msg_dtleitu is null then '-' " +
				"   else msg.msg_dtleitu " +
				"   end  as dtlei " +
				" from       areas are " +
				" inner join alvos alv on " +
				"            alv.are_id=are.are_id " +
				" left join remetentes rem on " +
				"            rem.alv_id=alv.alv_id " +
				" left join mensagens msg on  " +
				"           msg.rem_id=rem.rem_id " +
				" order by are.are_nome, alv.alv_nome, rem.rem_nopessoa";
		String alvo;
		int idalv;
		String rem;
		String alvant = "-";
		int idalvant = -1;
		String remant = "-";
		int idr = -1;
		int idrant = -1;
		int qtRemALer = 0;
		int qtRemTot = 0;
		int qtAlvALer = 0;
		int qtAlvTot = 0;
		try
			{
			c = Globais.db.rawQuery( sql, null );
			while( c.moveToNext() )
				{
				alvo = c.getString( c.getColumnIndex( "noalvo" ) );
				idalv = c.getInt( c.getColumnIndex( "alv_id" ) );
				rem = c.getString( c.getColumnIndex( "norem" ) );
				idr = c.getInt( c.getColumnIndex( "idrem" ) );
				String dtlei = c.getString( c.getColumnIndex( "dtlei" ) );
				//  tratamento do remetente
				if( rem == null )
					{
					rem = "";
					}
				if( idr != idrant )
					{
					if( idrant != -1 && !remant.isEmpty() )
						lsrems.add( new clRems( idrant, remant, qtRemALer, qtRemTot ) );
					qtAlvALer += qtRemALer;
					qtAlvTot += qtRemTot;
					qtRemALer = 0;
					qtRemTot = 0;
					remant = rem;
					idrant = idr;
					}
				//  tratamento do alvo
				if( idalv != idalvant)        //  outro alvo
					{
					if( !alvant.equals( "-" ) )
						{
						clAlvs clalv = new clAlvs( alvant, idalvant, qtAlvALer, qtAlvTot );
						lista.put( clalv, lsrems );
						lsalvos.add( clalv );
						}
					lsrems = new ArrayList<>();
					//idrant = -1;
					alvant = alvo;
					idalvant = idalv;
					qtAlvALer = 0;
					qtAlvTot = 0;
					}
				//if( !dtlei.equals( "-" ) && !dtlei.equals( "" ) )
				if( dtlei.isEmpty() )
					qtRemALer++;
				qtRemTot++;
				}
			if( idrant != -1 )
				{
				if( !remant.equals( "" ))
					lsrems.add( new clRems( idrant, remant, qtRemALer, qtRemTot ) );
				qtAlvALer += qtRemALer;
				}
			if( !alvant.equals( "-" ) )
				{
				clAlvs clalv = new clAlvs( alvant, idalvant, qtAlvALer, qtAlvTot );
				lista.put( clalv, lsrems );
				lsalvos.add( clalv );
				}
			}
		catch( Exception e )
			{
			Log.i( Globais.apptag, "obterMensg: " + e.getMessage() );
			c.close();
			hdl.postDelayed( atualiza, delay );
			return false;
			}
		expadapter = new ElsAlvosAdapter( this, lsalvos, lista );
		elsAlvos.setAdapter( expadapter );
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
			//  remove alvo e descendentes do DB interno
			//  opcoes
			String query =
				"delete from opcoes " +
					"   where opt_id in  " +
					"      (SELECT opt.opt_id " +
					"         FROM      remetentes rem " +
					"         inner join   mensagens msg on msg.rem_id=rem.rem_id " +
					"         inner join   corpo cor on cor.msg_id=msg.msg_id " +
					"         inner join   opcoes opt on opt.cor_id=cor.cor_id " +
					"         where rem.alv_id=" + clalvgl.id + " )";
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
			query = "delete from corpo " +
				"   where cor_id in  " +
				"      (SELECT cor.cor_id " +
				"         FROM        remetentes rem " +
				"         inner join  mensagens msg on msg.rem_id=rem.rem_id " +
				"         inner join  corpo cor on cor.msg_id=msg.msg_id " +
				"         where rem.alv_id=" + clalvgl.id + " )";
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
			query = "delete from mensagens " +
				"   where msg_id in  " +
				"      (SELECT msg.msg_id " +
				"         FROM        remetentes rem " +
				"         inner join  mensagens msg on msg.rem_id=rem.rem_id " +
				"         where rem.alv_id=" + clalvgl.id + " )";
			try
				{
				Globais.db.execSQL( query );
				}
			catch( Exception e )
				{
				Log.i( Globais.apptag, "Removendo mensagens: " + e.getMessage() );
				return;
				}
			//  remetentes
			query = "delete from remetentes where alv_id=" + clalvgl.id;
			try
				{
				Globais.db.execSQL( query );
				}
			catch( Exception e )
				{
				Log.i( Globais.apptag, "Removendo remetentes: " + e.getMessage() );
				return;
				}
			//  alvo
			query = "delete from alvos where alv_id=" + clalvgl.id;
			try
				{
				Globais.db.execSQL( query );
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
