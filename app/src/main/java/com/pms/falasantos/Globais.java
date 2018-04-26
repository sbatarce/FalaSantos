package com.pms.falasantos;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;

import com.google.firebase.iid.FirebaseInstanceId;
import com.pms.falasantos.Atividades.AlvosActivity;
import com.pms.falasantos.Atividades.ConfidenActivity;
import com.pms.falasantos.Atividades.MainActivity;
import com.pms.falasantos.Atividades.SetupActivity;
import com.pms.falasantos.Comunicacoes.processFBMens;
import com.pms.falasantos.Outras.clAlvo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by w0513263 on 26/07/17.
 */

public class Globais
	{
	public static String apptag = "falaSantos";
	
	private static boolean netState  = false;
	private static boolean wifiState = false;
	private static boolean connState   = false;
	
	private static boolean httpConn = false;
	private static boolean httpWifi = false;
	
	public enum Atividade
		{
		nenhuma,
		AddAlvo,
		Alvos,
		Main,
		Mensagem,
		Mensagens,
		Setup
		}
	public static Atividade atividade = Atividade.nenhuma;
	
	public static String dominio = "https://egov.santos.sp.gov.br/simensweb";
	
	static private Boolean emuso = false;
	static private Boolean semaf = false;
	static public Boolean flfim = false;
	static public boolean fldebug = false;
	
	static public long delayRefresh = 1800000;
	static public boolean flatua = false;
	
	static public  Context   ctx       = null;
	static public  processFBMens pFBMens;
	static public  MainActivity pMain;
	static private Resources resources = null;
	static private String    nodb      = "falasantos.db";
	static private String    DBfile    = "";        //  nome do db com path
	static private String    pathSD    = "";
	static private String    publSD    = "";
	static private String    pathDB    = "/data/data/com.pms.falasantos/databases";
	static public SQLiteDatabase db = null;
	
	static public boolean dbOK = false;
	static public boolean dbnew;
	static public String lastMessage = "ok";
	
	static private int    versaoDB;
	static private int    versaoApp;
	
	static public Boolean alertResu = false;
	
	static private clAlvo clalvo = null;
	public static clAlvo getClalvo()
		{
		return clalvo;
		}
	public static void setClalvo( clAlvo clalvo )
		{
		Globais.clalvo = clalvo;
		}
	
	static public class mensPosic           //  posição da lista de mensagens
		{
		static public int grPos;
		static public int chPos;
		}
	
	static public class config
		{
		static public int     iddis = -1;                //  id deste dispositivo
		static public int     iddes = -1;                //  id do destinatário deste dispositivo
		static public String  nuserie = "";
		static public String  token = "";
		static public String  destin = "";
		static public String  sshd = "";
		static public String  nome = "";
		static public String  cpf = "";
		static public String  dtnas = "";
		static public String  hoini = "";
		static public String  hofim = "";
		static public String  vrsapp = "";
		static public int     vrsdb = -1;
		static public boolean flWIFI = false;
		static public String  senhaconf = "";
		static public String  questao = "";
		}
	
	static public AlertDialog dlgOKCancel( Context ctx, String titulo, String msg, String posit, String negat )
		{
		android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder( ctx );
		alertDialog.setTitle( titulo );
		alertDialog.setMessage( msg );
		alertDialog.setCancelable( false );
		alertDialog.setNegativeButton( negat, new DialogInterface.OnClickListener()
			{
			@Override
			public void onClick( DialogInterface dialogInterface, int i )
				{
				Globais.alertResu = false;
				}
			} );
		alertDialog.setPositiveButton( posit, new DialogInterface.OnClickListener()
			{
			@Override
			public void onClick( DialogInterface dialogInterface, int i )
				{
				Globais.alertResu = true;
				}
			} );
		return alertDialog.create();
		}
	
	//  mensagem com OK & Cancelar
	static public void AlertaOKCancel( Context ctx, String titulo, String msg, String posit, String negat )
		{
		android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder( ctx );
		alertDialog.setTitle( titulo );
		alertDialog.setMessage( msg );
		alertDialog.setCancelable( false );
		alertDialog.setNegativeButton( negat, new DialogInterface.OnClickListener()
			{
			@Override
			public void onClick( DialogInterface dialogInterface, int i )
				{
				Globais.alertResu = false;
				}
			} );
		alertDialog.setPositiveButton( posit, new DialogInterface.OnClickListener()
			{
			@Override
			public void onClick( DialogInterface dialogInterface, int i )
				{
				Globais.alertResu = true;
				}
			} );
		alertDialog.show();
		}
	
	//  mensagem só com OK
	static public void Alerta( Context ctx, String titulo, String msg )
		{
		android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder( ctx );
		alertDialog.setTitle( titulo );
		alertDialog.setMessage( msg );
		alertDialog.setCancelable( false );
		alertDialog.setPositiveButton( "OK", new DialogInterface.OnClickListener()
			{
			@Override
			public void onClick( DialogInterface dialogInterface, int i )
				{
				}
			} );
		alertDialog.show();
		}
	
	static public boolean temNovidades()
		{
		boolean flatua = false;
		String sql = "SELECT dis_flatua FROM dispositivo";
		Cursor c = Globais.db.rawQuery( sql, null );
		if( c.moveToFirst() )
			{
			if( c.getInt( c.getColumnIndex( "dis_flatua" ) ) == 1 )
				flatua = true;
			else
				flatua = false;
			if( c != null )
				c.close();
			}
		else
			{
			if( c != null )
				c.close();
			}
		return flatua;
		}
	
	static public void semNovidades()
		{
		Globais.db.execSQL( "UPDATE dispositivo SET dis_flatua=0" );
		flatua = false;
		}
	
	static public void comNovidades()
		{
		Globais.db.execSQL( "UPDATE dispositivo SET dis_flatua=1" );
		flatua = true;
		}
	
	static public String nuSerial()
		{
		int permite = ContextCompat.checkSelfPermission( ctx,
		                                                 android.Manifest.permission.READ_PHONE_STATE );
		if( permite == PackageManager.PERMISSION_GRANTED )
			{
			TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(
				Context.TELEPHONY_SERVICE );
			return telephonyManager.getDeviceId();
			}
		else
			return null;
		}
	
	//  salva o contexto da Activity que chamou
	static public void setContext( Context context )
		{
		ctx = context;
		resources = ctx.getResources();
		DBfile = context.getDatabasePath( nodb ).getAbsolutePath();
		pathSD = Environment.getExternalStorageDirectory().getAbsolutePath();
		publSD = Environment.getExternalStoragePublicDirectory(
			Environment.DIRECTORY_DCIM ).getAbsolutePath();
		versaoDB  = resources.getInteger( R.integer.dbvers );
		versaoApp = resources.getInteger( R.integer.appvers );
		pFBMens = new processFBMens( ctx );
		}
	
	static public void setEmUso()
		{
		synchronized( emuso )
			{
			emuso = true;
			}
		}
	
	static public void setSemUso()
		{
		synchronized( emuso )
			{
			emuso = false;
			}
		}
	
	static public boolean bloqueia()
		{
		Boolean wsemaf;
		//  verifica se já está bloqueado
		synchronized( semaf )
			{
			wsemaf = semaf;
			if( !semaf )
				semaf = true;
			}
		if( wsemaf )
			return false;       //  esta bloqueado
		else
			return true;        //  bloqueou
		}
	
	static public boolean libera()
		{
		Boolean wsemaf;
		//  verifica se já está bloqueado
		synchronized( semaf )
			{
			wsemaf = semaf;
			if( semaf )
				semaf = false;
			}
		if( !wsemaf )
			return false;     //  ja esta liberado
		else
			return true;      //  liberou
		}
	
	static public boolean espera()
		{
		Boolean wsemaf = false;
		//  verifica se já está bloqueado
		while( !wsemaf )
			{
			synchronized( semaf )
				{
				if( !semaf )
					wsemaf = true;
				}
			}
		return true;
		}
	
	static public boolean isConnected()
		{
		httpConn = false;
		httpWifi = false;
		ConnectivityManager cm = ( ConnectivityManager ) ctx.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		//  verifica conexão
		if( netInfo == null )
			return false;
		if( netInfo.isConnected( ) )
			httpConn = true;
		else
			return false;
		//  verifica wifi
		NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if( mWifi.isConnected() )
			httpWifi = true;
		else
			httpWifi = false;

		if( config.flWIFI && !httpWifi )
			return false;
		return true;
		}
	
	static private boolean verifyConn()
		{
		if( config.flWIFI )
			return wifiState;
		else
			return netState || wifiState;
		}
	
	static public void setConnected( boolean conn )
		{
		if( netState == conn )
			return;
		//  verifica se mudou o estado da conecção
		netState = conn;
		boolean stt = verifyConn();
		if( stt == connState )
			return;
		//  mudou
		connState = stt;
		if( stt )        //  está conectando ?
			{
			Log.i( apptag, "setConnected conectou Conn=" + netState + " WIFI=" + wifiState );
			pFBMens.mandaRespostas();
			}
		else
			Log.i( apptag, "setConnected Desonectou");
		}
	
	static public void setWifi( boolean conn )
		{
		if( wifiState == conn )
			return;
		//  verifica se mudou o estado da conecção
		wifiState = conn;
		boolean stt = verifyConn();
		if( stt == connState )
			return;
		//  mudou
		connState = stt;
		if( stt )            //  está conectando?
			{
			Log.i( apptag, "setWifi conectou Conn=" + netState + " WIFI=" + wifiState );
			pFBMens.mandaRespostas();
			}
		else
			Log.i( apptag, "setWifi desconectou ");
		return;
		}
	
	static public long ixArea( String area )
		{
		String sql = "SELECT are_id from areas where are_nome='" + area + "'";
		long res = -1;
		Cursor c = db.rawQuery( sql, null );
		if( c.moveToFirst() )
			{
			res = c.getLong( c.getColumnIndex( "are_id" ) );
			c.close();
			return res;
			}
		if( c != null )
			c.close();
		ContentValues cv = new ContentValues( 5 );
		cv.put( "are_nome", area );
		res = db.insert( "areas", null, cv );
		return res;
		}
	
	static public long ixRemet( String sshd, long alv, String nome )
		{
		String sql =  "SELECT rem_id FROM remetentes WHERE alv_id=" + alv +
									" AND rem_nopessoa='" + nome + "'";
		long res = -1;
		Cursor c = db.rawQuery( sql, null );
		if( c.moveToFirst() )
			{
			res = c.getLong( c.getColumnIndex( "rem_id" ) );
			c.close();
			return res;
			}
		if( c != null )
			c.close();
		ContentValues cv = new ContentValues( 5 );
		cv.put( "alv_id", alv );
		cv.put( "rem_usuario", sshd );
		cv.put( "rem_nopessoa", nome );
		cv.put( "rem_flsilen", 0 );
		res = db.insert( "remetentes", null, cv );
		return res;
		}
	
	//  verifica se já foi feito setup
	static public boolean fezSetup()
		{
		try
			{
			String sql = "SELECT des_id FROM destinatario";
			Cursor c = db.rawQuery( sql, null );
			if( c.moveToFirst() )
				{
				c.close();
				return true;
				}
			else
				{
				c.close();
				return false;
				}
			}
		catch( Exception exp )
			{
			Log.i( apptag, "fezSetup select: " + exp.getMessage() );
			return false;
			}
		}
	
	//  subrotinas dedicadas ao banco de dados
	public static void limpaDB()
		{
		try
			{
			db.execSQL( "delete from corpo" );
			db.execSQL( "delete from opcoes" );
			db.execSQL( "delete from mensagens" );
			db.execSQL( "delete from remetentes" );
			db.execSQL( "delete from alvos" );
			}
		catch( Exception exp )
			{
			Log.i( apptag, "Erro no delete: " + exp.getMessage() );
			}
		return;
		}
	public static void RemoveDB()
		{
		if( db != null )
			{
			if( db.isOpen() )
				db.close();
			}
		ctx.deleteDatabase( nodb );
		}
	//  cria ou abre o DB
	public static boolean AbreDB()
		{
		if( ctx == null )
			{
			lastMessage = "Nao foi setado o contexto.";
			return false;
			}
		try
			{
			db = ctx.openOrCreateDatabase( nodb, ctx.MODE_PRIVATE, null );
			db.execSQL( "PRAGMA foreign_keys=ON;" );
			}
		catch( Exception exc )
			{
			lastMessage = exc.getMessage();
			Log.i( apptag, "onCreate: " + exc.getMessage() );
			return false;
			}
		if( !criaTabelas() )
			return false;
		if( !obterConfig() )
			return false;
		if( !ajustaVersao() )
			return false;
		//  verifica se o telefone ja tem perfis associados
		if( config.vrsdb == 0 )
			dbnew = true;
		dbOK = true;
		return true;
		}
	
	private static boolean initDB()
		{
		//  cria o registro de configuração inicial
		ContentValues cv = new ContentValues( 15 );
		String serial = nuSerial();
		
		cv.put( "dis_id", 1 );
		cv.put( "dis_nuserie", serial );
		cv.put( "dis_vrsdb", versaoDB );
		cv.put( "dis_vrsapp", "1.0.0" );
		cv.put( "dis_hoinic", "08:00" );
		cv.put( "dis_hoterm", "21:00" );
		cv.put( "dis_flwifi", 0 );
		cv.put( "dis_flsilen", 0 );
		cv.put( "dis_frase", "" );
		cv.put( "dis_senha", "" );
		//
		long ret = -1;
		try
			{
			ret = db.insertOrThrow( "dispositivo", null, cv );
			if( ret < 1 )
				{
				Log.i( Globais.apptag, "Erro inserindo dispositivo" );
				}
			}
		catch( Exception e )
			{
			lastMessage = e.getMessage();
			Log.i( apptag, "Globais: criaTabelas dispositivo " + e.getMessage() );
			return false;
			}
		return true;
		}
	//  cria as tabelas usando o dbscripts
	public static boolean criaTabelas()
		{
		versaoDB = resources.getInteger( R.integer.dbvers );
		Cursor c = db.rawQuery(
			"select DISTINCT tbl_name from sqlite_master where tbl_name ='dispositivo'", null );
		if( c.getCount() > 0 )
			{
			c.close();
			dbnew = false;
			return true;
			}
		c.close();
		//  cria as tabelas do script
		dbnew = true;
		try
			{
			String[] tables = resources.getStringArray( R.array.tabelas );
			for( int ix = 0; ix < tables.length; ix++ )
				{
				String[] partes = tables[ix].split( "\\|" );
				String script = "CREATE TABLE IF NOT EXISTS " + partes[0] + "(" +
					partes[1] + ");";
				db.execSQL( script );
				}
			}
		catch( Exception exc )
			{
			lastMessage = exc.getMessage();
			Log.i( apptag, "Globais criaTabelas " + lastMessage );
			return false;
			}
		initDB();
		/////////////////////////// testes /////////////////////////////////////////////////////////////
		return true;
		}
	
	//  ajusta a versão do DB
	static boolean ajustaVersao()
		{
		boolean fltran = false;
		try
			{
			PackageInfo pkInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			String novrs = pkInfo.versionName;
//			int nuvrs = pkInfo.versionCode;
//			if( versaoDB <= config.vrsdb )
//				return true;
			if( versaoDB > 500 )
				versaoDB = 9;
			if( versaoDB >= 10 && config.vrsdb < 10 )
				{
				fltran = true;
				String alter = "ALTER TABLE mensagens ADD COLUMN " +
					"msg_dtnotif TEXT";
				db.execSQL( alter );
				//
				alter = "update mensagens set msg_dtnotif=msg_dtreceb";
				db.execSQL( alter );
				}
			if( versaoDB >= 8 && config.vrsdb < 8 )
				{
				fltran = true;
				db.beginTransaction();
				String alter = "ALTER TABLE dispositivo RENAME TO dispositivoold";
				db.execSQL( alter );
				String[] tables = resources.getStringArray( R.array.tabelas );
				for( int ix = 0; ix < tables.length; ix++ )
					{
					String[] partes = tables[ix].split( "\\|" );
					if( !partes[0].equals( "dispositivo" ))
						continue;
					String script = "CREATE TABLE IF NOT EXISTS " + partes[0] + "(" +
						partes[1] + ");";
					db.execSQL( script );
					break;
					}
				//  copia os dados iguais o antigo para o novo
				alter = "INSERT INTO dispositivo( dis_id, dis_nuserie, dis_fbtoken, " +
					"dis_vrsapp, dis_hoinic, dis_hoterm, dis_flwifi, dis_flsilen, dis_flatua, " +
					"dis_flpenden, dis_frase, dis_senha ) " +
					"SELECT dis_id, dis_nuserie, dis_fbtoken, " +
					"dis_vrsapp, dis_hoinic, dis_hoterm, dis_flwifi, dis_flsilen, dis_flatua, " +
					"dis_flpenden, dis_frase, dis_senha " +
					"FROM dispositivoold ";
				db.execSQL( alter );
				//  altera o dado novo
				alter = "UPDATE dispositivo SET dis_vrsapp='" + novrs + "', dis_vsrdb=" + versaoDB;
				db.execSQL( alter );
				//
				fltran = false;
				db.setTransactionSuccessful();
				}
			
			if( versaoDB >= 6 && config.vrsdb < 6 )
				{
				String alter =  "ALTER TABLE dispositivo ADD COLUMN " +
					"dis_frase TEXT";
				db.execSQL( alter );
				alter =  "ALTER TABLE dispositivo ADD COLUMN " +
					"dis_senha TEXT";
				db.execSQL( alter );
				String sql = "UPDATE DISPOSITIVO SET dis_frase='', dis_senha=''";
				db.execSQL( sql );
				}
			if( versaoDB >= 5 && config.vrsdb < 5 )
				{
				String alter =  "ALTER TABLE dispositivo ADD COLUMN " +
					"dis_flpenden INTEGER DEFAULT 0";
				db.execSQL( alter );
				String sql = "UPDATE DISPOSITIVO SET dis_vrsdb=1, dis_vrsapp='1.0.0'";
				db.execSQL( sql );
				}
			String sql = "UPDATE DISPOSITIVO SET " +
				"dis_vrsdb=" + versaoDB + ", " +
				"dis_vrsapp='" + novrs + "'";
			db.execSQL( sql );
			config.vrsapp = novrs;
			config.vrsdb = versaoDB;
			}
		catch( Exception exc )
			{
			if( fltran )
				db.endTransaction();
			Log.i( apptag, "ajustaVersao(1) " + exc.getMessage() );
			return false;
			}
		return true;
		}
	
	//  obter os dados de configuração
	static public boolean obterConfig()
		{
		try
			{
			String sql = "SELECT dis_id, dis_nuserie, dis_vrsdb, dis_hoinic, dis_hoterm, " +
				"dis_flwifi, dis_flsilen, dis_frase, dis_senha " +
				"FROM dispositivo";
			Cursor c = db.rawQuery( sql, null );
			if( c.getCount() < 1 )
				{
				config.iddis = -1;
				config.nuserie = "";
				config.vrsdb = 0;
				config.hoini = "";
				config.hofim = "";
				config.flWIFI = true;
				}
			else
				{
				if( c.moveToNext() )
					{
					config.iddis = c.getInt( c.getColumnIndex( "dis_id" ) );
					config.nuserie = c.getString( c.getColumnIndex( "dis_nuserie" ) );
					config.vrsdb = c.getInt( c.getColumnIndex( "dis_vrsdb" ) );
					config.hoini = c.getString( c.getColumnIndex( "dis_hoinic" ) );
					config.hofim = c.getString( c.getColumnIndex( "dis_hoterm" ) );
					if( c.getInt( c.getColumnIndex( "dis_flwifi" ) ) == 1 )
						config.flWIFI = true;
					else
						config.flWIFI = false;
					config.questao = c.getString( c.getColumnIndex( "dis_frase" ) );
					config.senhaconf = c.getString( c.getColumnIndex( "dis_senha" ) );
					}
				else
					return false;
				}
			c.close();
			//
			sql = "SELECT des_id, des_sshd, des_cpf, des_dtnas, des_nome " +
				"FROM destinatario";
			c = db.rawQuery( sql, null );
			if( c.getCount() < 1 )
				{
				config.iddes = -1;
				config.nome = "";
				config.sshd = "";
				config.cpf = "";
				config.dtnas = "";
				config.destin = "";
				}
			else
				{
				if( c.moveToNext() )
					{
					config.iddes = c.getInt( c.getColumnIndex( "des_id" ) );
					config.nome = c.getString( c.getColumnIndex( "des_nome" ) );
					config.sshd = c.getString( c.getColumnIndex( "des_sshd" ) );
					config.cpf = c.getString( c.getColumnIndex( "des_cpf" ) );
					config.dtnas = c.getString( c.getColumnIndex( "des_dtnas" ) );
					config.destin = c.getString( c.getColumnIndex( "des_nome" ) );
					if( config.sshd == null )
						config.sshd = "";
					}
				else
					return false;
				}
			c.close();
			}
		catch( Exception exc )
			{
			Log.i( apptag, "erro obtendo config: " + exc.getMessage() );
			}
		return true;
		}
	
	//  validação de nome
	public static boolean isAlfa( String txt )
		{
		String norm = Normalizer.normalize( txt, Normalizer.Form.NFD ).replaceAll( "[^\\p{ASCII}]", "" );
		if( norm.length() != txt.length() )
			return false;
		for( int ix=0; ix<norm.length(); ix++ )
			{
			if( norm.charAt( ix ) == ' ' )
				continue;
			if( Character.isLetter( norm.charAt( ix ) ) )
				continue;
			return false;
			}
		return true;
		}
	
	//  validação de CPF
	public static boolean isCPF( String CPF )
		{
		//  tem que ter 11 dígitos
		if( CPF.length() != 11 )
			return false;
		//  não pode ser uma sequencia de nros iguais
		if( CPF.equals( "00000000000" ) || CPF.equals( "11111111111" ) ||
			CPF.equals( "22222222222" ) || CPF.equals( "33333333333" ) ||
			CPF.equals( "44444444444" ) || CPF.equals( "55555555555" ) ||
			CPF.equals( "66666666666" ) || CPF.equals( "77777777777" ) ||
			CPF.equals( "88888888888" ) || CPF.equals( "99999999999" ) )
			return (false);
		
		char dig, dig10, dig11;
		int sm, i, r, num, peso;
		
		// Calculo do 1o. Digito Verificador
		sm = 0;
		peso = 10;
		for( i = 0; i < 9; i++ )
			{
			dig = CPF.charAt( i );                  //  dígito na posição i
			if( !Character.isDigit( dig ) )         //  verifica se é mesmo dígito
				return false;
			num = (int) (dig - '0');              //  converte para inteiro
			sm = sm + (num * peso);                 //  calcula o nro com peso
			peso = peso - 1;                        //  atualiza o peso
			}
		
		r = 11 - (sm % 11);
		if( (r == 10) || (r == 11) )
			dig10 = '0';
		else
			dig10 = (char) (r + '0');              // converte no respectivo caractere numerico
		if( dig10 != CPF.charAt( 9 ) )
			return false;
		
		// Calculo do 2o. Digito Verificador
		sm = 0;
		peso = 11;
		for( i = 0; i < 10; i++ )
			{
			dig = CPF.charAt( i );                  //  dígito na posição i
			num = (int) (dig - '0');              //  converte para inteiro
			sm = sm + (num * peso);
			peso = peso - 1;
			}
		
		r = 11 - (sm % 11);
		if( (r == 10) || (r == 11) )
			dig11 = '0';
		else dig11 = (char) (r + 48);
		if( dig11 != CPF.charAt( 10 ) )
			return false;
		
		return true;
		}
	//  retorna uma data no formato legivel
	//    'd'   DD/MM/YYYY
	//    'dt' DD/MM/YYY HH:MM
	//    't'  HH:MM
	public static String stData( Date date, String fmt )
		{
		SimpleDateFormat sdf = null;
		switch(fmt)
			{
			case "d":
				sdf = new SimpleDateFormat( "dd/MM/yyyy" );
				break;
			case "t":
				sdf = new SimpleDateFormat( "HH:mm" );
				break;
			case "dt":
				sdf = new SimpleDateFormat( "dd/MM/yyyy HH:mm" );
				break;
			}
		return sdf.format( date );
		}
	//  converte string dd/mm/aaaa ou ddmmaaaa para date
	public static Date toDate( String data )
		{
		if( data.length() != 8 && data.length() != 10 )
			return null;
		SimpleDateFormat sdf;
		Date date;
		if( data.length() == 8 )
			sdf = new SimpleDateFormat( "ddMMyyyy" );
		else
			sdf = new SimpleDateFormat( "dd/MM/yyyy" );
		sdf.setLenient( false );
		try
			{
			date = sdf.parse( data );
			}
		catch(ParseException pexc )
			{
			return null;
			}
		return date;
		}
	
	//  de AAAAMMDDHHMM => DD/MM/AAAA HH:MM
	//  ou AAAAMMDD => DD/MM/AAAA
	static public String toHumanDt( String dbdate )
		{
		String res = null;
		if( dbdate.length() == 12 )
			{
			res = dbdate.substring( 6, 8 );
			res += "/";
			res += dbdate.substring( 4, 6 );
			res += "/";
			res += dbdate.substring( 0, 4 );
			res += " ";
			res += dbdate.substring( 8, 10 );
			res += ":";
			res += dbdate.substring( 10, 12 );
			}
		if( dbdate.length() == 8 )
			{
			res = dbdate.substring( 6, 8 );
			res += "/";
			res += dbdate.substring( 4, 6 );
			res += "/";
			res += dbdate.substring( 0, 4 );
			}
		return res;
		}
	//  retorna a data corrente no formato AAAAMMDDHH24MI
	static public String agoraDB()
		{
		Date dt = new Date();
		String dtdb = "" + (dt.getYear() + 1900);
		if( dt.getMonth() + 1 < 10 )
			dtdb += "0" + (dt.getMonth() + 1);
		else
			dtdb += (dt.getMonth() + 1);
		if( dt.getDate() < 10 )
			dtdb += "0" + dt.getDate();
		else
			dtdb += dt.getDate();
		if( dt.getHours() < 10 )
			dtdb += "0" + (dt.getHours());
		else
			dtdb += (dt.getHours());
		if( dt.getMinutes() < 10 )
			dtdb += "0" + dt.getMinutes();
		else
			dtdb += dt.getMinutes();
		return dtdb;
		}
	
	static private void helpAddAlvo()
		{
		String msg =  "-";
		Globais.Alerta( ctx, "Ajuda", msg );
		}
	
	static private void helpAlvos()
		{
		String msg =  "-Tela de escolha de alvos a adicionar.\n" +
			"-Abaixo estão todos os \"alvos\", separados por área de interesse " +
			"disponíveis para você.\n" +
			"-Toque em uma das áreas e os alvos associados serão mostrados.\n" +
			"-Toque a seguir no alvo e eventualmente alguns dados serão solicitados " +
			"em uma nova tela, específica de cada alvo.\n" +
			"-Preencha os dados corretamente e estará apto a receber mensagens " +
			"específicas do alvo criado.";
		Globais.Alerta( ctx, "Ajuda", msg );
		}
	
	static private void helpMain()
		{
		String msg =  "-Tela inicial do FalaSantos.\n" +
			"-Abaixo estão todos os \"alvos\" nos quais você mostrou interesse.\n" +
			"-Toque em qualquer um deles e surgirão os remetentes associados ao alvo tocado.\n" +
			"-Toque em um dos remetentes para ler as mensagens do mesmo.";
		Globais.Alerta( ctx, "Ajuda", msg );
		}
	
	static private void helpMensagem()
		{
		String msg =  "-";
		Globais.Alerta( ctx, "Ajuda", msg );
		}
	
	static private void helpMensagens()
		{
		String msg =  "-";
		Globais.Alerta( ctx, "Ajuda", msg );
		}
	
	static private void helpSetup()
		{
		String msg = "-Se você for funcionário ou terceiro da Prefeitura e possuir " +
			"SSHD, marque \"Funcionário ou terceiro da Prefeitura\".\n" +
			"-Caso contrário marque \"Munícipe\".\n" +
			"-Os horários inicial e final definem o intervalo em que você aceita receber notificações.\n" +
			"-Se vocẽ não quiser usar seu plano de dados para receber as mensagens da Prefeitura, " +
			"marque \"Não usar a banda do celular(so WIFI)\". " +
			"Mesmo conectado pela banda larga, você continuará a receber notificações da Prefeitura " +
			"mas as mensagens só serão enviadas ao seu celular quando você estiver conectado via WIFI.\n" +
			"-Crie uma senha para que você possa ler mensagens confidenciais. Algumas mensagens " +
			"somente deverão ser vistas por você e você somente poderá acessá-las se tiver uma " +
			"Senha Confidencial cadastrada. Crie uma senha no campo \"Senha Confidencial\". " +
			"No campo \"Pergunta mágica\" escreva uma pergunta cuja resposta seja a " +
			"Senha Confidencial. Assim, Caso voce esqueça a senha mostraremos " +
			"a pergunta para te auxiliar. Por exemplo:\n" +
			"Pergunta mágica: Qual o nome do meu primeiro cãozinho\n" +
			"Senha Confidencial: floquinho\n" +
			"Evite usar letras maiúsculas e acentos na senha para evitar dúvidas.\n" +
			"Quando solicitado, mostraremos a Pergunta mágica de forma a te ajudar a " +
			"relembrar a Senha Confidencial criada.\n" +
			"-Se você for funcionário ou terceiro da Prefeitura preencha seu SSHD e a " +
			"senha correspondente.\n" +
			"-Se você não for funcionário da Prefeitura, preencha seu nome completo, " +
			"a data de seu nascimento e seu CPF.\n" +
			"-Estes dados são necessários para te identificar junto à Prefeitura\n" +
			"-Toque em OK no pé da tela e você estará apto a receber mensagens da Prefeitura.";
		Globais.Alerta( ctx, "Ajuda", msg );
		}
	
	static public void setMenuPadrao( Context contx, Menu menu )
		{
		int order = 0;
		menu.add( 0, R.integer.mnidAddAlvo, ++order, R.string.mntxAddAlvo );
		menu.add( 0, R.integer.mnidAjuda, ++order, R.string.mntxAjuda );
		menu.add( 0, R.integer.mnidSetup, ++order, R.string.mntxSetup );
		menu.add( 0, R.integer.mnidConfi, ++order, R.string.mntxConfi );
		menu.add( 0, R.integer.mnidWIFI, ++order, R.string.mntxWIFI );
		menu.add( 0, R.integer.mnidBanda, ++order, R.string.mntxBanda );
		
		String idtel = nuSerial();
		if( idtel != null )
			{
			if( idtel.equals( "355256063048937" ) ||
				idtel.equals( "354133073258312" ) ||
				idtel.equals( "000000000000000" ) )
				{
				menu.add( 0, R.integer.mnidToken, ++order, R.string.mntxToken );
				menu.add( 0, R.integer.mnidRemoveDB, ++order, R.string.mntxRemoveDB );
				menu.add( 0, R.integer.mnidCopyDB, ++order, R.string.mntxCopyDB );
				menu.add( 0, R.integer.mnidRecuDB, ++order, R.string.mntxRecuDB );
				menu.add( 0, R.integer.mnidInitDB, ++order, R.string.mntxInitDB );
				}
			}
		}
	
	static public void setMenuAjuda( Context contx, Menu menu )
		{
		int order = 0;
		menu.add( 0, R.integer.mnidAjuda, ++order, R.string.mntxAjuda );
		}
	
	static public void prcMenuItem( Context ctx, int idmenu )
		{
		Log.i( apptag, "prcMenu: " + ctx.getClass().getSimpleName() );
		if( idmenu == R.integer.mnidAddAlvo )
			{
			if( isConnected() )
				{
				Intent alvos = new Intent( ctx, AlvosActivity.class );
				ctx.startActivity( alvos );
				}
			else
				{
				String msg = "Para usar esta função, você deve estar conectado à internet\n";
				if( config.flWIFI )
					msg += "Você impediu o acesso via banda larga\n" +
						"Liberando talvez você consiga executar esta função\n" +
						"Utilize a função de configuração para liberar.";
				Alerta( ctx, "Sem conexão", msg );
				}
			return;
			}
		
		if( idmenu == R.integer.mnidAjuda )
			{
			switch( ctx.getClass().getSimpleName() )
				{
				case "MainActivity":
					helpMain();
					break;
				case "AlvosActivity":
					helpAlvos();
					break;
				case "SetupActivity":
					helpSetup();
					break;
				case "AddAlvoActivity":
					helpAddAlvo();
					break;
				case "MensagemActivity":
					helpMensagem();
					break;
				case "MensagensActivity":
					helpMensagens();
					break;
				}
			}
		
		if( idmenu == R.integer.mnidSetup )
			{
			if( isConnected() )
				{
				Intent setup = new Intent( ctx, SetupActivity.class );
				ctx.startActivity( setup );
				}
			else
				{
				String msg = "Para usar esta função, você deve estar conectado à internet\n";
				if( config.flWIFI )
					msg += "Você impediu o acesso via banda larga\n" +
						"Liberando (vá em configuração) talvez você consiga executar esta função" +
						"Utilize a função de configuração para liberar.";
				Alerta( ctx, "Sem conexão", msg );
				}
			return;
			}
		
		if( idmenu == R.integer.mnidConfi )
			{
			Intent setup = new Intent( ctx, ConfidenActivity.class );
			ctx.startActivity( setup );
			}
		
		if( idmenu == R.integer.mnidWIFI )
			{
			String sql = "update dispositivo set dis_flwifi=1";
			db.execSQL( sql );
			obterConfig();
			return;
			}
		
		if( idmenu == R.integer.mnidBanda )
			{
			String sql = "update dispositivo set dis_flwifi=0";
			db.execSQL( sql );
			obterConfig();
			return;
			}
		
		//  elementos do menu especial
		if( idmenu == R.integer.mnidRemoveDB )
			{
			Globais.RemoveDB();
			Globais.AbreDB();
			return;
			}
		
		if( idmenu == R.integer.mnidToken )
			{
			String token = FirebaseInstanceId.getInstance().getToken();
			Log.i( Globais.apptag, "<" + token + ">" );
			
			android.content.ClipboardManager clipboard =
				(android.content.ClipboardManager) ctx.getSystemService( Context.CLIPBOARD_SERVICE );
			android.content.ClipData clip =
				android.content.ClipData.newPlainText( "Copied Text", token );
			clipboard.setPrimaryClip( clip );
			}
		
		if( idmenu == R.integer.mnidCopyDB )
			{
			copyDB( pathSD + "/transfer" );
			return;
			}
		
		if( idmenu == R.integer.mnidRecuDB )
			{
			recuDB( pathSD + "/transfer/" + nodb );
			return;
			}
		
		}
	//  utilitarios
	/**
	 * copyFile           copia um arquivo para o diretório de destino
	 * O diretório de destino deve existir ou terminará com erro
	 * Criar o diretório no gerenciador de arquivos
	 * ou será impossível acessar o dito cujo depois.
	 *
	 * @param origem     arquivo de origem com Diretório
	 * @param destino    nome do arquivo de destino, sem diretório
	 * @param destinoDir diretório do arquivo de destino
	 * @return true se foi tudo certo
	 */
	static public boolean copyFile( String origem, String destino, String destinoDir )
		{
		InputStream in = null;
		OutputStream out = null;
		try
			{
			//  verifica a existencia do destino
			File dir = new File( destinoDir );
			if( !dir.exists() )
				{
				Log.i( apptag, "Diretório " + destinoDir + " não existe. Crie " );
				return false;
				}
			File dest = new File( destinoDir + "/" + destino );
			if( dest.exists() )
				dest.delete();
			in = new FileInputStream( origem );
			out = new FileOutputStream( destinoDir + "/" + destino );
			
			byte[] buffer = new byte[1024];
			int read;
			while( (read = in.read( buffer )) != -1 )
				{
				out.write( buffer, 0, read );
				}
			in.close();
			out.flush();
			out.close();
			MediaScannerConnection.scanFile( ctx, new String[]{ destinoDir }, null, null );
			}
		catch( FileNotFoundException fnfe1 )
			{
			Log.e( apptag, fnfe1.getMessage() );
			return false;
			}
		catch( Exception e )
			{
			Log.e( apptag, e.getMessage() );
			return false;
			}
		return true;
		}
	
	static public boolean copyDB( String path )
		{
		return copyFile( DBfile, nodb, path );
		}
	
	static public boolean recuDB( String path )
		{
		return copyFile( path, nodb, pathDB );
		}
	}
