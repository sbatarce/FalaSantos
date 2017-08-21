package com.pms.falasantos;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
/**
 * Created by w0513263 on 17/08/17.
 */

public class ElsAlvosAdapter extends BaseExpandableListAdapter
	{
	private Context ctx;
	private List<String> lsAlvos;
	private List<String> lsRemet;
	private HashMap<String, List<String>> lista;
	
	public ElsAlvosAdapter( Context ctx, List<String> lsAlvos, HashMap<String, List<String>> lista )
		{
		this.ctx = ctx;
		this.lsAlvos = lsAlvos;
		this.lista = lista;
		}
	
	@Override
	public int getGroupCount()
		{
		return lsAlvos.size();
		}
	@Override
	public int getChildrenCount( int grpos )
		{
		return lista.get( lsAlvos.get( grpos ) ).size();
		}
	@Override
	public Object getGroup( int grpos )
		{
		return lsAlvos.get( grpos );
		}
	@Override
	public Object getChild( int grpos, int rmpos )
		{
		return lista.get( lsAlvos.get( grpos ) ).get( rmpos );
		}
	@Override
	public long getGroupId( int grpos )
		{
		return grpos;
		}
	@Override
	public long getChildId( int grpos, int rmpos )
		{
		return rmpos;
		}
	@Override
	public boolean hasStableIds()
		{
		return false;
		}
	@Override
	public View getGroupView( int grpos, boolean b, View view, ViewGroup viewGroup )
		{
		String titulo = (String) getGroup( grpos );
		if( view == null )
			{
			LayoutInflater infalInflater = (LayoutInflater) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			view = infalInflater.inflate( R.layout.grupo_alvos, null );
			}
		
		((TextView)view.findViewById( R.id.txAlvo )).setText( titulo );
		return view;
		}
	@Override
	public View getChildView( int grpos, int rmpos, boolean b, View view, ViewGroup viewGroup )
		{
		return null;
		}
	@Override
	public boolean isChildSelectable( int grpos, int rmpos )
		{
		return false;
		}
	}
