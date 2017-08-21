package com.pms.falasantos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity
	{
	ExpandableListView elsAlvos;
	ElsAlvosAdapter expadapter;

	@Override
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		//
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setCustomView(R.layout.actbar);
		View view =getSupportActionBar().getCustomView();
		//  verifica se é uma URL
		Bundle ext = getIntent().getExtras();
		if( ext != null )
			{
			if( ext.containsKey( "url" ) )
				{
				Log.i( Globais.apptag, "tem data" );
				String url = ext.getString( "url" );
				Uri uri = Uri.parse( url );
				Intent intent = new Intent( Intent.ACTION_VIEW, uri );
				intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
				finishAffinity();
				return;
				}
			}
		//  prepara a lista de alvos
		elsAlvos = (ExpandableListView) findViewById( R.id.elsAlvos );
		}
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu )
		{
		//  infla o menu
		getMenuInflater().inflate( R.menu.menu_main, menu );
		return true;
		}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item )
		{
		int id = item.getItemId();
		
		//noinspection SimplifiableIfStatement
		if( id == R.id.action_settings )
			{
			return true;
			}
		
		if( id == R.id.action_token )
			{
			String token = FirebaseInstanceId.getInstance().getToken();
			Log.i( "falaSantos", "<" + token + ">" );
			
			android.content.ClipboardManager clipboard =
				(android.content.ClipboardManager) this.getSystemService( Context.CLIPBOARD_SERVICE );
			android.content.ClipData clip =
				android.content.ClipData.newPlainText( "Copied Text", token );
			clipboard.setPrimaryClip( clip );
			}
		
		return super.onOptionsItemSelected( item );
		}
	//
	private void setupList()
		{
		elsAlvos.setOnGroupExpandListener( new ExpandableListView.OnGroupExpandListener()
			{
			@Override
			public void onGroupExpand( int ixgr )
				{
				Log.i( Globais.apptag, "onGroupExpand i=" + ixgr );
				View view = null;
				ViewGroup gview = null;
				int qtch = expadapter.getChildrenCount( ixgr );
				}
			} );
		}
	}
