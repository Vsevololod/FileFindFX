package today.vse;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FileSearchThread extends Thread {
    class SaveFilterFileVisitor extends SimpleFileVisitor<Path> {

        private Queue<Path> pathQueue;
        PathMatcher matcher;
        String strToFind;

        private boolean containString(Path file) {
            try {
                return Files.lines(file).parallel().anyMatch(s -> s.contains(strToFind));
            } catch (IOException e) {
                return false;
            }
        }

        SaveFilterFileVisitor(Queue<Path> queue, String pattern, String findString) {
            this.pathQueue = queue;
            this.strToFind = findString;
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (attrs.isRegularFile() && matcher.matches(file.getFileName())) {
                if (strToFind.isEmpty() | strToFind.equals("*") | containString(file)) {
                    pathQueue.add(file);
                }
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private ConcurrentLinkedQueue<Path> filePatchQueue;
    private Path root;
    private String filePattern;
    private String searchString;

    FileSearchThread(Path path, String pattern, String searchString, ConcurrentLinkedQueue<Path> queue) {
        this.root = path;
        this.filePattern = pattern;
        this.filePatchQueue = queue;
        this.searchString = searchString;
    }

    @Override
    public void run() {
        try {
            Files.walkFileTree(root, new SaveFilterFileVisitor(filePatchQueue, filePattern, searchString));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
