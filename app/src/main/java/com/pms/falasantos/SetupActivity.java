package com.pms.falasantos;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SetupActivity extends AppCompatActivity implements View.OnClickListener
	{
	@Override
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_setup );
		//
		if( !Globais.obterConfig() )
			{
			Toast.makeText( this, "O banco de dados de seu aplicativo foi corrompido. Por favor reinstale.", Toast.LENGTH_LONG ).show();
			this.finish();
			}
		if( !Globais.isConnected() && !Globais.fezSetup() )
			{
			AlertDialog alertDialog = new AlertDialog.Builder( this).create();
			alertDialog.setTitle("");
			alertDialog.setMessage("Welcome to dear user.");
			alertDialog.setButton( 1, "OK", new DialogInterface.OnClickListener()
				{
				@Override
				public void onClick( DialogInterface dialogInterface, int i )
					{
						
					}
				} );
			alertDialog.show();
			}
		((EditText) findViewById( R.id.txSocial )).setText( Globais.config.noSocial );
		((EditText) findViewById( R.id.txHrInicial )).setText( Globais.config.hoini );
		((EditText) findViewById( R.id.txHrFinal )).setText( Globais.config.hofim );
		if( Globais.config.flWIFI )
			((CheckBox) findViewById( R.id.ckWIFI )).setChecked( true );
		else
			((CheckBox) findViewById( R.id.ckWIFI )).setChecked( false );
		if( Globais.config.flSilen )
			((CheckBox) findViewById( R.id.ckSilenc )).setChecked( true );
		else
			((CheckBox) findViewById( R.id.ckSilenc )).setChecked( false );
		}
	
	@Override
	public void onWindowFocusChanged( boolean hasFocus )
		{
		super.onWindowFocusChanged( hasFocus );
		if( hasFocus )
			Globais.setEmUso();
		else
			Globais.setSemUso();
		}
	
	@Override
	public void onClick( View v )
		{
		if( v == findViewById( R.id.btSavConfig ) )
			{
			if( ((EditText) findViewById( R.id.txSocial )).length() < 1 )
				{
				Toast.makeText( this, "Por favor, especifique seu nome social.", Toast.LENGTH_SHORT ).show();
				return;
				}
			if( ((EditText) findViewById( R.id.txHrInicial )).length() < 4 ||
				((EditText) findViewById( R.id.txHrInicial )).length() > 5 )
				{
				Toast.makeText( this, "Por favor, especifique a hora inicial no formato HH:MM ou H:MM", Toast.LENGTH_SHORT ).show();
				return;
				}
			if( ((EditText) findViewById( R.id.txHrFinal )).length() < 4 ||
				((EditText) findViewById( R.id.txHrFinal )).length() > 5 )
				{
				Toast.makeText( this, "Por favor, especifique a hora final no formato HH:MM ou H:MM", Toast.LENGTH_SHORT ).show();
				return;
				}
			//
			String txnome = ((EditText) findViewById( R.id.txSocial )).getText().toString();
			String txhrin = ((EditText) findViewById( R.id.txHrInicial )).getText().toString();
			String txhrfi = ((EditText) findViewById( R.id.txHrFinal )).getText().toString();
			boolean ckwifi = ((CheckBox) findViewById( R.id.ckWIFI )).isChecked();
			boolean cksile = ((CheckBox) findViewById( R.id.ckSilenc )).isChecked();
			//
			String hoini = Globais.toHoraDB( txhrin );
			if( hoini == null )
				{
				Toast.makeText( this, "Por favor, especifique a hora inicial no formato HH:MM ou H:MM", Toast.LENGTH_SHORT ).show();
				return;
				}
			String hofin = Globais.toHoraDB( txhrfi );
			if( hofin == null )
				{
				Toast.makeText( this, "Por favor, especifique a hora final no formato HH:MM ou H:MM", Toast.LENGTH_SHORT ).show();
				return;
				}
			//
			try
				{
				ContentValues cv = new ContentValues( 10 );
				cv.put( "noSocial", txnome );
				cv.put( "hoInic", hoini );
				cv.put( "hoTerm", hofin );
				if( ckwifi )
					cv.put( "flWIFI", 1 );
				else
					cv.put( "flWIFI", 0 );
				if( cksile )
					cv.put( "flSilen", 1 );
				else
					cv.put( "flSilen", 0 );
				int ret = Globais.db.update( "config", cv, "idConfig=1", null );
				if( ret < 1 )
					{
					Toast.makeText( this, "O banco de dados de seu aplicativo foi corrompido. Por favor reinstale.", Toast.LENGTH_LONG ).show();
					return;
					}
				}
			catch( Exception exc )
				{
				Log.i( Globais.tag, "Erro " + exc.getMessage() );
				Toast.makeText( this, "Erro: " + exc.getMessage(), Toast.LENGTH_LONG ).show();
				return;
				}
			this.finish();
			}
		}
	}
