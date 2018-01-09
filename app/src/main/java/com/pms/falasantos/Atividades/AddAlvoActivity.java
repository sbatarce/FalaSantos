package com.pms.falasantos.Atividades;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;

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
		setContentView( R.layout.activity_add_alvo );
		//
		getSupportActionBar().setDisplayOptions( ActionBar.DISPLAY_SHOW_CUSTOM );
		getSupportActionBar().setDisplayShowCustomEnabled( true );
		getSupportActionBar().setCustomView( R.layout.actbar );
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
				if( cmp.tipo != 30 && cmp.tipo != 31 && cmp.tipo != 32 )
					{
					llp = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 30f );
					titCmp = new TextView( this );
					titCmp.setText( cmp.nome );
					titCmp.setWidth( 0 );
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
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 70f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType( InputType.TYPE_CLASS_TEXT );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						ll.addView( edtx );
						break;
					case 2:     //  número
						//  adiciona o editor de texto
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 70f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						ll.addView( edtx );
						break;
					case 3:     //  número com sinal
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 70f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_FLAG_SIGNED );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						ll.addView( edtx );
						break;
					case 4:     //  senha
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 70f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						ll.addView( edtx );
						break;
					case 5:     //  senha numérica
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 70f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType(
							InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_PASSWORD );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						ll.addView( edtx );
						break;
					case 6:     //  telefone
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 70f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						ll.addView( edtx );
						break;
					case 7:     //  e-mail
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 70f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType(
							InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS );
						if( cmp.tamax > 0 )
							edtx.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( cmp.tamax ) } );
						ll.addView( edtx );
						break;
					case 21:     //  CEP
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 70f );
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
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 70f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType(
							InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME );
						ll.addView( edtx );
						break;
					case 23:     //  data
						edtx = new EditText( this );
						edtx.setId( id );
						llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
						                                     LinearLayout.LayoutParams.WRAP_CONTENT, 70f );
						edtx.setLayoutParams( llp );
						edtx.setWidth( 0 );
						edtx.setInputType(
							InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE );
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
						if( Globais.config.sshd.length() != 8 )
							{
							String msg = "Este alvo esta disponível apenas para funcionários.\n" +
								"Se for este o seu caso, vá em configuração, selecione\n" +
								"'Funcionário ou terceiro da Prefeitura',\n" +
								"preencha seu SSHD e sua SENHA.";
							Globais.Alerta( this, "Acesso negado", msg );
							finish();
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
			//  botão de OK
			Button botao = new Button( this );
			botao.setText( "OK" );
			idOK = View.generateViewId();
			botao.setId( idOK );
			botao.setOnClickListener( this );
			ll.addView( botao );
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
	
	public void onClick( View view )
		{
		if( view.getId() == idOK )
			{
			int id;
			EditText edtx;
			String val;
			clAlvo clalvo = Globais.getClalvo();
			String body = "{ \"alvo\": \"" + noalvo + "\"";
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
					case 23:
						edtx = (EditText) findViewById( id );
						if( edtx.getText().toString().length() > 0 )
							{
							body += "\"" + cmp.campo + "\": \"";
							body += edtx.getText().toString() + "\"";
							}
						break;
					
					case 30:
						Spinner cbbx = (Spinner) findViewById( id );
						if( cbbx.getSelectedItem().toString().length() > 0 )
							{
							body += "\"" + cmp.campo + "\": \"";
							body += cbbx.getSelectedItem().toString() + "\"";
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
					
					case 40:
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
					Globais.Alerta( this, "Acesso negado", "SSHD e/ou senha não corretos" );
				else
					Globais.Alerta( this, "Resposta do servidor com erro", erro );
				return;
				}
			if( !jobj.has( "status" ) )
				{
				Globais.Alerta( this, "Resposta do servidor com erro",
				                "Resposta sem indicativo de estado" );
				return;
				}
			if( !jobj.getString( "status" ).equals( "OK" ) && !jobj.getString( "status" ).equals( "ok" ) )
				{
				String status = jobj.getString( "status" );
				Globais.Alerta( this, "Resposta do servidor com erro", status );
				return;
				}
			switch( state )
				{
				case valida:
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
					int id = jobj.getInt( "id" );
					long ixarea = Globais.ixArea( area );
					if( ixarea < 0 )
						{
						Globais.Alerta( this, "Banco de dados corrompido", "incapaz de cadastrar a área" );
						Log.i( Globais.apptag, "Globais: criaTabelas dispositivo " + "incapaz de cadastrar a área" );
						return;
						}
					ContentValues cv = new ContentValues( 5 );
					cv.put( "are_id", ixarea );
					cv.put( "alv_nome", nopes );
					long ixalvo = Globais.db.insertOrThrow( "alvos", null, cv );
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
