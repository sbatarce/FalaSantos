package com.pms.falasantos;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.pms.falasantos.Atividades.SetupActivity;
import com.pms.falasantos.Comunicacoes.processFBMens;
import com.pms.falasantos.Outras.clAlvo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * Created by w0513263 on 26/07/17.
 */

public class Globais
	{
	public static String apptag = "falaSantos";
	
	public static String dominio = "https://egov.santos.sp.gov.br/simensweb";
	
	static private Boolean emuso = false;
	static private Boolean semaf = false;
	
	static public  Context   ctx       = null;
	static public  processFBMens pFBMens;
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
		static public int     vrsdb = -1;
		static public boolean flWIFI = false;
		static public boolean flSilen = false;
		}
	
	//  mensagem com OK & Cancel
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
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(
			Context.CONNECTIVITY_SERVICE );
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		
		if( netInfo != null )
			{
			if( netInfo.isConnectedOrConnecting() || netInfo.isConnected() )
				{
				if( config.flWIFI )
					{
					NetworkInfo mWifi = cm.getNetworkInfo( ConnectivityManager.TYPE_WIFI );
					if( mWifi.isConnected() )
						return true;
					else
						return false;
					}
				else
					return true;
				}
			}
		return false;
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
									" AND rem_sshd='" + sshd + "'";
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
		cv.put( "rem_sshd", sshd );
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
	
	private static boolean dadosTeste()
		{
		ContentValues cv;
		String sql, local;
		long ret;
		try
			{
			//  cria areas
			cv = new ContentValues( 5 );
			cv.put( "are_id", 100 );
			cv.put( "are_nome", "Ponto" );
			ret = db.insertOrThrow( "areas", null, cv );
			cv = new ContentValues( 5 );
			cv.put( "are_id", 102 );
			cv.put( "are_nome", "Educação" );
			ret = db.insertOrThrow( "areas", null, cv );
			cv = new ContentValues( 5 );
			cv.put( "are_id", 103 );
			cv.put( "are_nome", "Saúde" );
			ret = db.insertOrThrow( "areas", null, cv );
			cv = new ContentValues( 5 );
			cv.put( "are_id", 199 );
			cv.put( "are_nome", "Comunicação" );
			ret = db.insertOrThrow( "areas", null, cv );
			//  alvos
			sql = "INSERT INTO  VALUES " +
				"(  )";
			cv = new ContentValues( 10 );
			cv.put( "alv_id", 1 );
			cv.put( "are_id", 100 );
			cv.put( "alv_nome", "alvo 1 de 1" );
			ret = db.insertOrThrow( "alvos", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "alv_id", 2 );
			cv.put( "are_id", 100 );
			cv.put( "alv_nome", "alvo 2 de 1" );
			ret = db.insertOrThrow( "alvos", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "alv_id", 3 );
			cv.put( "are_id", 102 );
			cv.put( "alv_nome", "alvo 1 de 2" );
			ret = db.insertOrThrow( "alvos", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "alv_id", 4 );
			cv.put( "are_id", 102 );
			cv.put( "alv_nome", "alvo 2 de 2" );
			ret = db.insertOrThrow( "alvos", null, cv );
			//  remetentes
			sql = "INSERT INTO  VALUES " +
				"(  )";
			cv = new ContentValues( 10 );
			cv.put( "rem_id", 1 );
			cv.put( "alv_id", 1 );
			cv.put( "rem_sshd", "Z0000000" );
			cv.put( "rem_nopessoa", "remetente 1 de 1 de 1" );
			cv.put( "rem_flsilen", 0 );
			ret = db.insertOrThrow( "remetentes", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "rem_id", 2 );
			cv.put( "alv_id", 1 );
			cv.put( "rem_sshd", "Z0000000" );
			cv.put( "rem_nopessoa", "remetente 2 de 1 de 1" );
			cv.put( "rem_flsilen", 0 );
			ret = db.insertOrThrow( "remetentes", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "rem_id", 3 );
			cv.put( "alv_id", 2 );
			cv.put( "rem_sshd", "Z0000000" );
			cv.put( "rem_nopessoa", "remetente 1 de 2 de 1" );
			cv.put( "rem_flsilen", 0 );
			ret = db.insertOrThrow( "remetentes", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "rem_id", 4 );
			cv.put( "alv_id", 2 );
			cv.put( "rem_sshd", "Z0000000" );
			cv.put( "rem_nopessoa", "remetente 2 de 2 de 1" );
			cv.put( "rem_flsilen", 0 );
			ret = db.insertOrThrow( "remetentes", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "rem_id", 5 );
			cv.put( "alv_id", 3 );
			cv.put( "rem_sshd", "Z0000000" );
			cv.put( "rem_nopessoa", "remetente 1 de 1 de 2" );
			cv.put( "rem_flsilen", 0 );
			ret = db.insertOrThrow( "remetentes", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "rem_id", 6 );
			cv.put( "alv_id", 3 );
			cv.put( "rem_sshd", "Z0000000" );
			cv.put( "rem_nopessoa", "remetente 2 de 1 de 2" );
			cv.put( "rem_flsilen", 0 );
			ret = db.insertOrThrow( "remetentes", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "rem_id", 7 );
			cv.put( "alv_id", 4 );
			cv.put( "rem_sshd", "Z0000000" );
			cv.put( "rem_nopessoa", "remetente 1 de 2 de 2" );
			cv.put( "rem_flsilen", 0 );
			ret = db.insertOrThrow( "remetentes", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "rem_id", 8 );
			cv.put( "alv_id", 4 );
			cv.put( "rem_sshd", "Z0000000" );
			cv.put( "rem_nopessoa", "remetente 2 de 2 de 2" );
			cv.put( "rem_flsilen", 0 );
			ret = db.insertOrThrow( "remetentes", null, cv );
			//  mensagens do remetente 1
			sql = "INSERT INTO  VALUES " +
				"(  )";
			cv = new ContentValues( 15 );
			cv.put( "msg_id", 1 );
			cv.put( "rem_id", 1 );
			cv.put( "msg_msaid", 123 );
			cv.put( "msg_titulo", "titulo 1" );
			cv.put( "msg_timsg", 1 );
			cv.put( "msg_dtreceb", "201709091000" );
			cv.put( "msg_flatua", 0 );
			ret = db.insertOrThrow( "mensagens", null, cv );
			cv = new ContentValues( 15 );
			cv.put( "msg_id", 2 );
			cv.put( "rem_id", 1 );
			cv.put( "msg_msaid", 123 );
			cv.put( "msg_titulo", "titulo 2" );
			cv.put( "msg_timsg", 1 );
			cv.put( "msg_dtreceb", "201709091000" );
			cv.put( "msg_flatua", 0 );
			ret = db.insertOrThrow( "mensagens", null, cv );
			cv = new ContentValues( 15 );
			cv.put( "msg_id", 3 );
			cv.put( "rem_id", 1 );
			cv.put( "msg_msaid", 123 );
			cv.put( "msg_titulo", "titulo 3" );
			cv.put( "msg_timsg", 1 );
			cv.put( "msg_dtreceb", "201709091000" );
			cv.put( "msg_flatua", 0 );
			ret = db.insertOrThrow( "mensagens", null, cv );
			cv = new ContentValues( 15 );
			cv.put( "msg_id", 4 );
			cv.put( "rem_id", 1 );
			cv.put( "msg_msaid", 123 );
			cv.put( "msg_titulo", "titulo 4" );
			cv.put( "msg_timsg", 1 );
			cv.put( "msg_dtreceb", "201709091000" );
			cv.put( "msg_flatua", 0 );
			ret = db.insertOrThrow( "mensagens", null, cv );
			//  mensagens do remetente 8
			sql = "INSERT INTO  VALUES " +
				"(  )";
			cv = new ContentValues( 15 );
			cv.put( "msg_id", 5 );
			cv.put( "rem_id", 8 );
			cv.put( "msg_msaid", 123 );
			cv.put( "msg_titulo", "titulo 5" );
			cv.put( "msg_timsg", 1 );
			cv.put( "msg_dtreceb", "201709091000" );
			cv.put( "msg_flatua", 0 );
			ret = db.insertOrThrow( "mensagens", null, cv );
			cv = new ContentValues( 15 );
			cv.put( "msg_id", 6 );
			cv.put( "rem_id", 8 );
			cv.put( "msg_msaid", 123 );
			cv.put( "msg_titulo", "titulo 6" );
			cv.put( "msg_timsg", 1 );
			cv.put( "msg_dtreceb", "201709091000" );
			cv.put( "msg_flatua", 0 );
			ret = db.insertOrThrow( "mensagens", null, cv );
			cv = new ContentValues( 15 );
			cv.put( "msg_id", 7 );
			cv.put( "rem_id", 8 );
			cv.put( "msg_msaid", 123 );
			cv.put( "msg_titulo", "titulo 7" );
			cv.put( "msg_timsg", 1 );
			cv.put( "msg_dtreceb", "201709091000" );
			cv.put( "msg_flatua", 0 );
			ret = db.insertOrThrow( "mensagens", null, cv );
			cv = new ContentValues( 15 );
			cv.put( "msg_id", 8 );
			cv.put( "rem_id", 8 );
			cv.put( "msg_msaid", 123 );
			cv.put( "msg_timsg", 1 );
			cv.put( "msg_titulo", "titulo 8" );
			cv.put( "msg_dtreceb", "201709091000" );
			cv.put( "msg_flatua", 0 );
			ret = db.insertOrThrow( "mensagens", null, cv );
			cv = new ContentValues( 15 );
			cv.put( "msg_id", 9 );
			cv.put( "rem_id", 2 );
			cv.put( "msg_msaid", 123 );
			cv.put( "msg_titulo", "titulo 9" );
			cv.put( "msg_timsg", 1 );
			cv.put( "msg_dtreceb", "201709091000" );
			cv.put( "msg_flatua", 0 );
			ret = db.insertOrThrow( "mensagens", null, cv );
			//  corpos de mensagens
			sql = "INSERT INTO  VALUES " +
				"(  )";
			cv = new ContentValues( 10 );
			cv.put( "cor_id", 1 );
			cv.put( "msg_id", 1 );
			cv.put( "cor_ticorpo", 1 );
			cv.put( "cor_texto", "corpo 1 mensagem 1" );
			ret = db.insertOrThrow( "corpo", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "cor_id", 2 );
			cv.put( "msg_id", 1 );
			cv.put( "cor_ticorpo", 1 );
			cv.put( "cor_texto", "corpo 2 mensagem 1" );
			ret = db.insertOrThrow( "corpo", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "cor_id", 3 );
			cv.put( "msg_id", 2 );
			cv.put( "cor_ticorpo", 1 );
			cv.put( "cor_texto", "corpo 1 mensagem 2" );
			ret = db.insertOrThrow( "corpo", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "cor_id", 4 );
			cv.put( "msg_id", 3 );
			cv.put( "cor_ticorpo", 1 );
			cv.put( "cor_texto", "corpo 1 mensagem 3" );
			ret = db.insertOrThrow( "corpo", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "cor_id", 5 );
			cv.put( "msg_id", 4 );
			cv.put( "cor_ticorpo", 1 );
			cv.put( "cor_texto", "corpo 1 mensagem 4" );
			ret = db.insertOrThrow( "corpo", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "cor_id", 6 );
			cv.put( "msg_id", 5 );
			cv.put( "cor_ticorpo", 1 );
			cv.put( "cor_texto", "corpo 1 mensagem 5" );
			ret = db.insertOrThrow( "corpo", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "cor_id", 7 );
			cv.put( "msg_id", 6 );
			cv.put( "cor_ticorpo", 1 );
			cv.put( "cor_texto", "corpo 1 mensagem 6" );
			ret = db.insertOrThrow( "corpo", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "cor_id", 8 );
			cv.put( "msg_id", 7 );
			cv.put( "cor_ticorpo", 1 );
			cv.put( "cor_texto", "corpo 1 mensagem 7" );
			ret = db.insertOrThrow( "corpo", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "cor_id", 9 );
			cv.put( "msg_id", 8 );
			cv.put( "cor_ticorpo", 1 );
			cv.put( "cor_texto", "corpo 1 mensagem 8" );
			ret = db.insertOrThrow( "corpo", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "cor_id", 10 );
			cv.put( "msg_id", 8 );
			cv.put( "cor_ticorpo", 1 );
			cv.put( "cor_texto", "http://www.tetra.srv.br" );
			ret = db.insertOrThrow( "corpo", null, cv );
			cv = new ContentValues( 10 );
			cv.put( "cor_id", 11 );
			cv.put( "msg_id", 9 );
			cv.put( "cor_ticorpo", 1 );
			cv.put( "cor_texto", "corpo 1 mensagem 9" );
			ret = db.insertOrThrow( "corpo", null, cv );
			}
		catch( Exception e )
			{
			lastMessage = e.getMessage();
			Log.i( apptag, "Globais: criando testes " + e.getMessage() );
			return false;
			}
		return true;
		}
	
	private static boolean initDB()
		{
		//  cria o registro de configuração
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
		if( versaoDB <= config.vrsdb )
			return true;
		try
			{
			if( versaoDB >= 5 )
				{
				String alter =  "ALTER TABLE dispositivo ADD COLUMN " +
												"dis_flpenden INTEGER DEFAULT 0";
				db.execSQL( alter );
				String sql = "UPDATE DISPOSITIVO SET dis_vrsdb=1, dis_vrsapp='1.0.0'";
				db.execSQL( sql );
				}
			String sql = "UPDATE DISPOSITIVO SET " +
				"dis_vrsdb=" + versaoDB + ", " +
				"dis_vrsapp='" + versaoApp + "'";
			db.execSQL( sql );
			config.vrsdb = versaoDB;
			}
		catch( Exception exc )
			{
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
			String sql = "SELECT dis_id, dis_nuserie, dis_vrsdb, dis_hoinic, dis_hoterm, dis_flwifi, dis_flsilen " +
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
				config.flSilen = false;
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
					if( c.getInt( c.getColumnIndex( "dis_flsilen" ) ) == 1 )
						config.flSilen = true;
					else
						config.flSilen = false;
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
	//  converte string dd/mm/aaaa para date
	public static Date toDate( String data )
		{
		String[] strdt = data.split( "/" );
		if( strdt.length != 3 )
			return null;
		try
			{
			int dia = Integer.parseInt( strdt[0] );
			int mes = Integer.parseInt( strdt[1] );
			int ano = Integer.parseInt( strdt[2] );
			Date res = new Date( ano - 1900, mes - 1, dia );
			if( res.getMonth() != mes - 1 )
				return null;
			if( res.getYear() != ano - 1900 )
				return null;
			return res;
			}
		catch( Exception exc )
			{
			return null;
			}
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
	
	static public void setMenuPadrao( Context contx, Menu menu )
		{
		int order = 0;
		menu.add( 0, R.integer.mnidAddAlvo, ++order, R.string.mntxAddAlvo );
		menu.add( 0, R.integer.mnidSetup, ++order, R.string.mntxSetup );
		String idtel = nuSerial();
		if( idtel != null )
			{
			if( idtel.equals( "355256063048937" ) ||
					idtel.equals( "354133073258312" ) ||
					idtel.equals( "000000000000000" ) )
				{
				menu.add( 0, R.integer.mnidToken, ++order, R.string.mntxToken );
				menu.add( 0, R.integer.mnidTestes, ++order, R.string.mntxTestes );
				menu.add( 0, R.integer.mnidRemoveDB, ++order, R.string.mntxRemoveDB );
				menu.add( 0, R.integer.mnidCopyDB, ++order, R.string.mntxCopyDB );
				menu.add( 0, R.integer.mnidRecuDB, ++order, R.string.mntxRecuDB );
				menu.add( 0, R.integer.mnidInitDB, ++order, R.string.mntxInitDB );
				}
			}
		}
	
	static public void prcMenuItem( Context ctx, int idmenu )
		{
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
		
		if( idmenu == R.integer.mnidTestes )
			{
			
			Globais.Alerta( ctx, "Titulo", "Mensagem linha 1\nMensagem Linha 2" );
			return;
			}
		
		if( idmenu == R.integer.mnidInitDB )
			{
			dadosTeste();
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
