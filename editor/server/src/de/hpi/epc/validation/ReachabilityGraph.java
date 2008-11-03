package de.hpi.epc.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import de.hpi.diagram.Diagram;
import de.hpi.diagram.DiagramNode;
import de.hpi.epc.Marking;
import de.hpi.epc.Marking.NodeNewMarkingPair;

/*
 * TODO title thesis?
 * Implementation as proposed by Jan Mendling, p. 90 
 */
public class ReachabilityGraph {
	Diagram diag;
	
	public ReachabilityGraph(Diagram diag){
		this.diag = diag;
	}
	
	public void calculate(Marking marking){
		Stack<Marking> toBePropagated = new Stack<Marking>();
		toBePropagated.push(marking);
		
		Set<Marking> propagated = new HashSet<Marking>();
		
		while(!toBePropagated.isEmpty()){
			Marking currentMarking = toBePropagated.pop();
			Marking oldMarking = currentMarking.clone();
			List<NodeNewMarkingPair> nodeNewMarkings = currentMarking.propagate(diag);
			propagated.add(oldMarking);
			for(NodeNewMarkingPair nodeNewMarking : nodeNewMarkings){
				add(oldMarking, nodeNewMarking.node, nodeNewMarking.newMarking);
				if(!propagated.contains(nodeNewMarking.newMarking)){
					toBePropagated.push(nodeNewMarking.newMarking);
				}
			}
		}
	}
	
	public void add(Marking fromMarking, DiagramNode node, Marking toMarking){
		
	}
}