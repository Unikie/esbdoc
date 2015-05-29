package fi.mystes.esbdoc;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by mystes-am on 29.5.2015.
 */
public class JsonWriter {
    private static Log log = LogFactory.getLog(JsonWriter.class);

    private ArtifactMap artifactMap;
    private TestMap testsMap;
    private ArtifactDependencyMap forwardDependencyMap;
    private ArtifactDependencyMap reverseDependencyMap;


    public JsonWriter(ArtifactDependencyMap forwardDependencyMap, ArtifactDependencyMap reverseDependencyMap, TestMap testsMap, ArtifactMap artifactMap){
        this.artifactMap = artifactMap;
        this.testsMap = testsMap;
        this.forwardDependencyMap = forwardDependencyMap;
        this.reverseDependencyMap = reverseDependencyMap;
    }

    private JsonGenerator createGeneratorFor(OutputStream outputStream) throws IOException{
        JsonFactory factory = new JsonFactory();
        return factory.createGenerator(outputStream);
    }

    public void writeJson(OutputStream outputStream) throws IOException {
        JsonGenerator generator = createGeneratorFor(outputStream);
        generator.writeStartObject();

        generator.writeObjectFieldStart("resources");
        for (Artifact artifact : artifactMap.getArtifacts()) {
            generator.writeObjectFieldStart(artifact.getName());
            if (artifact.isDescriptionDefined()) {
                writeArtifactDescriptionJson(artifact.description, generator);
            }
            generator.writeStringField("type", artifact.getType().toString());
            generator.writeEndObject();
        }
        generator.writeEndObject();

        generator.writeObjectFieldStart("dependencies");

        generator.writeObjectFieldStart("forward");
        for (Entry<Artifact, Set<Dependency>> entry : forwardDependencyMap.entrySet()) {
            generator.writeArrayFieldStart(entry.getKey().getName());

            for (Dependency d : entry.getValue()) {
                // Currently only Artifacts are included in the JSON output
                if (d.getDependency() instanceof Artifact) {
                    generator.writeStartObject();
                    generator.writeStringField("target", ((Artifact) d.getDependency()).getName());
                    generator.writeStringField("type", d.getType().toString());
                    generator.writeEndObject();
                }
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();

        generator.writeObjectFieldStart("reverse");
        for (Entry<Artifact, Set<Dependency>> entry : reverseDependencyMap.entrySet()) {
            generator.writeArrayFieldStart(entry.getKey().getName());
            for (Dependency d : entry.getValue()) {
                generator.writeStartObject();
                generator.writeStringField("source", d.dependent.getName());
                generator.writeStringField("type", d.getType().toString());
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();
        generator.writeEndObject();

        generator.writeObjectFieldStart("tests");
        for (Entry<String, Set<TestProject>> entry : testsMap.entrySet()) {
            generator.writeArrayFieldStart(entry.getKey());
            for (TestProject p : entry.getValue()) {
                generator.writeStartObject();
                generator.writeStringField("project", p.getName());
                generator.writeStringField("filename", p.getFilename());
                generator.writeArrayFieldStart("suites");
                for (TestSuite s : p.getTestSuites()) {
                    generator.writeStartObject();
                    generator.writeStringField("name", s.getName());
                    generator.writeArrayFieldStart("cases");
                    for (TestCase c : s.getTestCases()) {
                        generator.writeStartObject();
                        generator.writeStringField("name", c.getName());
                        generator.writeEndObject();
                    }
                    generator.writeEndArray();
                    generator.writeEndObject();
                }
                generator.writeEndArray();
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();

        generator.writeEndObject();
        generator.close();
    }

    private void writeArtifactDescriptionJson(ArtifactDescription artifactDescription, JsonGenerator generator) throws IOException {
        if (artifactDescription.isPurposeDefined()) {
            generator.writeStringField("purpose", artifactDescription.purpose);
        }

        if (artifactDescription.isReceivesDefined()) {
            generator.writeObjectFieldStart("receives");
            writeArtifactInterfaceInfoJson(artifactDescription.receives, generator);
            generator.writeEndObject();
        }

        if (artifactDescription.isReturnsDefined()) {
            generator.writeObjectFieldStart("returns");
            writeArtifactInterfaceInfoJson(artifactDescription.returns, generator);
            generator.writeEndObject();
        }
    }

    private void writeArtifactInterfaceInfoJson(ArtifactInterfaceInfo artifactInterfaceInfo, JsonGenerator generator) throws IOException {
        if (artifactInterfaceInfo.isDescriptionDefined()) {
            generator.writeStringField("description", removeLineBreaks(artifactInterfaceInfo.description));
        }

        if (artifactInterfaceInfo.isFieldsDefined()) {
            generator.writeArrayFieldStart("fields");
            for (ArtifactIntefaceField artifactIntefaceField : artifactInterfaceInfo.fields) {
                generator.writeStartObject();
                if (artifactIntefaceField.isDescriptionDefined()) {
                    generator.writeStringField("description", StringEscapeUtils.escapeJson(removeLineBreaks(artifactIntefaceField.description)));
                } else {
                    generator.writeStringField("description", "");
                    log.warn(artifactIntefaceField.getArtifactName() + ": Has empty description field.");
                }
                generator.writeStringField("path", artifactIntefaceField.path);
                generator.writeBooleanField("optional", artifactIntefaceField.optional);
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }

        if (artifactInterfaceInfo.isExampleDefined()) {
            generator.writeStringField("example", removeLineBreaks(artifactInterfaceInfo.example));
        }
    }

    private String removeLineBreaks(String text) {
        if(StringUtils.isBlank(text)){
            return null;
        }
        return StringEscapeUtils.escapeJson(text.replace("\n", "").replace("\r", "").replace("\r\n", ""));
    }
}
