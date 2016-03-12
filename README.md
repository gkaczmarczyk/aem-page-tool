# AEM Page Tool

A small utility to ease in updating a single or many properties for any number of pages in AEM. This will update properties for the page(s) in the `jcr:content` node of the page itself.

###Usage

Typical usage will include specifying the top-level node under which all pages that are expected to be updated fall or are descendents.
Additionally, a single or set of properties are specified which are expected to be added or updated.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1 -p prop2=val2
```

You can also save multi-value properties. To do so, surround your property value with square brackets & comma separate the values if there are more than one. _(Currently only `String` multi-values are supported)_

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=[val1] -p prop2=[val2,val3]
```

if you wish to create a property in a node nested within the `jcr:content` node, you need to specify the entire path past the `jcr:content` node. For example, to create/update a property, `prop1`, in the path `/content/path/to/my/page/jcr:content/par/subnode` you would use the following command:

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p par/subnode/prop1=val1
```

Rather than updating properties, you can also delete properties that the page may contain.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -d undesiredProp
```

You can specify both `-p` & `-d` arguments. If you specify `-d` & `-p` with the same property name, the update will follow the rules of the SlingPostServlet. _The property is first deleted and then filled with the new content._


An optional argument to include is a property (or properties) which the page to be updated must contain if that page is to be updated.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1 -m hasProp=currVal
```

So, only a page(s) that has the property `hasProp` with the value `currVal` will get updated.

#####Copying Nodes

When copying nodes, you need to specify the source (or the node that should be copied) and the target (or the name of the node to which the source should be copied). The source is specified with `-i` (or `--copy-from`) & the target is specified with `-o` (or `--copy-to`).

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -i node1 -o node2
```

If you wish to copy a node nested within the `jcr:content` node, you would use the following command:

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -i par/node1 -o par/node2
```

_Note: At this time, only the first node specified will be copied regardless of how many nodes you specify at the command line._ 

#####Copying Properties

Copying properties is done similarly to copying nodes with the additional parameter `-P` (or `--property`). So, having a property, `prop1`, in the `jcr:content` node of the given page, you can copy the value of it to the newly specified property, `prop2` with the following command: 

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -i prop1 -o prop2 -P
```

More deeply nested properties can be copied using the same technique as specified previously:

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -i par/subpar/prop1 -o par/subpar/prop2 -P
```

#####Simple Searching

If just interested in finding which nodes contain specific properties with their values or finding if a node exists within a tree by a specific name, then you can perform a search. The option to search is `-f` and can be used as:

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -f property=value
```

-or-

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -f mynode
```

_Note: just as with sling queries, you can add a wildcard in the node name, i.e. `-f mynode_*`_

#####Searching for Non cq:Page nodes

By default, matches are performed on pages with a JCR primary type of "cq:Page". If you wish to leave the type unspecified, you can use the `-N` flag:

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -N -f mynode
```

#####Passing credentials

Credentials can be specified in any manner depending on your needs with any of the following options:

- Specifying username, password, server, & port: `-c userName:passwd@hostname:port`
- Specifying username & password: `-l userName:passwd`
- Specifying hostname & port: `-s hostname:port`
- Specifying _only_ username: `-u userName`
- Specifying _only_ password: `-w passwd`
- Specifying _only_ hostname: `-h hostname`
- Specifying _only_ port: `-t portNum`

#####Other Options

A dry run will allow you to see which pages will get updated prior to making any real changes. It is enabled with `-y`.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1 -y
```

If you would prefer more verbose output to see what's happening as it's happening, use `-x`.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1 -x
```
