package com.pms.falasantos.Outras;
/**
 * Created by w0513263 on 12/09/17.
 */

public class clMensagem
	{
	public String  titulo;
	public String  danotif;
	public String  dareceb;
	public String  daleitu;
	public String  daresp;
	public String  mensagem;
	public String  idmens;      //  id da mensagem msg_id => vide msg_msaid
	public boolean flresp;      //  indica que a mensagem tem pelo menos 1 corpo com resposta
	public boolean confidencial;      //  indica que a mensagem é confidencial
	
	public clMensagem( String titulo, String danotif, String dareceb, String daleitu, String daresp, String mensagem, String idmens )
		{
		this.titulo = titulo;
		this.danotif = danotif;
		this.dareceb = dareceb;
		this.daleitu = daleitu;
		this.daresp = daresp;
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
	public boolean isConfidencial()
		{
		return confidencial;
		}
	public void setConfidencial( boolean confidencial )
		{
		this.confidencial = confidencial;
		}
	}
