package com.pms.falasantos.Atividades;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pms.falasantos.Globais;
import com.pms.falasantos.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MensagemActivity extends AppCompatActivity implements View.OnClickListener
	{
	ScrollView                scroll;
	LinearLayout              llmens;
	LinearLayout              ll;
	LinearLayout.LayoutParams llp;
	TextView                  txtx;
	EditText                  edtx;
	CheckBox                  ckbx;
	Spinner                   cbbx;
	RadioButton               rbbt;
	RadioGroup                rbgr;
	
	Button  btmand, btsalv, btcanc;
	int idacts, idmand, idsalv, idcanc;
	String dtresp;
	
	public boolean flsalvo;
	private boolean flfim = false;
	private boolean flsav = false;
	
	int left = 20;
	
	int idmens = -1;
	String titulo;
	//  objetos auxiliares
	JSONArray  corpos = new JSONArray();
	JSONObject corpo  = null;
	JSONArray  opcoes = null;
	JSONObject opcao  = null;
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_mensagem );
		Globais.atividade = Globais.Atividade.Mensagem;
		Globais.setContext( this );
		//
		Intent mensg = getIntent();
		idmens = Integer.parseInt( mensg.getStringExtra( "idmens" ) );
		titulo = mensg.getStringExtra( "titulo" );
		dtresp = mensg.getStringExtra( "dtresp" );
		flsalvo = true;
		//
		llmens = (LinearLayout) findViewById( R.id.llmensagem );
		llmens.removeAllViews();
		//
		((TextView) findViewById( R.id.txTitMens )).setText( titulo );
		setupMens();
		}
	@Override
	protected void onPause()
		{
		super.onPause();
		Globais.atividade = Globais.Atividade.nenhuma;
		}
	@Override
	protected void onResume()
		{
		super.onResume();
		Globais.atividade = Globais.Atividade.Mensagem;
		Globais.setContext( this );
		}
	@Override
	public void onClick( View view )
		{
		if( view.getId() == idmand )
			{
			if( enviar() )
				{
				Toast.makeText( this,
				                "Processo de envio encerrado normalmente.",
				                Toast.LENGTH_LONG ).show();
				if( Globais.isConnected() )
					{
					Globais.pFBMens.mandaRespostas();
					}
				finish();
				}
			}
		if( view.getId() == idsalv )
			{
			if( salvar() )
				Toast.makeText( this,
				                "Dados salvos.",
				                Toast.LENGTH_LONG ).show();
			}
		if( view.getId() == idcanc )
			{
			cancelar();
			}
		}
	
	@Override
	public void onBackPressed()
		{
		cancelar();
		}

	@Override
	public void onWindowFocusChanged( boolean hasFocus )
		{
		super.onWindowFocusChanged( hasFocus );
		if( hasFocus )
			{
			if( flfim )
				{
				if( Globais.alertResu )
					salvar();
				finish();
				}
			}
		}
	
	///////////////////////////////////////
	//  rotinas de salvamento de dados
	
	//  salva a resposta TEXTO no corpo
	private boolean salvaResposta( JSONObject corpo )
		{
		try
			{
			edtx = (EditText)findViewById( corpo.getInt( "cmpid" ) );
			String resposta = edtx.getText().toString();
			if( corpo.has( "resposta" ))
				{
				if( resposta.equals( corpo.getString( "resposta" ) ) )
					return true;
				}
			else
				{
				if( resposta.equals( "" ) )
					return true;
				}
			String sql = "UPDATE corpo set cor_flatua=1, cor_resposta='" + resposta +
				"' WHERE cor_id=" + corpo.getInt( "id" );
			Globais.db.execSQL( sql );
			corpo.put( "resposta", resposta );
			return true;
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return false;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return false;
			}
		}
	//  salva CheckBoxs
	private boolean salvaCheck( JSONObject corpo )
		{
		String sql;
		try
			{
			opcoes = corpo.getJSONArray( "opcoes" );
			int qtopt = opcoes.length();
			boolean flatua = false;
			for( int ixopt=0; ixopt<qtopt; ixopt++ )
				{
				opcao = opcoes.getJSONObject( ixopt );
				ckbx = (CheckBox)findViewById( opcao.getInt( "cmpid" ) );
				boolean ckatua = ckbx.isChecked();
				boolean ckinit = opcao.getBoolean( "check" );
				if( ckinit != ckatua )
					{
					String chk = "0";
					if( ckatua )
						chk = "1";
					sql = "UPDATE opcoes SET opt_flatua=1, opt_flchecked=" + chk +
						" WHERE opt_id=" + opcao.getInt( "id" );
					Globais.db.execSQL( sql );
					opcao.put("check", ckatua );
					flatua = true;
					}
				}
			if( flatua )
				{
				sql = "UPDATE corpo SET cor_flatua=1 WHERE cor_id=" + corpo.getInt( "id" );
				Globais.db.execSQL( sql );
				}
			return true;
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return false;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return false;
			}
		}
	//  salva RadioGroup
	private boolean salvaRadio( JSONObject corpo )
		{
		String sql;
		try
			{
			opcoes = corpo.getJSONArray( "opcoes" );
			int qtopt = opcoes.length();
			boolean flatua = false;
			for( int ixopt=0; ixopt<qtopt; ixopt++ )
				{
				opcao = opcoes.getJSONObject( ixopt );
				rbbt = (RadioButton) findViewById( opcao.getInt( "cmpid" ) );
				boolean ckatua = rbbt.isChecked();
				boolean ckinit = opcao.getBoolean( "check" );
				if( ckinit != ckatua )
					{
					String chk = "0";
					if( ckatua )
						chk = "1";
					sql = "UPDATE opcoes SET opt_flatua=1, opt_flchecked=" + chk +
						" WHERE opt_id=" + opcao.getInt( "id" );
					Globais.db.execSQL( sql );
					opcao.put("check", ckatua );
					flatua = true;
					}
				}
			if( flatua )
				{
				sql = "UPDATE corpo SET cor_flatua=1 WHERE cor_id=" + corpo.getInt( "id" );
				Globais.db.execSQL( sql );
				}
			return true;
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return false;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return false;
			}
		}
	//  salva combobox
	private boolean salvaCombo( JSONObject corpo )
		{
		String sql;
		try
			{
			opcoes = corpo.getJSONArray( "opcoes" );
			cbbx = (Spinner)findViewById( corpo.getInt( "cmpid" ) );
			int selatua = cbbx.getSelectedItemPosition()-1;
			int qtopt = opcoes.length();
			boolean flatua = false;
			for( int ixopt=0; ixopt<qtopt; ixopt++ )
				{
				opcao = opcoes.getJSONObject( ixopt );
				if( opcao.getBoolean( "check" ) &&
						selatua != ixopt )
					{
					sql = "UPDATE opcoes SET opt_flatua=1, opt_flchecked=0 " +
						"WHERE opt_id=" + opcao.getInt( "id" );
					Globais.db.execSQL( sql );
					opcao.put( "check", false );
					flatua = true;
					}
				if( !opcao.getBoolean( "check" ) &&
						selatua == ixopt )
					{
					sql = "UPDATE opcoes SET opt_flatua=1, opt_flchecked=1 " +
						"WHERE opt_id=" + opcao.getInt( "id" );
					Globais.db.execSQL( sql );
					flatua = true;
					opcao.put( "check", true );
					}
				}
			if( flatua )
				{
				sql = "UPDATE corpo SET cor_flatua=1 WHERE cor_id=" + corpo.getInt( "id" );
				Globais.db.execSQL( sql );
				}
			return true;
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return false;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return false;
			}
		}
	
	//  atendimento aos botões
	private boolean alterado()
		{
		int qtopt;
		try
			{
			int qtcor = corpos.length();
			for( int ixcor=0; ixcor<qtcor; ixcor++ )
				{
				corpo = corpos.getJSONObject( ixcor );
				if( !corpo.getBoolean( "temresp" ) )
					continue;
				switch( corpo.getInt( "tipo" ) )
					{
					case 1:
						edtx = (EditText)findViewById( corpo.getInt( "cmpid" ) );
						String stinic;
						if( corpo.has("resposta") )
							stinic = corpo.getString( "resposta" );
						else
							stinic = "";
						String statua = edtx.getText().toString();
						if( !statua.equals( stinic ) )
							return true;
						break;
					case 2:
					case 3:
						break;
					case 4:
					case 5:
						opcoes = corpo.getJSONArray( "opcoes" );
						qtopt = opcoes.length();
						for( int ixopt=0; ixopt<qtopt; ixopt++ )
							{
							opcao = opcoes.getJSONObject( ixopt );
							ckbx = (CheckBox)findViewById( opcao.getInt( "cmpid" ) );
							boolean ckinit = ckbx.isChecked();
							boolean ckatua = opcao.getBoolean( "check" );
							if( ckinit != ckatua )
								return true;
							}
						break;
					case 6:
						opcoes = corpo.getJSONArray( "opcoes" );
						cbbx = (Spinner)findViewById( corpo.getInt( "cmpid" ) );
						int selatua = cbbx.getSelectedItemPosition();
						qtopt = opcoes.length();
						int selinic = 0;
						for( int ixopt=0; ixopt<qtopt; ixopt++ )
							{
							opcao = opcoes.getJSONObject( ixopt );
							if( opcao.getBoolean( "check" ) )
								{
								selinic = ixopt+1;
								break;
								}
							}
						if( selinic != selatua )
							return true;
						break;
					}
				}
			return false;
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return false;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return false;
			}
		}
	
	private boolean enviar()
		{
		salvar();
		if( !verRespostas() )
			{
			String msg = "Ainda há questões obrigatórias não respondidas.\n" +
				"Você deve responder todas as questões marcadas com *.\n" +
				"Alternativamente, você pode salvar as respostas já preenchidas\n" +
				"tocando em SALVAR e voltar mais tarde para responder as demais\n" +
				"sem perder o que você já preencheu.";
			Globais.Alerta( this, "Atenção", msg );
			return false;
			}
		String dt = Globais.agoraDB();
		String sql = "UPDATE mensagens SET msg_dtresp='" + dt + "' "+
			"WHERE msg_id=" + idmens;
		try
			{
			Globais.db.execSQL( sql );
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, exc.getMessage() );
			return false;
			}
		return true;
		}
	
	private boolean salvar()
		{
		try
			{
			int qtcor = corpos.length();
			for( int ixcor=0; ixcor<qtcor; ixcor++ )
				{
				corpo = corpos.getJSONObject( ixcor );
				if( !corpo.getBoolean( "temresp" ) )
					continue;
				switch( corpo.getInt( "tipo" ) )
					{
					case 1:
						salvaResposta( corpo );
						break;
					case 2:
					case 3:
						break;
					case 4:
						salvaRadio( corpo );
					case 5:
						salvaCheck( corpo );
						break;
					case 6:
						salvaCombo( corpo );
						break;
					}
				}
			return true;
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return false;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return false;
			}
		}
	
	//  verifica se todas as respostas obrigatórias foram dadas
	private boolean verRespostas()
		{
		int qtopt;
		boolean flchk;
		try
			{
			int qtcor = corpos.length();
			for( int ixcor=0; ixcor<qtcor; ixcor++ )
				{
				corpo = corpos.getJSONObject( ixcor );
				if( !corpo.getBoolean( "temresp" ) )
					continue;
				if( !corpo.getBoolean( "obrig" ) )
					continue;
				switch( corpo.getInt( "tipo" ) )
					{
					case 1:
						if( !corpo.has("resposta") )
							return false;
						if( corpo.getString( "resposta" ).length() < 1 )
							return false;
						break;
					case 2:
					case 3:
						break;
					case 4:
					case 5:
						opcoes = corpo.getJSONArray( "opcoes" );
						qtopt = opcoes.length();
						flchk = false;
						for( int ixopt=0; ixopt<qtopt; ixopt++ )
							{
							opcao = opcoes.getJSONObject( ixopt );
							if( opcao.getBoolean( "check" ) )
								flchk = true;
							}
						if( !flchk )
							return false;
						break;
					case 6:
						opcoes = corpo.getJSONArray( "opcoes" );
						qtopt = opcoes.length();
						flchk = false;
						for( int ixopt=0; ixopt<qtopt; ixopt++ )
							{
							opcao = opcoes.getJSONObject( ixopt );
							if( opcao.getBoolean( "check" ) )
								flchk = true;
							}
						if( !flchk )
							return false;
						break;
					}
				}
			return true;
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return false;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return false;
			}
		}
	
	private void cancelar()
		{
		if( dtresp != null )
			{
			finish();
			return;
			}
			
		if( alterado() )
			{
			flfim = true;
			String msg = "Você tem alterações não salvas nesta tela\n" +
				"Se sair agora, vai perdê-las.\n" +
				"Para não perder seu trabalho toque em SALVAR \n" +
				"e seu trabalho será salvo antes de sair";
			Globais.AlertaOKCancel( this, "Cuidado", msg, "Salvar", "Esquecer" );
			}
		else
			finish();
		}

	private int adcStatic( String nocmp, String txt, boolean flobr )
		{
		int id = View.generateViewId();
		ll = new LinearLayout( this );
		ll.setOrientation( LinearLayout.HORIZONTAL );
		llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,
		                                     LinearLayout.LayoutParams.WRAP_CONTENT  );
		
		txtx = new TextView( this );
		txtx.setText( txt );
		txtx.setId( id );
		if( flobr )
			txtx.setTextColor( Color.parseColor( "#FF0000" ) );
		else
			txtx.setTextColor( Color.parseColor( "#000000" ) );
		llmens.addView( txtx );
		return id;
		}
	
	private void adcEdtTx( JSONObject corpo )
		{
		try
			{
			ll = new LinearLayout( this );
			LinearLayout.LayoutParams llparms = new RadioGroup.LayoutParams( this, null );
			llparms.setMargins( left, 0, 0, 0 );
			ll.setLayoutParams( llparms );
			ll.setOrientation( LinearLayout.VERTICAL );
			
			llmens.addView( ll );
			
			edtx = new EditText( this );
			int id = View.generateViewId();
			edtx.setId( id );
			corpo.put( "cmpid", id );
			if( corpo.has("resposta") )
				edtx.setText( corpo.getString( "resposta" ) );
			else
				edtx.setText( "" );
			edtx.setMinimumWidth( 50 );
			if( dtresp != null )
				edtx.setEnabled( false );
			ll.addView( edtx );
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return;
			}
		}
	
	private void adcRGroup( JSONArray opcoes )
		{
		try
			{
			rbgr = new RadioGroup( this );
			RadioGroup.LayoutParams rbparms = new RadioGroup.LayoutParams( this, null );
			rbparms.setMargins( left, 0, 0, 0 );
			rbgr.setLayoutParams( rbparms );
			
			llmens.addView( rbgr );
			for( int ixopt=0; ixopt<opcoes.length(); ixopt++ )
				{
				JSONObject opcao = opcoes.getJSONObject(ixopt);
				rbbt = new RadioButton( this );
				int id = View.generateViewId();
				opcao.put( "cmpid", id );
				rbbt.setId( id );
				rbbt.setText( opcao.getString( "texto" ) );
				if( opcao.getBoolean( "check" ) )
					rbbt.setChecked( true );
				else
					rbbt.setChecked( false );
				if( dtresp != null )
					rbbt.setEnabled( false );
				rbgr.addView( rbbt );
				}
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return;
			}
		}
	
	private void adcCheck( JSONArray opcoes )
		{
		try
			{
			ll = new LinearLayout( this );
			LinearLayout.LayoutParams llparms = new RadioGroup.LayoutParams( this, null );
			llparms.setMargins( left, 0, 0, 0 );
			ll.setLayoutParams( llparms );
			ll.setOrientation( LinearLayout.VERTICAL );
			
			llmens.addView( ll );
			
			for( int ixopt=0; ixopt<opcoes.length(); ixopt++ )
				{
				JSONObject opcao = opcoes.getJSONObject(ixopt);
				ckbx = new CheckBox( this );
				int id = View.generateViewId();
				opcao.put( "cmpid", id );
				ckbx.setId( id );
				ckbx.setText( opcao.getString( "texto" ) );
				if( opcao.getBoolean( "check" ) )
					ckbx.setChecked( true );
				if( dtresp != null )
					ckbx.setEnabled( false );
				ll.addView( ckbx );
				}
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return;
			}
		}
	
	private void adcCombo( JSONObject corpo )
		{
		try
			{
			ll = new LinearLayout( this );
			LinearLayout.LayoutParams llparms = new RadioGroup.LayoutParams( this, null );
			llparms.setMargins( left, 0, 0, 0 );
			ll.setLayoutParams( llparms );
			ll.setOrientation( LinearLayout.VERTICAL );
			
			llmens.addView( ll );
			
			cbbx = new Spinner( this );
			int id = View.generateViewId();
			cbbx.setId( id );
			corpo.put( "cmpid", id );
			//  carrega a combobox
			JSONArray opcoes = corpo.getJSONArray( "opcoes" );
			ArrayList<String> itens = new ArrayList<>();
			itens.add( "Escolha:" );
			int pos = 0;
			for( int ixopt=0; ixopt<opcoes.length(); ixopt++ )
				{
				JSONObject opcao = opcoes.getJSONObject( ixopt );
				itens.add( opcao.getString( "texto" ) );
				if( opcao.getBoolean( "check" ) )
					pos = ixopt+1;
				}
			ArrayAdapter<String> cbadp = new
				ArrayAdapter<String>( this,
                          android.R.layout.simple_spinner_dropdown_item,
                           itens );
			cbbx.setAdapter( cbadp );
			cbbx.setSelection( pos );
			if( dtresp != null )
				cbbx.setEnabled( false );
			ll.addView( cbbx );
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return;
			}
		}
	
	private void setupMens()
		{
		try
			{
			String sql = "SELECT cor.cor_id, cor_ticorpo, cor_corpo, cor_texto," +
				"cor_resposta, cor_stresposta, cor_stobrigatoria," +
				"opt_id, opt_codigo, opt_texto, opt_flchecked, opt_flatua " +
				"FROM         corpo cor " +
				"left join   opcoes opt ON " +
				"             opt.cor_id=cor.cor_id " +
				"WHERE cor.msg_id=? ";
			String[] parm = new String[]{ "" + idmens };
			String[] lista;
			Cursor curopt = Globais.db.rawQuery( sql, parm );
			
			String stjson = "{ \"corpos\":[";
			int ticor = 0;
			String codigo, texto, resposta;
			boolean flres, flobr;
			int corant = -1;
			
			while( curopt.moveToNext() )
				{
				int corid = curopt.getInt( curopt.getColumnIndex( "cor_id" ) );
				if( corid != corant )
					{
					if( corpo != null )
						{
						corpos.put( corpo );
						}
					ticor = curopt.getInt( curopt.getColumnIndex( "cor_ticorpo" ) );
					codigo = curopt.getString( curopt.getColumnIndex( "cor_corpo" ) );
					texto = curopt.getString( curopt.getColumnIndex( "cor_texto" ) );
					resposta = curopt.getString( curopt.getColumnIndex( "cor_resposta" ) );
					if( curopt.getInt( curopt.getColumnIndex( "cor_stresposta" ) ) == 1 )
						flres = true;
					else
						flres = false;
					if( curopt.getInt( curopt.getColumnIndex( "cor_stobrigatoria" ) ) == 1 )
						flobr = true;
					else
						flobr = false;
					corant = corid;
					corpo = new JSONObject();
					corpo.put( "id", corid );
					corpo.put( "tipo", ticor );
					corpo.put( "codigo", codigo );
					corpo.put( "texto", texto );
					corpo.put( "temresp", flres );
					corpo.put( "obrig", flobr );
					corpo.put( "resposta", resposta );
					if( ticor > 3 )
						{
						opcoes = new JSONArray();
						corpo.put( "opcoes", opcoes );
						}
					}       //  quebra de corpo
				if( ticor <= 3 )
					continue;
				int optid = curopt.getInt( curopt.getColumnIndex( "opt_id" ) );
				codigo = curopt.getString( curopt.getColumnIndex( "opt_codigo" ) );
				texto = curopt.getString( curopt.getColumnIndex( "opt_texto" ) );
				boolean flchk = false;
				if( curopt.getInt( curopt.getColumnIndex( "opt_flchecked" ) ) == 1 )
					flchk = true;
				boolean flatu = false;
				if( curopt.getInt( curopt.getColumnIndex( "opt_flatua" ) ) == 1 )
					flatu = true;
				opcao = new JSONObject();
				opcao.put( "id", optid );
				opcao.put( "codigo", codigo );
				opcao.put( "texto", texto );
				opcao.put( "check", flchk );
				opcao.put( "atua", flatu );
				opcoes.put( opcao );
				}         //  curopt.moveToNext
			if( corpo != null )
				corpos.put( corpo );
			Log.i( Globais.apptag, corpos.toString() );
			//  monta a tela
			boolean temresp = false;
			for( int ixcor=0; ixcor<corpos.length(); ixcor++ )
				{
				//  mostra o texto do corpo
				corpo = corpos.getJSONObject( ixcor );
				if( corpo.getBoolean( "temresp" ) )
					temresp = true;
				if( corpo.getBoolean( "obrig" ) )
					adcStatic( corpo.getString( "codigo" ), "* " + corpo.getString( "texto" ), true );
				else
					adcStatic( corpo.getString( "codigo" ), " " + corpo.getString( "texto" ), false );
				ticor = corpo.getInt( "tipo" );
				switch( ticor )
					{
					case 1:
						if( corpo.getBoolean( "temresp" ) )
							adcEdtTx( corpo );
						break;
					case 2:
					case 3:
						break;
					case 4:
						opcoes = corpo.getJSONArray( "opcoes" );
						adcRGroup( opcoes );
						break;
					case 5:
						opcoes = corpo.getJSONArray( "opcoes" );
						adcCheck( opcoes );
						break;
					case 6:
						adcCombo( corpo );
						break;
					}
				}
			if( dtresp != null )
				{
				//
				ll = new LinearLayout( this );
				ll.setOrientation( LinearLayout.HORIZONTAL );
				llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,
				                                     LinearLayout.LayoutParams.WRAP_CONTENT  );
				llp.gravity = Gravity.CENTER;
				ll.setGravity( Gravity.CENTER );
				idacts = View.generateViewId();
				ll.setId( idacts );
				//  botão de voltar
				btcanc = new Button( this );
				btcanc.setText( "voltar" );
				idcanc = View.generateViewId();
				btcanc.setId( idcanc );
				btcanc.setOnClickListener( this );
				ll.addView( btcanc );
				llmens.addView( ll );
				temresp = false;
				}
			if( temresp )
				{
				ll = new LinearLayout( this );
				ll.setOrientation( LinearLayout.HORIZONTAL );
				llp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,
				                                     LinearLayout.LayoutParams.WRAP_CONTENT  );
				llp.gravity = Gravity.CENTER;
				ll.setGravity( Gravity.CENTER );
				idacts = View.generateViewId();
				ll.setId( idacts );
				
				//  botão de enviar
				btmand = new Button( this );
				btmand.setText( "enviar" );
				idmand = View.generateViewId();
				btmand.setId( idmand );
				btmand.setOnClickListener( this );
				ll.addView( btmand );
				//  botão de salvar
				btsalv = new Button( this );
				btsalv.setText( "salvar" );
				idsalv = View.generateViewId();
				btsalv.setId( idsalv );
				btsalv.setOnClickListener( this );
				ll.addView( btsalv );
				//  botão de cancelar
				btcanc = new Button( this );
				btcanc.setText( "cancelar" );
				idcanc = View.generateViewId();
				btcanc.setId( idcanc );
				btcanc.setOnClickListener( this );
				ll.addView( btcanc );
				
				llmens.addView( ll );;
				}
			}
		catch( JSONException jexc )
			{
			Log.i( Globais.apptag, "JSON invalido: " + jexc.getMessage() );
			return;
			}
		catch( Exception exc )
			{
			Log.i( Globais.apptag, "Exceção: " + exc.getMessage() );
			return;
			}
		}
	}
