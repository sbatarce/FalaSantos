package com.pms.falasantos.Adaptadores;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.pms.falasantos.Outras.clAlvo;
import com.pms.falasantos.R;

import java.util.HashMap;
import java.util.List;
/**
 * Created by w0513263 on 14/09/17.
 */

public class ElsAreasAdapter extends BaseExpandableListAdapter
	{
	private Context                       ctx;
	private List<String>                  lsAreas;
	private HashMap<String, List<clAlvo>> lista;
	
	public ElsAreasAdapter( Context ctx, List<String> lsAreas, HashMap<String, List<clAlvo>> lista )
		{
		this.ctx = ctx;
		this.lsAreas = lsAreas;
		this.lista = lista;
		}
	
	@Override
	public int getGroupCount()
		{
		return lsAreas.size();
		}
	@Override
	public int getChildrenCount( int grpos )
		{
		return lista.get( lsAreas.get( grpos ) ).size();
		}
	@Override
	public Object getGroup( int grpos )
		{
		return lsAreas.get( grpos );
		}
	@Override
	public Object getChild( int grpos, int rmpos )
		{
		return lista.get( lsAreas.get( grpos ) ).get( rmpos );
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
		String clalv = (String) getGroup( grpos );
		if( view == null )
			{
			LayoutInflater infalInflater = (LayoutInflater) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			view = infalInflater.inflate( R.layout.area_alvo_item, null );
			}
		((TextView)view.findViewById( R.id.txAreaAlvo )).setText( lsAreas.get(grpos) );
		return view;
		}
	@Override
	public View getChildView( int grpos, int rmpos, boolean b, View view, ViewGroup viewGroup )
		{
		final clAlvo clAlvo = (clAlvo) getChild( grpos, rmpos );
		
		if( view == null )
			{
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService( ctx.LAYOUT_INFLATER_SERVICE );
			view = inflater.inflate( R.layout.area_alvo_item, null );
			}
		((TextView)view.findViewById( R.id.txAreaAlvo )).setText( clAlvo.alvo );
		return view;
		}
	@Override
	public boolean isChildSelectable( int grpos, int rmpos )
		{
		return true;
		}
	}
