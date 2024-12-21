package it.fulminazzo.railway

import org.jetbrains.annotations.NotNull

/**
 * A class responsible for correctly handling paths and returning appropriate files.
 */
class ContentHandler {
    static final INDEX_NAME = 'index.html'

    final @NotNull File root

    /**
     * Instantiates a new ContextHandler
     *
     * @param rootDirPath the root directory path
     */
    ContentHandler(@NotNull String rootDirPath) {
        this.root = new File(rootDirPath)
        if (!this.root.isDirectory())
            throw new ContentHandlerException('Could not find directory at path: ' + rootDirPath)
    }

    /**
     * Resolves the given path by:
     * <ul>
     *     <li>if it contains a file extension, it searches for the file in the file system;</li>
     *     <li>if it does not contain, it searches for the file <code>&lt;path&gt;.html</code> in the file system;</li>
     *     <li>if it is a directory, it searches for the file <code>&lt;path&gt;/index.html</code> in the file system.</li>
     * <ul>
     * In every other case, it throws an exception.
     *
     * @param path the path
     * @return the file
     * @throws ContentHandlerException the exception thrown in case of error
     */
    File resolvePath(@NotNull String path) throws ContentHandlerException {
        def file = new File(this.root, path)
        if (file.isFile()) return file
        else if (file.isDirectory()) {
            file = new File(file, INDEX_NAME)
            if (file.isFile()) return file
            else throw new ContentHandlerException('Could not find path: ' + path)
        } else {
            def pattern = ~/(.*)\.([^.]+)/
            def matcher = path =~ pattern
            if (matcher.matches())
                throw new ContentHandlerException('Could not find path: ' + path)
            else {
                file = new File(this.root, path + '.html')
                if (file.isFile()) return file
                else throw new ContentHandlerException('Could not find path: ' + path)
            }
        }
    }
}
