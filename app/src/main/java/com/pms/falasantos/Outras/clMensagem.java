package com.pms.falasantos.Outras;
/**
 * Created by w0513263 on 12/09/17.
 */

public class clMensagem
	{
	public String titulo;
	public String dareceb;
	public String  daleitu;
	public String  mensagem;
	public String  idmens;
	public boolean flresp;      //  indica que a mensagem tem pelo menos 1 corpo com resposta
	
	public clMensagem( String titulo, String dareceb, String daleitu, String mensagem, String idmens )
		{
		this.titulo = titulo;
		this.dareceb = dareceb;
		this.daleitu = daleitu;
		this.mensagem = mensagem;
		this.idmens = idmens;
		this.flresp = false;
		}
	public boolean getFlresp()
		{
		return flresp;
		}
	public void setFlresp( boolean flresp )
		{
		this.flresp = flresp;
		}
	}
