package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.net.URI;
import java.util.Set;

import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT;


public interface CanonicalDataRange {
    
    /**
     * The URI of the datatype that implements this DataRange instance 
     * @return The URI for the type of the concrete implementation for this 
     *         DataRange.   
     */
    public URI getDatatypeURI();
    
    /**
     * The datatype implements this DataRange instance 
     * @return The datatype for the type of the concrete implementation for this 
     *         DataRange.   
     */
    public DT getDatatype();
    
    /**
     * Checks whether this data range is negated. 
     * @return true if negated and false otherwise.
     */
    public boolean isNegated();
    
    /**
     * Negate this data range, i.e., if the range was negated it is no longer 
     * negated afterwards and if it was not negated it will be negated afterwards. 
     */
    public void negate();
    
    /**
     * Checks whether this data range cannot contain values
     * @return true if the restrictions on this data range cause the 
     *         interpretation of it to be empty and false otherwise. 
     */
    public boolean isBottom();
    
    /**
     * Checks whether the interpretation of this data range is necessarily 
     * finite. 
     * @return true if this data range has only finite representations and false  
     *         otherwise. 
     */
    public boolean isFinite();
    
    /**
     * Constants that are representing the allowed assignments for this datatype 
     * restriction. The strings are String representations of the datatype that 
     * concretely implements the DataRange, e.g., if the concrete implementation 
     * has URI for integers, the returned strings have to be interpreted as 
     * integers.  
     * @return A set of constants that represent the current datatype 
     *         restrictions and are to be interpreted according to the datatype 
     *         URI of the concrete implementation of the DataRange. 
     */
    public Set<DataConstant> getOneOf();
    
    /**
     * Defines the datatype restriction in terms of a set of constants. 
     * @param oneOf A set of constants that are String representations of values 
     *        that are to be interpreted according to the datatype URI of the 
     *        concrete implementation of the DataRange 
     */
    public void setOneOf(Set<DataConstant> oneOf);
    
    /**
     * A set of values that are not in the interpretation of this datatype 
     * restriction.  
     * @param notOneOf A set of constants that are String representations of 
     *        values that are to be interpreted according to the datatype URI of 
     *        the concrete implementation of the DataRange 
     */
    public void setNotOneOf(Set<DataConstant> notOneOf);
    
    /**
     * Adds constant to the values that are not allowed in this data range. 
     * @param constant A constants that is a String representation of a value 
     *        that is to be interpreted according to the datatype URI of the 
     *        concrete implementation of the DataRange 
     * @return true if the notOneOf values did not already contain the given 
     *         constant and false otherwise
     */
    public boolean notOneOf(DataConstant constant);
    
    /**
     * Conjoins the facet restrictions from the given data range, provided it is 
     * of the same concrete implementation, i.e., we can conjoins the facets of 
     * an instance of DatatypeRestrictionString to another instance of 
     * DatatypeRestrictionString. 
     * @param range a data range that is of the same concrete implementation as 
     *        this. 
     * @throws IllegalArgumentException if the concrete realisation of the given 
     *         data range is different from the one for this data range.  
     */
    public void conjoinFacetsFrom(DataRange range) throws IllegalArgumentException;
    
    /**
     * Checks if the constant conforms to the facets
     * @param constant
     * @return true if no facets are present or if facets accept constant and 
     * false otherwise
     */
    public boolean facetsAccept(DataConstant constant);
    
    /**
     * Checks whether constant is the String representation of a value in the 
     * data range, where the string has to be interpreted according to the 
     * datatype URI. 
     * @param constant A String representation of a constant for this data range.
     * @return true if constant represents a value in the interpretation of this 
     *         data range and false otherwise. 
     */
    public boolean accepts(DataConstant constant);
    
    /**
     * Can be used to check whether the implementation supports the given set of 
     * datatypes, e.g., the implementation for integers supports long, 
     * unsignedByte, etc. 
     * @param datatypes a set of datatypes
     * @return true if the implementation can handle all the given datatypes and 
     * false otherwise
     */
    public boolean canHandleAll(Set<DT> datatypes);
    
    /**
     * Can be used to check whether a given constant is in principle in the 
     * range of this implementation. Whether it conforms to the facets is not 
     * checked. 
     * @param constant a constant
     * @return true if the constant has a datatype that in principle is ok for 
     * this datatype restriction and false otherwise
     */
    public boolean datatypeAccepts(DataConstant constant);
    
    /**
     * Checks whether the interpretation of this data range consists of at least 
     * n values. 
     * @param n 
     * @return true if the interpretation of this data range allows for at least 
     *         n values and false otherwise.  
     */
    public boolean hasMinCardinality(BigInteger n);
    
    /**
     * Compute the set of String representations of the allowed values for this 
     * data range, provided it is finite.   
     * @return The set of all Strings that are a representation of a value in 
     *         the interpretation according to the datatype URI of this 
     *         DataRange and null if the interpretation of this data range is 
     *         infinite. 
     */
    public BigInteger getEnumerationSize();
    
    /**
     * Computes the lexicographically smallest assignment given the 
     * restrictions. 
     * @return The lexicographically smallest assignment given the restrictions 
     *         or null if none exists. 
     */
    public DataConstant getSmallestAssignment();
}
