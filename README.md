# Booklink Data Integration: openlibrary.org
Import process for migrating raw data dumps from [openlibrary.org](https://openlibrary.org). Depot implementation 
(REST API) for interaction with imported data source to facilitate author-book feed (search, etc) into the 
[booklink-backend](https://github.com/mrazjava/booklink-backend).

# Overview
openlibrary.org is a fantastic public archive which provides a rich aggregation of book info from all over the world. 
Practically speaking, it is the most complete - public and free - archive of books and authors available on the 
Internet. As of September 2020, they release updated dumps on the monthly basis. Dumps are provided in a specific 
JSON-ish format.

Even though openlibrary exposes a [rich interface API](https://github.com/internetarchive/openlibrary) for their dumps, 
I could not find a stable schema for processing these dumps easily with java, let alone a full fledged import program, 
not even mentioning one that would meet the requirements of the booklink application. There was no other way around 
this than writing one from scratch.

This project builds into two executables:

* `com.github.mrazjava.booklink.openlibrary.OpenLibraryImportApp`: import openlibrary dumps into mongo
* `com.github.mrazjava.booklink.openlibrary.OpenLibraryDepotApp`: expose imported mongo data as REST API

These could be two separate projects/apps with a shared schema *.jar (3rd project), but to keep things simple they 
are both part of the same project. Should there be another integration in the future, it only has to follow the same 
structure; that is, 1) provide import into some persistent store with its own api, and, 2) provide depot implementation. 

## Tech Stack
* [Java 11](https://openjdk.java.net/projects/jdk/11/)
* [Spring Boot](https://spring.io/projects/spring-boot), [Springfox](https://github.com/springfox/springfox/releases/tag/3.0.0)
* [Jackson](https://github.com/FasterXML/jackson-docs)
* [Apache Commons](https://commons.apache.org/): LANG3, COLLECTIONS4, IO, COMPRESS
* [Lombok](https://projectlombok.org/)
* [MongoDB](https://www.mongodb.com/)

## Version
POM version follows the format `YYYYMMvX` which is date based. `v` indicates version by `X` which is the sequence number 
starting with `1` and resetting each month.

## Architecture
The goal of booklink data integration is to provide author/book data to the backend over the unified API. This is 
accomplished by building this project into two modules:

1) `IMPORT`: transform raw dumps from openlibrary.org into mongo records which can be served over REST API by the depot
2) `DEPOT SERVER`: implements a book querying mechanism over the uniform REST API
> As long as there is a single depot implementation (OpenLibrary), server definitions are hand crafted. If in the 
> future another data integration is introduced (eg: google books), server API definitions will have to be either 
> extracted to another library project with something like delegate definitions; or, auto generated with something like 
> swagger codegen.

The consumer of `DEPOT SERVER` is [booklink-backend](https://github.com/mrazjava/booklink-backend). On the backend side, 
depot client is auto generated from [depot definitions](https://github.com/mrazjava/booklink-backend/blob/develop/src/main/resources/depot-api.json). 
> Any time depot server API is changed, the `depot-api.json` on the backend must be updated and `servers` section 
> removed. JSON can be obtained by starting depot server and clicking the `apidocs` link from within the swagger UI. 
> By default it is minified, so I usually run it through a prettyfier. No futher changes should be necessary.

## Quick Start
See `application.yml` for available configuration options. Each spring boot config is driven by environment variable.

#### Dependencies
Persistence store is provided via docker:
```
docker-compose up
```

#### Data Import
```
mvn clean spring-boot:run
```
By default, import runs off a sample file. Apply `-Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_*..."` configuration 
overrides to customize behavior. See [examples](https://github.com/mrazjava/booklink-openlibrary/blob/master/EXAMPLES.md) 
for details.

Once import is finished, application shuts down the main process and exists.

#### REST API
```
mvn clean spring-boot:run -Pdepot
```
Depot runs over REST and provides Swagger interface which will be available at `localhost:8080/swagger-ui/`.

#### Sandbox Dataset
The following commands create sample dataset (using authors defined in `src/test/resources/author-id-incl-filter.txt`) used 
by the sandbox. Assuming that openlibrary dumps have been prepped, and that the dumps and cover images exist in 
`/tmp/openlibrary/`:
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/tmp/openlibrary/authors.json -DBOOKLINK_DI_IMG_DIR=/tmp/openlibrary/authors -DBOOKLINK_DI_PERSIST=true -DBOOKLINK_DI_PERSIST_OVERRIDE=true -DBOOKLINK_DI_IMAGE_PULL=true -DBOOKLINK_DI_WITH_MONGO_IMGS=true"
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/tmp/openlibrary/works.json -DBOOKLINK_DI_PERSIST=true -DBOOKLINK_DI_PERSIST_OVERRIDE=true"
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_DI_DUMP_FILE=/tmp/openlibrary/editions.json -DBOOKLINK_DI_IMG_DIR=/tmp/openlibrary/covers/ -DBOOKLINK_DI_PERSIST=true -DBOOKLINK_DI_PERSIST_OVERRIDE=true -DBOOKLINK_DI_IMAGE_PULL=true -DBOOKLINK_DI_WITH_MONGO_IMGS=true"
```

## Docker Images
MongoDB dependency is assembled for convenience via docker-compose. Import and Depot are packaged into docker 
images of which only Depot (REST API) is ran via sandbox. Import can be ran via docker image, but often it's more 
practical to just run it via maven.
#### MongoDB image
The sample dataset (using authors defined in `src/test/resources/author-id-incl-filter.txt`) is freely available as a docker 
image on [dockerhub](https://hub.docker.com/repository/docker/mrazjava/booklink-mongo). This database contains embedded 
images of authors and editions (if available) as per openlibrary specification. This same image is used by the sandbox. 
Run it with:
```
docker run -p 27017:27017/tcp mrazjava/booklink-mongo:202008-4.4.0
```
#### Depot image
A new image should be rebuilt to corelate with MongoDB image.
```
docker build -f depot.Dockerfile -t mrazjava/booklink-openlibrary-depot:YYYYMM .
```
Where `YYYYMM` is the month of release and the `vX` is the version sequence for that month's release which is handy 
in case there are multiple release in a month (REST improvements, etc).
 
To run a depot image, make sure mongo is available (via `docker-compose up`):
```
docker run -p 8070:8080 --network=booklinkopenlibrary_default --env-file=depot.env mrazjava/booklink-openlibrary-depot:YYYYMM.vX 
```
Depot will be available at http://localhost:8070/swagger-ui

#### Import image
A new image should be rebuilt on monthly basis as openlibrary data dumps are released and schema is updated. The 
version tag follows a format `YYYYMM`, here for sake of the example we use a version from Aug 2020:
```
docker build -f import.Dockerfile -t mrazjava/booklink-openlibrary-import:YYYYMM .
```
Assuming mongo was started with docker-compose we run the image by attaching to the same default network. We bind a 
sample file to the container path for test import. Note that we connect to internal container port (`27017`), not 
exposed host port (`27117`) - see `import.env` for details:
```
docker run --network=booklinkopenlibrary_default -v ~/idea/projects/booklink-openlibrary/src/main/resources/openlibrary/samples:/opt/app/samples --env-file=import.env mrazjava/booklink-openlibrary-import:YYYYMM
```

## Filters
All filters allow comments. A comment starts with a `#` and is ignored. Empty lines are also allowed and ignored. Each 
filtered item should appear on its own line. Typically a filtered item is some form of an ID. All filters are optional 
and should live in the working directory (same location as dump file).
#### `author-id-incl-filter.txt`
If enabled, then only authors listed in that file will be handled (persisted, etc). All other authors from the dump 
file will be still parsed, but will be ignored. The format of this file is one author ID (eg: `OL1179289A`) per line.
#### `author-img-exclusions.txt`
If enabled, then images present in this filter file will be ignored during the download process of author images. Some 
images, even though listed as an author option in the dump file, are missing from openlibrary archive and return a 
`404`. I've seen others regularly return a `500`. Normally, the import process will abort when it encounters unexpected 
error such as this and we do want to fail fast to know which image caused the problem, especially since the process is 
designed to be easily resumable (see the `BOOKLINK_DI_START_WITH_RECORD` config option). The format is one file name 
without extension per line. Example: `3401366-L`.

## Raw Data Sources
[Data](https://openlibrary.org/data/) [dumps](https://archive.org/details/ol_exports?sort=-publicdate) come from [openlibrary](https://openlibrary.org/developers/dumps). 
These are large downloads (authors ~320mb, works ~1.7gb, editions 6.1gb) and big files once uncompressed (authors ~2.5gb, works ~10.6gb, editions ~29gb); 
sizes as of Aug 2020. Row counts (Aug 2020 dumps): authors ~7.4M, works ~19.2M, editions ~27.1M.
> !!! Avoid working these files in a text editor.

Latest dumps are always available at `https://archive.org/details/ol_dump_YYYY-MM-DD` where the date is typically last 
day of a month. Openlibrary conveniently links latest files for easy reference:

```
wget https://openlibrary.org/data/ol_dump_authors_latest.txt.gz
wget https://openlibrary.org/data/ol_dump_works_latest.txt.gz
wget https://openlibrary.org/data/ol_dump_editions_latest.txt.gz
```
Since these files are so large, it's helpful to unzip them with `pv` to see the progress of what is otherwise a lengthy 
operation:
```
pv ol_dump_authors_latest.txt.gz | gunzip > authors.txt
pv ol_dump_works_latest.txt.gz | gunzip > works.txt
pv ol_dump_editions_latest.txt.gz | gunzip > editions.txt
```

## Graphics
Openlibrary.org provides pictures of authors and books for downloads. Usually, each image available through open 
library comes in three sizes: (S)mall, (M)edium and (L)arge. Openlibrary also provides images in the original size but 
I noticed this set is incomplete, meaning, there are many small/medium/large images which do not have the original. 
Besides, original images are too large for a typical practical usage in a web application - so it's not a big loss.

Not all images can be downloaded in bulk. For instance, author images can only be downloaded directly on case by case 
basis.

Book covers are available as a bulk download.

#### Covers
Book records contain references to covers which are by far the largest download. Covers are available in three sizes, 
small, medium and large. Details explained on [this](https://openlibrary.org/dev/docs/api/covers) openlibrary page 
(towards the bottom). 

#### Authors
In the same [cover link](https://openlibrary.org/dev/docs/api/covers), there is a section about "Author Photos" 
followed by "Rate Limiting" section which describes limits enforced: maximum 100 requests per IP per 5 min (as of 
Aug 2020). The booklink importer throttles author image download to ensure that no more than 100 images are downloaded 
in a 5 minute period.

Author images can be downloaded by primary key or by explicit photo ID (see openlibary.org for details). A download 
by primary key is convenient but it's often just a copy of one of the photo IDs, may possibly be an alias to one of 
the IDs. More reliable way is to download directly off photo ID. Not every author has a collection of photo IDs. 
Booklink importer will download author photo off a first ID in a photo collection only.

Author images must be download individually on case-by-case basis, which makes obtaining them much more difficult. This 
import process does support author image download though. I ran it over several nights (about a week in total) with 
stop-resume approach quite successfully. As of September 2020, downloading 1 image per author (some authors have more 
than one image available) resulted in about 150k images for ~7.5 million authors which adjusting for size (S/M/L) gives 
us one image every 150th author on average; or, only about 0.67% of authors (usually most popular ones) have a 
downloadable image.

## Dump Processing
Once uncompressed, data dumps must be prepared before parsing as they are not in JSON ready import format. They are JSON exports, but they contain additional metadata which must be stripped.

Author sample:
```
/type/author	/authors/OL1179289A	2	2008-08-19T08:41:22.731275	{"name": "Fritz Zimmermann", "personal_name": "Fritz Zimmermann", "last_modified": {"type": "/type/datetime", "value": "2008-08-19T08:41:22.731275"}, "key": "/authors/OL1179289A", "type": {"key": "/type/author"}, "revision": 2}
/type/author	/authors/OL21003A	4	2017-03-31T08:51:05.317330	{"bio": {"type": "/type/text", "value": "Joyce Arthur Cary was an Irish novelist and artist born in Derry, Ireland. His family had been landlords in Donegal since Elizabethan times, but lost their property after passage of the Irish Land Act in 1882. Cary's grandfather died soon after and his grandmother moved into a cottage near Cary Castle, one of the lost family properties.\r\n\r\nThe family dispersed and Cary had uncles who served in the frontier US Cavalry and the Canadian North West Mounted Police. Most of the Carys wound up in England. Arthur Cary trained as an engineer and married Charlotte Joyce, the well-to-do daughter of a Belfast banker. After his son was born in 1888, Arthur moved his family to London.\r\n\r\nThroughout his childhood, Joyce Cary spent many summers at his grandmother's house in Ireland and at Cromwell House in England, home of his great-uncle, which served as a base for all the Cary clan. Some of this upbringing is described in the fictionalized memoir A House of Children (1941) and the novel Castle Corner (1938), i.e., Cary Castle. Although he always remembered his Irish childhood with affection and wrote about it with great feeling, Cary was based in England the rest of his life. The feeling of displacement and the idea that life's tranquility may be disturbed at any moment marked Cary and informs much of his writing.\r\n\r\nCary's health was poor as a child. He was subject to asthma, which recurred throughout his life, and was nearly blind in one eye, which caused him to wear a monocle when he was in his twenties. Cary was educated at Clifton College in Bristol, England, where he was a member of Dakyns House. His mother died during this period, leaving Cary a small legacy which served as his financial base until the 1930s.\r\n\r\nIn 1906, determined to be an artist, Cary travelled to Paris. Discovering that he needed more technical training, Cary then studied art in Edinburgh. Soon enough, he determined that he could never be more than a third rate painter and decided to apply himself to literature. Cary published a volume of poems which, by his own later account, was pretty bad, and then entered Trinity College, Oxford. There he became friends with fellow-student John Middleton Murry and introduced Murry to Paris on a holiday together. Cary neglected his studies and left Oxford with a fourth class degree.\r\n\r\nSeeking adventure, in 1912 Cary left for Montenegro and served as a Red Cross orderly during the Balkan Wars. Cary kept and illustrated a record of his experiences there, Memoir of the Bobotes (1964), that was not published until after his death.\r\n\r\nReturning to England the next year, Cary sought a post with an Irish agricultural cooperative scheme, but the project fell through. Dissatisfied and believing that he lacked the education that would provide him with a good position in Britain, Cary joined the Nigerian political service. During the First World War Cary served with a Nigerian regiment fighting in the German colony of Cameroon. The short story \"Umaru\" (1921) describes an incident from this period in which a British officer recognizes the common humanity that connects him with his African sergeant.\r\n\r\nCary was wounded at the battle of Mount Mora in 1916. He returned to England on leave and proposed marriage to Gertrude Oglivie, the sister of a friend, whom he had been courting for years. Three months later, Cary returned to service as a colonial officer, leaving a pregnant Gertrude in England. Cary held several posts in Nigeria including that of magistrate and executive officer in Borgu. Cary began his African service as a stereotypical colonial officer, determined to bring order to the natives, but by the end of his service, he had come to see the Nigerians as individuals facing difficult problems, including those created by colonial rule.\r\n\r\nBy 1920, Cary was concentrating his energies on providing clean water and roads to connect remote villages with the larger world. A second leave in England had left Gertrude pregnant with their second child. She begged Cary to retire from government service so that they could live together in England. Cary had thought this impossible for financial reasons, but in 1920, he obtained a literary agent and some of the stories he had written while in Africa were sold to The Saturday Evening Post, an American magazine, published under the name \"Thomas Joyce\". This provided Cary with enough incentive to resign from the Nigerian service and he and Gertrude found a house in Oxford on Parks Road opposite the University Parks (now with a blue plaque) for their growing family. They would have four sons.\r\n\r\nCary worked hard on developing as a writer but his brief economic success soon ended as the Post decided that his stories had become too \"literary\". Cary worked at various novels and a play, but nothing sold and the family soon had to take in tenants. Their plight worsened when the Depression wiped out the investments that provided them with income and, at one point, the family rented out their house and lived with family members. Finally, in 1932, Cary managed to publish Aissa Saved, a novel that drew on his Nigerian experience. The book was not particularly successful, but sold more than Cary's next novel, An American Visitor (1933), even though that book had some critical success. The African Witch (1936) did a little better and the Carys managed to move back into their home.\r\n\r\nAlthough none of Cary's first three novels was particularly successful critically or financially, they are progressively more ambitious and complex. Indeed, The African Witch (1936) is so rich in incident, character, and thematic possibility that it over-burdens its structure. Cary understood that he needed to find new ways to make the narrative form carry his ideas. With Mister Johnson (1939), written entirely in the present tense, Cary's work becomes generally identified with literary Modernism.\r\n\r\nGeorge Orwell, on his return from Spain, recommended Cary to the Liberal Book Club which requested Cary to put together a work outlining his ideas on freedom and liberty, a basic theme in all his novels. Released as Power in Men (1939) [not Cary's title], the publisher seriously cut the manuscript without Cary's approval and he was most unhappy with the book.\r\n\r\nNow Cary contemplated a trilogy of novels based on his Irish background. Castle Corner (1938) did not do well and Cary abandoned the idea. One last African novel, Mister Johnson (1939), followed. Although now regarded as one of Cary's best novels, it sold poorly at the time. But Charley Is My Darling (1940), about displaced young people at the start of World War II, found a wider readership, and the memoir A House of Children (1941) won the James Tait Black Memorial Prize for best novel.\r\n\r\nSource and more information: http://en.wikipedia.org/wiki/Joyce_Cary"}, "name": "Joyce Cary", "created": {"type": "/type/datetime", "value": "2008-04-01T03:28:50.625462"}, "death_date": "29 March 1957", "alternate_names": ["Arthur Joyce Lunel Cary"], "last_modified": {"type": "/type/datetime", "value": "2017-03-31T08:51:05.317330"}, "latest_revision": 4, "key": "/authors/OL21003A", "birth_date": "7 December 1888", "personal_name": "Joyce Cary", "revision": 4, "type": {"key": "/type/author"}, "remote_ids": {"viaf": "46778083", "wikidata": "Q716579"}}
```

Works sample:
```
/type/work	/works/OL10002179W	3	2010-04-28T06:54:19.472104	{"title": "H\u00e9matologie et soins infirmiers", "created": {"type": "/type/datetime", "value": "2009-12-11T01:57:38.254267"}, "covers": [3144815], "last_modified": {"type": "/type/datetime", "value": "2010-04-28T06:54:19.472104"}, "latest_revision": 3, "key": "/works/OL10002179W", "authors": [{"type": "/type/author_role", "author": {"key": "/authors/OL3967469A"}}], "type": {"key": "/type/work"}, "revision": 3}
/type/work	/works/OL10002232W	3	2010-07-22T06:00:07.555136	{"title": "L' apprentissage du vocabulaire m\u00e9dical", "created": {"type": "/type/datetime", "value": "2009-12-11T01:57:38.254267"}, "last_modified": {"type": "/type/datetime", "value": "2010-07-22T06:00:07.555136"}, "latest_revision": 3, "key": "/works/OL10002232W", "authors": [{"type": {"key": "/type/author_role"}, "author": {"key": "/authors/OL3967512A"}}], "type": {"key": "/type/work"}, "subjects": ["Fran\u00e7ais (Langue)", "M\u00e9decine", "Probl\u00e8mes et exercices", "Dictionnaires fran\u00e7ais", "Terminologie", "Suffixes et pr\u00e9fixes", "Fran\u00e7ais m\u00e9dical", "Dictionnaires"], "revision": 3}
```

Edition sample:
```
/type/edition	/books/OL10003949M	7	2019-11-11T06:22:33.558146	{"publishers": ["Stationery Office Books"], "key": "/books/OL10003949M", "title": "Finance Bill (Except Clauses 2, 5, 8, 15, 52, 64 and 91 and Schedules 1, 4, 11, and 14) (Parliamentary Debates: [1994-95)", "created": {"type": "/type/datetime", "value": "2008-04-30T09:38:13.731961"}, "isbn_13": ["9780109303950"], "physical_format": "Paperback", "isbn_10": ["0109303954"], "publish_date": "February 27, 1995", "last_modified": {"type": "/type/datetime", "value": "2019-11-11T06:22:33.558146"}, "latest_revision": 7, "oclc_numbers": ["316113477"], "works": [{"key": "/works/OL14903198W"}], "type": {"key": "/type/edition"}, "subjects": ["English law: financial law"], "revision": 7}
/type/edition	/books/OL10004188M	6	2019-11-11T06:11:51.436958	{"publishers": ["Stationery Office Books"], "physical_format": "Paperback", "key": "/books/OL10004188M", "title": "Sexual Offences (Amendment) Bill (Except Clause 1) (Parliamentary Debates: [1998-99)", "created": {"type": "/type/datetime", "value": "2008-04-30T09:38:13.731961"}, "isbn_13": ["9780109401991"], "isbn_10": ["0109401999"], "publish_date": "February 11, 1999", "last_modified": {"type": "/type/datetime", "value": "2019-11-11T06:11:51.436958"}, "authors": [{"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}, {"key": "/authors/OL1550675A"}, {"key": "/authors/OL2656742A"}], "latest_revision": 6, "oclc_numbers": ["315446543"], "works": [{"key": "/works/OL14902588W"}], "type": {"key": "/type/edition"}, "subjects": ["English criminal law: offences against the person"], "revision": 6}
```
Full samples are available in `src/main/resources/openlibrary/samples/`. To make data dumps ready for import, we must 
analyze raw dumps and convert them to a proper JSON format (an array of JSON elements) before we can run import off of 
them.

#### Line count
To get an idea how many records we deal with we can count number of lines in each dump file:
```
wc -l authors.txt
wc -l works.txt
wc -l editions.txt
```
Line count is useful for final verification once data is imported into the database.

#### Remove line metadata
In order to properly parse each line into a JSON object, we must remove a prefix provided by OpenLibrary:
```
sed 's/^[^{]*//' works.txt > works.json
```

#### Parsing, Debugging, Troubleshooting
Building a parser by trail-and-error requires sequential repetitive read. It's much easier to split the original file 
into smaller chunks, say 1 million lines each and work on each piece separately until parser is built or whatever 
problem resolved:
```
split -l 1000000 ol_dump_editions_latest.json
``` 
Above we are splitting editions dump file which is almost 27 milion lines long and over 27 GB in size. We create 
smaller chunks, 1 million lines each.

## Importing Dumps
Once data dumps are processed, run the import (see the quick start). Import can process only one file per process, and 
it's recommended to process them in order, starting with author, followed by works and editions last.

## Notes
To create short samples with specific content use `fgrep` as explained [here](https://stackoverflow.com/questions/13913014/grepping-a-huge-file-80gb-any-way-to-speed-it-up):
```
fgrep -i -A 5 -B 5 'George Orwell' authors.json
```

#### Mongo Archives
The database created with `author-id-incl-filter.txt` (with embedded binary images of authors and editions) is used by the 
sandbox environment. See [sandbox persistence](https://github.com/mrazjava/booklink/tree/master/sandbox/persistence) 
for steps to create a sample mongo docker image. 

A gzipped mongo archive of a filtered sandbox sample is about `290mb`. Exporting an archive is done from a running 
docker container:
```
docker exec CONTAINER_ID sh -c 'mongodump --username USERNAME --password PASSWORD --db DATABASE --authenticationDatabase admin --gzip --archive' > /tmp/openlibrary-mongo.archive
```
Importing an archive is typically done into another docker container (see sandbox), but the basic command would be:
```
mongorestore --username root --password pass123 --authenticationDatabase admin --nsInclude=openlibrary.* --verbose --gzip --archive=/tmp/openlibrary-mongo.archive
```

#### Mongo Queries
Count authors which have a small image:
```
db.getCollection('authors').count({"imageSmall": {$exists: true}})
```
Search authors by [text](https://docs.mongodb.com/manual/reference/operator/query/text/) (case insensitive):
```
db.getCollection('authors').find({$text:{$search: "charles"}})
```
Above finds all authors where text index matches string `charles` (typically name fields).

[Search](https://docs.mongodb.com/manual/reference/operator/query/size/) by array field of a specific size:
```
db.collection.find( { field: { $size: 2 } } )
```

## Links

[Base64 Image Converter](https://codebeautify.org/base64-to-image-converter)
Downloader can optionally store default image in all three sizes diretly as a `Binary` mongo field in the 
`AuthorSchema` encoded as Base64. This handy online tool can be used to check the bytes and see the rendered image.