package it.fulminazzo.railway

import org.jetbrains.annotations.NotNull

class ContentHandler {
    static final INDEX_NAME = 'index.html'

    final @NotNull File root

    ContentHandler(@NotNull String rootDirPath) {
        this.root = new File(rootDirPath)
        if (!this.root.isDirectory())
            throw new ContentHandlerException('Could not find directory at path: ' + rootDirPath)
    }

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
