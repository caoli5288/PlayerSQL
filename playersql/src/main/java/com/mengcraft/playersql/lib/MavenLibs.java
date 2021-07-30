package com.mengcraft.playersql.lib;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenLibs {

    private static final String LOCAL_REPOSITORY = System.getProperty("user.home") + "/.m2/repository";
    private static final String CENTRAL = System.getProperty("maven.repository", "https://mirrors.huaweicloud.com/repository/maven");

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final XPathFactory X_PATH_FACTORY = XPathFactory.newInstance();

    private static final Logger LOGGER = Logger.getLogger("MavenLibs");
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{(.+?)}");
    private static final Set<String> NSS = Sets.newHashSet();
    private static final Method INVOKER_addURL = asInvoker(URLClassLoader.class, "addURL", URL.class);

    private final Map<String, String> properties = Maps.newHashMap();
    private final String ns;
    private final String basename;
    private final boolean optional;

    private MavenLibs(String groupId, String artifactId, String version, boolean optional) {
        properties.put("project.groupId", groupId);
        properties.put("project.artifactId", artifactId);
        properties.put("project.version", version);
        basename = artifactId + "-" + version;
        ns = groupId.replace('.', '/') + "/" + artifactId + "/" + version;
        this.optional = optional;
    }

    public void load() {
        load(MavenLibs.class.getClassLoader());
    }

    @SneakyThrows
    public void load(ClassLoader cl) {
        Preconditions.checkState(cl instanceof URLClassLoader, "Current classloader not instanceof URLClassLoader");
        if (optional || NSS.contains(ns)) {
            return;
        }
        // process depends first
        List<MavenLibs> depends = depends();
        if (!depends.isEmpty()) {
            for (MavenLibs depend : depends) {
                depend.load(cl);
            }
        }
        NSS.add(ns);
        // skip if not jar packages
        if (!isNullOrEquals(properties.get("project.packaging"), "jar")) {
            return;
        }
        // hack into classloader
        File jar = new File(LOCAL_REPOSITORY, ns + "/" + basename + ".jar");
        if (!jar.exists()) {
            String url = CENTRAL + "/" + ns + "/" + basename + ".jar";
            LOGGER.info("Get " + url);
            downloads(jar, url);
        }
        INVOKER_addURL.invoke(cl, jar.toURI().toURL());
        LOGGER.info(String.format("Load MavenLibs(%s)", ns));
    }

    @SneakyThrows
    private List<MavenLibs> depends() {
        File pom = new File(LOCAL_REPOSITORY, ns + "/" + basename + ".pom");
        if (!pom.exists()) {
            downloads(pom, CENTRAL + "/" + ns + "/" + basename + ".pom");
        }
        DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        Document doc = builder.parse(pom);
        XPath x = X_PATH_FACTORY.newXPath();
        // parse properties
        Node node = (Node) x.evaluate("/project/properties", doc, XPathConstants.NODE);
        if (node != null) {
            mapOf(() -> properties, node);
        }
        // result container
        List<MavenLibs> result = new ArrayList<>();
        // parent
        node = (Node) x.evaluate("/project/parent", doc, XPathConstants.NODE);
        if (node != null) {
            MavenLibs parent = of(mapOf(node));
            parent.properties.put("project.packaging", "pom");
            result.add(parent);
        }
        // dependencies
        NodeList dependencies = (NodeList) x.evaluate("/project/dependencies/dependency", doc, XPathConstants.NODESET);
        int length = dependencies.getLength();
        for (int i = 0; i < length; i++) {
            result.add(of(mapOf(dependencies.item(i))));
        }
        return result;
    }

    @SneakyThrows
    private void downloads(File f, String url) {
        File parent = f.getParentFile();
        Preconditions.checkState(parent.exists() || parent.mkdirs(), "mkdirs");
        File tmp = File.createTempFile("MavenLibs", ".tmp");
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0");
        try {
            try (FileOutputStream fs = new FileOutputStream(tmp)) {
                ByteStreams.copy(connection.getInputStream(), fs);
            }
            if (f.exists()) {// async downloads?
                Preconditions.checkState(tmp.delete(), "delete tmp");
            } else {
                Files.move(tmp, f);
            }
        } catch (Exception e) {
            Preconditions.checkState(tmp.delete(), "delete tmp");
        } finally {
            connection.disconnect();
        }
    }

    private Map<String, String> mapOf(Node node) {
        return mapOf(Maps::newHashMap, node);
    }

    private Map<String, String> mapOf(Supplier<Map<String, String>> factory, Node node) {
        Map<String, String> map = factory.get();
        NodeList childNodes = node.getChildNodes();
        int length = childNodes.getLength();
        for (int i = 0; i < length; i++) {
            Node _node = childNodes.item(i);
            if (_node.hasChildNodes()) {
                String nodeName = _node.getNodeName();
                String value = _node.getFirstChild().getNodeValue();
                Matcher mc = PROPERTY_PATTERN.matcher(value);
                while (mc.find()) {
                    value = value.replace(mc.group(), String.valueOf(properties.get(mc.group(1))));
                }
                map.put(nodeName, value);
            }
        }
        return map;
    }

    private static Method asInvoker(Class<?> cls, String name, Class<?>... parameters) {
        try {
            Method method = cls.getDeclaredMethod(name, parameters);
            method.setAccessible(true);
            return method;
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean isNullOrEquals(String obj, String comp) {
        return obj == null || obj.equals(comp);
    }

    private static MavenLibs of(Map<String, String> map) {
        String groupId = map.get("groupId");
        String artifactId = map.get("artifactId");
        String version = map.get("version");
        boolean optional = Boolean.parseBoolean(map.get("optional")) || version == null || !isNullOrEquals(map.get("scope"), "compile");
        return new MavenLibs(groupId, artifactId, version, optional);
    }

    public static MavenLibs of(String groupId, String artifactId, String version) {
        return new MavenLibs(groupId, artifactId, version, false);
    }

    public static MavenLibs of(String namespaces) {
        String[] split = namespaces.split(":");
        return of(split[0], split[1], split[2]);
    }
}
