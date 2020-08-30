# Booklink Data Integration: openlibrary.org
Import process for migrating raw data dumps from [openlibrary.org](https://openlibrary.org) into [booklink-data-openlibrary](../booklink-data-openlibrary).

## Tech Stack
* Spring Boot
* [Jackson](https://github.com/FasterXML/jackson-docs)
* Apache Commons IO
* Lombok
* MongoDB

## Quick Start
See `application.yml` for available configuration options. Each spring boot config is driven by environment variable.

Dependencies (mongo) are provided via docker:
```
docker-compose up
```

Examples:

*Process authors dump without persisting data, but if image is available, download it to a file and 
store it in author_imgs subdirectory of the location where the dump file is read from. Sample record 
is printed every 50,000th row:*
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_OL_DUMP_FILE=/home/azimowski/Downloads/booklink/authors.json -DBOOKLINK_SCHEMA=AuthorSchema -DBOOKLINK_FREQUENCY_CHECK=50000 -DBOOKLINK_AUTHOR_IMG_DIR=author_imgs -DBOOKLINK_PERSIST=false"
```
*Process authors dump and persist each record in mongo. If avilable, download author images to a file 
and store them in bronco subdirectory of the location where the dump file is read from. In addition, 
store a copy of an image as a binary property of author mongo record. Sample record is printed out 
every 1000th row:*
```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-DBOOKLINK_OL_DUMP_FILE=/home/azimowski/Downloads/booklink/authors.json -DBOOKLINK_SCHEMA=AuthorSchema -DBOOKLINK_FREQUENCY_CHECK=1000 -DBOOKLINK_AUTHOR_IMG_DIR=bronco -DBOOKLINK_PERSIST=true -DBOOKLINK_PERSIST_OVERRIDE=true -DBOOKLINK_AUTHOR_IMG_MONGO=true"
```

## Features
#### Author Filtering
If a file called `author-id-filter.txt` exists in the working directory (same location as dump file), then only authors 
listed in that file will be handled (persisted, etc). The format of this file is one author ID per line. Comment is 
allowed and must start with a `#`. Empty lines are also allowed and are ignored.

## Datasources
Raw [data](https://openlibrary.org/data/) [dumps](https://archive.org/details/ol_exports?sort=-publicdate) are pulled from [openlibrary](https://openlibrary.org/developers/dumps). 
These are large downloads (authors ~320mb, works ~1.7gb, editions 6.1gb) and big files once uncompressed (authors ~2.5gb, works ~10.6gb, editions ~29gb); 
sizes as of May 2020. Row counts (Aug 2020 dumps): authors ~7.4M, works ~19.2M, editions ~27.1M.
> !!! Avoid working these files in a text editor.
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

## Images
Openlibrary.org provides images for downloads. Usually, each image available through open library comes in three 
sizes: (S)mall, (M)edium and (L)arge. Not all images can be downloaded in bulk, though.

Book covers are available for bulk download. Author images must be download individually which makes obtaining them 
much more difficult. 

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
sed 's/^[^{]*//'  ol_dump_works_latest.txt > works.txt
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
it's recommended to prcess them in order, starting with author, followed by works and editions last.

## Notes
To create short samples with specific content use `fgrep` as explained [here](https://stackoverflow.com/questions/13913014/grepping-a-huge-file-80gb-any-way-to-speed-it-up):
```
fgrep -i -A 5 -B 5 'George Orwell' ol_dump_authors_latest.txt
```
Dropping prefix types from dump files to produce JSON only:
```
sed 's/^[^{]*//' ol_dump_authors_latest.txt > authors.txt
```

## Links

[Base64 Image Converter](https://codebeautify.org/base64-to-image-converter)
Downloader can optionally store default image in all three sizes diretly as a `Binary` mongo field in the 
`AuthorSchema` encoded as Base64. This handy online tool can be used to check the bytes and see the rendered image.