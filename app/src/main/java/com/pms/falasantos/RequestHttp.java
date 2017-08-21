package com.pms.falasantos;
/**
 * Created by w0513263 on 21/08/17.
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RequestHttp extends AsyncTask< String, Long, String >
	{
	enum HTTPSTATE
		{
			conectado,
			conectando,
			desconectado,
			desconhecido
		}
	public HTTPSTATE httpState = HTTPSTATE.desconectado;
	public HTTPSTATE wifiState = HTTPSTATE.desconectado;
	
	public  RespostaConfig            delegate  = null;
	private String                    authbasic = null;
	private byte[]                    body      = null;
	private HashMap< String, String > header    = null;
	Context context;
	public void setAuth( String user, String senha )
		{
		String aux = user + ":" + senha;
		authbasic = "Basic " + Base64.encodeToString( aux.getBytes( ), Base64.DEFAULT );
		}
	public void setHeader( String key, String val )
		{
		if( header == null )
			header = new HashMap< String, String >( );
		header.put( key, val );
		}
	
	public boolean setBody( String body )
		{
		if( body == null || body.length( ) < 1 )
			return false;
		try
			{
			this.body = body.getBytes( "UTF-8" );
			}
		catch( Exception exc )
			{
			return false;
			}
		return true;
		}
	
	public HTTPSTATE Conectado()
		{
		return VerifyNet();
		}
	
	public HTTPSTATE WIFI()
		{
		VerifyNet();
		return wifiState;
		}
	
	public HTTPSTATE VerifyNet( )
		{
		httpState = HTTPSTATE.desconhecido;
		wifiState = HTTPSTATE.desconhecido;
		ConnectivityManager cm = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		
		if( netInfo == null )
			return httpState;
		if( netInfo.isConnectedOrConnecting( ) )
			httpState = HTTPSTATE.conectando;
		if( netInfo.isConnected( ) )
			httpState = HTTPSTATE.conectado;
		if( httpState == HTTPSTATE.conectado )
			{
			NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if( mWifi.isConnected() )
				wifiState = HTTPSTATE.conectado;
			else
				wifiState = HTTPSTATE.desconectado;
			}
		return httpState;
		}
	
	public RequestHttp( Context context )
		{
		this.context = context;
		//
		VerifyNet();
		}
	@Override
	protected void onPreExecute( )
		{
		super.onPreExecute( );
		}
	/**
	 * inputToString
	 * convert um InputStream para String
	 * emite publicacoes do progresso da conversao
	 *
	 * @param stream =>  conteudo a converter
	 * @return =>  String com conteudo convertido
	 * @throws Exception
	 */
	private StringBuilder inputToString( InputStream stream, int len ) throws Exception
		{
		long tam = 0;
		BufferedReader r = new BufferedReader( new InputStreamReader( stream ) );
		StringBuilder tot = new StringBuilder( );
		String line;
		while( ( line = r.readLine( ) ) != null )
			{
			tot.append( line );
			tam += line.length( );
	    /*
      pct = (long)(tam * 100 / len );
      publishProgress( pct );
      */
			}
		stream.close( );
		return tot;
		}                                   //  inputToString
	
	@Override
	protected String doInBackground( String... params )
		{
		String sturl = params[ 0 ];
		String resp;
		HttpURLConnection conn = null;
		InputStream in;
		try
			{
			URL url = new URL( sturl );
			conn = ( HttpURLConnection ) url.openConnection( );
			conn.setRequestMethod( "POST" );
			conn.setRequestProperty( "Accept-Charset", "ISO-8859-1" );
			if( authbasic != null )
				{
				conn.setRequestProperty( "Authorization", authbasic );
				authbasic = null;
				}
			if( header != null )
				{
				for( Map.Entry< String, String > hea : header.entrySet( ) )
					{
					conn.setRequestProperty( hea.getKey( ), hea.getValue( ) );
					}
				header = null;
				}
			if( body != null )
				{
				conn.setDoOutput( true );
				conn.setInstanceFollowRedirects( false );
				conn.setRequestProperty( "Content-Length", "" + body.length );
				conn.setUseCaches( false );
				DataOutputStream wr = new DataOutputStream( conn.getOutputStream( ) );
				wr.write( body );
				body = null;
				}
			int stt = conn.getResponseCode( );
			if( stt >= 300 )
				in = conn.getErrorStream( );
			else
				{
				InputStream instr = conn.getInputStream( );
				in = new BufferedInputStream( instr );
				}
			int len = in.available( );
			resp = inputToString( in, len ).toString( );
			conn.disconnect( );
			}
		catch( MalformedURLException malf )
			{
			Log.i( "Chamada", "Malformed: " + malf.getMessage( ) );
			return "{ \"status\": \"Exception\", \"erro\": \"" + malf.getMessage( ) + "\"}";
			}
		catch( Exception exc )
			{
			Log.i( "Chamada", "Exception: " + exc.getMessage( ) );
			return "{ \"status\": \"Exception\", \"erro\": \"" + exc.getMessage( ) + "\"}";
			}
		return resp;
		}
	@Override
	protected void onPostExecute( String s )
		{
		delegate.Resposta( s );
		}
	@Override
	protected void onProgressUpdate( Long... values )
		{
		super.onProgressUpdate( values );
		}
	}
