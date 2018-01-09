package com.pms.falasantos.Outras;
/**
 * Created by w0513263 on 11/09/17.
 */

public class clRems
	{
	public int    idrem;        //  id do remetente
	public String rem;          //  nome do remetente
	public int    qtALer;       //  qtALer mensagens não lidas
	public int    qtTot;        //  total de mensagens não removidas
	
	public clRems( int idrem, String rem, int qtALer, int qtTot )
		{
		this.idrem = idrem;
		this.rem = rem;
		this.qtALer = qtALer;
		this.qtTot = qtTot;
		}
	}
