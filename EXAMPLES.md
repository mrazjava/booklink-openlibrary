#### Examples
Various scenarios with full parameterized commands.

Unless `BOOKLINK_DI_START_WITH_RECORD` is defined (default `0`), every record is unmarshalled into a POJO and then 
marshalled back into a `String` as means to test the parser. 

*Scan the **author** sample from `src/main/resources/openlibrary/samples/authors-tail-n1000.json`. No image 
processing is done. Nothing is persisted.*
```
mvn clean spring-boot:run
```

*Scan a full **author** dump in a custom location. No image processing is done. Nothing is persisted. Each record is 
logged with the frequency of `BOOKLINK_DI_FREQUENCY_CHECK`*
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/home/azimowski/Downloads/booklink/authors.json"
```

*Load into mongo only **authors** defined in `src/main/resources/author-id-filter.txt` (assuming file had been copied to 
the same location where the dump file is). Use author images in the provided `BOOKLINK_DI_IMG_DIR` and make them part of a 
mongo record (as a binary field, in all three sizes: small, medium, large). If the image is not available, try to 
download it from the Internet. Any author records that may have existed in mongo will be overriden.*
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/home/azimowski/Downloads/booklink/authors.json -DBOOKLINK_DI_IMG_DIR=/media/azimowski/booklink-5TB/authors -DBOOKLINK_DI_PERSIST=true -DBOOKLINK_DI_PERSIST_OVERRIDE=true -DBOOKLINK_DI_IMAGE_PULL=true -DBOOKLINK_DI_WITH_MONGO_IMGS=true"
```

*Process **authors** dump without persisting data, but if image is available, download it to a file and 
store it in `author` directory to a connected USB drive. Note that author dump file and image output directory are 
two different locations. In addition, skip first 1 milion records (eg: they were processed on earlier run). Sample 
record is printed every 100,000th row:*
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/home/azimowski/Downloads/booklink/authors.json -DBOOKLINK_DI_IMAGE_PULL=true -DBOOKLINK_DI_IMG_DIR=/media/azimowski/booklink-5TB/authors -DBOOKLINK_DI_PERSIST=false -DBOOKLINK_DI_START_WITH_RECORD=1000000"
```
*Process **authors** dump and persist each record in mongo. If avilable, download author images to a file 
and store them in `authors` subdirectory of the location where the dump file is read from. In addition, 
store a copy of an image as a binary property of author mongo record. Sample record is printed out 
every 1000th row:*
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/home/azimowski/Downloads/booklink/authors.json -DBOOKLINK_DI_FREQUENCY_CHECK=1000 -DBOOKLINK_DI_IMG_DIR=authors -DBOOKLINK_DI_PERSIST=true -DBOOKLINK_DI_PERSIST_OVERRIDE=true -DBOOKLINK_DI_WITH_MONGO_IMGS=true"
```
*Process **works** dump (named in a non-standard way `books.json`) without persisting data or downloading any images. 
This is a simple scan through of a dump file. Useful for checking the parser when a new dump file is release by 
openlibrary.org (they release dumps on the monthly basis).*
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/home/azimowski/Downloads/booklink/books.json -DBOOKLINK_DI_HANDLER_CLASS=WorkHandler"
```
*Process **editions** dump and persist selected records that match author filter. Also fetch images for each edition cover 
which is available in bulk tar archives (already downloaded).*
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/home/azimowski/Downloads/booklink/editions.json -DBOOKLINK_DI_IMAGE_PULL=true -DBOOKLINK_DI_WITH_MONGO_IMGS=true -DBOOKLINK_DI_IMG_DIR=/media/azimowski/booklink-500GB-e/covers/ -DBOOKLINK_DI_PERSIST=true"
```