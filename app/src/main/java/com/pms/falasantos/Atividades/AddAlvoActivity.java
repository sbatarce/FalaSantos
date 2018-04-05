package com.pms.falasantos.Atividades;

/**
 *    Monta a tela de entrada dos dados do alvo escolhido pelo usuário
 *    mostra a tela e espera botão OK ou cancel
 *    chama o validador do sistema de origem
 *    segue com o cadastramento do alvo no servidor
 *
 */

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pms.falasantos.Comunicacoes.RequestHttp;
import com.pms.falasantos.Globais;
import com.pms.falasantos.Outras.clAlvo;
import com.pms.falasantos.Outras.clCampo;
import com.pms.falasantos.R;
import com.pms.falasantos.RespostaConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddAlvoActivity extends AppCompatActivity implements RespostaConfig, View.OnClickListener
	{
	clAlvo clalvo = null;
	String noalvo;
	String idalvo = "";
	String area;
	String nopes;
	
	private enum State
		{
		nenhum,
		valida,
		cadalvo
		}
	State state = State.nenhum;
	
	boolean flokc = false;
	
	RequestHttp req = null;
	private ProgressDialog progress;
	
	int idOK, idCancel;
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		Globais.atividade = Globais.Atividade.AddAlvo;
		Globais.setContext( this );
		boolean ok = true;
		setContentView( R.layout.activity_add_alvo );
		//
		getSupportActionBar().setDisplayOptions( ActionBar.DISPLAY_SHOW_CUSTOM );
		getSupportActionBar().setDisplayShowCustomEnabled( true );
		getSupportActionBar().setCustomView( R.layout.actbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );
		//  obtem o alvo e monta a tela dinamica
		clalvo = Globais.getClalvo();
		area = clalvo.area;
		noalvo = clalvo.alvo;
		String aux = "Alvo: " + noalvo;
		((TextView) findViewById( R.id.txNoAlvo )).setText( "Alvo: " + clalvo.alvo );
		LinearLayout ll;
		LinearLayout.LayoutParams llp;
		TextView titCmp;
		EditText edtx;
		CheckBox ckbx;
		Spinner cbbx;
		int id = -1;
		
		if( clalvo.qtCampos() > 0 )
			{
			LinearLayout llalvo = (LinearLayout) findViewById( R.id.linAlvo );
			for( clCampo cmp : clalvo.campos )
				{
				ll = new LinearLayout( this );
				ll.setOrientation( LinearLayout.HORIZONTAL );
				ll.setWeightSum( 100 );
				llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,
				                                     LinearLayout.LayoutParams.WRAP_CONTENT );
				ll.setLayoutParams( llp );
				llalvo.addView( ll );
				id = View.generateViewId();
				cmp.setId( id );
				//  adiciona o título
				if( cmp.tipo != 30 && cmp.tipo != 31 && cmp.tipo != 32 && cmp.tipo != 40 )
					{
					llp = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 50f );
					titCmp = new TextView( this );
					titCmp.setText( cmp.nome );
					titCmp.setWidth( 0 );
					titCmp.setMinLines( 2 );
					titCmp.setMinHeight( 70 );
					titCmp.setLayoutParams( llp );
					ll.addView( titCmp );
					}
				switch( cmp.tipo )
					{
					case 1:     //  texto normal
						//  adiciona o editor de texto
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 50f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType( InputType.TYPE_CLASS_TEXT );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						else
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 256 ) } );
						ll.addView( edtx );
						break;
					case 2:     //  número
						//  adiciona o editor de texto
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 50f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						else
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 64 ) } );
						ll.addView( edtx );
						break;
					case 3:     //  número com sinal
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 50f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_FLAG_SIGNED );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						else
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 64 ) } );
						ll.addView( edtx );
						break;
					case 4:     //  senha
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 50f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						else
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 64 ) } );
						ll.addView( edtx );
						break;
					case 5:     //  senha numérica
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 50f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType(
							InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_PASSWORD );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						else
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 64 ) } );
						ll.addView( edtx );
						break;
					case 6:     //  telefone
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 50f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						else
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 64 ) } );
						ll.addView( edtx );
						break;
					case 7:     //  e-mail
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 50f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType(
							InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						else
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 128 ) } );
						ll.addView( edtx );
						break;
					case 21:     //  CEP
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 50f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER );
						edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 8 ) } );
						ll.addView( edtx );
						break;
					case 22:     //  horário
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 50f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType(
							InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME );
						edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 12 ) } );
						ll.addView( edtx );
						break;
					case 23:     //  data
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 50f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 10 );
						edtx.setInputType(
							InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE );
						edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 10 ) } );
						ll.addView( edtx );
						break;
					case 30:     //  box
						llp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
						titCmp = new TextView( this );
						titCmp.setText( cmp.nome );
						titCmp.setLayoutParams( llp );
						ll.addView( titCmp );
						
						cbbx = new Spinner( this );
						cbbx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT );
						cbbx.setLayoutParams( llp );
						ArrayList<String> itens = new ArrayList<>();
						for( String item : cmp.lsItens )
							{
							itens.add( item );
							}
						ArrayAdapter<String> cbadp = new ArrayAdapter<String>( this,
						                                                       android.R.layout.simple_spinner_dropdown_item,
						                                                       itens );
						cbbx.setAdapter( cbadp );
						ll.addView( cbbx );
						break;
					case 31:     //  checkbox
						ckbx = new CheckBox( this );
						ckbx.setId( id );
						ckbx.setText( cmp.nome );
						ll.addView( ckbx );
						break;
					case 40:
						if( Globais.config.sshd.length() == 8 )
							{
							llp = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 30f );
							titCmp = new TextView( this );
							titCmp.setText( cmp.nome );
							titCmp.setWidth( 0 );
							titCmp.setLayoutParams( llp );
							ll.addView( titCmp );
							//  adiciona o editor de texto
							edtx = new EditText( this );
							edtx.setId( id );
							llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
							                                     LinearLayout.LayoutParams.WRAP_CONTENT, 50f );
							edtx.setLayoutParams( llp );
							edtx.setWidth( 8 );
							edtx.setText( Globais.config.sshd );
							edtx.setEnabled( false );
							edtx.setInputType( InputType.TYPE_CLASS_TEXT );
							if( cmp.tamax > 0 )
								edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
							else
								edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 8 ) } );
							ll.addView( edtx );
							}
						else
							{
							String msg = "Este alvo esta disponível apenas para funcionários. " +
								"Se for este o seu caso, vá em configuração, selecione " +
								"'Funcionário ou terceiro da Prefeitura', " +
								"preencha seu SSHD e sua SENHA.\n Volte aqui para adicionar o alvo.";
							llp = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f );
							titCmp = new TextView( this );
							titCmp.setText( msg );
							titCmp.setWidth( 0 );
							titCmp.setMinLines( 2 );
							titCmp.setLayoutParams( llp );
							ll.addView( titCmp );
							ok = false;
							}
						break;
					}
				}
			ll = new LinearLayout( this );
			ll.setOrientation( LinearLayout.HORIZONTAL );
			ll.setGravity( Gravity.CENTER_HORIZONTAL );
			llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,
			                                     LinearLayout.LayoutParams.WRAP_CONTENT );
			ll.setLayoutParams( llp );
			llalvo.addView( ll );
			//  botões
			Button botao;
			//  botão de OK
			if( ok )
				{
				botao = new Button( this );
				botao.setText( "OK" );
				idOK = View.generateViewId();
				botao.setId( idOK );
				botao.setOnClickListener( this );
				ll.addView( botao );
				}
			//  botão cancel
			botao = new Button( this );
			botao.setText( "cancel" );
			idCancel = View.generateViewId();
			botao.setId( idCancel );
			botao.setOnClickListener( this );
			ll.addView( botao );
			}
		else
			{
				
			}
		}
	@Override
	protected void onResume()
		{
		super.onResume();
		Globais.atividade = Globais.Atividade.AddAlvo;
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
	
	public void onClick( View view )
		{
		if( view.getId() == idOK )
			{
			int id;
			EditText edtx;
			String val;
			clAlvo clalvo = Globais.getClalvo();
			String body = "{ \"alvo\": \"" + clalvo.id + "\",";
			body += "\"destinatario\": \"" + Globais.config.destin + "\",";
			body += "\"dispositivo\": \"" + Globais.config.nuserie + "\"";
			for( clCampo cmp : clalvo.campos )
				{
				id = cmp.id;
				body += ",";
				switch( cmp.tipo )
					{
					case 1:       //  tipos com EditText
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
					case 21:
					case 22:
					case 40:
						edtx = (EditText) findViewById( id );
						if( edtx.getText().toString().length() > 0 )
							{
							body += "\"" + cmp.campo + "\": \"";
							body += edtx.getText().toString() + "\"";
							}
						else
							{
							Globais.Alerta( this, "Atenção", "Todos os campos são obrigatórios" );
							return;
							}
						break;
					
					case 23:
						edtx = (EditText) findViewById( id );
						String vedtx = edtx.getText().toString();
						if( vedtx.length() < 1 )
							{
							Globais.Alerta( this, "Atenção", "Todos os campos são obrigatórios" );
							return;
							}
						if( vedtx.length() != 8 &&
							vedtx.length() != 10 )
							{
							Globais.Alerta( this, "Data inválida",
							                "Deve ser no formato DDMMAAAA ou DD/MM/AAAA" );
							return;
							}
						Date date = null;
						SimpleDateFormat sdf;
						if( vedtx.length() == 8 )
							{
							sdf = new SimpleDateFormat( "ddmmyyyy" );
							try
								{
								date = sdf.parse( vedtx );
								}
							catch(ParseException pexc )
								{
								Globais.Alerta( this, "Data inválida",
								                "Deve ser no formato DDMMAAAA ou DD/MM/AAAA" );
								return;
								}
							}
						if( vedtx.length() == 10 )
							{
							sdf = new SimpleDateFormat( "dd/mm/yyyy" );
							try
								{
								date = sdf.parse( vedtx );
								}
							catch(ParseException pexc )
								{
								Globais.Alerta( this, "Data inválida",
								                "Deve ser no formato DDMMAAAA ou DD/MM/AAAA" );
								return;
								}
							}
						sdf = new SimpleDateFormat( "dd/mm/yyyy" );
						body += "\"" + cmp.campo + "\": \"";
						body += sdf.format( date ) + "\"";
						break;
						
					case 30:
						Spinner cbbx = (Spinner) findViewById( id );
						if( cbbx.getSelectedItem().toString().length() > 0 )
							{
							body += "\"" + cmp.campo + "\": \"";
							body += cbbx.getSelectedItem().toString() + "\"";
							}
						else
							{
							Globais.Alerta( this, "Atenção", "Todos os campos são obrigatórios" );
							return;
							}
						break;
					
					case 31:
						CheckBox ckbx = (CheckBox) findViewById( id );
						body += "\"" + cmp.campo + "\": ";
						if( ckbx.isChecked() )
							body += "1";
						else
							body += "0";
						break;
					}
				}
			body += " }";
			//  chama o validador
			progress = ProgressDialog.show( this, "Por favor, espere...", "Validando os dados digitados...",
			                                true );
			state = State.valida;
			req = new RequestHttp( this );
			String url = clalvo.service;
			//req.setAuth( sshd, senha );
			req.setBody( body );
			req.delegate = this;
			req.execute( url );
			}
		if( view.getId() == idCancel )
			{
			finish();
			}
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
				if( erro.contains( "exclusiv") )
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
			switch( state )
				{
				case valida:
					if( !jobj.has( "id" ))
						{
						Globais.Alerta( this, "Erro do validador", "Deve retornar o ID da pessoa" );
						return;
						}
					if( !jobj.has( "nomealvo" ))
						{
						Globais.Alerta( this, "Erro do validador", "Deve retornar o nome do alvo" );
						return;
						}
					
					idalvo = jobj.getString( "id" );
					Globais.obterConfig();
					
					progress = ProgressDialog.show( this, "Por favor, espere...", "Cadastrando o alvo...",
					                                true );
					state = State.cadalvo;
					req = new RequestHttp( this );
					String url = Globais.dominio + "/partes/funcoes.php?func=cadalvo";
					nopes = jobj.getString( "nomealvo" );
					url += "&idtia=" + URLEncoder.encode( ""+clalvo.id );
					url += "&iddis=" + URLEncoder.encode( ""+Globais.config.iddis );
					url += "&idpes=" + URLEncoder.encode( jobj.getString( "id" ) );
					url += "&nome=" + URLEncoder.encode( nopes );
					req.delegate = this;
					req.execute( url );
					break;
				
				case cadalvo:
					long ixarea = Globais.ixArea( area );
					if( ixarea < 0 )
						{
						Globais.Alerta( this, "Banco de dados corrompido", "incapaz de cadastrar a área" );
						Log.i( Globais.apptag, "Globais: criaTabelas dispositivo " + "incapaz de cadastrar a área" );
						return;
						}
					long ixalvo = jobj.getLong( "id" );
					ContentValues cv = new ContentValues( 5 );
					cv.put( "alv_id", ixalvo );
					cv.put( "are_id", ixarea );
					cv.put( "alv_nome", nopes );
					ixalvo = Globais.db.insertOrThrow( "alvos", null, cv );
					if( ixalvo < 0 )
						{
						Globais.Alerta( this, "Banco de dados corrompido", "incapaz de cadastrar o alvo" );
						Log.i( Globais.apptag, "Globais: criaTabelas dispositivo " + "incapaz de cadastrar o alvo" );
						return;
						}
					Globais.Alerta( this, "Alvo cadastrado", "A partir de agora, você receberá mensagens deste alvo" );
					finish();
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
	}
