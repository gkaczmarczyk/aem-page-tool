# AEM Page Tool

A small utility to ease in updating a single or many properties for any number of pages in AEM. This will update properties for the page(s) in the `jcr:content` node of the page itself.

###Usage

Typical usage will include specifying the top-level node under which all pages that are expected to be updated fall or are descendents.
Additionally, a single or set of properties are specified which are expected to be added or updated.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1 -p prop2=val2
```

An optional argument to include is a propery (or properties) which the page to be updated must contain if that page is to be updated.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1 -m hasProp=currVal
```

So, only a page(s) that has the property `hasProp` with the value `currVal` will get updated.

###Passing credentials

Credentials can be specified in any manner depending on your needs with any of the following options:

- Specifying username, password, server, & port: `-c userName:passwd@hostname:port`
- Specifying username & password: `-l userName:passwd`
- Specifying hostname & port: `-s hostname:port`
- Specifying _only_ username: `-u userName`
- Specifying _only_ password: `-w passwd`
- Specifying _only_ hostname: `-h hostname`
- Specifying _only_ port: `-t portNum`
