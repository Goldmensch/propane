import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.MalformedInputException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

class ReplacePlaceholder {
    private final Map<String, String> replacements;
    private  final JSONObject license;

    ReplacePlaceholder(Map<String, String> replacements) throws IOException, InterruptedException {
        this.replacements = new HashMap<>(replacements);
        this.license = loadLicense(this.replacements.get("LICENSE_NAME"));
        this.replacements.put("LICENSE_URL", license.getString("html_url"));
    }

    public static void replace(Project project) throws IOException {
        Map<String, String> replacements = Map.ofEntries(
                projectName(project),
                entry("PROJECT_LOWER_NAME", projectName(project).getValue().toLowerCase()),
                entry("PROJECT_DESC", project.description()),
                entry("JAVA_VERSION", String.valueOf(project.javaVersion())),
                entry("LICENSE_NAME", project.license()),
                entry("AUTHOR_NAME", project.authorName().isBlank()
                        ? getRepoMeta("REPO_OWNER")
                        : project.authorName()),
                entry("REPO_URL", getRepoMeta("REPO_URL")),
                entry("REPO_OWNER", getRepoMeta("REPO_OWNER")),
                entry("MVN_GROUP", project.mvnGroup()),
                entry("MVN_ARTIFACT", project.mvnArtifact().isBlank()
                        ? projectName(project).getValue().toLowerCase()
                        : project.mvnArtifact()
                ),
                repoWoOwnerName()
        );

        try {
            new ReplacePlaceholder(replacements).doReplace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getRepoMeta(String key) throws IOException {
        Path metaFile = RootPath.ROOT.resolve("repo_metadata");
        if (Files.notExists(metaFile)) {
            throw new RuntimeException("File `repo_metadata` not found. Please wait for ci to finish and fetch repo again!");
        }

        String meta = Files.readString(metaFile);
        for (String line : meta.lines().toList()) {
            String[] split = line.split("=", 2);
            if (split[0].equals(key)) {
                return split[1];
            }
        }
        throw new IllegalArgumentException("Key %s not found in repo_metadata".formatted(key));
    }

    private static Map.Entry<String, String> repoWoOwnerName() throws IOException {
        return entry("REPO_WO_OWNER_NAME", getRepoMeta("REPO_NAME").split("/")[1]);
    }

    private static Map.Entry<String, String> projectName(Project project) throws IOException {
        String projectName = project.name();
        String resolved = projectName.isBlank()
                ? repoWoOwnerName().getValue()
                : projectName;
        return entry("PROJECT_NAME", resolved);
    }

    private static final HttpClient client = HttpClient.newHttpClient();
    private static JSONObject loadLicense(String name) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.github.com/licenses/%s".formatted(name)))
                .header("Accept", "application/vnd.github+json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new JSONObject(response.body());
    }

    private void doReplace() throws IOException, InterruptedException {

        Files.walkFileTree(RootPath.ROOT, new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir.getFileName().equals(Path.of("setup_code"))) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (path.getFileName().equals(Path.of("README.md")) || Files.isDirectory(path)) {
                    return FileVisitResult.CONTINUE;
                }

                Files.createDirectories(path.getParent());

                try {
                    String content = Files.readString(path);
                    Files.writeString(path, replaceContent(content));
                } catch (MalformedInputException _) {
                    // ignore if no text file
                    return FileVisitResult.CONTINUE;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });


        Files.move(RootPath.ROOT.resolve("README.md"), RootPath.ROOT.resolve("SETUP.md"));
        copyDefault("README.md", Path.of("."));
        copyDefault("Library.java", Path.of("lib/src/main/java")
                .resolve(Path.of(replacements.get("MVN_GROUP").replace('.', '/')))
        );
        createLicenseFile();

    }

    private void createLicenseFile() throws IOException {
        String content = license.getString("body");
        String name = replacements.get("LICENSE_NAME");

        content = switch (name) {
            case "MIT" -> content.replace("[fullname]", replacements.get("AUTHOR_NAME")).replace("[year]", String.valueOf(LocalDate.now().getYear()));
            default -> content;
        };

        Path path = RootPath.ROOT.resolve("LICENSE");
        Files.deleteIfExists(path);
        Files.writeString(path, content, StandardOpenOption.CREATE);
    }

    private void copyDefault(String name, Path pathFromRoot) throws IOException {
        String s = Files.readString(RootPath.ROOT.resolve("setup_code/defaults").resolve(name));

        Path path = RootPath.ROOT.resolve(pathFromRoot).resolve(name);
        Files.deleteIfExists(path);
        Files.createDirectories(path.getParent());
        Files.writeString(path, replaceContent(s), StandardOpenOption.CREATE);
    }

    private String replaceContent(String content) {
        String text = content;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }


}



