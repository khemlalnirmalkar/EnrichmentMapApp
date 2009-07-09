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
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.data.readers.TextFileReader;

import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Jun 25, 2009
 * Time: 2:40:14 PM
 */
public class EnrichmentResultFileReaderTask implements Task {

    private EnrichmentMapParameters params;

    private String EnrichmentResultFileName;

    private HashMap results ;

    // Keep track of progress for monitoring:

    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    public EnrichmentResultFileReaderTask(EnrichmentMapParameters params, TaskMonitor taskMonitor, String FileName, int dataset) {
        this(params, FileName, dataset);
        this.taskMonitor = taskMonitor;
    }

    public EnrichmentResultFileReaderTask(EnrichmentMapParameters params, String FileName, int dataset) {
        this.params = params;
        EnrichmentResultFileName = FileName;
        if(dataset == 1)
            results = params.getEnrichmentResults1();
        else if(dataset == 2)
            results = params.getEnrichmentResults2();


    }

    public void parse() {

         //open Enrichment Result file

         TextFileReader reader = new TextFileReader(EnrichmentResultFileName);
         reader.read();
         String fullText = reader.getText();

         String [] lines = fullText.split("\n");


         //figure out what type of enrichment results file.  Either it is a GSEA result
        //file or it is a generic result file.
        // Currently the headings in the GSEA Results file are:
        // NAME <tab> GS<br> follow link to MSigDB <tab> GS DETAILS <tab> SIZE <tab> ES <tab> NES <tab> NOM p-val <tab> FDR q-val <tab> FWER p-val <tab> RANK AT MAX <tab> LEADING EDGE
        // There are eleven headings.

        //ES and NES columns are specific to the GSEA format
        String header_line = lines[0];
        String [] tokens = header_line.split("\t");

        //check to see if there are exactly 11 columns
        if(tokens.length == 11){
            //check to see if the ES is the 5th column and that NES is the 6th column
            if((tokens[4].equalsIgnoreCase("ES")) && (tokens[5].equalsIgnoreCase("NES")))
                parseGSEAFile(lines);
            //it is possible that the file can have 11 columns but that it is still a generic file
            //if it doesn't specify ES and NES in the 5 and 6th columns
            else
              parseGenericFile(lines);
        }
        else{
             parseGenericFile(lines);
        }



    }


    public void parseGSEAFile(String[] lines){
        //skip the first line which just has the field names (start i=1)

        params.setFDR(true);

         int currentProgress = 0;
         int maxValue = lines.length;
         for (int i = 1; i < lines.length; i++) {
                String line = lines[i];

                String [] tokens = line.split("\t");
                int size = 0;
                double ES = 0.0;
                double NES = 0.0;
                double pvalue = 1.0;
                double FDRqvalue = 1.0;
                double FWERqvalue = 1.0;

                //The first column of the file is the name of the geneset
                String Name = tokens[0].toUpperCase().trim();

                //The fourth column is the size of the geneset
                if(tokens[3].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    size = Integer.parseInt(tokens[3]);
                }

                //The fifth column is the Enrichment score (ES)
                if(tokens[4].equalsIgnoreCase("")){
                    //do nothing
                }else{
                     ES = Double.parseDouble(tokens[4]);
                }

                //The sixth column is the Normalize Enrichment Score (NES)
                if(tokens[5].equalsIgnoreCase("")){
                    //do nothing
                }else{
                     NES = Double.parseDouble(tokens[5]);
                }

                //The seventh column is the nominal p-value
                if(tokens[6].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    pvalue = Double.parseDouble(tokens[6]);
                }

                //the eighth column is the FDR q-value
                if(tokens[7].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    FDRqvalue = Double.parseDouble(tokens[7]);
                }
                //the ninth column is the FWER q-value
                if(tokens[8].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    FWERqvalue = Double.parseDouble(tokens[8]);
                }
                GSEAResult result = new GSEAResult(Name, size, ES, NES,pvalue,FDRqvalue,FWERqvalue);


                // Calculate Percentage.  This must be a value between 0..100.
                int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                //  Estimate Time Remaining
                long timeRemaining = maxValue - currentProgress;
                if (taskMonitor != null) {
                        taskMonitor.setPercentCompleted(percentComplete);
                        taskMonitor.setStatus("Parsing Enrichment Results file " + currentProgress + " of " + maxValue);
                        taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                    }
                currentProgress++;

                results.put(Name, result);
            }
    }

    public void parseGenericFile(String [] lines){

        //Get the current genesets so we can check that all the results are in the geneset list
        //and put the size of the genesets into the visual style
        HashMap genesets = params.getFilteredGenesets();

        int currentProgress = 0;
        int maxValue = lines.length;
        boolean FDR = false;

         //skip the first line which just has the field names (start i=1)
        //check to see how many columns the data has
        String line = lines[0];
        String [] tokens = line.split("\t");
        int length = tokens.length;
        //if (length < 3)
           //not enough data in the file!!

        for (int i = 1; i < lines.length; i++) {
            line = lines[i];

            tokens = line.split("\t");

            double pvalue = 1.0;
            double FDRqvalue = 1.0;
            GenericResult result;
            int gs_size = 0;
            double NES = 1.0;

            //The first column of the file is the name of the geneset
            String name = tokens[0].toUpperCase();

            if(genesets.containsKey(name)){
                GeneSet current_set = (GeneSet)genesets.get(name);
                gs_size = current_set.getGenes().size();
            }

            String description = tokens[1].toUpperCase();

            //The third column is the nominal p-value
            if(tokens[2].equalsIgnoreCase("")){
                //do nothing
            }else{
                pvalue = Double.parseDouble(tokens[2]);
            }

            if(length > 3){
                //the fourth column is the FDR q-value
                if(tokens[3].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    FDRqvalue = Double.parseDouble(tokens[3]);
                    FDR = true;
                }
                //the fifth column is the phenotype and it should be an integer but the only important
                //part is the sign
                if(length > 4){
                    if(tokens[4].equalsIgnoreCase("")){

                    }else{
                        NES = Double.parseDouble(tokens[4]);
                    }
                    result = new GenericResult(name,description,pvalue,gs_size,FDRqvalue,NES);
                }
                else
                    result = new GenericResult(name,description,pvalue,gs_size,FDRqvalue);

            }
            else{
                result = new GenericResult(name, description,pvalue,gs_size);
            }

            // Calculate Percentage.  This must be a value between 0..100.
            int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
            //  Estimate Time Remaining
            long timeRemaining = maxValue - currentProgress;
            if (taskMonitor != null) {
                    taskMonitor.setPercentCompleted(percentComplete);
                    taskMonitor.setStatus("Parsing Generic Results file " + currentProgress + " of " + maxValue);
                    taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                }
            currentProgress++;

            results.put(name, result);
        }
        if(FDR)
            params.setFDR(FDR);
    }

    /**
     * Run the Task.
     */
    public void run() {
        parse();
    }

    /**
     * Non-blocking call to interrupt the task.
     */
    public void halt() {
        this.interrupted = true;
    }

     /**
     * Sets the Task Monitor.
     *
     * @param taskMonitor TaskMonitor Object.
     */
    public void setTaskMonitor(TaskMonitor taskMonitor) {
        if (this.taskMonitor != null) {
            throw new IllegalStateException("Task Monitor is already set.");
        }
        this.taskMonitor = taskMonitor;
    }

    /**
     * Gets the Task Title.
     *
     * @return human readable task title.
     */
    public String getTitle() {
        return new String("Parsing Enrichment Result file");
    }
}