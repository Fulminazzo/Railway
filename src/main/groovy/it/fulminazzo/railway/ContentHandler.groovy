package it.fulminazzo.railway

import org.jetbrains.annotations.NotNull

class ContentHandler {
    final @NotNull File root

    ContentHandler(@NotNull String rootDirPath) {
        this.root = new File(rootDirPath)
        if (!this.root.isDirectory())
            throw new ContentHandlerException('Could not find directory at path: ' + rootDirPath)
    }

}
