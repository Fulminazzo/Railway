import com.sun.net.httpserver.HttpExchange
import it.fulminazzo.railway.ContentHandler
import it.fulminazzo.railway.HTTPCode

static ContentHandler.HTTPResponse handle(HttpExchange httpExchange) {
    def path = httpExchange.requestURI.path
    def matcher = path =~ /(\/scripts[\/A-Za-z.]+)\/([A-Za-z]+)/
    if (matcher.find()) return new ContentHandler.HTTPResponse(HTTPCode.OK, matcher.group(1),
                "Hello, ${matcher.group(2)}!")
    else return new ContentHandler.HTTPResponse(HTTPCode.BAD_REQUEST, path,
            "No name provided!"
    )
}