// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.*;

/**
 * Represents the atomic abstract role.
 */
public class AtomicAbstractRole extends AbstractRole implements DLPredicate {
    private static final long serialVersionUID=3766087788313643809L;

    protected String m_uri;
    
    protected AtomicAbstractRole(String uri) {
        m_uri=uri;
    }
    public String getURI() {
        return m_uri;
    }
    public int getArity() {
        return 2;
    }
    public AbstractRole getInverseRole() {
        return InverseAbstractRole.create(this);
    }
    public String toString(Namespaces namespaces) {
        return namespaces.abbreviateAsNamespace(m_uri);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtomicAbstractRole> s_interningManager=new InterningManager<AtomicAbstractRole>() {
        protected boolean equal(AtomicAbstractRole object1,AtomicAbstractRole object2) {
            return object1.m_uri.equals(object2.m_uri);
        }
        protected int getHashCode(AtomicAbstractRole object) {
            return object.m_uri.hashCode();
        }
    };
    
    public static AtomicAbstractRole create(String uri) {
        return s_interningManager.intern(new AtomicAbstractRole(uri));
    }
}