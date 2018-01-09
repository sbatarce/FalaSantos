package com.pms.falasantos.Atividades;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.pms.falasantos.Adaptadores.ElsMensAdapter;
import com.pms.falasantos.Globais;
import com.pms.falasantos.Outras.clMensagem;
import com.pms.falasantos.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MensagemActivity extends AppCompatActivity
	{
	String idreme;
	String noreme;
	String noalvo;
	
	//ArrayList<HashMap<String, Object>> oslist = new ArrayList<HashMap<String, Object>>();
	ArrayList<String> idmsgar = new ArrayList<>();
	
	ElsMensAdapter     expadapter;
	ExpandableListView expview;
	HashMap<String, List<clMensagem>> lista = new HashMap<>();
	
	int ixnld = -1;               //  índice da primeira mensagem não lida
	
	@Override
	protected void onPause()
		{
		super.onPause();
		//hdl.removeCallbacks( sendData );
		}
	
	@Override
	protected void onResume()
		{
		super.onResume();
		Globais.setSemUso();
		obterMensg();
		//hdl.postDelayed( sendData, Globais.delayRefresh );
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
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_mensagem );
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
		obterMensg();
		//hdl.postDelayed( sendData, 5000 );
		setupList();
		Globais.setSemUso();
		}
	/*
	@Override
	public void onWindowFocusChanged( boolean hasFocus )
		{
		super.onWindowFocusChanged( hasFocus );
		if( hasFocus )
			Globais.setEmUso();
		else
			Globais.setSemUso();
		}
	*/
	private void setupList()
		{
		expview.setOnGroupExpandListener( new ExpandableListView.OnGroupExpandListener()
			{
			@Override
			public void onGroupExpand( int ixgr )
				{
				Log.i( Globais.apptag, "onGroupExpand i=" + ixgr );
				View view = null;
				ViewGroup gview = null;
				int qtch = expadapter.getChildrenCount( ixgr );
				String agoradb = Globais.agoraDB();
				String agoraHu = Globais.toHumanDt( agoradb );
				for( int ixch = 0; ixch < qtch; ixch++ )
					{
					clMensagem mens = (clMensagem) expadapter.getChild( ixgr, ixch );
					if( mens.daleitu == null )
						{
						mens.daleitu = agoraHu;
						//  atualiza o banco interno
						String sql = "UPDATE mensagens SET msg_dtLeitu=" + agoradb + ", msg_flatua=1 " +
							"WHERE msg_id=" + mens.idmens;
						try
							{
							Globais.db.execSQL( sql );
							}
						catch( Exception e )
							{
							Log.i( Globais.apptag, "onGroupExpand UPDATE: " + e.getMessage() );
							continue;
							}
						//  atualiza o siges com a data da leitura da mensagem
						}
					}
				}
			} );
		}
	
	private boolean obterMensg()
		{
		Cursor c = null;
		List<clMensagem> lsmens = new ArrayList<>();
		List<String> lstitulo = new ArrayList<>();
		ArrayList<Boolean> lido = new ArrayList<>();      //  todas as mensagens lidas
		Boolean fllida = false;                           //  indica mensagem ja lida
		int qtold = lista.size();
		lista = new HashMap<>();
		idmsgar.clear();
		String sql =
			"select msg.msg_id, msg_msaid, msg_titulo, msg_dtenvio, " +
				"     case  " +
				"       when msg.msg_dtleitu is null then '' " +
				"       else msg.msg_dtleitu " +
				"       end  as msg_dtleitu, " +
				"     cor_texto " +
				"  from  mensagens msg " +
				"  left join corpo cor on " +
				"            cor.msg_id=msg.msg_id " +
				" where      msg.rem_id=" + idreme +
				" ORDER BY msg_dtenvio desc";
		String dtenv = "";
		String dtlei = "";
		int idmsg;
		String titant = "";

		String dtenvant = "-";
		int idmsgant = -1;
		String corpo = "";
		try
			{
			c = Globais.db.rawQuery( sql, null );
			int ixlista = 0;
			while( c.moveToNext() )
				{
				idmsg = c.getInt( c.getColumnIndex( "msg_id" ) );
				dtenv = c.getString( c.getColumnIndex( "msg_dtenvio" ) );
				dtlei = c.getString( c.getColumnIndex( "msg_dtleitu" ) );
				String texto = c.getString( c.getColumnIndex( "cor_texto" ) );
				String titulo = c.getString( c.getColumnIndex( "msg_titulo" ) );
				if( idmsg != idmsgant )
					{
					if( idmsgant != -1)
						{
						clMensagem clmens = new clMensagem
							(
							titant,
							Globais.toHumanDt( dtenv ),
							Globais.toHumanDt( dtlei ),
							corpo,
							""+idmsgant
							);
						lsmens.add( clmens );
						idmsgar.add( ""+idmsgant );
						ixlista++;
						}
					idmsgant = idmsg;
					corpo = "";
					titant = titulo;
					}
				if( corpo.length() > 0 )
					corpo += "\n";
				corpo += texto;
				if( !dtenv.substring( 0, 8 ).equals( dtenvant ) )
					{
					if( lsmens.size() > 0 )
						{
						lista.put( Globais.toHumanDt( dtenvant ), lsmens );
						lstitulo.add( Globais.toHumanDt( dtenvant ) );
						lido.add( fllida );
						lsmens = new ArrayList<>();
						}
					dtenvant = dtenv.substring( 0, 8 );
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
					titant,
					Globais.toHumanDt( dtenv ),
					Globais.toHumanDt( dtlei ),
					corpo,
					""+idmsgant
					);
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
			if( c != null )
				c.close();
			return false;
			}
		if( lsmens.size() > 0 )
			{
			lista.put( Globais.toHumanDt( dtenvant ), lsmens );
			lstitulo.add( Globais.toHumanDt( dtenvant ) );
			lido.add( fllida );
			}
		expadapter = new ElsMensAdapter( this, lstitulo, lista );
		expview.setAdapter( expadapter );
		for( int ix = 0; ix < lido.size(); ix++ )
			{
			if( !lido.get( ix ) )
				expview.expandGroup( ix );
			}
		return true;
		}
	}
