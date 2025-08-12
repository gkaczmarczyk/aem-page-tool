# AEM Page Tool

A small utility to ease in updating a single or many properties for any number of pages in AEM. This will update properties for the page(s) in the `jcr:content` node of the page itself.

## Building

This project uses the Maven Shade plugin to create a runnable Jar that includes all dependencies.  
To build, you can simply run:

```
mvn clean package
```

This will produce the shaded JAR and place it **directly in the project’s root directory** as `aem-page-tool.jar` for immediate use.  
You do **not** need to run `mvn clean install` unless you explicitly want to install the artifact into your local Maven repository.

### Usage

* [AEM Authentication](#aem-authentication)
* [cq:Page or Not](#cqpage-or-not)
* [Searching](#searching)
* [Updating](#updating)
* [Copying](#copying)
* [Other Options](#other-options)

## AEM Authentication

##### Passing credentials

Credentials can be specified in any manner depending on your needs with any of the following options:

- Specifying username, password, server, & port: `-c userName:passwd@hostname:port`
- Specifying username & password: `-l userName:passwd`
- Specifying hostname & port: `-s hostname:port`
- Specifying _only_ username: `-u userName`
- Specifying _only_ password: `-w passwd`
- Specifying _only_ hostname: `-h hostname`
- Specifying _only_ port: `-t portNum`

**Note:**  
When using the `-c` option, the value is parsed by splitting at the first `@` character to separate credentials from the server address. If your **password** contains an `@`, `:` or other special characters used as delimiters, you must:
- Use the individual options (`-l`, `-s`, `-u`, `-w`, `-h`, `-t`) instead

## cq:Page or Not

##### Working with cq:Page nodes

By default, matches are performed on pages with all nodes. If you wish to restrict queries to the jcr:content nodes of cq:Page nodes, then specify `-P` flag:

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -P -f mynode
```

## Searching

##### Simple Searching

If just interested in finding which nodes contain specific properties with their values or finding if a node exists within a tree by a specific name, then you can perform a search. The option to search is `-f` and can be used as:

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -f property=value
```

-or-

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -f mynode
```

_Note: just as with sling queries, you can add a wildcard in the node name, i.e. `-f mynode_*`_

## Updating

Typical usage will include specifying the top-level node under which all pages that are expected to be updated fall or are descendents.
Additionally, a single or set of properties are specified which are expected to be added or updated.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1 -p prop2=val2
```

##### Multi-Value Properties

You can also save multi-value properties. To do so, surround your property value with square brackets & comma separate the values if there are more than one. _(Currently only `String` multi-values are supported)_

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=[val1] -p prop2=[val2,val3]
```

##### Adding Properties

if you wish to create a property in a node nested within the `jcr:content` node, you need to specify the entire path past the `jcr:content` node. For example, to create/update a property, `prop1`, in the path `/content/path/to/my/page/jcr:content/par/subnode` you would use the following command:

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p par/subnode/prop1=val1
```

##### Creating Nodes

You can create a new node under nodes that match specific properties. Use the `-a` option to specify the node name and its `jcr:primaryType` (e.g. `newNode=nt:unstructured`). This requires the `-m` option to identify the parent nodes where the new node will be created. For example, to create a node `newNode` under `/jcr:content` nodes with `jcr:content=cq:PageContent`:

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -m jcr:content=cq:PageContent -a newNode=nt:unstructured
```

This creates `/content/path/to/my/page/jcr:content/newNode` with `jcr:primaryType=nt:unstructured` for each matched node.

##### Deleting Properties

Rather than updating properties, you can also delete properties that the page may contain.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -d undesiredProp
```

##### Replacing Properties

You can specify both `-p` & `-d` arguments. If you specify `-d` & `-p` with the same property name, the update will follow the rules of the SlingPostServlet. _The property is first deleted and then filled with the new content._


##### String Replacement in Single-Value Properties

You can replace a portion of the string of the value of a single-value property with another string by combining the `-p` and `-r` properties.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=orig_str_portion -r replacement_str
```

##### Conditional Replacement

An optional argument to include is a property (or properties) which the page to be updated must contain if that page is to be updated.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1 -m hasProp=currVal
```

So, only a page(s) that has the property `hasProp` with the value `currVal` will get updated.

## Copying

##### Copying Nodes

When copying nodes, you need to specify the source (or the node that should be copied) and the target (or the name of the node to which the source should be copied). The source is specified with `-i` (or `--copy-from`) & the target is specified with `-o` (or `--copy-to`).

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -i node1 -o node2
```

If you wish to copy a node nested within the `jcr:content` node, you would use the following command:

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -i par/node1 -o par/node2
```

_Note: At this time, only the first node specified will be copied regardless of how many nodes you specify at the command line._

##### Copying Properties

Copying properties is done similarly to copying nodes with the additional parameter `-P` (or `--property`). So, having a property, `prop1`, in the `jcr:content` node of the given page, you can copy the value of it to the newly specified property, `prop2` with the following command:

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -i prop1 -o prop2 -P
```

More deeply nested properties can be copied using the same technique as specified previously:

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -i par/subpar/prop1 -o par/subpar/prop2 -P
```

## Other Options


##### Use secure connection (HTTPS)

The connection to AEM is by default made over http. If you need to connect to your server via https, use the `-S` option.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1 -S
```

##### Skip SSL Checking

When using the previous option, `-S`, by default, Java validates the SSL certificate for the host you specify unless you use localhost as the host to which you're connecting. If your host is an IP address or a domain with no valid SSL certificate, then you'll need to bypass SSL certificate checking by using the `-C` option.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1 -S -C
```

##### Dry Run

A dry run will allow you to see which pages will get updated prior to making any real changes. It is enabled with `-y`.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1 -y
```

##### Verbose Output

If you would prefer more verbose output to see what's happening as it's happening, use `-x`.

```
java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1 -x
```
