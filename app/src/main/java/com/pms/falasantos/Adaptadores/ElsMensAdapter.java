package com.pms.falasantos.Adaptadores;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.pms.falasantos.Outras.clMensagem;
import com.pms.falasantos.R;

import java.util.HashMap;
import java.util.List;
/**
 * Created by w0513263 on 12/09/17.
 */

public class ElsMensAdapter extends BaseExpandableListAdapter
	{
	private Context                           ctx;
	private List<String>                      lstitulo;
	private HashMap<String, List<clMensagem>> lista;
	
	public ElsMensAdapter( Context ctx, List<String> lstitulo, HashMap<String, List<clMensagem>> lista )
		{
		this.ctx = ctx;
		this.lstitulo = lstitulo;
		this.lista = lista;
		}
	
	@Override
	public int getGroupCount()
		{
		return lstitulo.size();
		}
	@Override
	public int getChildrenCount( int grpos )
		{
		return lista.get( lstitulo.get( grpos ) ).size();
		}
	@Override
	public Object getGroup( int grpos )
		{
		return lstitulo.get( grpos );
		}
	@Override
	public Object getChild( int grpos, int chpos )
		{
		return lista.get( lstitulo.get( grpos ) ).get( chpos );
		}
	@Override
	public long getGroupId( int grpos )
		{
		return grpos;
		}
	@Override
	public long getChildId( int grpos, int chpos )
		{
		return chpos;
		}
	@Override
	public boolean hasStableIds()
		{
		return false;
		}
	//  chamado para montar o título da lista expansível
	@Override
	public View getGroupView( int grpos, boolean expandido, View view, ViewGroup viewGroup )
		{
		String titulo = (String) getGroup( grpos );
		if( view == null )
			{
			LayoutInflater infalInflater = (LayoutInflater) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			view = infalInflater.inflate( R.layout.mens_grupo, null );
			}
		
		List<clMensagem> lsmsg = lista.get( lstitulo.get( grpos ));
		boolean flaler = false;
		((TextView)view.findViewById( R.id.txDaRecep )).setText( titulo );
		for( int i=0; i<lsmsg.size(); i++ )
			{
			if( lsmsg.get( i ).daleitu == null )
				{
				flaler = true;
				break;
				}
			}
		if( flaler )
			{
			((TextView) view.findViewById( R.id.txDaRecep )).setTypeface( null, Typeface.BOLD );
			view.findViewById( R.id.imDtStat).setBackgroundResource( R.drawable.vezinhoverde );
			}
		else
			{
			((TextView) view.findViewById( R.id.txDaRecep )).setTypeface( null, Typeface.NORMAL );
			view.findViewById( R.id.imDtStat).setBackgroundResource( R.drawable.vezinhocinza );
			}
			
		return view;
		}
	
	//  chamado para montar a parte expansível da lista
	@Override
	public View getChildView( int grpos, int chpos, boolean ultimo, View view, ViewGroup viewGroup )
		{
		final clMensagem clmens = (clMensagem) getChild( grpos, chpos );
		
		if( view == null )
			{
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService( ctx.LAYOUT_INFLATER_SERVICE );
			view = inflater.inflate( R.layout.mens_item, null );
			}
		if( clmens.isConfidencial() )
			((TextView)view.findViewById( R.id.txTitulo )).setText( "Confidencial" );
		else
			((TextView)view.findViewById( R.id.txTitulo )).setText( clmens.titulo );
		((TextView)view.findViewById( R.id.txDaEnvio )).setText( clmens.danotif.substring( 11 ) );

		String mens = "Recebimento:" + clmens.dareceb + " ";
		if( clmens.daleitu == null || clmens.daleitu.equals( "" ) )
			{
			mens += "\ntoque para ler a mensagem";
			((TextView)view.findViewById( R.id.txMensagem )).setText( mens );
			return view;
			}
		mens += "\nLeitura:" + clmens.daleitu + " ";
		if( clmens.daresp == null || clmens.daresp.equals( "" ) )
			{
			if( clmens.flresp )
				mens += "\nA responder...";
			((TextView)view.findViewById( R.id.txMensagem )).setText( mens );
			return view;
			}
		mens += "\nResposta: " + clmens.daresp;
		((TextView)view.findViewById( R.id.txMensagem )).setText( mens );
		return view;
		}
	@Override
	public boolean isChildSelectable( int grpos, int chpos )
		{
		return true;
		}
	}
