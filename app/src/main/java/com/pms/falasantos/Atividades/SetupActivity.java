package com.pms.falasantos.Atividades;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.IdRes;
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
import com.pms.falasantos.R;
import com.pms.falasantos.Comunicacoes.RequestHttp;
import com.pms.falasantos.RespostaConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Date;

public class SetupActivity extends AppCompatActivity implements View.OnClickListener, RespostaConfig
	{
	LinearLayout llcomum, lloptfunc, lloptmuni;
	RadioGroup rbgr;
	
	String cpf, dtnas, nome,
		sshd, senha;
	int desid;
	
	private enum State
		{
		nenhum,
		cadmunicipe,
		cadfuncionario,
		dadosfuncionario,
		caddispositivo
		}
	State state = State.nenhum;
	
	boolean flokc = false;
	
	RequestHttp req = null;
	private ProgressDialog progress;
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_setup );
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
			((LinearLayout) findViewById( R.id.llComum )).setVisibility( View.VISIBLE );
			((EditText) findViewById( R.id.txHrInic )).setText( Globais.config.hoini );
			((EditText) findViewById( R.id.txHrFinal )).setText( Globais.config.hofim );
			((CheckBox) findViewById( R.id.ckWIFI )).setChecked( Globais.config.flWIFI );
			((CheckBox) findViewById( R.id.ckSilen )).setChecked( Globais.config.flSilen );
			
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
		}
	
	@Override
	public void onWindowFocusChanged( boolean hasFocus )
		{
		super.onWindowFocusChanged( hasFocus );
		if( hasFocus )
			{
			if( flokc )
				{
				flokc = false;
				if( !Globais.alertResu )
					finishAffinity();
				else
					Globais.alertResu = false;
				}
			Globais.setEmUso();
			}
		else
			Globais.setSemUso();
		}
	
	@Override
	public void onClick( View v )
		{
		if( v == findViewById( R.id.btSetupCancel ) )
			{
			if( !Globais.fezSetup() )
				{
				flokc = true;
				String msg = "Você ainda não configurou o aplicativo.\n" +
					"-Pressione Prosseguir para isso.\n" +
					"-Ou pressione Encerrar para sair e configurar mais tarde.";
				Globais.AlertaOKCancel( this, "Por favor preencha os dados", msg, "prosseguir",
				                        "Encerrar" );
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
					String url = Globais.dominio + "partes/funcoes.php?func=cadfuncionario";
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
				//  verifica o CPF
				cpf = ((EditText) findViewById( R.id.txCPF )).getText().toString();
				if( !Globais.isCPF( cpf ) )
					{
					String msg = "O CPF fornecido é inválido. Por favor introduza um CPF correto.";
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
					String url = Globais.dominio + "partes/funcoes.php?func=cadmunicipe";
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
			//  processamento de cada estado
			ContentValues cv;
			switch( state )
				{
				case cadmunicipe:
					if( !jobj.has( "id" ) )
						{
						Globais.Alerta( this, "Resposta do servidor com erro",
						                "Resposta sem indicativo do ID" );
						return;
						}
					desid = jobj.getInt( "id" );
					//  salva no sqlite
					configura();
					break;
				
				case cadfuncionario:
					if( !jobj.has( "id" ) )
						{
						Globais.Alerta( this, "Resposta do servidor com erro",
						                "Resposta sem indicativo do ID" );
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
					String url = Globais.dominio + "partes/procs.php?proc=dadosfunc";
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
					int qtd = jobj.getInt( "linhas" );
					if( !jobj.has( "dados" ) )
						{
						String msg = "Não contém dados. Por favor tente mais tarde";
						Globais.Alerta( this, "Resposta do servidor inválida!", msg );
						return;
						}
					JSONArray dados = jobj.getJSONArray( "dados" );
					JSONObject junid = dados.getJSONObject( 0 );
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
					int iddis = jobj.getInt( "id" );
					cv = new ContentValues( 5 );
					cv.put( "dis_id", iddis );
					cv.put( "dis_fbtoken", token );
					cv.put( "dis_nuserie", nuser );
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
					Toast.makeText( getApplicationContext(),
					                "Configuração efetuada. Adicione um alvo a seguir.",
					                Toast.LENGTH_LONG ).show();
					Globais.obterConfig();
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
		
	}
