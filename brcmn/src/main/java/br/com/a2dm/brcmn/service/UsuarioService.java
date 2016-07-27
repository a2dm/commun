package br.com.a2dm.brcmn.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import br.com.a2dm.brcmn.entity.Usuario;
import br.com.a2dm.brcmn.entity.log.UsuarioLog;
import br.com.a2dm.brcmn.service.log.UsuarioServiceLog;
import br.com.a2dm.brcmn.util.A2DMHbNgc;
import br.com.a2dm.brcmn.util.HibernateUtil;
import br.com.a2dm.brcmn.util.RestritorHb;
import br.com.a2dm.brcmn.util.criptografia.CriptoMD5;
import br.com.a2dm.brcmn.util.jsf.JSFUtil;
import br.com.a2dm.brcmn.util.outros.Email;
import br.com.a2dm.brcmn.util.outros.SenhaRandom;

public class UsuarioService extends A2DMHbNgc<Usuario>
{
	private JSFUtil util = new JSFUtil();
	
	private static UsuarioService instancia = null;
	
	public static final int JOIN_GRUPO = 1;
	
	public static final int JOIN_GRUPO_TELA_ACAO = 2;
	
	public static final int JOIN_TELA_ACAO = 4;
	
	public static final int JOIN_ACAO = 8;

	@SuppressWarnings("rawtypes")
	private static Map filtroPropriedade = new HashMap();
	
	@SuppressWarnings("rawtypes")
	private static Map restritores = new HashMap();
	
	public static UsuarioService getInstancia()
	{
		if (instancia == null)
		{
			instancia = new UsuarioService();
		}
		return instancia;
	}
	
	public UsuarioService()
	{
		adicionarFiltro("nome", RestritorHb.RESTRITOR_LIKE, "nome");
		adicionarFiltro("idUsuario", RestritorHb.RESTRITOR_EQ,"idUsuario");
		adicionarFiltro("login", RestritorHb.RESTRITOR_EQ, "login");
		adicionarFiltro("login", RestritorHb.RESTRITOR_LIKE, "filtroMap.likeLogin");
		adicionarFiltro("senha", RestritorHb.RESTRITOR_EQ, "senha");
		adicionarFiltro("flgAtivo", RestritorHb.RESTRITOR_EQ, "flgAtivo");
		adicionarFiltro("idConselho", RestritorHb.RESTRITOR_EQ, "idConselho");
		adicionarFiltro("numConselho", RestritorHb.RESTRITOR_EQ, "numConselho");
		adicionarFiltro("cpf", RestritorHb.RESTRITOR_EQ, "cpf");
		adicionarFiltro("flgAtivo", RestritorHb.RESTRITOR_EQ, "flgAtivo");
		adicionarFiltro("flgSeguranca", RestritorHb.RESTRITOR_EQ, "flgSeguranca");
		adicionarFiltro("telaAcao.idSistema", RestritorHb.RESTRITOR_EQ, "filtroMap.idSistema");
		adicionarFiltro("telaAcao.pagina", RestritorHb.RESTRITOR_EQ, "filtroMap.pagina");
		adicionarFiltro("acao.descricao", RestritorHb.RESTRITOR_EQ, "filtroMap.acao");
		adicionarFiltro("grupo.flgAtivo", RestritorHb.RESTRITOR_EQ, "filtroMap.flgAtivoGrupo");
		adicionarFiltro("listaGrupoTelaAcao.flgAtivo", RestritorHb.RESTRITOR_EQ, "filtroMap.flgAtivoGrupoTelaAcao");
		adicionarFiltro("telaAcao.flgAtivo", RestritorHb.RESTRITOR_EQ, "filtroMap.flgAtivoTelaAcao");
	}
	
	protected void validarInserir(Session sessao, Usuario vo) throws Exception
	{
		Criteria criteria = sessao.createCriteria(Usuario.class);
		Disjunction or = Restrictions.disjunction();
		or.add(Restrictions.eq("nome", vo.getNome()));
		or.add(Restrictions.eq("email",vo.getEmail()));
		or.add(Restrictions.eq("login",vo.getLogin()));
		or.add(Restrictions.eq("cpf",vo.getCpf()));
		criteria.add(or);
		
		Usuario usuario = (Usuario) criteria.uniqueResult();
		
		if(usuario != null)
		{
			if(vo.getLogin().equalsIgnoreCase(usuario.getLogin()))
			{
				throw new Exception("JÃ¡ existe um usuÃ¡rio cadastrado com este Login.");
			}
			
			if(vo.getCpf().equalsIgnoreCase(usuario.getCpf()))
			{
				throw new Exception("JÃ¡ existe um usuÃ¡rio cadastrado com este Cpf.");
			}
			
			if(vo.getEmail().equalsIgnoreCase(usuario.getEmail()))
			{
				throw new Exception("JÃ¡ existe um usuÃ¡rio cadastrado com este E-mail.");
			}
		}
		
		Usuario usrCons = new Usuario();
		usrCons.setIdConselho(vo.getIdConselho());
		usrCons.setNumConselho(vo.getNumConselho());
		
		List<Usuario> lista = this.pesquisar(sessao, usrCons, 0);
		
		if(lista != null
				&& lista.size() > 0)
		{
			throw new Exception("JÃ¡ existe um usuÃ¡rio com este Conselho.");
		}
	}
	
	@Override
	public Usuario alterar(Session sessao, Usuario vo) throws Exception
	{		
		Usuario usuario = new Usuario();
		usuario.setIdUsuario(vo.getIdUsuario());		
		usuario = this.get(sessao, usuario, 0);
		
		UsuarioLog usuarioLog = new UsuarioLog();
		JSFUtil.copiarPropriedades(usuario, usuarioLog);
		UsuarioServiceLog.getInstancia().inserir(sessao, usuarioLog);
		
		sessao.merge(vo);
		
		return vo;
	}
	
	@Override
	public Usuario inserir(Session sessao, Usuario vo) throws Exception
	{
		this.validarInserir(sessao, vo);
		
		//GERAR SENHA AUTOMÃ�TICA E INSERIR USUARIO
		String senha = SenhaRandom.getSenhaRandom();
		vo.setSenha(CriptoMD5.stringHexa(senha));
		sessao.save(vo);
		sessao.flush();
		
		//ENVIAR EMAIL
		this.enviarEmailSenha(vo, senha);
		
		return vo;
	}
	
	private void enviarEmailSenha(Usuario vo, String senha) throws Exception
	{
		Email email = new Email();
		
		String assunto = "Acesso A2DM - ClÃ­nica";
		String texto = "Nome: "+ vo.getNome() +" \n" +
				   "Login: "+ vo.getLogin() +" \n" +
				   "Senha: "+ senha;
		
		String to = vo.getEmail();
		
		email.enviar(to, assunto, texto);		
	}
	
	public Usuario ativar(Usuario vo) throws Exception
	{
		Session sessao = HibernateUtil.getSession();
		sessao.setFlushMode(FlushMode.COMMIT);
		Transaction tx = sessao.beginTransaction();
		try
		{
			vo = ativar(sessao, vo);
			tx.commit();
			return vo;
		}
		catch (Exception e)
		{
			tx.rollback();
			throw e;
		}
		finally
		{
			sessao.close();
		}
	}
	
	public Usuario ativar(Session sessao, Usuario vo) throws Exception
	{
		Usuario usuario = new Usuario();
		usuario.setIdUsuario(vo.getIdUsuario());
		usuario = this.get(sessao, usuario, 0);
		
		UsuarioLog usuarioLog = new UsuarioLog();
		JSFUtil.copiarPropriedades(usuario, usuarioLog);
		usuarioLog.setLogMapping("REGISTRO ATIVADO");
		UsuarioServiceLog.getInstancia().inserir(sessao, usuarioLog);
		
		vo.setFlgAtivo("S");
		vo.setIdUsuarioAlt(util.getUsuarioLogado().getIdUsuario());
		vo.setDataAlteracao(new Date());
		
		super.alterar(sessao, vo);
		
		return vo;
	}
	
	public Usuario inativar(Usuario vo) throws Exception
	{
		Session sessao = HibernateUtil.getSession();
		sessao.setFlushMode(FlushMode.COMMIT);
		Transaction tx = sessao.beginTransaction();
		try
		{
			vo = inativar(sessao, vo);
			tx.commit();
			return vo;
		}
		catch (Exception e)
		{
			tx.rollback();
			throw e;
		}
		finally
		{
			sessao.close();
		}
	}

	public Usuario inativar(Session sessao, Usuario vo) throws Exception
	{
		Usuario usuario = new Usuario();
		usuario.setIdUsuario(vo.getIdUsuario());
		usuario = this.get(sessao, usuario, 0);
		
		UsuarioLog usuarioLog = new UsuarioLog();
		JSFUtil.copiarPropriedades(usuario, usuarioLog);
		usuarioLog.setLogMapping("REGISTRO INATIVADO");
		UsuarioServiceLog.getInstancia().inserir(sessao, usuarioLog);
		
		vo.setFlgAtivo("N");
		vo.setIdUsuarioAlt(util.getUsuarioLogado().getIdUsuario());
		vo.setDataAlteracao(new Date());
		
		super.alterar(sessao, vo);
		
		return vo;
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
	protected Criteria montaCriteria(Session sessao, int join)
	{
		Criteria criteria = sessao.createCriteria(Usuario.class);
		
		if ((join & JOIN_GRUPO) != 0)
	    {
			criteria.createAlias("grupo", "grupo");
			
			if ((join & JOIN_GRUPO_TELA_ACAO) != 0)
		    {
		         criteria.createAlias("grupo.listaGrupoTelaAcao", "listaGrupoTelaAcao");
		         
		         if ((join & JOIN_TELA_ACAO) != 0)
				 {
		        	 criteria.createAlias("listaGrupoTelaAcao.telaAcao", "telaAcao");
		        	 
		        	 if ((join & JOIN_ACAO) != 0)
					 {
			        	 criteria.createAlias("telaAcao.acao", "acao");
					 }
				 }
		    }
	    }
		
		return criteria;
	}
	
	@Override
	protected void setarOrdenacao(Criteria criteria, Usuario vo, int join)
	{
		criteria.addOrder(Order.asc("nome"));
	}
}
