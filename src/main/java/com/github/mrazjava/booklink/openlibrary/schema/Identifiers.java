package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Identifiers {

    @JsonProperty("barcode")
    private List<String> barCode;

    private List<String> sudoc;

    private List<String> edition;

    @JsonProperty("limited_edition_book_number")
    private List<String> limitedEditionBookNumber;

    private List<String> oclc;

    private List<String> bca;

    private List<String> glossary;

    @JsonProperty("goodreads")
    private List<String> goodReads;

    @JsonProperty("e-books")
    private List<String> eBooks;

    private List<String> asin;

    @JsonProperty("cihm_number")
    private List<String> cihmNumber;

    @JsonProperty("gpn_(taiwan)")
    private List<String> gpnTaiwan;

    @JsonProperty("victoria_institutions")
    private List<String> victoriaInstitutions;

    @JsonProperty("ebook_family_friendly_store")
    private List<String> eBookFamilyFriendlyStore;

    @JsonProperty("num._national_de_thèse")
    private List<String> numNationalDeThese;

    @JsonProperty("mathematic_subject_classification")
    private List<String> mathematicSubjectClassification;

    @JsonProperty("librarything")
    private List<String> libraryThing;

    @JsonProperty("system_number_of_the_research_library_olomouc")
    private List<String> researchLibraryOlomouc;

    @JsonAlias({
            "library_of_congress",
            "lccn_permalink:",
            "lc_classification",
            "library_of_congress_classification_(lcc)",
            "library_of_congress_catalog_no.",
            "library_of_congress_catalog_card_no.",
            "library_of_congress_catalogue_number"})
    private List<String> lccn;

    @JsonProperty("l.n.")
    private List<String> ln;

    private List<String> wikidata;

    @JsonProperty("call_number")
    private List<String> callNumber;

    @JsonProperty("media_mint_publishing")
    private List<String> mediaMintPublishing;

    @JsonProperty("judaica_savings")
    private List<String> judaicaSavings;

    @JsonProperty("sadguru_sanga_diary")
    private List<String> sadguruSangaDiary;

    private List<String> freebase;

    @JsonProperty("booksonclick")
    private List<String> bookSonClick;

    @JsonProperty("learnawesome")
    private List<String> learnAwesome;

    private List<String> alecso;

    @JsonProperty("istc") // https://data.cerl.org/
    private List<String> incunabulaShortTitleCatalogue;

    @JsonAlias("dewey_decimal_classification_(ddc)")
    @JsonProperty("universal_decimal_classification")
    private List<String> deweyDecimalClass;

    @JsonProperty("create_space")
    private List<String> createSpace;

    @JsonAlias("smashwords.com")
    @JsonProperty("smashwords_book_download")
    private List<String> smashwordsBookDownload;

    @JsonProperty("imslp/petrucci_music_library")
    private List<String> petrucciMusicLibrary;

    @JsonProperty("national_diet_library,_japan")
    private List<String> nationalDietLibraryJapan;

    @JsonProperty("british_national_bibliography")
    private List<String> britishNationalBibliography;

    @JsonAlias({"estc", "etsc"}) // english short title catalogue http://estc.bl.uk/
    @JsonProperty("british_library")
    private List<String> britishLibrary;

    @JsonProperty("national_library_of_australia")
    private List<String> nationalLibraryOfAustralia;

    @JsonProperty("dnb_(german_national_library)")
    private List<String> germanNationalLibrary;

    @JsonProperty("bayerische_staatsbibliothek")
    private List<String> bayerischeBibliothek;

    @JsonProperty("deutsche_nationalbibliothek_(urn:nbn)")
    private List<String> deutscheNationalBibliothek;

    @JsonProperty("staats-_und_universitätsbibliothek_hamburg")
    private List<String> universitatBibliothekHamburg;

    @JsonProperty("universitätsbibliothek_heidelberg")
    private List<String> universitatBibliothekHeidelberg;

    @JsonProperty("número_de_incripción_de_la_obra")
    private List<String> numeroDeIncripcion;

    @JsonProperty("numéro_de_dépôt_légal")
    private List<String> numeroDeDepotLegal;

    @JsonProperty("university_library_of_the_ludwig-maximilian_university_of_munich")
    private List<String> munichUniversityLibrary;

    @JsonAlias("bibliothèque_nationale_de_france_(bnf)")
    @JsonProperty("bibliothèque_nationale_de_france")
    private List<String> bibliothequeNationaleDeFrance;

    @Field("deposito_legal")
    @JsonAlias("depósito_legal")
    @JsonProperty("depósito_legal_bolivia")
    private List<String> depositoLegalBolivia;

    @JsonProperty("el_número_de_inscripción_de_la_obra")
    private List<String> elNumeroDeInscripcionDeLaObra;

    @JsonProperty("national_library_of_egypt")
    private List<String> nationalLibraryOfEgypt;

    @JsonProperty("national_book_chamber_of_moldova")
    private List<String> nationalBookChamberOfMoldova;

    @JsonProperty("bibliotheca_alexandrina")
    private List<String> bibliothecaAlexandrina;

    @JsonAlias("cornell_university_online_library")
    @JsonProperty("cornell_university_library")
    private List<String> cornellUniversityLibrary;

    @JsonProperty("boston_public_library")
    private List<String> bostonPublicLibrary;

    @JsonProperty("special_library_collections")
    private List<String> specialLibraryCollections;

    @JsonProperty("abebooks,de")
    private List<String> abeBooksDe;

    // Antiquarian Booksellers' Association of America: www.abaa.org
    private List<String> abaa;

    private List<String> almedina;

    @JsonProperty("hakikat_kitabevi")
    private List<String> hakikatKitabevi;

    @JsonProperty("biodiversity_heritage_library")
    private List<String> biodiversityHeritageLibrary;

    @JsonProperty("usaid/dec")
    private List<String> usAidDec;

    @JsonProperty("bestellnummer")
    private List<String> bestellNummer;

    @JsonProperty("kindle.com")
    private List<String> kindle;

    private List<String> bookwire;

    @JsonProperty("bol.com")
    private List<String> bol;

    @JsonProperty("hizmetbooks.org")
    private List<String> hizmetBooks;

    private List<String> bibsys;

    @JsonProperty("abwa_bibliographic_number")
    private List<String> abwaBibliographicNumber;

    @JsonProperty("magcloud")
    private List<String> magCloud;

    @JsonProperty("publisher_catalog")
    private List<String> publisherCatalog;

    @JsonProperty("publishamerica")
    private List<String> publishAmerica;

    private List<String> flipkart;

    private List<String> upc;

    @JsonProperty("taideteollisen_korkeakoulun_julkaisusarja")
    private List<String> taideteollisenKorkeakolunJulkaisusarija;

    @JsonProperty("abwa_talking_book_number")
    private List<String> abwaTalkingBookNumber;

    @JsonProperty("niedersächsische_staats-_und_universitätsbibliothek_göttingen")
    private List<String> bibliothekGottingen;

    @JsonProperty("bestell-nr._(ddr)")
    private List<String> bestellNr;

    @JsonProperty("archive.org")
    private List<String> archiveOrg;

    @JsonProperty("registro_autoral")
    private List<String> registroAutoral;

    @JsonProperty("ebooks_libres_et_gratuits")
    private List<String> ebooksLibresGratuits;

    private List<String> fennica;

    @JsonProperty("bibliografia_selecionada")
    private List<String> bibliografiaSelecionada;

    @JsonProperty("canadian_national_library_archive")
    private List<String> canadianNationalLibraryArchive;

    @JsonProperty("dominican_institute_for_oriental_studies_library")
    private List<String> dominicanInstituteForOrientalStudiesLibrary;

    @JsonProperty("paperback_swap")
    private List<String> paperbackSwap;

    @JsonProperty("newberry_library_cartographic_catalog")
    private List<String> newberryLibraryCartographicCatalog;

    @JsonProperty("bookmooch")
    private List<String> bookMooch;

    @JsonProperty("booksforyou")
    private List<String> booksForYou;

    @JsonProperty("books_on_demand")
    private List<String> booksOnDemand;

    @JsonProperty("apple_ibook_store")
    private List<String> appleIbookStore;

    private List<String> lulu;

    @JsonAlias("barnes_and_noble_-_bn.com")
    @JsonProperty("barnes_&_noble")
    private List<String> barnsAndNoble;

    private List<String> amazon;

    @JsonProperty("amazon.de_asin")
    private List<String> amazonDe;

    @JsonProperty("amazon.co.uk_asin")
    private List<String> amazonUk;

    @JsonProperty("amazon.ca_asin")
    private List<String> amazonCa;

    @JsonProperty("amazon.it_asin")
    private List<String> amazonIt;

    @JsonProperty("amazon.co.jp")
    private List<String> amazonJp;

    private List<String> google;

    private List<String> dnb;

    private List<String> nla;

    private List<String> uri;

    private List<String> doi;

    private List<String> issn;

    private List<String> ilmiolibro;

    @JsonAlias("isbn_10")
    private List<String> isbn;

    @JsonAlias("harvard_university_library")
    private List<String> harvard;

    @JsonProperty("bodleian,_oxford_university")
    private List<String> oxford;

    private List<String> overdrive;

    private List<String> shelfari;

    @JsonProperty("w._w._norton")
    private List<String> wwNorton;

    @JsonProperty("booklocker.com")
    private List<String> booklocker;

    private List<String> ean;

    private List<String> bhl;

    private List<String> format;

    @JsonProperty("format:_kindle_edition")
    private List<String> kindleEditionFormat;

    @JsonProperty("choosebooks")
    private List<String> choseBooks;

    @JsonProperty("idealbooks")
    private List<String> idealBooks;

    @JsonProperty("dc_books")
    private List<String> dbBooks;

    @JsonProperty("better_world_books")
    private List<String> betterWorldBooks;

    @JsonProperty("project_gutenberg")
    private List<String> projectGutenberg;

    @JsonProperty("hathi_trust")
    private List<String> hathiTrust;

    @JsonProperty("gallica_(bnf)")
    private List<String> gallica;

    @JsonProperty("alibris_id")
    private List<String> alibrisId;

    private List<String> bcid;

    @JsonProperty("zdb-id")
    private List<String> zdbId;

    private List<String> isfdb;

    private List<String> libris;

    @JsonProperty("publish_date")
    private String publishDate;

    private List<Key> works;

    private List<String> scribd;

    private List<String> ulrls;

    @JsonProperty("link")
    private List<String> links;

    @JsonProperty("open_library")
    private List<String> openLibrary;
}
