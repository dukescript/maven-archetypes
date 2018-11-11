/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2015 Anton Epple <toni.epple@eppleton.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dukescript.archetype.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.reporters.Files;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class VerifyBase {
    private final String archetypeName;
    protected String oat;
    protected String someuser;

    protected VerifyBase(String archetypeName) {
        this.archetypeName = archetypeName;
    }

    @BeforeClass
    public static void initializeOutput() {
        MavenRunner.initializeOutput();
    }

    @BeforeMethod
    public void cleanUpMavenRepo() throws IOException {
        String home = System.getProperty("java.home");
        final int hash = home.hashCode() + getClass().hashCode();
        someuser = "someuser" + Integer.toHexString(hash);

        File repo = new File(new File(
                new File(new File(System.getProperty("user.home"), ".m2"), "repository"),
                "org"), someuser
        );
        if (repo.exists()) {
            java.nio.file.Files.walkFileTree(repo.toPath(), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    java.nio.file.Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    java.nio.file.Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @BeforeMethod
    public void randomOat() {
        oat = "oat" + new Random().nextInt(10);
    }

    protected void adjustArchetype(Properties sysProp) {
    }

    protected final File generateFromArchetype(String aId, final File dir, String... params) throws Exception {
        Verifier v = createVerifier(dir.getAbsolutePath());
        v.getCliOptions().add("-Denforcer.fail=true");
        v.setAutoclean(false);
        v.setLogFileName("generate.log");
        v.deleteDirectory("");
        dir.mkdirs();
        aId = getClass().getSimpleName() + "-" + aId;
        Properties sysProp = v.getSystemProperties();
        sysProp.put("groupId", "org." + someuser + ".test");
        sysProp.put("artifactId", aId);
        sysProp.put("package", "org." + someuser + ".test." + oat + "");
        sysProp.put("archetypeGroupId", "com.dukescript.archetype");
        sysProp.put("archetypeArtifactId", archetypeName);
        sysProp.put("archetypeVersion", findCurrentVersion());
        sysProp.put("archetypeCatalog", "local");
        adjustArchetype(sysProp);

        for (String p : params) {
            v.addCliOption(p);
        }
        v.executeGoal("archetype:generate");
        v.verifyErrorFreeLog();
        File created = new File(dir, aId);
        assertTrue(created.exists(), "Directory created: " + created);
        assertPomVersions(created);
        return created;
    }

    private String findCurrentVersion() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException, XPathFactoryConfigurationException {
        final ClassLoader l = getClass().getClassLoader();
        URL u = l.getResource("META-INF/maven/com.dukescript.archetype/" + archetypeName + "/pom.xml");
        assertNotNull(u, "Own pom found: " + System.getProperty("java.class.path"));

        final XPathFactory fact = XPathFactory.newInstance();
        fact.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        XPathExpression xp = fact.newXPath().compile("project/version/text()");

        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(u.openStream());
        return xp.evaluate(dom);
    }

    protected static void writeBinary(File out) throws IOException {
        FileOutputStream fos = new FileOutputStream(out);
        for (int i = 0; i < 256; i++) {
            fos.write(i);
        }
        fos.close();
    }

    protected static void assertBinary(InputStream is) throws IOException {
        for (int i = 0; i < 256; i++) {
            int b = is.read();
            assertEquals((byte)b, (byte)i, i + "th byte of the stream should be equal");
        }
        is.close();
    }

    protected static void assertNoTextInSubdir(String fx, File genRoot) throws IOException {
        if (genRoot.isFile()) {
            if (Files.readFile(genRoot).contains(fx)) {
                fail("String " + fx + " is in file " + genRoot);
            }
        } else {
            for (File ch : genRoot.listFiles()) {
                assertNoTextInSubdir(fx, ch);
            }
        }
    }

    protected void assertHTMLContent(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        for (;;) {
            String l = br.readLine();
            if (l == null) {
                fail("There should be 'foreach: words' in the file!");
            }
            if (l.contains("foreach:") && l.contains("words")) {
                br.close();
                return;
            }
        }
    }
    protected Verifier createVerifier(String path) throws VerificationException {
        return new MavenRunner(path);
    }

    private static void assertPomVersions(File root) throws IOException {
        File[] children = root.listFiles();
        if (children == null) {
            return;
        }
        for (File ch : children) {
            if (ch.getName().equals("target")) {
                continue;
            }
            if (ch.isDirectory()) {
                assertPomVersions(ch);
                continue;
            }
            if (!ch.getName().equals("pom.xml")) {
                continue;
            }
            Pattern artifact = Pattern.compile("<artifactId>(.*)</artifactId>");
            Pattern pattern = Pattern.compile("<version>(.*)</version>");
            BufferedReader r = new BufferedReader(new FileReader(ch));
            String lastArtifact = "";
            for (int lineNo = 1;; lineNo++) {
                String line = r.readLine();
                if (line == null) {
                    break;
                }
                Matcher artiMatcher = artifact.matcher(line);
                if (artiMatcher.find()) {
                    lastArtifact = artiMatcher.group(1);
                }
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String version = matcher.group(1);
                    if (version.startsWith("${")) {
                        continue;
                    }
                    if (version.equals("1.0-SNAPSHOT")) {
                        continue;
                    }
                    if (lastArtifact.startsWith("maven-") && lastArtifact.endsWith("-plugin")) {
                        continue;
                    }
                    if ("nbm-maven-plugin".equals(lastArtifact)) {
                        continue;
                    }
                    if ("exec-maven-plugin".equals(lastArtifact)) {
                        continue;
                    }
                    if ("image-maven-plugin".equals(lastArtifact)) {
                        continue;
                    }
                    if (version.equals("4.12") && "junit-osgi".equals(lastArtifact)) {
                        continue;
                    }
                    if (version.equals("1.2.3.RELEASE") && "springloaded".equals(lastArtifact)) {
                        continue;
                    }
                    if (version.equals("5.0") && "asm".equals(lastArtifact)) {
                        continue;
                    }
                    fail("Hardcoded version " + version + " for " + lastArtifact + " line " + lineNo + " in " + ch);
                }
            }
            r.close();
        }
    }

    protected static String assertReplace(String in, String find, String insert) {
        int at = in.indexOf(find);
        assertNotEquals(-1, at, find + " found in " + in);
        return in.substring(0, at) + insert + in.substring(at + find.length());
    }

    protected final void verifyErrorFreeLogSkipJavaSupport(Verifier v) throws VerificationException {
        String foundError = null;
        for (String line : v.loadFile(v.getBasedir(), v.getLogFileName(), false)) {
            if (line.contains("[ERROR]")) {
                foundError = line;
                continue;
            }
            if (line.contains("System artifact: moe.sdk:moe.sdk.java8support:jar:1.0:system has no file attached")) {
                foundError = null;
            }
            if (foundError != null) {
                fail("Found error in the log: " + foundError);
            }
        }
    }

    protected static boolean isJDK11Plus() {
        String version = System.getProperty("java.version");
        if (version != null && version.startsWith("11")) {
            return true;
        }
        try {
            Class.forName("java.lang.Module");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

}
