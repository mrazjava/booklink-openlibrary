package com.github.mrazjava.booklink.openlibrary.depot;

import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Data
public class DepotWork {

    public DepotWork(WorkSchema schema) {

    }
}
