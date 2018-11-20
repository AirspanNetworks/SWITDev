package EnodeB.Components.Log.Analzyers;

public enum StringAnalyzers {
    Contains("Contains"), NotContains("NotContains");
    private String name;

    StringAnalyzers(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
