package org.freelasearch.service;

import org.freelasearch.dtos.DtoAnunciante;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AnuncianteService extends AbstractService<DtoAnunciante> {

    public DtoAnunciante save(DtoAnunciante dto) throws IOException {
        return (DtoAnunciante) sendObject(dto, "anunciante/salvar");
    }

    public List<DtoAnunciante> findByFiltro(Map<String, String> params) throws IOException {
        return retrieveListObject(params, "anunciante/buscar");
    }

}
