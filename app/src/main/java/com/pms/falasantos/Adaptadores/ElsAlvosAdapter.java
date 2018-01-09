package com.pms.falasantos.Adaptadores;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.pms.falasantos.Outras.clAlvs;
import com.pms.falasantos.Outras.clRems;
import com.pms.falasantos.R;

import java.util.HashMap;
import java.util.List;
/**
 * Created by w0513263 on 17/08/17.
 */

public class ElsAlvosAdapter extends BaseExpandableListAdapter
	{
	private Context                       ctx;
	private List<clAlvs>                  lsAlvos;
	private HashMap<clAlvs, List<clRems>> lista;
	
	public ElsAlvosAdapter( Context ctx, List<clAlvs> lsAlvos, HashMap<clAlvs, List<clRems>> lista )
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
		clAlvs clalv = (clAlvs) getGroup( grpos );
		if( view == null )
			{
			LayoutInflater infalInflater = (LayoutInflater) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			view = infalInflater.inflate( R.layout.alvo_item, null );
			}
		
		((TextView)view.findViewById( R.id.txAlvo )).setText( clalv.alvo );
		((TextView)view.findViewById( R.id.txTotALer )).setText( ""+clalv.qtALer+"/"+clalv.qtTot );
		return view;
		}
	@Override
	public View getChildView( int grpos, int rmpos, boolean b, View view, ViewGroup viewGroup )
		{
		final clRems clrems = (clRems) getChild( grpos, rmpos );
		
		if( view == null )
			{
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService( ctx.LAYOUT_INFLATER_SERVICE );
			view = inflater.inflate( R.layout.rem_item, null );
			}
		((TextView)view.findViewById( R.id.txRemetente )).setText( clrems.rem );
		((TextView)view.findViewById( R.id.txQtALer )).setText( ""+clrems.qtALer+"/"+clrems.qtTot );
		return view;
		}
	@Override
	public boolean isChildSelectable( int grpos, int rmpos )
		{
		return true;
		}
	}