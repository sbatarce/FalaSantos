package com.pms.falasantos.Atividades;

/**
 * prepara a tela de escolha de alvos disponíveis para o dispositivo
 *    busca todos os alvos disponíveis  - partes/procs.php?proc=obttialvos
 *    monta e mostra uma lista de 2 níveis por área do alvo
 */

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.pms.falasantos.Adaptadores.ElsAreasAdapter;
import com.pms.falasantos.Globais;
import com.pms.falasantos.Outras.clAlvo;
import com.pms.falasantos.R;
import com.pms.falasantos.Comunicacoes.RequestHttp;
import com.pms.falasantos.RespostaConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AlvosActivity extends AppCompatActivity implements RespostaConfig
	{
	ExpandableListView elsAreas;
	ElsAreasAdapter    expadapter;
	HashMap<String, List<clAlvo>> lista = null;
	//  variáveis usadas no HTTP
	RequestHttp            req   = null;
	private ProgressDialog progress;
	private enum State
		{
			nenhum,
			obteralvos,
			cadasalvo
		}
	State state = State.nenhum;
	//
	@Override
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		Globais.atividade = Globais.Atividade.Alvos;
		Globais.setContext( this );
		setContentView( R.layout.activity_alvos );
		//
		getSupportActionBar().setDisplayOptions( ActionBar.DISPLAY_SHOW_CUSTOM );
		getSupportActionBar().setDisplayShowCustomEnabled( true );
		getSupportActionBar().setCustomView( R.layout.actbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );
		//
		setuplist();
		//
		progress = ProgressDialog.show( this, "Por favor, aguarde...",
		                                "Obtendo alvos disponíveis...",
		                                true );
		state = State.obteralvos;
		req = new RequestHttp( this );
		String url = Globais.dominio + "/partes/procs.php?proc=obttialvos";
		req.delegate = this;
		req.execute( url );
		}
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu )
		{
		Globais.setMenuAjuda( this, menu );
		getMenuInflater().inflate( R.menu.menu_main, menu );
		return true;
		}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item )
		{
		int id = item.getItemId();
		if( id == android.R.id.home )
			{
			finish();
			return true;
			}
		Globais.prcMenuItem( this, id );
		return super.onOptionsItemSelected( item );
		}
	
	@Override
	protected void onResume()
		{
		super.onResume();
		Globais.atividade = Globais.Atividade.Alvos;
		Globais.setContext( this );
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
		if( !flback )
			Globais.atividade = Globais.Atividade.nenhuma;
		flback = false;
		}
	@Override
	public void Resposta( String resposta )
		{
		progress.dismiss();
		//  verifica a parte comum a todas as respostas
		try
			{
			String aux = "";
			JSONObject jobj = new JSONObject( resposta );
			if( jobj.has( "erro" ) )
				{
				String erro = jobj.getString( "erro" );
				String tit = "Resposta do servidor com erro (10)";
				if( erro.contains( "01017" ) )
					{
					erro = "SSHD e/ou senha não corretos";
					tit = "Acesso negado.";
					}
				if( erro.toUpperCase().contains( "TIME" ) && erro.toUpperCase().contains( "OUT" ) )
					{
					erro =  "A resposta demorou um tempo excessivo para retornar.\n" +
									"Por favor, tente mais tarde.";
					tit = "Sem resposta do servidor de mensagens.";
					}
				if( jobj.has("status") )
					aux += "status: " + jobj.getString( "status" ) + "\n";
				aux += "erro: " + erro + "\n";
				aux += "estado: " + state;
				Globais.Alerta( this, "Resposta do servidor com erro (10)", aux );
				return;
				}
			if( !jobj.has( "status" ) )
				{
				aux += "Resposta do servidor sem especificação de estado válido.\n";
				aux += "estado: " + state;
				Globais.Alerta( this, "Resposta do servidor com erro (11)", aux );
				return;
				}
			if( !jobj.getString( "status" ).toUpperCase().equals( "OK" ) )
				{
				aux = "status: " + jobj.getString( "status" ) + "\n" +
							"estado: " + state;
				Globais.Alerta( this, "Resposta do servidor com erro (12)", aux );
				return;
				}
			//  processamento de cada estado
			ContentValues cv;
			switch( state )
				{
				case obteralvos:
					lista = new HashMap<>(  );
					List<String> lsareas = new ArrayList<>();
					List<clAlvo> lsalvos = new ArrayList<>();
					if( !jobj.has( "dados" ) )
						{
						Globais.Alerta( this, "Resposta do servidor com erro (13)",
						                "Resposta sem dados" );
						return;
						}
					JSONArray dados = jobj.getJSONArray( "dados" );
					int tiaant = -1;
					String areaant = "";
					clAlvo clalvo = null;
					for( int i = 0; i < dados.length(); i++ )
						{
						JSONObject junid = dados.getJSONObject( i );
						int idtia = junid.getInt( "IDTIA" );
						
						String noalvo = junid.getString( "TIALVO" );
						String origem = junid.getString( "ORIGEM" );
						String area = junid.getString( "AREA" );
						String titulo = junid.getString( "TITULO" );
						String service = junid.getString( "SERVICE" );

						int idcvl = -1;
						int idtcv = -1;
						int tamax = 0;
						String campo = "";
						String itens = "";
						if( !titulo.equals( "" ) )
							{
							idcvl = junid.getInt( "IDCVL" );
							idtcv = junid.getInt( "IDTCV" );
							tamax = junid.getInt( "TAMAX" );
							campo = junid.getString( "CAMPO" );
							itens = junid.getString( "ITENS" );
							}
						//
						if( !area.equals( areaant ) )
							{
							if( !areaant.equals( "" ) )
								{
								lista.put( areaant, lsalvos );
								lsareas.add( areaant );
								}
							lsalvos = new ArrayList<>(  );
							areaant = area;
							}
						if( idtia != tiaant )
							{
							clalvo = new clAlvo( idtia, idcvl, noalvo, origem, area, service );
							lsalvos.add( clalvo );
							tiaant = idtia;
							}
						if( titulo != null && !titulo.equals( "" ) )
							{
							clalvo.addCampo( idtcv, tamax, titulo, campo, itens );
							}
						}
					if( tiaant != -1 )
						{
						lista.put( areaant, lsalvos );
						lsareas.add( areaant );
						}
					expadapter = new ElsAreasAdapter( this, lsareas, lista );
					elsAreas.setAdapter( expadapter );
					break;
				
				case cadasalvo:
					break;
				}
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
	
	private void setuplist()
		{
		elsAreas = (ExpandableListView) findViewById( R.id.elsAreas );
		//
		elsAreas.setOnChildClickListener( new ExpandableListView.OnChildClickListener()
			{
			@Override
			public boolean onChildClick( ExpandableListView explv, View view, int ixarea, int ixalvo, long l )
				{
				clAlvo clalvo = (clAlvo) explv.getExpandableListAdapter().getChild( ixarea, ixalvo );
				Globais.setClalvo( clalvo );
				Intent addalvo = new Intent( AlvosActivity.this, AddAlvoActivity.class );
				startActivity( addalvo );
				return false;
				}
			} );
		//
		elsAreas.setOnGroupExpandListener( new ExpandableListView.OnGroupExpandListener()
			{
			@Override
			public void onGroupExpand( int i )
				{
				return;
				}
			} );
		}
	}

