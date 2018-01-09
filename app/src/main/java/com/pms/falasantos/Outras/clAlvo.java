package com.pms.falasantos.Outras;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by w0513263 on 14/09/17.
 * <p>
 * classe de alvos a apresentar ao usuário para escolha
 */

public class clAlvo
	{
	public int id;
	public int idcvl;
	
	public String   alvo;
	public String   origem;
	public String   area;
	public String   service;
	
	public List<clCampo> campos;
	
	public clAlvo( int id, int idcvl, String alvo, String origem, String area, String service )
		{
		this.id = id;
		this.idcvl = idcvl;
		
		this.alvo = alvo;
		this.origem = origem;
		this.area = area;
		this.service = service;
		campos = new ArrayList<>(  );
		}
	
	public void addCampo( int idtcv, int tamax, String nome, String campo, String itens )
		{
		clCampo cmp = new clCampo( idtcv, tamax, nome, campo, itens );
		campos.add( cmp );
		}
	
	public int qtCampos()
		{
		return campos.size();
		}
	}
