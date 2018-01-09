package com.pms.falasantos.Outras;
/**
 * Created by w0513263 on 14/09/17.
 */

public class clCampo
	{
	public int      tipo;
	public int      tamax;
	public String   nome;
	public String   campo;
	public String[] lsItens;
	public int      id;
	
	public clCampo( int tipo, int tamax, String nome, String campo, String itens )
		{
		this.tipo = tipo;
		this.nome = nome;
		this.campo = campo;
		this.tamax = tamax;
		this.lsItens = itens.split( "," );
		id = -1;
		}
	
	public int getId()
		{
		return id;
		}
	public void setId( int id )
		{
		this.id = id;
		}
	}
