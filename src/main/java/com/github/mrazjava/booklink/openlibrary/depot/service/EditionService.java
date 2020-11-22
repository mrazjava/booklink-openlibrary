package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.depot.DepotEdition;
import com.github.mrazjava.booklink.openlibrary.repository.EditionRepository;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
public class EditionService extends AbstractDepotService<DepotEdition, EditionSchema> {

    @Autowired
    private EditionRepository editionRepository;


    @Override
    protected Function<EditionSchema, DepotEdition> schemaToDepot() {
        return DepotEdition::new;
    }

    @Override
    protected DepotEdition depotFallback() {
        return new DepotEdition();
    }

    @Override
    protected Class<EditionSchema> getSchemaClass() {
        return EditionSchema.class;
    }

    @Override
    protected String getCollectionName() {
        return EditionRepository.COLLECTION_NAME;
    }

    public List<EditionSchema> findByWorkId(String workId) {
        return editionRepository.findByWorks(List.of(workId));
    }
}
