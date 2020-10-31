package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.depot.DepotWork;
import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WorkService extends AbstractMongoSupport<DepotWork, WorkSchema> {

    @Autowired
    private WorkRepository workRepository;


    public DepotWork findById(String id) {
        return workRepository.findById(id).map(DepotWork::new).orElse(new DepotWork());
    }

    public List<DepotWork> findById(List<String> ids) {
        return iterableToList(workRepository.findAllById(ids));
    }

    public List<DepotWork> findByAuthorId(String authorId) {
        return workRepository.findByAuthors(List.of(authorId)).stream().map(DepotWork::new).collect(Collectors.toList());
    }

    @Override
    protected Function<WorkSchema, DepotWork> mapper() {
        return DepotWork::new;
    }

    @Override
    protected Class<WorkSchema> getSchemaClass() {
        return WorkSchema.class;
    }
}
