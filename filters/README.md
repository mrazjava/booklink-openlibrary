# Import Filters
Filter definitions used for openlibrary import.

## Monto Releases
Odd month mongo releases are unique because they are generated off authors selected randomly. Even month mongo 
releases are based on a fixed [filter](author-id-incl-filter.txt), which may be periodically updated (see git history). 

## Mongo Image Versions
Openlibrary.org releases its dumps on the last day of the month. Booklink mongo image is released the following 
month for the data dump of a previous month (eg: data dumps from Nov 30 would be part of December mongo image).