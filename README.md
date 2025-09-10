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

## Usage

The AEM Page Tool can be invoked using platform-specific wrapper scripts or directly via the Java command line. The wrapper scripts simplify execution by eliminating the need to type `java -jar aem-page-tool.jar`.

- **Unix-based Systems (Linux, macOS)**: Use the `aempagetool.sh` script to run the tool. Ensure the script has execute permissions (`chmod +x aempagetool.sh`). 
  ```
  ./aempagetool.sh -n /content/path/to/my/page -p prop1=val1
  ```

- **Windows**: Use the `aempagetool.cmd` script to run the tool.
  ```
  aempagetool.cmd -n /content/path/to/my/page -p prop1=val1
  ```

- **Direct JAR Invocation**: For environments without the wrapper scripts or for custom setups, run directly using:
  ```
  java -jar aem-page-tool.jar -n /content/path/to/my/page -p prop1=val1
  ```

### Usage Options

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

By default, matches are performed all nodes. If you wish to restrict queries to nodes with a jcr:primaryType of cq:Page, then specify `-P` flag:

```
./aempagetool.sh -n /content/path/to/my/page -P -f mynode
```

## Searching

##### Simple Searching

If just interested in finding which nodes contain specific properties with their values or finding if a node exists within a tree by a specific name, then you can perform a search. The option to search is `-f` and can be used as:

```
./aempagetool.sh -n /content/path/to/my/page -f property=value
```

-or-

```
./aempagetool.sh -n /content/path/to/my/page -f jcr:content/jcr:title='My Title'
```

-or-

```
./aempagetool.sh -n /content/path/to/my/page -f mynode
```

_Note: just as with sling queries, you can add a wildcard in the node name, i.e. `-f mynode_*`_

## Updating

Typical usage will include specifying the top-level node under which all nodes that are expected to be updated fall or are descendents.
Additionally, a single or set of properties are specified which are expected to be added or updated.

```
./aempagetool.sh -n /content/path/to/my/page -p prop1=val1 -p prop2=val2
```

-or-

```
./aempagetool.sh -n /content/path/to/my/page -p prop1=val1 -p prop2=val2 -P
```

_Adding `-P` will make updates only to the `jcr:content` node of pages._

##### Multi-Value Properties

You can also save multi-value properties. To do so, surround your property value with square brackets & comma separate the values if there are more than one. _(Currently only `String` multi-values are supported)_

```
./aempagetool.sh -n /content/path/to/my/page -p prop1=[val1] -p prop2=[val2,val3]
```

```
./aempagetool.sh -n /content/path/to/my/page -p prop1=[val1] -p prop2=[val2,val3] -P
```

_Adding `-P` will make updates only to the `jcr:content` node of pages._

##### Adding Properties

if you wish to create a property in a node nested within the `jcr:content` node, you need to specify the entire path past the `jcr:content` node. For example, to create/update a property, `prop1`, in the path `/content/path/to/my/page/jcr:content/par/subnode` you would use the following command:

```
./aempagetool.sh -n /content/path/to/my/page -p par/subnode/prop1=val1
```

```
./aempagetool.sh -n /content/path/to/my/page -p par/subnode/prop1=val1 -P
```

_Adding `-P` will add properties only to the `jcr:content` node of pages._

**Please note:** this is the same syntax as the update properties syntax. This adds properties and overwrites existing properties.

##### Creating Nodes

You can create a new node under nodes that match specific properties. Use the `-a` option to specify the node name and its `jcr:primaryType` (e.g. `newNode=nt:unstructured`). This requires the `-m` option to identify the parent nodes where the new node will be created. For example, to create a node `newNode` under `/jcr:content` nodes with `jcr:content=cq:PageContent`:

```
./aempagetool.sh -n /content/path/to/my/page -m jcr:content=cq:PageContent -a newNode=nt:unstructured
```

This creates `/content/path/to/my/page/jcr:content/newNode` with `jcr:primaryType=nt:unstructured` for each matched node.

##### Deleting Properties

Rather than updating properties, you can also delete properties that the node may contain.

```
./aempagetool.sh -n /content/path/to/my/page -d undesiredProp
```

-or-

```
./aempagetool.sh -n /content/path/to/my/page -d undesiredProp -P
```

_Adding `-P` will delete only properties found in the `jcr:content` node of pages._

##### Replacing Properties

You can specify both `-p` & `-d` arguments. If you specify `-d` & `-p` with the same property name, the update will follow the rules of the SlingPostServlet. _The property is first deleted and then filled with the new content._


##### String Replacement in Single-Value Properties

You can replace a portion of the string of the value of a single-value property with another string by combining the `-p` and `-r` properties.

```
./aempagetool.sh -n /content/path/to/my/page -p prop1=orig_str_portion -r replacement_str
```

-or-

```
./aempagetool.sh -n /content/path/to/my/page -p prop1=orig_str_portion -r replacement_str -P
```

_Adding `-P` will update only properties found in the `jcr:content` node of pages._

##### Conditional Replacement

An optional argument to include is a property (or properties) which the node to be updated must contain if that node is to be updated.

```
./aempagetool.sh -n /content/path/to/my/page -p prop1=val1 -m hasProp=currVal
```

...so, only a node(s) that has the property `hasProp` with the value `currVal` will get updated.

```
./aempagetool.sh -n /content/path/to/my/page -p prop1=val1 -m hasProp=currVal -P
```

_Adding `-P` will update only properties found in the `jcr:content` node of pages._

## Copying

##### Copying Nodes

When copying nodes, you need to specify the source (or the node that should be copied) and the target (or the name of the node to which the source should be copied). The source is specified with `-i` (or `--copy-from`) & the target is specified with `-o` (or `--copy-to`).

```
./aempagetool.sh -n /content/path/to/my/page -i node1 -o node2
```

-or-

```
./aempagetool.sh -n /content/path/to/my/page -i node1 -o node2 -P
```

_Adding `-P` will copy only pages (nodes whose `jcr:primaryType` is `cq:Page`)._

If you wish to copy a node nested within the `jcr:content` node, you would use the following command:

```
./aempagetool.sh -n /content/path/to/my/page -i par/node1 -o par/node2
```

_Note: At this time, only the first node specified will be copied regardless of how many nodes you specify at the command line._

##### Copying Properties

Copying properties is done similarly to copying nodes with the additional parameter `-R` (or `--property-copy`). For example, to copy the value of a property `prop1` in the `jcr:content` node of the given node to a new property `prop2`, use the following command:

```
./aempagetool.sh -n /content/path/to/my/page -i prop1 -o prop2 -R
```

-or-

```
./aempagetool.sh -n /content/path/to/my/page -i prop1 -o prop2 -R
```

_Adding `-P` will update only properties found in the `jcr:content` node of pages._

More deeply nested properties can be copied using the same technique as specified previously:

```
./aempagetool.sh -n /content/path/to/my/page -i par/subpar/prop1 -o par/subpar/prop2 -R
```

_Note: The `-R` option ensures the source property exists before copying and handles nested property paths correctly._

## Other Options


##### Use secure connection (HTTPS)

The connection to AEM is by default made over http. If you need to connect to your server via https, use the `-S` option.

```
./aempagetool.sh -n /content/path/to/my/page -p prop1=val1 -S
```

##### Skip SSL Checking

When using the previous option, `-S`, by default, Java validates the SSL certificate for the host you specify unless you use localhost as the host to which you're connecting. If your host is an IP address or a domain with no valid SSL certificate, then you'll need to bypass SSL certificate checking by using the `-C` option.

```
./aempagetool.sh -n /content/path/to/my/page -p prop1=val1 -S -C
```

##### Dry Run

A dry run will allow you to see which pages will get updated prior to making any real changes. It is enabled with `-y`.

```
./aempagetool.sh -n /content/path/to/my/page -p prop1=val1 -y
```

##### Verbose Output

If you would prefer more verbose output to see what's happening as it's happening, use `-x`.

```
./aempagetool.sh -n /content/path/to/my/page -p prop1=val1 -x
```

## Full List of Options

```
usage: pagetool -n /path/to/parent/page -p property=value [-p p=v ...] [OPTIONS]
Available options:
  -a,--add-node <arg>     Create node with name=jcr:primaryType (e.g. newNode=nt:unstructured)
  -c <arg>                Credentials: Full combo (e.g. admin:admin@localhost:4502)
  -C                      Bypass SSL certificate checking
  -d,--delete <arg>       Property to delete
  -f,--find <arg>         Search criteria (node_name or property=value)
  -h <arg>                Server: AEM hostname (default: localhost)
  -i,--copy-from <arg>    Property to copy from (use with -o)
  -l <arg>                Credentials: Username:password combo (e.g. admin:admin)
  -m,--match <arg>        Match nodes with property=value (multiple allowed)
  -n <arg>                Parent node path for updates (required)
  -o,--copy-to <arg>      Property to copy to (use with -i)
  -p <arg>                Property to update (property=value, multiple allowed)
  -P,--page               Restrict to cq:Page nodes (default: all node types)
  -R,--property-copy      Copy properties instead of nodes (use with -i and -o)
  -r,--replace <arg>      Replace string in -p property with this value
  -s <arg>                Server: Hostname:port combo (e.g. localhost:4502)
  -S                      Use HTTPS instead of HTTP
  -t <arg>                Server: AEM port (default: 4502)
  -u <arg>                Credentials: Username for AEM (default: admin)
  -w <arg>                Credentials: Password for AEM (default: admin)
  -x                      Verbose output
  -y                      Perform a dry run (no updates)
```
