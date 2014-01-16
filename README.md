# `sparqlcc`: an implementation of the CITE Collections service #

Work in progress.

Requires a SPARQL endpoint.  References to its address have not been cleaned up yet in source code:  ultimately will be universally set from the `SPARQL` property in `gradle.properties`.  For now, if you're not running on `localhost:3030` you may get in trouble.  Sorry.

## Dependencies ##

`prestochango` lib 

## Running tests ##

Load `testdata/test1.ttl` into your triple store.  Tests expect to find the endpoint at fuseki's default localhost:3030 location.

## Expected RDF ##

### Basic identification ###

Collections and objects are each identified by CITE URNs.

Each object should be explicitly related to its collection with a statement like this:

    <urn:cite:hmt:msA.327v> hmt:belongsTo <urn:cite:hmt:msA> .

### Collections schema information
Translating a collections schema in to RDF:

the following RDF namespaces should be defined:

    @prefix hmt:        <http://www.homermultitext.org/hmt/rdfverbs/> .
    @prefix citedata:        <http://www.homermultitext.org/hmt/citedata/> .
    @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. 
    @prefix  xsd: <http://www.w3.org/2001/XMLSchema#> .

Each CITE Collection namespace should be documented with two lines of TTL that look like this:

    <http://www.homermultitext.org/datans> rdf:type hmt:DataNs .
    <http://www.homermultitext.org/datans> hmt:abbreviatedBy "hmt" .

Each CITE Collection should be documented like this: 

    <urn:cite:hmt:codices> rdf:type hmt:CiteCollection .

Each property of a collection should be documented with four lines like this:

    <urn:cite:hmt:codices> hmt:collProperty citedata:codices_description .
    citedata:codices_description rdf:type rdf:Property .
    citedata:codices_description hmt:propLabel "description" .
    citedata:codices_description hmt:propType hmt:String .


### Ordered collections

In addition to the above, ordered collections should have three ordering statements for each object.

    <urn:cite:hmt:msA.12r> hmt:seq 23 .
    <urn:cite:hmt:msA.12r> hmt:prev 22 .
    <urn:cite:hmt:msA.12r> hmt:next 24 .
