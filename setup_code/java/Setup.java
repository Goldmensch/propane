///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.jline:jline:3.25.1
//DEPS org.json:json:20250517
//JAVA 25
//SOURCES Project.java
//SOURCES ReplacePlaceholder.java
//SOURCES RootPath.java
//SOURCES Util.java

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

Terminal terminal;

void main() throws Exception {
    terminal = TerminalBuilder.builder().system(true).build();

    try {
        Project project = new Project(
                read("Project name (default repository name)", false),
                read("Project description", true),
                readInt("Used Java version", 8, 25),
                read("Project author name (default repository owner)", false),
                read("Maven group", true),
                read("Maven artifact (default repository name)", false),
                read("Project license (TAB for list)", true, Project.LICENSES)
        );

        // replace placeholders
        IO.println("Replacing placeholders in repository...");
        ReplacePlaceholder.replace(project);

        // remove setup files
        IO.println("Removing setup files...");
        Files.deleteIfExists(RootPath.ROOT.resolve(".github/workflows/create_metadata_file.yml"));
        Files.deleteIfExists(RootPath.ROOT.resolve("setup"));
        Files.deleteIfExists(RootPath.ROOT.resolve("repo_metadata"));
        Util.deleteDirectory(RootPath.ROOT.resolve("setup_code"));

        // git: commit changes
        exec("git", "add", ".");
        exec("git", "commit", "-m", "Prepare repository");

        // git: create gh-pages
        exec("git", "switch", "--orphan", "gh-pages");
        exec("git", "commit", "--allow-empty", "-m", "Prepare Github Pages branch");
        pushQuestion("gh-pages");


        // git: switch back to master
        exec("git", "switch", "master");
        pushQuestion("master");

    } catch (Abort _ ) {}
}

void pushQuestion(String branch) throws IOException, InterruptedException {
    String answer = read("Push changes to %s? [yes/no]".formatted(branch), true, "yes", "no");
    if (answer.startsWith("y")) {
        exec("git", "push");
    }
}

void exec(String... cmd) throws IOException, InterruptedException {
    new ProcessBuilder(cmd)
            .inheritIO()
            .directory(RootPath.ROOT.toFile())
            .start()
            .waitFor();
}

int readInt(String prompt, int from, int to) {
    String[] allowed = IntStream.range(from, to + 1).mapToObj(String::valueOf).toArray(String[]::new);

    String read = read(prompt, true, allowed);

    try {
        return Integer.parseUnsignedInt(read);
    } catch (NumberFormatException e) {
        System.err.println("Input must be number!");
        throw new Abort();
    }
}

String read(String prompt, boolean required, String... completions) {
    LineReader reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .completer(new StringsCompleter(completions))
            .build();

    String input = reader.readLine(prompt + ": ").trim();
    if (input.isBlank() && required) {
        System.err.println("Input cannot be blank!");
        throw new Abort();
    }

    if (completions.length != 0 && !List.of(completions).contains(input)) {
        System.err.println("Input doesn't match any of: " + String.join(", ", completions));
        throw new Abort();
    }

    return input;
}

static class Abort extends RuntimeException {}



