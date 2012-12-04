/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates,
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap;
import giny.model.Node;
import giny.model.Edge;

import java.util.*;
import java.io.File;

import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.Rank;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.baderlab.csplugins.enrichmentmap.view.SliderBarPanel;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;

/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 12:04:28 PM
 * <p>
 * Enrichment map Parameters define all the variables that are needed to create, manipulate, explore
 * and save an individual Enrichment map.  It stores the parsed files used to create it, all genes in the network,
 * all enrichment results, cuts-offs, expression files, and ranks
 */
public class EnrichmentMapParameters {
    
	//attribute prefix associated with this map
	private String attributePrefix = null;
	   
    //BULK EM required parameters
    //directory where the GMT and GCT(expression) files are found
    //needed for Bulk EM creation when the user has moved their files
    //around and they are not in the same directories that GSEA used.
    private String GMTDirName = null;
    private String GCTDirName = null;
    private String GSEAResultsDirName = null;
    private int lowerlimit = 1;
    private int upperlimit = 1;    
    //value specifying whether bulk EM is being used (needed to transfer file name to the network names)
    private boolean BulkEM = false;
    
    //DataSet Files
    private HashMap<String, DataSetFiles> files = new HashMap<String,DataSetFiles>();
    
    
    //FLAGS used in the analysis.
    //flag to indicate if the user has supplied a data/expression file
    private boolean Data = false;
    //flag to indicate if the user has supplied a second data/expression file
    private boolean Data2 = false;
    //flag to indicate there are two datasets.
    private boolean twoDatasets = false;
    //flag to indicate if there are FDR Q-values
    private boolean FDR = false;
    //add boolean to indicate whether the geneset files are EM specific gmt files
    //if they are the visual style changes slightly
    private boolean EMgmt = false;
    //a flag to indicate if the two expression files have the exact same set of genes
    private boolean twoDistinctExpressionSets = false;
    //flag to indicate if the results are from GSEA or generic or DAVID or other method
    private String method;
    //private boolean GSEA = true;
    //flag to indicate if the similarity cut off is jaccard or overlap
    private String similarityMetric;
    //edge type
    private String enrichment_edge_type;
    
    // DEFAULT VALUES for pvalue, qvalue, similarityCutOff and jaccard
    // will be assigned in the constructor
    //p-value cutoff
    private double pvalue;
    //fdr q-value cutoff
    private double qvalue;
    //similarity cutoff
    private double similarityCutOff;
    //value to store the constant needed for constructing the combined similarity metric
    private double combinedConstant;
    
    //pvalue slider bar
    private SliderBarPanel pvalueSlider;    
    //qvalue slider bar
    private SliderBarPanel qvalueSlider;    
    //similarity cutoff slider bar
    private SliderBarPanel similaritySlider;
    //list associated with slider bars.  As the slider bar is moved the removed nodes
    //and edges are stored in these lists
    //TODO: currently this is not stored in the session file.  So if the user moves the slider bar and saves a session the nodes or edges stored are lost.
    private ArrayList<Node> selectedNodes;
    private ArrayList<Edge> selectedEdges;
        
    //Heat map parameters for this enrichment map - user specified current normalization, and sorting.
    private HeatMapParameters hmParams;

    private Properties cyto_prop;
    private double defaultJaccardCutOff;
    private double defaultOverlapCutOff;
    private String defaultSimilarityMetric;
    private Boolean disable_heatmap_autofocus;
    private String defaultSortMethod;
    private String defaultDistanceMetric;

    //Constants
    final public static String ENRICHMENT_INTERACTION_TYPE = "Geneset_Overlap";
    final public static String ENRICHMENT_INTERACTION_TYPE_SET1 = "Geneset_Overlap_set1";
    final public static String ENRICHMENT_INTERACTION_TYPE_SET2 = "Geneset_Overlap_set2";
    //with more methods to support can't just have generic or gsea
    final public static String method_GSEA = "GSEA";
    final public static String method_generic = "generic";
    final public static String method_DAVID = "DAVID/BiNGO";
    //with more similarity metric can't use a boolean to reprensent them.
    final public static String SM_JACCARD = "JACCARD";
    final public static String SM_OVERLAP = "OVERLAP";
    final public static String SM_COMBINED = "COMBINED";    
    //Dataset phenotypes that are loaded in from the input panel
    private String Dataset1Phenotype1 = "UP";
    private String Dataset1Phenotype2 = "DOWN";
    private String Dataset2Phenotype1 = "UP";
    private String Dataset2Phenotype2 = "DOWN";
    
    //Create a hashmap to contain all the values in the rpt file.
    HashMap<String, String> props;
    /**
     * Default constructor to create a fresh instance.
     */
    public EnrichmentMapParameters() {
    		   
        //by default GSEA is the method.
        this.method = EnrichmentMapParameters.method_GSEA;

        //default Values from Cytoscape properties
        this.cyto_prop = CytoscapeInit.getProperties() ;      
        this.defaultJaccardCutOff      = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_jaccard", "0.25") );
        this.defaultOverlapCutOff      = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_overlap", "0.50") );
        this.defaultSimilarityMetric   = this.cyto_prop.getProperty("EnrichmentMap.default_similarity_metric",
                this.cyto_prop.getProperty("EnrichmentMap.default_overlap_metric", "overlap")); // looking for Property "EnrichmentMap.default_overlap_metric" for legacy reasons
        this.disable_heatmap_autofocus = Boolean.parseBoolean( this.cyto_prop.getProperty("EnrichmentMap.disable_heatmap_autofocus", "false") );

        //get the default heatmap sort algorithm
        this.defaultSortMethod = this.cyto_prop.getProperty("EnrichmentMap.default_sort_method", HeatMapParameters.sort_hierarchical_cluster);
        
        //get the default distance metric algorithm
        this.defaultDistanceMetric = this.cyto_prop.getProperty("EnrichmentMap.default_distance_metric", HeatMapParameters.pearson_correlation);

        //assign the defaults:
        this.pvalue = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_pvalue",  "0.005") );      
        this.qvalue = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_qvalue",  "0.1") );
       //get the default combined metric constant
        this.combinedConstant = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_combinedConstant", "0.50") );
        
        //choose Jaccard or Overlap as default
        if ( this.defaultSimilarityMetric .equalsIgnoreCase(SM_OVERLAP) ){
            this.similarityCutOff = this.defaultOverlapCutOff;
            this.similarityMetric = SM_OVERLAP;
        } else if( this.defaultSimilarityMetric .equalsIgnoreCase(SM_JACCARD)) {
            this.similarityCutOff = this.defaultJaccardCutOff;
            this.similarityMetric = SM_JACCARD;
        } else if( this.defaultSimilarityMetric .equalsIgnoreCase(SM_COMBINED)) {
            this.similarityCutOff = this.defaultJaccardCutOff;
            this.similarityMetric = SM_COMBINED;
        }
        this.enrichment_edge_type = ENRICHMENT_INTERACTION_TYPE;
        
        //reset all boolean values
        this.setFDR(false);
        this.setData(false);
        this.setData2(false);
        this.setTwoDatasets(false);
        this.setTwoDistinctExpressionSets(false);

        this.selectedNodes = new ArrayList<Node>();
        this.selectedEdges = new ArrayList<Edge>();

        //create the slider for this pvalue
        pvalueSlider = new SliderBarPanel(0,this.pvalue,"P-value Cutoff",this, EnrichmentMapVisualStyle.PVALUE_DATASET1, EnrichmentMapVisualStyle.PVALUE_DATASET2,ParametersPanel.summaryPanelWidth, false, this.pvalue);
        //create the slider for the qvalue
        qvalueSlider = new SliderBarPanel(0,this.qvalue,"Q-value Cutoff",this, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2,ParametersPanel.summaryPanelWidth, false, this.qvalue);
        //create the slider for the similarity cutoff
        similaritySlider = new SliderBarPanel(this.similarityCutOff,1,"Similarity Cutoff",this, EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT, EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT,ParametersPanel.summaryPanelWidth, true, this.similarityCutOff);
       
        //initialize first dataset
        this.files.put(EnrichmentMap.DATASET1, new DataSetFiles());
    }


    /**
     * Constructor to create enrichment map parameters from a cytoscape property file while restoring a Session
     * (property file is created when an enrichment map session is saved)
     *
     *  @param propFile     the name of the property file as a String
     */
    public EnrichmentMapParameters(String propFile){
        this();

        //Create a hashmap to contain all the values in the rpt file.
        this.props = new HashMap<String, String>();

        String [] lines = propFile.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] tokens = line.split("\t");
            //there should be two values on each line of the rpt file.
            if(tokens.length == 2 )
                this.props.put(tokens[0] ,tokens[1]);
        }
                
        this.attributePrefix = this.props.get("attributePrefix");

        if ( this.props.containsKey("enrichment_edge_type") )
            this.enrichment_edge_type = this.props.get("enrichment_edge_type");
        else
            this.enrichment_edge_type = "pp"; // legacy setting: assume it's "pp" if it's not specified in the props file 

      //boolean flags
        if((this.props.get("twoDatasets")).equalsIgnoreCase("true"))
            this.twoDatasets = true;
        //first two take care of old session files that only had JAccard or overlap metrics
        if((this.props.get("jaccard")).equalsIgnoreCase("false"))
            this.similarityMetric = SM_JACCARD;
        else if((this.props.get("jaccard")).equalsIgnoreCase("true"))
            this.similarityMetric = SM_OVERLAP;
        else if ((this.props.get("jaccard")).equalsIgnoreCase(SM_JACCARD))
            this.similarityMetric = SM_JACCARD;
        else if ((this.props.get("jaccard")).equalsIgnoreCase(SM_OVERLAP))
            this.similarityMetric = SM_OVERLAP;
        else if ((this.props.get("jaccard")).equalsIgnoreCase(SM_COMBINED))
            this.similarityMetric = SM_COMBINED;

        //get the combined constant
        if(this.props.get("CombinedConstant") != null)
            setCombinedConstant(Double.parseDouble(this.props.get("CombinedConstant")));
        else
            setCombinedConstant(0.5);

        //have to deal with legacy issue by switching from two methods to multiple
        if(this.props.get("GSEA") != null){
         if((this.props.get("GSEA")).equalsIgnoreCase("false"))
            this.method = EnrichmentMapParameters.method_generic;
         else
            this.method = EnrichmentMapParameters.method_GSEA;
        }
        if((this.props.get("Data")).equalsIgnoreCase("true"))
            this.Data = true;
        if((this.props.get("Data2")).equalsIgnoreCase("true"))
            this.Data2 = true;
        if((this.props.get("FDR")).equalsIgnoreCase("true"))
            this.FDR = true;

        if(this.props.get("method") != null)
            this.method = this.props.get("method");
        
      //cutoffs
        setPvalue(Double.parseDouble(this.props.get("pvalue")));
        setQvalue(Double.parseDouble(this.props.get("qvalue")));

        //older version had the similarityCutOff specified as jaccardCutOff.
        //need to check if this is an old session file
        String cutoff;
        if(this.props.get("jaccardCutOff") != null)
            cutoff = this.props.get("jaccardCutOff");
        else
            cutoff = this.props.get("similarityCutOff");

        if(cutoff != null){
            this.similarityCutOff = Double.parseDouble(cutoff);
        }
        
        if(!this.props.containsKey("Version"))
    			reconstruct_ver1(this.props);
        else if(Double.parseDouble(this.props.get("Version")) >= 2.0)
    			reconstruct_ver2(this.props);
    
        
        //create the slider for this pvalue
        pvalueSlider = new SliderBarPanel(0,this.pvalue,"P-value Cutoff",this, EnrichmentMapVisualStyle.PVALUE_DATASET1, EnrichmentMapVisualStyle.PVALUE_DATASET2,ParametersPanel.summaryPanelWidth, false, this.pvalue);

        //create the slider for the qvalue
        qvalueSlider = new SliderBarPanel(0,this.qvalue,"Q-value Cutoff",this, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2,ParametersPanel.summaryPanelWidth, false, this.pvalue);

        //create the slider for the similarity cutoff
        similaritySlider = new SliderBarPanel(this.similarityCutOff,1,"Similarity Cutoff",this, EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT, EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT,ParametersPanel.summaryPanelWidth, true, this.similarityCutOff);

    }
    
    /* 
     * Restore old session file
     */
    private void reconstruct_ver1(HashMap<String,String> props){
    		    	
    		//create a new dataset Files
    		DataSetFiles files1 = new DataSetFiles();
    		
        files1.setGMTFileName(props.get("GMTFileName"));

        if(props.get("expressionFileName1")!= null)
        		files1.setExpressionFileName(props.get("expressionFileName1"));
        //account for legacy issue with rename of parameter
        else
        		files1.setExpressionFileName(props.get("GCTFileName1"));

        files1.setEnrichmentFileName1(props.get("enerichmentDataset1FileName1"));
        files1.setEnrichmentFileName2(props.get("enrichmentDataset1FileName2"));
        files1.setGseaHtmlReportFile(props.get("gseaHtmlReportFileDataset1"));
        
        this.Dataset1Phenotype1 = props.get("dataset1Phenotype1");
        this.Dataset1Phenotype2 = props.get("dataset1Phenotype2");
        this.Dataset2Phenotype1 = props.get("dataset2Phenotype1");
        this.Dataset2Phenotype2 = props.get("dataset2Phenotype2");
        
        //rank files 1
        if(props.get("rankFile1")!= null){
            if((props.get("rankFile1")).equalsIgnoreCase("null") )
            		files1.setRankedFile(null);
            else
            		files1.setRankedFile(props.get("rankFile1"));
        }

        if(props.get("classFile1") != null){
            if((props.get("classFile1")).equalsIgnoreCase("null") )
            		files1.setClassFile(null);
            else
            		files1.setClassFile(props.get("classFile1"));
        }
        //Get the class one array from the prop file
/*        if(props.get("class1")!= null){
            if((props.get("class1")).equalsIgnoreCase("null") )
                this.temp_class1 = null;
            else{
                String classes = props.get("class1");
                String [] set = classes.split(",");

                this.temp_class1 = new String[set.length];

                for (int i = 0; i < set.length; i++) {
                    this.temp_class1[i] = set[i];
                }
            }
        }*/
        //add the first set of files
        this.files.put(EnrichmentMap.DATASET1, files1);
        
        if(twoDatasets){
        		DataSetFiles files2 = new DataSetFiles();
        	
            if(Data2){
                if(props.get("expressionFileName2")!= null)
                		files2.setExpressionFileName(props.get("expressionFileName2"));
                //account for legacy issue with rename of parameter
                else
                		files2.setExpressionFileName(props.get("GCTFileName2"));
            }
            	files2.setEnrichmentFileName1(props.get("enerichmentDataset2FileName1"));
            	files2.setEnrichmentFileName2(props.get("enrichmentDataset2FileName2"));
            //rankfile 2
            if(props.get("rankFile2") != null){
                if((props.get("rankFile2")).equalsIgnoreCase("null") )
                		files2.setRankedFile(null);
                 else
                	 	files2.setRankedFile(props.get("rankFile2"));
            }
            
            files2.setGseaHtmlReportFile(props.get("gseaHtmlReportFileDataset2"));
            if(props.get("classFile2")!= null){
                if((props.get("classFile2")).equalsIgnoreCase("null"))
                		files2.setClassFile(null);
                else
                		files2.setClassFile(props.get("classFile2"));
            }
            
           
            //Get the class two array from the prop file
/*            if(props.get("class2")!= null){
                if((props.get("class2")).equalsIgnoreCase("null") )
                	this.datasetFiles.get(EnrichmentMap.DATASET2).setTemp_class1(null);
                else{
                    String classes = props.get("class2");
                    String [] set = classes.split(",");

                    this.temp_class2 = new String[set.length];

                    for (int i = 0; i < set.length; i++) {
                        this.temp_class2[i] = set[i];
                    }
                }
            }*/
            //Add Dataset 2 files
            if(!this.files.containsKey(EnrichmentMap.DATASET2))
    				this.files.put(EnrichmentMap.DATASET2, files2);
        }
        
    }
    
    /*
     * Restore session file for version 2.0 and up
     */
    private void reconstruct_ver2(HashMap<String,String> props){

    		//Get the list of Datasets
    		if(props.containsKey("Datasets")){
    			String list_ds = props.get("Datasets");
    			list_ds.replace("[", "");
    			list_ds.replace("]", "");
    			String[] datasets = list_ds.split(",");
    			
    			//For Each Dataset populate the dataset files
    			for(int i = 0; i < datasets.length; i++){
    				String current_ds = datasets[i];
    				
    				DataSetFiles new_dsf = new DataSetFiles();
    				if(props.containsKey(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%GMTFileName"))
    					new_dsf.setGMTFileName(props.get(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%GMTFileName"));
    				if(props.containsKey(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%expressionFileName"))
    					new_dsf.setExpressionFileName(props.get(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%expressionFileName"));
    				if(props.containsKey(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%enrichmentFileName1"))
    					new_dsf.setEnrichmentFileName1(props.get(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%enrichmentFileName1"));
    				if(props.containsKey(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%enrichmentFileName2"))
    					new_dsf.setEnrichmentFileName2(props.get(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%enrichmentFileName2"));
    				if(props.containsKey(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%gseaHtmlReportFileDataset"))
    					new_dsf.setGseaHtmlReportFile(props.get(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%gseaHtmlReportFileDataset"));
    				if(props.containsKey(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%classFile"))
    					new_dsf.setClassFile(props.get(current_ds+ "%" + DataSetFiles.class.getSimpleName() + "%classFile"));
    				if(props.containsKey(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%RankedFile"))
    					new_dsf.setRankedFile(props.get(current_ds + "%" + DataSetFiles.class.getSimpleName() + "%RankedFile"));
    				
    				this.files.put(current_ds, new_dsf);
    				
    			}
    			
    		}
    }
    
   /* Method to copy the input contents of an enrichment map paremeter set
    * Only copy parameters specified in the input window
    *
    * Given -
    */

    /**
     * Method to copy the input contents of an enrichment map paremeter set
     * Only copy parameters specified in the input window
     *
     * @param copy -  a parameters set to copy from.
     */
    public void copyInputParameters(EnrichmentMapParameters copy){

        //We can only transfer the Network name if it is needed
        //for instance for Bulk EM creation.
        if(copy.isBulkEM()){
            //this.NetworkName = copy.getNetworkName();
            this.BulkEM = copy.isBulkEM();
        }
        
        //go through each dataset and copy it into the current em
        for(Iterator<?> i = copy.getFiles().keySet().iterator();i.hasNext();){
          	String ds = (String)i.next();
          	DataSetFiles new_ds = new DataSetFiles();
          	new_ds.copy(copy.getFiles().get(ds));
          	
          	//put the new dataset in
          	this.files.put(ds, new_ds);
        	
        }
        
        this.pvalue = copy.getPvalue();
        //create the slider for this pvalue
        pvalueSlider = new SliderBarPanel(0,this.pvalue,"P-value Cutoff",this, EnrichmentMapVisualStyle.PVALUE_DATASET1, EnrichmentMapVisualStyle.PVALUE_DATASET2,ParametersPanel.summaryPanelWidth, false, this.pvalue);

        this.qvalue = copy.getQvalue();
        //create the slider for the qvalue
        qvalueSlider = new SliderBarPanel(0,this.qvalue,"Q-value Cutoff",this, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2,ParametersPanel.summaryPanelWidth, false, this.qvalue);

        this.similarityCutOff = copy.getSimilarityCutOff();
        //create the slider for the similarity cutoff
        similaritySlider = new SliderBarPanel(this.similarityCutOff,1,"Similarity Cutoff",this, EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT, EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT,ParametersPanel.summaryPanelWidth,true, this.similarityCutOff);


        this.Data = copy.isData();
        this.Data2 = copy.isData2();
        this.twoDatasets = copy.isTwoDatasets();
        this.method = copy.getMethod();
        this.similarityMetric = copy.getSimilarityMetric();

       

        //field needed when calculating bulk enrichment maps.
        this.GMTDirName = copy.getGMTDirName();
        this.GCTDirName = copy.getGCTDirName();
        this.GSEAResultsDirName = copy.getGSEAResultsDirName();
        this.upperlimit = copy.getUpperlimit();
        this.lowerlimit = copy.getLowerlimit();
        
        //this.attributePrefix = copy.getAttributePrefix();


   }


   /**
    * Method to copy the contents of one set of parameters into another instance
    *
    * @param copy the parameters to copy from.
    */
   public void copy(EnrichmentMapParameters copy){

       //this.NetworkName = copy.getNetworkName();

	 //go through each dataset and copy it into the current em
       for(Iterator<?> i = copy.getFiles().keySet().iterator();i.hasNext();){
         	String ds = (String)i.next();
         	DataSetFiles new_ds = new DataSetFiles();
         	new_ds.copy(copy.getFiles().get(ds));
         	
         	//put the new dataset in
         	this.files.put(ds, new_ds);
       	
       }

        this.pvalue = copy.getPvalue();
        this.pvalueSlider = copy.getPvalueSlider();

        this.qvalue = copy.getQvalue();
        qvalueSlider = copy.getQvalueSlider();

        this.similarityCutOff = copy.getSimilarityCutOff();
        this.similaritySlider = copy.getSimilaritySlider();

        this.Data = copy.isData();
        this.Data2 = copy.isData2();
        this.twoDatasets = copy.isTwoDatasets();
        this.method = copy.getMethod();
        this.FDR = copy.isFDR();
        this.similarityMetric = copy.getSimilarityMetric();
        this.combinedConstant = copy.getCombinedConstant();
        this.twoDistinctExpressionSets =  copy.isTwoDistinctExpressionSets();

        this.selectedEdges = copy.getSelectedEdges();
        this.selectedNodes = copy.getSelectedNodes();
        this.hmParams = copy.getHmParams();
        this.enrichment_edge_type = copy.getEnrichment_edge_type();

       //field needed when calculating bulk enrichment maps.
        this.GMTDirName = copy.getGMTDirName();
        this.GCTDirName = copy.getGCTDirName();
        this.GSEAResultsDirName = copy.getGSEAResultsDirName();
        this.upperlimit = copy.getUpperlimit();
        this.lowerlimit = copy.getLowerlimit();

        //copy loadRpt, EGgmt and genesettypes
       this.EMgmt = copy.isEMgmt();
       
       this.attributePrefix = copy.getAttributePrefix();

       }

   /**
    * Checks all values of the EnrichmentMapInputPanel
    * to see if the current set of enrichment map parameters has the minimal amount
    * of information to run enrichment maps.
    *
    * If it is a GSEA run then gmt,gct,2 enrichment files are needed
    * If it is a generic run then gmt and 1 enrichment file is needed
    * if there are two datasets then depending on type it requires the same as above.
    *
    * @return A String with error messages (one error per line) or empty String if everything is okay.
    */
    public String checkMinimalRequirements(){
        String errors = "";
        
        //Go through each Dataset
        for(Iterator<?> i = this.getFiles().keySet().iterator();i.hasNext();){
         	String ds = (String)i.next();
         	DataSetFiles dsFiles = this.getFiles().get(ds);
        
         	//minimal for either analysis
         	//check to see if GMT is not null but everything else is
         	if((dsFiles.getEnrichmentFileName1() == null || dsFiles.getEnrichmentFileName1().equalsIgnoreCase(""))
                && (dsFiles.getEnrichmentFileName2() == null || dsFiles.getEnrichmentFileName2().equalsIgnoreCase("")) &&
                dsFiles.getGMTFileName() != null && !dsFiles.getGMTFileName().equalsIgnoreCase(""))
         		errors = "GMTONLY";
         	else{


         		if(dsFiles.getEnrichmentFileName1() == null || (dsFiles.getEnrichmentFileName1().equalsIgnoreCase("") || !checkFile(dsFiles.getEnrichmentFileName1())))
         			errors = errors + "Dataset 1, enrichment file 1 can not be found\n";


         		//GMT file is not required for David analysis
         		if(!this.method.equalsIgnoreCase(EnrichmentMapParameters.method_DAVID))
         			if(dsFiles.getGMTFileName() == null || dsFiles.getGMTFileName().equalsIgnoreCase("") || !checkFile(dsFiles.getGMTFileName()))
         				errors = errors + "GMT file can not be found \n";

         		// /GSEA inputs
         		if(this.method.equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
         			if(dsFiles.getEnrichmentFileName2() != null && (dsFiles.getEnrichmentFileName2().equalsIgnoreCase("") || !checkFile(dsFiles.getEnrichmentFileName2())))
         				errors = errors + "Dataset 1, enrichment file 2 can not be found\n";         			
         		}       		
         	}
        }
        
        //if there is more than one dataset
        //check to see if there are two datasets if the two gct files are the same
 		if((this.twoDatasets) && (this.files.get(EnrichmentMap.DATASET1).getExpressionFileName() != null) && (this.files.get(EnrichmentMap.DATASET1).getExpressionFileName().equalsIgnoreCase(this.files.get(EnrichmentMap.DATASET2).getExpressionFileName()))){
 			this.Data2 = false;
 			this.files.get(EnrichmentMap.DATASET2).setExpressionFileName("");
 		}
 		//if there are no expression files and this is a david analysis there is no way of telling if they are from the same gmt file so use different one
 		else if(this.method.equalsIgnoreCase(EnrichmentMapParameters.method_DAVID) && (this.files.get(EnrichmentMap.DATASET1).getExpressionFileName() != null) && (this.files.get(EnrichmentMap.DATASET2).getExpressionFileName() != null)){
 			this.setTwoDistinctExpressionSets(true);
 		}


        return errors;
    }

    /**
     * Check to see if the file is readable.
     *
     * @param filename - name of file to be checked
     * @return boolean - true if file is readable, false if it is not.
     */
    protected boolean checkFile(String filename){
           //check to see if the files exist and are readable.
           //if the file is unreadable change the color of the font to red
           //otherwise the font should be black.
           if(filename != null){
               File tempfile = new File(filename);
               if(!tempfile.canRead())
                   return false;
           }
           return true;
       }

    


     /**
     * String representation of EnrichmentMapParameters.
     * Is used to store the persistent Attributes as a property file in the Cytoscape Session file.
     *
     * @see java.lang.Object#toString()
     */
    public String toString(){
        StringBuffer paramVariables = new StringBuffer();

        paramVariables.append("enrichment_edge_type\t" + enrichment_edge_type + "\n");
        paramVariables.append("attributePrefix\t" + this.attributePrefix + "\n");
        
        //enrichment method.
        paramVariables.append("method\t" + this.method + "\n");

        //boolean flags
        paramVariables.append("twoDatasets\t" + twoDatasets + "\n");
        paramVariables.append("jaccard\t" + similarityMetric + "\n");

        //add the combined constant
        paramVariables.append("CombinedConstant" + combinedConstant + "\n");

        paramVariables.append("Data\t" + Data + "\n");
        paramVariables.append("Data2\t" + Data2 + "\n");
        paramVariables.append("FDR\t" + FDR + "\n");

        //cutoffs
        paramVariables.append("pvalue\t" + pvalue + "\n");
        paramVariables.append("qvalue\t" + qvalue + "\n");
        paramVariables.append("similarityCutOff\t" + similarityCutOff + "\n");

        //go through each dataset and copy it into the current em
        for(Iterator<?> i = this.files.keySet().iterator();i.hasNext();){
        		String ds = (String)i.next();
        		paramVariables.append(this.files.get(ds).toString(ds));
        	
        }
        
        //Write the classes/phenotypes as a comma separated list.
        //TODO:print classes out to prop file
        /*       if(!EM.getExpressionSets().isEmpty()){
            for(Iterator<String> k = EM.getExpressionSets().keySet().iterator();k.hasNext();){
            		String current_expression = k.next().toString();
            		GeneExpressionMatrix expression = EM.getExpression(current_expression);
            		if(expression != null){
                        String[] current_pheno = expression.getPhenotypes();
                        if (current_pheno != null){
                            StringBuffer output = new StringBuffer();
                            for(int j = 0; j < current_pheno.length;j++)
                                output.append(current_pheno[j] + ",");
                            paramVariables.append("class%"+current_expression+"\t" + output.toString() + "\n");
                        }
                    }
            }
        		
        }
*/
        return paramVariables.toString();
    }

    /**
     * go through Hashmap and print all the objects
     * @param map - any type of hashmap
     * @return string representation of the hash with the "key tab object newline" representation.
     */
    public String printHashmap(HashMap map ){
       StringBuffer result = new StringBuffer();
       if(map != null){
    	   		for(Iterator<String> i = (Iterator<String>) map.keySet().iterator(); i.hasNext(); ){
    	   			Object key = i.next();
    	   			result.append( key.toString() + "\t" + map.get(key).toString() + "\n");
    	   		}
    	   		return result.toString();
       }
       return null;
   }

    /**
     * This method repopulates a properly specified Hashmap from the given file and type.
     *
     * @param fileInput - file name where the hash map is stored.
     * @param type - the type of hashmap in the file.  The hashes are repopulated
     * based on the property file stored in the session file.  The property file
     * specifies the type of objects contained in each file and this is needed in order
     * to create the proper hash in the current set of parameters.
     * types are GeneSet(1), Genes(2), GSEAResult(3), GenericResult(4), Int to String (5), Ranking (6)
     * @return  properly constructed Hashmap repopulated from the specified file.
     */
    public HashMap repopulateHashmap(String fileInput, int type ){
        //TODO: for Type-safety we should generate and return individual HashMaps specifying the correct Types

        //Create a hashmap to contain all the values in the rpt file.
        HashMap newMap;

        boolean incrementRank = false;

        String [] lines = fileInput.split("\n");

        //GeneSet
        if(type == 1)
            newMap = new HashMap<String, GeneSet>();
        //Genes
        else if(type == 2)
            newMap = new HashMap<String, Integer>();
         //GSEAResults
        else if(type == 3)
            newMap = new HashMap<String, GSEAResult>();
        //GenericResults
        else if(type == 4)
            newMap = new HashMap<String, GenericResult>();
        //Hashmap key to gene
        else if(type == 5)
            newMap = new HashMap<Integer, String>();
        //Hashmap gene key to ranking
        else if(type == 6){
            newMap = new HashMap<Integer, Rank>();

            //issue with ranks from old session files where if there is a rank of -1
            // any heatmap that has that gene will be missing it.
            //ticket #152
            //scan the rank file to see if there is a negative rank
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                String[] tokens = line.split("\t");

                if(Integer.parseInt(tokens[3]) < 0)
                    incrementRank = true;
            }

        }
        //Hashmap rank to genekey
        else if(type ==7)
            newMap = new HashMap<Integer, Integer>();
        else
            newMap = new HashMap<Object, Object>();



        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] tokens = line.split("\t");

            //the first token is the key and the rest of the line is the object
            //depending on the type there is different data

            //Genesets
            if(type == 1)
                if(tokens.length>=3)
                    newMap.put(tokens[0], new GeneSet(tokens));

            //Genes
            if(type == 2){
            // need to control the Type of the Objects inside the HashMap, otherwise
            // we can't store the List of Genes to new Nodes and Edges in a restored Session
            // e.g. in in the Signature-Post-Analysis
                if(tokens.length > 1)
                    newMap.put(tokens[0], Integer.parseInt(tokens[1]));
                else
                    System.out.println(tokens.toString());
            }

            //GseaResult
            if(type == 3)
                newMap.put(tokens[0], new GSEAResult(tokens));

            if(type == 4)
                     //legacy issue, check to see if the line has enough items
                    //Generic results were being printed with blank lines between the results
                    //ignore blanks lines
                    if(tokens.length > 3)
                        newMap.put(tokens[0], new GenericResult(tokens));

            //HashMap Key 2 Genes
            if(type == 5) {
            // need to control the Type of the Objects inside the HashMap, otherwise
            // we can't store the List of Genes to new Nodes and Edges in a restored Session
            // e.g. in in the Signature-Post-Analysis
                if(tokens.length > 1)
                    newMap.put(Integer.parseInt(tokens[0]),tokens[1]);
                else
                    System.out.println(tokens.toString());
            }

            //Rankings
            if(type == 6){
                if(incrementRank){
                    Integer newRank = (Integer.parseInt(tokens[3]) + 1);
                    tokens[3] = newRank.toString();
                }
                newMap.put(Integer.parseInt(tokens[0]),new Rank(tokens));
            }
            //rank to gene id
            if(type == 7)
                newMap.put(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));

        }

        return newMap;
    }
    
    // Class Getters and Setters
    
    
    
    public String getSimilarityMetric() {
        return similarityMetric;
    }

    public String getAttributePrefix() {
		return attributePrefix;
	}

    //Set up the attributePrefix
    //The attribute prefix is based on the number of nextworks in cytoscape.
    //TODO:make attribute prefix independent on cytoscape
    public void setAttributePrefix(){
    		Set<CyNetwork> networks = Cytoscape.getNetworkSet();
        CyNetwork network;

        if(networks == null || networks.isEmpty())
        		this.attributePrefix = "EM1_";
        else{
        		//how many enrichment maps are there?
            int num_networks = 1;
            int max_prefix = 0;
            EnrichmentMapManager manager = EnrichmentMapManager.getInstance();
            //go through all the networks, check to see if they are enrichment maps
            //if they are then calculate the max EM_# and use the max number + 1 for the 
            // current attributes
            for(Iterator<CyNetwork> i = networks.iterator(); i.hasNext();){
            		CyNetwork current_network = i.next();
                String networkId = current_network.getIdentifier();
                if( manager.isEnrichmentMap(networkId) ) {//fails
                		num_networks++;
                    EnrichmentMap tmpMap = manager.getMap(networkId);
                    String tmpPrefix = tmpMap.getParams().getAttributePrefix();
                    tmpPrefix = tmpPrefix.replace("EM", "");
                    tmpPrefix = tmpPrefix.replace("_", "");
                    int tmpNum = Integer.parseInt(tmpPrefix);
                    if (tmpNum > max_prefix)
                    		max_prefix = tmpNum;
                     }
             }
            this.attributePrefix = "EM" + (max_prefix + 1) + "_";
           }
    }
    
	public void setAttributePrefix(String attributePrefix) {
		this.attributePrefix = attributePrefix;
	}


	public void setSimilarityMetric(String similarityMetric) {
        this.similarityMetric = similarityMetric;
    }


 /*   public String getGMTFileName() {

        return GMTFileName;
    }

    public void setGMTFileName(String GMTFileName) {
        this.GMTFileName = GMTFileName;
    }

    public String getExpressionFileName1() {
        return expressionFileName1;
    }

    public void setExpressionFileName1(String GCTFileName) {
        this.expressionFileName1 = GCTFileName;
    }


    public String getExpressionFileName2() {
        return expressionFileName2;
    }

    public void setExpressionFileName2(String GCTFileName) {
        this.expressionFileName2 = GCTFileName;
    }

    public String getEnrichmentDataset1FileName1() {
        return enrichmentDataset1FileName1;
    }

    public void setEnrichmentDataset1FileName1(String enrichmentDataset1FileName1) {
        this.enrichmentDataset1FileName1 = enrichmentDataset1FileName1;
    }

    public String getEnrichmentDataset1FileName2() {
        return enrichmentDataset1FileName2;
    }

    public void setEnrichmentDataset1FileName2(String enrichmentDataset1FileName2) {
        this.enrichmentDataset1FileName2 = enrichmentDataset1FileName2;
    }

    public String getEnrichmentDataset2FileName1() {
        return enrichmentDataset2FileName1;
    }

    public void setEnrichmentDataset2FileName1(String enrichmentDataset2FileName1) {
        this.enrichmentDataset2FileName1 = enrichmentDataset2FileName1;
    }

    public String getEnrichmentDataset2FileName2() {
        return enrichmentDataset2FileName2;
    }

    public void setEnrichmentDataset2FileName2(String enrichmentDataset2FileName2) {
        this.enrichmentDataset2FileName2 = enrichmentDataset2FileName2;
    }
*/
    public double getPvalue() {
        return pvalue;
    }

    public void setPvalue(double pvalue) {
        this.pvalue = pvalue;

    }

    public double getQvalue() {
        return qvalue;
    }

    public void setQvalue(double qvalue) {
        this.qvalue = qvalue;

    }

    public double getSimilarityCutOff() {
        return similarityCutOff;
    }

    public void setSimilarityCutOff(double similarityCutOff) {
        this.similarityCutOff = similarityCutOff;
    }
   

    /**
     * @return flag to indicate there are two datasets
     */
    public boolean isTwoDatasets() {
        return twoDatasets;
    }

    public void setTwoDatasets(boolean twoDatasets) {
        this.twoDatasets = twoDatasets;
    }

    /**
     * @return flag to indicate if the user has supplied a data/expression file
     */
    public boolean isData() {
        return Data;
    }

    public void setData(boolean data) {
        Data = data;
    }

    /**
     * @return flag to indicate if the user has supplied a second data/expression file
     */
    public boolean isData2() {
        return Data2;
    }

    public void setData2(boolean data2) {
        Data2 = data2;
    }

    public boolean isFDR() {
        return FDR;
    }

    public void setFDR(boolean FDR) {
        this.FDR = FDR;
    }

 /*   public String getClassFile1() {
        return classFile1;
    }

    public void setClassFile1(String classFile1) {
        this.classFile1 = classFile1;
    }

    public String getClassFile2() {
        return classFile2;
    }

    public void setClassFile2(String classFile2) {
        this.classFile2 = classFile2;
    }
*/
    /**
     * @param gseaHtmlReportFileDataset1 the gseaHtmlReportFileDataset1 to set
     */
/*    public void setGseaHtmlReportFileDataset1(String gseaHtmlReportFileDataset1) {
        this.gseaHtmlReportFileDataset1 = gseaHtmlReportFileDataset1;
    }


    /**
     * @return the gseaHtmlReportFileDataset1
     */
 /*   public String getGseaHtmlReportFileDataset1() {
        return gseaHtmlReportFileDataset1;
    }


    /**
     * @param gseaHtmlReportFileDataset2 the gseaHtmlReportFileDataset2 to set
     */
/*    public void setGseaHtmlReportFileDataset2(String gseaHtmlReportFileDataset2) {
        this.gseaHtmlReportFileDataset2 = gseaHtmlReportFileDataset2;
    }


    /**
     * @return the gseaHtmlReportFileDataset2
     */
 /*   public String getGseaHtmlReportFileDataset2() {
        return gseaHtmlReportFileDataset2;
    }
*/
    public SliderBarPanel getPvalueSlider() {
        return pvalueSlider;
    }

    public SliderBarPanel getQvalueSlider() {
        return qvalueSlider;
    }

    public SliderBarPanel getSimilaritySlider() {
        return similaritySlider;
    }

    public ArrayList<Node> getSelectedNodes() {
        return selectedNodes;
    }

    public ArrayList<Edge> getSelectedEdges() {
        return selectedEdges;
    }

    public void setSelectedNodes(ArrayList<Node> selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    public void setSelectedEdges(ArrayList<Edge> selectedEdges) {
        this.selectedEdges = selectedEdges;
    }

    public HeatMapParameters getHmParams() {
        return hmParams;
    }

    public void setHmParams(HeatMapParameters hmParams) {
        this.hmParams = hmParams;
    }

/*    public String getDataset1RankedFile() {
        return dataset1RankedFile;
    }

    public void setDataset1RankedFile(String dataset1RankedFile) {
        this.dataset1RankedFile = dataset1RankedFile;
    }

    public String getDataset2RankedFile() {
        return dataset2RankedFile;
    }

    public void setDataset2RankedFile(String dataset2RankedFile) {
        this.dataset2RankedFile = dataset2RankedFile;
    }
*/
   
    /*create a method to re-create rank to gene given the gene to rank*/
    public HashMap<Integer, Integer> getRank2geneDataset(HashMap<Integer,Rank> gene2rank){
        HashMap<Integer,Integer> rank2gene = new HashMap<Integer, Integer>();

        for(Iterator<?> i = gene2rank.keySet().iterator();i.hasNext();){
            Integer cur = (Integer)i.next();
            rank2gene.put(gene2rank.get(cur).getRank(), cur);
        }
        return rank2gene;
    }
    
    public void setDefaultJaccardCutOff(double defaultJaccardCutOff) {
        this.defaultJaccardCutOff = defaultJaccardCutOff;
    }

    public double getDefaultJaccardCutOff() {
        return defaultJaccardCutOff;
    }

    public void setDefaultOverlapCutOff(double defaultOverlapCutOff) {
        this.defaultOverlapCutOff = defaultOverlapCutOff;
    }

    public double getDefaultOverlapCutOff() {
        return defaultOverlapCutOff;
    }

    public boolean isDisableHeatmapAutofocus() {
        return this.disable_heatmap_autofocus ;
    }

    public void setDisableHeatmapAutofocus(boolean disable_heatmap_autofocus) {
        this.disable_heatmap_autofocus = disable_heatmap_autofocus;
    }
    
    public String getDefaultSortMethod() {
        return defaultSortMethod;
    }

    public void setDefaultSortMethod(String defaultSortMethod) {
        this.defaultSortMethod = defaultSortMethod;

        //also update the property in the cytoscape property file
        this.cyto_prop = CytoscapeInit.getProperties() ;
        cyto_prop.setProperty("EnrichmentMap.default_sort_method",defaultSortMethod);
    }

/*    public String[] getTemp_class1() {
        return temp_class1;
    }

    public void setTemp_class1(String[] temp_class1) {
        this.temp_class1 = temp_class1;
    }

    public String[] getTemp_class2() {
        return temp_class2;
    }

    public void setTemp_class2(String[] temp_class2) {
        this.temp_class2 = temp_class2;
    }
*/
    

    /**
     * @param enrichment_edge_type the enrichment_edge_type to set
     */
    public void setEnrichment_edge_type(String enrichment_edge_type) {
        this.enrichment_edge_type = enrichment_edge_type;
    }


    /**
     * @return the enrichment_edge_type
     */
    public String getEnrichment_edge_type() {
        return enrichment_edge_type;
    }

    public String getDefaultDistanceMetric() {
        return defaultDistanceMetric;
    }

    public void setDefaultDistanceMetric(String defaultDistanceMetric) {
        this.defaultDistanceMetric = defaultDistanceMetric;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isTwoDistinctExpressionSets() {
        return twoDistinctExpressionSets;
    }

    public String getGMTDirName() {
        return GMTDirName;
    }

    public void setTwoDistinctExpressionSets(boolean twoDistinctExpressionSets) {
        this.twoDistinctExpressionSets = twoDistinctExpressionSets;
    }

    

    public void setGMTDirName(String GMTDirName) {
        this.GMTDirName = GMTDirName;
    }

    public String getGCTDirName() {
        return GCTDirName;
    }

    public void setGCTDirName(String GCTDirName) {
        this.GCTDirName = GCTDirName;
    }

    public int getLowerlimit() {
        return lowerlimit;
    }

    public void setLowerlimit(int lowerlimit) {
        this.lowerlimit = lowerlimit;
    }

    public int getUpperlimit() {
        return upperlimit;
    }

    public void setUpperlimit(int upperlimit) {
        this.upperlimit = upperlimit;
    }

    public String getGSEAResultsDirName() {
        return GSEAResultsDirName;
    }

    public void setGSEAResultsDirName(String GSEAResultsDirName) {
        this.GSEAResultsDirName = GSEAResultsDirName;
    }

    public double getCombinedConstant() {
        return combinedConstant;
    }

    public void setCombinedConstant(double combinedConstant) {
        this.combinedConstant = combinedConstant;
    }

    public boolean isBulkEM() {
        return BulkEM;
    }

    public void setBulkEM(boolean bulkEM) {
        BulkEM = bulkEM;
    }

    public boolean isEMgmt(){
        return EMgmt;
    }

    public void setEMgmt(boolean flag){
        this.EMgmt = flag;
    }


	public String getDataset1Phenotype1() {
		return Dataset1Phenotype1;
	}


	public void setDataset1Phenotype1(String dataset1Phenotype1) {
		Dataset1Phenotype1 = dataset1Phenotype1;
	}


	public String getDataset1Phenotype2() {
		return Dataset1Phenotype2;
	}


	public void setDataset1Phenotype2(String dataset1Phenotype2) {
		Dataset1Phenotype2 = dataset1Phenotype2;
	}


	public String getDataset2Phenotype1() {
		return Dataset2Phenotype1;
	}


	public void setDataset2Phenotype1(String dataset2Phenotype1) {
		Dataset2Phenotype1 = dataset2Phenotype1;
	}


	public String getDataset2Phenotype2() {
		return Dataset2Phenotype2;
	}


	public void setDataset2Phenotype2(String dataset2Phenotype2) {
		Dataset2Phenotype2 = dataset2Phenotype2;
	}
	
	/*
	 * Get the GMT file
	 * If there are multiple dataset first check to see if the GMT files are the same
	 * If all the gmt files are the same then it returns the file name.
	 * If they are not the same then returns null
	 */
	public String getGMTFileName(){
		String gmt = "";
		for(Iterator<?> i = this.files.keySet().iterator();i.hasNext();){
			String current = (String)i.next();
			if(gmt.equalsIgnoreCase(""))
				gmt = this.files.get(current).getGMTFileName();
			else if(!gmt.equalsIgnoreCase(this.files.get(current).getGMTFileName()))
				gmt = null;
		}
		return gmt;
	}
	
	/*
	 * Method for Post Analysis
	 * Currently the post analysis assumes that there is only one GMT file
	 * It has to be assoicated with Dataset 1
	 * TODO:Get rid of this dependancy
	 */
	public void setGMTFileName(String name){
		this.files.get(EnrichmentMap.DATASET1).setGMTFileName(name);
	}


	public HashMap<String, String> getProps() {
		return props;
	}


	public void setProps(HashMap<String, String> props) {
		this.props = props;
	}


	public HashMap<String, DataSetFiles> getFiles() {
		return files;
	}


	public void setFiles(HashMap<String, DataSetFiles> files) {
		this.files = files;
	}
	
	public void addFiles(String name, DataSetFiles files){
		this.files.put(name, files);
	}
	
}