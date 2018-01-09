package com.pms.falasantos.Atividades;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
import android.widget.ExpandableListView;

import com.pms.falasantos.Adaptadores.ElsAlvosAdapter;
import com.pms.falasantos.Comunicacoes.TraceNet;
import com.pms.falasantos.Globais;
import com.pms.falasantos.Outras.clAlvs;
import com.pms.falasantos.Outras.clRems;
import com.pms.falasantos.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
	{
	ExpandableListView elsAlvos;
	ElsAlvosAdapter    expadapter;
	HashMap<clAlvs, List<clRems>> lista = new HashMap<>();    //  lista pronta da expandabler
	
	TraceNet trace;
	
	boolean flfim = false;
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		//
		Globais.setContext( this );
		trace = new TraceNet( this );
		//  seta o action bar
		getSupportActionBar().setDisplayOptions( ActionBar.DISPLAY_SHOW_CUSTOM );
		getSupportActionBar().setDisplayShowCustomEnabled( true );
		getSupportActionBar().setCustomView( R.layout.actbar );
		View view = getSupportActionBar().getCustomView();
		//  verifica se é uma URL
		Bundle ext = getIntent().getExtras();
		if( ext != null )
			{
			if( ext.containsKey( "tipo" ) )
				{
				int tipo = ext.getInt( "tipo" );
				Log.i( Globais.apptag, "Iniciando a partir de notificação tipo " + tipo );
				switch( tipo )
					{
					case 1:       //  notificação de mensagens
						break;
					case 2:       //  notificação de URL
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
		//  prepara a lista de alvos
		elsAlvos = (ExpandableListView) findViewById( R.id.elsAlvos );
		setupList();
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
	protected void onResume()
		{
		super.onResume();
		//
		if( flfim )
			{
			finishAffinity();
			return;
			}
		//  verifica SDK version
		if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN )
			{
			String msg = "Desculpe, mas o FalaSantos não está preparado para rodar nesta versão de celular.";
			Globais.Alerta( this, "O FalaSantos se encerrará", msg );
			flfim = true;
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
			return;
			}
		//
		if( !Globais.AbreDB() )
			{
			Globais.RemoveDB();
			String msg = "O FalaSantos será encerrado.  " +
				"Por favor, reinicie-o para corrigir os problemas detectados.";
			Globais.Alerta( this, "Banco de dados corrompido", msg );
			flfim = true;
			return;
			}
		//  verifica se fez setup
		if( Globais.fezSetup() )
			obterMensg();
		else
			{
			Intent setup = new Intent( this, SetupActivity.class );
			startActivity( setup );
			return;
			}
		}
	//
	private void setupList()
		{
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
					Intent mens = new Intent( MainActivity.this, MensagemActivity.class );
					mens.putExtra( "idReme", "" + clrem.idrem );
					mens.putExtra( "noReme", clrem.rem );
					mens.putExtra( "noAlvo", clalv.alvo );
					startActivity( mens );
					}
				return true;
				}
			} );
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
			"select are.are_nome as noarea, alv.alv_nome as noalvo, rem.rem_id as idrem, rem.rem_nopessoa as norem,  " +
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
		String rem;
		String alvant = "-";
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
				alvo = c.getString( c.getColumnIndex( "noarea" ) ) + " - " +
					c.getString( c.getColumnIndex( "noalvo" ) );
				rem = c.getString( c.getColumnIndex( "norem" ) );
				idr = c.getInt( c.getColumnIndex( "idrem" ) );
				String dtlei = c.getString( c.getColumnIndex( "dtlei" ) );
				if( rem == null )
					{
					rem = "Não há mensagens ainda";
					}
				if( !rem.equals( remant ) )
					{
					if( !remant.equals( "-" ) )
						lsrems.add( new clRems( idrant, remant, qtRemALer, qtRemTot ) );
					qtAlvALer += qtRemALer;
					qtAlvTot += qtRemTot;
					qtRemALer = 0;
					qtRemTot = 0;
					remant = rem;
					idrant = idr;
					}
				if( !alvo.equals( alvant ) )      //  novo area/alvo
					{
					if( lsrems.size() > 0 )
						{
						clAlvs clalv = new clAlvs( alvant, qtAlvALer, qtAlvTot );
						lista.put( clalv, lsrems );
						lsalvos.add( clalv );
						lsrems = new ArrayList<>();
						}
					alvant = alvo;
					qtAlvALer = 0;
					qtAlvTot = 0;
					}
				if( dtlei.equals( "-" ) && !rem.contains( "Não há mens" ) )
					qtRemALer++;
				qtRemTot++;
				}
			if( !remant.equals( "-" ) )
				lsrems.add( new clRems( idrant, remant, qtRemALer, qtRemTot ) );
			qtAlvALer += qtRemALer;
			if( lsrems.size() > 0 )
				{
				clAlvs clalv = new clAlvs( alvant, qtAlvALer, qtAlvTot );
				lista.put( clalv, lsrems );
				lsalvos.add( clalv );
				}
			}
		catch( Exception e )
			{
			Log.i( Globais.apptag, "obterMensg: " + e.getMessage() );
			c.close();
			return false;
			}
		expadapter = new ElsAlvosAdapter( this, lsalvos, lista );
		elsAlvos.setAdapter( expadapter );
		return true;
		}
	}
