package fi.mystes.esbdoc;

/**
 * Created by mystes-am on 20.5.2015.
 */

import java.util.Set;

/**
 * Represents a single TestSuite
 */
public class TestSuite implements Comparable<TestSuite> {

    private final String name;
    private final Set<TestCase> cases;

    public TestSuite(String name, Set<TestCase> cases) {
        assertNotNull("name", name);
        assertNotNull("cases", cases);

        this.name = name;
        this.cases = cases;
    }

    private void assertNotNull(String paramName, Object param){
        if(null == param){
            throw new IllegalArgumentException("TestSuite constructor parameter '" + paramName + "' must be non-null!");
        }
    }

    public String getName() {
        return name;
    }

    public Set<TestCase> getTestCases() {
        return cases;
    }

    @Override
    public String toString() {
        return "{name: \"" + name + "\"}";
    }

    @Override
    public int compareTo(TestSuite that) {
        return this.name.compareTo(that.name);
    }

    @Override
    public boolean equals(Object object) {
        if(null == object){
            return false;
        }
        if(!(object instanceof TestSuite)){
            return false;
        }
        TestSuite that = (TestSuite) object;
        return this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return 37 * 37 + name.hashCode(); //TODO why like this?
    }
}