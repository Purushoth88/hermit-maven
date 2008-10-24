package org.semanticweb.HermiT.reasoner;

public class DatatypesTest extends AbstractReasonerTest {

    public DatatypesTest(String name) {
        super(name);
    }
    
    public void testDatatypesUnsat1() throws Exception {
        String axioms = "SubClassOf(A DataAllValuesFrom(dp xsd:string)) "
                + "SubClassOf(A DataSomeValuesFrom(dp xsd:integer)) "
                + "ClassAssertion(a A) ";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }

    public void testDatatypesUnsat2() throws Exception {
        String axioms = "SubClassOf(DataHasValue(hasAge \"18\"^^xsd:integer) Eighteen) "
                + "ClassAssertion(a DataHasValue(hasAge \"18\"^^xsd:integer)) " 
                + "ClassAssertion(a ObjectComplementOf(Eighteen))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypesUnsat3() throws Exception {
        String axioms = "DataPropertyRange(hasAge xsd:integer) "
                + "ClassAssertion(a DataHasValue(hasAge \"aString\"^^xsd:string)) ";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypesUnsat4() throws Exception {
        String axioms = "FunctionalDataProperty(hasAge) "
            + "ClassAssertion(a DataHasValue(hasAge \"18\"^^xsd:integer)) " 
            + "ClassAssertion(a DataHasValue(hasAge \"19\"^^xsd:integer)) ";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypesSat1() throws Exception {
        String axioms = "SubClassOf(A DataHasValue(dp \"18\"^^xsd:integer)) "
                + "ClassAssertion(a A) "
                + "ClassAssertion(a DataAllValuesFrom(dp xsd:integer)) ";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(true);
    }
    
    public void testminInclMaxIncl() throws Exception {
        String axioms = "SubClassOf(A DataSomeValuesFrom(dp DatatypeRestriction(xsd:integer minInclusive \"18\"^^xsd:integer))) "
                + "SubClassOf(A DataAllValuesFrom(dp DatatypeRestriction(xsd:integer maxInclusive \"10\"^^xsd:integer))) " 
                + "ClassAssertion(a A)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }
    
    public void testDisjointDPsSatInteger() throws Exception {
        String axioms = "DisjointDataProperties(dp1 dp2) " 
                + "DataPropertyAssertion(dp1 a \"10\"^^xsd:integer)"
                + "SubClassOf(A DataSomeValuesFrom(dp2 DatatypeRestriction(xsd:integer minInclusive \"18\"^^xsd:integer maxInclusive \"18\"^^xsd:integer)))"
                + "ClassAssertion(a A)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(true);
    }

    public void testAllValuesFromInteger() throws Exception {
        String axioms = "SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"3\"^^xsd:integer \"4\"^^xsd:integer))) " 
                + "SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"2\"^^xsd:integer \"3\"^^xsd:integer)))"
                + "SubClassOf(A DataSomeValuesFrom(dp DatatypeRestriction(xsd:integer minInclusive \"4\"^^xsd:integer)))"
                + "ClassAssertion(a A)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }
    
    public void testAllValuesFromInteger2() throws Exception {
        String axioms = "SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"3\"^^xsd:integer \"4\"^^xsd:integer))) " 
                + "SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"2\"^^xsd:integer \"3\"^^xsd:integer)))"
                + "ClassAssertion(a A)"
                + "ClassAssertion(a DataSomeValuesFrom(dp DataComplementOf(DataOneOf(\"3\"^^xsd:integer))))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }
    
    public void testAllValuesFromDifferentTypes() throws Exception {
        String axioms = "SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"3\"^^xsd:integer \"4\"^^xsd:int))) " 
                + "SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"2\"^^xsd:short \"3\"^^xsd:integer)))"
                + "ClassAssertion(a A)"
                + "ClassAssertion(a DataSomeValuesFrom(dp DataComplementOf(DataOneOf(\"3\"^^xsd:integer))))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }
    
    public void testAllValuesFromDifferentTypes3() throws Exception {
        String axioms = "SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"3\"^^xsd:integer \"4\"^^xsd:int))) " 
                + "SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"2\"^^xsd:short \"3\"^^xsd:int)))"
                + "ClassAssertion(a A)"
                + "ClassAssertion(a DataSomeValuesFrom(dp DataOneOf(\"3\"^^xsd:integer)))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(true);
    }
    
    public void testParsingError() throws Exception {
        String axioms = "SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"3\"^^xsd:integer \"4a\"^^xsd:int))) " 
                + "SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"2\"^^xsd:short \"3\"^^xsd:integer)))"
                + "ClassAssertion(a A)"
                + "ClassAssertion(a DataSomeValuesFrom(dp DataComplementOf(DataOneOf(\"3\"^^xsd:integer))))";
        boolean exceptionThrown = false;
        try {
            loadOntologyWithAxioms(axioms, null);
        } catch (RuntimeException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }
    
    public void testAllValuesFromDifferentTypes2() throws Exception {
        String axioms = "SubClassOf(A DataAllValuesFrom(dp xsd:byte)) " 
                + "ClassAssertion(a A)"
                + "ClassAssertion(a DataSomeValuesFrom(dp DataOneOf(\"6542145\"^^xsd:integer)))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }
    
    public void testNegZero() throws Exception {
        String axioms = "SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"0\"^^xsd:integer))) " 
                + "ClassAssertion(a A)"
                + "ClassAssertion(a DataSomeValuesFrom(dp DataOneOf(\"-0\"^^xsd:integer)))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(true);
    }
    
    public void testNegZero2() throws Exception {
        String axioms = "SubClassOf(A DataAllValuesFrom(dp owl:real)) " 
                + "ClassAssertion(a A)"
                + "ClassAssertion(a DataSomeValuesFrom(dp DataOneOf(\"-0\"^^xsd:integer)))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(true);
    }
    
    public void testDisjointDPsUnsat() throws Exception {
        String axioms = "DisjointDataProperties(dp1 dp2) " 
                + "DataPropertyAssertion(dp1 a \"10\"^^xsd:integer)"
                + "SubClassOf(A DataSomeValuesFrom(dp2 DatatypeRestriction(xsd:integer minInclusive \"10\"^^xsd:integer maxInclusive \"10\"^^xsd:integer)))"
                + "ClassAssertion(a A)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }
    
    public void testDisjointDPsUnsatStringPattern() throws Exception {
        String axioms = "DisjointDataProperties(dp1 dp2) " 
                + "DataPropertyAssertion(dp1 a \"ab\"^^xsd:string)"
                + "DataPropertyAssertion(dp1 a \"ac\"^^xsd:string)"
                + "SubClassOf(A DataSomeValuesFrom(dp2 DatatypeRestriction(xsd:string pattern \"a(b|c)\"^^xsd:string)))"
                + "ClassAssertion(a A)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }
    
    public void testDateTime() throws Exception {
        String axioms = "SubClassOf(A DataSomeValuesFrom(dp DatatypeRestriction(xsd:dateTime minInclusive \"2008-10-08T20:44:11.656+0100\"^^xsd:dateTime))) "
                + "SubClassOf(A DataAllValuesFrom(dp DatatypeRestriction(xsd:dateTime maxInclusive \"2008-10-08T20:44:11.656+0100\"^^xsd:dateTime))) " 
                + "ClassAssertion(a A)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(true);
    }
    
    public void testDateTime2() throws Exception {
        String axioms = "SubClassOf(A DataHasValue(dp \"2007-10-08T20:44:11.656+0100\"^^xsd:dateTime)) "
                + "SubClassOf(A DataAllValuesFrom(dp DatatypeRestriction(xsd:dateTime minInclusive \"2008-07-08T20:44:11.656+0100\"^^xsd:dateTime maxInclusive \"2008-10-08T20:44:11.656+0100\"^^xsd:dateTime))) " 
                + "ClassAssertion(a A)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }
    
//    public void testNegZero2() throws Exception {
//        String axioms = "SubClassOf(A DataAllValuesFrom(dp owl:real)) " 
//                + "ClassAssertion(a A)"
//                + "ClassAssertion(a DataSomeValuesFrom(dp DataOneOf(\"-0\"^^xsd:float)))";
//        loadOntologyWithAxioms(axioms, null);
//        assertABoxSatisfiable(false);
//    }
//    
//  public void testNegZero2() throws Exception {
//  String axioms = "SubClassOf(A DataAllValuesFrom(dp owl:realPlus)) " 
//          + "ClassAssertion(a A)"
//          + "ClassAssertion(a DataSomeValuesFrom(dp DataOneOf(\"-0\"^^xsd:float)))";
//  loadOntologyWithAxioms(axioms, null);
//  assertABoxSatisfiable(true);
//}
//
//    public void testFloat1() throws Exception {
//        // +0 and -0 are not equal 
//        String axioms = "PropertyAssertion(Meg numberOfChildren \"+0\"^^xsd:float) "
//                + "PropertyAssertion(Meg numberOfChildren \"-0\"^^xsd:float) " 
//                + "FunctionalProperty(numberOfChildren)";
//        loadOntologyWithAxioms(axioms, null);
//        assertABoxSatisfiable(false);
//    }
}
