# Examples
Various scenarios with full parameterized commands.

Unless `BOOKLINK_DI_START_WITH_RECORD` is defined (default `0`), every record is unmarshalled into a POJO and then 
marshalled back into a `String` as means to test the parser. 

#### Author (sample)
Scan the `src/main/resources/openlibrary/samples/authors-tail-n1000.json` sample. No image processing is done. 
Nothing is persisted.
```
mvn clean spring-boot:run
```
#### Author (built-in sample + persist)
Same as above except that the (default) sample which comes with the project is persisted to mongo:
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_PERSIST=true"
```

#### Author
Scan a full openlibrary data dump in a custom location. No image processing is done. Nothing is persisted. Each record is 
logged with the frequency of `BOOKLINK_DI_FREQUENCY_CHECK`
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/home/foo/Downloads/booklink/authors.json"
```

#### Author
Same as above except that sample author ID file is auto generated off every frequency check:
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/media/foo/booklink-500GB-e/openlibrary/authors.json -DBOOKLINK_DI_AUTHOR_ID_SAMPLE_OUTPUT_FILE=/tmp/author-id-sample.txt"
```

#### Author
Process full openlibrary data dump but load into mongo only authors defined in `src/main/resources/author-id-incl-filter.txt` (assuming file had been copied to 
the same location where the dump file is). Use author images in the provided `BOOKLINK_DI_IMG_DIR` and make them part of a 
mongo record (as a binary field, in all three sizes: small, medium, large). If the image is not available, try to 
download it from the Internet. Any author records that may have existed in mongo will be overriden.
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/home/foo/Downloads/booklink/authors.json -DBOOKLINK_DI_IMG_DIR=/media/foo/booklink-5TB/authors -DBOOKLINK_DI_PERSIST=true -DBOOKLINK_DI_PERSIST_OVERRIDE=true -DBOOKLINK_DI_IMAGE_PULL=true -DBOOKLINK_DI_WITH_MONGO_IMGS=true"
```
#### Author
Process full openlibrary data dump without persisting data, but if image of an author is available, download it to a file and 
store it in `author` directory to a connected USB drive. Note that author dump file and image output directory are 
two different locations. In addition, skip first 1 milion records (eg: they were processed on earlier run). Sample 
record is printed every 100,000th row:
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/home/foo/Downloads/booklink/authors.json -DBOOKLINK_DI_IMAGE_PULL=true -DBOOKLINK_DI_IMG_DIR=/media/foo/booklink-5TB/authors -DBOOKLINK_DI_PERSIST=false -DBOOKLINK_DI_START_WITH_RECORD=1000000"
```
Similar example, one I actually use to download author files every month when new dump is published (no mongo 
persistence):
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/media/foo/booklink-500GB-e/openlibrary/authors.json"
``` 

#### Author
Process full openlibrary data dump and persist each record in mongo. If available, download author images to a file 
and store them in `authors` subdirectory of the location where the dump file is read from. In addition, 
store a copy of an image as a binary property of author mongo record. Sample record is printed out 
every 1000th row:
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/home/foo/Downloads/booklink/authors.json -DBOOKLINK_DI_FREQUENCY_CHECK=1000 -DBOOKLINK_DI_IMG_DIR=authors -DBOOKLINK_DI_PERSIST=true -DBOOKLINK_DI_PERSIST_OVERRIDE=true -DBOOKLINK_DI_WITH_MONGO_IMGS=true"
```
#### Works
Process full openlibrary data dump (named in a non-standard way `books.json`) without persisting data or downloading any images. 
This is a simple scan through of a dump file. Useful for checking the parser when a new dump file is release by 
openlibrary.org (they release dumps on the monthly basis).
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/home/foo/Downloads/booklink/books.json -DBOOKLINK_DI_HANDLER_CLASS=WorkHandler"
```
#### Editions
Process full openlibrary data dump and persist selected records that match author filter. Also fetch images for each edition cover 
which is available in bulk tar archives (already downloaded).
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/home/foo/Downloads/booklink/editions.json -DBOOKLINK_DI_IMAGE_PULL=true -DBOOKLINK_DI_WITH_MONGO_IMGS=true -DBOOKLINK_DI_IMG_DIR=/media/foo/booklink-500GB-e/openlibrary/covers/ -DBOOKLINK_DI_PERSIST=true -DBOOKLINK_DI_PERSIST_OVERRIDE=true "
```

## Monthly Imports
These are the commands I use to process monthly imports from latest data dumps. On even months, I generate randomized 
author filter and use that to create a database. On odd months, I always create sample database off the `author-id-incl-filter.txt` 
provided in the project. On even months database feature different sets of sample authors. On odd months, databases 
feature the same set of authors. 

#### Generate Random Author Filter (even months only)
There are two ways to generate random author IDs. In both cases, this feature is enabled only, if `BOOKLINK_DI_AUTHOR_ID_SAMPLE_OUTPUT_FILE` 
is enabled.

Default randomizer samples IDs off `BOOKLINK_DI_FREQUENCY_CHECK` value. Every time record is logged according to 
frequency check, its ID is recorded as a sample: 
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/media/azimowski/booklink-500GB-e/openlibrary/authors.json -DBOOKLINK_DI_AUTHOR_ID_SAMPLE_OUTPUT_FILE=author-id-incl-filter.txt"
```

Explicit randomizer creates a random set of indexes over the domain of entire dump record count. This feature requires 
additional configuration `BOOKLINK_DI_AUTHOR_ID_SAMPLE_RANDOMIZE`. See `application.yml` for additional documentation.
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/media/azimowski/booklink-500GB-e/openlibrary/authors.json -DBOOKLINK_DI_AUTHOR_ID_SAMPLE_OUTPUT_FILE=/tmp/author-id-incl-filter.txt -DBOOKLINK_DI_AUTHOR_ID_SAMPLE_RANDOMIZE=75|7972562"
```

#### Authors
NOTE: if running mongo via docker-compose from project dir, then we must match its port (last var)
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/media/azimowski/booklink-500GB-e/openlibrary/authors.json -DBOOKLINK_DI_IMG_DIR=/media/azimowski/booklink-500GB-e/openlibrary/authors -DBOOKLINK_DI_PERSIST=true -DBOOKLINK_DI_PERSIST_OVERRIDE=true -DBOOKLINK_DI_IMAGE_PULL=true -DBOOKLINK_DI_WITH_MONGO_IMGS=true -DBOOKLINK_MONGO_PORT=27117"
```

#### Works
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/media/azimowski/booklink-500GB-e/openlibrary/works.json -DBOOKLINK_DI_PERSIST=true -DBOOKLINK_DI_PERSIST_OVERRIDE=true -DBOOKLINK_DI_IMAGE_PULL=true -DBOOKLINK_DI_WITH_MONGO_IMGS=true -DBOOKLINK_DI_IMG_DIR=/media/azimowski/booklink-500GB-e/openlibrary/covers/"
```

#### Editions
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/media/azimowski/booklink-500GB-e/openlibrary/editions.json -DBOOKLINK_DI_IMAGE_PULL=true -DBOOKLINK_DI_WITH_MONGO_IMGS=true -DBOOKLINK_DI_IMG_DIR=/media/azimowski/booklink-500GB-e/openlibrary/covers/ -DBOOKLINK_DI_PERSIST=true -DBOOKLINK_DI_PERSIST_OVERRIDE=true"
```

#### Notes
`/media/azimowski/booklink-500GB-e/openlibrary/covers/` is the location of bulk cover files from openlibrary.org. The 
directory structure inside follows the format `SIZE_covers_000X` where `SIZE` is either `S`, `M` or `L` and `X` in the 
range between `0` and `6`. Inside each directory are tar files as downloaded via [torrent](https://archive.org/details/covers_0000).