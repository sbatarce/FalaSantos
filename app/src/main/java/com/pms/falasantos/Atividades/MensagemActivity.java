package com.pms.falasantos.Atividades;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pms.falasantos.Globais;
import com.pms.falasantos.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class MensagemActivity extends AppCompatActivity
	{
	LinearLayout llmens;
	int idmens = -1;
	String titulo;
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
		{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_mensagem );
		//
		Intent mensg = getIntent();
		idmens = Integer.parseInt( mensg.getStringExtra( "idmens" ) );
		titulo = mensg.getStringExtra( "titulo" );
		//
		llmens = (LinearLayout)findViewById( R.id.llmensagem );
		llmens.removeAllViews();
		//
		((TextView)findViewById( R.id.txTitMens )).setText( titulo );
		setupMens();
		}
	
	private void setupMens()
		{
		JSONObject mensg = new JSONObject(  );
		JSONArray corpos = new JSONArray(  );
		JSONObject corpo = new JSONObject(  );
		
		String sql = "SELECT cor_id, cor_ticorpo, cor_corpo, cor_texto," +
			"cor_stresposta, cor_stobrigatoria," +
			"opt_id, opt_codigo, opt_texto, opt_flchecked " +
			"FROM         corpo cor " +
			"inner join   opcoes opt ON " +
			"             opt.cor_id=cor.cor_id " +
			"WHERE cor.msg_id=? ";
		String[] parm = new String[]{""+idmens};
		Cursor curopt = Globais.db.rawQuery( sql, parm );
		int ticor;
		String codigo, texto;
		boolean flres, flobr;
		int corant = -1;
		while( curopt.moveToNext() )
			{
			int corid = curopt.getInt( curopt.getColumnIndex( "corid" ) );
			if( corid != corant )
				{
				ticor = curopt.getInt( curopt.getColumnIndex( "cor_ticorpo" ) );
				codigo = curopt.getString( curopt.getColumnIndex( "cor_corpo" ) );
				texto = curopt.getString( curopt.getColumnIndex( "cor_texto" ) );
				if( curopt.getInt( curopt.getColumnIndex( "cor_stresposta" ) ) == 1 )
					flres = true;
				else
					flres = false;
				if( curopt.getInt( curopt.getColumnIndex( "cor_stobrigatoria" ) ) == 1 )
					flobr = true;
				else
					flobr = false;
				corant = corid;
				}       //  quebra de corpo
			
			}         //  curopt.moveToNext
		}
	}
