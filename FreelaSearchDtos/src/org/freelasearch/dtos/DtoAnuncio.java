package org.freelasearch.dtos;

import java.io.Serializable;
import java.util.Date;

public class DtoAnuncio implements Serializable {

	private static final long serialVersionUID = -1832092211830965147L;

	private Integer id;
	private String titulo;
	private String descricao;
	private Date data;
	private DtoLocalizacao localizacao;
	private DtoAnunciante anunciante;
	private DtoCategoria categoria;
	private Integer status;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public DtoLocalizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(DtoLocalizacao localizacao) {
		this.localizacao = localizacao;
	}

	public DtoAnunciante getAnunciante() {
		return anunciante;
	}

	public void setAnunciante(DtoAnunciante anunciante) {
		this.anunciante = anunciante;
	}

	public DtoCategoria getCategoria() {
		return categoria;
	}

	public void setCategoria(DtoCategoria categoria) {
		this.categoria = categoria;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}
