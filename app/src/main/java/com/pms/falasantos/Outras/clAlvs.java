package com.pms.falasantos.Outras;
/**
 * Created by w0513263 on 12/09/17.
 */

public class clAlvs
	{
	public String alvo;       //  nome do alvo
	public int    id;         //  id do alvo no banco
	public int    qtALer;     //  quantidade de mensagens não lidas
	public int    qtTot;      //  quantidade total de mensagens não removidas
	
	public clAlvs( String alvo, int id, int qtALer, int qtTot )
		{
		this.alvo = alvo;
		this.id = id;
		this.qtALer = qtALer;
		this.qtTot = qtTot;
		}
	}
