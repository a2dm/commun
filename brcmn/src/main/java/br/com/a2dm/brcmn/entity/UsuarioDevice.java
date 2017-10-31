package br.com.a2dm.brcmn.entity;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;

/**
 * @author Carlos Diego
 * @since 06/12/2016
 */

@Entity
@Table(name = "tb_usuario_device", schema = "ped")
@SequenceGenerator(name = "SQ_USUARIO_DEVICE", sequenceName = "SQ_USUARIO_DEVICE", allocationSize = 1)
@Proxy(lazy = true)
public class UsuarioDevice implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_USUARIO_DEVICE")
	@Column(name = "id_usuario_device")
	private BigInteger idUsuarioDevice;

	@Column(name = "id_usuario")
	private BigInteger idUsuario;

	@Column(name = "id_device")
	private String idDevice;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", insertable = false, updatable = false)
	private Usuario usuario;

	public BigInteger getIdUsuarioDevice() {
		return idUsuarioDevice;
	}

	public void setIdUsuarioDevice(BigInteger idUsuarioDevice) {
		this.idUsuarioDevice = idUsuarioDevice;
	}

	public BigInteger getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(BigInteger idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getIdDevice() {
		return idDevice;
	}

	public void setIdDevice(String idDevice) {
		this.idDevice = idDevice;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
}