package br.com.a2dm.brcmn.service;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import br.com.a2dm.brcmn.entity.UsuarioDevice;
import br.com.a2dm.brcmn.util.A2DMHbNgc;
import br.com.a2dm.brcmn.util.RestritorHb;

public class UsuarioDeviceService extends A2DMHbNgc<UsuarioDevice>
{
	private static UsuarioDeviceService instancia = null;

	@SuppressWarnings("rawtypes")
	private static Map filtroPropriedade = new HashMap();
	
	@SuppressWarnings("rawtypes")
	private static Map restritores = new HashMap();
	
	public static UsuarioDeviceService getInstancia()
	{
		if (instancia == null)
		{
			instancia = new UsuarioDeviceService();
		}
		return instancia;
	}
	
	public UsuarioDeviceService()
	{
		adicionarFiltro("idUsuario", RestritorHb.RESTRITOR_EQ,"idUsuario");
		adicionarFiltro("idDevice", RestritorHb.RESTRITOR_EQ,"idDevice");
	}
		
	@Override
	protected Criteria montaCriteria(Session sessao, int join)
	{
		Criteria criteria = sessao.createCriteria(UsuarioDevice.class);
		
		return criteria;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	protected Map restritores() 
	{
		return restritores;
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected Map filtroPropriedade() 
	{
		return filtroPropriedade;
	}
	
	@Override
	protected void setarOrdenacao(Criteria criteria, UsuarioDevice vo, int join) {
		criteria.addOrder(Order.desc("idUsuarioDevice"));
	}
}