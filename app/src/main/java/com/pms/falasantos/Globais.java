package com.pms.falasantos;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
/**
 * Created by w0513263 on 26/07/17.
 */

public class Globais
	{
	static public String apptag = "falaSantos";
	
	static public Context ctx = null;
	static private Resources resources = null;
	static private String nodb = "falasantos.db";
	static private String pathDB = "";
	static private String pathSD = "";
	
	static public SQLiteDatabase db = null;
	
	static public boolean dbOK = false;
	static public boolean dbnew;
	static public String lastMessage = "ok";
	
	static private String versaoapp = "'1.0.0";
	static private int versaoDB = 1;
	
	static public class config
		{
		static public String id;
		static public String nuserie;
		static public String destin;
		static public String sshd;
		static public String cpf;
		static public String dtnas;
		static public String hoini;
		static public String hofim;
		static public int vrsdb;
		static public boolean flWIFI;
		static public boolean flSilen;
		static public boolean flSinc;
		}
	
	//  salva o contexto da Activity que chamou
	static public void setContext( Context context )
		{
		ctx = context;
		resources = ctx.getResources();
		pathDB = context.getDatabasePath( nodb ).getAbsolutePath();
		pathSD = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
	
	//  subrotinas dedicadas ao banco de dados
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
		if( !ajustaVersao( versaoDB ) )
			return false;
		//  verifica se o telefone ja tem perfis associados
		if( config.vrsdb == 0 )
			dbnew = true;
		dbOK = true;
		return true;
		}
	
	//  cria as tabelas usando o dbscripts
	public static boolean criaTabelas()
		{
		int dbv = resources.getInteger( R.integer.dbvers );
		Cursor c = db.rawQuery(
			"select DISTINCT tbl_name from sqlite_master where tbl_name ='dispositivo'", null );
		if( c.getCount() > 0 )
			{
			c.close();
			ajustaVersao( dbv );
			dbnew = false;
			return true;
			}
		c.close();
		//  cria as tabelas do script
		TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(
			Context.TELEPHONY_SERVICE );
		String idtel = telephonyManager.getDeviceId();
		String nutel = telephonyManager.getLine1Number();
		String serial = telephonyManager.getSimSerialNumber();
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
		//  cria o registro de configuração
		ContentValues cv = new ContentValues( 15 );
		
		cv.put( "dis_id", 1 );
		cv.put( "dis_nuserie", serial );
		cv.put( "dis_vrsdb", 0 );
		cv.put( "dis_vrsapp", "1.0.0" );
		cv.put( "dis_hoinic", "08:00" );
		cv.put( "dis_hoterm", "21:00" );
		cv.put( "dis_flwifi", 0 );
		cv.put( "dis_flsilen", 0 );
		//
		long ret = -1;
		try
			{
			ret = db.insert( "dispositivo", null, cv );
			}
		catch( Exception e )
			{
			lastMessage = e.getMessage();
			Log.i( apptag, "Globais: criaTabelas dispositivo " + e.getMessage() );
			return false;
			}
		//
		cv = new ContentValues( 15 );
		
		cv.put( "des_id", 1 );
		cv.put( "des_sshd", "" );
		cv.put( "des_cpf", "" );
		cv.put( "des_dtnasc", "" );
		cv.put( "des_nome", "" );
		//
		ret = -1;
		try
			{
			ret = db.insert( "destinatario", null, cv );
			}
		catch( Exception e )
			{
			lastMessage = e.getMessage();
			Log.i( apptag, "Globais: criaTabelas destinatario " + e.getMessage() );
			return false;
			}
		//
		return true;
		}
	
	//  ajusta a versão do DB
	static boolean ajustaVersao( int dbvers )
		{
		if( dbvers <= config.vrsdb )
			return true;
		try
			{
			if( dbvers == 1 )
				{
				String sql = "UPDATE DISPOSITIVO SET dis_vrsdb=1, dis_vrsapp='1.0.0'";
				db.execSQL( sql );
				}
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
		String sql = "SELECT dis_nuserie, dis_vrsdb, dis_hoinic, dis_hoterm, dis_flwifi, dis_flsilen " +
			"FROM dispositivo";
		Cursor c = db.rawQuery( sql, null );
		if( c.moveToNext() )
			{
			config.nuserie = c.getString( c.getColumnIndex( "dis_nuserie" ) );
			config.vrsdb = c.getInt( c.getColumnIndex( "dis_vrsdb" ) );
			config.hoini = c.getString( c.getColumnIndex( "dis_hoinic" ) );
			config.hofim = c.getString( c.getColumnIndex( "dis_hoterm" ) );
			if( c.getInt( c.getColumnIndex( "flWIFI" ) ) == 1 )
				config.flWIFI = true;
			else
				config.flWIFI = false;
			if( c.getInt( c.getColumnIndex( "flSilen" ) ) == 1 )
				config.flSilen = true;
			else
				config.flSilen = false;
			if( c.getInt( c.getColumnIndex( "flSinc" ) ) == 1 )
				config.flSinc = true;
			else
				config.flSinc = false;
			}
		else
			return false;
		c.close();
		//
		sql = "SELECT des_sshd, des_cpf, des_dtnasc, des_nome " +
			"FROM destinatario";
		c = db.rawQuery( sql, null );
		if( c.moveToNext() )
			{
			config.sshd = c.getString( c.getColumnIndex( "des_sshd" ) );
			config.cpf = c.getString( c.getColumnIndex( "des_cpf" ) );
			config.dtnas = c.getString( c.getColumnIndex( "des_dtnas" ) );
			config.destin = c.getString( c.getColumnIndex( "des_nome" ) );
			}
		else
			return false;
		c.close();
		return true;
		}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////
	///// somente para testes ///////////////////////////////////////////////////////////////////////
	static boolean dadosTeste()
		{
		String sql;
		String local = "inicio";
		try
			{
			local = "areas";
			sql = "INSERT INTO areas VALUES " +
				"( 100, 'Ponto' )" +
				"( 102, 'Educação' )" +
				"( 103, 'Saúde' )" +
				"( 199, 'Mensageria' )";
			db.execSQL( sql );
			
			local = "alvos";
			sql = "INSERT INTO alvos VALUES " +
				"( 1, 100, 1, 'alvo 1' )," +
				"( 2, 100, 2, 'alvo 2' )," +
				"( 3, 100, 3, 'alvo 3' )," +
				"( 4, 103, 4, 'alvo 4' )," +
				"( 5, 103, 5, 'alvo 5' )";
			db.execSQL( sql );
			
			local = "remetentes";
			sql = "INSERT INTO remetentes VALUES " +
				"( 1, 1, 1, 'remetente 1', 0 ), " +
				"( 2, 1, 2, 'remetente 2', 0 ), " +
				"( 3, 2, 1, 'remetente 1', 0 ), " +
				"( 4, 2, 2, 'remetente 2', 0 ), " +
				"( 5, 3, 1, 'remetente 1', 0 ), " +
				"( 6, 3, 2, 'remetente 2', 0 ), " +
				"( 7, 3, 3, 'remetente 3', 0 ), " +
				"( 8, 3, 4, 'remetente 4', 0 ), ";
			db.execSQL( sql );
			
			local = "mensagens";
			sql = "INSERT INTO mensagens VALUES " +
				"(  1, 1,  1, 1, '201608101500', '201608101500', '201608101500', '201608101500', '201608101500', 1 ), " +
				"(  2, 1,  2, 1, '201608101500', '201608101500', '201608101500', '201608101500', '201608101500', 1 ), " +
				"(  3, 1,  3, 1, '201608101500', '201608101500', '201608101500', '201608101500', '201608101500', 1 ), " +
				"(  4, 2,  4, 1, '201608101500', '201608101500', '201608101500', '201608101500', '201608101500', 1 ), " +
				"(  5, 2,  5, 1, '201608101500', '201608101500', '201608101500', '201608101500', '201608101500', 1 ), " +
				"(  6, 2,  6, 1, '201608101500', '201608101500', '201608101500', '201608101500', '201608101500', 1 ), " +
				"(  7, 3,  7, 1, '201608101500', '201608101500', '201608101500', '201608101500', '201608101500', 1 ), " +
				"(  8, 3,  8, 1, '201608101500', '201608101500', '201608101500', '201608101500', '201608101500', 1 ), " +
				"(  9, 3,  9, 1, '201608101500', '201608101500', '201608101500', '201608101500', '201608101500', 1 ), " +
				"( 10, 3,  0, 1, '201608101500', '201608101500', '201608101500', '201608101500', '201608101500', 1 ), " +
				"( 11, 3, 11, 1, '201608101500', '201608101500', '201608101500', '201608101500', '201608101500', 1 ), " +
				"( 12, 3, 12, 1, '201608101500', '201608101500', '201608101500', '201608101500', '201608101500', 1 ), ";
			db.execSQL( sql );
			
			local = "corpo";
			sql = "INSERT INTO corpo VALUES " +
				"(  1,  1, 1, 'Texto 1 ' ), " +
				"(  2,  2, 1, 'Texto Texto ' ), " +
				"(  3,  3, 1, 'Texto Texto Texto ' ), " +
				"(  4,  4, 1, 'Texto Texto Texto Texto ' ), " +
				"(  5,  5, 1, 'Texto Texto Texto Texto Texto ' ), " +
				"(  6,  6, 1, 'Texto Texto Texto Texto Texto Texto ' ), " +
				"(  7,  7, 1, 'Texto Texto Texto Texto Texto Texto Texto ' ), " +
				"(  8,  8, 1, 'Texto Texto Texto Texto Texto Texto Texto Texto ' ), " +
				"(  9,  9, 1, 'Texto Texto Texto Texto Texto Texto Texto Texto Texto ' ), " +
				"( 10, 10, 1, 'Texto Texto Texto Texto Texto Texto Texto Texto Texto Texto ' ), " +
				"( 11, 11, 1, 'Texto Texto Texto Texto Texto Texto Texto Texto Texto Texto Texto ' ), " +
				"( 12, 12, 1, 'Texto Texto Texto Texto Texto Texto Texto Texto Texto Texto Texto Texto ' ), ";
			db.execSQL( sql );
			}
			catch( Exception exc )
				{
				Log.i( apptag, "dadosTest " + local + " Erro: " + exc.getMessage() );
				return false;
				}
		
		//
		return true;
		}
	}
