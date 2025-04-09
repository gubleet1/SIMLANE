package ch.simlane.tme;

import ch.simlane.Simlane;
import ch.simlane.ModelLoader;
import ch.simlane.editor.Editor;
import ch.simlane.editor.Map;
import ch.simlane.editor.scenario.Scenario;
import ch.simlane.utils.JSONMap;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ModelTest {

    private final String VALID_JSON_MAP = "out/test/SIMLANE/validMap.siml";
    private final String INVALID_JSON_MAP = "out/test/SIMLANE/invalidMap.siml";

    private Simlane simlane;
    private Editor editor;
    private Engine tme;


    @BeforeEach
    public void initialize() {
        this.simlane = new Simlane();
        this.editor = simlane.getEditor();
        this.tme = simlane.getTme();
    }

    @AfterEach
    public void destroy() {
        this.simlane = null;
        this.editor = null;
        this.tme = null;
    }

    @Test
    public void testModelValid() throws IOException {
        File mapFile = new File(VALID_JSON_MAP);
        JSONMap jsonMap = new JSONMap(IOUtils.toString(new FileInputStream(mapFile), StandardCharsets.UTF_8));
        Map validMap = jsonMap.getMap();
        Scenario validScenario = editor.getScenario();
        new Thread(new ModelLoader(validMap, validScenario, tme)).start();
    }

    @Test
    public void testModelInvalid() throws IOException {
        File mapFile = new File(INVALID_JSON_MAP);
        JSONMap jsonMap = new JSONMap(IOUtils.toString(new FileInputStream(mapFile), StandardCharsets.UTF_8));
        Map invalidMap = jsonMap.getMap();
        Scenario invalidScenario = editor.getScenario();
        ModelLoader modelLoader = new ModelLoader(invalidMap, invalidScenario, tme);
        assertThrows(Exception.class, () -> {
            modelLoader.createModel();
        });
    }

}
