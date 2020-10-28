package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.depot.DepotWork;
import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkService extends AbstractMongoSupport {

    @Autowired
    private WorkRepository workRepository;


    public DepotWork findById(String id) {
        return workRepository.findById(id).map(DepotWork::new).orElse(new DepotWork());
    }

    public List<DepotWork> findById(List<String> ids) {
        List<DepotWork> results = new LinkedList<>();
        workRepository.findAllById(ids).forEach(w -> results.add(new DepotWork(w)));
        return results;
    }

    public List<DepotWork> findByAuthorId(String authorId) {
        throw new IllegalStateException("not implemented yet");
    }

    public List<DepotWork> searchText(String search, String langIso, boolean caseSensitive) {

        return mongoTemplate.find(prepareTextQuery(search, langIso, caseSensitive), WorkSchema.class)
                .stream()
                .map(DepotWork::new)
                .collect(Collectors.toList());
    }
}
