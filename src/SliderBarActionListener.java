
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.data.CyAttributes;

import java.util.Iterator;
import java.util.ArrayList;

import giny.model.Node;
import giny.model.Edge;
import giny.view.NodeView;
import giny.view.EdgeView;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * Created by
 * User: risserlin
 * Date: Feb 24, 2009
 * Time: 3:29:39 PM
 */
public class SliderBarActionListener implements ChangeListener {


    private SliderBarPanel panel;
    private NumberRangeModel rangeModel;
    private EnrichmentMapParameters params;
    //private CyNetwork network;
    //private int[] nodes;
    private ArrayList<HiddenNodes> hiddenNodes;
    private ArrayList<Edge> hiddenEdges;

    private String attrib_dataset1;
    private String attrib_dataset2;

    public SliderBarActionListener(SliderBarPanel panel, EnrichmentMapParameters params, String attrib1, String attrib2) {
        this.panel = panel;
        this.params = params;
        rangeModel = panel.getRangeModel();
        hiddenNodes = new ArrayList();
        hiddenEdges = new ArrayList();

        attrib_dataset1 = attrib1;
        attrib_dataset2 = attrib2;
    }

    public void stateChanged(ChangeEvent e){

        Double max_cutoff = (Double) rangeModel.getHighValue();
        Double min_cutoff = (Double) rangeModel.getLowValue();
        CyNetwork network = Cytoscape.getCurrentNetwork();
        CyNetworkView view = Cytoscape.getCurrentNetworkView();
        CyAttributes attributes = Cytoscape.getNodeAttributes();

        int[] nodes = network.getNodeIndicesArray();

       //get the prefix of the current network
       String prefix = network.getTitle().split("_")[0] + "_";

        /*There are two different ways to hide and restore nodes.
        *if you hide the nodes from the view perspective the node is still in the underlying
        * network but it is just not visible.  So when the node is restored it is in the exact
        * same state and location.  The only problem with this way is if you try and re-layout
        * your network it behaves as if all the nodes that are hidden are still there and the layout
        * does not change.
        * if you hide the node from the network perspective the node is deleted from the network
        * and when the node is restored (granted that you have tracked references of the "hidden"
        * nodes and edges) it restored to the top right of the panel and the user is required to
        * relayout the nodes. (can also be done programmatically)
        */

 /*       for(int i = 0; i< nodes.length; i++){
           Node currentNode = network.getNode(nodes[i]);
           NodeView currentView = Cytoscape.getCurrentNetworkView().getNodeView(currentNode);
           Double pvalue_dataset1 = attributes.getDoubleAttribute(currentNode.getIdentifier(), prefix + attrib_dataset1);

           if((pvalue_dataset1 > max_cutoff) || (pvalue_dataset1 < min_cutoff)){
               if(params.isTwoDatasets()){
                   Double pvalue_dataset2 = attributes.getDoubleAttribute(currentNode.getIdentifier(), prefix + attrib_dataset2);
                  if((pvalue_dataset2 > max_cutoff) || (pvalue_dataset2 < min_cutoff)){
                        view.hideGraphObject(currentView);
                  }
                   else{
                      view.showGraphObject(currentView);
                      //restore the edges as well
                      int edges[] = network.getAdjacentEdgeIndicesArray(currentNode.getRootGraphIndex(),true,true,true);
                      for(int m = 0;m< edges.length;m++){
                          EdgeView currentEdgeView = view.getEdgeView(edges[m]);
                          view.showGraphObject(currentEdgeView);
                      }

                  }

               }
               else{
                   view.hideGraphObject(currentView);
               }
            }
            else{
               view.showGraphObject(currentView);
               //restore the edges as well
               int edges[] = network.getAdjacentEdgeIndicesArray(currentNode.getRootGraphIndex(),true,true,true);
               for(int m = 0;m< edges.length;m++){
                   EdgeView currentEdgeView = view.getEdgeView(edges[m]);
                   view.showGraphObject(currentEdgeView);
               }
           }
       }
*/

       //go through all the existing nodes to see if we need to hide any new nodes.
       for(int i = 0; i< nodes.length; i++){
           Node currentNode = network.getNode(nodes[i]);
           NodeView currentView = view.getNodeView(currentNode);
           Double pvalue_dataset1 = attributes.getDoubleAttribute(currentNode.getIdentifier(), prefix + attrib_dataset1);

           if((pvalue_dataset1 > max_cutoff) || (pvalue_dataset1 < min_cutoff)){
               if(params.isTwoDatasets()){
                   Double pvalue_dataset2 = attributes.getDoubleAttribute(currentNode.getIdentifier(), prefix + attrib_dataset2);
                  if((pvalue_dataset2 > max_cutoff) || (pvalue_dataset2 < min_cutoff)){

                        int edges[] = network.getAdjacentEdgeIndicesArray(currentNode.getRootGraphIndex(),true,true,true);
                        for(int m = 0;m< edges.length;m++)
                            hiddenEdges.add(network.getEdge(edges[m]));

                        hiddenNodes.add(new HiddenNodes(currentNode, currentView.getXPosition(), currentView.getYPosition()));
                        network.hideNode(currentNode);
                  }

               }
               else{
                   int edges[] = network.getAdjacentEdgeIndicesArray(currentNode.getRootGraphIndex(),true,true,true);
                   for(int m = 0;m< edges.length;m++)
                            hiddenEdges.add(network.getEdge(edges[m]));
                   hiddenNodes.add(new HiddenNodes(currentNode, currentView.getXPosition(), currentView.getYPosition()));
                   network.hideNode(currentNode);
                  }
           }
       }

        //go through all the hidden nodes to see if we need to restore any of them
        ArrayList<HiddenNodes> unhiddenNodes = new ArrayList();
        ArrayList<Edge> unhiddenEdges = new ArrayList();

        for(Iterator j = hiddenNodes.iterator();j.hasNext();){
            HiddenNodes currentHN = (HiddenNodes)j.next();
            Node currentNode = currentHN.getNode();
            Double pvalue_dataset1 = attributes.getDoubleAttribute(currentNode.getIdentifier(), prefix + attrib_dataset1);

            if((pvalue_dataset1 <= max_cutoff) && (pvalue_dataset1 >= min_cutoff)){

                network.restoreNode(currentNode);
                NodeView currentNodeView = view.getNodeView(currentNode);
                currentNodeView.setXPosition(currentHN.getX());
                currentNodeView.setYPosition(currentHN.getY());
                view.updateView();
                unhiddenNodes.add(currentHN);



            }
            if(params.isTwoDatasets()){
                   Double pvalue_dataset2 = attributes.getDoubleAttribute(currentNode.getIdentifier(), prefix + attrib_dataset2);
                  if((pvalue_dataset2 <= max_cutoff) && (pvalue_dataset2 >= min_cutoff)){
                        network.restoreNode(currentNode);
                        NodeView currentNodeView = view.getNodeView(currentNode);
                        currentNodeView.setXPosition(currentHN.getX());
                        currentNodeView.setYPosition(currentHN.getY());
                        unhiddenNodes.add(currentHN);


                  }
           }

        }

        //For the unhidden edges we need to restore its edges with nodes that exist in the network
        //restore edges where both nodes are in the network.
         for(Iterator k = hiddenEdges.iterator();k.hasNext();){
            Edge currentEdge = (Edge)k.next();
            if((network.getNode(currentEdge.getSource().getRootGraphIndex()) != null) &&
                (network.getNode(currentEdge.getTarget().getRootGraphIndex()) != null)){
                network.restoreEdge(currentEdge);
                unhiddenEdges.add(currentEdge);
            }
         }

        //remove the unhidden nodes from the list of hiddenNodes.
        for(Iterator k = unhiddenNodes.iterator();k.hasNext();)
            hiddenNodes.remove(k.next());

        //remove the unhidden edges from the list of hiddenEdges.
        for(Iterator k = unhiddenEdges.iterator();k.hasNext();)
            hiddenEdges.remove(k.next());

        view.redrawGraph(true,true);
        view.updateView();

   }


    private class HiddenNodes{
           Node node;
           double x;
           double y;

           public HiddenNodes(Node node, double x , double y){
               this.node = node;
               this.x = x;
               this.y = y;
           }

        public Node getNode() {
            return node;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

    }

}