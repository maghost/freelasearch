package org.freelasearch.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.freelasearch.converters.AnuncioConverter;
import org.freelasearch.dao.AnuncioDao;
import org.freelasearch.dtos.DtoAnuncio;
import org.freelasearch.entities.Anuncio;
import org.freelasearch.filters.FiltroAnuncio;
import org.freelasearch.utils.DaoFactory;

public class ServicoAnuncio {

	private AnuncioDao anuncioDao;

	public ServicoAnuncio() {
		anuncioDao = DaoFactory.anuncioInstance();
	}

	public DtoAnuncio montarDtoById(Integer id) {
		Anuncio domain = anuncioDao.findById(id);
		DtoAnuncio dto = AnuncioConverter.domainToDto(domain);

		return dto;
	}

	public List<DtoAnuncio> buscarLista(FiltroAnuncio filtro) {
		List<Anuncio> anuncios = anuncioDao.findByFiltro(filtro);
		List<DtoAnuncio> dtoAnuncios = new ArrayList<DtoAnuncio>();

		for (Anuncio anuncio : anuncios) {
			dtoAnuncios.add(AnuncioConverter.domainToDto(anuncio));
		}

		return dtoAnuncios;
	}

	public void salvar(DtoAnuncio dto) {
		Anuncio anuncio = AnuncioConverter.dtoToDomain(dto);

		if (anuncio.getId() == null) {
			anuncio.setData(new Date());
			anuncioDao.save(anuncio);
		} else {
			anuncioDao.update(anuncio);
		}

	}
}
