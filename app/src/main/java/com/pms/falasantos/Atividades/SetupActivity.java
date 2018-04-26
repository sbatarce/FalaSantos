package com.pms.falasantos.Atividades;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.annotation.IdRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.pms.falasantos.Globais;
import com.pms.falasantos.Outras.clAlvs;
import com.pms.falasantos.R;
import com.pms.falasantos.Comunicacoes.RequestHttp;
import com.pms.falasantos.RespostaConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Date;

public class SetupActivity extends AppCompatActivity implements View.OnClickListener, RespostaConfig
	{
	LinearLayout llcomum, lloptfunc, lloptmuni;
	RadioGroup rbgr;
	
	String cpf, dtnas, nome,
		sshd, senha, senhaconf, questao;
	int desid, iddis;
	
	private enum State
		{
			nenhum,
			cadmunicipe,
			cadfuncionario,
			dadosfuncionario,
			caddispositivo,
			obteralvos
		}
	State state = State.nenhum;
	
	boolean flokc   = false,
					flwifi  = false;
	int qtalvos = 0;
	
	
	RequestHttp req = null;
	private ProgressDialog progress;
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_setup );
		Globais.atividade = Globais.Atividade.Setup;
		Globais.setContext( this );
		//
		getSupportActionBar().setDisplayOptions( ActionBar.DISPLAY_SHOW_CUSTOM );
		getSupportActionBar().setDisplayShowCustomEnabled( true );
		getSupportActionBar().setCustomView( R.layout.actbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );
		//  prepara a visualização da tela
		setRadioBT();
		if( !Globais.obterConfig() )
			{
			Toast.makeText( this,
			                "O banco de dados de seu aplicativo foi corrompido. Por favor reinstale.",
			                Toast.LENGTH_LONG ).show();
			this.finish();
			}
		if( !Globais.isConnected() )
			{
			String msg = "Use esta função somente quando conectado.";
			Globais.Alerta( this, "Sem conexão de internet!", msg );
			return;
			}
		if( Globais.fezSetup() )
			{
			//  ja fez o setup => deve ser alteração
			Globais.obterConfig();
			(findViewById( R.id.llComum )).setVisibility( View.VISIBLE );
			((CheckBox) findViewById( R.id.ckWIFI )).setChecked( Globais.config.flWIFI );
			
			((EditText) findViewById( R.id.txNome )).setText( Globais.config.nome );
			((EditText) findViewById( R.id.txDtNasc )).setText( Globais.config.dtnas );
			((EditText) findViewById( R.id.txCPF )).setText( Globais.config.cpf );
			
			((EditText) findViewById( R.id.txSSHD )).setText( Globais.config.sshd );
			((EditText) findViewById( R.id.txSenha )).setText( "" );
			
			if( Globais.config.sshd.equals( "" ) )
				{
				((RadioButton) findViewById( R.id.rbFunc )).setChecked( false );
				((RadioButton) findViewById( R.id.rbMunic )).setChecked( true );
				((LinearLayout) findViewById( R.id.llOptMuni )).setVisibility( View.VISIBLE );
				((LinearLayout) findViewById( R.id.llOptFunc )).setVisibility( View.INVISIBLE );
				}
			else
				{
				((RadioButton) findViewById( R.id.rbMunic )).setChecked( false );
				((RadioButton) findViewById( R.id.rbMunic )).setClickable( false );
				((EditText) findViewById( R.id.txSSHD )).setEnabled( false );
				((RadioButton) findViewById( R.id.rbFunc )).setChecked( true );
				((LinearLayout) findViewById( R.id.llOptMuni )).setVisibility( View.INVISIBLE );
				((LinearLayout) findViewById( R.id.llOptFunc )).setVisibility( View.VISIBLE );
				}
			}
		else
			{
			((RadioButton) findViewById( R.id.rbFunc )).setChecked( false );
			((RadioButton) findViewById( R.id.rbMunic )).setChecked( true );
			
			((EditText) findViewById( R.id.txNome )).setText( "" );
			((EditText) findViewById( R.id.txDtNasc )).setText( "" );
			((EditText) findViewById( R.id.txCPF )).setText( "" );
			
			((EditText) findViewById( R.id.txSSHD )).setText( "" );
			((EditText) findViewById( R.id.txSenha )).setText( "" );
			
			((LinearLayout) findViewById( R.id.llOptMuni )).setVisibility( View.VISIBLE );
			((LinearLayout) findViewById( R.id.llOptFunc )).setVisibility( View.INVISIBLE );
			}
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
		Globais.atividade = Globais.Atividade.Setup;
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
	public void onWindowFocusChanged( boolean hasFocus )
		{
		super.onWindowFocusChanged( hasFocus );
		if( hasFocus )
			Globais.setEmUso();
		else
			Globais.setSemUso();
		}
	
	private void confirma()
		{
		String msg = "Você ainda não configurou o aplicativo.\n" +
			"-Pressione Prosseguir para isso.\n" +
			"-Ou pressione Encerrar para sair e configurar mais tarde.";
		final android.app.AlertDialog.Builder prompt =
			new android.app.AlertDialog.Builder( SetupActivity.this );
		prompt.setTitle( "Por favor, preencha os dados" );
		prompt.setMessage( msg );
		prompt.setCancelable( false );
		prompt.setNegativeButton( "Encerrar", new DialogInterface.OnClickListener()
			{
			@Override
			public void onClick( DialogInterface dialogInterface, int i )
				{
				Globais.flfim = true;
				SetupActivity.this.finish();
				}
			} );
		prompt.setPositiveButton( "Prosseguir", new DialogInterface.OnClickListener()
			{
			@Override
			public void onClick( DialogInterface dialogInterface, int i )
				{
				}
			} );
		prompt.show();
		}
	
	@Override
	public void onClick( View v )
		{
		if( v == findViewById( R.id.btSetupCancel ) )
			{
			if( !Globais.fezSetup() )
				{
				flokc = true;
				confirma();
				return;
				}
			else
				finish();
			}
		if( v == findViewById( R.id.btSetupOK ) )
			{
			//  criação/atualização dos dados de setup
			if( !((RadioButton) findViewById( R.id.rbFunc )).isChecked() && !((RadioButton) findViewById(
				R.id.rbMunic )).isChecked() )
				{
				flokc = true;
				String msg = "É necessário que você preencha todos os dados para prosseguir.\n" +
					"-Pressione Prosseguir para isso.\n" +
					"-Ou pressione Encerrar para sair do programa e configurar mais tarde.";
				Globais.AlertaOKCancel( this, "Por favor preencha os dados", msg, "prosseguir",
				                        "Encerrar" );
				return;
				}
			if( ((CheckBox) findViewById( R.id.ckWIFI )).isChecked() )
				flwifi = true;
			else
				flwifi = false;
			
			//  funcionários ou munícipes
			if( ((RadioButton) findViewById( R.id.rbFunc )).isChecked() )
				{
				//  de funcionario
				sshd = ((EditText) findViewById( R.id.txSSHD )).getText().toString().toUpperCase();
				if( sshd == null )
					{
					String msg = "O SSHD fornecido é inválido. Por favor introduza um SSHD correto.";
					Globais.Alerta( this, "Por favor, corrija.", msg );
					return;
					}
				if( sshd.length() < 8 )
					{
					String msg = "O SSHD fornecido é inválido. Por favor introduza um SSHD correto.";
					Globais.Alerta( this, "Por favor, corrija.", msg );
					return;
					}
				senha = ((EditText) findViewById( R.id.txSenha )).getText().toString();
				if( senha == null )
					{
					String msg = "SENHA fornecida inválida. Por favor introduza uma SENHA correta.";
					Globais.Alerta( this, "Por favor, corrija.", msg );
					return;
					}
				if( senha.length() < 1 )
					{
					String msg = "SENHA fornecida inválida. Por favor introduza uma SENHA correta.";
					Globais.Alerta( this, "Por favor, corrija.", msg );
					return;
					}
				//  faz o setup
				try
					{
					//  verifica conexão
					if( !Globais.isConnected() )
						{
						String msg = "Tente conectar na internet e então volte a pressionar OK.";
						Globais.Alerta( this, "Conexão de internet perdida!", msg );
						return;
						}
					//  executa
					progress = ProgressDialog.show( this, "Por favor, espere...", "Verificando SSHD...",
					                                true );
					state = State.cadfuncionario;
					req = new RequestHttp( this );
					req.setAuth( sshd, senha );
					String url = Globais.dominio + "/partes/funcoes.php?func=cadfuncionario";
					url += "&sshd=" + URLEncoder.encode( sshd );
					req.delegate = this;
					req.execute( url );
					}
				catch( Exception exc )
					{
					Log.i( Globais.apptag, exc.getMessage() );
					progress.dismiss();
					}
				}
			else
				{
				//  de munícipe
				//  verifica o nome
				nome = ((EditText) findViewById( R.id.txNome )).getText().toString();
				if( nome == null )
					{
					String msg = "Por favor forneça seu nome, de preferencia completo.";
					Globais.Alerta( this, "Por favor, corrija.", msg );
					return;
					}
				if( nome.length() < 2 )
					{
					String msg = "Por favor forneça seu nome, de preferencia completo.";
					Globais.Alerta( this, "Por favor, corrija.", msg );
					return;
					}
				if( !Globais.isAlfa( nome ) )
					{
					String msg = "O nome contém caracteres não aceitáveis.";
					Globais.Alerta( this, "Por favor, corrija.", msg );
					return;
					}
				//  verifica data de nascimento
				dtnas = ((EditText) findViewById( R.id.txDtNasc )).getText().toString();
				Date dt = Globais.toDate( dtnas );
				if( dt == null )
					{
					String msg = "Data de nascimento inválida. Por favor introduza uma data correta.";
					Globais.Alerta( this, "Por favor, corrija.", msg );
					return;
					}
				Date dtaux = new Date();
				if( dt.after( dtaux ) )
					{
					String msg = "Data de nascimento inválida. Deve ser uma data passada.";
					Globais.Alerta( this, "Por favor, corrija.", msg );
					return;
					}
				dtaux = new Date( 0, 01, 01 );
				if( dt.before( dtaux ) )
					{
					String msg = "Data de nascimento inválida.";
					Globais.Alerta( this, "Por favor, corrija.", msg );
					return;
					}
				dtnas = Globais.stData( dt, "d" );
				((EditText) findViewById( R.id.txDtNasc )).setText( dtnas );
				//  verifica o CPF
				cpf = ((EditText) findViewById( R.id.txCPF )).getText().toString();
				if( !Globais.isCPF( cpf ) )
					{
					String msg = "O CPF fornecido é inválido. Por favor introduza um CPF correto.";
					Globais.Alerta( this, "Por favor, corrija.", msg );
					return;
					}
				//  faz o setup
				try
					{
					//  verifica conexão
					if( !Globais.isConnected() )
						{
						String msg = "Tente conectar na internet e então volte a pressionar OK.";
						Globais.Alerta( this, "Conexão de internet perdida!", msg );
						return;
						}
					//  executa
					progress = ProgressDialog.show( this, "Por favor, espere...", "Atualizando seus dados...",
					                                true );
					state = State.cadmunicipe;
					req = new RequestHttp( this );
					String url = Globais.dominio + "/partes/funcoes.php?func=cadmunicipe";
					url += "&cpf=" + URLEncoder.encode( cpf );
					url += "&dtnas=" + URLEncoder.encode( dtnas );
					url += "&nome=" + URLEncoder.encode( nome );
					req.delegate = this;
					req.execute( url );
					}
				catch( Exception exc )
					{
					Log.i( Globais.apptag, exc.getMessage() );
					progress.dismiss();
					}
				}
			}
		}
	
	@Override
	public void Resposta( String resposta )
		{
		int qtd;
		JSONArray dados;
		JSONObject junid;
		ContentValues cv;
		progress.dismiss();
		//  verifica a parte comum a todas as respostas
		try
			{
			JSONObject jobj = new JSONObject( resposta );
			if( jobj.has( "erro" ) )
				{
				String erro = jobj.getString( "erro" );
				if( erro.contains( "01017" ) )
					Globais.Alerta( this, "Acesso negado", "SSHD e/ou senha não corretos" );
				else
					Globais.Alerta( this, "Por favor, tente mais tarde! (1)",
				                "O acesso aos dados apresentou um problema.\n" +
					                "Pode estar passando por dificuldades no momento.\n" );
				return;
				}
			if( !jobj.has( "status" ) )
				{
				Globais.Alerta( this, "Por favor, tente mais tarde! (2)",
				                "O acesso aos dados apresentou um problema.\n" +
					                "Pode estar passando por dificuldades no momento.\n" );
				return;
				}
			if( !jobj.getString( "status" ).equals( "OK" ) && !jobj.getString( "status" ).equals( "ok" ) )
				{
				String status = jobj.getString( "status" );
				Globais.Alerta( this, "Por favor, tente mais tarde! (3)",
				                "O acesso aos dados apresentou um problema.\n" +
					                "Pode estar passando por dificuldades no momento.\n" );
				return;
				}
			//  processamento de cada estado
			switch( state )
				{
				case cadmunicipe:
					if( !jobj.has( "id" ) )
						{
						Globais.Alerta( this, "Por favor, tente mais tarde! (4)",
						                "O acesso aos dados apresentou um problema.\n" +
							                "Pode estar passando por dificuldades no momento.\n" );
						return;
						}
					desid = jobj.getInt( "id" );
					//  salva no sqlite
					configura();
					break;
				
				case cadfuncionario:
					if( !jobj.has( "id" ) )
						{
						Globais.Alerta( this, "Por favor, tente mais tarde! (5)",
						                "O acesso aos dados apresentou um problema.\n" +
							                "Pode estar passando por dificuldades no momento." );
						return;
						}
					desid = jobj.getInt( "id" );
					
					//  verifica conexão
					if( !Globais.isConnected() )
						{
						String msg = "Tente conectar na internet e então volte a pressionar OK.";
						Globais.Alerta( this, "Conexão de internet perdida!", msg );
						return;
						}
					//  executa
					progress = ProgressDialog.show( this, "Por favor, espere...",
					                                "Obtendo dados do funcionário...",
					                                true );
					state = State.dadosfuncionario;
					req = new RequestHttp( this );
					req.setAuth( sshd, senha );
					String url = Globais.dominio + "/partes/procs.php?proc=dadosfunc";
					url += "&sshd=" + URLEncoder.encode( sshd );
					req.delegate = this;
					req.execute( url );
					break;
				
				case dadosfuncionario:
					if( !jobj.has( "linhas" ) )
						{
						String msg = "Não contém o contador de linhas. Por favor tente mais tarde";
						Globais.Alerta( this, "Resposta do servidor inválida!", msg );
						return;
						}
					if( jobj.getInt( "linhas" ) < 1 )
						{
						String msg = "Funcionário não localizado";
						Globais.Alerta( this, "Atenção!", msg );
						return;
						}
					qtd = jobj.getInt( "linhas" );
					if( !jobj.has( "dados" ) )
						{
						String msg = "Não contém dados. Por favor tente mais tarde";
						Globais.Alerta( this, "Resposta do servidor inválida!", msg );
						return;
						}
					dados = jobj.getJSONArray( "dados" );
					junid = dados.getJSONObject( 0 );
					nome = junid.getString( "NOME" );
					cpf = junid.getString( "CPF" );
					dtnas = junid.getString( "DTNASCIMENTO" );
					//
					configura();
					break;
				
				case caddispositivo:
					Log.i( Globais.apptag, "caddispositivo" );
					String token = FirebaseInstanceId.getInstance().getToken();
					String nuser = Globais.nuSerial();
					iddis = jobj.getInt( "id" );
					cv = new ContentValues( 5 );
					cv.put( "dis_id", iddis );
					cv.put( "dis_fbtoken", token );
					cv.put( "dis_nuserie", nuser );
					if( flwifi )
						cv.put( "dis_flwifi", 1 );
					else
						cv.put( "dis_flwifi", 0 );
					cv.put( "dis_flsilen", 0 );
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
					//  obter eventuais alvos
					obterAlvos();
					break;
					
				case obteralvos:
					if( !jobj.has( "linhas" ) )
						{
						String msg = "Não contém o contador de linhas. Por favor tente mais tarde";
						Globais.Alerta( this, "Resposta do servidor inválida!", msg );
						return;
						}
					if( jobj.getInt( "linhas" ) > 0 )
						{
						qtd = jobj.getInt( "linhas" );
						if( !jobj.has( "dados" ) )
							{
							String msg = "Não contém dados. Por favor tente mais tarde";
							Globais.Alerta( this, "Resposta do servidor inválida!", msg );
							return;
							}
						dados = jobj.getJSONArray( "dados" );
						Globais.db.beginTransaction();
						for( int ix=0; ix<qtd; ix++ )
							{
							cv = new ContentValues( 5 );
							junid = dados.getJSONObject( ix );
							int idalv = junid.getInt( "IDALV" );
							String nome = junid.getString( "ALV_DLNOME" );
							String area = junid.getString( "AREA" );
							long idare = Globais.ixArea( area );
							cv.put( "alv_id", idalv );
							cv.put( "are_id", idare );
							cv.put( "alv_nome", nome );
							long id = Globais.db.insert( "alvos", null, cv );
							}
						Globais.db.setTransactionSuccessful();
						Globais.db.endTransaction();
						}
					
					//  acabou a configuração
					if( qtalvos == 0 )
						Toast.makeText( getApplicationContext(),
						                "Configuração efetuada. Adicione um alvo a seguir.",
						                Toast.LENGTH_LONG ).show();
					else
						Toast.makeText( getApplicationContext(),
						                "Configuração efetuada.",
						                Toast.LENGTH_LONG ).show();
					Globais.obterConfig();
					Thread.sleep( 3000 );
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
	
	//  tratamento dos radiobuttons
	private void setRadioBT()
		{
		//  controles
		rbgr = (RadioGroup) findViewById( R.id.rgEscUser );
		llcomum = (LinearLayout) findViewById( R.id.llComum );
		lloptfunc = (LinearLayout) findViewById( R.id.llOptFunc );
		lloptmuni = (LinearLayout) findViewById( R.id.llOptMuni );
		llcomum.setVisibility( View.INVISIBLE );
		lloptfunc.setVisibility( View.INVISIBLE );
		lloptmuni.setVisibility( View.INVISIBLE );
		//  eventos
		rbgr.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener()
			{
			@Override
			public void onCheckedChanged( RadioGroup radioGroup, @IdRes int id )
				{
				if( id == R.id.rbFunc )
					{
					llcomum.setVisibility( View.VISIBLE );
					lloptfunc.setVisibility( View.VISIBLE );
					lloptmuni.setVisibility( View.INVISIBLE );
					}
				else
					{
					llcomum.setVisibility( View.VISIBLE );
					lloptfunc.setVisibility( View.INVISIBLE );
					lloptmuni.setVisibility( View.VISIBLE );
					}
				}
			} );
		}
	//  faz a configuração completa
	private void configura()
		{
		ContentValues cv = new ContentValues( 10 );
		
		while( cpf.length() < 11 )
			{
			String aux = cpf;
			cpf = "0"+aux;
			}
		
		cv.put( "des_id", desid );
		cv.put( "des_sshd", sshd );
		cv.put( "des_cpf", cpf );
		cv.put( "des_dtnas", dtnas );
		cv.put( "des_nome", nome );
		
		try
			{
			//  insere o destinatário
			Cursor c = Globais.db.rawQuery( "SELECT des_id FROM destinatario", null );
			if( c.moveToFirst() )
				{
				c.close();
				Globais.db.update( "destinatario", cv, null, null );
				}
			else
				{
				c.close();
				Globais.db.insertOrThrow( "destinatario", null, cv );
				}
			//  cria o dispositivo no servidor
			String token = FirebaseInstanceId.getInstance().getToken();
			String nuser = Globais.nuSerial();
			//  cadastra o dispositivo
			progress = ProgressDialog.show( this, "Por favor, espere...", "Cadastrando seu aparelho...",
			                                true );
			state = State.caddispositivo;
			req = new RequestHttp( this );
			if( sshd != null && senha != null )
				req.setAuth( sshd, senha );
			String url = Globais.dominio + "/partes/funcoes.php?func=caddispositivo";
			url += "&iddes=" + URLEncoder.encode( "" + desid );
			url += "&serie=" + URLEncoder.encode( nuser );
			url += "&token=" + URLEncoder.encode( token );
			req.delegate = this;
			req.execute( url );
			}
		catch( Exception e )
			{
			Globais.Alerta( this, "Banco de dados corrompido", e.getMessage() );
			Log.i( Globais.apptag, "Globais: criaTabelas dispositivo " + e.getMessage() );
			return;
			}
		}
	
	private void obterAlvos()
		{
		Cursor cur = Globais.db.rawQuery( "select count(1) as qtd from alvos", null );
		cur.moveToFirst();
		qtalvos = cur.getInt( cur.getColumnIndex( "qtd" ) );
		cur.close();
		if( qtalvos > 0 )
			{
			finish();
			return;
			}
		//  pede todos os eventuais alvos
		progress = ProgressDialog.show( this, "Por favor, espere...", "Obtendo eventuais alvos antigos...",
		                                true );
		state = State.obteralvos;
		req = new RequestHttp( this );
		if( sshd != null && senha != null )
			req.setAuth( sshd, senha );
		String url = Globais.dominio + "/partes/procs.php?proc=obteralvos&iddis";
		url += "&iddis=" + URLEncoder.encode( "" + iddis  );
		req.delegate = this;
		req.execute( url );
		}
		
	}
