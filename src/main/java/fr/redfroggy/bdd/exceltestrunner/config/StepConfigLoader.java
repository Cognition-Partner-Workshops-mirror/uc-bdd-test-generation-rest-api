package fr.redfroggy.bdd.exceltestrunner.config;

import fr.redfroggy.bdd.exceltestrunner.exception.StepConfigNotFoundException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Loads and parses step-config.yml, provides regex-based lookup to resolve
 * a step description to its corresponding StepConfig.
 *
 * @author Ashish,Raut
 */
@Component
public class StepConfigLoader {

    // Path to the step configuration YAML file on the classpath
    private static final String CONFIG_FILE = "step-config.yml";

    // Loaded step configurations parsed from the YAML file
    private List<StepConfig> stepConfigs = new ArrayList<>();

    /**
     * Loads and parses the step-config.yml file at application startup.
     * Each entry in the YAML is converted into a StepConfig POJO.
     *
     * @throws IOException if the configuration file cannot be read
     */
    @PostConstruct
    @SuppressWarnings("unchecked")
    public void loadConfig() throws IOException {
        ClassPathResource resource = new ClassPathResource(CONFIG_FILE);
        try (InputStream inputStream = resource.getInputStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(inputStream);

            // Parse "steps" list from the YAML root
            List<Map<String, Object>> stepsYaml = (List<Map<String, Object>>) root.get("steps");
            if (stepsYaml == null) {
                return;
            }

            for (Map<String, Object> stepYaml : stepsYaml) {
                StepConfig config = new StepConfig();
                config.setStepPattern((String) stepYaml.get("stepPattern"));
                config.setHttpMethod((String) stepYaml.get("httpMethod"));
                config.setEndpoint((String) stepYaml.get("endpoint"));
                config.setJsonTemplate((String) stepYaml.get("jsonTemplate"));
                config.setRequiredFields((List<String>) stepYaml.get("requiredFields"));

                // Parse verification configurations if present
                List<Map<String, Object>> verificationsYaml =
                        (List<Map<String, Object>>) stepYaml.get("verifications");
                if (verificationsYaml != null) {
                    List<VerificationConfig> verifications = new ArrayList<>();
                    for (Map<String, Object> verYaml : verificationsYaml) {
                        VerificationConfig verConfig = new VerificationConfig();
                        verConfig.setType((String) verYaml.get("type"));
                        verConfig.setExpectedValue((String) verYaml.get("expectedValue"));
                        verConfig.setJsonPath((String) verYaml.get("jsonPath"));
                        verifications.add(verConfig);
                    }
                    config.setVerifications(verifications);
                }

                stepConfigs.add(config);
            }
        }
    }

    /**
     * Finds the StepConfig whose stepPattern regex matches the given step description.
     *
     * @param stepDescription the step description text from the Excel file
     * @return the matching StepConfig
     * @throws StepConfigNotFoundException if no matching configuration is found
     */
    public StepConfig findByDescription(String stepDescription) {
        for (StepConfig config : stepConfigs) {
            Pattern pattern = Pattern.compile(config.getStepPattern(), Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(stepDescription).matches()) {
                return config;
            }
        }
        throw new StepConfigNotFoundException(
                "No step configuration found matching description: " + stepDescription);
    }

    /**
     * Returns all loaded step configurations.
     *
     * @return list of StepConfig objects
     */
    public List<StepConfig> getStepConfigs() {
        return stepConfigs;
    }

    /**
     * Sets the step configurations (useful for testing).
     *
     * @param stepConfigs the list of StepConfig objects to set
     */
    public void setStepConfigs(List<StepConfig> stepConfigs) {
        this.stepConfigs = stepConfigs;
    }
}
