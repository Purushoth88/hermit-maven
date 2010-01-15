/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory
   
   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.cli;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.PrintWriter;
import java.net.URI;
import java.text.BreakIterator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.EntailmentChecker;
import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.monitor.Timer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

public class CommandLine {
    
    @SuppressWarnings("serial")
    protected static class UsageException extends IllegalArgumentException {
        public UsageException(String inMessage) {
            super(inMessage);
        }
    }


    protected static class StatusOutput {
        protected int level;
        public StatusOutput(int inLevel) {
            level=inLevel;
        }
        static public final int ALWAYS=0;
        static public final int STATUS=1;
        static public final int DETAIL=2;
        static public final int DEBUG=3;
        public void log(int inLevel,String message) {
            if (inLevel<=level)
                System.err.println(message);
        }
    }

    protected interface Action {
        void run(Reasoner hermit,Prefixes prefixes,StatusOutput status,PrintWriter output);
    }

    static protected class DumpPrefixesAction implements Action {
        public void run(Reasoner hermit,Prefixes prefixes,StatusOutput status,PrintWriter output) {
            output.println("Prefixes:");
            for (Map.Entry<String,String> e : prefixes.getPrefixIRIsByPrefixName().entrySet()) {
                output.println("\t"+e.getKey()+"\t"+e.getValue());
            }
            output.flush();
        }
    }

    static protected class DumpClausesAction implements Action {
        final String file;
        public DumpClausesAction(String fileName) {
            file=fileName;
        }
        public void run(Reasoner hermit,Prefixes prefixes,StatusOutput status,PrintWriter output) {
            if (file!=null) {
                if (file.equals("-")) {
                    output=new PrintWriter(System.out);
                }
                else {
                    java.io.FileOutputStream f;
                    try {
                        f=new java.io.FileOutputStream(file);
                    }
                    catch (java.io.FileNotFoundException e) {
                        throw new IllegalArgumentException("unable to open "+file+" for writing");
                    }
                    catch (SecurityException e) {
                        throw new IllegalArgumentException("unable to write to "+file);
                    }
                    output=new PrintWriter(f);
                }
            }
            output.println(hermit.getDLOntology().toString(prefixes));
            output.flush();
        }
    }

    static protected class ClassifyAction implements Action {
        final String file;
        public ClassifyAction(String fileName) {
            file=fileName;
        }
        public void run(Reasoner hermit,Prefixes prefixes,StatusOutput status,PrintWriter output) {
            status.log(2,"classifying...");
            hermit.classify();
            if (file!=null) {
                status.log(2,"writing taxonomy to "+file);
                if (file.equals("-")) {
                    output=new PrintWriter(System.out);
                }
                else {
                    java.io.FileOutputStream f;
                    try {
                        f=new java.io.FileOutputStream(file);
                    }
                    catch (java.io.FileNotFoundException e) {
                        throw new IllegalArgumentException("unable to open "+file+" for writing");
                    }
                    catch (SecurityException e) {
                        throw new IllegalArgumentException("unable to write to "+file);
                    }
                    output=new PrintWriter(f);
                }
                hermit.printHierarchies(output,true,false,false);
                output.flush();
            }
        }
    }

    static protected class SatisfiabilityAction implements Action {
        final String conceptName;
        public SatisfiabilityAction(String c) {
            conceptName=c;
        }
        public void run(Reasoner hermit,Prefixes prefixes,StatusOutput status,PrintWriter output) {
            status.log(2,"Checking satisfiability of '"+conceptName+"'");
            String conceptUri=prefixes.expandAbbreviatedIRI(conceptName);
            OWLClass owlClass=OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(conceptUri));
            if (!hermit.isDefined(owlClass)) {
                status.log(0,"Warning: class '"+conceptUri+"' was not declared in the ontology.");
            }
            boolean result=hermit.isSatisfiable(owlClass);
            output.println(conceptName+(result ? " is satisfiable." : " is not satisfiable."));
            output.flush();
        }
    }

    static protected class SupersAction implements Action {
        final String conceptName;
        boolean all;
        public SupersAction(String name,boolean getAll) {
            conceptName=name;
            all=getAll;
        }
        public void run(Reasoner hermit,Prefixes prefixes,StatusOutput status,PrintWriter output) {
            status.log(2,"Finding supers of '"+conceptName+"'");
            String conceptUri=prefixes.expandAbbreviatedIRI(conceptName);
            OWLClass owlClass=OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(conceptUri));
            if (!hermit.isDefined(owlClass)) {
                status.log(0,"Warning: class '"+conceptUri+"' was not declared in the ontology.");
            }
            NodeSet<OWLClass> classes;
            if (all) {
                classes=hermit.getSuperClasses(owlClass,false);
                output.println("All super-classes of '"+conceptName+"':");
            }
            else {
                classes=hermit.getSuperClasses(owlClass,false);
                output.println("Direct super-classes of '"+conceptName+"':");
            }
            for (Node<OWLClass> set : classes)
                for (OWLClass classInSet : set)
                    output.println("\t"+prefixes.abbreviateIRI(classInSet.getIRI().toString()));
            output.flush();
        }
    }

    static protected class SubsAction implements Action {
        final String conceptName;
        boolean all;
        public SubsAction(String name,boolean getAll) {
            conceptName=name;
            all=getAll;
        }
        public void run(Reasoner hermit,Prefixes prefixes,StatusOutput status,PrintWriter output) {
            status.log(2,"Finding subs of '"+conceptName+"'");
            String conceptUri=prefixes.expandAbbreviatedIRI(conceptName);
            OWLClass owlClass=OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(conceptUri));
            if (!hermit.isDefined(owlClass)) {
                status.log(0,"Warning: class '"+conceptUri+"' was not declared in the ontology.");
            }
            NodeSet<OWLClass> classes;
            if (all) {
                classes=hermit.getSubClasses(owlClass,false);
                output.println("All sub-classes of '"+conceptName+"':");
            }
            else {
                classes=hermit.getSubClasses(owlClass,true);
                output.println("Direct sub-classes of '"+conceptName+"':");
            }
            for (Node<OWLClass> set : classes)
                for (OWLClass classInSet : set)
                    output.println("\t"+prefixes.abbreviateIRI(classInSet.getIRI().toString()));
            output.flush();
        }
    }

    static protected class EquivalentsAction implements Action {
        final String conceptName;
        public EquivalentsAction(String name) {
            conceptName=name;
        }
        public void run(Reasoner hermit,Prefixes prefixes,StatusOutput status,PrintWriter output) {
            status.log(2,"Finding equivalents of '"+conceptName+"'");
            String conceptUri=prefixes.expandAbbreviatedIRI(conceptName);
            OWLClass owlClass=OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(conceptUri));
            if (!hermit.isDefined(owlClass)) {
                status.log(0,"Warning: class '"+conceptUri+"' was not declared in the ontology.");
            }
            Node<OWLClass> classes=hermit.getEquivalentClasses(owlClass);
            output.println("Classes equivalent to '"+conceptName+"':");
            for (OWLClass classInSet : classes)
                output.println("\t"+prefixes.abbreviateIRI(classInSet.getIRI().toString()));
            output.flush();
        }
    }
    
    static protected class EntailsAction implements Action {
        final IRI conclusionIRI;
        public EntailsAction(Configuration config,IRI conclusionIRI) {
            this.conclusionIRI=conclusionIRI;
        }
        public void run(Reasoner hermit,Prefixes prefixes,StatusOutput status,PrintWriter output) {
            status.log(2,"Checking whether the loaded ontology entails the conclusion ontology");
            OWLOntologyManager m=OWLManager.createOWLOntologyManager();
            try {
                OWLOntology conclusions = m.loadOntology(conclusionIRI);
                EntailmentChecker checker=new EntailmentChecker(hermit, m.getOWLDataFactory());
                boolean isEntailed=checker.entails(conclusions.getLogicalAxioms());
                output.println(isEntailed);
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }
            output.flush();
        }
    }

    static protected class TaxonomyAction implements Action {

        public TaxonomyAction() {
        }

        public void run(Reasoner hermit,Prefixes prefixes,StatusOutput status,PrintWriter output) {
            hermit.printHierarchies(output,true,false,false);
            output.flush();
        }
    }

    protected static final int
        kTime=1000,
        kDumpClauses=1001,
        kDumpRoleBox=1002,
        kDirectBlock=1003,
        kBlockStrategy=1004,
        kBlockCache=1005,
        kExpansion=1006,
        kBase=1007,
        kParser=1008,
        kDefaultPrefix=1009,
        kDumpPrefixes=1010,
        kTaxonomy=1011,
        kIgnoreUnsupportedDatatypes=1012,
        kPremise=1013,
        kConclusion=1014;

    protected static final String versionString;
    static {
        String version=CommandLine.class.getPackage().getImplementationVersion();
        if (version==null)
            version="<no version set>";
        versionString=version;
    }
    protected static final String usageString="Usage: hermit [OPTION]... IRI...";
    protected static final String[] helpHeader={
        "Perform reasoning on each OWL ontology IRI.",
        "Example: hermit -ds owl:Thing http://hermit-reasoner.org/2008/test.owl",
        "    (prints direct subclasses of owl:Thing within the test ontology)",
        "Example: hermit --premise http://km.aifb.uni-karlsruhe.de/projects/owltests/index.php/Special:GetOntology/New-Feature-DisjointObjectProperties-002?m=p --conclusion http://km.aifb.uni-karlsruhe.de/projects/owltests/index.php/Special:GetOntology/New-Feature-DisjointObjectProperties-002?m=c --checkEntailment",
        "    (prints direct subclasses of owl:Thing within the test ontology)",
        "",
        "Both relative and absolute ontology IRIs can be used. Relative IRIs",
        "are resolved with respect to the current directory (i.e. local file",
        "names are valid IRIs); this behavior can be changed with the '--base'",
        "option.",
        "",
        "Classes and properties are identified using functional-syntax-style",
        "identifiers: names not containing a colon are resolved against the",
        "ontology's default prefix; otherwise the portion of the name",
        "preceding the colon is treated as a prefix prefix. Use of",
        "prefixes can be controlled using the -p, -N, and --prefix",
        "options. Alternatively, classes and properties can be identified with",
        "full IRIs by enclosing the IRI in <angle brackets>.",
        "",
        "By default, ontologies are simply retrieved and parsed. For more",
        "interesting reasoning, set one of the -c/-k/-s/-S/-e/-U options."
    };
    protected static final String[] footer={
        "HermiT is a product of Oxford University.",
        "Visit <http://hermit-reasoner.org/> for details."
    };
    protected static final String
        kMisc="Miscellaneous",
        kActions="Actions",
        kParsing="Parsing and loading",
        kPrefixes="Prefix name and IRI",
        kAlgorithm="Algorithm settings (expert users only!)",
        kInternals="Internals and debugging (unstable)";

    protected static final Option[] options=new Option[] {
        // meta:
        new Option('h',"help",kMisc,"display this help and exit"),
        new Option('V',"version",kMisc,"display version information and exit"),
        new Option('v',"verbose",kMisc,false,"AMOUNT","increase verbosity by AMOUNT levels (default 1)"),
        new Option('q',"quiet",kMisc,false,"AMOUNT","decrease verbosity by AMOUNT levels (default 1)"),
        new Option('o',"output",kMisc,true,"FILE","write output to FILE"),
        new Option(kPremise,"premise",kMisc,true,"PREMISE","set the premise ontology to PREMISE"),
        new Option(kConclusion,"conclusion",kMisc,true,"CONCLUSION","set the conclusion ontology to CONCLUSION"),
        
        // actions:
        new Option('l',"load",kActions,"parse and preprocess ontologies (default action)"),
        new Option('c',"classify",kActions,false,"FILE","classify ontology, optionally writing taxonomy to FILE (use - for standard out)"),
        new Option('k',"consistency",kActions,false,"CLASS","check satisfiability of CLASS (default owl:Thing)"),
        new Option('d',"direct",kActions,"restrict next subs/supers call to only direct sub/superclasses"),
        new Option('s',"subs",kActions,true,"CLASS","output classes subsumed by CLASS (or only direct subs if following --direct)"),
        new Option('S',"supers",kActions,true,"CLASS","output classes subsuming CLASS (or only direct supers if following --direct)"),
        new Option('e',"equivalents",kActions,true,"CLASS","output classes equivalent to CLASS"),
        new Option('U',"unsatisfiable",kActions,"output unsatisfiable classes (equivalent to --equivalents=owl:Nothing)"),
        new Option(kDumpPrefixes,"print-prefixes",kActions,"output prefix names available for use in identifiers"),
        new Option('E',"checkEntailment",kActions,"check whether the premise (option premise) ontology entails the conclusion ontology (option conclusion)"),
        
        new Option('N',"no-prefixes",kPrefixes,"do not abbreviate or expand identifiers using prefixes defined in input ontology"),
        new Option('p',"prefix",kPrefixes,true,"PN=IRI","use PN as an abbreviation for IRI in identifiers"),
        new Option(kDefaultPrefix,"prefix",kPrefixes,true,"IRI","use IRI as the default identifier prefix"),

        // algorithm tweaks:
        new Option(kDirectBlock,"block-match",kAlgorithm,true,"TYPE","identify blocked nodes with TYPE blocking; supported values are 'single', 'pairwise', 'pairwise-reflexive', and 'optimal' (default 'optimal')"),
        new Option(kIgnoreUnsupportedDatatypes,"ignoreUnsupportedDatatypes",kAlgorithm,"ignore unsupported datatypes"),

        // internals:
        new Option(kDumpClauses,"dump-clauses",kInternals,false,"FILE","output DL-clauses to FILE (default stdout)"),
        new Option(kTaxonomy,"taxonomy",kInternals,"output the taxonomy")
    };

    public static void main(String[] argv) {
        try {
            int verbosity=1;
            boolean ignoreOntologyPrefixes=false;
            Prefixes prefixes=new Prefixes();
            PrintWriter output=new PrintWriter(System.out);
            Collection<Action> actions=new LinkedList<Action>();
            URI base;
            IRI conclusionIRI=null;
            Configuration config=new Configuration();
            boolean doAll=true;
            try {
                base=new URI("file",System.getProperty("user.dir")+"/",null);
            }
            catch (java.net.URISyntaxException e) {
                throw new RuntimeException("unable to create default IRI base");
            }
            Collection<IRI> ontologies=new LinkedList<IRI>();
            boolean didSomething=false;
            {
                Getopt g=new Getopt("hermit",argv,Option.formatOptionsString(options),Option.createLongOpts(options));
                g.setOpterr(false);
                int opt;
                while ((opt=g.getopt())!=-1) {
                    switch (opt) {
                    // meta:
                    case 'h': {
                        System.out.println(usageString);
                        for (String s : helpHeader)
                            System.out.println(s);
                        System.out.println(Option.formatOptionHelp(options));
                        for (String s : footer)
                            System.out.println(s);
                        System.exit(0);
                        didSomething=true;
                    }
                        break;
                    case 'V': {
                        System.out.println(versionString);
                        for (String s : footer)
                            System.out.println(s);
                        System.exit(0);
                        didSomething=true;
                    }
                        break;
                    case 'v': {
                        String arg=g.getOptarg();
                        if (arg==null) {
                            verbosity+=1;
                        }
                        else
                            try {
                                verbosity+=Integer.parseInt(arg,10);
                            }
                            catch (NumberFormatException e) {
                                throw new UsageException("argument to --verbose must be a number");
                            }
                    }
                        break;
                    case 'q': {
                        String arg=g.getOptarg();
                        if (arg==null) {
                            verbosity-=1;
                        }
                        else
                            try {
                                verbosity-=Integer.parseInt(arg,10);
                            }
                            catch (NumberFormatException e) {
                                throw new UsageException("argument to --verbose must be a number");
                            }
                    }
                        break;
                    case 'o': {
                        String arg=g.getOptarg();
                        if (arg==null)
                            throw new UsageException("--output requires an argument");
                        if (arg.equals("-"))
                            output=new PrintWriter(System.out);
                        else {
                            java.io.FileOutputStream f;
                            try {
                                f=new java.io.FileOutputStream(arg);
                            }
                            catch (java.io.FileNotFoundException e) {
                                throw new IllegalArgumentException("unable to open "+arg+" for writing");
                            }
                            catch (SecurityException e) {
                                throw new IllegalArgumentException("unable to write to "+arg);
                            }
                            output=new PrintWriter(f);
                        }
                    }
                        break;
                    case kPremise: {
                        String arg=g.getOptarg();
                        if (arg==null)
                            throw new UsageException("--premise requires a IRI as argument");
                        else {
                            ontologies.add(IRI.create(arg));
                        }
                    }
                        break;
                    case kConclusion: {
                        String arg=g.getOptarg();
                        if (arg==null)
                            throw new UsageException("--conclusion requires a IRI as argument");
                        else {
                            conclusionIRI=IRI.create(arg);
                        }
                    }
                        break;
                    // actions:
                    case 'l': {
                        // load is a no-op; loading happens no matter what the user asks
                    }
                        break;
                    case 'c': {
                        actions.add(new ClassifyAction(g.getOptarg()));
                    }
                        break;
                    case 'k': {
                        String arg=g.getOptarg();
                        if (arg==null) {
                            arg="<http://www.w3.org/2002/07/owl#Thing>";
                        }
                        actions.add(new SatisfiabilityAction(arg));
                    }
                        break;
                    case 'd': {
                        doAll=false;
                    }
                        break;
                    case 's': {
                        String arg=g.getOptarg();
                        actions.add(new SubsAction(arg,doAll));
                        doAll=true;
                    }
                        break;
                    case 'S': {
                        String arg=g.getOptarg();
                        actions.add(new SupersAction(arg,doAll));
                        doAll=true;
                    }
                        break;
                    case 'e': {
                        String arg=g.getOptarg();
                        actions.add(new EquivalentsAction(arg));
                    }
                        break;
                    case 'U': {
                        actions.add(new EquivalentsAction("<http://www.w3.org/2002/07/owl#Nothing>"));
                    }
                        break;
                    case 'E': {
                        if (conclusionIRI!=null) 
                            actions.add(new EntailsAction(config, conclusionIRI));
                    }
                        break;
                    case kDumpPrefixes: {
                        actions.add(new DumpPrefixesAction());
                    }
                        break;
                    case 'N': {
                        ignoreOntologyPrefixes=true;
                    }
                        break;
                    case 'p': {
                        String arg=g.getOptarg();
                        int eqIndex=arg.indexOf('=');
                        if (eqIndex==-1) {
                            throw new IllegalArgumentException("the prefix declaration '"+arg+"' is not of the form PN=IRI.");
                        }
                        prefixes.declarePrefix(arg.substring(0,eqIndex),arg.substring(eqIndex+1));
                    }
                        break;
                    case kDefaultPrefix: {
                        String arg=g.getOptarg();
                        prefixes.declareDefaultPrefix(arg);
                    }
                        break;
                    case kBase: {
                        String arg=g.getOptarg();
                        try {
                            base=new URI(arg);
                        }
                        catch (java.net.URISyntaxException e) {
                            throw new IllegalArgumentException("'"+arg+"' is not a valid base URI.");
                        }
                    }
                        break;

                    case kDirectBlock: {
                        String arg=g.getOptarg();
                        if (arg.toLowerCase().equals("pairwise")) {
                            config.directBlockingType=Configuration.DirectBlockingType.PAIR_WISE;
                        }
                        else if (arg.toLowerCase().equals("single")) {
                            config.directBlockingType=Configuration.DirectBlockingType.SINGLE;
                        }
                        else if (arg.toLowerCase().equals("optimal")) {
                            config.directBlockingType=Configuration.DirectBlockingType.OPTIMAL;
                        }
                        else
                            throw new UsageException("unknown direct blocking type '"+arg+"'; supported values are 'pairwise', 'single', and 'optimal'");
                    }
                        break;
                    case kBlockStrategy: {
                        String arg=g.getOptarg();
                        if (arg.toLowerCase().equals("anywhere")) {
                            config.blockingStrategyType=Configuration.BlockingStrategyType.ANYWHERE;
                        }
                        else if (arg.toLowerCase().equals("ancestor")) {
                            config.blockingStrategyType=Configuration.BlockingStrategyType.ANCESTOR;
                        }
                        else
                            throw new UsageException("unknown blocking strategy type '"+arg+"'; supported values are 'ancestor' and 'anywhere'");
                    }
                        break;
                    case kBlockCache: {
                        String arg=g.getOptarg();
                        if (arg.toLowerCase().equals("on")) {
                            config.blockingSignatureCacheType=Configuration.BlockingSignatureCacheType.CACHED;
                        }
                        else if (arg.toLowerCase().equals("off")) {
                            config.blockingSignatureCacheType=Configuration.BlockingSignatureCacheType.NOT_CACHED;
                        }
                        else
                            throw new UsageException("unknown blocking cache type '"+arg+"'; supported values are 'on' and 'off'");
                    }
                        break;
                    case kExpansion: {
                        String arg=g.getOptarg();
                        if (arg.toLowerCase().equals("creation")) {
                            config.existentialStrategyType=Configuration.ExistentialStrategyType.CREATION_ORDER;
                        }
                        else if (arg.toLowerCase().equals("el")) {
                            config.existentialStrategyType=Configuration.ExistentialStrategyType.EL;
                        }
                        else if (arg.toLowerCase().equals("reuse")) {
                            config.existentialStrategyType=Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE;
                        }
                        else
                            throw new UsageException("unknown existential strategy type '"+arg+"'; supported values are 'creation', 'el', and 'reuse'");
                    }
                        break;
                    case kIgnoreUnsupportedDatatypes: {
                        config.ignoreUnsupportedDatatypes=true;
                    }
                        break;
                    case kDumpClauses: {
                        actions.add(new DumpClausesAction(g.getOptarg()));
                    }
                        break;
                    case kTaxonomy: {
                        actions.add(new TaxonomyAction());
                    }
                        break;
                    default: {
                        if (g.getOptopt()!=0) {
                            throw new UsageException("invalid option -- "+(char)g.getOptopt());
                        }
                        throw new UsageException("invalid option");
                    }
                    } // end option switch
                } // end loop over options
                for (int i=g.getOptind();i<argv.length;++i) {
                    try {
                        ontologies.add(IRI.create(base.resolve(argv[i])));
                    }
                    catch (IllegalArgumentException e) {
                        throw new UsageException(argv[i]+" is not a valid ontology name");
                    }
                }
            } // done processing arguments
            StatusOutput status=new StatusOutput(verbosity);
            if (verbosity>3)
                config.monitor=new Timer(new PrintWriter(System.err));
            for (IRI ont : ontologies) {
                didSomething=true;
                status.log(2,"Processing "+ont.toString());
                status.log(2,String.valueOf(actions.size())+" actions");
                try {
                    long startTime=System.currentTimeMillis();
                    OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
                    OWLOntology ontology=ontologyManager.loadOntology(ont);
                    long parseTime=System.currentTimeMillis()-startTime;
                    status.log(2,"Ontology parsed in "+String.valueOf(parseTime)+" msec.");
                    startTime=System.currentTimeMillis();
                    Reasoner hermit=new Reasoner(config,ontologyManager,ontology);
                    long loadTime=System.currentTimeMillis()-startTime;
                    status.log(2,"Reasoner created in "+String.valueOf(loadTime)+" msec.");
                    if (!ignoreOntologyPrefixes)
                        prefixes.addPrefixes(hermit.getPrefixes());
                    for (Action action : actions) {
                        status.log(2,"Doing action...");
                        startTime=System.currentTimeMillis();
                        action.run(hermit,prefixes,status,output);
                        long actionTime=System.currentTimeMillis()-startTime;
                        status.log(2,"...action completed in "+String.valueOf(actionTime)+" msec.");
                    }
                }
                catch (org.semanticweb.owlapi.model.OWLException e) {
                    System.err.println("It all went pear-shaped: "+e.getMessage());
                }
            }
            if (!didSomething)
                throw new UsageException("No ontologies given.");
        }
        catch (UsageException e) {
            System.err.println(e.getMessage());
            System.err.println(usageString);
            System.err.println("Try 'hermit --help' for more information.");
        }
    }
}

enum Arg { NONE,OPTIONAL,REQUIRED }

class Option {
    protected int optChar;
    protected String longStr;
    protected String group;
    protected Arg arg;
    protected String metavar;
    protected String help;
    
    public Option(int inChar,String inLong,String inGroup,String inHelp) {
        optChar=inChar;
        longStr=inLong;
        group=inGroup;
        arg=Arg.NONE;
        help=inHelp;
    }
    public Option(int inChar,String inLong,String inGroup,boolean argRequired,String inMetavar,String inHelp) {
        optChar=inChar;
        longStr=inLong;
        group=inGroup;
        arg=(argRequired ? Arg.REQUIRED : Arg.OPTIONAL);
        metavar=inMetavar;
        help=inHelp;
    }
    public static LongOpt[] createLongOpts(Option[] opts) {
        LongOpt[] out=new LongOpt[opts.length];
        for (int i=0;i<opts.length;++i) {
            out[i]=new LongOpt(opts[i].longStr,(opts[i].arg==Arg.NONE ? LongOpt.NO_ARGUMENT : opts[i].arg==Arg.OPTIONAL ? LongOpt.OPTIONAL_ARGUMENT : LongOpt.REQUIRED_ARGUMENT),null,opts[i].optChar);
        }
        return out;
    }

    public String getLongOptExampleStr() {
        if (longStr==null||longStr.equals(""))
            return "";
        return new String("--"+longStr+(arg==Arg.NONE ? "" : arg==Arg.OPTIONAL ? "[="+metavar+"]" : "="+metavar));
    }

    public static String formatOptionHelp(Option[] opts) {
        StringBuffer out=new StringBuffer();
        int fieldWidth=0;
        for (Option o : opts) {
            int curWidth=o.getLongOptExampleStr().length();
            if (curWidth>fieldWidth)
                fieldWidth=curWidth;
        }
        String curGroup=null;
        for (Option o : opts) {
            if (o.group!=curGroup) {
                curGroup=o.group;
                out.append(System.getProperty("line.separator"));
                if (o.group!=null) {
                    out.append(curGroup+":");
                    out.append(System.getProperty("line.separator"));
                }
            }
            if (o.optChar<256) {
                out.append("  -");
                out.appendCodePoint(o.optChar);
                if (o.longStr!=null&&o.longStr!="") {
                    out.append(", ");
                }
                else {
                    out.append("  ");
                }
            }
            else {
                out.append("      ");
            }
            int fieldLeft=fieldWidth+1;
            if (o.longStr!=null&&o.longStr!="") {
                String s=o.getLongOptExampleStr();
                out.append(s);
                fieldLeft-=s.length();
            }
            for (;fieldLeft>0;--fieldLeft)
                out.append(' ');
            out.append(breakLines(o.help,80,6+fieldWidth+1));
            out.append(System.getProperty("line.separator"));
        }
        return out.toString();
    }
    
    public static String formatOptionsString(Option[] opts) {
        StringBuffer out=new StringBuffer();
        for (Option o : opts) {
            if (o.optChar<256) {
                out.appendCodePoint(o.optChar);
                switch (o.arg) {
                case REQUIRED:
                    out.append(":");
                    break;
                case OPTIONAL:
                    out.append("::");
                    break;
                case NONE:
                    break;
                }
            }
        }
        return out.toString();
    }
    
    protected static String breakLines(String str,int lineWidth,int indent) {
        StringBuffer out=new StringBuffer();
        BreakIterator i=BreakIterator.getLineInstance();
        i.setText(str);
        int curPos=0;
        int curLinePos=indent;
        int next=i.first();
        while (next!=BreakIterator.DONE) {
            String curSpan=str.substring(curPos,next);
            if (curLinePos+curSpan.length()>lineWidth) {
                out.append(System.getProperty("line.separator"));
                for (int j=0;j<indent;++j)
                    out.append(" ");
                curLinePos=indent;
            }
            out.append(curSpan);
            curLinePos+=curSpan.length();
            curPos=next;
            next=i.next();
        }
        return out.toString();
    }
}
