import cytoscape.util.CyFileFilter;
import cytoscape.util.FileUtil;

import javax.swing.*;
import java.io.File;
import java.awt.*;

/**
 * Created by
 * User: risserlin
 * Date: Jan 7, 2009
 * Time: 4:01:05 PM
 */

/**
 * Class that extends JPanel and takes care of the GUI-objects
 * Allows users to load in geneset GMT file, GSEA results for Up and DOWN genesets
 *
 * */

public class InputFilesPanel extends JDialog {


    /*--------------------------------------------------------------
    FIELDS.
    --------------------------------------------------------------*/

    /**
     * the height of the panel
     */
    private final int DIM_HEIGHT = 250;
    /**
     * the width of the panel
     */
    private final int DIM_WIDTH = 350;

    private boolean status = false;

    /* components of the dialog */
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton importButton;

    //Genesets file related components
    private javax.swing.JTextField GMTFileNameTextField;
    private javax.swing.JButton selectGMTFileButton;

    private javax.swing.JTextField GCTFileNameTextField;
    private javax.swing.JButton selectGCTFileButton;

    private javax.swing.JTextArea Dataset1FileNameTextField;
    private javax.swing.JButton selectDataset1FileButton;

    private javax.swing.JTextArea Dataset2FileNameTextField;
    private javax.swing.JButton selectDataset2FileButton;

    private javax.swing.JTextField pvalueTextField;
    private javax.swing.JTextField qvalueTextField;
    private javax.swing.JTextField jaccardTextField; 

    private boolean Dataset1FileSelected = false;
    private boolean GMTFileSelected = false;
    private boolean GCTFileSelected = false;

    private EnrichmentMapParameters params;


    /** Creates new form AttributeMatrixImportDialog */
    public InputFilesPanel(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
	    status = false;
	    pack();
    }

    public void initComponents() {

        javax.swing.JLabel titleLabel;
        javax.swing.JLabel pvalueLabel;
        javax.swing.JLabel qvalueLabel;
        javax.swing.JLabel jaccardLabel;
        params = new EnrichmentMapParameters();

        titleLabel = new javax.swing.JLabel();
        pvalueLabel = new javax.swing.JLabel();
        qvalueLabel = new javax.swing.JLabel();
        jaccardLabel = new javax.swing.JLabel();

        //text boxes
        GMTFileNameTextField = new javax.swing.JTextField();
        GCTFileNameTextField = new javax.swing.JTextField();
        Dataset1FileNameTextField = new javax.swing.JTextArea();
        Dataset2FileNameTextField = new javax.swing.JTextArea();
        pvalueTextField = new javax.swing.JTextField();
        qvalueTextField = new javax.swing.JTextField();
        jaccardTextField = new javax.swing.JTextField();

        //buttons
        selectGMTFileButton = new javax.swing.JButton();
        selectGCTFileButton = new javax.swing.JButton();
        selectDataset1FileButton = new javax.swing.JButton();
        selectDataset2FileButton = new javax.swing.JButton();

        cancelButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();


        titleLabel.setFont(new java.awt.Font("Dialog", 1, 14));
        titleLabel.setText("Import GSEA files used to calculate enrichment maps");


        //components needed for the GMT file load
        GMTFileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        GMTFileNameTextField.setText("Please select a geneset (.gmt) file...");

        selectGMTFileButton.setText("Select");
        selectGMTFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectGMTFileButtonActionPerformed(evt);
                    }
                });

        //components needed for the GMT file load
        GCTFileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        GCTFileNameTextField.setText("Please select the dataset (.gct) file used for GSEA analysis...");

        selectGCTFileButton.setText("Select");
        selectGCTFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectGCTFileButtonActionPerformed(evt);
                    }
                });

        //components needed for the GSEA Up file
        Dataset1FileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset1FileNameTextField.setText("Please select the gsea result files for first dataset...");
        Dataset1FileNameTextField.setLineWrap(true);
        Dataset1FileNameTextField.setRows(2);

        selectDataset1FileButton.setText("Select");
        selectDataset1FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset1FileButtonActionPerformed(evt);
                    }
                });


        //components needed for the GSEA Up file
        Dataset2FileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset2FileNameTextField.setText("Please select the gsea result files for second dataset...");
        Dataset2FileNameTextField.setLineWrap(true);
        Dataset2FileNameTextField.setRows(2);

        selectDataset2FileButton.setText("Select");
        selectDataset2FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset2FileButtonActionPerformed(evt);
                    }
                });



        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        importButton.setText("Build Enrichment Map");
        importButton.addActionListener(new BuildEnrichmentMapActionListener(this,params));
	    importButton.setEnabled(false);

        // Layout with GridBagLayout.
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        int current_row = 0;

        c.weighty = 1;
        c.weightx = 1;
        c.insets = new Insets(5,5,10,10);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = current_row;
        gridbag.setConstraints(titleLabel, c);
        add(titleLabel);
        current_row++;

        //put in the fields for the genesets file
        c.gridx = 0;
        c.gridy = current_row;
        gridbag.setConstraints(GMTFileNameTextField, c);
        add(GMTFileNameTextField);
        c.gridx = 1;
        gridbag.setConstraints(selectGMTFileButton, c);
        add(selectGMTFileButton);
        current_row++;

         //put in the fields for the gsea dataset file
        c.gridx = 0;
        c.gridy = current_row;
        gridbag.setConstraints(GCTFileNameTextField, c);
        add(GCTFileNameTextField);
        c.gridx = 1;
        gridbag.setConstraints(selectGCTFileButton, c);
        add(selectGCTFileButton);
        current_row++;

        //put in the fields for the GSEA UP results file
        c.gridx = 0;
        c.gridy = current_row;
        gridbag.setConstraints(Dataset1FileNameTextField, c);
        add(Dataset1FileNameTextField);
        c.gridx = 1;
        gridbag.setConstraints(selectDataset1FileButton, c);
        add(selectDataset1FileButton);
        current_row++;

        //put in the fields for the GSEA DOWN results file.
        c.gridx = 0;
        c.gridy = current_row;
        gridbag.setConstraints(Dataset2FileNameTextField, c);
        add(Dataset2FileNameTextField);
        c.gridx = 1;
        gridbag.setConstraints(selectDataset2FileButton, c);
        add(selectDataset2FileButton);
        current_row++;

        //put the fields to set p-value
        c.gridx = 0;
        c.gridy = current_row;
        pvalueLabel.setFont(new java.awt.Font("Dialog", 1, 10));
        pvalueLabel.setText("P-value cut-off (Only genesets with p-value less than this value will be included)");
        pvalueTextField.setText("0.05");
        gridbag.setConstraints(pvalueLabel, c);
        add(pvalueLabel);
        c.gridx = 1;
        gridbag.setConstraints(pvalueTextField, c);
        add(pvalueTextField);
        current_row++;

        //put the fields to set q-value
        c.gridx = 0;
        c.gridy = current_row;
        qvalueLabel.setFont(new java.awt.Font("Dialog", 1, 10));
        qvalueLabel.setText("FDR Q-value cut-off (Only genesets with fdr q-value less than this value will be included)");
        qvalueTextField.setText("0.25");
        gridbag.setConstraints(qvalueLabel, c);
        add(qvalueLabel);
        c.gridx = 1;
        gridbag.setConstraints(qvalueTextField, c);
        add(qvalueTextField);
        current_row++;


        //put the fields to set q-value
        c.gridx = 0;
        c.gridy = current_row;
        jaccardLabel.setFont(new java.awt.Font("Dialog", 1, 10));
        jaccardLabel.setText("Jaccard Coeffecient (only edges that exceed this cutoff will be included)");
        jaccardTextField.setText("0.50");
        gridbag.setConstraints(jaccardLabel, c);
        add(jaccardLabel);
        c.gridx = 1;
        gridbag.setConstraints(jaccardTextField, c);
        add(jaccardTextField);
        current_row++;

        //put in the cancel and import buttons.
        c.gridx = 0;
        c.gridy = current_row;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(cancelButton, c);
        add(cancelButton);
        c.gridx = 1;
        gridbag.setConstraints(importButton, c);
        add(importButton);


    }


    public double getPvalue() {
        try{
            return  Double.parseDouble(pvalueTextField.getText());
        } catch (NumberFormatException nfe) {
            return -1;
        }

    }

    public double getQvalue() {
        try{
            return  Double.parseDouble(qvalueTextField.getText());
        }catch (NumberFormatException nfe) {
            return -1;
        }
    }

    public double getJaccard() {
        try{
            return  Double.parseDouble(jaccardTextField.getText());
        } catch (NumberFormatException nfe) {
            return -1;
        }

    }



    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
           // TODO add your handling code here:
           status = false;
            this.dispose();
       }



       private void selectGMTFileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
           CyFileFilter filter = new CyFileFilter();

           // Add accepted File Extensions
           filter.addExtension("gmt");
           filter.setDescription("All GMT files");

           // Get the file name
           File file = FileUtil.getFile("Import GMT File", FileUtil.LOAD,
                        new CyFileFilter[] { filter });
           if(file != null) {
               GMTFileNameTextField.setText(file.getAbsolutePath());
               params.setGMTFileName(file.getAbsolutePath());
               GMTFileNameTextField.setToolTipText(file.getAbsolutePath());
               GMTFileSelected = true;
                if (Dataset1FileSelected && GCTFileSelected  ){
                    importButton.setEnabled(true);
               }
           }
       }

         private void selectGCTFileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
           CyFileFilter filter = new CyFileFilter();

           // Add accepted File Extensions
           filter.addExtension("gct");
           filter.addExtension("txt");
           filter.setDescription("All GCT files");

           // Get the file name
           File file = FileUtil.getFile("Import GCT File", FileUtil.LOAD,
                        new CyFileFilter[] { filter });
           if(file != null) {
               GCTFileNameTextField.setText(file.getAbsolutePath());
               params.setGCTFileName(file.getAbsolutePath());
               GCTFileNameTextField.setToolTipText(file.getAbsolutePath());
               GCTFileSelected = true;
                if (Dataset1FileSelected && GMTFileSelected   ){
                    importButton.setEnabled(true);
               }
           }
       }

        private void selectDataset1FileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
           CyFileFilter filter = new CyFileFilter();

           // Add accepted File Extensions
           filter.addExtension("txt");
           filter.addExtension("xls");
           filter.setDescription("All GSEA result files");

           // Get the file name
            File files[] = FileUtil.getFiles("import GSEA dataset 1 result files", FileUtil.LOAD, new CyFileFilter[]{ filter });


           if(files != null) {
               //There should be two files inputted.  If it more or less then report error
               if(files.length == 2){
                    Dataset1FileNameTextField.setText(files[0].getName() + "\n" + files[1].getName());
                    params.setGSEADataset1FileName1(files[0].getAbsolutePath());
                    params.setGSEADataset1FileName2(files[1].getAbsolutePath());
                    Dataset1FileNameTextField.setToolTipText(files[0].getAbsolutePath() + "," + files[1].getAbsolutePath());
                    Dataset1FileSelected = true;
                    if ( GMTFileSelected && GCTFileSelected){
                        importButton.setEnabled(true);
                    }
               }
           }
       }


        private void selectDataset2FileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
           CyFileFilter filter = new CyFileFilter();

            // Add accepted File Extensions
           filter.addExtension("txt");
           filter.addExtension("xls");
           filter.setDescription("All GSEA results files");

           // Get the file name
           File files[] = FileUtil.getFiles("Import GSEA Dataset 2 result Files", FileUtil.LOAD,
                        new CyFileFilter[] { filter });
           if(files != null) {
               if(files.length == 2){
                    Dataset2FileNameTextField.setText(files[0].getName() + "\n" + files[1].getName());
                    params.setGSEADataset2FileName1(files[0].getAbsolutePath());
                    params.setGSEADataset2FileName2(files[1].getAbsolutePath());
                    params.setTwoDatasets(true);
                    Dataset2FileNameTextField.setToolTipText(files[0].getAbsolutePath() + "," + files[1].getAbsolutePath());
                    if (Dataset1FileSelected &&  GMTFileSelected && GCTFileSelected){
                        importButton.setEnabled(true);
                    }
               }
           }
       }
}
