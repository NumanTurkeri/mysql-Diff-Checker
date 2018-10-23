import java.util.List;

public class SchemasPair {
    private List<String> referenceSchemas;
    private List<String> targetSchemas;

    public SchemasPair(List<String> referenceSchemas, List<String> targetSchemas) {
        this.referenceSchemas = referenceSchemas;
        this.targetSchemas = targetSchemas;
    }

    public List<String> getReferenceSchemas() {
        return referenceSchemas;
    }

    public void setReferenceSchemas(List<String> referenceSchemas) {
        this.referenceSchemas = referenceSchemas;
    }

    public List<String> getTargetSchemas() {
        return targetSchemas;
    }

    public void setTargetSchemas(List<String> targetSchemas) {
        this.targetSchemas = targetSchemas;
    }
}
