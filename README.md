**Railway** is a simple **HTTP Groovy server** that supports **dynamic page** loading. 
It does so by utilizing **Groovy scripts** in the root directory.

Assume the path `/greet/<name>`, where the name parameter will be substituted with one given by the user.

The correspondent **Groovy script** should be located at `<root_directory>/greet.groovy` with contents:

```groovy
import com.sun.net.httpserver.HttpExchange
import it.fulminazzo.railway.ContentHandler
import it.fulminazzo.railway.HTTPCode

static ContentHandler.HTTPResponse handle(HttpExchange httpExchange) {
    def path = httpExchange.requestURI.path
    def matcher = path =~ /(\/greet\/)([A-Za-z]+)/
    if (matcher.find()) return new ContentHandler.HTTPResponse(HTTPCode.OK, matcher.group(1),
                "Hello, ${matcher.group(2)}!")
    else return new ContentHandler.HTTPResponse(HTTPCode.BAD_REQUEST, path,
            "No name provided!"
    )
}
```

**NOTE**: this structure is **mandatory**. 
The file can be manipulated as the developer pleases, 
but a method named **handle** with **parameter `HttpExchange`** and **return type `HTTPResponse`** 
will **always** be required.
