package com.pms.falasantos.Atividades;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pms.falasantos.Globais;
import com.pms.falasantos.R;

public class ConfidenActivity extends AppCompatActivity
	{
	EditText txque, txsen;
	boolean flalt = false;
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_confiden );
		//
		getSupportActionBar().setDisplayOptions( ActionBar.DISPLAY_SHOW_CUSTOM );
		getSupportActionBar().setDisplayShowCustomEnabled( true );
		getSupportActionBar().setCustomView( R.layout.actbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );
		//
		txque = (EditText) findViewById( R.id.txQuestao );
		txsen = (EditText) findViewById( R.id.txSenhaConf );
		txque.addTextChangedListener( new TextWatcher()
			{
			@Override
			public void beforeTextChanged( CharSequence s, int start, int count, int after )
				{
				
				}
			@Override
			public void onTextChanged( CharSequence s, int start, int before, int count )
				{
				flalt = true;
				}
			@Override
			public void afterTextChanged( Editable s )
				{
				
				}
			} );
		txsen.addTextChangedListener( new TextWatcher()
			{
			@Override
			public void beforeTextChanged( CharSequence s, int start, int count, int after )
				{
				
				}
			@Override
			public void onTextChanged( CharSequence s, int start, int before, int count )
				{
				flalt = true;
				}
			@Override
			public void afterTextChanged( Editable s )
				{
				
				}
			} );
		//
		String txt =
			"Preencha abaixo uma \"Pergunta Secreta\" e uma \"senha\" para abrir " +
				"mensagens confidenciais.\n" +
				"A senha deve ser a resposta à pergunta que você escreveu na " +
				"\"Pergunta Secreta\".\n" +
				"Por exemplo:\n" +
				"Pergunta Secreta: qual o nome do cãozinho do Franjinha?\n" +
				"Senha: floquinho\n" +
				"Se você esquecer a senha, a \"Pergunta Secreta\" será mostrada para " +
				"te ajudar a lembrá-la.\n";
		((TextView) findViewById( R.id.txHelp )).setText( txt );
		Globais.obterConfig();
		if( Globais.config.senhaconf.length() > 0 )
			{
			final AlertDialog.Builder alert = new AlertDialog.Builder( this );
			alert.setTitle( "Senha de confidencialidade" );
			alert.setMessage( Globais.config.questao );
			alert.setCancelable( false );
			final EditText txsen = new EditText( this );
			alert.setView( txsen );
			alert.setNegativeButton( "Cancel", new DialogInterface.OnClickListener()
				{
				@Override
				public void onClick( DialogInterface dialog, int which )
					{
					finish();
					}
				} );
			alert.setPositiveButton( "Ok", new DialogInterface.OnClickListener()
				{
				public void onClick( DialogInterface dialog, int whichButton )
					{
					}
				} );
			final AlertDialog dlg = alert.create();
			dlg.show();
			
			Button btpos = dlg.getButton( AlertDialog.BUTTON_POSITIVE );
			btpos.setOnClickListener( new View.OnClickListener()
				{
				@Override
				public void onClick( View v )
					{
					String senha = txsen.getText().toString();
					if( senha.equals( Globais.config.senhaconf ) )
						dlg.dismiss();
					else
						txsen.setTextColor( getResources().getColor( R.color.colorError ) );
					}
				} );
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
	
	public void onClick( View v )
		{
		if( v == findViewById( R.id.btConfOK ) )
			{
			if( !flalt )
				{
				finish();
				return;
				}
			String quest = txque.getText().toString();
			String senha = txsen.getText().toString();
			if( quest.length() < 1 )
				{
				Globais.Alerta( this, "Por favor!", "Especifique uma Pergunta Secreta" );
				return;
				}
			if( senha.length() < 5 )
				{
				Globais.Alerta( this, "Por favor!", "A senha deve ter pelo menos 5 caracteres" );
				return;
				}
			ContentValues cv = new ContentValues( 5 );
			cv.put( "dis_frase", quest );
			cv.put( "dis_senha", senha );
			try
				{
				int ret = Globais.db.update( "dispositivo", cv, null, null );
				}
			catch( Exception exc )
				{
				Log.i( Globais.apptag, "Update dispositivo: " + exc.getMessage() );
				Globais.Alerta( this, "Banco de dados corrompido!", exc.getMessage() );
				return;
				}
			Globais.obterConfig();
			finish();
			}
		if( v == findViewById( R.id.btConfCanc ) )
			{
			if( flalt )
				{
				AlertDialog dlg = Globais.dlgOKCancel( this, "Alterações não salvas",
				                                "Voce efetuou alterações que não foram salvas ainda.\n" +
				                      "Se voce Clicar em \"prosseguir\" elas serão perdidadas.\n" +
				                      "Clique em \"voltar\" para salvá-las", "prosseguir", "salvar" );
				dlg.show();
				Button btpos = dlg.getButton( AlertDialog.BUTTON_POSITIVE );
				btpos.setOnClickListener( new View.OnClickListener()
					{
					@Override
					public void onClick( View v )
						{
						finish();
						}
					} );
				return;
				}
			finish();
			}
		return;
		}
	}
